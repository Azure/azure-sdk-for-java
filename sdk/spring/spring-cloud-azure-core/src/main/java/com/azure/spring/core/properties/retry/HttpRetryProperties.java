// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.retry;

import com.azure.spring.core.aware.RetryAware;

import java.time.temporal.ChronoUnit;

/**
 * Unified http retry properties for all Azure SDKs based on HTTP.
 */
public class HttpRetryProperties extends RetryProperties implements RetryAware.HttpRetry {

    /**
     * HTTP header, such as Retry-After or x-ms-retry-after-ms, to lookup for the retry delay.
     * If the value is null, will calculate the delay using backoff and ignore the delay provided in response header.
     */
    private String retryAfterHeader;
    /**
     * Time unit to use when applying the retry delay.
     */
    private ChronoUnit retryAfterTimeUnit;

    /**
     * Get the retry after header.
     * @return the retry after header.
     */
    public String getRetryAfterHeader() {
        return retryAfterHeader;
    }

    /**
     * Set the retry after header.
     * @param retryAfterHeader the retry after header.
     */
    public void setRetryAfterHeader(String retryAfterHeader) {
        this.retryAfterHeader = retryAfterHeader;
    }

    /**
     * Get the time unit to use when applying the retry delay.
     * @return the time unit to use when applying the retry delay.
     */
    public ChronoUnit getRetryAfterTimeUnit() {
        return retryAfterTimeUnit;
    }

    /**
     * Set the time unit to use when applying the retry delay.
     * @param retryAfterTimeUnit the time unit to use when applying the retry delay.
     */
    public void setRetryAfterTimeUnit(ChronoUnit retryAfterTimeUnit) {
        this.retryAfterTimeUnit = retryAfterTimeUnit;
    }
}
