// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.queue;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.spring.integration.servicebus.ServiceBusTemplateSendTest;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QueueTemplateSendTest extends ServiceBusTemplateSendTest<ServiceBusQueueClientFactory, ServiceBusSenderClient> {

    @Before
    @Override
    public void setUp() {
        /*this.mockClientFactory = mock(ServiceBusQueueClientFactory.class);
        this.mockClient = mock(IQueueClient.class);

        when(this.mockClientFactory.getOrCreateSender(anyString())).thenReturn(this.mockClient);
        when(this.mockClient.sendAsync(isA(IMessage.class))).thenReturn(future);

        this.sendOperation = new ServiceBusQueueTemplate(mockClientFactory, new ServiceBusMessageConverter());*/
    }
}
