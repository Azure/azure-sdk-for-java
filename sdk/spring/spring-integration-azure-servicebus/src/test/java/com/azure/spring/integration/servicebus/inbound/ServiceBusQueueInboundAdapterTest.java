// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.inbound;

import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.servicebus.core.processor.container.ServiceBusQueueProcessorContainer;
import com.azure.spring.servicebus.core.processor.ServiceBusProcessorFactory;
import com.azure.spring.servicebus.core.producer.ServiceBusProducerFactory;
import com.azure.spring.integration.endpoint.InboundChannelAdapterTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;

public class ServiceBusQueueInboundAdapterTest extends InboundChannelAdapterTest<ServiceBusInboundChannelAdapter> {

    @Mock
    ServiceBusProcessorFactory processorClientFactory;
    @Mock
    ServiceBusProducerFactory senderClientFactory;

    private AutoCloseable closeable;

    @BeforeEach
    @Override
    public void setUp() {
        ServiceBusQueueProcessorContainer processorsContainer = mock(ServiceBusQueueProcessorContainer.class);
        this.closeable = MockitoAnnotations.openMocks(this);
        this.adapter = new ServiceBusInboundChannelAdapter(processorsContainer, destination,
            CheckpointConfig.builder()
                            .checkpointMode(CheckpointMode.RECORD)
                            .build());
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }
}
