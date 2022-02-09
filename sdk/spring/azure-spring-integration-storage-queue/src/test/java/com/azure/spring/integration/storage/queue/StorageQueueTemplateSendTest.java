// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.storage.queue;

import com.azure.spring.integration.storage.queue.factory.StorageQueueClientFactory;
import com.azure.spring.integration.test.support.reactor.SendOperationTest;
import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.models.SendMessageResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StorageQueueTemplateSendTest extends SendOperationTest<StorageQueueOperation> {

    @Mock
    private StorageQueueClientFactory mockClientFactory;

    @Mock
    private QueueAsyncClient mockClient;

    private AutoCloseable closeable;

    @BeforeEach
    public void init() {
        this.closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void close() throws Exception {
        this.closeable.close();
    }

    @BeforeEach
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
