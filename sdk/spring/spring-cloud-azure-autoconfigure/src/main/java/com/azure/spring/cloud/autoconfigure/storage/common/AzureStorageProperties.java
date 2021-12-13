// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.common;

import com.azure.spring.cloud.autoconfigure.implementation.core.AbstractAzureServiceCP;
import com.azure.spring.cloud.autoconfigure.implementation.core.client.HttpClientCP;
import com.azure.spring.cloud.autoconfigure.implementation.core.proxy.HttpProxyCP;
import com.azure.spring.service.storage.common.StorageProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Common properties for all Azure Storage services.
 */
public class AzureStorageProperties extends AbstractAzureServiceCP implements StorageProperties {

    protected String endpoint;

    protected String accountKey;

    protected String sasToken;

    protected String connectionString;

    protected String accountName;

    @NestedConfigurationProperty
    protected final StorageRetryCP retry = new StorageRetryCP();

    @NestedConfigurationProperty
    protected final HttpClientCP client = new HttpClientCP();

    @NestedConfigurationProperty
    protected final HttpProxyCP proxy = new HttpProxyCP();

    @Override
    public StorageRetryCP getRetry() {
        return retry;
    }

    @Override
    public HttpClientCP getClient() {
        return client;
    }

    @Override
    public HttpProxyCP getProxy() {
        return proxy;
    }

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
