// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.servicebus.queue;

import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.microsoft.azure.spring.integration.servicebus.inbound.ServiceBusQueueInboundChannelAdapter;
import com.microsoft.azure.spring.integration.servicebus.queue.support.ServiceBusQueueTestOperation;
import com.microsoft.azure.spring.integration.test.support.InboundChannelAdapterTest;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceBusQueueInboundAdapterTest extends InboundChannelAdapterTest<ServiceBusQueueInboundChannelAdapter> {

    @Mock
    ServiceBusQueueClientFactory clientFactory;

    @Mock
    IQueueClient queueClient;

    @Override
    public void setUp() {
        when(this.clientFactory.getOrCreateClient(anyString())).thenReturn(queueClient);
        this.adapter =
            new ServiceBusQueueInboundChannelAdapter(destination, new ServiceBusQueueTestOperation(clientFactory));
    }
}
