// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.queue;

import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.azure.spring.integration.test.support.SubscribeOperationTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ExecutorService;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QueueTemplateSubscribeTest extends SubscribeOperationTest<ServiceBusQueueOperation> {

    @Mock
    private ServiceBusQueueClientFactory mockClientFactory;

   /* @Mock
    private QueueClient queueClient;*/ //TODO

    @Before
    public void setUp() {
        //TODO
       /* this.subscribeOperation = new ServiceBusQueueTemplate(mockClientFactory, new ServiceBusMessageConverter());
        when(this.mockClientFactory.getOrCreateClient(anyString())).thenReturn(queueClient);
        whenRegisterMessageHandler(this.queueClient);*/
    }

    @Override
    protected void verifySubscriberCreatorCalled() {
        //TODO
      /*  verify(this.mockClientFactory, atLeastOnce()).getOrCreateClient(anyString());*/
    }

    @Override
    protected void verifySubscriberCreatorNotCalled() {
        //TODO
        /*verify(this.mockClientFactory, never()).getOrCreateClient(anyString());*/
    }

    @Override
    protected void verifySubscriberRegistered(int times) {
        //TODO
       /* try {
            verify(this.queueClient, times(times)).registerMessageHandler(isA(IMessageHandler.class), any(),
                any(ExecutorService.class));
        } catch (InterruptedException | ServiceBusException e) {
            fail("Exception should not throw" + e);
        }*/
    }

    @Override
    protected void verifySubscriberUnregistered(int times) {
    }

    //TODO
   /* private void whenRegisterMessageHandler(QueueClient queueClient) {
        try {
            doNothing().when(queueClient).registerMessageHandler(isA(IMessageHandler.class), any(), any(ExecutorService
                .class));
        } catch (InterruptedException | ServiceBusException e) {
            fail("Exception should not throw" + e);
        }
    }*/
}
