// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties.retry;

import com.azure.spring.cloud.core.provider.RetryOptionsProvider;

import java.time.Duration;

/**
 * Properties of the fixed retry mode.
 */
public class FixedRetryProperties implements RetryOptionsProvider.RetryOptions.FixedRetryOptions {

    /**
     * The maximum number of attempts.
     */
    private Integer maxRetries;
    /**
     * Amount of time to wait between retry attempts.
     */
    private Duration delay;

    @Override
    public Integer getMaxRetries() {
        return maxRetries;
    }

    /**
     * Set the maximum number attempts.
     *
     * @param maxRetries the maximum number attempts.
     */
    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public Duration getDelay() {
        return delay;
    }

    /**
     * Set the amount of time to wait between retry attempts.
     *
     * @param delay the amount of time to wait between retry attempts.
     */
    public void setDelay(Duration delay) {
        this.delay = delay;
    }
}
