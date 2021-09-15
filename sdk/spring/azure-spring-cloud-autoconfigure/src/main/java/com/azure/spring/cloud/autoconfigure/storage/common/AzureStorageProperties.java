// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.common;

import com.azure.spring.cloud.autoconfigure.properties.AbstractAzureHttpConfigurationProperties;
import com.azure.spring.core.properties.aware.credential.SasTokenAware;

/**
 * Common properties for all Azure Storage services.
 */
public class AzureStorageProperties extends AbstractAzureHttpConfigurationProperties implements SasTokenAware {

    protected String endpoint;

    protected String accountKey;

    protected String sasToken;

    protected String connectionString;

    protected String accountName;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
    }

    @Override
    public String getSasToken() {
        return sasToken;
    }

    @Override
    public void setSasToken(String sasToken) {
        this.sasToken = sasToken;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }
}
