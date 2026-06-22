// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
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
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests for {@link SharedPartitionKeyRangeCacheRegistry}: spin up real
 * {@link CosmosAsyncClient} instances, perform partition-key-routed operations to
 * populate the routing-map cache, and verify cross-client sharing semantics.
 *
 * <p>Runs against the endpoint configured in {@link TestConfigurations}. The
 * regional-endpoint test is skipped when the account exposes fewer than two
 * distinct readable locations (single-region accounts / emulator).</p>
 */
public class SharedPartitionKeyRangeCacheE2ETest extends TestSuiteBase {
    private static final Logger logger = LoggerFactory.getLogger(SharedPartitionKeyRangeCacheE2ETest.class);

    private static final int TIMEOUT = 90_000;
    private static final int SETUP_TIMEOUT = 60_000;
    private static final int SHUTDOWN_TIMEOUT = 30_000;

    private CosmosAsyncClient setupClient;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;
    private String accountId;

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

        this.accountId = getAccountId(this.setupClient);
        assertThat(this.accountId)
            .as("Cosmos account id must be available after client init")
            .isNotBlank();
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
     * Two {@link CosmosAsyncClient} instances targeting the same Cosmos account
     * must share the underlying {@link AsyncCacheNonBlocking} routing-map storage,
     * and the registry refcount must reflect both holders.
     */
    @Test(groups = {"emulator", "fast"}, timeOut = TIMEOUT)
    public void twoClientsOnSameAccountShareRoutingMapStorage() {
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
                .as("Two CosmosAsyncClients on the same account must share the routing-map AsyncCacheNonBlocking instance")
                .isSameAs(storageB);

            int refCount = registryReferenceCount(this.accountId);
            assertThat(refCount)
                .as("Registry refcount for account [%s] must include both clients", this.accountId)
                .isGreaterThanOrEqualTo(2);

            ConcurrentHashMap<String, ?> values =
                ReflectionUtils.getValueMapNonBlockingCache(storageA);
            assertThat(values)
                .as("Routing-map cache must contain at least one entry after PK-routed reads")
                .isNotEmpty();
        } finally {
            int refCountBeforeClose = registryReferenceCount(this.accountId);
            safeClose(clientA);
            int refCountAfterFirstClose = registryReferenceCount(this.accountId);
            assertThat(refCountAfterFirstClose)
                .as("Closing one client must drop the registry refcount by exactly one")
                .isEqualTo(refCountBeforeClose - 1);

            safeClose(clientB);
            int refCountAfterSecondClose = registryReferenceCount(this.accountId);
            assertThat(refCountAfterSecondClose)
                .as("Closing both test clients must drop refcount by two (setup client may still hold a reference)")
                .isEqualTo(refCountBeforeClose - 2);
        }
    }

    /**
     * Motivating case: a client built with a regional endpoint (e.g.
     * {@code my-acct-eastus.documents.azure.com}) must still share the cache
     * entry with a client built with the global endpoint (e.g.
     * {@code my-acct.documents.azure.com}) because the registry key is the
     * Cosmos database account id, not the service-endpoint URI.
     */
    @Test(groups = {"emulator", "fast"}, timeOut = TIMEOUT)
    public void clientUsingRegionalEndpointSharesCacheWithClientUsingGlobalEndpoint() {
        List<String> regionalEndpoints = readableRegionalEndpoints(this.setupClient);
        if (regionalEndpoints.isEmpty()) {
            throw new SkipException(
                "No readable regional locations exposed by the account; skipping regional-endpoint sharing test.");
        }
        String regionalEndpoint = regionalEndpoints.get(0);
        if (regionalEndpoint.equals(TestConfigurations.HOST)) {
            // Single-region or aliased account where the regional endpoint IS the global endpoint.
            // The test would degenerate into the same-endpoint case already covered above.
            throw new SkipException(
                "First readable regional endpoint matches the global endpoint [" + regionalEndpoint
                    + "]; sharing across distinct endpoint URIs cannot be exercised here.");
        }

        logger.info("Global endpoint: [{}], regional endpoint: [{}]", TestConfigurations.HOST, regionalEndpoint);

        String originalEndpoint = TestConfigurations.HOST;
        CosmosAsyncClient globalClient = null;
        CosmosAsyncClient regionalClient = null;
        try {
            globalClient = getClientBuilder().endpoint(originalEndpoint).buildAsyncClient();
            regionalClient = getClientBuilder().endpoint(regionalEndpoint).buildAsyncClient();

            TestObject seed = TestObject.create();
            createItem(globalClient, seed);
            readItemSilently(globalClient, seed.getMypk());
            readItemSilently(regionalClient, seed.getMypk());

            String globalAccountId = getAccountId(globalClient);
            String regionalAccountId = getAccountId(regionalClient);
            assertThat(regionalAccountId)
                .as("Regional and global endpoints must resolve to the same Cosmos account id")
                .isEqualTo(globalAccountId);

            AsyncCacheNonBlocking<String, CollectionRoutingMap> globalStorage = routingMapStorageOf(globalClient);
            AsyncCacheNonBlocking<String, CollectionRoutingMap> regionalStorage = routingMapStorageOf(regionalClient);

            assertThat(globalStorage)
                .as("Regional and global endpoint clients must share the routing-map AsyncCacheNonBlocking instance "
                    + "(keyed by account id, not endpoint URI)")
                .isSameAs(regionalStorage);

            assertThat(registryReferenceCount(globalAccountId))
                .as("Registry refcount for account [%s] must include both global and regional clients",
                    globalAccountId)
                .isGreaterThanOrEqualTo(2);
        } finally {
            safeClose(globalClient);
            safeClose(regionalClient);
            // Restore the shared builder's endpoint so later tests aren't poisoned.
            getClientBuilder().endpoint(originalEndpoint);
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

    private static String getAccountId(CosmosAsyncClient client) {
        RxDocumentClientImpl rxDocumentClient =
            (RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(client);
        DatabaseAccount account = rxDocumentClient.getGlobalEndpointManager().getLatestDatabaseAccount();
        assertThat(account).as("globalEndpointManager.getLatestDatabaseAccount()").isNotNull();
        return account.getId();
    }

    private static List<String> readableRegionalEndpoints(CosmosAsyncClient client) {
        RxDocumentClientImpl rxDocumentClient =
            (RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(client);
        DatabaseAccount account = rxDocumentClient.getGlobalEndpointManager().getLatestDatabaseAccount();
        List<String> endpoints = new ArrayList<>();
        if (account == null) {
            return endpoints;
        }
        Iterable<DatabaseAccountLocation> readable = account.getReadableLocations();
        if (readable == null) {
            return endpoints;
        }
        for (DatabaseAccountLocation loc : readable) {
            if (loc != null && loc.getEndpoint() != null && !loc.getEndpoint().isEmpty()) {
                endpoints.add(loc.getEndpoint());
            }
        }
        return endpoints;
    }

    /**
     * The registry's {@code referenceCount} accessor is package-private (test-only).
     * Reflect into it from this package; widening visibility for a test-only check
     * would pollute the implementation class's surface.
     */
    private static int registryReferenceCount(String accountId) {
        try {
            SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
            Method m = SharedPartitionKeyRangeCacheRegistry.class.getDeclaredMethod("referenceCount", String.class);
            m.setAccessible(true);
            return (Integer) m.invoke(registry, accountId);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to reflect SharedPartitionKeyRangeCacheRegistry.referenceCount", e);
        }
    }
}
