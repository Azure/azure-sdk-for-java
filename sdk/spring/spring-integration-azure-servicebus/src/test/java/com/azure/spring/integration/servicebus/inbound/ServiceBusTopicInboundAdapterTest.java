// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.inbound;

import com.azure.spring.integration.endpoint.InboundChannelAdapterTest;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.servicebus.core.processor.container.ServiceBusTopicProcessorContainer;
import com.azure.spring.servicebus.core.processor.ServiceBusTopicProcessorClientFactory;
import com.azure.spring.servicebus.core.producer.ServiceBusProducerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;

public class ServiceBusTopicInboundAdapterTest extends InboundChannelAdapterTest<ServiceBusTopicInboundChannelAdapter> {

    @Mock
    ServiceBusTopicProcessorClientFactory processorClientFactory;
    @Mock
    ServiceBusProducerFactory senderClientFactory;

    private AutoCloseable closeable;

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @BeforeEach
    @Override
    public void setUp() {
        ServiceBusTopicProcessorContainer processorsContainer = mock(ServiceBusTopicProcessorContainer.class);
        this.closeable = MockitoAnnotations.openMocks(this);
        this.adapter = new ServiceBusTopicInboundChannelAdapter(processorsContainer, destination, consumerGroup,
            CheckpointConfig.builder()
                            .checkpointMode(CheckpointMode.RECORD)
                            .build());
    }
}
