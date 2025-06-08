// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.CosmosAsyncClient;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A cached Cosmos client item with reference counting.
 */
public class CosmosClientCacheItem implements AutoCloseable {
    private final CosmosAsyncClient cosmosClient;
    private final String context;
    private final AtomicLong refCount;

    public CosmosClientCacheItem(CosmosAsyncClient cosmosClient, String context) {
        this.cosmosClient = cosmosClient;
        this.context = context;
        this.refCount = new AtomicLong(1);
    }

    public CosmosAsyncClient getCosmosClient() {
        return cosmosClient;
    }

    public String getContext() {
        return context;
    }

    public long incrementAndGetRefCount() {
        return refCount.incrementAndGet();
    }

    public long decrementAndGetRefCount() {
        return refCount.decrementAndGet();
    }

    public long getRefCount() {
        return refCount.get();
    }

    @Override
    public void close() {
        if (cosmosClient != null) {
            cosmosClient.close();
        }
    }
}