// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.implementation.ServiceBusReactorAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import com.azure.messaging.servicebus.implementation.instrumentation.ReceiverKind;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// This class is executed synchronously as it runs into a NullPointerException when attempting to dispose
// connectionProcessor in parallel runs.
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
class ServiceBusSessionManagerTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final Duration MAX_LOCK_RENEWAL = Duration.ofSeconds(5);
    private static final Duration SESSION_IDLE_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);
    private static final String NAMESPACE = "my-namespace-foo.net";
    private static final String ENTITY_PATH = "queue-name";
    private static final MessagingEntityType ENTITY_TYPE = MessagingEntityType.QUEUE;
    private static final String CLIENT_IDENTIFIER = "my-client-identifier";
    private static final ServiceBusTracer NOOP_TRACER = new ServiceBusTracer(null, NAMESPACE, ENTITY_PATH);
    private static final ServiceBusReceiverInstrumentation NOOP_INSTRUMENTATION
        = new ServiceBusReceiverInstrumentation(null, null, NAMESPACE, ENTITY_PATH, null, ReceiverKind.ASYNC_RECEIVER);
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusReceiverAsyncClientTest.class);
    private final Sinks.Many<AmqpEndpointState> endpointStates
        = Sinks.many().replay().latestOrDefault(AmqpEndpointState.UNINITIALIZED);
    private final Sinks.Many<Message> messages = Sinks.many().multicast().onBackpressureBuffer();
    private ServiceBusSingleSessionManager sessionManager;
    private AutoCloseable mocksCloseable;

    @Mock
    private ServiceBusReceiveLink amqpReceiveLink;
    @Mock
    private ServiceBusReactorAmqpConnection connection;
    @Mock
    private MessageSerializer messageSerializer;
    @Mock
    private ServiceBusManagementNode managementNode;

    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        LOGGER.info("===== [{}] Setting up. =====", testInfo.getDisplayName());

        mocksCloseable = MockitoAnnotations.openMocks(this);

        // Publish messages using boundedElastic, similar to what library internally do.
        when(amqpReceiveLink.receive()).thenReturn(messages.asFlux().publishOn(Schedulers.boundedElastic()));

        when(amqpReceiveLink.getHostname()).thenReturn(NAMESPACE);
        when(amqpReceiveLink.getEntityPath()).thenReturn(ENTITY_PATH);
        when(amqpReceiveLink.getEndpointStates()).thenReturn(endpointStates.asFlux());
        when(amqpReceiveLink.closeAsync()).thenReturn(Mono.empty());

        when(connection.getEndpointStates()).thenReturn(endpointStates.asFlux());
        endpointStates.emitNext(AmqpEndpointState.ACTIVE, Sinks.EmitFailureHandler.FAIL_FAST);
        when(connection.connectAndAwaitToActive()).thenReturn(Mono.just(connection));
        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE)).thenReturn(Mono.just(managementNode));
    }

    @AfterEach
    void afterEach(TestInfo testInfo) throws Exception {
        LOGGER.info("===== [{}] Tearing down. =====", testInfo.getDisplayName());

        // If this test class is made to run in parallel this will need to change to
        // Mockito.framework().clearInlineMock(this), as that is scoped to the specific test object.
        Mockito.framework().clearInlineMocks();

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }

        if (sessionManager != null) {
            sessionManager.close();
        }
    }

    /**
     * Verify that when we receive for a single, unnamed session, when no more items are emitted, it completes.
     */
    @Test
    void singleUnnamedSession() {
        // Arrange
        final String sessionId = "session-1";
        final String lockToken = "a-lock-token";
        final String linkName = "my-link-name";
        final OffsetDateTime sessionLockedUntil = OffsetDateTime.now().plus(Duration.ofSeconds(30));
        final ServiceBusReceiveLink.SessionProperties sessionProperties
            = new ServiceBusReceiveLink.SessionProperties(sessionId, sessionLockedUntil);

        final Message message = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);
        when(receivedMessage.getSessionId()).thenReturn(sessionId);
        when(receivedMessage.getLockToken()).thenReturn(lockToken);

        final int numberOfMessages = 5;

        when(amqpReceiveLink.getLinkName()).thenReturn(linkName);
        when(amqpReceiveLink.getSessionId()).thenReturn(Mono.just(sessionId));
        when(amqpReceiveLink.getSessionLockedUntil()).thenAnswer(invocation -> Mono.just(sessionLockedUntil));
        when(amqpReceiveLink.getSessionProperties()).thenReturn(Mono.just(sessionProperties));
        when(amqpReceiveLink.updateDisposition(lockToken, Accepted.getInstance())).thenReturn(Mono.empty());
        when(amqpReceiveLink.addCredits(anyInt())).thenReturn(Mono.empty());
        when(amqpReceiveLink.closeAsync()).thenReturn(Mono.empty());

        when(connection.createReceiveLink(anyString(), eq(ENTITY_PATH), any(ServiceBusReceiveMode.class), isNull(),
            any(MessagingEntityType.class), eq(CLIENT_IDENTIFIER), isNull())).thenReturn(Mono.just(amqpReceiveLink));

        when(managementNode.renewSessionLock(sessionId, linkName))
            .thenReturn(Mono.fromCallable(() -> OffsetDateTime.now().plus(Duration.ofSeconds(5))));

        final ServiceBusSessionAcquirer.Session session
            = new ServiceBusSessionAcquirer.Session(amqpReceiveLink, sessionProperties, Mono.just(managementNode));
        final ServiceBusSessionReactorReceiver receiver = new ServiceBusSessionReactorReceiver(LOGGER, NOOP_TRACER,
            session, SESSION_IDLE_TIMEOUT, MAX_LOCK_RENEWAL);
        sessionManager = new ServiceBusSingleSessionManager(LOGGER, CLIENT_IDENTIFIER, receiver, 0, messageSerializer,
            new AmqpRetryOptions(), NOOP_INSTRUMENTATION);

        // Act & Assert
        assertEquals(CLIENT_IDENTIFIER, sessionManager.getIdentifier());
        StepVerifier.create(sessionManager.receiveMessages()).then(() -> {
            for (int i = 0; i < numberOfMessages; i++) {
                messages.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
            }
        })
            .assertNext(context -> assertMessageEquals(sessionId, receivedMessage, context))
            .assertNext(context -> assertMessageEquals(sessionId, receivedMessage, context))
            .assertNext(context -> assertMessageEquals(sessionId, receivedMessage, context))
            .assertNext(context -> assertMessageEquals(sessionId, receivedMessage, context))
            .assertNext(context -> assertMessageEquals(sessionId, receivedMessage, context))
            .thenCancel()
            .verify(Duration.ofSeconds(45));
    }

    /**
     * Verify that when we receive for a single, unnamed session, the session Lock renew is called for one session only.
     */
    @Test
    void singleUnnamedSessionLockRenew() {
        // Arrange
        final String sessionId = "session-1";
        final String lockToken = "a-lock-token";
        final String linkName = "my-link-name";
        final OffsetDateTime sessionLockedUntil = OffsetDateTime.now().plus(Duration.ofSeconds(1));
        final ServiceBusReceiveLink.SessionProperties sessionProperties
            = new ServiceBusReceiveLink.SessionProperties(sessionId, sessionLockedUntil);

        final Message message = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);
        when(receivedMessage.getSessionId()).thenReturn(sessionId);
        when(receivedMessage.getLockToken()).thenReturn(lockToken);

        final int numberOfMessages = 2;

        when(amqpReceiveLink.getLinkName()).thenReturn(linkName);
        when(amqpReceiveLink.getSessionId()).thenReturn(Mono.just(sessionId));
        when(amqpReceiveLink.getSessionLockedUntil()).thenAnswer(invocation -> Mono.just(sessionLockedUntil));
        when(amqpReceiveLink.getSessionProperties()).thenReturn(Mono.just(sessionProperties));
        when(amqpReceiveLink.updateDisposition(lockToken, Accepted.getInstance())).thenReturn(Mono.empty());
        when(amqpReceiveLink.addCredits(anyInt())).thenReturn(Mono.empty());
        when(amqpReceiveLink.closeAsync()).thenReturn(Mono.empty());

        when(connection.createReceiveLink(anyString(), eq(ENTITY_PATH), any(ServiceBusReceiveMode.class), isNull(),
            any(MessagingEntityType.class), eq(CLIENT_IDENTIFIER), isNull())).thenReturn(Mono.just(amqpReceiveLink));

        AtomicBoolean sessionLockRenewCalled = new AtomicBoolean();
        when(managementNode.renewSessionLock(sessionId, linkName)).thenReturn(Mono.fromCallable(() -> {
            sessionLockRenewCalled.set(true);
            return OffsetDateTime.now().plus(Duration.ofSeconds(5));
        }));

        final ServiceBusSessionAcquirer.Session session
            = new ServiceBusSessionAcquirer.Session(amqpReceiveLink, sessionProperties, Mono.just(managementNode));
        final ServiceBusSessionReactorReceiver receiver = new ServiceBusSessionReactorReceiver(LOGGER, NOOP_TRACER,
            session, SESSION_IDLE_TIMEOUT, MAX_LOCK_RENEWAL);
        sessionManager = new ServiceBusSingleSessionManager(LOGGER, CLIENT_IDENTIFIER, receiver, 0, messageSerializer,
            new AmqpRetryOptions(), NOOP_INSTRUMENTATION);

        // Act & Assert
        assertEquals(CLIENT_IDENTIFIER, sessionManager.getIdentifier());
        StepVerifier.create(sessionManager.receiveMessages()).then(() -> {
            for (int i = 0; i < numberOfMessages; i++) {
                messages.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
            }
        })
            .assertNext(context -> assertMessageEquals(sessionId, receivedMessage, context))
            .assertNext(context -> assertMessageEquals(sessionId, receivedMessage, context))
            .thenAwait(Duration.ofSeconds(5))
            .thenCancel()
            .verify(Duration.ofSeconds(45));

        assertTrue(sessionLockRenewCalled.get());
    }

    /**
     * Validate that session-id specific session receiver is removed after {@link AmqpRetryOptions#getTryTimeout()} is passed.
     */
    @Test
    void singleUnnamedSessionCleanupAfterTimeout() {
        // Arrange
        final String sessionId = "session-1";
        final String lockToken = "a-lock-token";
        final String linkName = "my-link-name";
        final OffsetDateTime sessionLockedUntil = OffsetDateTime.now().plus(Duration.ofSeconds(30));
        final ServiceBusReceiveLink.SessionProperties sessionProperties
            = new ServiceBusReceiveLink.SessionProperties(sessionId, sessionLockedUntil);

        final Message message = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);
        when(receivedMessage.getSessionId()).thenReturn(sessionId);
        when(receivedMessage.getLockToken()).thenReturn(lockToken);

        when(amqpReceiveLink.getLinkName()).thenReturn(linkName);
        when(amqpReceiveLink.getSessionId()).thenReturn(Mono.just(sessionId));
        when(amqpReceiveLink.getSessionLockedUntil()).thenAnswer(invocation -> Mono.just(sessionLockedUntil));
        when(amqpReceiveLink.getSessionProperties()).thenReturn(Mono.just(sessionProperties));
        when(connection.createReceiveLink(anyString(), eq(ENTITY_PATH), any(ServiceBusReceiveMode.class), isNull(),
            any(MessagingEntityType.class), eq(CLIENT_IDENTIFIER), isNull())).thenReturn(Mono.just(amqpReceiveLink));
        when(amqpReceiveLink.addCredits(anyInt())).thenReturn(Mono.empty());
        when(amqpReceiveLink.closeAsync()).thenReturn(Mono.empty());

        final ServiceBusSessionAcquirer.Session session
            = new ServiceBusSessionAcquirer.Session(amqpReceiveLink, sessionProperties, Mono.just(managementNode));
        final ServiceBusSessionReactorReceiver receiver = new ServiceBusSessionReactorReceiver(LOGGER, NOOP_TRACER,
            session, SESSION_IDLE_TIMEOUT, MAX_LOCK_RENEWAL);
        sessionManager = new ServiceBusSingleSessionManager(LOGGER, CLIENT_IDENTIFIER, receiver, 0, messageSerializer,
            new AmqpRetryOptions(), NOOP_INSTRUMENTATION);

        // Act & Assert
        assertEquals(CLIENT_IDENTIFIER, sessionManager.getIdentifier());
        StepVerifier.create(sessionManager.receiveMessages().publishOn(Schedulers.parallel())).then(() -> {
            messages.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
        }).assertNext(context -> {
            assertMessageEquals(sessionId, receivedMessage, context);
            assertNotNull(sessionManager.getLinkName(sessionId));
        }).then(() -> {
            try {
                TimeUnit.SECONDS.sleep(TIMEOUT.getSeconds());
            } catch (InterruptedException ignored) {
            }

        }).thenCancel().verify(DEFAULT_TIMEOUT);
    }

    private static void assertMessageEquals(String sessionId, ServiceBusReceivedMessage expected,
        ServiceBusMessageContext actual) {
        assertEquals(sessionId, actual.getSessionId());
        assertNull(actual.getThrowable());

        assertEquals(expected, actual.getMessage());
    }

    private static void assertMessageEquals(String sessionId, ServiceBusReceivedMessage expected,
        ServiceBusReceivedMessage actual) {
        assertEquals(sessionId, actual.getSessionId());
        assertEquals(expected, actual);
    }
}
