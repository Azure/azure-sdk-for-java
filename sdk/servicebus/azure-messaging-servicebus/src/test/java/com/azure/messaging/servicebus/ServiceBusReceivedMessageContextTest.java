// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServiceBusReceivedMessageContextTest {
    @Test
    public void deferShouldDelegateToAsyncClientAndBlock() {
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);

        final AtomicBoolean subscribed = new AtomicBoolean(false);
        final Mono<Void> mono = createMonoThatTracksSubscription(subscribed);

        when(asyncClient.defer(any(), any())).thenReturn(mono);

        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithReceiverClient(asyncClient);
        receivedMessageContext.defer();

        verify(asyncClient, times(1)).defer(any(), any());
        assertTrue(subscribed.get(), "Mono was not consumed, so block() likely was not called");
    }

    @Test
    public void deferShouldDelegateToSessionTrackerAndBlock() {
        final SessionsMessagePump.SessionReceiversTracker tracker = mock(SessionsMessagePump.SessionReceiversTracker.class);

        final AtomicBoolean subscribed = new AtomicBoolean(false);
        final Mono<Void> mono = createMonoThatTracksSubscription(subscribed);

        when(tracker.defer(any(), any())).thenReturn(mono);

        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithSessionReceiversTracker(tracker);
        receivedMessageContext.defer();

        verify(tracker, times(1)).defer(any(), any());
        assertTrue(subscribed.get(), "Mono was not consumed, so block() likely was not called");
    }

    @Test
    public void deferWithNullAsOptionsShouldThrowAnException() {
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);

        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithReceiverClient(asyncClient);

        final Exception result = assertThrows(NullPointerException.class, () -> receivedMessageContext.defer(null));
        assertTrue(result.getMessage().contains("'options' cannot be null"));
    }

    @Test
    public void deferShouldDelegateToAsyncClientAndNotBlock() {
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);

        final AtomicBoolean subscribed = new AtomicBoolean(false);
        final Mono<Void> mono = createMonoThatTracksSubscription(subscribed);

        when(asyncClient.defer(any(), any())).thenReturn(mono);

        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithReceiverClient(asyncClient);
        final Mono<Void> result = receivedMessageContext.deferAsync();
        assertFalse(subscribed.get(), "Mono was consumed which should not be the case for the async call");

        result.block();
        assertTrue(subscribed.get());
    }

    @Test
    public void deferShouldDelegateToSessionTrackerAndNotBlock() {
        final SessionsMessagePump.SessionReceiversTracker tracker = mock(SessionsMessagePump.SessionReceiversTracker.class);

        final AtomicBoolean subscribed = new AtomicBoolean(false);
        final Mono<Void> mono = createMonoThatTracksSubscription(subscribed);

        when(tracker.defer(any(), any())).thenReturn(mono);

        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithSessionReceiversTracker(tracker);
        final Mono<Void> result = receivedMessageContext.deferAsync();
        assertFalse(subscribed.get(), "Mono was consumed which should not be the case for the async call");

        result.block();
        assertTrue(subscribed.get());
    }

    @Test
    public void deferAsyncWithNullAsOptionsShouldThrowAnException() {
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithReceiverClient(asyncClient);

        final Exception result = assertThrows(NullPointerException.class, () -> receivedMessageContext.deferAsync(null));
        assertTrue(result.getMessage().contains("'options' cannot be null"));
    }

    @Test
    public void completeShouldDelegateToAsyncClientAndBlock() {
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);

        final AtomicBoolean subscribed = new AtomicBoolean(false);
        final Mono<Void> mono = createMonoThatTracksSubscription(subscribed);

        when(asyncClient.complete(any(), any())).thenReturn(mono);

        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithReceiverClient(asyncClient);
        receivedMessageContext.complete();

        verify(asyncClient, times(1)).complete(any(), any());
        assertTrue(subscribed.get(), "Mono was not consumed, so block() likely was not called");
    }

    @Test
    public void completeShouldDelegateToSessionsTrackerAndBlock() {
        final SessionsMessagePump.SessionReceiversTracker tracker = mock(SessionsMessagePump.SessionReceiversTracker.class);

        final AtomicBoolean subscribed = new AtomicBoolean(false);
        final Mono<Void> mono = createMonoThatTracksSubscription(subscribed);

        when(tracker.complete(any(), any())).thenReturn(mono);

        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithSessionReceiversTracker(tracker);
        receivedMessageContext.complete();

        verify(tracker, times(1)).complete(any(), any());
        assertTrue(subscribed.get(), "Mono was not consumed, so block() likely was not called");
    }

    @Test
    public void completeWithNullAsOptionsShouldThrowAnException() {
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithReceiverClient(asyncClient);

        final Exception result = assertThrows(NullPointerException.class, () -> receivedMessageContext.complete(null));
        assertTrue(result.getMessage().contains("'options' cannot be null"));
    }

    @Test
    public void completeAsyncShouldDelegateToAsyncClientAndNotBlock() {
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);

        final AtomicBoolean subscribed = new AtomicBoolean(false);
        final Mono<Void> mono = createMonoThatTracksSubscription(subscribed);

        when(asyncClient.complete(any(), any())).thenReturn(mono);

        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithReceiverClient(asyncClient);
        final Mono<Void> result = receivedMessageContext.completeAsync();
        assertFalse(subscribed.get(), "Mono was consumed which should not be the case for the async call");

        result.block();
        assertTrue(subscribed.get());
    }

    @Test
    public void completeAsyncShouldDelegateToSessionsTrackerAndNotBlock() {
        final SessionsMessagePump.SessionReceiversTracker tracker = mock(SessionsMessagePump.SessionReceiversTracker.class);

        final AtomicBoolean subscribed = new AtomicBoolean(false);
        final Mono<Void> mono = createMonoThatTracksSubscription(subscribed);

        when(tracker.complete(any(), any())).thenReturn(mono);

        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithSessionReceiversTracker(tracker);
        final Mono<Void> result = receivedMessageContext.completeAsync();
        assertFalse(subscribed.get(), "Mono was consumed which should not be the case for the async call");

        result.block();
        assertTrue(subscribed.get());
    }

    @Test
    public void completeAsyncWithNullAsOptionsShouldThrowAnException() {
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithReceiverClient(asyncClient);

        final Exception result = assertThrows(NullPointerException.class, () -> receivedMessageContext.completeAsync(null));
        assertTrue(result.getMessage().contains("'options' cannot be null"));
    }

    @Test
    public void abandonShouldDelegateToAsyncClientAndBlock() {
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);

        final AtomicBoolean subscribed = new AtomicBoolean(false);
        final Mono<Void> mono = createMonoThatTracksSubscription(subscribed);

        when(asyncClient.abandon(any(), any())).thenReturn(mono);

        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithReceiverClient(asyncClient);
        receivedMessageContext.abandon();

        verify(asyncClient, times(1)).abandon(any(), any());
        assertTrue(subscribed.get(), "Mono was not consumed, so block() likely was not called");
    }

    @Test
    public void abandonShouldDelegateToSessionTrackerAndBlock() {
        final SessionsMessagePump.SessionReceiversTracker tracker = mock(SessionsMessagePump.SessionReceiversTracker.class);

        final AtomicBoolean subscribed = new AtomicBoolean(false);
        final Mono<Void> mono = createMonoThatTracksSubscription(subscribed);

        when(tracker.abandon(any(), any())).thenReturn(mono);

        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithSessionReceiversTracker(tracker);
        receivedMessageContext.abandon();

        verify(tracker, times(1)).abandon(any(), any());
        assertTrue(subscribed.get(), "Mono was not consumed, so block() likely was not called");
    }

    @Test
    public void abandonWithNullAsOptionsShouldThrowAnException() {
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithReceiverClient(asyncClient);

        final Exception result = assertThrows(NullPointerException.class, () -> receivedMessageContext.abandon(null));
        assertTrue(result.getMessage().contains("'options' cannot be null"));
    }

    @Test
    public void abandonShouldDelegateToAsyncClientAndNotBlock() {
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);

        final AtomicBoolean subscribed = new AtomicBoolean(false);
        final Mono<Void> mono = createMonoThatTracksSubscription(subscribed);

        when(asyncClient.abandon(any(), any())).thenReturn(mono);

        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithReceiverClient(asyncClient);
        final Mono<Void> result = receivedMessageContext.abandonAsync();
        assertFalse(subscribed.get(), "Mono was consumed which should not be the case for the async call");

        result.block();
        assertTrue(subscribed.get());
    }

    @Test
    public void abandonShouldDelegateToSessionTrackerAndNotBlock() {
        final SessionsMessagePump.SessionReceiversTracker tracker = mock(SessionsMessagePump.SessionReceiversTracker.class);

        final AtomicBoolean subscribed = new AtomicBoolean(false);
        final Mono<Void> mono = createMonoThatTracksSubscription(subscribed);

        when(tracker.abandon(any(), any())).thenReturn(mono);

        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithSessionReceiversTracker(tracker);
        final Mono<Void> result = receivedMessageContext.abandonAsync();
        assertFalse(subscribed.get(), "Mono was consumed which should not be the case for the async call");

        result.block();
        assertTrue(subscribed.get());
    }

    @Test
    public void abandonAsyncWithNullAsOptionsShouldThrowAnException() {
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithReceiverClient(asyncClient);

        final Exception result = assertThrows(NullPointerException.class, () -> receivedMessageContext.abandonAsync(null));
        assertTrue(result.getMessage().contains("'options' cannot be null"));
    }

    @Test
    public void deadLetterShouldDelegateToAsyncClientAndBlock() {
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);

        final AtomicBoolean subscribed = new AtomicBoolean(false);
        final Mono<Void> mono = createMonoThatTracksSubscription(subscribed);

        when(asyncClient.deadLetter(any(), any())).thenReturn(mono);

        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithReceiverClient(asyncClient);
        receivedMessageContext.deadLetter();

        verify(asyncClient, times(1)).deadLetter(any(), any());
        assertTrue(subscribed.get(), "Mono was not consumed, so block() likely was not called");
    }

    @Test
    public void deadLetterShouldDelegateToSessionTrackerAndBlock() {
        final SessionsMessagePump.SessionReceiversTracker tracker = mock(SessionsMessagePump.SessionReceiversTracker.class);

        final AtomicBoolean subscribed = new AtomicBoolean(false);
        final Mono<Void> mono = createMonoThatTracksSubscription(subscribed);

        when(tracker.deadLetter(any(), any())).thenReturn(mono);

        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithSessionReceiversTracker(tracker);
        receivedMessageContext.deadLetter();

        verify(tracker, times(1)).deadLetter(any(), any());
        assertTrue(subscribed.get(), "Mono was not consumed, so block() likely was not called");
    }

    @Test
    public void deadLetterWithNullAsOptionsShouldThrowAnException() {
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithReceiverClient(asyncClient);

        final Exception result = assertThrows(NullPointerException.class, () -> receivedMessageContext.deadLetter(null));
        assertTrue(result.getMessage().contains("'options' cannot be null"));
    }

    @Test
    public void deadLetterShouldDelegateToAsyncClientAndNotBlock() {
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);

        final AtomicBoolean subscribed = new AtomicBoolean(false);
        final Mono<Void> mono = createMonoThatTracksSubscription(subscribed);

        when(asyncClient.deadLetter(any(), any())).thenReturn(mono);

        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithReceiverClient(asyncClient);
        final Mono<Void> result = receivedMessageContext.deadLetterAsync();
        assertFalse(subscribed.get(), "Mono was consumed which should not be the case for the async call");

        result.block();
        assertTrue(subscribed.get());
    }

    @Test
    public void deadLetterShouldDelegateToSessionTrackerAndNotBlock() {
        final SessionsMessagePump.SessionReceiversTracker tracker = mock(SessionsMessagePump.SessionReceiversTracker.class);

        final AtomicBoolean subscribed = new AtomicBoolean(false);
        final Mono<Void> mono = createMonoThatTracksSubscription(subscribed);

        when(tracker.deadLetter(any(), any())).thenReturn(mono);

        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithSessionReceiversTracker(tracker);
        final Mono<Void> result = receivedMessageContext.deadLetterAsync();
        assertFalse(subscribed.get(), "Mono was consumed which should not be the case for the async call");

        result.block();
        assertTrue(subscribed.get());
    }

    @Test
    public void deadLetterAsyncWithNullAsOptionsShouldThrowAnException() {
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        final ServiceBusReceivedMessageContext receivedMessageContext = createContextWithReceiverClient(asyncClient);

        final Exception result = assertThrows(NullPointerException.class, () -> receivedMessageContext.deadLetterAsync(null));
        assertTrue(result.getMessage().contains("'options' cannot be null"));
    }

    @Test
    public void getEntityPathReturnsValueFromAsyncClient() {
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        when(asyncClient.getEntityPath()).thenReturn("test-entityPath");
        when(asyncClient.getFullyQualifiedNamespace()).thenReturn("test-fullyQualifiedNamespace");

        final ServiceBusReceivedMessageContext context = createContextWithReceiverClient(asyncClient);

        assertEquals("test-entityPath", context.getEntityPath());
        assertEquals("test-fullyQualifiedNamespace", context.getFullyQualifiedNamespace());
    }

    @Test
    public void getEntityPathReturnsValueFromSessionTracker() {
        final SessionsMessagePump.SessionReceiversTracker tracker = mock(SessionsMessagePump.SessionReceiversTracker.class);
        when(tracker.getEntityPath()).thenReturn("test-entityPath");
        when(tracker.getFullyQualifiedNamespace()).thenReturn("test-fullyQualifiedNamespace");

        final ServiceBusReceivedMessageContext context = createContextWithSessionReceiversTracker(tracker);

        assertEquals("test-entityPath", context.getEntityPath());
        assertEquals("test-fullyQualifiedNamespace", context.getFullyQualifiedNamespace());
    }

    private Mono<Void> createMonoThatTracksSubscription(AtomicBoolean isSubscribed) {
        return Mono.create(sink -> {
            isSubscribed.set(true);
            sink.success();
        });
    }

    private ServiceBusReceivedMessageContext createContextWithReceiverClient(ServiceBusReceiverAsyncClient client) {
        final ServiceBusMessageContext messageContext = mock(ServiceBusMessageContext.class);
        when(messageContext.getMessage()).thenReturn(mock(ServiceBusReceivedMessage.class));
        return new ServiceBusReceivedMessageContext(client, messageContext);
    }

    private ServiceBusReceivedMessageContext createContextWithSessionReceiversTracker(SessionsMessagePump.SessionReceiversTracker sessionReceiversTracker) {
        final ServiceBusMessageContext messageContext = mock(ServiceBusMessageContext.class);
        when(messageContext.getMessage()).thenReturn(mock(ServiceBusReceivedMessage.class));
        return new ServiceBusReceivedMessageContext(sessionReceiversTracker, messageContext);
    }
}
