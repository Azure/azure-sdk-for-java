// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.queue;

import com.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.azure.spring.integration.servicebus.queue.support.ServiceBusQueueTestOperation;
import com.azure.spring.integration.test.support.SendSubscribeWithoutGroupOperationTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServiceBusQueueOperationSendSubscribeTest
    extends SendSubscribeWithoutGroupOperationTest<ServiceBusQueueOperation> {

    @Mock
    ServiceBusQueueClientFactory clientFactory;

   /* @Mock
    IQueueClient queueClient;*/ //TODO

    @Before
    @Override
    public void setUp() {
        //TODO
       /* CompletableFuture<Void> future = new CompletableFuture<>();
        future.complete(null);
        when(this.clientFactory.getOrCreateClient(anyString())).thenReturn(queueClient);
        whenRegisterMessageHandler(queueClient);
        when(this.queueClient.completeAsync(any())).thenReturn(future);
        when(this.queueClient.abandonAsync(any())).thenReturn(future);
        this.sendSubscribeOperation = new ServiceBusQueueTestOperation(clientFactory);*/
    }

    @Override
    protected void verifyCheckpointSuccessCalled(int times) {
        //TODO
        /*verify(this.queueClient, times(times)).completeAsync(any());*/
    }

    @Override
    protected void verifyCheckpointBatchSuccessCalled(int times) {
        // Nothing to verify since batch checkpoint unsupported
    }

    @Override
    protected void verifyCheckpointFailureCalled(int times) {
        //TODO
        /*verify(this.queueClient, times(times)).abandonAsync(any());*/
    }

    //TODO
  /*  private void whenRegisterMessageHandler(IQueueClient queueClient) {
        try {
            doNothing().when(queueClient).registerMessageHandler(isA(IMessageHandler.class));
        } catch (InterruptedException | ServiceBusException e) {
            fail("Exception should not throw" + e);
        }
    }*/
}
