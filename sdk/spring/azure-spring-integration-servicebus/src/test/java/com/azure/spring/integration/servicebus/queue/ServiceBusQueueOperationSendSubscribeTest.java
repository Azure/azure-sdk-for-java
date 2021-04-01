// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.queue;

import com.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.azure.spring.integration.servicebus.queue.support.ServiceBusQueueTestOperation;
import com.azure.spring.integration.test.support.SendSubscribeWithoutGroupOperationTest;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class ServiceBusQueueOperationSendSubscribeTest
    extends SendSubscribeWithoutGroupOperationTest<ServiceBusQueueOperation> {

    @Mock
    ServiceBusQueueClientFactory clientFactory;

    @Mock
    IQueueClient queueClient;

    @BeforeEach
    @Override
    public void setUp() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.complete(null);
        when(this.clientFactory.getOrCreateClient(anyString())).thenReturn(queueClient);
        whenRegisterMessageHandler(queueClient);
        when(this.queueClient.completeAsync(any())).thenReturn(future);
        when(this.queueClient.abandonAsync(any())).thenReturn(future);
        this.sendSubscribeOperation = new ServiceBusQueueTestOperation(clientFactory);
    }

    @Override
    protected void verifyCheckpointSuccessCalled(int times) {
        verify(this.queueClient, times(times)).completeAsync(any());
    }

    @Override
    protected void verifyCheckpointBatchSuccessCalled(int times) {
        // Nothing to verify since batch checkpoint unsupported
    }

    @Override
    protected void verifyCheckpointFailureCalled(int times) {
        verify(this.queueClient, times(times)).abandonAsync(any());
    }

    private void whenRegisterMessageHandler(IQueueClient queueClient) {
        try {
            doNothing().when(queueClient).registerMessageHandler(isA(IMessageHandler.class));
        } catch (InterruptedException | ServiceBusException e) {
            fail("Exception should not throw" + e);
        }
    }
}
