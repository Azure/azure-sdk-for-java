// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties.retry;

import com.azure.spring.cloud.core.provider.RetryOptionsProvider;

import java.time.Duration;

/**
 * Properties of the exponential retry mode.
 */
public class ExponentialRetryProperties implements RetryOptionsProvider.RetryOptions.ExponentialRetryOptions {

    /**
     * Creates an instance of {@link ExponentialRetryProperties}.
     */
    public ExponentialRetryProperties() {
    }

    /**
     * The maximum number of attempts.
     */
    private Integer maxRetries;
    /**
     * Amount of time(Duration) to wait between retry attempts.
     */
    private Duration baseDelay;
    /**
     * Maximum permissible amount(duration) of time between retry attempts.
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
