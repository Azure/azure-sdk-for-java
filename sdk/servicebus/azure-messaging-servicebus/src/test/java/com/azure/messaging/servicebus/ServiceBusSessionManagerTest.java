// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusConstants;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.messaging.servicebus.ReceiverOptions.createUnnamedSessionOptions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// This class is executed synchronously as it runs into a NullPointerException when attempting to dispose
// connectionProcessor in parallel runs.
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
class ServiceBusSessionManagerTest {
    private static final ClientOptions CLIENT_OPTIONS = new ClientOptions();
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final Duration MAX_LOCK_RENEWAL = Duration.ofSeconds(5);
    private static final Duration SESSION_IDLE_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);
    private static final String NAMESPACE = "my-namespace-foo.net";
    private static final String ENTITY_PATH = "queue-name";
    private static final MessagingEntityType ENTITY_TYPE = MessagingEntityType.QUEUE;
    private static final String CLIENT_IDENTIFIER = "my-client-identifier";
    private static final ServiceBusTracer NOOP_TRACER = new ServiceBusTracer(null, NAMESPACE, ENTITY_PATH);
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusReceiverAsyncClientTest.class);
    private final Sinks.Many<AmqpEndpointState> endpointStates
        = Sinks.many().replay().latestOrDefault(AmqpEndpointState.UNINITIALIZED);
    private final Sinks.Many<Message> messages = Sinks.many().multicast().onBackpressureBuffer();

    private ServiceBusConnectionProcessor connectionProcessor;
    private ConnectionCacheWrapper connectionCacheWrapper;
    private ServiceBusSessionManager sessionManager;
    private AutoCloseable mocksCloseable;

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
    @Captor
    private ArgumentCaptor<String> linkNameCaptor;

    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        LOGGER.info("===== [{}] Setting up. =====", testInfo.getDisplayName());

        mocksCloseable = MockitoAnnotations.openMocks(this);

        // Forcing us to publish the messages we receive on the AMQP link on single. Similar to how it is done
        // in ReactorExecutor.
        when(amqpReceiveLink.receive()).thenReturn(messages.asFlux().publishOn(Schedulers.single()));

        when(amqpReceiveLink.getHostname()).thenReturn(NAMESPACE);
        when(amqpReceiveLink.getEntityPath()).thenReturn(ENTITY_PATH);
        when(amqpReceiveLink.getEndpointStates()).thenReturn(endpointStates.asFlux());
        when(amqpReceiveLink.closeAsync()).thenReturn(Mono.empty());

        ConnectionOptions connectionOptions = new ConnectionOptions(NAMESPACE, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, ServiceBusConstants.AZURE_ACTIVE_DIRECTORY_SCOPE,
            AmqpTransportType.AMQP, new AmqpRetryOptions().setTryTimeout(TIMEOUT), ProxyOptions.SYSTEM_DEFAULTS,
            Schedulers.boundedElastic(), CLIENT_OPTIONS, SslDomain.VerifyMode.VERIFY_PEER_NAME, "test-product",
            "test-version");

        when(connection.getEndpointStates()).thenReturn(endpointStates.asFlux());
        endpointStates.emitNext(AmqpEndpointState.ACTIVE, Sinks.EmitFailureHandler.FAIL_FAST);

        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE)).thenReturn(Mono.just(managementNode));

        final ServiceBusConnectionProcessor connectionProcessor
            = Flux.<ServiceBusAmqpConnection>create(sink -> sink.next(connection))
                .subscribeWith(new ServiceBusConnectionProcessor(connectionOptions.getFullyQualifiedNamespace(),
                    connectionOptions.getRetry()));
        connectionCacheWrapper = new ConnectionCacheWrapper(connectionProcessor);
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

        if (connectionProcessor != null) {
            connectionProcessor.dispose();
        }
    }

    @Test
    void properties() {
        // Arrange
        ReceiverOptions receiverOptions = createUnnamedSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, 1,
            MAX_LOCK_RENEWAL, false, 5, SESSION_IDLE_TIMEOUT);
        sessionManager = new ServiceBusSessionManager(ENTITY_PATH, ENTITY_TYPE, connectionCacheWrapper,
            messageSerializer, receiverOptions, CLIENT_IDENTIFIER, NOOP_TRACER);

        // Act & Assert
        assertEquals(CLIENT_IDENTIFIER, sessionManager.getIdentifier());
    }

    @Test
    void receiveNull() {
        // Arrange
        ReceiverOptions receiverOptions = createUnnamedSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, 1,
            MAX_LOCK_RENEWAL, false, 5, SESSION_IDLE_TIMEOUT);
        sessionManager = new ServiceBusSessionManager(ENTITY_PATH, ENTITY_TYPE, connectionCacheWrapper,
            messageSerializer, receiverOptions, CLIENT_IDENTIFIER, NOOP_TRACER);

        // Act & Assert
        StepVerifier.create(sessionManager.receive()).expectError(NullPointerException.class).verify(DEFAULT_TIMEOUT);
    }

    /**
     * Verify that when we receive for a single, unnamed session, when no more items are emitted, it completes.
     */
    @Test
    void singleUnnamedSession() {
        // Arrange
        ReceiverOptions receiverOptions = createUnnamedSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, 1,
            MAX_LOCK_RENEWAL, false, 5, SESSION_IDLE_TIMEOUT);
        sessionManager = new ServiceBusSessionManager(ENTITY_PATH, ENTITY_TYPE, connectionCacheWrapper,
            messageSerializer, receiverOptions, CLIENT_IDENTIFIER, NOOP_TRACER);

        final String sessionId = "session-1";
        final String lockToken = "a-lock-token";
        final String linkName = "my-link-name";
        final OffsetDateTime sessionLockedUntil = OffsetDateTime.now().plus(Duration.ofSeconds(30));

        final Message message = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);
        when(receivedMessage.getSessionId()).thenReturn(sessionId);
        when(receivedMessage.getLockToken()).thenReturn(lockToken);

        final int numberOfMessages = 5;

        when(amqpReceiveLink.getLinkName()).thenReturn(linkName);
        when(amqpReceiveLink.getSessionId()).thenReturn(Mono.just(sessionId));
        when(amqpReceiveLink.getSessionLockedUntil()).thenAnswer(invocation -> Mono.just(sessionLockedUntil));
        when(amqpReceiveLink.updateDisposition(lockToken, Accepted.getInstance())).thenReturn(Mono.empty());
        when(amqpReceiveLink.addCredits(anyInt())).thenReturn(Mono.empty());
        when(amqpReceiveLink.closeAsync()).thenReturn(Mono.empty());

        when(connection.createReceiveLink(anyString(), eq(ENTITY_PATH), any(ServiceBusReceiveMode.class), isNull(),
            any(MessagingEntityType.class), eq(CLIENT_IDENTIFIER), isNull())).thenReturn(Mono.just(amqpReceiveLink));

        when(managementNode.renewSessionLock(sessionId, linkName))
            .thenReturn(Mono.fromCallable(() -> OffsetDateTime.now().plus(Duration.ofSeconds(5))));

        // Act & Assert
        StepVerifier.create(sessionManager.receive()).then(() -> {
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
        ReceiverOptions receiverOptions = createUnnamedSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, 1,
            MAX_LOCK_RENEWAL, false, 1, SESSION_IDLE_TIMEOUT);
        sessionManager = new ServiceBusSessionManager(ENTITY_PATH, ENTITY_TYPE, connectionCacheWrapper,
            messageSerializer, receiverOptions, CLIENT_IDENTIFIER, NOOP_TRACER);

        final String sessionId = "session-1";
        final String lockToken = "a-lock-token";
        final String linkName = "my-link-name";
        final OffsetDateTime sessionLockedUntil = OffsetDateTime.now().plus(Duration.ofSeconds(1));

        final Message message = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);
        when(receivedMessage.getSessionId()).thenReturn(sessionId);
        when(receivedMessage.getLockToken()).thenReturn(lockToken);

        final int numberOfMessages = 2;

        when(amqpReceiveLink.getLinkName()).thenReturn(linkName);
        when(amqpReceiveLink.getSessionId()).thenReturn(Mono.just(sessionId));
        when(amqpReceiveLink.getSessionLockedUntil()).thenAnswer(invocation -> Mono.just(sessionLockedUntil));
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
        StepVerifier.create(sessionManager.receive()).then(() -> {
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
     * Verify that when we receive multiple sessions, it'll change to the next session when one is complete.
     */
    @Test
    void multipleSessions() {
        // Arrange
        final ReceiverOptions receiverOptions = createUnnamedSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, 1,
            MAX_LOCK_RENEWAL, true, 5, SESSION_IDLE_TIMEOUT);
        sessionManager = new ServiceBusSessionManager(ENTITY_PATH, ENTITY_TYPE, connectionCacheWrapper,
            messageSerializer, receiverOptions, CLIENT_IDENTIFIER, NOOP_TRACER);

        final int numberOfMessages = 5;
        final Callable<OffsetDateTime> onRenewal = () -> OffsetDateTime.now().plus(Duration.ofSeconds(5));

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
        when(amqpReceiveLink.addCredits(anyInt())).thenReturn(Mono.empty());
        when(amqpReceiveLink.closeAsync()).thenReturn(Mono.empty());

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
        when(amqpReceiveLink2.getEndpointStates()).thenReturn(endpointStates.asFlux());
        when(amqpReceiveLink2.getLinkName()).thenReturn(linkName2);
        when(amqpReceiveLink2.getSessionId()).thenReturn(Mono.just(sessionId2));
        when(amqpReceiveLink2.getSessionLockedUntil()).thenReturn(Mono.fromCallable(onRenewal));
        when(amqpReceiveLink2.updateDisposition(lockToken2, Accepted.getInstance())).thenReturn(Mono.empty());
        when(amqpReceiveLink2.addCredits(anyInt())).thenReturn(Mono.empty());
        when(amqpReceiveLink2.closeAsync()).thenReturn(Mono.empty());

        final AtomicInteger count = new AtomicInteger();
        when(connection.createReceiveLink(anyString(), eq(ENTITY_PATH), any(ServiceBusReceiveMode.class), isNull(),
            any(MessagingEntityType.class), eq(CLIENT_IDENTIFIER), isNull())).thenAnswer(invocation -> {
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
        StepVerifier.create(sessionManager.receive().publishOn(Schedulers.parallel())).then(() -> {
            for (int i = 0; i < numberOfMessages; i++) {
                messages.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
            }
        }).assertNext(context -> {
            LOGGER.log(LogLevel.VERBOSE, () -> "1");
            assertMessageEquals(sessionId, receivedMessage, context);
        }).assertNext(context -> {
            LOGGER.log(LogLevel.VERBOSE, () -> "2");
            assertMessageEquals(sessionId, receivedMessage, context);
        }).assertNext(context -> {
            LOGGER.log(LogLevel.VERBOSE, () -> "3");
            assertMessageEquals(sessionId, receivedMessage, context);
        }).assertNext(context -> {
            LOGGER.log(LogLevel.VERBOSE, () -> "4");
            assertMessageEquals(sessionId, receivedMessage, context);
        }).assertNext(context -> {
            LOGGER.log(LogLevel.VERBOSE, () -> "5");
            assertMessageEquals(sessionId, receivedMessage, context);
        }).thenAwait(Duration.ofSeconds(13)).then(() -> {
            for (int i = 0; i < 3; i++) {
                messagePublisher2.next(message2);
            }
        }).assertNext(context -> {
            LOGGER.log(LogLevel.VERBOSE, () -> "6");
            assertMessageEquals(sessionId2, receivedMessage2, context);
        }).assertNext(context -> {
            LOGGER.log(LogLevel.VERBOSE, () -> "7");
            assertMessageEquals(sessionId2, receivedMessage2, context);
        }).assertNext(context -> {
            LOGGER.log(LogLevel.VERBOSE, () -> "8");
            assertMessageEquals(sessionId2, receivedMessage2, context);
        }).thenAwait(Duration.ofSeconds(15)).thenCancel().verify(DEFAULT_TIMEOUT);
    }

    /**
     * Verify that when we can call multiple receive, it'll create a new link.
     */
    @Test
    void multipleReceiveUnnamedSession() {
        // Arrange
        final int expectedLinksCreated = 2;
        final Callable<OffsetDateTime> onRenewal = () -> OffsetDateTime.now().plus(Duration.ofSeconds(5));
        final ReceiverOptions receiverOptions = createUnnamedSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, 1,
            Duration.ZERO, false, 1, SESSION_IDLE_TIMEOUT);

        sessionManager = new ServiceBusSessionManager(ENTITY_PATH, ENTITY_TYPE, connectionCacheWrapper,
            messageSerializer, receiverOptions, CLIENT_IDENTIFIER, NOOP_TRACER);

        final String sessionId = "session-1";
        final String linkName = "my-link-name";

        when(amqpReceiveLink.getLinkName()).thenReturn(linkName);
        when(amqpReceiveLink.getSessionId()).thenReturn(Mono.just(sessionId));
        when(amqpReceiveLink.getSessionLockedUntil()).thenReturn(Mono.fromCallable(onRenewal));
        when(amqpReceiveLink.addCredits(anyInt())).thenReturn(Mono.empty());
        when(amqpReceiveLink.closeAsync()).thenReturn(Mono.empty());

        // Session 2's
        final ServiceBusReceiveLink amqpReceiveLink2 = mock(ServiceBusReceiveLink.class);
        final String sessionId2 = "session-2";
        final String linkName2 = "my-link-name-2";
        final TestPublisher<Message> messagePublisher2 = TestPublisher.create();
        final Flux<Message> messageFlux2 = messagePublisher2.flux();

        when(amqpReceiveLink2.receive()).thenReturn(messageFlux2);
        when(amqpReceiveLink2.getHostname()).thenReturn(NAMESPACE);
        when(amqpReceiveLink2.getEntityPath()).thenReturn(ENTITY_PATH);
        when(amqpReceiveLink2.getEndpointStates()).thenReturn(endpointStates.asFlux());
        when(amqpReceiveLink2.getLinkName()).thenReturn(linkName2);
        when(amqpReceiveLink2.getSessionId()).thenReturn(Mono.just(sessionId2));
        when(amqpReceiveLink2.getSessionLockedUntil()).thenReturn(Mono.fromCallable(onRenewal));
        when(amqpReceiveLink2.addCredits(anyInt())).thenReturn(Mono.empty());
        when(amqpReceiveLink2.closeAsync()).thenReturn(Mono.empty());

        final AtomicInteger count = new AtomicInteger();
        when(connection.createReceiveLink(anyString(), eq(ENTITY_PATH), any(ServiceBusReceiveMode.class), isNull(),
            any(MessagingEntityType.class), eq(CLIENT_IDENTIFIER), isNull())).thenAnswer(invocation -> {
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

        // Act & Assert
        StepVerifier.create(sessionManager.receive())
            .thenAwait(Duration.ofSeconds(5))
            .thenCancel()
            .verify(DEFAULT_TIMEOUT);

        StepVerifier.create(sessionManager.receive())
            .thenAwait(Duration.ofSeconds(5))
            .thenCancel()
            .verify(DEFAULT_TIMEOUT);

        verify(connection, times(2)).createReceiveLink(linkNameCaptor.capture(), eq(ENTITY_PATH),
            any(ServiceBusReceiveMode.class), isNull(), any(MessagingEntityType.class), eq(CLIENT_IDENTIFIER),
            isNull());

        final List<String> actualLinksCreated = linkNameCaptor.getAllValues();
        assertNotNull(actualLinksCreated);
        assertEquals(expectedLinksCreated, actualLinksCreated.size());
        assertFalse(actualLinksCreated.get(0).equalsIgnoreCase(actualLinksCreated.get(1)));
    }

    /**
     * Validate that session-id specific session receiver is removed after {@link AmqpRetryOptions#getTryTimeout()} is passed.
     */
    @Test
    void singleUnnamedSessionCleanupAfterTimeout() {
        // Arrange
        ReceiverOptions receiverOptions = createUnnamedSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, 1,
            MAX_LOCK_RENEWAL, false, 2, SESSION_IDLE_TIMEOUT);
        sessionManager = new ServiceBusSessionManager(ENTITY_PATH, ENTITY_TYPE, connectionCacheWrapper,
            messageSerializer, receiverOptions, CLIENT_IDENTIFIER, NOOP_TRACER);

        final String sessionId = "session-1";
        final String lockToken = "a-lock-token";
        final String linkName = "my-link-name";
        final OffsetDateTime sessionLockedUntil = OffsetDateTime.now().plus(Duration.ofSeconds(30));

        final Message message = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);
        when(receivedMessage.getSessionId()).thenReturn(sessionId);
        when(receivedMessage.getLockToken()).thenReturn(lockToken);

        when(amqpReceiveLink.getLinkName()).thenReturn(linkName);
        when(amqpReceiveLink.getSessionId()).thenReturn(Mono.just(sessionId));
        when(amqpReceiveLink.getSessionLockedUntil()).thenAnswer(invocation -> Mono.just(sessionLockedUntil));
        when(connection.createReceiveLink(anyString(), eq(ENTITY_PATH), any(ServiceBusReceiveMode.class), isNull(),
            any(MessagingEntityType.class), eq(CLIENT_IDENTIFIER), isNull())).thenReturn(Mono.just(amqpReceiveLink));
        when(amqpReceiveLink.addCredits(anyInt())).thenReturn(Mono.empty());
        when(amqpReceiveLink.closeAsync()).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(sessionManager.receive().publishOn(Schedulers.parallel())).then(() -> {
            messages.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
        }).assertNext(context -> {
            assertMessageEquals(sessionId, receivedMessage, context);
            assertNotNull(sessionManager.getLinkName(sessionId));
        }).then(() -> {
            try {
                TimeUnit.SECONDS.sleep(TIMEOUT.getSeconds());
                assertNull(sessionManager.getLinkName(sessionId));
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
}
