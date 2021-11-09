// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.retry;

import com.azure.spring.core.aware.RetryAware;

import java.time.Duration;

/**
 * Backoff properties when a retry fails.
 */
public class BackoffProperties implements RetryAware.Backoff {

    private Duration delay;
    private Duration maxDelay;

    /**
     * If positive, then used as a multiplier for generating the next delay for backoff.
     *
     * @return a multiplier to use to calculate the next backoff delay
     */
    private Double multiplier;

    public Duration getDelay() {
        return delay;
    }

    public void setDelay(Duration delay) {
        this.delay = delay;
    }

    public Duration getMaxDelay() {
        return maxDelay;
    }

    public void setMaxDelay(Duration maxDelay) {
        this.maxDelay = maxDelay;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }
}
