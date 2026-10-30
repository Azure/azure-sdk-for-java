// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.DeferOptions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class ServiceBusReceivedMessageAsyncContextTest {
    @Test
    public void settlementsAreColdAndForwardOptions() {
        ServiceBusReceiverAsyncClient receiver = mock(ServiceBusReceiverAsyncClient.class);
        ServiceBusReceivedMessage message = new ServiceBusReceivedMessage(BinaryData.fromString("message"));
        ServiceBusReceivedMessageAsyncContext context
            = new ServiceBusReceivedMessageAsyncContext(receiver, new ServiceBusMessageContext(message));
        when(receiver.getFullyQualifiedNamespace()).thenReturn("namespace.servicebus.windows.net");
        when(receiver.getEntityPath()).thenReturn("topic/subscriptions/subscription");
        CompleteOptions complete = new CompleteOptions();
        AbandonOptions abandon = new AbandonOptions();
        DeferOptions defer = new DeferOptions();
        DeadLetterOptions deadLetter = new DeadLetterOptions();
        Mono<Void> operations = context.complete()
            .then(context.complete(complete))
            .then(context.abandon())
            .then(context.abandon(abandon))
            .then(context.defer())
            .then(context.defer(defer))
            .then(context.deadLetter())
            .then(context.deadLetter(deadLetter));
        verifyNoInteractions(receiver);
        when(receiver.complete(message)).thenReturn(Mono.empty());
        when(receiver.complete(message, complete)).thenReturn(Mono.empty());
        when(receiver.abandon(message)).thenReturn(Mono.empty());
        when(receiver.abandon(message, abandon)).thenReturn(Mono.empty());
        when(receiver.defer(message)).thenReturn(Mono.empty());
        when(receiver.defer(message, defer)).thenReturn(Mono.empty());
        when(receiver.deadLetter(message)).thenReturn(Mono.empty());
        when(receiver.deadLetter(message, deadLetter)).thenReturn(Mono.empty());

        StepVerifier.create(operations).verifyComplete();
        verify(receiver).complete(message);
        verify(receiver).complete(message, complete);
        verify(receiver).abandon(message);
        verify(receiver).abandon(message, abandon);
        verify(receiver).defer(message);
        verify(receiver).defer(message, defer);
        verify(receiver).deadLetter(message);
        verify(receiver).deadLetter(message, deadLetter);
        assertEquals(message, context.getMessage());
        assertEquals("namespace.servicebus.windows.net", context.getFullyQualifiedNamespace());
        assertEquals("topic/subscriptions/subscription", context.getEntityPath());
    }

    @Test
    public void settlementErrorsRemainVisible() {
        ServiceBusReceiverAsyncClient receiver = mock(ServiceBusReceiverAsyncClient.class);
        ServiceBusReceivedMessage message = new ServiceBusReceivedMessage(BinaryData.fromString("message"));
        ServiceBusReceivedMessageAsyncContext context
            = new ServiceBusReceivedMessageAsyncContext(receiver, new ServiceBusMessageContext(message));
        when(receiver.complete(message)).thenReturn(Mono.error(new IllegalStateException("lock lost")));

        StepVerifier.create(context.complete()).expectErrorMessage("lock lost").verify();
    }
}
