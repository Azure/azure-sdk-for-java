// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.queue;

import com.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.azure.spring.integration.servicebus.inbound.ServiceBusQueueInboundChannelAdapter;
import com.azure.spring.integration.servicebus.queue.support.ServiceBusQueueTestOperation;
import com.azure.spring.integration.test.support.InboundChannelAdapterTest;
import com.microsoft.azure.servicebus.IQueueClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class ServiceBusQueueInboundAdapterTest extends InboundChannelAdapterTest<ServiceBusQueueInboundChannelAdapter> {

    @Mock
    ServiceBusQueueClientFactory clientFactory;

    @Mock
    IQueueClient queueClient;

    @BeforeEach
    @Override
    public void setUp() {
        when(this.clientFactory.getOrCreateClient(anyString())).thenReturn(queueClient);
        this.adapter =
            new ServiceBusQueueInboundChannelAdapter(destination, new ServiceBusQueueTestOperation(clientFactory));
    }
}
