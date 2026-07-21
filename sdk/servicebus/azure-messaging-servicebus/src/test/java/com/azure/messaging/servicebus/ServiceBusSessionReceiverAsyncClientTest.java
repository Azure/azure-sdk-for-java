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
import com.azure.messaging.servicebus.implementation.ManagementConstants;
import com.azure.messaging.servicebus.implementation.MessageSessionsResult;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusConstants;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import com.azure.messaging.servicebus.implementation.instrumentation.ReceiverKind;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.messaging.servicebus.ReceiverOptions.createNamedSessionOptions;
import static com.azure.messaging.servicebus.ReceiverOptions.createUnnamedSessionOptions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceBusSessionReceiverAsyncClientTest {
    private static final ClientOptions CLIENT_OPTIONS = new ClientOptions();
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final Duration SESSION_IDLE_TIMEOUT = Duration.ofSeconds(20);
    private static final String NAMESPACE = "my-namespace-foo.net";
    private static final String ENTITY_PATH = "queue-name";
    private static final MessagingEntityType ENTITY_TYPE = MessagingEntityType.QUEUE;
    private static final String CLIENT_IDENTIFIER = "my-client-identifier";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusReceiverAsyncClientTest.class);
    private final TestPublisher<AmqpEndpointState> endpointProcessor = TestPublisher.createCold();
    private final TestPublisher<Message> messageProcessor = TestPublisher.createCold();
    private final ServiceBusReceiverInstrumentation instrumentation
        = new ServiceBusReceiverInstrumentation(null, null, NAMESPACE, ENTITY_PATH, null, ReceiverKind.ASYNC_RECEIVER);

    private ServiceBusConnectionProcessor connectionProcessor;
    private ConnectionCacheWrapper connectionCacheWrapper;
    private ServiceBusSessionManager sessionManager;
    private AutoCloseable autoCloseable;

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

    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        LOGGER.info("===== [{}] Setting up. =====", testInfo.getDisplayName());

        autoCloseable = MockitoAnnotations.openMocks(this);

        // Forcing us to publish the messages we receive on the AMQP link on single. Similar to how it is done
        // in ReactorExecutor.
        when(amqpReceiveLink.receive()).thenReturn(messageProcessor.flux().publishOn(Schedulers.single()));

        when(amqpReceiveLink.getHostname()).thenReturn(NAMESPACE);
        when(amqpReceiveLink.getEntityPath()).thenReturn(ENTITY_PATH);
        when(amqpReceiveLink.getEndpointStates()).thenReturn(endpointProcessor.flux());
        when(amqpReceiveLink.addCredits(anyInt())).thenReturn(Mono.empty());

        ConnectionOptions connectionOptions = new ConnectionOptions(NAMESPACE, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, ServiceBusConstants.AZURE_ACTIVE_DIRECTORY_SCOPE,
            AmqpTransportType.AMQP, new AmqpRetryOptions().setTryTimeout(TIMEOUT), ProxyOptions.SYSTEM_DEFAULTS,
            Schedulers.boundedElastic(), CLIENT_OPTIONS, SslDomain.VerifyMode.VERIFY_PEER_NAME, "test-product",
            "test-version");

        when(connection.getEndpointStates()).thenReturn(endpointProcessor.flux());
        endpointProcessor.next(AmqpEndpointState.ACTIVE);

        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE)).thenReturn(Mono.just(managementNode));

        when(connection.closeAsync()).thenReturn(Mono.empty());

        connectionProcessor = Flux.<ServiceBusAmqpConnection>create(sink -> sink.next(connection))
            .subscribeWith(new ServiceBusConnectionProcessor(connectionOptions.getFullyQualifiedNamespace(),
                connectionOptions.getRetry()));
        connectionCacheWrapper = new ConnectionCacheWrapper(connectionProcessor);
    }

    @AfterEach
    void afterEach(TestInfo testInfo) throws Exception {
        LOGGER.info("===== [{}] Tearing down. =====", testInfo.getDisplayName());

        if (sessionManager != null) {
            sessionManager.close();
        }

        if (connectionProcessor != null) {
            connectionProcessor.dispose();
        }

        if (autoCloseable != null) {
            autoCloseable.close();
        }

        Mockito.framework().clearInlineMock(this);
    }

    @Test
    void acceptSession() {
        // Arrange
        final String lockToken = "a-lock-token";
        final String linkName = "my-link-name";
        final String sessionId = linkName;
        final OffsetDateTime sessionLockedUntil = OffsetDateTime.now().plus(Duration.ofSeconds(30));
        ReceiverOptions receiverOptions
            = createNamedSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, 1, Duration.ZERO, false, sessionId);

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

        when(connection.createReceiveLink(anyString(), eq(ENTITY_PATH), any(ServiceBusReceiveMode.class), isNull(),
            any(MessagingEntityType.class), eq(CLIENT_IDENTIFIER), eq(sessionId)))
                .thenReturn(Mono.just(amqpReceiveLink));

        ServiceBusSessionReceiverAsyncClient client
            = new ServiceBusSessionReceiverAsyncClient(NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE,
                receiverOptions, connectionCacheWrapper, instrumentation, messageSerializer, () -> {
                }, CLIENT_IDENTIFIER, false);

        // Act & Assert
        StepVerifier
            .create(
                client.acceptSession(sessionId).flatMapMany(ServiceBusReceiverAsyncClient::receiveMessagesWithContext))
            .then(() -> {
                for (int i = 0; i < numberOfMessages; i++) {
                    messageProcessor.next(message);
                }
            })
            .assertNext(context -> assertMessageEquals(sessionId, receivedMessage, context))
            .assertNext(context -> assertMessageEquals(sessionId, receivedMessage, context))
            .assertNext(context -> assertMessageEquals(sessionId, receivedMessage, context))
            .assertNext(context -> assertMessageEquals(sessionId, receivedMessage, context))
            .assertNext(context -> assertMessageEquals(sessionId, receivedMessage, context))
            .thenCancel()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    void acceptNextSession() {
        // Arrange
        ReceiverOptions receiverOptions = createUnnamedSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, 1, Duration.ZERO,
            false, null, SESSION_IDLE_TIMEOUT);
        sessionManager = new ServiceBusSessionManager(ENTITY_PATH, ENTITY_TYPE, connectionCacheWrapper,
            messageSerializer, receiverOptions, CLIENT_IDENTIFIER, instrumentation.getTracer());

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
        when(amqpReceiveLink2.getEndpointStates()).thenReturn(endpointProcessor.flux());
        when(amqpReceiveLink2.getLinkName()).thenReturn(linkName2);
        when(amqpReceiveLink2.getSessionId()).thenReturn(Mono.just(sessionId2));
        when(amqpReceiveLink2.getSessionLockedUntil()).thenReturn(Mono.fromCallable(onRenewal));
        when(amqpReceiveLink2.updateDisposition(lockToken2, Accepted.getInstance())).thenReturn(Mono.empty());
        when(amqpReceiveLink2.addCredits(anyInt())).thenReturn(Mono.empty());

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

        ServiceBusSessionReceiverAsyncClient client
            = new ServiceBusSessionReceiverAsyncClient(NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE,
                receiverOptions, connectionCacheWrapper, instrumentation, messageSerializer, () -> {
                }, CLIENT_IDENTIFIER, false);

        // Act & Assert
        StepVerifier
            .create(client.acceptNextSession().flatMapMany(ServiceBusReceiverAsyncClient::receiveMessagesWithContext))
            .then(() -> {
                for (int i = 0; i < numberOfMessages; i++) {
                    messageProcessor.next(message);
                }
            })
            .assertNext(context -> assertMessageEquals(sessionId, receivedMessage, context))
            .assertNext(context -> assertMessageEquals(sessionId, receivedMessage, context))
            .assertNext(context -> assertMessageEquals(sessionId, receivedMessage, context))
            .assertNext(context -> assertMessageEquals(sessionId, receivedMessage, context))
            .assertNext(context -> assertMessageEquals(sessionId, receivedMessage, context))
            .thenAwait(Duration.ofSeconds(1))
            .thenCancel()
            .verify(DEFAULT_TIMEOUT);

        StepVerifier
            .create(client.acceptNextSession().flatMapMany(ServiceBusReceiverAsyncClient::receiveMessagesWithContext))
            .then(() -> {
                for (int i = 0; i < 3; i++) {
                    messagePublisher2.next(message2);
                }
            })
            .assertNext(context -> assertMessageEquals(sessionId2, receivedMessage2, context))
            .assertNext(context -> assertMessageEquals(sessionId2, receivedMessage2, context))
            .assertNext(context -> assertMessageEquals(sessionId2, receivedMessage2, context))
            .thenAwait(Duration.ofSeconds(1))
            .thenCancel()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    void specificSessionReceive() {
        // Arrange
        final ReceiverOptions receiverOptions = createUnnamedSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, 1,
            Duration.ZERO, false, 1, SESSION_IDLE_TIMEOUT);

        final String lockToken = "a-lock-token";
        final String linkName = "my-link-name";
        final String sessionId = "my-session-id";

        final OffsetDateTime sessionLockedUntil = OffsetDateTime.now().plus(Duration.ofSeconds(30));

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

        when(connection.createReceiveLink(anyString(), eq(ENTITY_PATH), any(ServiceBusReceiveMode.class), isNull(),
            any(MessagingEntityType.class), eq(CLIENT_IDENTIFIER), eq(sessionId)))
                .thenReturn(Mono.just(amqpReceiveLink));

        final ServiceBusSessionReceiverAsyncClient client
            = new ServiceBusSessionReceiverAsyncClient(NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE,
                receiverOptions, connectionCacheWrapper, instrumentation, messageSerializer, () -> {
                }, CLIENT_IDENTIFIER, false);

        try {
            final Flux<ServiceBusReceivedMessage> sessionMessages
                = client.acceptSession(sessionId).flatMapMany(s -> s.receiveMessages());

            // Act & Assert
            StepVerifier.create(sessionMessages).then(() -> {
                for (int i = 0; i < numberOfMessages; i++) {
                    messageProcessor.next(message);
                }
            })
                .assertNext(m -> assertEquals(receivedMessage, m))
                .assertNext(m -> assertEquals(receivedMessage, m))
                .then(() -> {
                    // Simulates a close operation on the underlying ReceiveLinkHandler, which completes the deliveries
                    // sink.
                    messageProcessor.complete();
                })
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);
        } finally {
            client.close();
        }

    }

    private static void assertMessageEquals(String sessionId, ServiceBusReceivedMessage expected,
        ServiceBusMessageContext actual) {
        assertEquals(sessionId, actual.getSessionId());
        assertNull(actual.getThrowable());

        assertEquals(expected, actual.getMessage());
    }

    private ServiceBusSessionReceiverAsyncClient newSessionReceiver() {
        final ReceiverOptions receiverOptions = createUnnamedSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, 1,
            Duration.ZERO, false, null, SESSION_IDLE_TIMEOUT);
        return new ServiceBusSessionReceiverAsyncClient(NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE,
            receiverOptions, connectionCacheWrapper, instrumentation, messageSerializer, () -> {
            }, CLIENT_IDENTIFIER, false);
    }

    /**
     * Verifies the no-arg listSessions() drives the broker with the active-messages sentinel and
     * collects every page until the broker returns an empty page.
     */
    @Test
    void listSessionsActiveModeStreamsAllPagesUntilEmpty() {
        // First page: 2 sessions, server-returned skip = 2.
        when(managementNode.getMessageSessions(eq(ManagementConstants.ACTIVE_MESSAGES_SENTINEL), eq(0), eq(100),
            isNull())).thenReturn(Mono.just(new MessageSessionsResult(Arrays.asList("s1", "s2"), 2)));
        // Cursor for the second page is encoded server-skip + base64url(lastSessionId), which decodes
        // back to (skip=2, lastSessionId="s2"). Empty page terminates pagination.
        when(managementNode.getMessageSessions(eq(ManagementConstants.ACTIVE_MESSAGES_SENTINEL), eq(2), eq(100),
            eq("s2"))).thenReturn(Mono.just(new MessageSessionsResult(Collections.emptyList(), 2)));

        final ServiceBusSessionReceiverAsyncClient client = newSessionReceiver();

        StepVerifier.create(client.listSessions()).expectNext("s1", "s2").expectComplete().verify(DEFAULT_TIMEOUT);
    }

    /**
     * Verifies the cursor uses the server-returned skip (which may differ from {@code requestSkip +
     * page.size()} when the broker filters expired entries between pages) and the last session ID
     * of the previous page, matching Track 1's SessionBrowser semantics.
     */
    @Test
    void listSessionsHonorsServerSkipAndLastSessionId() {
        final OffsetDateTime sessionStateUpdatedAfter = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        // First page returns 2 items but the server reports skip = 7 (5 entries filtered server-side).
        when(managementNode.getMessageSessions(eq(sessionStateUpdatedAfter), eq(0), eq(100), isNull()))
            .thenReturn(Mono.just(new MessageSessionsResult(Arrays.asList("a", "b"), 7)));
        // Second-page request must use the server-returned skip (7) and lastSessionId ("b").
        when(managementNode.getMessageSessions(eq(sessionStateUpdatedAfter), eq(7), eq(100), eq("b")))
            .thenReturn(Mono.just(new MessageSessionsResult(Collections.singletonList("c"), 8)));
        // Third page empty terminates pagination.
        when(managementNode.getMessageSessions(eq(sessionStateUpdatedAfter), eq(8), eq(100), eq("c")))
            .thenReturn(Mono.just(new MessageSessionsResult(Collections.emptyList(), 8)));

        final ServiceBusSessionReceiverAsyncClient client = newSessionReceiver();

        StepVerifier.create(client.listSessions(sessionStateUpdatedAfter))
            .expectNext("a", "b", "c")
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    /**
     * Verifies that null {@code sessionStateUpdatedAfter} surfaces as a logged {@link NullPointerException} via
     * the {@link reactor.core.publisher.Flux} returned by the {@link com.azure.core.http.rest.PagedFlux},
     * matching the contract documented on the public method.
     */
    @Test
    void listSessionsRejectsNullSessionStateUpdatedAfter() {
        final ServiceBusSessionReceiverAsyncClient client = newSessionReceiver();

        StepVerifier.create(client.listSessions(null)).expectError(NullPointerException.class).verify(DEFAULT_TIMEOUT);
    }

    /**
     * Verifies the continuation token round-trips arbitrary session IDs (including ones that
     * contain the {@code |} separator character) without escaping the cursor. This is the
     * Base64url-encoded payload guarantee.
     */
    @Test
    void listSessionsRoundTripsArbitrarySessionIdsThroughCursor() {
        final String sessionWithPipe = "weird|session|id";

        when(managementNode.getMessageSessions(eq(ManagementConstants.ACTIVE_MESSAGES_SENTINEL), eq(0), eq(100),
            isNull())).thenReturn(Mono.just(new MessageSessionsResult(Collections.singletonList(sessionWithPipe), 1)));
        when(managementNode.getMessageSessions(eq(ManagementConstants.ACTIVE_MESSAGES_SENTINEL), eq(1), eq(100),
            eq(sessionWithPipe))).thenReturn(Mono.just(new MessageSessionsResult(Collections.emptyList(), 1)));

        final ServiceBusSessionReceiverAsyncClient client = newSessionReceiver();

        StepVerifier.create(client.listSessions()).expectNext(sessionWithPipe).expectComplete().verify(DEFAULT_TIMEOUT);
    }

    /**
     * Verifies that calling {@link com.azure.core.http.rest.PagedFlux#byPage(String)} with a token
     * that doesn't match the {@code <skip>|<base64url(lastSessionId)>} format surfaces a clear
     * {@link IllegalArgumentException} via {@code monoError(LOGGER, ...)} rather than
     * {@code NumberFormatException} / {@code IndexOutOfBoundsException}.
     */
    @Test
    void listSessionsRejectsInvalidContinuationToken() {
        final ServiceBusSessionReceiverAsyncClient client = newSessionReceiver();

        StepVerifier.create(client.listSessions().byPage("not-a-valid-token"))
            .expectError(IllegalArgumentException.class)
            .verify(DEFAULT_TIMEOUT);
    }

    /**
     * Verifies that a continuation token whose decoded skip is negative (e.g., {@code "-1|...")} is
     * rejected with {@link IllegalArgumentException} rather than producing an invalid management
     * request with a negative {@code skip} on the wire.
     */
    @Test
    void listSessionsRejectsNegativeSkipInContinuationToken() {
        final ServiceBusSessionReceiverAsyncClient client = newSessionReceiver();

        StepVerifier.create(client.listSessions().byPage("-1|YWJj"))
            .expectError(IllegalArgumentException.class)
            .verify(DEFAULT_TIMEOUT);
    }

    /**
     * Verifies that a continuation token whose Base64 payload decodes to bytes that aren't valid
     * UTF-8 is rejected with {@link IllegalArgumentException}. Without strict decoding,
     * {@code new String(decoded, UTF_8)} would silently substitute U+FFFD and we'd send a
     * corrupted session ID to the broker.
     */
    @Test
    void listSessionsRejectsNonUtf8ContinuationToken() {
        // 0xFF, 0xFE is a stray UTF-16 BOM and isn't valid UTF-8. Base64url("0xFF 0xFE") = "__4".
        final ServiceBusSessionReceiverAsyncClient client = newSessionReceiver();

        StepVerifier.create(client.listSessions().byPage("0|__4"))
            .expectError(IllegalArgumentException.class)
            .verify(DEFAULT_TIMEOUT);
    }

    /**
     * Verifies that an empty continuation token completes the {@link PagedFlux} without error,
     * matching the convention used by {@code ServiceBusAdministrationAsyncClient.listQueuesNextPage}
     * and the wider Azure SDK paging APIs (null or empty token = no more pages). Tolerant of
     * callers that persist the token to storage and read back an empty string.
     */
    @Test
    void listSessionsEmptyContinuationTokenCompletes() {
        final ServiceBusSessionReceiverAsyncClient client = newSessionReceiver();

        StepVerifier.create(client.listSessions().byPage("")).expectComplete().verify(DEFAULT_TIMEOUT);
    }

    /**
     * Verifies that a caller-supplied page size from {@link PagedFlux#byPage(int)} flows through
     * to the management request's {@code top} parameter. Without page-size propagation the broker
     * would always be asked for the default of 100 entries even when the caller requested fewer.
     */
    @Test
    void listSessionsHonorsCallerPageSize() {
        // Caller asks for pages of 25; both first-page and next-page calls must pass top=25.
        when(managementNode.getMessageSessions(eq(ManagementConstants.ACTIVE_MESSAGES_SENTINEL), eq(0), eq(25),
            isNull())).thenReturn(Mono.just(new MessageSessionsResult(Arrays.asList("a", "b"), 2)));
        when(
            managementNode.getMessageSessions(eq(ManagementConstants.ACTIVE_MESSAGES_SENTINEL), eq(2), eq(25), eq("b")))
                .thenReturn(Mono.just(new MessageSessionsResult(Collections.emptyList(), 2)));

        final ServiceBusSessionReceiverAsyncClient client = newSessionReceiver();

        StepVerifier.create(client.listSessions().byPage(25)).assertNext(page -> {
            org.junit.jupiter.api.Assertions.assertEquals(2, page.getValue().size());
            org.junit.jupiter.api.Assertions.assertEquals("a", page.getValue().get(0));
            org.junit.jupiter.api.Assertions.assertEquals("b", page.getValue().get(1));
        })
            .assertNext(page -> org.junit.jupiter.api.Assertions.assertTrue(page.getValue().isEmpty()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }
}
