// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.queue;

import com.azure.spring.service.core.properties.TestAzureProperties;
import com.azure.storage.queue.QueueServiceVersion;

/**
 * Properties for Azure Storage Queue service.
 */
public class TestAzureStorageQueueProperties extends TestAzureProperties implements StorageQueueProperties {

    public static final String PREFIX = "spring.cloud.azure.storage.queue";
    public static final String QUEUE_ENDPOINT_PATTERN = "https://%s.queue%s";

    private QueueServiceVersion serviceVersion;
    private String messageEncoding;
    private String endpoint;
    private String accountKey;
    private String sasToken;
    private String connectionString;
    private String accountName;


    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public String getAccountName() {
        return accountName;
    }

    @Override
    public String getAccountKey() {
        return accountKey;
    }

    @Override
    public String getConnectionString() {
        return connectionString;
    }

    public QueueServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(QueueServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getMessageEncoding() {
        return messageEncoding;
    }

    public void setMessageEncoding(String messageEncoding) {
        this.messageEncoding = messageEncoding;
    }

    @Override
    public void setSasToken(String sasToken) {
        this.sasToken = sasToken;
    }

    @Override
    public String getSasToken() {
        return this.sasToken;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
