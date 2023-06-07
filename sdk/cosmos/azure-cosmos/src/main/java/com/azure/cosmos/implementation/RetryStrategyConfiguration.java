package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosRetryStrategy;

import java.time.Duration;

public final class RetryStrategyConfiguration {

    private CosmosRetryStrategy.RegionSwitchHint regionSwitchHint;
    private Duration endToEndTimeout;

    public RetryStrategyConfiguration() {
        this.regionSwitchHint = CosmosRetryStrategy.RegionSwitchHint.NONE;
        this.endToEndTimeout = null;
    }

    public void setRegionSwitchHint(CosmosRetryStrategy.RegionSwitchHint regionSwitchHint) {
        this.regionSwitchHint = regionSwitchHint;
    }

    void setEndToEndOperationTimeout(Duration endToEndTimeout) {
        this.endToEndTimeout = endToEndTimeout;
    }

    CosmosRetryStrategy.RegionSwitchHint getRegionSwitchHint() {
        return regionSwitchHint;
    }

    Duration getEndToEndTimeout() {
        return endToEndTimeout;
    }
}
