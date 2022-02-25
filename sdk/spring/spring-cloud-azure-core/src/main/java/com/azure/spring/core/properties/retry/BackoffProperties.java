// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.retry;

import com.azure.spring.core.aware.RetryOptionsAware;

import java.time.Duration;

/**
 * Backoff properties when a retry fails.
 */
public final class BackoffProperties implements RetryOptionsAware.Backoff {

    /**
     * Amount of time to wait between retry attempts.
     */
    private Duration delay;
    /**
     * Maximum permissible amount of time between retry attempts.
     */
    private Duration maxDelay;
    /**
     * Multiplier used to calculate the next backoff delay. If positive, then used as a multiplier for generating the
     * next delay for backoff.
     */
    private Double multiplier;

    /**
     * Get the amount of time to wait between retry attempts.
     * @return The delay to wait between retry attempts.
     */
    public Duration getDelay() {
        return delay;
    }

    /**
     * Set the amount of time to wait between retry attempts.
     * @param delay The delay to wait between retry attempts.
     */
    public void setDelay(Duration delay) {
        this.delay = delay;
    }

    /**
     * Get the maximum permissible amount of time between retry attempts.
     * @return The maximum permissible amount of time between retry attempts.
     */
    public Duration getMaxDelay() {
        return maxDelay;
    }

    /**
     * Set the maximum permissible amount of time between retry attempts.
     * @param maxDelay The maximum permissible amount of time between retry attempts.
     */
    public void setMaxDelay(Duration maxDelay) {
        this.maxDelay = maxDelay;
    }

    /**
     * Get the multiplier used to calculate the next backoff delay. If positive, then used as a multiplier for generating
     * the next delay for backoff.
     * @return the multiplier.
     */
    public Double getMultiplier() {
        return multiplier;
    }

    /**
     * Set the multiplier used to calculate the next backoff delay. If positive, then used as a multiplier for generating
     * the next delay for backoff.
     * @param multiplier The multiplier.
     */
    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }
}
