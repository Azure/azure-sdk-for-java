// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.topic;

import com.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.azure.spring.integration.servicebus.inbound.ServiceBusTopicInboundChannelAdapter;
import com.azure.spring.integration.servicebus.topic.support.ServiceBusTopicTestOperation;
import com.azure.spring.integration.test.support.InboundChannelAdapterTest;
import com.microsoft.azure.servicebus.SubscriptionClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class ServiceBusTopicInboundAdapterTest extends InboundChannelAdapterTest<ServiceBusTopicInboundChannelAdapter> {

    @Mock
    ServiceBusTopicClientFactory clientFactory;

    @Mock
    SubscriptionClient subscriptionClient;

    @BeforeEach
    @Override
    public void setUp() {
        when(this.clientFactory.getOrCreateSubscriptionClient(this.destination, this.consumerGroup))
            .thenReturn(this.subscriptionClient);
        this.adapter =
            new ServiceBusTopicInboundChannelAdapter(destination, new ServiceBusTopicTestOperation(clientFactory),
                consumerGroup);
    }
}
