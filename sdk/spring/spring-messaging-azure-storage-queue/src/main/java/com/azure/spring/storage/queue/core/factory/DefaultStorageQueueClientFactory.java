// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.storage.queue.core.factory;

import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.service.storage.queue.QueueServiceClientBuilderFactory;
import com.azure.spring.storage.queue.core.properties.StorageQueueProperties;
import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.QueueServiceAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link StorageQueueClientFactory} implementation to produce new {@link QueueAsyncClient} instances
 * for provided {@link QueueServiceAsyncClient} on each {@link #createQueueClient} invocation.
 */
public final class DefaultStorageQueueClientFactory implements StorageQueueClientFactory, DisposableBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStorageQueueClientFactory.class);

    private final List<Listener> listeners = new ArrayList<>();
    private final Map<String, QueueAsyncClient> clients = new ConcurrentHashMap<>();
    private final QueueServiceAsyncClient queueServiceAsyncClient;

    /**
     * Construct a factory with the provided {@link StorageQueueProperties}.
     * @param storageQueueProperties the storage queue properties
     */
    public DefaultStorageQueueClientFactory(@NonNull StorageQueueProperties storageQueueProperties) {
        QueueServiceClientBuilderFactory queueServiceClientBuilderFactory =
            new QueueServiceClientBuilderFactory(storageQueueProperties);
        queueServiceClientBuilderFactory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_INTEGRATION_STORAGE_QUEUE);
        this.queueServiceAsyncClient = queueServiceClientBuilderFactory.build().buildAsyncClient();
    }

    @Override
    public QueueAsyncClient createQueueClient(String queueName) {
        return clients.computeIfAbsent(queueName, q -> {
            final QueueAsyncClient queueClient = queueServiceAsyncClient.getQueueAsyncClient(queueName);
            // TODO (xiada): when used with connection string, this call will throw exception
            // TODO (xiada): https://github.com/Azure/azure-sdk-for-java/issues/15008
            // Do we need to auto create the queue or add another property of autoCreateQueueIfNotExist for it?
            queueClient.create().subscribe(
                response -> {
                },
                e -> LOGGER.error("Fail to create the queue.", e),
                () -> LOGGER.info("Complete creating the queue!")
            );
            this.listeners.forEach(l -> l.queueClientAdded(queueName, queueClient));
            return queueClient;
        });
    }

    @Override
    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    @Override
    public boolean removeListener(Listener listener) {
        return this.listeners.remove(listener);
    }

    @Override
    public void destroy() {
        clients.forEach((name, client) -> {
            listeners.forEach(l -> l.queueClientRemoved(name, client));
        });
        this.clients.clear();
        this.listeners.clear();
    }

}
