// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.common;

import com.azure.spring.cloud.autoconfigure.properties.AzureHttpConfigurationProperties;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.aware.credential.ConnectionStringAware;
import com.azure.spring.core.properties.aware.credential.SasTokenAware;

/**
 * Common properties for all Azure Storage services.
 */
public class AzureStorageProperties extends AzureHttpConfigurationProperties implements ConnectionStringAware,
                                                                                        SasTokenAware {

    protected String accountName;

    protected String accountKey;

    protected String sasToken;

    protected String connectionString;

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

    @Override
    public String getConnectionString() {
        return connectionString;
    }

    @Override
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }
}
