// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.topic;

import com.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.azure.spring.integration.servicebus.inbound.ServiceBusTopicInboundChannelAdapter;
import com.azure.spring.integration.servicebus.topic.support.ServiceBusTopicTestOperation;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.azure.spring.integration.test.support.InboundChannelAdapterTest;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceBusTopicInboundAdapterTest extends InboundChannelAdapterTest<ServiceBusTopicInboundChannelAdapter> {

    @Mock
    ServiceBusTopicClientFactory clientFactory;

    @Mock
    SubscriptionClient subscriptionClient;

    @Override
    public void setUp() {
        when(this.clientFactory.getOrCreateSubscriptionClient(this.destination, this.consumerGroup))
            .thenReturn(this.subscriptionClient);
        this.adapter =
            new ServiceBusTopicInboundChannelAdapter(destination, new ServiceBusTopicTestOperation(clientFactory),
                consumerGroup);
    }
}
