// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.RetryStrategyConfiguration;
import com.azure.cosmos.models.CosmosRegionSwitchHint;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link CosmosRetryStrategyBuilder} instance will be used to build
 * a {@link CosmosRetryStrategy} instance which applies to all operations
 * if only configured on {@link CosmosClientBuilder}
 * <br>
 * If the {@link CosmosRetryStrategy} is to be configured for a specific operation do so through
 * {@link com.azure.cosmos.models.CosmosItemRequestOptions} or {@link com.azure.cosmos.models.CosmosQueryRequestOptions}.
 * */
public final class CosmosRetryStrategyBuilder {

    private final RetryStrategyConfiguration retryStrategyConfiguration;

    public CosmosRetryStrategyBuilder() {
        this.retryStrategyConfiguration = new RetryStrategyConfiguration();
    }

    /**
     * Sets the {@link CosmosRegionSwitchHint} which specifies for
     * an operation whether internal retry policies should prioritize a local region or a remote region.
     * */
    public CosmosRetryStrategyBuilder setRegionSwitchHint(CosmosRegionSwitchHint regionSwitchHint) {
        retryStrategyConfiguration.setRegionSwitchHint(regionSwitchHint);
        return this;
    }

    public CosmosRetryStrategy build() {
        CosmosRetryStrategy cosmosRetryStrategy = new CosmosRetryStrategy(retryStrategyConfiguration);
        return cosmosRetryStrategy;
    }
}
