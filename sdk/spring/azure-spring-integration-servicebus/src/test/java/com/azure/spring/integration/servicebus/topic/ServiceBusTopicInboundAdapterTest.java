// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.topic;

import com.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.azure.spring.integration.servicebus.inbound.ServiceBusTopicInboundChannelAdapter;
import com.azure.spring.integration.servicebus.support.ServiceBusTopicTestOperation;
import com.azure.spring.integration.test.support.InboundChannelAdapterTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ServiceBusTopicInboundAdapterTest extends InboundChannelAdapterTest<ServiceBusTopicInboundChannelAdapter> {

    @Mock
    ServiceBusTopicClientFactory clientFactory;

    private AutoCloseable closeable;

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }
    @BeforeEach
    @Override
    public void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
        this.adapter = new ServiceBusTopicInboundChannelAdapter(destination,
                                                                new ServiceBusTopicTestOperation(clientFactory),
                                                                consumerGroup);
    }
}
