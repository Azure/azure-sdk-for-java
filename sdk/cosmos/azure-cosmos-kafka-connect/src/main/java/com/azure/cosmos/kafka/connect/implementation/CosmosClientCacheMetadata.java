// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.CosmosAsyncClient;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Metadata for cached Cosmos client instances.
 */
public class CosmosClientCacheMetadata {
    private final CosmosAsyncClient client;
    private final AtomicLong refCount;
    private volatile Instant lastAccessed;
    private final Instant created;

    public CosmosClientCacheMetadata(CosmosAsyncClient client, Instant created) {
        checkNotNull(client, "Argument 'client' must not be null");

        this.client = client;
        this.refCount = new AtomicLong(1);
        this.lastAccessed = created;
        this.created = created;
    }

    public CosmosAsyncClient getClient() {
        return client;
    }

    public void updateLastAccessed() {
        this.lastAccessed = Instant.now();
    }

    public Instant getLastAccessed() {
        return lastAccessed;
    }

    public long incrementRefCount() {
        return refCount.incrementAndGet();
    }

    public long decrementRefCount() {
        return refCount.decrementAndGet();
    }

    public long getRefCount() {
        return refCount.get();
    }

    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
