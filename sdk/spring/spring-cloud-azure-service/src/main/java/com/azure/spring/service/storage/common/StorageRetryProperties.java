// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.common;

import com.azure.spring.core.properties.retry.HttpRetryProperties;

/**
 *
 */
public class StorageRetryProperties extends HttpRetryProperties implements StorageRetry {

    private String secondaryHost;

    public String getSecondaryHost() {
        return secondaryHost;
    }

    public void setSecondaryHost(String secondaryHost) {
        this.secondaryHost = secondaryHost;
    }
}
