// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link ServiceBusSessionReactorReceiver}.
 */
public class ServiceBusSessionReactorReceiverTest {
    private static final String NAMESPACE = "contoso.servicebus.windows.net";
    private static final String ENTITY_PATH = "queue0";
    private static final String SESSION_ID = "1";
    private static final String LINK_NAME = "queue0-session-1";
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusSessionReceiverTest.class);

    private final TestPublisher<AmqpEndpointState> endpointStates = TestPublisher.createCold();
    private final TestPublisher<Message> messagePublisher = TestPublisher.createCold();
    private AutoCloseable autoCloseable;

    @Mock
    private ServiceBusReceiveLink sessionLink;

    @BeforeEach
    public void beforeEach() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        when(sessionLink.receive()).thenReturn(messagePublisher.flux().publishOn(Schedulers.single()));
        when(sessionLink.getHostname()).thenReturn(NAMESPACE);
        when(sessionLink.getEntityPath()).thenReturn(ENTITY_PATH);
        when(sessionLink.getLinkName()).thenReturn(LINK_NAME);
        when(sessionLink.getEndpointStates()).thenReturn(endpointStates.flux());
        when(sessionLink.addCredits(anyInt())).thenReturn(Mono.empty());
        when(sessionLink.closeAsync()).thenReturn(Mono.empty());
        endpointStates.next(AmqpEndpointState.ACTIVE);
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (autoCloseable != null) {
            autoCloseable.close();
        }
        Mockito.framework().clearInlineMock(this);
    }

    @Test
    public void properties() {
        // Arrange
        final Disposable lockRenewDisposable = Disposables.single();
        doNothing().when(sessionLink).dispose();
        final ServiceBusSessionAcquirer.Session session = mock(ServiceBusSessionAcquirer.Session.class);
        when(session.getId()).thenReturn(SESSION_ID);
        when(session.getLink()).thenReturn(sessionLink);
        when(session.beginLockRenew(any(ServiceBusTracer.class), any(Duration.class))).thenReturn(lockRenewDisposable);
        final ServiceBusSessionReactorReceiver receiver = new ServiceBusSessionReactorReceiver(LOGGER,
            mock(ServiceBusTracer.class), session, null, Duration.ofSeconds(1));

        // Act and assert
        try {
            Assertions.assertEquals(SESSION_ID, receiver.getSessionId());
            Assertions.assertEquals(NAMESPACE, receiver.getHostname());
            Assertions.assertEquals(LINK_NAME, receiver.getLinkName());
            Assertions.assertEquals(ENTITY_PATH, receiver.getEntityPath());
        } finally {
            receiver.dispose();
        }
    }

    @Test
    public void disposeResources() {
        // Arrange
        final Disposable lockRenewDisposable = Disposables.single();
        doNothing().when(sessionLink).dispose();
        final ServiceBusSessionAcquirer.Session session = mock(ServiceBusSessionAcquirer.Session.class);
        when(session.getId()).thenReturn(SESSION_ID);
        when(session.getLink()).thenReturn(sessionLink);
        when(session.beginLockRenew(any(ServiceBusTracer.class), any(Duration.class))).thenReturn(lockRenewDisposable);
        final ServiceBusSessionReactorReceiver receiver = new ServiceBusSessionReactorReceiver(LOGGER,
            mock(ServiceBusTracer.class), session, null, Duration.ofSeconds(1));

        // Act
        receiver.dispose();

        // Assert
        verify(sessionLink).dispose();
        Assertions.assertTrue(lockRenewDisposable.isDisposed());
    }

    @Test
    public void receivesThenCompletes() {
        // Arrange
        doNothing().when(sessionLink).dispose();
        final ServiceBusSessionAcquirer.Session session = mock(ServiceBusSessionAcquirer.Session.class);
        when(session.getId()).thenReturn(SESSION_ID);
        when(session.getLink()).thenReturn(sessionLink);
        when(session.beginLockRenew(any(ServiceBusTracer.class), any(Duration.class))).thenReturn(Disposables.single());
        final Message message0 = mock(Message.class);
        final Message message1 = mock(Message.class);
        final ServiceBusSessionReactorReceiver receiver = new ServiceBusSessionReactorReceiver(LOGGER,
            mock(ServiceBusTracer.class), session, null, Duration.ofSeconds(1));

        // Act and assert
        try {
            final Flux<Message> messages = receiver.receive();
            StepVerifier.create(messages)
                .then(() -> messagePublisher.next(message0, message1))
                .then(messagePublisher::complete)
                .assertNext(m -> assertEquals(message0, m))
                .assertNext(m -> assertEquals(message1, m))
                .expectComplete()
                .verify(Duration.ofSeconds(5));
        } finally {
            receiver.dispose();
        }
    }

    @Test
    public void completesOnIdleTimeout() {
        // Arrange
        final Duration idleTimeout = Duration.ofSeconds(3);
        doNothing().when(sessionLink).dispose();
        final ServiceBusSessionAcquirer.Session session = mock(ServiceBusSessionAcquirer.Session.class);
        when(session.getId()).thenReturn(SESSION_ID);
        when(session.getLink()).thenReturn(sessionLink);
        when(session.beginLockRenew(any(ServiceBusTracer.class), any(Duration.class))).thenReturn(Disposables.single());
        final Message message0 = mock(Message.class);
        final Message message1 = mock(Message.class);
        final ServiceBusSessionReactorReceiver receiver = new ServiceBusSessionReactorReceiver(LOGGER,
            mock(ServiceBusTracer.class), session, idleTimeout, Duration.ofSeconds(1));

        // Act and assert
        try {
            final Flux<Message> messages = receiver.receive();
            final Flux<AmqpEndpointState> states = receiver.getEndpointStates();
            StepVerifier.create(messages.takeUntilOther(states.then()))
                .then(() -> messagePublisher.next(message0, message1))
                .assertNext(m -> assertEquals(message0, m))
                .assertNext(m -> assertEquals(message1, m))
                .expectComplete()
                .verify(Duration.ofSeconds(15));
        } finally {
            receiver.dispose();
        }
    }

    @Test
    public void completesOnNoMessagesIdleTimeout() {
        // Arrange
        final Duration idleTimeout = Duration.ofSeconds(3);
        doNothing().when(sessionLink).dispose();
        final ServiceBusSessionAcquirer.Session session = mock(ServiceBusSessionAcquirer.Session.class);
        when(session.getId()).thenReturn(SESSION_ID);
        when(session.getLink()).thenReturn(sessionLink);
        when(session.beginLockRenew(any(ServiceBusTracer.class), any(Duration.class))).thenReturn(Disposables.single());
        final ServiceBusSessionReactorReceiver receiver = new ServiceBusSessionReactorReceiver(LOGGER,
            mock(ServiceBusTracer.class), session, idleTimeout, Duration.ofSeconds(1));

        // Act and assert
        try {
            // Since there are no messages arrives at 'messages' Flux, the endpoint 'states' Flux should terminate after
            // the idle timeout of 1 second.
            final Flux<Message> messages = receiver.receive();
            final Flux<AmqpEndpointState> states = receiver.getEndpointStates();
            StepVerifier.create(messages.takeUntilOther(states.then())).expectComplete().verify(Duration.ofSeconds(15));
        } finally {
            receiver.dispose();
        }
    }
}
