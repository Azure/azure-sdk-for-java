// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import com.azure.spring.messaging.core.SendOperationTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
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
    private ServiceBusSessionReceiverClient mockSessionReceiverClient;
    private ServiceBusReceiverClient mockReceiverClient;
    private ServiceBusProducerFactory producerFactory;
    protected String replyEntity = "reply-event-hub";

    @BeforeEach
    public void setUp() {
        this.producerFactory = mock(ServiceBusProducerFactory.class);
        ServiceBusConsumerFactory consumerFactory = mock(ServiceBusConsumerFactory.class);
        this.mockSenderClient = mock(ServiceBusSenderAsyncClient.class);

        this.mockSessionReceiverClient = mock(ServiceBusSessionReceiverClient.class);
        this.mockReceiverClient = mock(ServiceBusReceiverClient.class);
        when(consumerFactory.createReceiver(eq(this.replyEntity), any())).thenReturn(this.mockSessionReceiverClient);
        when(mockSessionReceiverClient.acceptSession(isA(String.class))).thenReturn(mockReceiverClient);

        when(this.producerFactory.createProducer(eq(this.destination), any())).thenReturn(this.mockSenderClient);
        when(this.mockSenderClient.sendMessage(isA(ServiceBusMessage.class))).thenReturn(this.mono);

        this.sendOperation = new ServiceBusTemplate(producerFactory, consumerFactory);

        Map<String, Object> valueMap = new HashMap<>(message.getHeaders());
        valueMap.put(MessageHeaders.REPLY_CHANNEL, replyEntity);
        message = new GenericMessage<>("testPayload", valueMap);

    }

    @Test
    public void testSendAndReceive() {
        when(mockReceiverClient.receiveMessages(1)).thenReturn(mock());
        final ServiceBusReceivedMessage replyMessage = this.sendOperation.sendAndReceive(destination, null, message);

        assertNull(replyMessage);
        verify(mockSenderClient, times(1)).sendMessage(isA(ServiceBusMessage.class));
        verify(mockSessionReceiverClient, times(1)).acceptSession(isA(String.class));
        verify(mockReceiverClient, times(1)).receiveMessages(1);
    }

    @Test
    public void testSendAndReceiveReturnNull() {
        when(mockReceiverClient.receiveMessages(1)).thenReturn(new IterableStream<>(List.of()));
        assertNull(this.sendOperation.sendAndReceive(destination, null, message));
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
