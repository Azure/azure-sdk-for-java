// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties.core.retry;

import com.azure.spring.core.aware.RetryAware;

import java.time.temporal.ChronoUnit;

/**
 * Unified http retry properties for all Azure SDKs based on HTTP.
 */
public class HttpRetryConfigurationProperties extends RetryConfigurationProperties implements RetryAware.HttpRetry {

    /**
     * HTTP header, such as Retry-After or x-ms-retry-after-ms, to lookup for the retry delay.
     * If the value is null, will calculate the delay using backoff and ignore the delay provided in response header.
     */
    private String retryAfterHeader;
    /**
     * Time unit to use when applying the retry delay.
     */
    private ChronoUnit retryAfterTimeUnit;

    public String getRetryAfterHeader() {
        return retryAfterHeader;
    }

    public void setRetryAfterHeader(String retryAfterHeader) {
        this.retryAfterHeader = retryAfterHeader;
    }

    public ChronoUnit getRetryAfterTimeUnit() {
        return retryAfterTimeUnit;
    }

    public void setRetryAfterTimeUnit(ChronoUnit retryAfterTimeUnit) {
        this.retryAfterTimeUnit = retryAfterTimeUnit;
    }
}
