// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.queue;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.servicebus.core.ServiceBusTemplateSendTest;
import com.azure.spring.servicebus.core.sender.ServiceBusSenderClientFactory;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import com.azure.spring.servicebus.core.processor.ServiceBusQueueProcessorClientFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QueueTemplateSendTest
    extends ServiceBusTemplateSendTest<ServiceBusQueueProcessorClientFactory, ServiceBusSenderClientFactory,ServiceBusSenderAsyncClient> {

    private AutoCloseable closeable;

    @BeforeEach
    @Override
    public void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
        this.mockSenderClientFactory = mock(ServiceBusSenderClientFactory.class);
        this.mockSenderClient = mock(ServiceBusSenderAsyncClient.class);

        when(this.mockSenderClientFactory.createSender(anyString())).thenReturn(this.mockSenderClient);
        when(this.mockSenderClient.sendMessage(isA(ServiceBusMessage.class))).thenReturn(this.mono);

        this.sendOperation = new ServiceBusQueueTemplate(mockSenderClientFactory, mockProcessorClientFactory, new ServiceBusMessageConverter());
    }


    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

}
