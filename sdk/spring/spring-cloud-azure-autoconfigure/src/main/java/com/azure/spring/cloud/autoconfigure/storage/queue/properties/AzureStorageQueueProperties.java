// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.queue.properties;

import com.azure.spring.cloud.autoconfigure.storage.common.AzureStorageProperties;
import com.azure.spring.service.storage.queue.QueueServiceClientProperties;
import com.azure.storage.queue.QueueMessageEncoding;
import com.azure.storage.queue.QueueServiceVersion;

/**
 * Properties for Azure Storage Queue service.
 */
public class AzureStorageQueueProperties extends AzureStorageProperties implements QueueServiceClientProperties {

    public static final String PREFIX = "spring.cloud.azure.storage.queue";
    public static final String QUEUE_ENDPOINT_PATTERN = "https://%s.queue%s";

    /**
     * Queue service version used when making API requests.
     */
    private QueueServiceVersion serviceVersion;
    /**
     * How queue message body is represented in HTTP requests and responses.
     */
    private QueueMessageEncoding messageEncoding;

    /**
     * Whether to create the queue if it does not exist.
     */
    private Boolean createQueueIfNotExists;

    public String getEndpoint() {
        return endpoint == null ? buildEndpointFromAccountName() : endpoint;
    }

    private String buildEndpointFromAccountName() {
        return String.format(QUEUE_ENDPOINT_PATTERN, accountName, profile.getEnvironment().getStorageEndpointSuffix());
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

    @Override
    public Boolean getCreateQueueIfNotExists() {
        return createQueueIfNotExists;
    }

    public void setCreateQueueIfNotExists(Boolean createQueueIfNotExists) {
        this.createQueueIfNotExists = createQueueIfNotExists;
    }
}
