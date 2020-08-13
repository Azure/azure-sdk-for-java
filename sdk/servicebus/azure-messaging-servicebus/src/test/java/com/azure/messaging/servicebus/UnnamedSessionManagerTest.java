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
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

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

        if (sessionManager != null) {
            sessionManager.close();
        }

        if (connectionProcessor != null) {
            connectionProcessor.dispose();
        }

        Mockito.framework().clearInlineMocks();
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
        ReceiverOptions receiverOptions = new ReceiverOptions(ReceiveMode.PEEK_LOCK, 1,
            Duration.ofSeconds(20), null, false, null);
        sessionManager = new UnnamedSessionManager(ENTITY_PATH, ENTITY_TYPE, connectionProcessor,
            TIMEOUT, tracerProvider, messageSerializer, receiverOptions);

        final String sessionId = "session-1";
        final String lockToken = "a-lock-token";
        final String linkName = "my-link-name";
        final Instant sessionLockedUntil = Instant.now().plus(Duration.ofSeconds(30));

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

        when(managementNode.renewSessionLock(sessionId, linkName)).thenReturn(
            Mono.fromCallable(() -> Instant.now().plus(Duration.ofSeconds(5))));

        // Act & Assert
        StepVerifier.create(sessionManager.receive())
            .then(() -> {
                for (int i = 0; i < numberOfMessages; i++) {
                    messageSink.next(message);
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
     * Verify that when we receive multiple sessions, it'll change to the next session when one is complete.
     */
    @Test
    void multipleSessions() {
        // Arrange
        final ReceiverOptions receiverOptions = new ReceiverOptions(ReceiveMode.PEEK_LOCK, 1,
            Duration.ofSeconds(8), null, true, 1);
        sessionManager = new UnnamedSessionManager(ENTITY_PATH, ENTITY_TYPE, connectionProcessor,
            TIMEOUT, tracerProvider, messageSerializer, receiverOptions);

        final int numberOfMessages = 5;
        final Callable<Instant> onRenewal = () -> Instant.now().plus(Duration.ofSeconds(5));

        final String sessionId = "session-1";
        final String lockToken = "a-lock-token";
        final String linkName = "my-link-name";

        final Message message = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);

        when(receivedMessage.getSessionId()).thenReturn(sessionId);
        when(receivedMessage.getLockToken()).thenReturn(lockToken);

        when(amqpReceiveLink.getLinkName()).thenReturn(linkName);
        when(amqpReceiveLink.getSessionId()).thenReturn(Mono.just(sessionId));
        when(amqpReceiveLink.getSessionLockedUntil()).thenReturn(Mono.fromCallable(onRenewal));
        when(amqpReceiveLink.updateDisposition(lockToken, Accepted.getInstance())).thenReturn(Mono.empty());

        // Session 2's messages
        final ServiceBusReceiveLink amqpReceiveLink2 = mock(ServiceBusReceiveLink.class);
        final Message message2 = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage2 = mock(ServiceBusReceivedMessage.class);
        final String sessionId2 = "session-2";
        final String lockToken2 = "a-lock-token-2";
        final String linkName2 = "my-link-name-2";
        final TestPublisher<Message> messagePublisher2 = TestPublisher.create();
        final Flux<Message> messageFlux2 = messagePublisher2.flux();

        when(receivedMessage2.getSessionId()).thenReturn(sessionId2);
        when(receivedMessage2.getLockToken()).thenReturn(lockToken2);

        when(amqpReceiveLink2.receive()).thenReturn(messageFlux2);
        when(amqpReceiveLink2.getHostname()).thenReturn(NAMESPACE);
        when(amqpReceiveLink2.getEntityPath()).thenReturn(ENTITY_PATH);
        when(amqpReceiveLink2.getEndpointStates()).thenReturn(endpointProcessor);
        when(amqpReceiveLink2.getLinkName()).thenReturn(linkName2);
        when(amqpReceiveLink2.getSessionId()).thenReturn(Mono.just(sessionId2));
        when(amqpReceiveLink2.getSessionLockedUntil()).thenReturn(Mono.fromCallable(onRenewal));
        when(amqpReceiveLink2.updateDisposition(lockToken2, Accepted.getInstance())).thenReturn(Mono.empty());

        final AtomicInteger count = new AtomicInteger();
        when(connection.createReceiveLink(anyString(), eq(ENTITY_PATH), any(ReceiveMode.class), isNull(),
            any(MessagingEntityType.class), isNull())).thenAnswer(invocation -> {
                final int number = count.getAndIncrement();
                switch (number) {
                    case 0:
                        return Mono.just(amqpReceiveLink);
                    case 1:
                        return Mono.just(amqpReceiveLink2);
                    default:
                        return Mono.empty();
                }
            });

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);
        when(messageSerializer.deserialize(message2, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage2);

        when(managementNode.renewSessionLock(sessionId, linkName)).thenReturn(Mono.fromCallable(onRenewal));
        when(managementNode.renewSessionLock(sessionId2, linkName2)).thenReturn(Mono.fromCallable(onRenewal));

        // Act & Assert
        StepVerifier.create(sessionManager.receive())
            .then(() -> {
                for (int i = 0; i < numberOfMessages; i++) {
                    messageSink.next(message);
                }
            })
            .assertNext(context -> {
                System.out.println("1");
                assertMessageEquals(sessionId, receivedMessage, context);
            })
            .assertNext(context -> {
                System.out.println("2");
                assertMessageEquals(sessionId, receivedMessage, context);
            })
            .assertNext(context -> {
                System.out.println("3");
                assertMessageEquals(sessionId, receivedMessage, context);
            })
            .assertNext(context -> {
                System.out.println("4");
                assertMessageEquals(sessionId, receivedMessage, context);
            })
            .assertNext(context -> {
                System.out.println("5");
                assertMessageEquals(sessionId, receivedMessage, context);
            })
            .thenAwait(Duration.ofSeconds(13))
            .then(() -> {
                for (int i = 0; i < 3; i++) {
                    messagePublisher2.next(message2);
                }
            })
            .assertNext(context -> {
                System.out.println("6");
                assertMessageEquals(sessionId2, receivedMessage2, context);
            })
            .assertNext(context -> {
                System.out.println("7");
                assertMessageEquals(sessionId2, receivedMessage2, context);
            })
            .assertNext(context -> {
                System.out.println("8");
                assertMessageEquals(sessionId2, receivedMessage2, context);
            })
            .thenAwait(Duration.ofSeconds(15))
            .thenCancel()
            .verify();
    }

    private static void assertMessageEquals(String sessionId, ServiceBusReceivedMessage expected,
        ServiceBusReceivedMessageContext actual) {
        assertEquals(sessionId, actual.getSessionId());
        assertNull(actual.getThrowable());

        assertEquals(expected, actual.getMessage());
    }
}
