// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Metadata for cached Cosmos client instances.
 */
public class CosmosClientCacheMetadata {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosClientCacheMetadata.class);

    private final CosmosAsyncClient client;
    private final AutoCloseable credentialResource;
    private final AtomicLong refCount;
    private volatile Instant lastAccessed;
    private final Instant created;

    public CosmosClientCacheMetadata(CosmosAsyncClient client, Instant created) {
        this(client, created, null);
    }

    /**
     * @param credentialResource resource owned by the client's credential (for example an access token provider),
     * closed together with the client. May be null.
     */
    public CosmosClientCacheMetadata(CosmosAsyncClient client, Instant created, AutoCloseable credentialResource) {
        checkNotNull(client, "Argument 'client' must not be null");

        this.client = client;
        this.credentialResource = credentialResource;
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
        try {
            if (client != null) {
                client.close();
            }
        } finally {
            closeQuietly(credentialResource);
        }
    }

    static void closeQuietly(AutoCloseable resource) {
        if (resource == null) {
            return;
        }

        try {
            resource.close();
        } catch (Exception e) {
            LOGGER.warn("Failed to close credential resource", e);
        }
    }
}
