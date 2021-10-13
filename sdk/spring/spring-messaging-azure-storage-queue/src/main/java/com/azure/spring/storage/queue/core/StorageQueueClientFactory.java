// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.storage.queue.core;

import com.azure.storage.queue.QueueAsyncClient;

/**
 * @author Miao Cao
 */
public interface StorageQueueClientFactory {

    QueueAsyncClient getOrCreateQueueClient(String queueName);
}
