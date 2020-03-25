// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.CbsAuthorizationType;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessageLockContainer;
import com.azure.messaging.servicebus.implementation.MessageWithLockToken;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.messaging.servicebus.TestUtils.getMessage;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

class ServiceBusReceiverAsyncClientTest {
    private static final String PAYLOAD = "hello";
    private static final byte[] PAYLOAD_BYTES = PAYLOAD.getBytes(UTF_8);
    private static final int PREFETCH = 5;
    private static final String NAMESPACE = "my-namespace-foo";
    private static final String ENTITY_PATH = "queue-name";
    private static final MessagingEntityType ENTITY_TYPE = MessagingEntityType.QUEUE;

    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverAsyncClientTest.class);
    private final String messageTrackingUUID = UUID.randomUUID().toString();
    private final DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();
    private final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private final DirectProcessor<Message> messageProcessor = DirectProcessor.create();
    private final FluxSink<Message> messageSink = messageProcessor.sink(FluxSink.OverflowStrategy.BUFFER);

    private ServiceBusConnectionProcessor connectionProcessor;
    private ServiceBusReceiverAsyncClient consumer;
    private ReceiveMessageOptions receiveOptions;
    private MessageLockContainer messageContainer;

    @Mock
    private AmqpReceiveLink amqpReceiveLink;
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

        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE)).thenReturn(Mono.just(managementNode));
        when(connection.createReceiveLink(anyString(), anyString(), any(ReceiveMode.class), anyBoolean(), any(),
            any(MessagingEntityType.class))).thenReturn(Mono.just(amqpReceiveLink));

        connectionProcessor =
            Flux.<ServiceBusAmqpConnection>create(sink -> sink.next(connection))
                .subscribeWith(new ServiceBusConnectionProcessor(connectionOptions.getFullyQualifiedNamespace(),
                    connectionOptions.getRetry()));

        receiveOptions = new ReceiveMessageOptions(false, ReceiveMode.PEEK_LOCK, PREFETCH, false,
            Duration.ofSeconds(10));

        messageContainer = new MessageLockContainer();
        consumer = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE,
            false, receiveOptions, connectionProcessor, tracerProvider, messageSerializer,
            messageContainer);
    }

    @AfterEach
    void teardown(TestInfo testInfo) {
        logger.info("[{}] Tearing down.", testInfo.getDisplayName());

        consumer.close();
        Mockito.framework().clearInlineMocks();
    }

    /**
     * Verifies that when user calls peek more than one time, It returns different object.
     */
    @SuppressWarnings("unchecked")
    @Test
    void peekTwoMessages() {
        // Arrange
        when(managementNode.peek()).thenReturn(Mono.just(receivedMessage), Mono.just(receivedMessage2));

        // Act & Assert
        StepVerifier.create(consumer.peek())
            .expectNext(receivedMessage)
            .verifyComplete();

        // Act & Assert
        StepVerifier.create(consumer.peek())
            .expectNext(receivedMessage2)
            .verifyComplete();
    }

    /**
     * Verifies that this peek one messages from a sequence Number.
     */
    @Test
    void peekWithSequenceOneMessage() {
        // Arrange
        final int fromSequenceNumber = 10;
        final ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);

        when(managementNode.peek(fromSequenceNumber)).thenReturn(Mono.just(receivedMessage));

        // Act & Assert
        StepVerifier.create(consumer.peek(fromSequenceNumber))
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

        when(messageSerializer.deserialize(any(Message.class), eq(ServiceBusReceivedMessage.class)))
            .thenReturn(mock(ServiceBusReceivedMessage.class));

        // Act & Assert
        StepVerifier.create(consumer.receive().take(numberOfEvents))
            .then(() -> messages.forEach(m -> messageSink.next(m)))
            .expectNextCount(numberOfEvents)
            .verifyComplete();

        verify(amqpReceiveLink).addCredits(PREFETCH);
    }

    /**
     * Verifies that we can receive messages from the processor.
     */
    @Test
    void receivesAndAutoCompletes() {
        // Arrange
        final ReceiveMessageOptions options = new ReceiveMessageOptions(true, ReceiveMode.PEEK_LOCK,
            PREFETCH, false, null);
        final ServiceBusReceiverAsyncClient consumer2 = new ServiceBusReceiverAsyncClient(
            NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE, false, options, connectionProcessor,
            tracerProvider, messageSerializer, messageContainer);

        final UUID lockToken1 = UUID.randomUUID();
        final UUID lockToken2 = UUID.randomUUID();
        final Instant expiration = Instant.now().plus(Duration.ofMinutes(1));

        final MessageWithLockToken message = mock(MessageWithLockToken.class);
        final MessageWithLockToken message2 = mock(MessageWithLockToken.class);

        when(message.getLockToken()).thenReturn(lockToken1);
        when(message2.getLockToken()).thenReturn(lockToken2);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);
        when(messageSerializer.deserialize(message2, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage2);

        when(receivedMessage.getLockToken()).thenReturn(lockToken1);
        when(receivedMessage.getLockedUntil()).thenReturn(expiration);
        when(receivedMessage2.getLockToken()).thenReturn(lockToken2);
        when(receivedMessage2.getLockedUntil()).thenReturn(expiration);

        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE))
            .thenReturn(Mono.just(managementNode));

        when(managementNode.updateDisposition(eq(lockToken1), eq(DispositionStatus.COMPLETED), isNull(), isNull(), isNull()))
            .thenReturn(Mono.empty());
        when(managementNode.updateDisposition(eq(lockToken2), eq(DispositionStatus.COMPLETED), isNull(), isNull(), isNull()))
            .thenReturn(Mono.empty());

        // Act and Assert
        StepVerifier.create(consumer2.receive().take(2))
            .then(() -> {
                messageSink.next(message);
                messageSink.next(message2);
            })
            .expectNext(receivedMessage)
            .expectNext(receivedMessage2)
            .thenAwait(Duration.ofSeconds(5))
            .verifyComplete();

        logger.info("Verifying assertions.");
        verify(managementNode).updateDisposition(eq(lockToken1), eq(DispositionStatus.COMPLETED), isNull(), isNull(), isNull());
    }


    /**
     * Verifies that if there is no lock token, the message is not completed.
     */
    @Test
    void receivesAndAutoCompleteWithoutLockToken() {
        // Arrange
        final ReceiveMessageOptions options = new ReceiveMessageOptions(true, ReceiveMode.PEEK_LOCK,
            PREFETCH, false, null);
        final ServiceBusReceiverAsyncClient consumer2 = new ServiceBusReceiverAsyncClient(
            NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE, false, options, connectionProcessor,
            tracerProvider, messageSerializer, messageContainer);

        final MessageWithLockToken message = mock(MessageWithLockToken.class);
        final MessageWithLockToken message2 = mock(MessageWithLockToken.class);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);
        when(messageSerializer.deserialize(message2, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage2);

        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE))
            .thenReturn(Mono.just(managementNode));

        when(managementNode.updateDisposition(any(), eq(DispositionStatus.COMPLETED), isNull(), isNull(), isNull()))
            .thenReturn(Mono.delay(Duration.ofMillis(250)).then());

        // Act and Assert
        try {
            StepVerifier.create(consumer2.receive().take(2))
                .then(() -> {
                    messageSink.next(message);
                    messageSink.next(message2);
                })
                .expectNext(receivedMessage)
                .expectNext(receivedMessage2)
                .verifyComplete();
        } finally {
            consumer2.close();
        }

        verifyZeroInteractions(managementNode);
    }

    /**
     * Verifies that we error if we try to complete a message without a lock token.
     */
    @Test
    void completeNullLockToken() {
        // Arrange
        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE)).thenReturn(Mono.just(managementNode));
        when(managementNode.updateDisposition(any(), eq(DispositionStatus.COMPLETED), isNull(), isNull(), isNull()))
            .thenReturn(Mono.delay(Duration.ofMillis(250)).then());

        when(receivedMessage.getLockToken()).thenReturn(null);

        StepVerifier.create(consumer.complete(receivedMessage))
            .expectError(IllegalArgumentException.class)
            .verify();

        verify(managementNode, times(0))
            .updateDisposition(any(), eq(DispositionStatus.COMPLETED), isNull(), isNull(), isNull());
    }

    /**
     * Verifies that we error if we try to complete a null message.
     */
    @Test
    void completeNullMessage() {
        StepVerifier.create(consumer.complete(null)).expectError(NullPointerException.class).verify();
    }

    /**
     * Verifies that we error if we complete in RECEIVE_AND_DELETE mode.
     */
    @Test
    void completeInReceiveAndDeleteMode() {
        final ReceiveMessageOptions options = new ReceiveMessageOptions(false,
            ReceiveMode.RECEIVE_AND_DELETE, PREFETCH, false, null);
        ServiceBusReceiverAsyncClient client = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH,
            MessagingEntityType.QUEUE, false, options, connectionProcessor, tracerProvider,
            messageSerializer, messageContainer);

        final UUID lockToken1 = UUID.randomUUID();

        when(receivedMessage.getLockToken()).thenReturn(lockToken1);

        try {
            StepVerifier.create(client.complete(receivedMessage))
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

        when(managementNode.peekBatch(numberOfEvents))
            .thenReturn(Flux.fromArray(new ServiceBusReceivedMessage[]{receivedMessage, receivedMessage2}));

        // Act & Assert
        StepVerifier.create(consumer.peekBatch(numberOfEvents))
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

        when(managementNode.peekBatch(numberOfEvents, fromSequenceNumber))
            .thenReturn(Flux.fromArray(new ServiceBusReceivedMessage[]{receivedMessage, receivedMessage2}));

        // Act & Assert
        StepVerifier.create(consumer.peekBatch(numberOfEvents, fromSequenceNumber))
            .expectNext(receivedMessage, receivedMessage2)
            .verifyComplete();
    }

    /**
     * Verifies that we can deadletter a message with an error and description.
     */
    @Test
    void deadLetterWithDescription() {
        final UUID lockToken1 = UUID.randomUUID();
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

        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE)).thenReturn(Mono.just(managementNode));
        when(managementNode.updateDisposition(lockToken1, DispositionStatus.SUSPENDED, reason, description, propertiesToModify))
            .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(consumer.receive()
            .take(1)
            .flatMap(m -> consumer.deadLetter(m, deadLetterOptions)))
            .then(() -> messageSink.next(message))
            .expectNext()
            .verifyComplete();

        verify(managementNode).updateDisposition(lockToken1, DispositionStatus.SUSPENDED, reason, description, propertiesToModify);
    }

    /**
     * Verifies that the user can complete settlement methods on received message.
     */
    @ParameterizedTest
    @EnumSource(DispositionStatus.class)
    void settleMessage(DispositionStatus dispositionStatus) {
        // Arrange
        final UUID lockToken1 = UUID.randomUUID();
        final UUID lockToken2 = UUID.randomUUID();
        final Instant expiration = Instant.now().plus(Duration.ofMinutes(5));

        final MessageWithLockToken message = mock(MessageWithLockToken.class);
        final MessageWithLockToken message2 = mock(MessageWithLockToken.class);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);
        when(messageSerializer.deserialize(message2, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage2);

        when(receivedMessage.getLockToken()).thenReturn(lockToken1);
        when(receivedMessage.getLockedUntil()).thenReturn(expiration);
        when(receivedMessage2.getLockToken()).thenReturn(lockToken2);
        when(receivedMessage2.getLockedUntil()).thenReturn(expiration);

        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE))
            .thenReturn(Mono.just(managementNode));

        when(managementNode.updateDisposition(lockToken1, dispositionStatus, null, null, null))
            .thenReturn(Mono.empty());
        when(managementNode.updateDisposition(lockToken2, dispositionStatus, null, null, null))
            .thenReturn(Mono.empty());

        // Pretend we receive these before. This is to simulate that so that the receiver keeps track of them in
        // the lock map.
        StepVerifier.create(consumer.receive().take(2))
            .then(() -> {
                messageSink.next(message);
                messageSink.next(message2);
            })
            .expectNext(receivedMessage)
            .expectNext(receivedMessage2)
            .thenAwait(Duration.ofSeconds(5))
            .verifyComplete();

        // Act and Assert
        final Mono<Void> operation;
        switch (dispositionStatus) {
            case DEFERRED:
                operation = consumer.defer(receivedMessage);
                break;
            case ABANDONED:
                operation = consumer.abandon(receivedMessage);
                break;
            case COMPLETED:
                operation = consumer.complete(receivedMessage);
                break;
            case SUSPENDED:
                operation = consumer.deadLetter(receivedMessage);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized operation: " + dispositionStatus);
        }

        StepVerifier.create(operation)
            .verifyComplete();

        verify(managementNode).updateDisposition(lockToken1, dispositionStatus, null, null, null);
        verify(managementNode, times(0)).updateDisposition(lockToken2, dispositionStatus, null, null, null);
    }

    /**
     * Verifies that this receive deferred one messages from a sequence Number.
     */
    @Test
    void receiveDeferredWithSequenceOneMessage() {
        // Arrange
        final int fromSequenceNumber = 10;
        final ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);

        when(managementNode.receiveDeferredMessage(receiveOptions.getReceiveMode(), fromSequenceNumber)).thenReturn(Mono.just(receivedMessage));

        // Act & Assert
        StepVerifier.create(consumer.receiveDeferredMessage(fromSequenceNumber))
            .expectNext(receivedMessage)
            .verifyComplete();
    }

    /**
     * Verifies that this receive deferred messages from a sequence Number.
     */
    @Test
    void receiveDeferredBatchFromSequenceNumber() {
        // Arrange
        final int fromSequenceNumber1 = 10;
        final int fromSequenceNumber2 = 11;
        when(managementNode.receiveDeferredMessageBatch(receiveOptions.getReceiveMode(), fromSequenceNumber1, fromSequenceNumber2))
            .thenReturn(Flux.fromArray(new ServiceBusReceivedMessage[]{receivedMessage, receivedMessage2}));

        // Act & Assert
        StepVerifier.create(consumer.receiveDeferredMessageBatch(fromSequenceNumber1, fromSequenceNumber2))
            .expectNext(receivedMessage)
            .expectNext(receivedMessage2)
            .verifyComplete();
    }

    private List<Message> getMessages(int numberOfEvents) {
        final Map<String, String> map = Collections.singletonMap("SAMPLE_HEADER", "foo");

        return IntStream.range(0, numberOfEvents)
            .mapToObj(index -> getMessage(PAYLOAD_BYTES, messageTrackingUUID, map))
            .collect(Collectors.toList());
    }
}
