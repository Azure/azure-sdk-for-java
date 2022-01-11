// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.storage.queue.core;

import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.QueueServiceAsyncClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultStorageQueueClientFactoryTests {

    @Test
    void returnSameQueueClientWhenMultiGetQueueClient() {
        QueueServiceAsyncClient serviceAsyncClient = mock(QueueServiceAsyncClient.class);
        DefaultStorageQueueClientFactory factory = new DefaultStorageQueueClientFactory(serviceAsyncClient);
        String queueName = "test-queue";
        QueueAsyncClient queueAsyncClient = mock(QueueAsyncClient.class);
        when(queueAsyncClient.create()).thenReturn(Mono.empty());
        when(serviceAsyncClient.getQueueAsyncClient(queueName)).thenReturn(queueAsyncClient);
        QueueAsyncClient queueClientFirst = factory.getOrCreateQueueClient(queueName);
        QueueAsyncClient queueClientTwo = factory.getOrCreateQueueClient(queueName);
        assertEquals(queueClientFirst, queueClientTwo);
        verify(serviceAsyncClient, times(1)).getQueueAsyncClient(queueName);
    }
}
