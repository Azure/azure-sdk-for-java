// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.storage.queue.core;

import com.azure.spring.core.util.Memoizer;
import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.QueueServiceAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.function.Function;

/**
 * Default client factory for Storage Queue.
 */
public class DefaultStorageQueueClientFactory implements StorageQueueClientFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStorageQueueClientFactory.class);
    private final Function<String, QueueAsyncClient> queueClientCreator = Memoizer.memoize(this::createQueueClient);
    private final QueueServiceAsyncClient queueServiceAsyncClient;

    public DefaultStorageQueueClientFactory(@NonNull QueueServiceAsyncClient queueServiceAsyncClient) {
        this.queueServiceAsyncClient = queueServiceAsyncClient;
    }

    @Override
    public QueueAsyncClient getOrCreateQueueClient(String queueName) {
        return this.queueClientCreator.apply(queueName);
    }

    private QueueAsyncClient createQueueClient(String queueName) {
        // TODO (xiada): the application id
        final QueueAsyncClient queueClient = queueServiceAsyncClient.getQueueAsyncClient(queueName);


        // TODO (xiada): when used with connection string, this call will throw exception
        // TODO (xiada): https://github.com/Azure/azure-sdk-for-java/issues/15008
        queueClient.create().subscribe(
            response -> {
            },
            e -> LOGGER.error("Fail to create the queue.", e),
            () -> LOGGER.info("Complete creating the queue!")
        );

        return queueClient;
    }

}
