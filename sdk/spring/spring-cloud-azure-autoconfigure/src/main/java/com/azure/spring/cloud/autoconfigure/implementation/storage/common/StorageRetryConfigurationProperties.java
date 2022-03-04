// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.common;

import com.azure.spring.cloud.autoconfigure.properties.core.retry.RetryConfigurationProperties;
import com.azure.spring.cloud.service.implementation.storage.common.StorageRetry;

import java.time.Duration;

/**
 *
 */
public class StorageRetryConfigurationProperties extends RetryConfigurationProperties implements StorageRetry {

    /**
     * Secondary Storage account to retry requests against.
     */
    private String secondaryHost;
    /**
     * Amount of time to wait until a timeout.
     */
    private Duration tryTimeout;


    @Override
    public String getSecondaryHost() {
        return secondaryHost;
    }

    public void setSecondaryHost(String secondaryHost) {
        this.secondaryHost = secondaryHost;
    }

    @Override
    public Duration getTryTimeout() {
        return tryTimeout;
    }

    public void setTryTimeout(Duration tryTimeout) {
        this.tryTimeout = tryTimeout;
    }
}
