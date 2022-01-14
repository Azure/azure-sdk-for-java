// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.queue;

import com.azure.spring.service.storage.common.StorageProperties;
import com.azure.storage.queue.QueueMessageEncoding;
import com.azure.storage.queue.QueueServiceVersion;

/**
 * Properties for Azure Storage Queue service.
 */
public interface QueueServiceClientProperties extends StorageProperties {

    QueueServiceVersion getServiceVersion();

    QueueMessageEncoding getMessageEncoding();

    Boolean getCreateQueueIfNotExists();

}
