package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosRetryStrategy;

public final class RetryStrategyConfiguration {

    private CosmosRetryStrategy.RegionSwitchHint regionSwitchHint;

    public RetryStrategyConfiguration() {
        this.regionSwitchHint = CosmosRetryStrategy.RegionSwitchHint.NONE;
    }

    public void setRegionSwitchHint(CosmosRetryStrategy.RegionSwitchHint regionSwitchHint) {
        this.regionSwitchHint = regionSwitchHint;
    }

    CosmosRetryStrategy.RegionSwitchHint getRegionSwitchHint() {
        return regionSwitchHint;
    }
}
