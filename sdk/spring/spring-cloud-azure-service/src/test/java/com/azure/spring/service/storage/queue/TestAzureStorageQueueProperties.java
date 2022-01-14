// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.queue;

import com.azure.spring.service.storage.TestAzureStorageProperties;
import com.azure.storage.queue.QueueMessageEncoding;
import com.azure.storage.queue.QueueServiceVersion;

/**
 * Properties for Azure Storage Queue service.
 */
class TestAzureStorageQueueProperties extends TestAzureStorageProperties implements QueueServiceClientProperties {

    private QueueServiceVersion serviceVersion;
    private QueueMessageEncoding messageEncoding;

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
