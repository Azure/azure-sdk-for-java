// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.storage;

import com.azure.spring.cloud.core.implementation.properties.AzureHttpSdkProperties;
import com.azure.spring.cloud.service.implementation.storage.common.StorageRetryProperties;

public abstract class AzureStorageTestProperties extends AzureHttpSdkProperties {

    protected String endpoint;
    protected String accountKey;
    protected String sasToken;
    protected String connectionString;
    protected String accountName;

    private final StorageRetryProperties retry = new StorageRetryProperties();

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
    }

    public String getSasToken() {
        return sasToken;
    }

    public void setSasToken(String sasToken) {
        this.sasToken = sasToken;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @Override
    public StorageRetryProperties getRetry() {
        return retry;
    }
}
