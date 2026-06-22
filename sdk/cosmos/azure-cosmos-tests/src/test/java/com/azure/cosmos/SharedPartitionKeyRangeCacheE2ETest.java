// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
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
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests for {@link SharedPartitionKeyRangeCacheRegistry}: spin up real
 * {@link CosmosAsyncClient} instances, do a few partition-key-routed operations
 * to populate the routing-map cache, and verify cross-client sharing semantics.
 *
 * <p>These tests run against the configured Cosmos endpoint
 * ({@code TestConfigurations.HOST}). The regional-endpoint test is skipped when
 * the account exposes fewer than two readable locations (single-region accounts /
 * emulator).</p>
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

    public SharedPartitionKeyRangeCacheE2ETest() {
        super();
    }

    @BeforeClass(groups = {"emulator", "fast"}, timeOut = SETUP_TIMEOUT)
    public void before() {
        this.setupClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildAsyncClient();
        this.database = getSharedCosmosDatabase(this.setupClient);

        String containerId = "pkr-share-e2e-" + UUID.randomUUID();
        CosmosContainerProperties properties =
            new CosmosContainerProperties(containerId, "/pk");
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
            clientA = newClient(TestConfigurations.HOST);
            clientB = newClient(TestConfigurations.HOST);

            // Trigger a PK-routed read on both clients to force the routing-map cache to populate.
            String pk = UUID.randomUUID().toString();
            createDoc(clientA, pk);
            readDocSilently(clientA, pk);
            readDocSilently(clientB, pk);

            AsyncCacheNonBlocking<String, CollectionRoutingMap> storageA = routingMapStorageOf(clientA);
            AsyncCacheNonBlocking<String, CollectionRoutingMap> storageB = routingMapStorageOf(clientB);

            assertThat(storageA)
                .as("Two CosmosAsyncClients on the same account must share the routing-map AsyncCacheNonBlocking instance")
                .isSameAs(storageB);

            int refCount = registryReferenceCount(this.accountId);
            assertThat(refCount)
                .as("Registry refcount for account [%s] must include both clients", this.accountId)
                .isGreaterThanOrEqualTo(2);

            // The cached value-map must contain the e2e container's RID after the reads.
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

        CosmosAsyncClient globalClient = null;
        CosmosAsyncClient regionalClient = null;
        try {
            globalClient = newClient(TestConfigurations.HOST);
            regionalClient = newClient(regionalEndpoint);

            String pk = UUID.randomUUID().toString();
            createDoc(globalClient, pk);
            readDocSilently(globalClient, pk);
            readDocSilently(regionalClient, pk);

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
        }
    }

    // --- helpers ----------------------------------------------------------------

    private CosmosAsyncClient newClient(String endpoint) {
        return new CosmosClientBuilder()
            .endpoint(endpoint)
            .key(TestConfigurations.MASTER_KEY)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildAsyncClient();
    }

    private void createDoc(CosmosAsyncClient client, String pk) {
        CosmosAsyncContainer c = client
            .getDatabase(this.database.getId())
            .getContainer(this.container.getId());
        Map<String, Object> doc = new HashMap<>();
        String id = UUID.randomUUID().toString();
        doc.put("id", id);
        doc.put("pk", pk);
        c.createItem(doc, new PartitionKey(pk), new CosmosItemRequestOptions()).block();
    }

    private void readDocSilently(CosmosAsyncClient client, String pk) {
        // We don't care whether the doc exists for the cache to populate — only that the request
        // resolves through the partition key range cache. Use a random id; ignore 404s.
        CosmosAsyncContainer c = client
            .getDatabase(this.database.getId())
            .getContainer(this.container.getId());
        try {
            CosmosItemResponse<Map> resp = c.readItem(
                UUID.randomUUID().toString(),
                new PartitionKey(pk),
                new CosmosItemRequestOptions(),
                Map.class).block();
            // 200 path
            assertThat(resp).isNotNull();
        } catch (CosmosException ex) {
            if (ex.getStatusCode() != 404) {
                throw ex;
            }
            // 404 expected (random id); routing-map cache still populated.
        }
    }

    private static AsyncCacheNonBlocking<String, CollectionRoutingMap> routingMapStorageOf(CosmosAsyncClient client) {
        AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(client);
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
        RxPartitionKeyRangeCache partitionKeyRangeCache =
            ReflectionUtils.getPartitionKeyRangeCache(rxDocumentClient);
        return ReflectionUtils.getRoutingMapAsyncCacheNonBlocking(partitionKeyRangeCache);
    }

    private static String getAccountId(CosmosAsyncClient client) {
        AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(client);
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
        GlobalEndpointManager gem = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
        DatabaseAccount account = gem.getLatestDatabaseAccount();
        assertThat(account).as("globalEndpointManager.getLatestDatabaseAccount()").isNotNull();
        return account.getId();
    }

    private static List<String> readableRegionalEndpoints(CosmosAsyncClient client) {
        AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(client);
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
        GlobalEndpointManager gem = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
        DatabaseAccount account = gem.getLatestDatabaseAccount();
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
     * Reflect into it from this package; widening visibility for an e2e test would
     * pollute the public surface of an {@code implementation} class.
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
