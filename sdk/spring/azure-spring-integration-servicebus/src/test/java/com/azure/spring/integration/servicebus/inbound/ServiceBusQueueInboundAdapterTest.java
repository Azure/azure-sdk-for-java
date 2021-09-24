// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.inbound;

import com.azure.spring.servicebus.core.ServiceBusQueueClientFactory;
import com.azure.spring.servicebus.support.ServiceBusQueueTestOperation;
import com.azure.spring.integration.endpoint.InboundChannelAdapterTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ServiceBusQueueInboundAdapterTest extends InboundChannelAdapterTest<ServiceBusQueueInboundChannelAdapter> {

    @Mock
    ServiceBusQueueClientFactory clientFactory;

    private AutoCloseable closeable;

    @BeforeEach
    @Override
    public void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
        this.adapter = new ServiceBusQueueInboundChannelAdapter(destination,
            new ServiceBusQueueTestOperation(clientFactory));
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }
}
