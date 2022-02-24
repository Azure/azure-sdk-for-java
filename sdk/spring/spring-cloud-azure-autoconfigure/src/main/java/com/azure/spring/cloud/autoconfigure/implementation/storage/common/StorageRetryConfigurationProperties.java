// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.common;

import com.azure.spring.cloud.autoconfigure.implementation.properties.core.retry.HttpRetryConfigurationProperties;
import com.azure.spring.service.implementation.storage.common.StorageRetry;

/**
 *
 */
public class StorageRetryConfigurationProperties extends HttpRetryConfigurationProperties implements StorageRetry {

    /**
     * Secondary Storage account to retry requests against.
     */
    private String secondaryHost;

    @Override
    public String getSecondaryHost() {
        return secondaryHost;
    }

    public void setSecondaryHost(String secondaryHost) {
        this.secondaryHost = secondaryHost;
    }
}
