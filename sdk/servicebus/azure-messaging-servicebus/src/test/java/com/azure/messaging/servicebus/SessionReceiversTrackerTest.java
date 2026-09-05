// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SessionsMessagePump.SessionReceiversTracker} session-state routing (#49207): looking up the
 * session receiver by the message's session id, erroring when the session is not (or no longer) tracked, and
 * delegating to the tracked {@link ServiceBusSessionReactorReceiver} otherwise.
 */
class SessionReceiversTrackerTest {
    private static final byte[] STATE = new byte[] { 5, 6, 7 };

    private static SessionsMessagePump.SessionReceiversTracker newTracker() {
        return new SessionsMessagePump.SessionReceiversTracker(new ClientLogger(SessionReceiversTrackerTest.class), 4,
            "fqdn.servicebus.windows.net", "entity", ServiceBusReceiveMode.PEEK_LOCK, null);
    }

    private static ServiceBusReceivedMessage messageForSession(String sessionId) {
        final ServiceBusReceivedMessage message = new ServiceBusReceivedMessage(BinaryData.fromString("data"));
        if (sessionId != null) {
            message.setSessionId(sessionId);
        }
        return message;
    }

    @Test
    void getSessionStateErrorsWhenMessageHasNoSession() {
        StepVerifier.create(newTracker().getSessionState(messageForSession(null)))
            .expectError(IllegalStateException.class)
            .verify();
    }

    @Test
    void getSessionStateErrorsWhenMessageHasEmptySession() {
        StepVerifier.create(newTracker().getSessionState(messageForSession("")))
            .expectErrorMatches(e -> e instanceof IllegalStateException
                && e.getMessage().contains("was not received from a session-enabled entity"))
            .verify();
    }

    @Test
    void setSessionStateErrorsWhenMessageHasEmptySession() {
        StepVerifier.create(newTracker().setSessionState(messageForSession(""), STATE))
            .expectErrorMatches(e -> e instanceof IllegalStateException
                && e.getMessage().contains("was not received from a session-enabled entity"))
            .verify();
    }

    @Test
    void getSessionStateErrorsWhenSessionNotTracked() {
        StepVerifier.create(newTracker().getSessionState(messageForSession("absent-session")))
            .expectError(IllegalStateException.class)
            .verify();
    }

    @Test
    void setSessionStateErrorsWhenSessionNotTracked() {
        StepVerifier.create(newTracker().setSessionState(messageForSession("absent-session"), STATE))
            .expectError(IllegalStateException.class)
            .verify();
    }

    @Test
    void getSessionStateDelegatesToTrackedReceiver() {
        final SessionsMessagePump.SessionReceiversTracker tracker = newTracker();
        final ServiceBusSessionReactorReceiver receiver = mock(ServiceBusSessionReactorReceiver.class);
        when(receiver.getSessionId()).thenReturn("s1");
        when(receiver.getSessionState()).thenReturn(Mono.just(STATE));
        tracker.track(receiver);

        StepVerifier.create(tracker.getSessionState(messageForSession("s1")))
            .expectNextMatches(actual -> Arrays.equals(STATE, actual))
            .verifyComplete();
        verify(receiver).getSessionState();
    }

    @Test
    void setSessionStateDelegatesToTrackedReceiver() {
        final SessionsMessagePump.SessionReceiversTracker tracker = newTracker();
        final ServiceBusSessionReactorReceiver receiver = mock(ServiceBusSessionReactorReceiver.class);
        when(receiver.getSessionId()).thenReturn("s1");
        when(receiver.setSessionState(STATE)).thenReturn(Mono.empty());
        tracker.track(receiver);

        StepVerifier.create(tracker.setSessionState(messageForSession("s1"), STATE)).verifyComplete();
        verify(receiver).setSessionState(STATE);
    }

    /**
     * Session-id lookup is case-insensitive: a receiver tracked under one casing is found by a message whose session
     * id differs only in case (mirrors how {@code track()} and the settlement path key the receivers map).
     */
    @Test
    void getSessionStateLookupIsCaseInsensitive() {
        final SessionsMessagePump.SessionReceiversTracker tracker = newTracker();
        final ServiceBusSessionReactorReceiver receiver = mock(ServiceBusSessionReactorReceiver.class);
        when(receiver.getSessionId()).thenReturn("Session-A");
        when(receiver.getSessionState()).thenReturn(Mono.just(STATE));
        tracker.track(receiver);

        StepVerifier.create(tracker.getSessionState(messageForSession("SESSION-A")))
            .expectNextMatches(actual -> Arrays.equals(STATE, actual))
            .verifyComplete();
    }
}
