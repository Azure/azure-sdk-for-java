// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.servicebus.topic;

import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.microsoft.azure.spring.integration.test.support.SubscribeByGroupOperationTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ExecutorService;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TopicTemplateSubscribeTest extends SubscribeByGroupOperationTest<ServiceBusTopicOperation> {

    @Mock
    private ServiceBusTopicClientFactory mockClientFactory;

    @Mock
    private SubscriptionClient subscriptionClient;

    @Mock
    private SubscriptionClient anotherSubscriptionClient;

    @Before
    public void setUp() {
        this.subscribeByGroupOperation = new ServiceBusTopicTemplate(mockClientFactory);
        when(this.mockClientFactory.getOrCreateSubscriptionClient(this.destination, this.consumerGroup))
            .thenReturn(this.subscriptionClient);
        when(this.mockClientFactory.getOrCreateSubscriptionClient(this.destination, this.anotherConsumerGroup))
            .thenReturn(this.anotherSubscriptionClient);
        whenRegisterMessageHandler(this.subscriptionClient);
        whenRegisterMessageHandler(this.anotherSubscriptionClient);
    }

    @Override
    protected void verifySubscriberCreatorCalled() {
        verify(this.mockClientFactory, atLeastOnce())
            .getOrCreateSubscriptionClient(eq(this.destination), eq(this.consumerGroup));
    }

    @Override
    protected void verifySubscriberCreatorNotCalled() {
        verify(this.mockClientFactory, never())
            .getOrCreateSubscriptionClient(eq(this.destination), eq(this.consumerGroup));
    }

    @Override
    protected void verifySubscriberRegistered(int times) {
        try {
            verify(this.subscriptionClient, times(times)).registerMessageHandler(isA(IMessageHandler.class), any(),
                any(ExecutorService.class));
        } catch (InterruptedException | ServiceBusException e) {
            fail("Exception should not throw" + e);
        }
    }

    @Override
    protected void verifySubscriberUnregistered(int times) {
    }

    private void whenRegisterMessageHandler(SubscriptionClient subscriptionClient) {
        try {
            doNothing().when(subscriptionClient).registerMessageHandler(isA(IMessageHandler.class), any(),
                any(ExecutorService.class));
        } catch (InterruptedException | ServiceBusException e) {
            fail("Exception should not throw" + e);
        }
    }
}
