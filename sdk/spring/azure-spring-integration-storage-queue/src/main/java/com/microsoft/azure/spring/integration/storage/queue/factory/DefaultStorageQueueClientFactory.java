// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.storage.queue.factory;

import com.azure.core.http.policy.HttpLogOptions;
import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.models.QueueStorageException;
import com.microsoft.azure.spring.cloud.context.core.util.Memoizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.function.Function;

import static com.microsoft.azure.spring.cloud.context.core.util.Constants.SPRING_INTEGRATION_STORAGE_QUEUE_APPLICATION_ID;

public class DefaultStorageQueueClientFactory implements StorageQueueClientFactory {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultStorageQueueClientFactory.class);
    private String connectionString;
    private final Function<String, QueueAsyncClient> queueClientCreator = Memoizer.memoize(this::createQueueClient);

    public DefaultStorageQueueClientFactory(@NonNull String connectionString) {
        this.connectionString = connectionString;
    }


    @Override
    public QueueAsyncClient getOrCreateQueueClient(String queueName) {
        return this.queueClientCreator.apply(queueName);
    }

    private QueueAsyncClient createQueueClient(String queueName) {
        final QueueAsyncClient queueClient = new QueueClientBuilder()
            .connectionString(this.connectionString)
            .queueName(queueName)
            .httpLogOptions(new HttpLogOptions().setApplicationId(SPRING_INTEGRATION_STORAGE_QUEUE_APPLICATION_ID))
            .buildAsyncClient();

        queueClient.create()
            .onErrorContinue(QueueStorageException.class, (e, r) -> LOG.error(e.getMessage()))
            .subscribe();

        return queueClient;
    }

}
