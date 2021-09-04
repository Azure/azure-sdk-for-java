// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.retry;

/**
 * Common retry properties for all Azure SDKs.
 */
public class RetryProperties {

    private BackoffProperties backoff = new BackoffProperties();
    private Integer maxAttempts;
    private Long timeout;

    public BackoffProperties getBackoff() {
        return backoff;
    }

    public void setBackoff(BackoffProperties backoff) {
        this.backoff = backoff;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }
}
