package com.azure.cosmos;

import com.azure.cosmos.implementation.RetryStrategyConfiguration;

import java.util.HashMap;
import java.util.Map;

public final class CosmosRetryStrategyBuilder {

    private Map<CosmosRetryStrategy.OperationType, RetryStrategyConfiguration> retryStrategies = new HashMap<>();

    public CosmosRetryStrategyBuilder withRetryStrategy(CosmosRetryStrategy.OperationType operationType, CosmosRetryStrategy.RegionSwitchHint regionSwitchHint) {

        retryStrategies.compute(operationType, (cosmosItemOperationType, retryStrategyConfiguration) -> {
            if (retryStrategyConfiguration == null) {
                retryStrategyConfiguration = new RetryStrategyConfiguration();
            }
            retryStrategyConfiguration.setRegionSwitchHint(regionSwitchHint);
            return retryStrategyConfiguration;
        });

        return this;
    }

    public CosmosRetryStrategy build() {
        CosmosRetryStrategy cosmosRetryStrategy = new CosmosRetryStrategy(retryStrategies);
        return cosmosRetryStrategy;
    }
}
