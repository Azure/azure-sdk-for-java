// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.implementation.caches.AsyncCache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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
            return ((AsyncCache.SerializableAsyncCache.SerializableAsyncCollectionCache)
                new ObjectInputStream(new ByteArrayInputStream(collectionInfoByNameCache)).readObject())
                .toAsyncCache();
        } catch (IOException | ClassNotFoundException e) {
            throw CosmosBridgeInternal.cosmosException(ERROR_CODE, e);
        }
    }

    public AsyncCache<String, DocumentCollection> getCollectionInfoByIdCache() {
        try {
            return ((AsyncCache.SerializableAsyncCache.SerializableAsyncCollectionCache)
                new ObjectInputStream(new ByteArrayInputStream(collectionInfoByIdCache)).readObject())
                .toAsyncCache();
        } catch (IOException | ClassNotFoundException e) {
            throw CosmosBridgeInternal.cosmosException(ERROR_CODE, e);
        }
    }
}
