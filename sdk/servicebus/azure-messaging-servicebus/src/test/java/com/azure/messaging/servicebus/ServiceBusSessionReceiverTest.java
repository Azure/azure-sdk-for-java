// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link ServiceBusSessionReceiver}.
 */
public class ServiceBusSessionReceiverTest {
    private static final String NAMESPACE = "my-namespace-foo.net";
    private static final String ENTITY_PATH = "queue-name";
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusSessionReceiverTest.class);
    private static final Duration NO_SESSION_IDLE_TIMEOUT = null;

    private final TestPublisher<AmqpEndpointState> endpointProcessor = TestPublisher.createCold();
    private final TestPublisher<Message> messagePublisher = TestPublisher.createCold();

    private AutoCloseable autoCloseable;

    @Mock
    private ServiceBusReceiveLink amqpReceiveLink;
    @Mock
    private ServiceBusAmqpConnection connection;
    @Mock
    private MessageSerializer messageSerializer;

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        LOGGER.info("===== [{}] Setting up. =====", testInfo.getDisplayName());

        autoCloseable = MockitoAnnotations.openMocks(this);

        when(amqpReceiveLink.receive()).thenReturn(messagePublisher.flux().publishOn(Schedulers.single()));

        when(amqpReceiveLink.getHostname()).thenReturn(NAMESPACE);
        when(amqpReceiveLink.getEntityPath()).thenReturn(ENTITY_PATH);
        when(amqpReceiveLink.getEndpointStates()).thenReturn(endpointProcessor.flux());
        when(amqpReceiveLink.addCredits(anyInt())).thenReturn(Mono.empty());

        endpointProcessor.next(AmqpEndpointState.ACTIVE);
    }

    @AfterEach
    public void afterEach(TestInfo testInfo) throws Exception {
        LOGGER.info("===== [{}] Tearing down. =====", testInfo.getDisplayName());

        if (autoCloseable != null) {
            autoCloseable.close();
        }

        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Verify that the properties returned are correct.
     */
    @Test
    public void getsProperties() {
        // Arrange
        final String linkName = "my-link-name";
        final String sessionId = "test-session-id";
        final OffsetDateTime sessionLockedUntil = OffsetDateTime.now().plus(Duration.ofSeconds(30));

        when(amqpReceiveLink.getLinkName()).thenReturn(linkName);
        when(amqpReceiveLink.getSessionId()).thenReturn(Mono.just(sessionId));
        when(amqpReceiveLink.getSessionLockedUntil())
            .thenAnswer(invocation -> Mono.defer(() -> Mono.just(sessionLockedUntil)));

        when(amqpReceiveLink.closeAsync()).thenReturn(Mono.empty());

        final AmqpRetryOptions retryOptions = new AmqpRetryOptions();
        final Scheduler scheduler = Schedulers.boundedElastic();
        final Duration maxSessionRenewalDuration = Duration.ofMinutes(5);

        // Act
        final ServiceBusSessionReceiver sessionReceiver
            = new ServiceBusSessionReceiver(sessionId, amqpReceiveLink, messageSerializer, retryOptions, 1, scheduler,
                unused -> renewSessionLock(Duration.ofMinutes(1)), maxSessionRenewalDuration, NO_SESSION_IDLE_TIMEOUT);

        // Assert
        assertEquals(sessionId, sessionReceiver.getSessionId());
        assertEquals(linkName, sessionReceiver.getLinkName());
    }

    /**
     * Verify that it can receive messages and completes close operation.
     */
    @Test
    public void receivesMessages() {
        // Arrange
        final String linkName = "my-link-name";
        final String sessionId = "test-session-id";
        final OffsetDateTime sessionLockedUntil = OffsetDateTime.now().plus(Duration.ofSeconds(30));

        final String lockToken = "a-lock-token";
        final String lockToken2 = "a-lock-token2";
        final Message message = mock(Message.class);
        final Message message2 = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage receivedMessage2 = mock(ServiceBusReceivedMessage.class);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);
        when(messageSerializer.deserialize(message2, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage2);

        when(receivedMessage.getSessionId()).thenReturn(sessionId);
        when(receivedMessage.getLockToken()).thenReturn(lockToken);

        when(receivedMessage2.getSessionId()).thenReturn(sessionId);
        when(receivedMessage2.getLockToken()).thenReturn(lockToken2);

        when(amqpReceiveLink.getLinkName()).thenReturn(linkName);
        when(amqpReceiveLink.getSessionId()).thenReturn(Mono.just(sessionId));
        when(amqpReceiveLink.getSessionLockedUntil())
            .thenAnswer(invocation -> Mono.defer(() -> Mono.just(sessionLockedUntil)));

        when(amqpReceiveLink.updateDisposition(lockToken, Accepted.getInstance())).thenReturn(Mono.empty());
        when(amqpReceiveLink.updateDisposition(lockToken2, Accepted.getInstance())).thenReturn(Mono.empty());

        when(amqpReceiveLink.closeAsync()).thenReturn(Mono.empty());

        final AmqpRetryOptions retryOptions = new AmqpRetryOptions();
        final Scheduler scheduler = Schedulers.boundedElastic();
        final Duration maxSessionRenewalDuration = Duration.ofMinutes(5);
        final ServiceBusSessionReceiver sessionReceiver
            = new ServiceBusSessionReceiver(sessionId, amqpReceiveLink, messageSerializer, retryOptions, 1, scheduler,
                unused -> renewSessionLock(Duration.ofMinutes(1)), maxSessionRenewalDuration, NO_SESSION_IDLE_TIMEOUT);

        // Act & Assert
        try {
            StepVerifier.create(sessionReceiver.receive()).then(() -> {
                messagePublisher.next(message, message2);
                messagePublisher.complete();
            }).assertNext(actual -> {
                assertEquals(receivedMessage, actual.getMessage());
                assertEquals(sessionId, actual.getSessionId());
            }).assertNext(actual -> {
                assertEquals(receivedMessage2, actual.getMessage());
                assertEquals(sessionId, actual.getSessionId());
            }).expectComplete().verify(Duration.ofSeconds(10));
        } finally {
            sessionReceiver.close();
        }
    }

    /**
     * Verify that it can receive messages and when the link is idle long enough, it will close the message flux.
     * Simulates rolling session receiver.
     */
    @Test
    public void disposesOnIdle() {
        // Arrange
        final String linkName = "my-link-name";
        final String sessionId = "test-session-id";
        final OffsetDateTime sessionLockedUntil = OffsetDateTime.now().plus(Duration.ofSeconds(30));

        final String lockToken = "a-lock-token";
        final String lockToken2 = "a-lock-token2";
        final Message message = mock(Message.class);
        final Message message2 = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage receivedMessage2 = mock(ServiceBusReceivedMessage.class);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);
        when(messageSerializer.deserialize(message2, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage2);

        when(receivedMessage.getSessionId()).thenReturn(sessionId);
        when(receivedMessage.getLockToken()).thenReturn(lockToken);

        when(receivedMessage2.getSessionId()).thenReturn(sessionId);
        when(receivedMessage2.getLockToken()).thenReturn(lockToken2);

        when(amqpReceiveLink.getLinkName()).thenReturn(linkName);
        when(amqpReceiveLink.getSessionId()).thenReturn(Mono.just(sessionId));
        when(amqpReceiveLink.getSessionLockedUntil())
            .thenAnswer(invocation -> Mono.defer(() -> Mono.just(sessionLockedUntil)));

        when(amqpReceiveLink.updateDisposition(lockToken, Accepted.getInstance())).thenReturn(Mono.empty());
        when(amqpReceiveLink.updateDisposition(lockToken2, Accepted.getInstance())).thenReturn(Mono.empty());

        when(amqpReceiveLink.closeAsync()).thenReturn(Mono.empty());

        // Wait 5 seconds for another message before disposing of the link.
        final Duration waitTime = Duration.ofSeconds(3);
        final Duration halfWaitTime = Duration.ofSeconds(waitTime.getSeconds() / 2);
        final Duration timeout = Duration.ofSeconds(waitTime.getSeconds() * 3);
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(Duration.ofMinutes(10));
        final Scheduler scheduler = Schedulers.boundedElastic();
        final Duration maxSessionRenewalDuration = Duration.ofMinutes(5);
        final ServiceBusSessionReceiver sessionReceiver
            = new ServiceBusSessionReceiver(sessionId, amqpReceiveLink, messageSerializer, retryOptions, 1, scheduler,
                unused -> renewSessionLock(Duration.ofMinutes(1)), maxSessionRenewalDuration, waitTime);

        // Act & Assert
        try {
            StepVerifier.create(sessionReceiver.receive())
                .then(() -> messagePublisher.next(message))
                .assertNext(actual -> {
                    assertEquals(receivedMessage, actual.getMessage());
                    assertEquals(sessionId, actual.getSessionId());
                })
                .thenAwait(halfWaitTime)
                .then(() -> messagePublisher.next(message2))
                .assertNext(actual -> {
                    assertEquals(receivedMessage2, actual.getMessage());
                    assertEquals(sessionId, actual.getSessionId());
                })
                .thenAwait(waitTime)
                .expectComplete()
                .verify(timeout);
        } finally {
            sessionReceiver.close();
        }
    }

    /**
     * Verifies that the lock exists until its disposition is updated.
     */
    @Test
    public void removesLockOnUpdateDisposition() {
        // Arrange
        final String linkName = "my-link-name";
        final String sessionId = "test-session-id";
        final OffsetDateTime sessionLockedUntil = OffsetDateTime.now().plus(Duration.ofSeconds(30));
        final OffsetDateTime messageLockedUntil = sessionLockedUntil.plusSeconds(-1);

        final String lockToken = "a-lock-token";
        final Message message = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);

        when(receivedMessage.getSessionId()).thenReturn(sessionId);
        when(receivedMessage.getLockToken()).thenReturn(lockToken);
        when(receivedMessage.getLockedUntil()).thenReturn(messageLockedUntil);

        when(amqpReceiveLink.getLinkName()).thenReturn(linkName);
        when(amqpReceiveLink.getSessionId()).thenReturn(Mono.just(sessionId));
        when(amqpReceiveLink.getSessionLockedUntil())
            .thenAnswer(invocation -> Mono.defer(() -> Mono.just(sessionLockedUntil)));

        when(amqpReceiveLink.updateDisposition(lockToken, Accepted.getInstance())).thenReturn(Mono.empty());

        when(amqpReceiveLink.closeAsync()).thenReturn(Mono.empty());

        final AmqpRetryOptions retryOptions = new AmqpRetryOptions();
        final Scheduler scheduler = Schedulers.boundedElastic();
        final Duration maxSessionRenewalDuration = Duration.ofMinutes(5);
        final ServiceBusSessionReceiver sessionReceiver
            = new ServiceBusSessionReceiver(sessionId, amqpReceiveLink, messageSerializer, retryOptions, 1, scheduler,
                unused -> renewSessionLock(Duration.ofMinutes(1)), maxSessionRenewalDuration, NO_SESSION_IDLE_TIMEOUT);

        // Act & Assert
        try {
            StepVerifier.create(sessionReceiver.receive()).then(() -> {
                assertFalse(sessionReceiver.containsLockToken(lockToken));

                messagePublisher.next(message);
            }).assertNext(actual -> {
                assertEquals(receivedMessage, actual.getMessage());
                assertEquals(sessionId, actual.getSessionId());
                assertEquals(messageLockedUntil, actual.getMessage().getLockedUntil());
            }).thenCancel().verify(Duration.ofSeconds(2));

            assertTrue(sessionReceiver.containsLockToken(lockToken));

            StepVerifier.create(sessionReceiver.updateDisposition(lockToken, Accepted.getInstance()))
                .expectComplete()
                .verify(Duration.ofSeconds(2));

            assertFalse(sessionReceiver.containsLockToken(lockToken));
        } finally {
            sessionReceiver.close();
        }
    }

    private static Mono<OffsetDateTime> renewSessionLock(Duration additionalDuration) {
        return Mono.defer(() -> Mono.just(OffsetDateTime.now().plus(additionalDuration)));
    }
}
