// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import java.time.Duration;

/**
 * The configuration for exponential backoff that has a delay duration that exponentially
 * increases with each retry attempt until an upper bound is reached after which every retry attempt is delayed by the
 * provided max delay duration.
 */
public class ExponentialBackoffOptions {
    private Integer maxRetries;
    private Duration baseDelay;
    private Duration maxDelay;

    /**
     * Gets the max retry attempts that can be made.
     *
     * @return The max retry attempts that can be made.
     */
    public Integer getMaxRetries() {
        return maxRetries;
    }

    /**
     * Sets the max retry attempts that can be made.
     *
     * @param maxRetries the max retry attempts that can be made.
     *
     * @return The updated {@link ExponentialBackoffOptions}
     */
    public ExponentialBackoffOptions setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    /**
     * Gets the base delay duration for retry.
     *
     * @return The base delay duration for retry.
     */
    public Duration getBaseDelay() {
        return baseDelay;
    }

    /**
     * Sets the base delay duration for retry.
     *
     * @param baseDelay the base delay duration for retry.
     *
     * @return The updated {@link ExponentialBackoffOptions}
     */
    public ExponentialBackoffOptions setBaseDelay(Duration baseDelay) {
        this.baseDelay = baseDelay;
        return this;
    }

    /**
     * Gets the max delay duration for retry.
     *
     * @return The max delay duration for retry.
     */
    public Duration getMaxDelay() {
        return maxDelay;
    }

    /**
     * Sets the max delay duration for retry.
     *
     * @param maxDelay the max delay duration for retry.
     *
     * @return The updated {@link ExponentialBackoffOptions}
     */
    public ExponentialBackoffOptions setMaxDelay(Duration maxDelay) {
        this.maxDelay = maxDelay;
        return this;
    }
}
