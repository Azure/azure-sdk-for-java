// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties.retry;

import com.azure.spring.cloud.core.provider.RetryOptionsProvider;

import java.time.Duration;

/**
 * Common retry properties for all Azure SDKs.
 */
public class RetryProperties implements RetryOptionsProvider.RetryOptions {

    /**
     * The maximum number of attempts.
     */
    private Integer maxRetries;
    /**
     * The mode for retry backoff.
     */
    private RetryOptionsProvider.RetryMode mode = RetryOptionsProvider.RetryMode.EXPONENTIAL;
    /**
     * Amount of time to wait between retry attempts.
     */
    private Duration baseDelay;
    /**
     * Maximum permissible amount of time between retry attempts.
     */
    private Duration maxDelay;

    @Override
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

    @Override
    public RetryOptionsProvider.RetryMode getMode() {
        return mode;
    }

    /**
     * Set the mode for retry backoff.
     * @param mode the mode for retry backoff.
     */
    public void setMode(RetryOptionsProvider.RetryMode mode) {
        this.mode = mode;
    }

    @Override
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

    @Override
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
