// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.retry;

/**
 * Backoff properties when a retry fails.
 */
public class BackoffProperties {

    private Long delay;
    private Long maxDelay;
    /**
     * If positive, then used as a multiplier for generating the next delay for backoff.
     *
     * @return a multiplier to use to calculate the next backoff delay
     */
    private Double multiplier;

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public Long getMaxDelay() {
        return maxDelay;
    }

    public void setMaxDelay(Long maxDelay) {
        this.maxDelay = maxDelay;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }
}
