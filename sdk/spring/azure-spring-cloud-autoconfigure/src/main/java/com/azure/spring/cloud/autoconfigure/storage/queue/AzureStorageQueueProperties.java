// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.queue;

import com.azure.spring.cloud.autoconfigure.storage.common.AzureStorageProperties;
import com.azure.storage.queue.QueueServiceVersion;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for Azure Storage Queue service.
 */
public class AzureStorageQueueProperties extends AzureStorageProperties {

    public static final String PREFIX = "spring.cloud.azure.storage.queue";

    private String endpoint;
    private QueueServiceVersion serviceVersion;
    private String messageEncoding;


    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
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
}
