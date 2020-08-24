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
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import com.azure.messaging.servicebus.administration.models.DeadLetterOptions;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessageWithLockToken;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.implementation.ServiceBusReactorReceiver;
import com.azure.messaging.servicebus.models.LockRenewalStatus;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.transport.DeliveryState.DeliveryStateType;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.messaging.servicebus.TestUtils.getMessage;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceBusReceiverAsyncClientTest {
    private static final String PAYLOAD = "hello";
    private static final byte[] PAYLOAD_BYTES = PAYLOAD.getBytes(UTF_8);
    private static final int PREFETCH = 5;
    private static final String NAMESPACE = "my-namespace-foo.net";
    private static final String ENTITY_PATH = "queue-name";
    private static final MessagingEntityType ENTITY_TYPE = MessagingEntityType.QUEUE;
    private static final String NAMESPACE_CONNECTION_STRING = String.format(
        "Endpoint=sb://%s;SharedAccessKeyName=%s;SharedAccessKey=%s",
        NAMESPACE, "some-name", "something-else");
    private static final Duration CLEANUP_INTERVAL = Duration.ofSeconds(10);
    private static final String SESSION_ID = "my-session-id";

    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverAsyncClientTest.class);
    private final String messageTrackingUUID = UUID.randomUUID().toString();
    private final ReplayProcessor<AmqpEndpointState> endpointProcessor = ReplayProcessor.cacheLast();
    private final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private final DirectProcessor<Message> messageProcessor = DirectProcessor.create();
    private final FluxSink<Message> messageSink = messageProcessor.sink(FluxSink.OverflowStrategy.BUFFER);

    private ServiceBusConnectionProcessor connectionProcessor;
    private ServiceBusReceiverAsyncClient receiver;
    private ServiceBusReceiverAsyncClient sessionReceiver;
    private Duration maxAutoLockRenewalDuration;

    @Mock
    private ServiceBusReactorReceiver amqpReceiveLink;
    @Mock
    private ServiceBusReactorReceiver sessionReceiveLink;
    @Mock
    private ServiceBusAmqpConnection connection;
    @Mock
    private TokenCredential tokenCredential;
    @Mock
    private MessageSerializer messageSerializer;
    @Mock
    private TracerProvider tracerProvider;
    @Mock
    private ServiceBusManagementNode managementNode;
    @Mock
    private ServiceBusReceivedMessage receivedMessage;
    @Mock
    private ServiceBusReceivedMessage receivedMessage2;
    @Mock
    private Runnable onClientClose;
    @Mock
    private Function<String, Mono<Instant>> renewalOperation;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(100));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        logger.info("[{}] Setting up.", testInfo.getDisplayName());

        MockitoAnnotations.initMocks(this);

        // Forcing us to publish the messages we receive on the AMQP link on single. Similar to how it is done
        // in ReactorExecutor.
        when(amqpReceiveLink.receive()).thenReturn(messageProcessor.publishOn(Schedulers.single()));
        when(amqpReceiveLink.getEndpointStates()).thenReturn(endpointProcessor);

        ConnectionOptions connectionOptions = new ConnectionOptions(NAMESPACE, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP, new AmqpRetryOptions(),
            ProxyOptions.SYSTEM_DEFAULTS, Schedulers.boundedElastic());

        when(connection.getEndpointStates()).thenReturn(endpointProcessor);
        endpointSink.next(AmqpEndpointState.ACTIVE);

        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE))
            .thenReturn(Mono.just(managementNode));

        when(connection.createReceiveLink(anyString(), anyString(), any(ReceiveMode.class), any(),
            any(MessagingEntityType.class))).thenReturn(Mono.just(amqpReceiveLink));
        when(connection.createReceiveLink(anyString(), anyString(), any(ReceiveMode.class), any(),
            any(MessagingEntityType.class), anyString())).thenReturn(Mono.just(sessionReceiveLink));

        connectionProcessor =
            Flux.<ServiceBusAmqpConnection>create(sink -> sink.next(connection))
                .subscribeWith(new ServiceBusConnectionProcessor(connectionOptions.getFullyQualifiedNamespace(),
                    connectionOptions.getRetry()));

        receiver = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE,
            new ReceiverOptions(ReceiveMode.PEEK_LOCK, PREFETCH, maxAutoLockRenewalDuration), connectionProcessor, CLEANUP_INTERVAL,
            tracerProvider, messageSerializer, onClientClose);

        sessionReceiver = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE,
            new ReceiverOptions(ReceiveMode.PEEK_LOCK, PREFETCH, maxAutoLockRenewalDuration, "Some-Session", false, null),
            connectionProcessor, CLEANUP_INTERVAL, tracerProvider, messageSerializer, onClientClose);
    }

    @AfterEach
    void teardown(TestInfo testInfo) {
        logger.info("[{}] Tearing down.", testInfo.getDisplayName());

        receiver.close();
        Mockito.framework().clearInlineMocks();
    }

    /**
     * Verifies that when user calls peek more than one time, It returns different object.
     */
    @SuppressWarnings("unchecked")
    @Test
    void peekTwoMessages() {
        final long sequence1 = 10;
        final long sequence2 = 12;
        final ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        when(receivedMessage.getSequenceNumber()).thenReturn(sequence1);
        when(receivedMessage2.getSequenceNumber()).thenReturn(sequence2);
        // Arrange
        when(managementNode.peek(anyLong(), isNull(), isNull()))
            .thenReturn(Mono.just(receivedMessage), Mono.just(receivedMessage2));

        // Act & Assert
        StepVerifier.create(receiver.peekMessage())
            .expectNext(receivedMessage)
            .verifyComplete();

        // Act & Assert
        StepVerifier.create(receiver.peekMessage())
            .expectNext(receivedMessage2)
            .verifyComplete();

        verify(managementNode, times(2)).peek(captor.capture(), isNull(), isNull());
        final List<Long> allValues = captor.getAllValues();

        Assertions.assertEquals(2, allValues.size());

        // Because we always add one when we fetch the next message.
        Assertions.assertTrue(allValues.contains(0L));
        Assertions.assertTrue(allValues.contains(11L));
    }

    /**
     * Verifies that this peek one messages from a sequence Number.
     */
    @Test
    void peekWithSequenceOneMessage() {
        // Arrange
        final int fromSequenceNumber = 10;
        final ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);

        when(managementNode.peek(fromSequenceNumber, null, null)).thenReturn(Mono.just(receivedMessage));

        // Act & Assert
        StepVerifier.create(receiver.peekMessageAt(fromSequenceNumber))
            .expectNext(receivedMessage)
            .verifyComplete();
    }

    /**
     * Verifies that this receives a number of messages. Verifies that the initial credits we add are equal to the
     * prefetch value.
     */
    @Test
    void receivesNumberOfEvents() {
        // Arrange
        final int numberOfEvents = 1;
        final List<Message> messages = getMessages(10);

        ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);
        when(receivedMessage.getLockToken()).thenReturn(UUID.randomUUID().toString());
        when(messageSerializer.deserialize(any(Message.class), eq(ServiceBusReceivedMessage.class)))
            .thenReturn(receivedMessage);

        // Act & Assert
        StepVerifier.create(receiver.receiveMessages().take(numberOfEvents))
            .then(() -> messages.forEach(m -> messageSink.next(m)))
            .expectNextCount(numberOfEvents)
            .verifyComplete();

        verify(amqpReceiveLink).addCredits(PREFETCH);
    }

    /**
     * Verifies that we error if we try to settle a message with null transaction.
     */
    @ParameterizedTest
    @EnumSource(DispositionStatus.class)
    void settleWithNullTransaction(DispositionStatus dispositionStatus) {
        // Arrange
        ServiceBusTransactionContext nullTransaction = null;
        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE)).thenReturn(Mono.just(managementNode));
        when(managementNode.updateDisposition(any(), eq(dispositionStatus), isNull(), isNull(), isNull(),
            isNull(), isNull(), isNull()))
            .thenReturn(Mono.delay(Duration.ofMillis(250)).then());
        when(receivedMessage.getLockToken()).thenReturn("mylockToken");

        final Mono<Void> operation;
        switch (dispositionStatus) {
            case DEFERRED:
                operation = receiver.defer(receivedMessage.getLockToken(), null, nullTransaction);
                break;
            case ABANDONED:
                operation = receiver.abandon(receivedMessage.getLockToken(), null, nullTransaction);
                break;
            case COMPLETED:
                operation = receiver.complete(receivedMessage.getLockToken(), nullTransaction);
                break;
            case SUSPENDED:
                operation = receiver.deadLetter(receivedMessage.getLockToken(), new DeadLetterOptions(), nullTransaction);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized operation: " + dispositionStatus);
        }

        StepVerifier.create(operation)
            .expectError(NullPointerException.class)
            .verify();

        verify(managementNode, never()).updateDisposition(any(), eq(dispositionStatus), isNull(), isNull(),
            isNull(), isNull(), isNull(), isNull());
    }

    /**
     * Verifies that we error if we try to settle a message with null transaction-id.
     */
    @ParameterizedTest
    @EnumSource(DispositionStatus.class)
    void settleWithNullTransactionId(DispositionStatus dispositionStatus) {
        // Arrange
        ServiceBusTransactionContext nullTransactionId = new ServiceBusTransactionContext(null);
        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE)).thenReturn(Mono.just(managementNode));
        when(managementNode.updateDisposition(any(), eq(dispositionStatus), isNull(), isNull(), isNull(),
            isNull(), isNull(), isNull()))
            .thenReturn(Mono.delay(Duration.ofMillis(250)).then());
        when(receivedMessage.getLockToken()).thenReturn("mylockToken");

        final Mono<Void> operation;
        switch (dispositionStatus) {
            case DEFERRED:
                operation = receiver.defer(receivedMessage.getLockToken(), null, nullTransactionId);
                break;
            case ABANDONED:
                operation = receiver.abandon(receivedMessage.getLockToken(), null, nullTransactionId);
                break;
            case COMPLETED:
                operation = receiver.complete(receivedMessage.getLockToken(), nullTransactionId);
                break;
            case SUSPENDED:
                operation = receiver.deadLetter(receivedMessage.getLockToken(), new DeadLetterOptions(), nullTransactionId);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized operation: " + dispositionStatus);
        }

        StepVerifier.create(operation)
            .expectError(NullPointerException.class)
            .verify();

        verify(managementNode, never()).updateDisposition(any(), eq(dispositionStatus), isNull(), isNull(),
            isNull(), isNull(), isNull(), isNull());
    }

    /**
     * Verifies that we error if we try to complete a message without a lock token.
     */
    @Test
    void completeNullLockToken() {
        // Arrange
        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE)).thenReturn(Mono.just(managementNode));
        when(managementNode.updateDisposition(any(), eq(DispositionStatus.COMPLETED), isNull(), isNull(), isNull(),
            isNull(), isNull(), isNull()))
            .thenReturn(Mono.delay(Duration.ofMillis(250)).then());

        when(receivedMessage.getLockToken()).thenReturn(null);

        StepVerifier.create(receiver.complete(receivedMessage.getLockToken()))
            .expectError(NullPointerException.class)
            .verify();

        verify(managementNode, never()).updateDisposition(any(), eq(DispositionStatus.COMPLETED), isNull(), isNull(),
            isNull(), isNull(), isNull(), isNull());
    }

    /**
     * Verifies that we error if we try to complete a null message.
     */
    @Test
    void completeNullMessage() {
        StepVerifier.create(receiver.complete(null)).expectError(NullPointerException.class).verify();
    }

    /**
     * Verifies that we error if we complete in RECEIVE_AND_DELETE mode.
     */
    @Test
    void completeInReceiveAndDeleteMode() {
        final ReceiverOptions options = new ReceiverOptions(ReceiveMode.RECEIVE_AND_DELETE, PREFETCH, maxAutoLockRenewalDuration);
        ServiceBusReceiverAsyncClient client = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH,
            MessagingEntityType.QUEUE, options, connectionProcessor, CLEANUP_INTERVAL, tracerProvider,
            messageSerializer, onClientClose);

        final String lockToken1 = UUID.randomUUID().toString();

        when(receivedMessage.getLockToken()).thenReturn(lockToken1);

        try {
            StepVerifier.create(client.complete(receivedMessage.getLockToken()))
                .expectError(UnsupportedOperationException.class)
                .verify();
        } finally {
            client.close();
        }
    }

    /**
     * Verifies that this peek batch of messages.
     */
    @Test
    void peekBatchMessages() {
        // Arrange
        final int numberOfEvents = 2;

        when(managementNode.peek(0, null, null, numberOfEvents))
            .thenReturn(Flux.fromArray(new ServiceBusReceivedMessage[]{receivedMessage, receivedMessage2}));

        // Act & Assert
        StepVerifier.create(receiver.peekMessages(numberOfEvents))
            .expectNextCount(numberOfEvents)
            .verifyComplete();
    }

    /**
     * Verifies that this peek batch of messages from a sequence Number.
     */
    @Test
    void peekBatchWithSequenceNumberMessages() {
        // Arrange
        final int numberOfEvents = 2;
        final int fromSequenceNumber = 10;

        when(managementNode.peek(fromSequenceNumber, null, null, numberOfEvents))
            .thenReturn(Flux.fromArray(new ServiceBusReceivedMessage[]{receivedMessage, receivedMessage2}));

        // Act & Assert
        StepVerifier.create(receiver.peekMessagesAt(numberOfEvents, fromSequenceNumber))
            .expectNext(receivedMessage, receivedMessage2)
            .verifyComplete();
    }

    /**
     * Verifies that we can deadletter a message with an error and description.
     */
    @Test
    void deadLetterWithDescription() {
        final String lockToken1 = UUID.randomUUID().toString();
        final String description = "some-dead-letter-description";
        final String reason = "dead-letter-reason";
        final Map<String, Object> propertiesToModify = new HashMap<>();
        propertiesToModify.put("something", true);

        final DeadLetterOptions deadLetterOptions = new DeadLetterOptions().setDeadLetterReason(reason)
            .setDeadLetterErrorDescription(description)
            .setPropertiesToModify(propertiesToModify);

        final Instant expiration = Instant.now().plus(Duration.ofMinutes(5));

        final MessageWithLockToken message = mock(MessageWithLockToken.class);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);

        when(receivedMessage.getLockToken()).thenReturn(lockToken1);
        when(receivedMessage.getLockedUntil()).thenReturn(expiration);

        when(amqpReceiveLink.updateDisposition(eq(lockToken1), argThat(e -> e.getType() == DeliveryStateType.Rejected))).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(receiver.receiveMessages()
            .take(1)
            .flatMap(context -> receiver.deadLetter(context.getMessage().getLockToken(), deadLetterOptions)))
            .then(() -> messageSink.next(message))
            .expectNext()
            .verifyComplete();

        verify(amqpReceiveLink).updateDisposition(eq(lockToken1), isA(Rejected.class));
    }

    /**
     * Verifies that the user can complete settlement methods on received message.
     */
    @ParameterizedTest
    @EnumSource(DispositionStatus.class)
    void settleMessageOnManagement(DispositionStatus dispositionStatus) {
        // Arrange
        final String lockToken1 = UUID.randomUUID().toString();
        final String lockToken2 = UUID.randomUUID().toString();
        final Instant expiration = Instant.now().plus(Duration.ofMinutes(5));
        final long sequenceNumber = 10L;
        final long sequenceNumber2 = 15L;

        final MessageWithLockToken message = mock(MessageWithLockToken.class);
        final MessageWithLockToken message2 = mock(MessageWithLockToken.class);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);
        when(messageSerializer.deserialize(message2, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage2);

        when(receivedMessage.getLockToken()).thenReturn(lockToken1);
        when(receivedMessage.getLockedUntil()).thenReturn(expiration);
        when(receivedMessage2.getLockToken()).thenReturn(lockToken2);
        when(receivedMessage2.getLockedUntil()).thenReturn(expiration);

        when(connection.getManagementNode(eq(ENTITY_PATH), eq(ENTITY_TYPE)))
            .thenReturn(Mono.just(managementNode));

        when(managementNode.receiveDeferredMessages(eq(ReceiveMode.PEEK_LOCK), isNull(), isNull(), argThat(arg -> {
            boolean foundFirst = false;
            boolean foundSecond = false;
            for (Long seq : arg) {
                if (!foundFirst && sequenceNumber == seq) {
                    foundFirst = true;
                } else if (!foundSecond && sequenceNumber2 == seq) {
                    foundSecond = true;
                }
            }

            return foundFirst && foundSecond;
        })))
            .thenReturn(Flux.just(receivedMessage, receivedMessage2));

        when(managementNode.updateDisposition(lockToken1, dispositionStatus, null, null, null, null, null, null))
            .thenReturn(Mono.empty());
        when(managementNode.updateDisposition(lockToken2, dispositionStatus, null, null, null, null, null, null))
            .thenReturn(Mono.empty());

        // Pretend we receive these before. This is to simulate that so that the receiver keeps track of them in
        // the lock map.
        StepVerifier.create(receiver.receiveDeferredMessages(Arrays.asList(sequenceNumber, sequenceNumber2)))
            .expectNext(receivedMessage, receivedMessage2)
            .verifyComplete();

        // Act and Assert
        final Mono<Void> operation;
        switch (dispositionStatus) {
            case DEFERRED:
                operation = receiver.defer(receivedMessage.getLockToken());
                break;
            case ABANDONED:
                operation = receiver.abandon(receivedMessage.getLockToken());
                break;
            case COMPLETED:
                operation = receiver.complete(receivedMessage.getLockToken());
                break;
            case SUSPENDED:
                operation = receiver.deadLetter(receivedMessage.getLockToken());
                break;
            default:
                throw new IllegalArgumentException("Unrecognized operation: " + dispositionStatus);
        }

        StepVerifier.create(operation)
            .verifyComplete();

        verify(managementNode).updateDisposition(lockToken1, dispositionStatus, null, null, null, null, null, null);
        verify(managementNode, never()).updateDisposition(lockToken2, dispositionStatus, null, null, null, null, null, null);
    }

    /**
     * Verifies that this receive deferred one messages from a sequence Number.
     */
    @Test
    void receiveDeferredWithSequenceOneMessage() {
        // Arrange
        final int fromSequenceNumber = 10;
        final ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);

        when(managementNode.receiveDeferredMessages(any(), any(), any(), any())).thenReturn(Flux.just(receivedMessage));

        // Act & Assert
        StepVerifier.create(receiver.receiveDeferredMessage(fromSequenceNumber))
            .expectNext(receivedMessage)
            .verifyComplete();
    }

    /**
     * Verifies that this receive deferred messages from a sequence Number.
     */
    @Test
    void receiveDeferredBatchFromSequenceNumber() {
        // Arrange
        final long fromSequenceNumber1 = 10;
        final long fromSequenceNumber2 = 11;

        when(managementNode.receiveDeferredMessages(any(), any(), any(), any()))
            .thenReturn(Flux.fromArray(new ServiceBusReceivedMessage[]{receivedMessage, receivedMessage2}));

        // Act & Assert
        StepVerifier.create(receiver.receiveDeferredMessages(Arrays.asList(fromSequenceNumber1, fromSequenceNumber2)))
            .expectNext(receivedMessage)
            .expectNext(receivedMessage2)
            .verifyComplete();
    }

    /**
     * Verifies that the onClientClose is called.
     */
    @Test
    void callsClientClose() {
        // Act
        receiver.close();

        // Assert
        verify(onClientClose).run();
    }

    /**
     * Verifies that the onClientClose is only called once.
     */
    @Test
    void callsClientCloseOnce() {
        // Act
        receiver.close();
        receiver.close();

        // Assert
        verify(onClientClose).run();
    }

    /**
     * Tests that invalid options throws and null options.
     */
    @Test
    void receiveIllegalOptions() {
        // Arrange
        ServiceBusReceiverClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(NAMESPACE_CONNECTION_STRING)
            .receiver()
            .topicName("baz").subscriptionName("bar")
            .maxAutoLockRenewalDuration(Duration.ofSeconds(-1))
            .receiveMode(ReceiveMode.PEEK_LOCK);

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.buildAsyncClient());
    }

    @Test
    void topicCorrectEntityPath() {
        // Arrange
        final String topicName = "foo";
        final String subscriptionName = "bar";
        final String entityPath = String.join("/", topicName, "subscriptions", subscriptionName);
        final ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(NAMESPACE_CONNECTION_STRING)
            .receiver()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .receiveMode(ReceiveMode.PEEK_LOCK)
            .buildAsyncClient();

        // Act
        final String actual = receiver.getEntityPath();
        final String actualNamespace = receiver.getFullyQualifiedNamespace();

        // Assert
        Assertions.assertEquals(entityPath, actual);
        Assertions.assertEquals(NAMESPACE, actualNamespace);
    }

    /**
     * Cannot get session state for non-session receiver.
     */
    @Test
    void cannotPerformGetSessionState() {
        // Arrange
        final String sessionId = "a-session-id";

        // Act & Assert
        StepVerifier.create(receiver.getSessionState(sessionId))
            .expectError(IllegalStateException.class)
            .verify();
    }

    /**
     * Cannot get session state for non-session receiver.
     */
    @Test
    void cannotPerformSetSessionState() {
        // Arrange
        final String sessionId = "a-session-id";
        final byte[] sessionState = new byte[]{10, 11, 8};

        // Act & Assert
        StepVerifier.create(receiver.setSessionState(sessionId, sessionState))
            .expectError(IllegalStateException.class)
            .verify();
    }

    /**
     * Cannot get session state for non-session receiver.
     */
    @Test
    void cannotPerformRenewSessionLock() {
        // Arrange
        final String sessionId = "a-session-id";

        // Act & Assert
        StepVerifier.create(receiver.renewSessionLock(sessionId))
            .expectError(IllegalStateException.class)
            .verify();
    }

    /**
     * Verifies that we can get a session state.
     */
    @SuppressWarnings("unchecked")
    @Test
    void getSessionState() {
        // Arrange
        final byte[] bytes = new byte[]{95, 11, 54, 10};

        when(managementNode.getSessionState(SESSION_ID, null))
            .thenReturn(Mono.just(bytes), Mono.empty());

        // Act & Assert
        StepVerifier.create(sessionReceiver.getSessionState(SESSION_ID))
            .expectNext(bytes)
            .expectComplete()
            .verify();
    }

    /**
     * Verifies that we can set a session state.
     */
    @Test
    void setSessionState() {
        // Arrange
        final byte[] bytes = new byte[]{95, 11, 54, 10};

        when(managementNode.setSessionState(SESSION_ID, bytes, null)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(sessionReceiver.setSessionState(SESSION_ID, bytes))
            .expectComplete()
            .verify();
    }

    /**
     * Verifies that we can renew a session state.
     */
    @Test
    void renewSessionLock() {
        // Arrange
        final Instant expiry = Instant.ofEpochSecond(1588011761L);

        when(managementNode.renewSessionLock(SESSION_ID, null)).thenReturn(Mono.just(expiry));

        // Act & Assert
        StepVerifier.create(sessionReceiver.renewSessionLock(SESSION_ID))
            .expectNext(expiry)
            .expectComplete()
            .verify();
    }

    /**
     * Verifies that we cannot renew a message lock when using a session receiver.
     */
    @Test
    void cannotRenewMessageLockInSession() {
        // Arrange
        final String lockToken = UUID.randomUUID().toString();

        // Act & Assert
        StepVerifier.create(sessionReceiver.renewMessageLock(lockToken))
            .expectError(IllegalStateException.class)
            .verify();
    }

    /**
     * Verifies that we can auto-renew a message lock.
     */
    @Test
    void autoRenewMessageLock() throws InterruptedException {
        // Arrange
        final Duration maxDuration = Duration.ofSeconds(8);
        final Duration renewalPeriod = Duration.ofSeconds(3);
        final String lockToken = "some-token";
        final Instant startTime = Instant.now();

        // At most 4 times because we renew the lock before it expires (by some seconds).
        final int atMost = 5;
        final Duration totalSleepPeriod = maxDuration.plusMillis(500);

        when(managementNode.renewMessageLock(lockToken, null))
            .thenReturn(Mono.fromCallable(() -> Instant.now().plus(renewalPeriod)));

        // Act & Assert
        final LockRenewalOperation operation = receiver.getAutoRenewMessageLock(lockToken, maxDuration);
        Thread.sleep(totalSleepPeriod.toMillis());
        logger.info("Finished renewals for first sleep.");

        // Assert
        assertEquals(LockRenewalStatus.COMPLETE, operation.getStatus());
        assertNull(operation.getThrowable());
        assertTrue(startTime.isBefore(operation.getLockedUntil()), String.format(
            "initial lockedUntil[%s] is not before lockedUntil[%s]", startTime, operation.getLockedUntil()));

        verify(managementNode, Mockito.atMost(atMost)).renewMessageLock(lockToken, null);
    }


    /**
     * Verifies that we can auto-renew a message lock.
     */
    @Test
    void autoRenewSessionLock() throws InterruptedException {
        // Arrange
        final Duration maxDuration = Duration.ofSeconds(8);
        final Duration renewalPeriod = Duration.ofSeconds(3);
        final String sessionId = "some-token";
        final Instant startTime = Instant.now();

        // At most 4 times because we renew the lock before it expires (by some seconds).
        final int atMost = 5;
        final Duration totalSleepPeriod = maxDuration.plusMillis(500);

        when(managementNode.renewSessionLock(sessionId, null))
            .thenReturn(Mono.fromCallable(() -> Instant.now().plus(renewalPeriod)));

        // Act & Assert
        final LockRenewalOperation operation = sessionReceiver.getAutoRenewSessionLock(sessionId, maxDuration);
        Thread.sleep(totalSleepPeriod.toMillis());
        logger.info("Finished renewals for first sleep.");

        // Assert
        assertEquals(LockRenewalStatus.COMPLETE, operation.getStatus());
        assertNull(operation.getThrowable());
        assertTrue(startTime.isBefore(operation.getLockedUntil()), String.format(
            "initial lockedUntil[%s] is not before lockedUntil[%s]", startTime, operation.getLockedUntil()));

        verify(managementNode, Mockito.atMost(atMost)).renewSessionLock(sessionId, null);
    }

    private List<Message> getMessages(int numberOfEvents) {
        final Map<String, String> map = Collections.singletonMap("SAMPLE_HEADER", "foo");

        return IntStream.range(0, numberOfEvents)
            .mapToObj(index -> getMessage(PAYLOAD_BYTES, messageTrackingUUID, map))
            .collect(Collectors.toList());
    }
}
