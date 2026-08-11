// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.apachecommons.lang.RandomStringUtils;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.SpatialSpec;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class SerializableAsyncCacheTest {

    @Test(groups = { "unit" }, dataProvider = "numberOfCollections")
    public void serialize_Deserialize_AsyncCache_ViaJson(int cnt) throws Exception {
        // This test exercises the full production serialization/deserialization path
        // through CosmosClientMetadataCachesSnapshot, which now uses JSON.
        AsyncCache<String, DocumentCollection> collectionInfoByNameCache = new AsyncCache<>();

        for (int i = 0; i < cnt; i++) {
            DocumentCollection collectionDef = generateDocumentCollectionDefinition();
            String collectionLink = "db/mydb/colls/" + collectionDef.getId();
            collectionInfoByNameCache.getAsync(collectionLink, null,
                () -> Mono.just(collectionDef)).block();
        }

        ConcurrentHashMap<String, AsyncLazy<DocumentCollection>> originalInternalCache =
            getInternalCache(collectionInfoByNameCache);

        // Serialize through CosmosClientMetadataCachesSnapshot (now uses JSON)
        CosmosClientMetadataCachesSnapshot snapshot = new CosmosClientMetadataCachesSnapshot();
        snapshot.serializeCollectionInfoByNameCache(collectionInfoByNameCache);

        // Deserialize through CosmosClientMetadataCachesSnapshot (JSON parsing + validation)
        AsyncCache<String, DocumentCollection> newAsyncCache = snapshot.getCollectionInfoByNameCache();
        assertThat(newAsyncCache).isNotNull();

        ConcurrentHashMap<String, AsyncLazy<DocumentCollection>> newInternalCache =
            getInternalCache(newAsyncCache);

        assertThat(newInternalCache).hasSize(cnt);

        for (String collectionLink : originalInternalCache.keySet()) {
            DocumentCollection resultFromNewCache = newAsyncCache.getAsync(collectionLink, null,
                () -> Mono.error(new RuntimeException("not expected"))).block();

            DocumentCollection resultFromOldCache = collectionInfoByNameCache.getAsync(collectionLink, null,
                () -> Mono.error(new RuntimeException("not expected"))).block();

            assertThat(resultFromNewCache.toJson()).isEqualTo(resultFromOldCache.toJson());
        }
    }

    @Test(groups = { "unit" })
    public void deserialize_InvalidJson_ReturnsNull() {
        CosmosClientMetadataCachesSnapshot snapshot = new CosmosClientMetadataCachesSnapshot();
        snapshot.collectionInfoByNameCache = "not valid json {{{".getBytes();

        AsyncCache<String, DocumentCollection> result = snapshot.getCollectionInfoByNameCache();
        assertThat(result).isNull();
    }

    @Test(groups = { "unit" })
    public void deserialize_WrongVersion_ReturnsNull() {
        CosmosClientMetadataCachesSnapshot snapshot = new CosmosClientMetadataCachesSnapshot();
        snapshot.collectionInfoByNameCache = "{\"version\":999,\"entries\":{}}".getBytes();

        AsyncCache<String, DocumentCollection> result = snapshot.getCollectionInfoByNameCache();
        assertThat(result).isNull();
    }

    @Test(groups = { "unit" })
    public void deserialize_MissingEntries_ReturnsNull() {
        CosmosClientMetadataCachesSnapshot snapshot = new CosmosClientMetadataCachesSnapshot();
        snapshot.collectionInfoByNameCache = "{\"version\":1}".getBytes();

        AsyncCache<String, DocumentCollection> result = snapshot.getCollectionInfoByNameCache();
        assertThat(result).isNull();
    }

    @Test(groups = { "unit" })
    public void deserialize_NullBytes_ReturnsNull() {
        CosmosClientMetadataCachesSnapshot snapshot = new CosmosClientMetadataCachesSnapshot();
        snapshot.collectionInfoByNameCache = null;

        AsyncCache<String, DocumentCollection> result = snapshot.getCollectionInfoByNameCache();
        assertThat(result).isNull();
    }

    @Test(groups = { "unit" })
    public void deserialize_OldJavaSerializationFormat_ReturnsNull() {
        CosmosClientMetadataCachesSnapshot snapshot = new CosmosClientMetadataCachesSnapshot();
        // Java serialization magic bytes (0xACED) are not valid JSON
        snapshot.collectionInfoByNameCache = new byte[] { (byte) 0xAC, (byte) 0xED, 0x00, 0x05 };

        AsyncCache<String, DocumentCollection> result = snapshot.getCollectionInfoByNameCache();
        assertThat(result).isNull();
    }

    @Test(groups = { "unit" }, dataProvider = "numberOfCollections")
    public void serialize_Deserialize_PreservesCollectionRidComparer(int cnt) throws Exception {
        // Verify that after JSON round-trip, the cache uses CollectionRidComparer
        AsyncCache<String, DocumentCollection> cache = new AsyncCache<>(
            new RxCollectionCache.CollectionRidComparer());

        List<String> keys = new ArrayList<>();
        for (int i = 0; i < cnt; i++) {
            DocumentCollection collectionDef = generateDocumentCollectionDefinition();
            collectionDef.setResourceId("rid-" + i);
            String collectionLink = "db/mydb/colls/" + collectionDef.getId();
            keys.add(collectionLink);
            cache.getAsync(collectionLink, null, () -> Mono.just(collectionDef)).block();
        }

        CosmosClientMetadataCachesSnapshot snapshot = new CosmosClientMetadataCachesSnapshot();
        snapshot.serializeCollectionInfoByNameCache(cache);

        AsyncCache<String, DocumentCollection> newCache = snapshot.getCollectionInfoByNameCache();
        assertThat(newCache).isNotNull();

        if (cnt > 0) {
            String firstKey = keys.get(0);
            DocumentCollection fromNewCache = newCache.getAsync(firstKey, null,
                () -> Mono.error(new RuntimeException("not expected"))).block();

            // Create a collection with the same resourceId but different content
            DocumentCollection sameRid = new DocumentCollection();
            sameRid.setResourceId(fromNewCache.getResourceId());
            sameRid.setId("completely-different-id");

            // CollectionRidComparer: same resourceId = equal → triggers refresh
            DocumentCollection refreshed = generateDocumentCollectionDefinition();
            refreshed.setResourceId("new-rid");
            final boolean[] initCalled = { false };
            newCache.getAsync(firstKey, sameRid, () -> {
                initCalled[0] = true;
                return Mono.just(refreshed);
            }).block();

            assertThat(initCalled[0])
                .as("CollectionRidComparer should treat same-resourceId as equal, triggering refresh")
                .isTrue();
        }
    }

    @DataProvider
    public static Object[][] numberOfCollections() {
        return new Object[][] {
            { 0 },
            { 1 },
            { 100 } };
    }

    @SuppressWarnings("unchecked")
    private <TKey, TValue> ConcurrentHashMap<TKey, AsyncLazy<TValue>> getInternalCache(AsyncCache<TKey, TValue> cache) {
        return ReflectionUtils.get(null, cache, "values");
    }

    private DocumentCollection generateDocumentCollectionDefinition() {
        DocumentCollection collection = new DocumentCollection();
        collection.setId(UUID.randomUUID().toString());
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.setPaths(ImmutableList.of("/" + RandomStringUtils.randomAlphabetic(3)));
        partitionKeyDefinition.setVersion(PartitionKeyDefinitionVersion.V2);
        collection.setPartitionKey(partitionKeyDefinition);
        SpatialSpec spatialSpec = new SpatialSpec();
        List<SpatialSpec> spatialSpecList = new ArrayList<>();
        spatialSpecList.add(spatialSpec);
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setSpatialIndexes(spatialSpecList);
        collection.setIndexingPolicy(indexingPolicy);

        return collection;
    }
}