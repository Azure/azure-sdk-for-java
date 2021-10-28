// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.retry;

/**
 * Unified storage retry properties for all Azure Storage Service
 */
public class StorageRetryProperties extends RetryProperties {

    private String secondaryHost;

    public String getSecondaryHost() {
        return secondaryHost;
    }

    public void setSecondaryHost(String secondaryHost) {
        this.secondaryHost = secondaryHost;
    }
}
