// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.implementation.caches.SafeObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class CosmosClientMetadataCachesSnapshot implements Serializable {
    private static final long serialVersionUID = 1l;
    private static final int ERROR_CODE = 0;

    // All classes that ObjectInputStream.resolveClass() will encounter during deserialization
    // of the cache. This includes the top-level class, its serializable parent, and all
    // transitively serialized classes in the deserialization chain.
    private static final String[] ALLOWED_DESERIALIZATION_CLASSES = new String[] {
        // Top-level serialized cache class and its serializable parent
        AsyncCache.SerializableAsyncCache.SerializableAsyncCollectionCache.class.getName(),
        AsyncCache.SerializableAsyncCache.class.getName(),
        // Nested class deserialized by SerializableAsyncCollectionCache
        DocumentCollection.SerializableDocumentCollection.class.getName(),
        // Jackson uses NodeSerialization (via writeReplace) to serialize ObjectNode/TextNode as bytes
        "com.fasterxml.jackson.databind.node.NodeSerialization",
        // Equality comparer - read from the stream (then discarded) for format compatibility
        "com.azure.cosmos.implementation.caches.RxCollectionCache$CollectionRidComparer"
    };

    public byte[] collectionInfoByNameCache;
    public byte[] collectionInfoByIdCache;

    public CosmosClientMetadataCachesSnapshot() {
    }

    public void serialize(CosmosAsyncClient client) {
        RxDocumentClientImpl documentClient =
            (RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(client);
        documentClient.serialize(this);
    }

    public void serializeCollectionInfoByNameCache(AsyncCache<String, DocumentCollection> cache) {
        this.collectionInfoByNameCache = serializeAsyncCollectionCache(cache);
    }

    public void serializeCollectionInfoByIdCache(AsyncCache<String, DocumentCollection> cache) {
        this.collectionInfoByIdCache = serializeAsyncCollectionCache(cache);
    }

    private byte[] serializeAsyncCollectionCache(AsyncCache<String, DocumentCollection> cache) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);
            objectOutputStream.writeObject(AsyncCache.SerializableAsyncCache.from(cache, String.class,
                DocumentCollection.class));

            objectOutputStream.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw CosmosBridgeInternal.cosmosException(ERROR_CODE, e);
        }
    }

    public AsyncCache<String, DocumentCollection> getCollectionInfoByNameCache() {
        try (SafeObjectInputStream ois = new SafeObjectInputStream(
                new ByteArrayInputStream(collectionInfoByNameCache),
                ALLOWED_DESERIALIZATION_CLASSES)) {
            return ((AsyncCache.SerializableAsyncCache.SerializableAsyncCollectionCache)
                ois.readObject())
                .toAsyncCache();
        } catch (IOException | ClassNotFoundException e) {
            throw CosmosBridgeInternal.cosmosException(ERROR_CODE, e);
        }
    }

    public AsyncCache<String, DocumentCollection> getCollectionInfoByIdCache() {
        try (SafeObjectInputStream ois = new SafeObjectInputStream(
                new ByteArrayInputStream(collectionInfoByIdCache),
                ALLOWED_DESERIALIZATION_CLASSES)) {
            return ((AsyncCache.SerializableAsyncCache.SerializableAsyncCollectionCache)
                ois.readObject())
                .toAsyncCache();
        } catch (IOException | ClassNotFoundException e) {
            throw CosmosBridgeInternal.cosmosException(ERROR_CODE, e);
        }
    }
}
