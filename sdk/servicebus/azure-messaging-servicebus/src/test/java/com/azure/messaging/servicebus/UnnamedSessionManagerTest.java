// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.CbsAuthorizationType;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnnamedSessionManagerTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private static final String NAMESPACE = "my-namespace-foo.net";
    private static final String ENTITY_PATH = "queue-name";
    private static final MessagingEntityType ENTITY_TYPE = MessagingEntityType.QUEUE;

    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverAsyncClientTest.class);
    private final ReplayProcessor<AmqpEndpointState> endpointProcessor = ReplayProcessor.cacheLast();
    private final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private final EmitterProcessor<Message> messageProcessor = EmitterProcessor.create();
    private final FluxSink<Message> messageSink = messageProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private final TracerProvider tracerProvider = new TracerProvider(Collections.emptyList());

    private ServiceBusConnectionProcessor connectionProcessor;
    private UnnamedSessionManager sessionManager;

    @Mock
    private ServiceBusReceiveLink amqpReceiveLink;
    @Mock
    private ServiceBusAmqpConnection connection;
    @Mock
    private TokenCredential tokenCredential;
    @Mock
    private MessageSerializer messageSerializer;
    @Mock
    private ServiceBusManagementNode managementNode;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(60));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        logger.info("===== [{}] Setting up. =====", testInfo.getDisplayName());

        MockitoAnnotations.initMocks(this);

        // Forcing us to publish the messages we receive on the AMQP link on single. Similar to how it is done
        // in ReactorExecutor.
        when(amqpReceiveLink.receive()).thenReturn(messageProcessor.publishOn(Schedulers.single()));

        when(amqpReceiveLink.getHostname()).thenReturn(NAMESPACE);
        when(amqpReceiveLink.getEntityPath()).thenReturn(ENTITY_PATH);
        when(amqpReceiveLink.getEndpointStates()).thenReturn(endpointProcessor);

        ConnectionOptions connectionOptions = new ConnectionOptions(NAMESPACE, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP,
            new AmqpRetryOptions().setTryTimeout(TIMEOUT), ProxyOptions.SYSTEM_DEFAULTS, Schedulers.boundedElastic());

        when(connection.getEndpointStates()).thenReturn(endpointProcessor);
        endpointSink.next(AmqpEndpointState.ACTIVE);

        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE))
            .thenReturn(Mono.just(managementNode));

        connectionProcessor =
            Flux.<ServiceBusAmqpConnection>create(sink -> sink.next(connection))
                .subscribeWith(new ServiceBusConnectionProcessor(connectionOptions.getFullyQualifiedNamespace(),
                    connectionOptions.getRetry()));
    }

    @AfterEach
    void afterEach(TestInfo testInfo) {
        logger.info("===== [{}] Tearing down. =====", testInfo.getDisplayName());
        Mockito.framework().clearInlineMocks();

        if (sessionManager != null) {
            sessionManager.close();
        }
    }

    @Test
    void receiveNull() {
        // Arrange
        ReceiverOptions receiverOptions = new ReceiverOptions(ReceiveMode.PEEK_LOCK, 1, Duration.ZERO, null, true, 5);
        sessionManager = new UnnamedSessionManager(ENTITY_PATH, ENTITY_TYPE, connectionProcessor,
            TIMEOUT, tracerProvider, messageSerializer, receiverOptions);

        // Act & Assert
        StepVerifier.create(sessionManager.receive())
            .expectError(NullPointerException.class)
            .verify();
    }

    /**
     * Verify that when we receive for a single, unnamed session, when no more items are emitted, it completes.
     */
    @Test
    void singleUnnamedSession() {
        // Arrange
        ReceiverOptions receiverOptions = new ReceiverOptions(ReceiveMode.PEEK_LOCK, 1, Duration.ofSeconds(20),
            null, false, null);
        sessionManager = new UnnamedSessionManager(ENTITY_PATH, ENTITY_TYPE, connectionProcessor,
            TIMEOUT, tracerProvider, messageSerializer, receiverOptions);

        final String sessionId = "session-1";
        final String lockToken = "a-lock-token";
        final String linkName = "my-link-name";
        final Instant sessionLockedUntil = Instant.now().plus(Duration.ofSeconds(60));

        final Message message = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);
        when(receivedMessage.getSessionId()).thenReturn(sessionId);
        when(receivedMessage.getLockToken()).thenReturn(lockToken);

        final int numberOfMessages = 5;

        when(amqpReceiveLink.getLinkName()).thenReturn(linkName);
        when(amqpReceiveLink.getSessionId()).thenReturn(Mono.just(sessionId));
        when(amqpReceiveLink.getSessionLockedUntil())
            .thenAnswer(invocation -> Mono.just(sessionLockedUntil));
        when(amqpReceiveLink.updateDisposition(lockToken, Accepted.getInstance())).thenReturn(Mono.empty());

        when(connection.createReceiveLink(anyString(), eq(ENTITY_PATH), any(ReceiveMode.class), isNull(),
            any(MessagingEntityType.class), isNull())).thenReturn(Mono.just(amqpReceiveLink));

        when(managementNode.renewSessionLock(sessionId, linkName)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(sessionManager.receive())
            .then(() -> {
                for (int i = 0; i < numberOfMessages; i++) {
                    messageSink.next(message);
                }
            })
            .assertNext(context -> assertMessageEquals(receivedMessage, context))
            .assertNext(context -> assertMessageEquals(receivedMessage, context))
            .assertNext(context -> assertMessageEquals(receivedMessage, context))
            .assertNext(context -> assertMessageEquals(receivedMessage, context))
            .assertNext(context -> assertMessageEquals(receivedMessage, context))
            .expectComplete()
            .verify();
    }

    /**
     * Verify that when we receive multiple sessions, it'll change to the next session when one is complete.
     */
    @Test
    void multipleSessions() {
        // Arrange
        ReceiverOptions receiverOptions = new ReceiverOptions(ReceiveMode.PEEK_LOCK, 1, Duration.ofSeconds(20),
            null, false, null);
        sessionManager = new UnnamedSessionManager(ENTITY_PATH, ENTITY_TYPE, connectionProcessor,
            TIMEOUT, tracerProvider, messageSerializer, receiverOptions);

        final int numberOfMessages = 5;
        final String sessionId = "session-1";
        final String lockToken = "a-lock-token";
        final String linkName = "my-link-name";
        final Instant sessionLockedUntil = Instant.now().plus(Duration.ofSeconds(5));

        final Message message = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);
        when(receivedMessage.getSessionId()).thenReturn(sessionId);
        when(receivedMessage.getLockToken()).thenReturn(lockToken);


        when(amqpReceiveLink.getLinkName()).thenReturn(linkName);
        when(amqpReceiveLink.getSessionId()).thenReturn(Mono.just(sessionId));
        when(amqpReceiveLink.getSessionLockedUntil())
            .thenAnswer(invocation -> Mono.just(sessionLockedUntil));
        when(amqpReceiveLink.updateDisposition(lockToken, Accepted.getInstance())).thenReturn(Mono.empty());

        when(connection.createReceiveLink(anyString(), eq(ENTITY_PATH), any(ReceiveMode.class), isNull(),
            any(MessagingEntityType.class), isNull())).thenReturn(Mono.just(amqpReceiveLink));

        when(managementNode.renewSessionLock(sessionId, linkName)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(sessionManager.receive())
            .then(() -> {
                for (int i = 0; i < numberOfMessages; i++) {
                    messageSink.next(message);
                }
            })
            .assertNext(context -> assertMessageEquals(receivedMessage, context))
            .assertNext(context -> assertMessageEquals(receivedMessage, context))
            .assertNext(context -> assertMessageEquals(receivedMessage, context))
            .assertNext(context -> assertMessageEquals(receivedMessage, context))
            .assertNext(context -> assertMessageEquals(receivedMessage, context))
            .expectComplete()
            .verify();
    }

    private static void assertMessageEquals(ServiceBusReceivedMessage expected,
        ServiceBusReceivedMessageContext actual) {
        assertEquals(expected, actual.getMessage());
        assertNull(actual.getThrowable());
        assertEquals(expected.getSessionId(), actual.getSessionId());
    }
}
