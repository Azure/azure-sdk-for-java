// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties.core.retry;

import com.azure.spring.core.aware.RetryAware;
import com.azure.spring.core.properties.retry.BackoffProperties;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Unified http retry properties for all Azure SDKs based on HTTP.
 */
public class HttpRetryCP implements RetryAware.HttpRetry {

    private final Backoff backoff = new Backoff();
    /**
     * The maximum number of attempts
     */
    private Integer maxAttempts;
    /**
     * How long to wait until a timeout
     */
    private Duration timeout;

    private String retryAfterHeader;
    private ChronoUnit retryAfterTimeUnit;

    @Override
    public Backoff getBackoff() {
        return backoff;
    }

    @Override
    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Override
    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    @Override
    public String getRetryAfterHeader() {
        return retryAfterHeader;
    }

    public void setRetryAfterHeader(String retryAfterHeader) {
        this.retryAfterHeader = retryAfterHeader;
    }

    @Override
    public ChronoUnit getRetryAfterTimeUnit() {
        return retryAfterTimeUnit;
    }

    public void setRetryAfterTimeUnit(ChronoUnit retryAfterTimeUnit) {
        this.retryAfterTimeUnit = retryAfterTimeUnit;
    }

    static class Backoff extends BackoffProperties {

    }
}
