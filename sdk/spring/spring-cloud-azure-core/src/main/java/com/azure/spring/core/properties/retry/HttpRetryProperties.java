// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.retry;

import com.azure.spring.core.aware.RetryAware;

import java.time.temporal.ChronoUnit;

/**
 * Unified http retry properties for all Azure SDKs based on HTTP.
 */
public final class HttpRetryProperties extends RetryProperties implements RetryAware.HttpRetry {

    private String retryAfterHeader;
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
