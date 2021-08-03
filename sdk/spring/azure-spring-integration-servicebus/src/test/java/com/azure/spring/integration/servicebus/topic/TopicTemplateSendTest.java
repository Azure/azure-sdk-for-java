// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.topic;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.integration.servicebus.ServiceBusTemplateSendTest;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TopicTemplateSendTest
    extends ServiceBusTemplateSendTest<ServiceBusTopicClientFactory, ServiceBusSenderAsyncClient> {

    private AutoCloseable closeable;

    @BeforeEach
    @Override
    public void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
        this.mockClientFactory = mock(ServiceBusTopicClientFactory.class);
        this.mockClient = mock(ServiceBusSenderAsyncClient.class);

        when(this.mockClientFactory.getOrCreateSender(anyString())).thenReturn(this.mockClient);
        when(this.mockClient.sendMessage(isA(ServiceBusMessage.class))).thenReturn(this.mono);

        this.sendOperation = new ServiceBusTopicTemplate(mockClientFactory, new ServiceBusMessageConverter());
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

}
