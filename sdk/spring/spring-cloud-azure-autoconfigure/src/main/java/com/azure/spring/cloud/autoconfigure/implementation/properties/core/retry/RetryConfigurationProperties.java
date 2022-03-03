// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.core.retry;

import com.azure.spring.cloud.core.aware.RetryOptionsAware;

import java.time.Duration;

/**
 * Retry properties.
 */
public class RetryConfigurationProperties implements RetryOptionsAware.Retry {

    /**
     * The maximum number of attempts.
     */
    private Integer maxRetries;
    /**
     * Amount of time to wait between retry attempts.
     */
    private Duration baseDelay;
    /**
     * Maximum permissible amount of time between retry attempts.
     */
    private Duration maxDelay;
    /**
     * Retry backoff mode.
     */
    private RetryOptionsAware.RetryMode mode = RetryOptionsAware.RetryMode.EXPONENTIAL;

    @Override
    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public Duration getBaseDelay() {
        return baseDelay;
    }

    public void setBaseDelay(Duration baseDelay) {
        this.baseDelay = baseDelay;
    }

    @Override
    public Duration getMaxDelay() {
        return maxDelay;
    }

    public void setMaxDelay(Duration maxDelay) {
        this.maxDelay = maxDelay;
    }

    @Override
    public RetryOptionsAware.RetryMode getMode() {
        return mode;
    }

    public void setMode(RetryOptionsAware.RetryMode mode) {
        this.mode = mode;
    }
}
