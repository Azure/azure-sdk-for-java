// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class CosmosClientCacheItem implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosClientCacheItem.class);

    private CosmosClientCacheConfig clientConfig;
    private CosmosClientCacheMetadata clientCacheMetadata;

    public CosmosClientCacheItem(
        CosmosClientCacheConfig clientConfig,
        CosmosClientCacheMetadata clientCacheMetadata) {
        checkNotNull(clientConfig, "Argument 'clientConfig' cannot be null.");
        checkNotNull(clientCacheMetadata, "Argument 'clientCacheMetadata' cannot be null.");

        this.clientConfig = clientConfig;
        this.clientCacheMetadata = clientCacheMetadata;
    }

    public CosmosClientCacheConfig getClientConfig() {
        return clientConfig;
    }

    public CosmosAsyncClient getClient() {
        return this.clientCacheMetadata.getClient();
    }

    @Override
    public void close() {
        long refCnt = this.clientCacheMetadata.decrementRefCount();
        if (refCnt < 0) {
            LOGGER.error("CosmosClient is released more than required.");
        }
    }
}
