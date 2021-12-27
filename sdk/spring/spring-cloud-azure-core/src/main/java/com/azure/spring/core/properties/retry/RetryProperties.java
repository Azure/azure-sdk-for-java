// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.retry;

import com.azure.spring.core.aware.RetryAware;

import java.time.Duration;

/**
 * Common retry properties for all Azure SDKs.
 */
public class RetryProperties implements RetryAware.Retry {

    private final BackoffProperties backoff = new BackoffProperties();
    /**
     * The maximum number of attempts.
     */
    private Integer maxAttempts;
    /**
     * How long to wait until a timeout.
     */
    private Duration timeout;

    /**
     * Get the backoff between retries.
     * @return The backoff.
     */
    public BackoffProperties getBackoff() {
        return backoff;
    }

    /**
     * Get the maximum number of attempts.
     * @return the maximum number of attempts.
     */
    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * Set the maximum number of attempts.
     * @param maxAttempts the maximum number of attempts.
     */
    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    /**
     * Get how long to wait until a timeout.
     * @return the timeout.
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * Set how long to wait until a timeout.
     * @param timeout the timeout.
     */
    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

}
