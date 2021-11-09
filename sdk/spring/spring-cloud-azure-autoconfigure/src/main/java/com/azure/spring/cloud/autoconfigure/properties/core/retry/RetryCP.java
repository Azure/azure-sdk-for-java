// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties.core.retry;

import com.azure.spring.core.aware.RetryAware;
import com.azure.spring.core.properties.retry.BackoffProperties;

import java.time.Duration;

/**
 * Http based client related retry properties.
 */
public class RetryCP implements RetryAware.Retry {

    private final Backoff backoff = new Backoff();
    /**
     * The maximum number of attempts
     */
    private Integer maxAttempts;
    /**
     * How long to wait until a timeout
     */
    private Duration timeout;

    public Backoff getBackoff() {
        return backoff;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    /**
     * Backoff properties when a retry fails.
     */
    public static class Backoff extends BackoffProperties {

    }
}
