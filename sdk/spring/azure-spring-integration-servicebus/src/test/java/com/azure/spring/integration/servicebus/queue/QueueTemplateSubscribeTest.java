// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.queue;

import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.azure.spring.integration.test.support.SubscribeOperationTest;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.concurrent.ExecutorService;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class QueueTemplateSubscribeTest extends SubscribeOperationTest<ServiceBusQueueOperation> {

    @Mock
    private ServiceBusQueueClientFactory mockClientFactory;

    @Mock
    private QueueClient queueClient;

    @BeforeEach
    public void setUp() {
        this.subscribeOperation = new ServiceBusQueueTemplate(mockClientFactory, new ServiceBusMessageConverter());
        when(this.mockClientFactory.getOrCreateClient(anyString())).thenReturn(queueClient);
        whenRegisterMessageHandler(this.queueClient);
    }

    @Override
    protected void verifySubscriberCreatorCalled() {
        verify(this.mockClientFactory, atLeastOnce()).getOrCreateClient(anyString());
    }

    @Override
    protected void verifySubscriberCreatorNotCalled() {
        verify(this.mockClientFactory, never()).getOrCreateClient(anyString());
    }

    @Override
    protected void verifySubscriberRegistered(int times) {
        try {
            verify(this.queueClient, times(times)).registerMessageHandler(isA(IMessageHandler.class), any(),
                any(ExecutorService.class));
        } catch (InterruptedException | ServiceBusException e) {
            fail("Exception should not throw" + e);
        }
    }

    @Override
    protected void verifySubscriberUnregistered(int times) {
    }

    private void whenRegisterMessageHandler(QueueClient queueClient) {
        try {
            doNothing().when(queueClient).registerMessageHandler(isA(IMessageHandler.class), any(), any(ExecutorService
                .class));
        } catch (InterruptedException | ServiceBusException e) {
            fail("Exception should not throw" + e);
        }
    }
}
