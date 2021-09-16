// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.storage.queue.core;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.spring.core.util.Memoizer;
import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.QueueClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.function.Function;

import static com.azure.spring.core.ApplicationId.AZURE_SPRING_STORAGE_QUEUE;
import static com.azure.spring.core.ApplicationId.VERSION;

/**
 * Default client factory for Storage Queue.
 */
public class DefaultStorageQueueClientFactory implements StorageQueueClientFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStorageQueueClientFactory.class);
    private final String connectionString;
    private final Function<String, QueueAsyncClient> queueClientCreator = Memoizer.memoize(this::createQueueClient);
    private final HttpLogDetailLevel httpLogDetailLevel;

    public DefaultStorageQueueClientFactory(@NonNull String connectionString) {
        this(connectionString, HttpLogDetailLevel.NONE);
    }

    public DefaultStorageQueueClientFactory(@NonNull String connectionString, HttpLogDetailLevel httpLogDetailLevel) {
        this.connectionString = connectionString;
        this.httpLogDetailLevel = httpLogDetailLevel;
    }

    @Override
    public QueueAsyncClient getOrCreateQueueClient(String queueName) {
        return this.queueClientCreator.apply(queueName);
    }

    private QueueAsyncClient createQueueClient(String queueName) {
        final QueueAsyncClient queueClient = new QueueClientBuilder()
            .connectionString(this.connectionString)
            .queueName(queueName)
            .httpLogOptions(new HttpLogOptions()
                .setApplicationId(AZURE_SPRING_STORAGE_QUEUE + VERSION)
                .setLogLevel(httpLogDetailLevel))
            .buildAsyncClient();

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
