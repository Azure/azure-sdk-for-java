// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.messaging.core.SendOperationTests;
import org.junit.jupiter.api.BeforeEach;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test cases to test service bus send operations.
 *
 */
public class ServiceBusTemplateSendTests extends SendOperationTests<ServiceBusTemplate> {

    private ServiceBusSenderAsyncClient mockSenderClient;
    private ServiceBusProducerFactory producerFactory;

    @BeforeEach
    public void setUp() {
        this.producerFactory = mock(ServiceBusProducerFactory.class);
        this.mockSenderClient = mock(ServiceBusSenderAsyncClient.class);

        when(this.producerFactory.createProducer(eq(this.destination), any())).thenReturn(this.mockSenderClient);
        when(this.mockSenderClient.sendMessage(isA(ServiceBusMessage.class))).thenReturn(this.mono);

        this.sendOperation = new ServiceBusTemplate(producerFactory);
    }

    @Override
    protected void setupError(String errorMessage) {
        when(this.mockSenderClient.sendMessage(isA(ServiceBusMessage.class))).thenReturn(Mono.error(new IllegalArgumentException(
            errorMessage)));
    }

    @Override
    protected void verifySendCalled(int times) {
        verify(this.mockSenderClient, times(times)).sendMessage(isA(ServiceBusMessage.class));
    }

    @Override
    protected void verifyGetClientCreator(int times) {
        verify(this.producerFactory, times(times)).createProducer(anyString(), any());
    }

}
