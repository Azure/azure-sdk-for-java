// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.queue.properties;

import com.azure.spring.cloud.autoconfigure.storage.common.AzureStorageProperties;
import com.azure.spring.service.storage.queue.QueueServiceClientProperties;
import com.azure.storage.queue.QueueServiceVersion;

/**
 * Properties for Azure Storage Queue service.
 */
public class AzureStorageQueueProperties extends AzureStorageProperties implements QueueServiceClientProperties {

    public static final String PREFIX = "spring.cloud.azure.storage.queue";
    public static final String QUEUE_ENDPOINT_PATTERN = "https://%s.queue%s";

    private QueueServiceVersion serviceVersion;
    private String messageEncoding;
    /**
     * Name of the storage queue.
     */
    private String queueName;


    public String getEndpoint() {
        return endpoint == null ? buildEndpointFromAccountName() : endpoint;
    }

    private String buildEndpointFromAccountName() {
        return String.format(QUEUE_ENDPOINT_PATTERN, accountName, profile.getEnvironment().getStorageEndpointSuffix());
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

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
}
