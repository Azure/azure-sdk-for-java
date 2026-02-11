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
import java.util.Arrays;
import java.util.HashSet;

public class CosmosClientMetadataCachesSnapshot implements Serializable {
    private static final long serialVersionUID = 1l;
    private static final int ERROR_CODE = 0;
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
        try {
            // Create allowlist for all classes that may be deserialized
            SafeObjectInputStream ois = new SafeObjectInputStream(
                new ByteArrayInputStream(collectionInfoByNameCache),
                new HashSet<>(Arrays.asList(
                    // Top-level serialized cache class
                    AsyncCache.SerializableAsyncCache.SerializableAsyncCollectionCache.class.getName(),
                    // Nested classes deserialized by SerializableAsyncCollectionCache
                    DocumentCollection.SerializableDocumentCollection.class.getName(),
                    // Jackson classes used by SerializableDocumentCollection
                    "com.fasterxml.jackson.databind.node.ObjectNode",
                    "com.fasterxml.jackson.databind.node.TextNode",
                    // Internal Jackson classes that may be involved
                    "com.fasterxml.jackson.databind.node.BaseJsonNode",
                    "com.fasterxml.jackson.databind.node.ContainerNode",
                    "com.fasterxml.jackson.databind.node.ValueNode",
                    "com.fasterxml.jackson.databind.JsonNode",
                    // Equality comparer - we skip deserialization but still need to allow reading it
                    "com.azure.cosmos.implementation.caches.RxCollectionCache$CollectionRidComparer",
                    // Java collections and concurrent classes used internally
                    "java.util.concurrent.ConcurrentHashMap",
                    "java.util.HashMap",
                    "java.util.LinkedHashMap"
                ))
            );
            return ((AsyncCache.SerializableAsyncCache.SerializableAsyncCollectionCache)
                ois.readObject())
                .toAsyncCache();
        } catch (IOException | ClassNotFoundException e) {
            throw CosmosBridgeInternal.cosmosException(ERROR_CODE, e);
        }
    }

    public AsyncCache<String, DocumentCollection> getCollectionInfoByIdCache() {
        try {
            // Create allowlist for all classes that may be deserialized
            SafeObjectInputStream ois = new SafeObjectInputStream(
                new ByteArrayInputStream(collectionInfoByIdCache),
                new HashSet<>(Arrays.asList(
                    // Top-level serialized cache class
                    AsyncCache.SerializableAsyncCache.SerializableAsyncCollectionCache.class.getName(),
                    // Nested classes deserialized by SerializableAsyncCollectionCache
                    DocumentCollection.SerializableDocumentCollection.class.getName(),
                    // Jackson classes used by SerializableDocumentCollection
                    "com.fasterxml.jackson.databind.node.ObjectNode",
                    "com.fasterxml.jackson.databind.node.TextNode",
                    // Internal Jackson classes that may be involved
                    "com.fasterxml.jackson.databind.node.BaseJsonNode",
                    "com.fasterxml.jackson.databind.node.ContainerNode",
                    "com.fasterxml.jackson.databind.node.ValueNode",
                    "com.fasterxml.jackson.databind.JsonNode",
                    // Equality comparer - we skip deserialization but still need to allow reading it
                    "com.azure.cosmos.implementation.caches.RxCollectionCache$CollectionRidComparer",
                    // Java collections and concurrent classes used internally
                    "java.util.concurrent.ConcurrentHashMap",
                    "java.util.HashMap",
                    "java.util.LinkedHashMap"
                ))
            );
            return ((AsyncCache.SerializableAsyncCache.SerializableAsyncCollectionCache)
                ois.readObject())
                .toAsyncCache();
        } catch (IOException | ClassNotFoundException e) {
            throw CosmosBridgeInternal.cosmosException(ERROR_CODE, e);
        }
    }
}
