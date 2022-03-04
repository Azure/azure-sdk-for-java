// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties.retry;

import com.azure.spring.cloud.core.aware.RetryOptionsAware;

import java.time.Duration;

/**
 * Common retry properties for all Azure SDKs.
 */
public class RetryProperties implements RetryOptionsAware.Retry {

    /**
     * The maximum number of attempts.
     */
    private Integer maxRetries;
    /**
     * The mode for retry backoff.
     */
    private RetryOptionsAware.RetryMode mode = RetryOptionsAware.RetryMode.EXPONENTIAL;
    /**
     * Amount of time to wait between retry attempts.
     */
    private Duration baseDelay;
    /**
     * Maximum permissible amount of time between retry attempts.
     */
    private Duration maxDelay;

    /**
     * Get the maximum number of attempts.
     * @return the maximum number of attempts.
     */
    public Integer getMaxRetries() {
        return maxRetries;
    }

    /**
     * Set the maximum number of attempts.
     * @param maxRetries the maximum number of attempts.
     */
    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    /**
     * Get the mode for retry backoff.
     * @return the mode for retry backoff.
     */
    @Override
    public RetryOptionsAware.RetryMode getMode() {
        return mode;
    }

    /**
     * Set the mode for retry backoff.
     * @param mode the mode for retry backoff.
     */
    public void setMode(RetryOptionsAware.RetryMode mode) {
        this.mode = mode;
    }
    /**
     * Get the amount of time to wait between retry attempts.
     * @return The delay to wait between retry attempts.
     */
    public Duration getBaseDelay() {
        return baseDelay;
    }

    /**
     * Set the amount of time to wait between retry attempts.
     * @param baseDelay The delay to wait between retry attempts.
     */
    public void setBaseDelay(Duration baseDelay) {
        this.baseDelay = baseDelay;
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

}
