// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.CosmosAsyncClient;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class CosmosClientCacheItem {
    private CosmosClientCacheConfig clientConfig;
    private CosmosAsyncClient client;

    public CosmosClientCacheItem(CosmosClientCacheConfig clientConfig, CosmosAsyncClient client) {
        checkNotNull(clientConfig, "Argument 'clientConfig' cannot be null.");
        checkNotNull(client, "Argument 'client' cannot be null.");

        this.clientConfig = clientConfig;
        this.client = client;
    }

    public CosmosClientCacheConfig getClientConfig() {
        return clientConfig;
    }

    public CosmosAsyncClient getClient() {
        return client;
    }

}
