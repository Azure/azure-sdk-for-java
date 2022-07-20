// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.messaging.storage.queue.core.properties;

import com.azure.spring.cloud.core.implementation.properties.AzureHttpSdkProperties;
import com.azure.spring.cloud.service.implementation.storage.queue.QueueServiceClientProperties;
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
    private String endpoint;
    /**
     * Storage account access key.
     */
    private String accountKey;

    /**
     * Shared access signatures (SAS) token used to authorize requests sent to the service.
     */
    private String sasToken;

    /**
     * Connection string to connect to the service.
     */
    private String connectionString;

    /**
     * Name for the storage account.
     */
    private String accountName;

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

    /**
     * Set the connection string to connect to the service.
     * @param connectionString the connection string to connect to the service.
     */
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

    /**
     * Set the endpoint for Azure Storage service.
     * @param endpoint the endpoint for Azure Storage service.
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String getAccountName() {
        return accountName;
    }

    /**
     * Set the name for the storage account.
     * @param accountName the name for the storage account.
     */
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @Override
    public String getAccountKey() {
        return accountKey;
    }

    /**
     * Set the Storage account access key.
     * @param accountKey the Storage account access key.
     */
    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
    }

    @Override
    public String getSasToken() {
        return sasToken;
    }

    /**
     * Set the Shared Access Signatures (SAS) token used to authorize requests sent to the service.
     * @param sasToken the Shared Access Signatures (SAS) token used to authorize requests sent to the service.
     */
    public void setSasToken(String sasToken) {
        this.sasToken = sasToken;
    }

    @Override
    public QueueServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Set the Storage Queue service version used when making API requests.
     * @param serviceVersion the Storage Queue service version used when making API requests.
     */
    public void setServiceVersion(QueueServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    @Override
    public QueueMessageEncoding getMessageEncoding() {
        return messageEncoding;
    }

    /**
     * Set the encoding mode of how queue message body is represented in HTTP requests and responses.
     * @param messageEncoding the encoding mode of how queue message body is represented in HTTP requests and responses.
     */
    public void setMessageEncoding(QueueMessageEncoding messageEncoding) {
        this.messageEncoding = messageEncoding;
    }

}
