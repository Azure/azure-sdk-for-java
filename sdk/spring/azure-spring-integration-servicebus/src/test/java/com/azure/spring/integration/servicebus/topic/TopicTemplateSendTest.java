// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.topic;

import com.azure.spring.integration.servicebus.ServiceBusTemplateSendTest;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.ITopicClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class TopicTemplateSendTest extends ServiceBusTemplateSendTest<ServiceBusTopicClientFactory, ITopicClient> {

    @BeforeEach
    @Override
    public void setUp() {
        this.mockClientFactory = mock(ServiceBusTopicClientFactory.class);
        this.mockClient = mock(ITopicClient.class);
        when(this.mockClientFactory.getOrCreateSender(anyString())).thenReturn(this.mockClient);
        when(this.mockClient.sendAsync(isA(IMessage.class))).thenReturn(future);

        this.sendOperation = new ServiceBusTopicTemplate(mockClientFactory, new ServiceBusMessageConverter());
    }

    @AfterEach
    public void cleanUp() {
        this.future = new CompletableFuture<>();
    }

}
