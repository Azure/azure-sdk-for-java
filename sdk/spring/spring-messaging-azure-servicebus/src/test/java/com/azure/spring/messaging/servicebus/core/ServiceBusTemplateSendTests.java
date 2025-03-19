// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.messaging.core.SendOperationTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyLong;
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

        ServiceBusEntityType entityType = ServiceBusEntityType.TOPIC;
        this.sendOperation = new ServiceBusTemplate(producerFactory);
        this.sendOperation.setDefaultEntityType(entityType);
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

    @Test
    public void testScheduleMessage() {
        OffsetDateTime offsetDateTime = OffsetDateTime.now().plusSeconds(10);
        when(this.mockSenderClient.scheduleMessage(isA(ServiceBusMessage.class), isA(OffsetDateTime.class))).thenReturn(Mono.just(1L));
        Mono<Long> longMono = this.sendOperation.scheduleMessage(destination, null, message, offsetDateTime);

        assertEquals(longMono.block(), 1L);
        verify(this.mockSenderClient, times(1)).scheduleMessage(isA(ServiceBusMessage.class), isA(OffsetDateTime.class));
    }
    @Test
    public void testCancelScheduledMessage() {
        when(this.mockSenderClient.cancelScheduledMessage(anyLong())).thenReturn(Mono.empty());
        Mono<Void> voidMono = this.sendOperation.cancelScheduledMessage(destination, null, 1);

        assertNull(voidMono.block());
        verify(this.mockSenderClient, times(1)).cancelScheduledMessage(anyLong());
    }

    @Test
    public void testScheduleMessages() {
        OffsetDateTime offsetDateTime = OffsetDateTime.now().plusSeconds(10);
        when(this.mockSenderClient.scheduleMessages(anyIterable(), isA(OffsetDateTime.class))).thenReturn(Flux.just(1L));
        Flux<Long> longFlux = this.sendOperation.scheduleMessages(destination, null, List.of(message), offsetDateTime);

        assertEquals(longFlux.next().block(), 1L);
        verify(this.mockSenderClient, times(1)).scheduleMessages(anyIterable(), isA(OffsetDateTime.class));
    }

    @Test
    public void testCancelScheduledMessages() {
        when(this.mockSenderClient.cancelScheduledMessages(anyIterable())).thenReturn(Mono.empty());
        Mono<Void> voidMono = this.sendOperation.cancelScheduledMessages(destination, null, List.of(1L, 2L));

        assertNull(voidMono.block());
        verify(this.mockSenderClient, times(1)).cancelScheduledMessages(anyIterable());
    }
}
