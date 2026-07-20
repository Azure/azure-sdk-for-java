// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ServiceBusReceivedMessageContext}, focused on the session-state operations exposed to a
 * {@link ServiceBusProcessorClient} message handler. Covers both delegation branches: the receiver-client path
 * (non-session and V1 processors) and the V2 session-processor path ({@code SessionsMessagePump}).
 */
class ServiceBusReceivedMessageContextTest {
    private static final byte[] STATE = new byte[] { 10, 111, 23 };

    @Mock
    private ServiceBusReceiverAsyncClient receiverClient;

    private AutoCloseable mocksCloseable;
    private ServiceBusReceivedMessage message;
    private ServiceBusMessageContext messageContext;

    @BeforeEach
    void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
        message = new ServiceBusReceivedMessage(BinaryData.fromString("some-data"));
        messageContext = new ServiceBusMessageContext(message);
    }

    @AfterEach
    void teardown() throws Exception {
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    // --- Receiver-client path: non-session processor and V1 processor ---

    @Test
    void getSessionStateDelegatesToReceiver() {
        when(receiverClient.getSessionState(message)).thenReturn(Mono.just(STATE));
        final ServiceBusReceivedMessageContext context
            = new ServiceBusReceivedMessageContext(receiverClient, messageContext);

        assertArrayEquals(STATE, context.getSessionState());
        verify(receiverClient).getSessionState(message);
    }

    @Test
    void getSessionStateReturnsNullWhenNoState() {
        when(receiverClient.getSessionState(message)).thenReturn(Mono.empty());
        final ServiceBusReceivedMessageContext context
            = new ServiceBusReceivedMessageContext(receiverClient, messageContext);

        assertNull(context.getSessionState());
    }

    @Test
    void setSessionStateDelegatesToReceiver() {
        when(receiverClient.setSessionState(message, STATE)).thenReturn(Mono.empty());
        final ServiceBusReceivedMessageContext context
            = new ServiceBusReceivedMessageContext(receiverClient, messageContext);

        context.setSessionState(STATE);

        verify(receiverClient).setSessionState(message, STATE);
    }

    /**
     * Asymmetry test: on a non-session entity the underlying receiver rejects session-state reads, and the context
     * must surface that {@link IllegalStateException} rather than silently succeeding.
     */
    @Test
    void getSessionStateOnNonSessionEntityThrows() {
        when(receiverClient.getSessionState(message))
            .thenReturn(Mono.error(new IllegalStateException("Cannot get session state on a non-session receiver.")));
        final ServiceBusReceivedMessageContext context
            = new ServiceBusReceivedMessageContext(receiverClient, messageContext);

        assertThrows(IllegalStateException.class, context::getSessionState);
    }

    /**
     * Asymmetry test: on a non-session entity the underlying receiver rejects session-state writes, and the context
     * must surface that {@link IllegalStateException} rather than silently succeeding.
     */
    @Test
    void setSessionStateOnNonSessionEntityThrows() {
        when(receiverClient.setSessionState(message, STATE))
            .thenReturn(Mono.error(new IllegalStateException("Cannot set session state on a non-session receiver.")));
        final ServiceBusReceivedMessageContext context
            = new ServiceBusReceivedMessageContext(receiverClient, messageContext);

        assertThrows(IllegalStateException.class, () -> context.setSessionState(STATE));
    }

    // --- Session-processor path: V2 SessionsMessagePump (the #49207 scenario) ---

    @Test
    void getSessionStateDelegatesToSessionTracker() {
        final SessionsMessagePump.SessionReceiversTracker tracker
            = mock(SessionsMessagePump.SessionReceiversTracker.class);
        when(tracker.getSessionState(message)).thenReturn(Mono.just(STATE));
        final ServiceBusReceivedMessageContext context = new ServiceBusReceivedMessageContext(tracker, messageContext);

        assertArrayEquals(STATE, context.getSessionState());
        verify(tracker).getSessionState(message);
    }

    @Test
    void getSessionStateViaSessionTrackerReturnsNullWhenNoState() {
        final SessionsMessagePump.SessionReceiversTracker tracker
            = mock(SessionsMessagePump.SessionReceiversTracker.class);
        when(tracker.getSessionState(message)).thenReturn(Mono.empty());
        final ServiceBusReceivedMessageContext context = new ServiceBusReceivedMessageContext(tracker, messageContext);

        assertNull(context.getSessionState());
    }

    @Test
    void setSessionStateDelegatesToSessionTracker() {
        final SessionsMessagePump.SessionReceiversTracker tracker
            = mock(SessionsMessagePump.SessionReceiversTracker.class);
        when(tracker.setSessionState(message, STATE)).thenReturn(Mono.empty());
        final ServiceBusReceivedMessageContext context = new ServiceBusReceivedMessageContext(tracker, messageContext);

        context.setSessionState(STATE);

        verify(tracker).setSessionState(message, STATE);
    }

    /**
     * Asymmetry test: when the session that delivered the message is no longer held by the processor, the tracker
     * errors and the context must surface that {@link IllegalStateException} for a read.
     */
    @Test
    void getSessionStateThrowsWhenSessionNoLongerActive() {
        final SessionsMessagePump.SessionReceiversTracker tracker
            = mock(SessionsMessagePump.SessionReceiversTracker.class);
        when(tracker.getSessionState(message))
            .thenReturn(Mono.error(new IllegalStateException("the session is no longer active for this processor.")));
        final ServiceBusReceivedMessageContext context = new ServiceBusReceivedMessageContext(tracker, messageContext);

        assertThrows(IllegalStateException.class, context::getSessionState);
    }

    /**
     * Asymmetry test: when the session that delivered the message is no longer held by the processor, the tracker
     * errors and the context must surface that {@link IllegalStateException} for a write.
     */
    @Test
    void setSessionStateThrowsWhenSessionNoLongerActive() {
        final SessionsMessagePump.SessionReceiversTracker tracker
            = mock(SessionsMessagePump.SessionReceiversTracker.class);
        when(tracker.setSessionState(message, STATE))
            .thenReturn(Mono.error(new IllegalStateException("the session is no longer active for this processor.")));
        final ServiceBusReceivedMessageContext context = new ServiceBusReceivedMessageContext(tracker, messageContext);

        assertThrows(IllegalStateException.class, () -> context.setSessionState(STATE));
    }
}
