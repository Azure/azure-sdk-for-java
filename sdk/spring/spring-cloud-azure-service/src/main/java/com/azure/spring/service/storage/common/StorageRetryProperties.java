// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.common;

import com.azure.spring.core.properties.retry.HttpRetryProperties;

/**
 *
 */
public class StorageRetryProperties extends HttpRetryProperties implements StorageRetry {

    private String secondaryHost;

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
}
