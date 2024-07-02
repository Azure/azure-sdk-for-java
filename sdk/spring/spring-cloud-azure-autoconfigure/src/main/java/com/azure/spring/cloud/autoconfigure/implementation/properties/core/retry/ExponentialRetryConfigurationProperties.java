// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.core.retry;

import com.azure.spring.cloud.core.provider.RetryOptionsProvider;

import java.time.Duration;

/**
 * Properties of the exponential retry mode.
 */
public class ExponentialRetryConfigurationProperties implements RetryOptionsProvider.RetryOptions.ExponentialRetryOptions {

    /**
     * The maximum number of attempts.
     */
    private Integer maxRetries;
    /**
     * Amount of time(Duration) to wait between retry attempts.
     */
    private Duration baseDelay;
    /**
     * Maximum permissible amount of time(duration) between retry attempts.
     */
    private Duration maxDelay;

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

}
