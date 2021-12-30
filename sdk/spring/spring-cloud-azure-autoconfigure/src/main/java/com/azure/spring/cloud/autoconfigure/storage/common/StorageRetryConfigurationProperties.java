// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.common;

import com.azure.spring.core.properties.retry.BackoffProperties;
import com.azure.spring.service.storage.common.StorageRetry;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 *
 */
public class StorageRetryConfigurationProperties implements StorageRetry {

    /**
     * The maximum number of attempts
     */
    private Integer maxAttempts;
    /**
     * How long to wait until a timeout
     */
    private Duration timeout;
    /**
     * HTTP header, such as Retry-After or x-ms-retry-after-ms, to lookup for the retry delay.
     * If the value is null, will calculate the delay using backoff and ignore the delay provided in response header.
     */
    private String retryAfterHeader;
    /**
     * Time unit to use when applying the retry delay.
     */
    private ChronoUnit retryAfterTimeUnit;
    private String secondaryHost;

    @NestedConfigurationProperty
    private final BackoffProperties backoff = new BackoffProperties();

    @Override
    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    @Override
    public String getSecondaryHost() {
        return secondaryHost;
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

    @Override
    public BackoffProperties getBackoff() {
        return backoff;
    }

    public void setSecondaryHost(String secondaryHost) {
        this.secondaryHost = secondaryHost;
    }
}
