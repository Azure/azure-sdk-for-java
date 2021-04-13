// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.topic;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.integration.servicebus.ServiceBusTemplateSendTest;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TopicTemplateSendTest
    extends ServiceBusTemplateSendTest<ServiceBusTopicClientFactory, ServiceBusSenderAsyncClient> {

    @Before
    @Override
    public void setUp() {
        this.mockClientFactory = mock(ServiceBusTopicClientFactory.class);
        this.mockClient = mock(ServiceBusSenderAsyncClient.class);

        when(this.mockClientFactory.getOrCreateSender(anyString())).thenReturn(this.mockClient);
        when(this.mockClient.sendMessage(isA(ServiceBusMessage.class))).thenReturn(this.mono);

        this.sendOperation = new ServiceBusTopicTemplate(mockClientFactory, new ServiceBusMessageConverter());
    }

}
