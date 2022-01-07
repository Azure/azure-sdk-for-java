// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.common;

import com.azure.spring.cloud.autoconfigure.properties.core.AbstractAzureServiceConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.properties.core.client.HttpClientConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.properties.core.proxy.HttpProxyConfigurationProperties;
import com.azure.spring.service.storage.common.StorageProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Common properties for all Azure Storage services.
 */
public class AzureStorageProperties extends AbstractAzureServiceConfigurationProperties implements StorageProperties {

    protected String endpoint;

    protected String accountKey;

    protected String sasToken;

    protected String connectionString;

    protected String accountName;

    @NestedConfigurationProperty
    protected final StorageRetryConfigurationProperties retry = new StorageRetryConfigurationProperties();

    @NestedConfigurationProperty
    protected final HttpClientConfigurationProperties client = new HttpClientConfigurationProperties();

    @NestedConfigurationProperty
    protected final HttpProxyConfigurationProperties proxy = new HttpProxyConfigurationProperties();

    @Override
    public StorageRetryConfigurationProperties getRetry() {
        return retry;
    }

    @Override
    public HttpClientConfigurationProperties getClient() {
        return client;
    }

    @Override
    public HttpProxyConfigurationProperties getProxy() {
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
