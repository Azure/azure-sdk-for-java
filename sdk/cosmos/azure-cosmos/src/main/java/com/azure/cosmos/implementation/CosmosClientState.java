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

public class CosmosClientState implements Serializable {
    public byte[] collectionInfoByNameCache;
    public byte[] collectionInfoByIdCache;

    public CosmosClientState() {
        System.out.println("initialized Cosmos Client State");
    }

    public void serialize(CosmosAsyncClient client) throws IOException {
        RxDocumentClientImpl documentClient = (RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(client);
        documentClient.serialize(this);
    }

    public void serializeCollectionInfoByNameCache(AsyncCache<String, DocumentCollection> cache) throws IOException {
        this.collectionInfoByNameCache = serializeAsyncCollectionCache(cache);
    }

    public void serializeCollectionInfoByIdCache(AsyncCache<String, DocumentCollection> cache) throws IOException {
        this.collectionInfoByIdCache = serializeAsyncCollectionCache(cache);
    }

    private byte[] serializeAsyncCollectionCache(AsyncCache<String, DocumentCollection> cache) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);
        objectOutputStream.writeObject(AsyncCache.SerializableAsyncCache.from(cache, String.class, DocumentCollection.class));

        objectOutputStream.close();
        return baos.toByteArray();
    }

    public AsyncCache<String, DocumentCollection> getCollectionInfoByNameCache() throws IOException, ClassNotFoundException {
        return ((AsyncCache.SerializableAsyncCache.SerializableAsyncCollectionCache)
            new ObjectInputStream(new ByteArrayInputStream(collectionInfoByNameCache)).readObject())
            .toAsyncCache();
    }

    public AsyncCache<String, DocumentCollection> getCollectionInfoByIdCache() throws IOException, ClassNotFoundException {
        return ((AsyncCache.SerializableAsyncCache.SerializableAsyncCollectionCache)
            new ObjectInputStream(new ByteArrayInputStream(collectionInfoByIdCache)).readObject())
            .toAsyncCache();
    }
}
