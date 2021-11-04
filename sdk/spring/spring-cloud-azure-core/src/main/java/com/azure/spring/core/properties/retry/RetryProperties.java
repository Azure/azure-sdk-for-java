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
     * The maximum number of attempts
     */
    private Integer maxAttempts;
    /**
     * How long to wait until a timeout
     */
    private Duration timeout;

    public BackoffProperties getBackoff() {
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

}
