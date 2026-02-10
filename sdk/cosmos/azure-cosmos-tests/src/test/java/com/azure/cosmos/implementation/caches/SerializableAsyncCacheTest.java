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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.caches.AsyncCache.SerializableAsyncCache;
import static com.azure.cosmos.implementation.caches.AsyncCache.SerializableAsyncCache.SerializableAsyncCollectionCache;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class SerializableAsyncCacheTest {

    @Test(groups = { "unit" }, dataProvider = "numberOfCollections")
    public void serialize_Deserialize_AsyncCache(int cnt) throws Exception {
        AsyncCache<String, DocumentCollection> collectionInfoByNameCache = new AsyncCache<>();

        for (int i = 0; i < cnt; i++) {
            DocumentCollection collectionDef = generateDocumentCollectionDefinition();
            String collectionLink = "db/mydb/colls/" + collectionDef.getId();

            // populate the cache
            collectionInfoByNameCache.getAsync(collectionLink, null,
                () -> Mono.just(collectionDef)).block();
        }

        ConcurrentHashMap<String, AsyncLazy<DocumentCollection>> originalInternalCache =
            getInternalCache(collectionInfoByNameCache);

        // serialize
        SerializableAsyncCache<String, DocumentCollection> serializableAsyncCache =
            SerializableAsyncCollectionCache.from(collectionInfoByNameCache, String.class, DocumentCollection.class);
        byte[] bytes = serializeObject(serializableAsyncCache);

        // deserialize
        SerializableAsyncCollectionCache serializableAsyncCollectionCache = deserializeObject(bytes);
        AsyncCache<String, DocumentCollection> newAsyncCache = serializableAsyncCollectionCache.toAsyncCache();
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

    @DataProvider
    public static Object[][] numberOfCollections() {
        return new Object[][] {
            { 0 },
            { 1 },
            { 100 } };
    }

    @SuppressWarnings("unchecked")
    private <T> T deserializeObject(byte[] objectSerializedAsBytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(objectSerializedAsBytes);
        ObjectInputStream ois = new ObjectInputStream(bais);

        return (T) ois.readObject();
    }

    private byte[] serializeObject(Object object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);
        objectOutputStream.writeObject(object);

        return baos.toByteArray();
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

    @Test(groups = { "unit" })
    public void deserializeWithInvalidClassType_shouldFail() throws Exception {
        // Create a malicious payload with a different class type
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        
        // Write a valid SerializableAsyncCollectionCache structure but with a malicious value
        oos.writeInt(1); // size = 1
        oos.writeUTF("testKey"); // key
        
        // Write a malicious object instead of SerializableDocumentCollection
        oos.writeObject("MaliciousString");
        
        // Write the equality comparer
        oos.writeObject((IEqualityComparer<DocumentCollection>) (v1, v2) -> v1 == v2);
        oos.flush();
        
        byte[] maliciousBytes = baos.toByteArray();
        
        // Attempt to deserialize - should fail with InvalidClassException
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(maliciousBytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            SerializableAsyncCollectionCache cache = (SerializableAsyncCollectionCache) ois.readObject();
            
            // Should not reach here
            assertThat(false).as("Expected InvalidClassException to be thrown").isTrue();
        } catch (java.io.InvalidClassException e) {
            // Expected - the malicious class type was rejected
            assertThat(e.getMessage()).contains("Expected SerializableDocumentCollection");
        }
    }

    @Test(groups = { "unit" })
    public void safeObjectInputStream_rejectsUnauthorizedClasses() throws Exception {
        // Create a malicious payload with an unauthorized class
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        
        // Write a malicious object (String instead of SerializableAsyncCollectionCache)
        oos.writeObject("MaliciousPayload");
        oos.flush();
        
        byte[] maliciousBytes = baos.toByteArray();
        
        // Create a CosmosClientMetadataCachesSnapshot with the malicious payload
        CosmosClientMetadataCachesSnapshot snapshot = new CosmosClientMetadataCachesSnapshot();
        snapshot.collectionInfoByNameCache = maliciousBytes;
        
        // Attempt to deserialize - should fail with InvalidClassException
        try {
            AsyncCache<String, DocumentCollection> cache = snapshot.getCollectionInfoByNameCache();
            
            // Should not reach here
            assertThat(false).as("Expected exception to be thrown for unauthorized class").isTrue();
        } catch (Exception e) {
            // Expected - the unauthorized class was rejected
            // The exception could be wrapped in a CosmosException, so check the cause chain
            Throwable cause = e;
            boolean foundInvalidClassException = false;
            while (cause != null && !foundInvalidClassException) {
                if (cause instanceof java.io.InvalidClassException) {
                    foundInvalidClassException = true;
                    assertThat(cause.getMessage()).contains("Unauthorized deserialization attempt");
                }
                cause = cause.getCause();
            }
            assertThat(foundInvalidClassException).as("Expected InvalidClassException in cause chain").isTrue();
        }
    }
}


