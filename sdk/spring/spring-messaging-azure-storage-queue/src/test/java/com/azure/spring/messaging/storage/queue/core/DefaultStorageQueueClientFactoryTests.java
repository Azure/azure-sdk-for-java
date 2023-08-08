// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.storage.queue.core;

import com.azure.spring.messaging.storage.queue.core.factory.StorageQueueClientFactory;
import com.azure.spring.messaging.storage.queue.core.properties.StorageQueueProperties;
import com.azure.spring.messaging.storage.queue.implementation.factory.DefaultStorageQueueClientFactory;
import com.azure.storage.queue.QueueAsyncClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DefaultStorageQueueClientFactoryTests {

    private StorageQueueClientFactory storageQueueClientFactory;
    private final String queueName = "queue";
    private int clientAddedTimes;

    @BeforeEach
    void setUp() {
        StorageQueueProperties storageQueueProperties = new StorageQueueProperties();
        storageQueueProperties.setAccountKey("test-key");
        storageQueueProperties.setAccountName("test-account");
        this.storageQueueClientFactory = new DefaultStorageQueueClientFactory(storageQueueProperties);
        clientAddedTimes = 0;
        this.storageQueueClientFactory.addListener((name, client) -> clientAddedTimes++);
    }

    @Test
    void testCreateQueueClient() {
        QueueAsyncClient client = storageQueueClientFactory.createQueueClient(queueName);
        assertNotNull(client);
        assertEquals(1, clientAddedTimes);
    }

    @Test
    void testCreateQueueClientTwice() {
        QueueAsyncClient client = storageQueueClientFactory.createQueueClient(queueName);
        assertNotNull(client);

        client = storageQueueClientFactory.createQueueClient(queueName);
        assertEquals(1, clientAddedTimes);
    }

    @Test
    void testRecreateQueueClient() {
        QueueAsyncClient client = storageQueueClientFactory.createQueueClient(queueName);
        assertNotNull(client);

        QueueAsyncClient client2 = storageQueueClientFactory.createQueueClient("queueName2");
        assertNotNull(client2);
        assertEquals(2, clientAddedTimes);
    }
}
