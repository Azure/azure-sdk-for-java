/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.storage.queue;

import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.models.SendMessageResult;
import com.microsoft.azure.spring.integration.storage.queue.factory.StorageQueueClientFactory;
import com.microsoft.azure.spring.integration.test.support.reactor.SendOperationTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StorageQueueTemplateSendTest extends SendOperationTest<StorageQueueOperation> {

    @Mock
    private StorageQueueClientFactory mockClientFactory;

    @Mock
    private QueueAsyncClient mockClient;

    @Before
    public void setup() {
        when(this.mockClientFactory.getOrCreateQueueClient(eq(destination))).thenReturn(mockClient);
        when(this.mockClient.sendMessage(anyString())).thenReturn(Mono.just(new SendMessageResult()));

        this.sendOperation = new StorageQueueTemplate(mockClientFactory);
    }

    @Override
    protected void setupError(String errorMessage) {
        when(this.mockClient.sendMessage(any(String.class)))
                .thenReturn(Mono.error(new IllegalArgumentException(errorMessage)));
    }

    @Override
    protected void verifySendCalled(int times) {
        verify(this.mockClient, times(times)).sendMessage(isA(String.class));
    }

    @Override
    protected void whenSendWithException() {
        when(this.mockClientFactory.getOrCreateQueueClient(eq(destination)))
                .thenThrow(new StorageQueueRuntimeException("Failed to get or create queue."));
    }

    @Override
    protected void verifyGetClientCreator(int times) {
        verify(this.mockClientFactory, times(times)).getOrCreateQueueClient(this.destination);
    }

}
