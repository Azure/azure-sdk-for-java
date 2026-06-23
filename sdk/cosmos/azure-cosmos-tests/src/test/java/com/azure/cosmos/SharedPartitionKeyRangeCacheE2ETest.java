// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.caches.AsyncCacheNonBlocking;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.caches.SharedPartitionKeyRangeCacheRegistry;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.TestSuiteBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests for {@link SharedPartitionKeyRangeCacheRegistry}: spin up real
 * {@link CosmosAsyncClient} instances, perform partition-key-routed operations to
 * populate the routing-map cache, and verify the registry's sharing semantics.
 *
 * <p>Sharing is keyed by the service endpoint {@link URI} configured on
 * {@link CosmosClientBuilder}. Two clients configured with the same endpoint
 * URI share the cache; clients configured with different endpoint URIs (e.g.
 * the global endpoint vs a regional endpoint of the same logical account) do
 * <b>not</b> share — see {@link SharedPartitionKeyRangeCacheRegistry} javadoc
 * for the rationale.</p>
 */
public class SharedPartitionKeyRangeCacheE2ETest extends TestSuiteBase {
    private static final Logger logger = LoggerFactory.getLogger(SharedPartitionKeyRangeCacheE2ETest.class);

    private static final int TIMEOUT = 90_000;
    private static final int SETUP_TIMEOUT = 60_000;
    private static final int SHUTDOWN_TIMEOUT = 30_000;

    private CosmosAsyncClient setupClient;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;
    private URI serviceEndpoint;

    @Factory(dataProvider = "simpleGatewayClient")
    public SharedPartitionKeyRangeCacheE2ETest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"emulator", "fast"}, timeOut = SETUP_TIMEOUT)
    public void before() {
        this.setupClient = getClientBuilder().buildAsyncClient();
        this.database = getSharedCosmosDatabase(this.setupClient);

        String containerId = "pkr-share-e2e-" + UUID.randomUUID();
        CosmosContainerProperties properties =
            new CosmosContainerProperties(containerId, "/mypk");
        this.database
            .createContainer(properties, ThroughputProperties.createManualThroughput(400))
            .block();
        this.container = this.database.getContainer(containerId);

        this.serviceEndpoint = serviceEndpointOf(this.setupClient);
        assertThat(this.serviceEndpoint)
            .as("service endpoint must be available after client init")
            .isNotNull();
    }

    @AfterClass(groups = {"emulator", "fast"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void after() {
        if (this.container != null) {
            try {
                this.container.delete().block();
            } catch (Exception e) {
                logger.warn("Failed to delete e2e container", e);
            }
        }
        safeClose(this.setupClient);
    }

    /**
     * Two {@link CosmosAsyncClient} instances configured with the same service
     * endpoint must share the underlying {@link AsyncCacheNonBlocking} routing-map
     * storage, and the registry refcount must reflect both holders.
     */
    @Test(groups = {"emulator", "fast"}, timeOut = TIMEOUT)
    public void twoClientsOnSameEndpointShareRoutingMapStorage() {
        CosmosAsyncClient clientA = null;
        CosmosAsyncClient clientB = null;
        try {
            clientA = getClientBuilder().buildAsyncClient();
            clientB = getClientBuilder().buildAsyncClient();

            // Trigger PK-routed operations on both clients so the routing-map cache populates.
            TestObject seed = TestObject.create();
            createItem(clientA, seed);
            readItemSilently(clientA, seed.getMypk());
            readItemSilently(clientB, seed.getMypk());

            AsyncCacheNonBlocking<String, CollectionRoutingMap> storageA = routingMapStorageOf(clientA);
            AsyncCacheNonBlocking<String, CollectionRoutingMap> storageB = routingMapStorageOf(clientB);

            assertThat(storageA)
                .as("Two CosmosAsyncClients on the same endpoint must share the routing-map AsyncCacheNonBlocking instance")
                .isSameAs(storageB);

            int refCount = registryReferenceCount(this.serviceEndpoint);
            assertThat(refCount)
                .as("Registry refcount for endpoint [%s] must include both clients", this.serviceEndpoint)
                .isGreaterThanOrEqualTo(2);

            ConcurrentHashMap<String, ?> values =
                ReflectionUtils.getValueMapNonBlockingCache(storageA);
            assertThat(values)
                .as("Routing-map cache must contain at least one entry after PK-routed reads")
                .isNotEmpty();
        } finally {
            int refCountBeforeClose = registryReferenceCount(this.serviceEndpoint);
            safeClose(clientA);
            int refCountAfterFirstClose = registryReferenceCount(this.serviceEndpoint);
            assertThat(refCountAfterFirstClose)
                .as("Closing one client must drop the registry refcount by exactly one")
                .isEqualTo(refCountBeforeClose - 1);

            safeClose(clientB);
            int refCountAfterSecondClose = registryReferenceCount(this.serviceEndpoint);
            assertThat(refCountAfterSecondClose)
                .as("Closing both test clients must drop refcount by two (setup client may still hold a reference)")
                .isEqualTo(refCountBeforeClose - 2);
        }
    }

    /**
     * Negative-case companion to {@link #twoClientsOnSameEndpointShareRoutingMapStorage()}:
     * clients configured with different endpoint URIs (for example, the global endpoint
     * versus a regional endpoint of the same logical account) must <em>not</em> share
     * the cache. The registry intentionally keys on the URI as configured on the builder,
     * not on the underlying Cosmos account, because the account id returned from regional
     * endpoints embeds a service-normalised region suffix that cannot be reliably
     * reversed. This test pins that contract.
     */
    @Test(groups = {"emulator", "fast"}, timeOut = TIMEOUT)
    public void clientsOnDifferentEndpointsDoNotShareRoutingMapStorage() {
        // Construct a syntactically-different endpoint URI by toggling host case.
        // URI.equals is case-insensitive on host (RFC 3986), so we need a more substantive
        // difference: alter the path. The registry uses URI.equals which keys on scheme,
        // host (case-insensitive), port, AND path, so a non-empty distinct path produces
        // a distinct registry key without changing where the request actually goes.
        // For an even cleaner separation we just point client B at an alternate endpoint
        // string by replacing the host with itself plus a non-routable suffix; both
        // clients still talk to the same logical account but the URIs differ.
        URI primaryEndpoint = this.serviceEndpoint;
        URI alternateEndpoint = URI.create(primaryEndpoint.toString().replaceFirst("/$", "/alt/"));

        CosmosAsyncClient clientA = null;
        CosmosAsyncClient clientB = null;
        try {
            clientA = getClientBuilder().endpoint(primaryEndpoint.toString()).buildAsyncClient();
            clientB = getClientBuilder().endpoint(alternateEndpoint.toString()).buildAsyncClient();

            AsyncCacheNonBlocking<String, CollectionRoutingMap> storageA = routingMapStorageOf(clientA);
            AsyncCacheNonBlocking<String, CollectionRoutingMap> storageB = routingMapStorageOf(clientB);

            assertThat(storageA)
                .as("Clients configured with different endpoint URIs must use distinct registry entries")
                .isNotSameAs(storageB);
        } finally {
            safeClose(clientA);
            safeClose(clientB);
            // Restore the shared builder's endpoint so later tests aren't poisoned.
            getClientBuilder().endpoint(primaryEndpoint.toString());
        }
    }

    // --- helpers ----------------------------------------------------------------

    private void createItem(CosmosAsyncClient client, TestObject item) {
        CosmosAsyncContainer c = client
            .getDatabase(this.database.getId())
            .getContainer(this.container.getId());
        c.createItem(item, new PartitionKey(item.getMypk()), new CosmosItemRequestOptions()).block();
    }

    private void readItemSilently(CosmosAsyncClient client, String pk) {
        // The cache is populated by the resolve step regardless of whether the doc exists;
        // we issue a random-id read and tolerate 404.
        CosmosAsyncContainer c = client
            .getDatabase(this.database.getId())
            .getContainer(this.container.getId());
        try {
            CosmosItemResponse<TestObject> resp = c.readItem(
                UUID.randomUUID().toString(),
                new PartitionKey(pk),
                new CosmosItemRequestOptions(),
                TestObject.class).block();
            assertThat(resp).isNotNull();
        } catch (CosmosException ex) {
            if (ex.getStatusCode() != 404) {
                throw ex;
            }
        }
    }

    private static AsyncCacheNonBlocking<String, CollectionRoutingMap> routingMapStorageOf(CosmosAsyncClient client) {
        RxDocumentClientImpl rxDocumentClient =
            (RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(client);
        RxPartitionKeyRangeCache partitionKeyRangeCache = rxDocumentClient.getPartitionKeyRangeCache();
        return ReflectionUtils.getRoutingMapAsyncCacheNonBlocking(partitionKeyRangeCache);
    }

    private static URI serviceEndpointOf(CosmosAsyncClient client) {
        RxDocumentClientImpl rxDocumentClient =
            (RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(client);
        return rxDocumentClient.getServiceEndpoint();
    }

    /**
     * The registry's {@code referenceCount} accessor is package-private (test-only).
     * Reflect into it from this package; widening visibility for a test-only check
     * would pollute the implementation class's surface.
     */
    private static int registryReferenceCount(URI endpoint) {
        try {
            SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
            Method m = SharedPartitionKeyRangeCacheRegistry.class.getDeclaredMethod("referenceCount", URI.class);
            m.setAccessible(true);
            return (Integer) m.invoke(registry, endpoint);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to reflect SharedPartitionKeyRangeCacheRegistry.referenceCount", e);
        }
    }
}
