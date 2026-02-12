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

    @Test(groups = { "unit" }, dataProvider = "numberOfCollections")
    public void serialize_Deserialize_AsyncCache_WithSafeObjectInputStream(int cnt) throws Exception {
        // This test exercises the full production deserialization path through
        // CosmosClientMetadataCachesSnapshot, which uses SafeObjectInputStream with
        // the production allowlist. This validates the allowlist covers all classes
        // that resolveClass() encounters during deserialization.
        AsyncCache<String, DocumentCollection> collectionInfoByNameCache = new AsyncCache<>();

        for (int i = 0; i < cnt; i++) {
            DocumentCollection collectionDef = generateDocumentCollectionDefinition();
            String collectionLink = "db/mydb/colls/" + collectionDef.getId();
            collectionInfoByNameCache.getAsync(collectionLink, null,
                () -> Mono.just(collectionDef)).block();
        }

        ConcurrentHashMap<String, AsyncLazy<DocumentCollection>> originalInternalCache =
            getInternalCache(collectionInfoByNameCache);

        // Serialize through CosmosClientMetadataCachesSnapshot (uses ObjectOutputStream)
        CosmosClientMetadataCachesSnapshot snapshot = new CosmosClientMetadataCachesSnapshot();
        snapshot.serializeCollectionInfoByNameCache(collectionInfoByNameCache);

        // Deserialize through CosmosClientMetadataCachesSnapshot (uses SafeObjectInputStream
        // with the production ALLOWED_DESERIALIZATION_CLASSES allowlist)
        AsyncCache<String, DocumentCollection> newAsyncCache = snapshot.getCollectionInfoByNameCache();
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
    public void deserializeWithInvalidClassType_shouldFail() throws Exception {
        // Serialize an unauthorized class (ArrayList) - this will trigger resolveClass()
        // unlike String which uses a special TC_STRING type code and bypasses resolveClass()
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(new ArrayList<>());
        oos.flush();
        byte[] maliciousBytes = baos.toByteArray();

        // Try to deserialize with SafeObjectInputStream that only allows cache classes
        ByteArrayInputStream bais = new ByteArrayInputStream(maliciousBytes);
        try (SafeObjectInputStream sois = new SafeObjectInputStream(bais,
                SerializableAsyncCollectionCache.class.getName(),
                SerializableAsyncCache.class.getName())) {
            sois.readObject();
            org.testng.Assert.fail("Expected InvalidClassException to be thrown");
        } catch (java.io.InvalidClassException e) {
            // Expected - the unauthorized class type was rejected by SafeObjectInputStream
            assertThat(e.getMessage()).contains("Unauthorized deserialization attempt");
        }
    }

    @Test(groups = { "unit" })
    public void safeObjectInputStream_rejectsUnauthorizedClasses() throws Exception {
        // Create a malicious payload with an unauthorized class (ArrayList instead of String,
        // since String uses a special TC_STRING type code in Java serialization and bypasses
        // ObjectInputStream.resolveClass() entirely)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(new ArrayList<>());
        oos.flush();
        
        byte[] maliciousBytes = baos.toByteArray();
        
        // Create a CosmosClientMetadataCachesSnapshot with the malicious payload
        CosmosClientMetadataCachesSnapshot snapshot = new CosmosClientMetadataCachesSnapshot();
        snapshot.collectionInfoByNameCache = maliciousBytes;
        
        // Attempt to deserialize - should fail with InvalidClassException
        try {
            AsyncCache<String, DocumentCollection> cache = snapshot.getCollectionInfoByNameCache();
            
            // Should not reach here
            org.testng.Assert.fail("Expected exception to be thrown for unauthorized class");
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


