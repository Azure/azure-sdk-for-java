// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.RxDocumentClientImpl;
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
            // The registry refcount for this endpoint is shared with every other client/test that
            // targets it, so in the parallel "fast" suite the absolute count changes under us. The
            // exact close-delta is therefore not asserted here; that wiring is covered
            // deterministically by SharedPartitionKeyRangeCacheRegistryTest. This e2e test's value is
            // proving that two real clients share the same storage instance (asserted above).
            safeClose(clientA);
            safeClose(clientB);
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
