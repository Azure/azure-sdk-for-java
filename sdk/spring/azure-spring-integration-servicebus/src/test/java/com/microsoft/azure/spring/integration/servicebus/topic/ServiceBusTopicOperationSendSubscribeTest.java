// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.servicebus.topic;

import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.microsoft.azure.spring.integration.servicebus.topic.support.ServiceBusTopicTestOperation;
import com.microsoft.azure.spring.integration.test.support.SendSubscribeByGroupOperationTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServiceBusTopicOperationSendSubscribeTest
    extends SendSubscribeByGroupOperationTest<ServiceBusTopicOperation> {

    @Mock
    ServiceBusTopicClientFactory clientFactory;

    @Mock
    SubscriptionClient subscriptionClient;

    @Before
    @Override
    public void setUp() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.complete(null);
        when(this.clientFactory.getOrCreateSubscriptionClient(anyString(), anyString()))
            .thenReturn(this.subscriptionClient);
        whenRegisterMessageHandler(subscriptionClient);
        when(this.subscriptionClient.completeAsync(any())).thenReturn(future);
        when(this.subscriptionClient.abandonAsync(any())).thenReturn(future);
        this.sendSubscribeOperation = new ServiceBusTopicTestOperation(clientFactory);
    }

    @Override
    protected void verifyCheckpointSuccessCalled(int times) {
        verify(this.subscriptionClient, times(times)).completeAsync(any());
    }

    @Override
    protected void verifyCheckpointBatchSuccessCalled(int times) {
        // Nothing to verify since batch checkpoint unsupported
    }

    @Override
    protected void verifyCheckpointFailureCalled(int times) {
        verify(this.subscriptionClient, times(times)).abandonAsync(any());
    }

    private void whenRegisterMessageHandler(SubscriptionClient subscriptionClient) {
        try {
            doNothing().when(subscriptionClient).registerMessageHandler(isA(IMessageHandler.class));
        } catch (InterruptedException | ServiceBusException e) {
            fail("Exception should not throw" + e);
        }
    }
}
