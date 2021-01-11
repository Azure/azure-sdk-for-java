// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.storage.queue.factory;

import com.azure.storage.queue.QueueAsyncClient;

/**
 * @author Miao Cao
 */
public interface StorageQueueClientFactory {

    QueueAsyncClient getOrCreateQueueClient(String queueName);
}
