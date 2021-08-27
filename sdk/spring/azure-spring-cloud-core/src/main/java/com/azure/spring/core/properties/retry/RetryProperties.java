// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.retry;

/**
 * Common retry properties for all Azure SDKs.
 */
public class RetryProperties {

    private BackoffProperties backoff;
    private int maxAttempts;
    private long timeout;

    public BackoffProperties getBackoff() {
        return backoff;
    }

    public void setBackoff(BackoffProperties backoff) {
        this.backoff = backoff;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
