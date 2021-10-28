// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.retry;

import java.time.temporal.ChronoUnit;

/**
 * Unified http retry properties for all Azure SDKs based on HTTP.
 */
public class HttpRetryProperties extends RetryProperties {

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
