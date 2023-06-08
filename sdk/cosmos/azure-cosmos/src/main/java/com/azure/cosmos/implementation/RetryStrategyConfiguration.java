// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.models.CosmosRegionSwitchHint;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

public final class RetryStrategyConfiguration {

    private final AtomicReference<CosmosRegionSwitchHint> regionSwitchHint;
    private final AtomicReference<Duration> endToEndTimeout;

    public RetryStrategyConfiguration() {
        this.regionSwitchHint = new AtomicReference<>(CosmosRegionSwitchHint.NONE);
        this.endToEndTimeout = new AtomicReference<>(null);
    }

    public void setRegionSwitchHint(CosmosRegionSwitchHint regionSwitchHint) {
        this.regionSwitchHint.set(regionSwitchHint);
    }

    public void setEndToEndOperationTimeout(Duration endToEndTimeout) {
        this.endToEndTimeout.set(endToEndTimeout);
    }

    public CosmosRegionSwitchHint getRegionSwitchHint() {
        return regionSwitchHint.get();
    }

    public Duration getEndToEndTimeout() {
        return endToEndTimeout.get();
    }
}
