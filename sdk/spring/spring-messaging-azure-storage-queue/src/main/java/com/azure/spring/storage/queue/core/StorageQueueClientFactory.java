// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.storage.queue.core;

import com.azure.storage.queue.QueueAsyncClient;

/**
 * The strategy to produce {@link QueueAsyncClient} instance.
 */
public interface StorageQueueClientFactory {

    /**
     * Create {@link QueueAsyncClient} to send to Storage Queue.
     * @param queueName the queue name
     * @return the QueueAsyncClient.
     */
    QueueAsyncClient getOrCreateQueueClient(String queueName);
}
