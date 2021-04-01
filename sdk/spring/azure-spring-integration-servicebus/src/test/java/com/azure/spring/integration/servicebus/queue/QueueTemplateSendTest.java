// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.queue;

import com.azure.spring.integration.servicebus.ServiceBusTemplateSendTest;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IQueueClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RunWith(MockitoJUnitRunner.class)
public class QueueTemplateSendTest extends ServiceBusTemplateSendTest<ServiceBusQueueClientFactory, IQueueClient> {

    @BeforeEach
    @Override
    public void setUp() {
        this.mockClientFactory = mock(ServiceBusQueueClientFactory.class);
        this.mockClient = mock(IQueueClient.class);

        when(this.mockClientFactory.getOrCreateSender(anyString())).thenReturn(this.mockClient);
        when(this.mockClient.sendAsync(isA(IMessage.class))).thenReturn(future);

        this.sendOperation = new ServiceBusQueueTemplate(mockClientFactory, new ServiceBusMessageConverter());
    }

    @AfterEach
    public void cleanUp() {
        this.future = new CompletableFuture<>();
    }
}
