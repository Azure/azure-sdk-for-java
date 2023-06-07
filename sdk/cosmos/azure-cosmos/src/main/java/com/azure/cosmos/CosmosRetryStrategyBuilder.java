package com.azure.cosmos;

import com.azure.cosmos.implementation.RetryStrategyConfiguration;

import java.util.HashMap;
import java.util.Map;

public final class CosmosRetryStrategyBuilder {

    private final Map<CosmosRetryStrategy.OperationType, RetryStrategyConfiguration> retryStrategyConfigs = new HashMap<>();

    public CosmosRetryStrategyBuilder withRegionSwitchHint(CosmosRetryStrategy.OperationType operationType, CosmosRetryStrategy.RegionSwitchHint regionSwitchHint) {

        retryStrategyConfigs.compute(operationType, (cosmosItemOperationType, retryStrategyConfiguration) -> {
            if (retryStrategyConfiguration == null) {
                retryStrategyConfiguration = new RetryStrategyConfiguration();
            }
            retryStrategyConfiguration.setRegionSwitchHint(regionSwitchHint);
            return retryStrategyConfiguration;
        });

        return this;
    }

    public CosmosRetryStrategy build() {
        CosmosRetryStrategy cosmosRetryStrategy = new CosmosRetryStrategy(retryStrategyConfigs);
        return cosmosRetryStrategy;
    }
}
