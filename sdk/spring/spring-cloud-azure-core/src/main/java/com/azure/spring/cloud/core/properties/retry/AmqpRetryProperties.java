// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties.retry;

import com.azure.spring.cloud.core.provider.RetryOptionsProvider;

import java.time.Duration;

/**
 * Unified http retry properties for all Azure SDKs based on HTTP.
 */
public class AmqpRetryProperties extends RetryProperties implements RetryOptionsProvider.AmqpRetryOptions {

    /**
     * How long to wait until a timeout.
     */
    private Duration tryTimeout;

    /**
     * Get how long to wait until a timeout.
     * @return the timeout.
     */
    public Duration getTryTimeout() {
        return tryTimeout;
    }

    /**
     * Set how long to wait until a timeout.
     * @param tryTimeout the timeout.
     */
    public void setTryTimeout(Duration tryTimeout) {
        this.tryTimeout = tryTimeout;
    }
}
