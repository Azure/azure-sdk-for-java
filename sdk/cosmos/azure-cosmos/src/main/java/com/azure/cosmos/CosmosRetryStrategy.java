package com.azure.cosmos;

import com.azure.cosmos.implementation.RetryStrategyConfiguration;

public final class CosmosRetryStrategy {

    private final RetryStrategyConfiguration retryStrategyConfiguration;

    CosmosRetryStrategy(RetryStrategyConfiguration retryStrategyConfiguration) {
        this.retryStrategyConfiguration = retryStrategyConfiguration;
    }

    // TODO: Do not expose
    public RetryStrategyConfiguration getRetryStrategyConfiguration() {
        return retryStrategyConfiguration;
    }
}
