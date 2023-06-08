// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.models.CosmosRegionSwitchHint;

import java.time.Duration;

public final class RetryStrategyConfiguration {

    private CosmosRegionSwitchHint regionSwitchHint;
    private Duration endToEndTimeout;

    public RetryStrategyConfiguration() {
        this.regionSwitchHint = CosmosRegionSwitchHint.NONE;
        this.endToEndTimeout = null;
    }

    public void setRegionSwitchHint(CosmosRegionSwitchHint regionSwitchHint) {
        this.regionSwitchHint = regionSwitchHint;
    }

    public void setEndToEndOperationTimeout(Duration endToEndTimeout) {
        this.endToEndTimeout = endToEndTimeout;
    }

    public CosmosRegionSwitchHint getRegionSwitchHint() {
        return regionSwitchHint;
    }

    public Duration getEndToEndTimeout() {
        return endToEndTimeout;
    }
}
