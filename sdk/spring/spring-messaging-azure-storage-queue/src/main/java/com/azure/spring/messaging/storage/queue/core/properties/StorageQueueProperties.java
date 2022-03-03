// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.messaging.storage.queue.core.properties;

import com.azure.spring.cloud.core.implementation.properties.AzureHttpSdkProperties;
import com.azure.spring.service.implementation.storage.queue.QueueServiceClientProperties;
import com.azure.storage.queue.QueueMessageEncoding;
import com.azure.storage.queue.QueueServiceVersion;

/**
 * Properties for Azure Storage Queue service.
 */
public class StorageQueueProperties extends AzureHttpSdkProperties implements QueueServiceClientProperties {

    private static final String QUEUE_ENDPOINT_PATTERN = "https://%s.queue%s";

    /**
     * Endpoint for Azure Storage service.
     */
    protected String endpoint;
    /**
     * Storage account access key.
     */
    protected String accountKey;

    /**
     * Shared access signatures (SAS) token used to authorize requests sent to the service.
     */
    protected String sasToken;

    /**
     * Connection string to connect to the service.
     */
    protected String connectionString;

    /**
     * Name for the storage account.
     */
    protected String accountName;

    /**
     * Queue service version used when making API requests.
     */
    private QueueServiceVersion serviceVersion;
    /**
     * How queue message body is represented in HTTP requests and responses.
     */
    private QueueMessageEncoding messageEncoding;

    @Override
    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    @Override
    public String getEndpoint() {
        return endpoint == null ? buildEndpointFromAccountName() : endpoint;
    }

    private String buildEndpointFromAccountName() {
        return String.format(QUEUE_ENDPOINT_PATTERN, accountName, getProfile().getEnvironment().getStorageEndpointSuffix());
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @Override
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

    @Override
    public QueueServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(QueueServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    @Override
    public QueueMessageEncoding getMessageEncoding() {
        return messageEncoding;
    }

    public void setMessageEncoding(QueueMessageEncoding messageEncoding) {
        this.messageEncoding = messageEncoding;
    }

}
