// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.storage.common;

import com.azure.spring.cloud.core.properties.retry.RetryProperties;

import java.time.Duration;

/**
 *
 */
public class StorageRetryProperties extends RetryProperties implements StorageRetry {

    private String secondaryHost;
    private Duration tryTimeout;

    @Override
    public String getSecondaryHost() {
        return secondaryHost;
    }

    /**
     * Set the secondary host.
     * @param secondaryHost the secondary host.
     */
    public void setSecondaryHost(String secondaryHost) {
        this.secondaryHost = secondaryHost;
    }

    @Override
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
