// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.storage.queue;

import com.azure.spring.cloud.service.implementation.storage.common.StorageProperties;
import com.azure.storage.queue.QueueMessageEncoding;
import com.azure.storage.queue.QueueServiceVersion;

/**
 * Properties for Azure Storage Queue service.
 */
public interface QueueServiceClientProperties extends StorageProperties {

    /**
     * Get the Stroage Queue service version.
     * @return the queue service version.
     */
    QueueServiceVersion getServiceVersion();

    /**
     * Get the message encoding.
     * @return the message encoding.
     */
    QueueMessageEncoding getMessageEncoding();

}
