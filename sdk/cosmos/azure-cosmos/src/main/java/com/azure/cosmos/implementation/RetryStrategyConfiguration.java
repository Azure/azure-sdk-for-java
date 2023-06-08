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

    public void setEndToEndOperationTimeout(Duration endToEndTimeout) {
        this.endToEndTimeout = endToEndTimeout;
    }

    public CosmosRetryStrategy.RegionSwitchHint getRegionSwitchHint() {
        return regionSwitchHint;
    }

    public Duration getEndToEndTimeout() {
        return endToEndTimeout;
    }
}
