// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.AzureException;
import com.azure.core.test.utils.metrics.TestGauge;
import com.azure.core.test.utils.metrics.TestHistogram;
import com.azure.core.test.utils.metrics.TestMeasurement;
import com.azure.core.test.utils.metrics.TestMeter;
import com.azure.core.util.BinaryData;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.LockContainer;
import com.azure.messaging.servicebus.implementation.MessageWithLockToken;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusConstants;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.implementation.ServiceBusReactorReceiver;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.DeferOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.DeliveryState.DeliveryStateType;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.azure.messaging.servicebus.ReceiverOptions.createNamedSessionOptions;
import static com.azure.messaging.servicebus.ReceiverOptions.createNonSessionOptions;
import static com.azure.messaging.servicebus.TestUtils.getMessage;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceBusReceiverAsyncClientTest {
    private static final ClientOptions CLIENT_OPTIONS = new ClientOptions();
    private static final String PAYLOAD = "hello";
    private static final byte[] PAYLOAD_BYTES = PAYLOAD.getBytes(UTF_8);
    private static final int PREFETCH = 5;
    private static final String NAMESPACE = "my-namespace-foo.net";
    private static final String ENTITY_PATH = "queue-name";
    private static final String SUBSCRIPTION_NAME = "subscription-name";
    private static final MessagingEntityType ENTITY_TYPE = MessagingEntityType.QUEUE;
    private static final String NAMESPACE_CONNECTION_STRING = String.format(
        "Endpoint=sb://%s;SharedAccessKeyName=%s;SharedAccessKey=%s",
        NAMESPACE, "some-name", "something-else");
    private static final Duration CLEANUP_INTERVAL = Duration.ofSeconds(10);
    private static final String SESSION_ID = "my-session-id";
    private static final String CLIENT_IDENTIFIER = "my-client-identifier";
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusReceiverAsyncClientTest.class);
    private static final ServiceBusTracer NOOP_TRACER = new ServiceBusTracer(null, NAMESPACE, ENTITY_PATH);
    private final String messageTrackingUUID = UUID.randomUUID().toString();
    private final ReplayProcessor<AmqpEndpointState> endpointProcessor = ReplayProcessor.cacheLast();
    private final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private final DirectProcessor<Message> messageProcessor = DirectProcessor.create();
    private final FluxSink<Message> messageSink = messageProcessor.sink(FluxSink.OverflowStrategy.BUFFER);

    private ServiceBusConnectionProcessor connectionProcessor;
    private ServiceBusReceiverAsyncClient receiver;
    private ServiceBusReceiverAsyncClient sessionReceiver;
    private AutoCloseable mocksCloseable;

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

    private ServiceBusReceiverInstrumentation instrumentation = new ServiceBusReceiverInstrumentation(null, null, NAMESPACE, ENTITY_PATH, null, false);
    @Mock
    private ServiceBusManagementNode managementNode;
    @Mock
    private ServiceBusReceivedMessage receivedMessage;
    @Mock
    private ServiceBusReceivedMessage receivedMessage2;
    @Mock
    private Runnable onClientClose;

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
        LOGGER.info("[{}] Setting up.", testInfo.getDisplayName());

        mocksCloseable = MockitoAnnotations.openMocks(this);

        // Forcing us to publish the messages we receive on the AMQP link on single. Similar to how it is done
        // in ReactorExecutor.
        when(amqpReceiveLink.receive()).thenReturn(messageProcessor.publishOn(Schedulers.single()));
        when(amqpReceiveLink.getEndpointStates()).thenReturn(endpointProcessor);
        when(amqpReceiveLink.addCredits(anyInt())).thenReturn(Mono.empty());

        when(sessionReceiveLink.receive()).thenReturn(messageProcessor.publishOn(Schedulers.single()));
        when(sessionReceiveLink.getEndpointStates()).thenReturn(endpointProcessor);
        when(sessionReceiveLink.addCredits(anyInt())).thenReturn(Mono.empty());

        ConnectionOptions connectionOptions = new ConnectionOptions(NAMESPACE, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, ServiceBusConstants.AZURE_ACTIVE_DIRECTORY_SCOPE,
            AmqpTransportType.AMQP, new AmqpRetryOptions(), ProxyOptions.SYSTEM_DEFAULTS, Schedulers.boundedElastic(),
            CLIENT_OPTIONS, SslDomain.VerifyMode.VERIFY_PEER_NAME, "test-product", "test-version");

        when(connection.getEndpointStates()).thenReturn(endpointProcessor);
        endpointSink.next(AmqpEndpointState.ACTIVE);

        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE))
            .thenReturn(Mono.just(managementNode));

        when(connection.createReceiveLink(anyString(), anyString(), any(ServiceBusReceiveMode.class), any(),
            any(MessagingEntityType.class), anyString())).thenReturn(Mono.just(amqpReceiveLink));
        when(connection.createReceiveLink(anyString(), anyString(), any(ServiceBusReceiveMode.class), any(),
            any(MessagingEntityType.class), anyString(), anyString())).thenReturn(Mono.just(sessionReceiveLink));

        connectionProcessor =
            Flux.<ServiceBusAmqpConnection>create(sink -> sink.next(connection))
                .subscribeWith(new ServiceBusConnectionProcessor(connectionOptions.getFullyQualifiedNamespace(),
                    connectionOptions.getRetry()));

        receiver = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE,
            createNonSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, PREFETCH, null, false),
            connectionProcessor, CLEANUP_INTERVAL, instrumentation, messageSerializer, onClientClose, CLIENT_IDENTIFIER);

        sessionReceiver = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE,
            createNamedSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, PREFETCH, null, false, SESSION_ID),
            connectionProcessor, CLEANUP_INTERVAL, instrumentation, messageSerializer, onClientClose, mock(ServiceBusSessionManager.class));
    }

    @AfterEach
    void teardown(TestInfo testInfo) throws Exception {
        LOGGER.info("[{}] Tearing down.", testInfo.getDisplayName());

        receiver.close();
        mocksCloseable.close();
        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Verifies that the correct Service Bus properties are set.
     */
    @Test
    void properties() {
        Assertions.assertEquals(ENTITY_PATH, receiver.getEntityPath());
        Assertions.assertEquals(NAMESPACE, receiver.getFullyQualifiedNamespace());
        Assertions.assertEquals(CLIENT_IDENTIFIER, receiver.getIdentifier());
    }

    /**
     * Verifies that when user calls peek more than one time, It returns different object.
     */
    @SuppressWarnings("unchecked")
    @Test
    void peekTwoMessages() {
        // Arrange
        final long sequence1 = 10;
        final long sequence2 = 12;
        final ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        when(receivedMessage.getSequenceNumber()).thenReturn(sequence1);
        when(receivedMessage2.getSequenceNumber()).thenReturn(sequence2);
        when(managementNode.peek(anyLong(), isNull(), isNull()))
            .thenReturn(Mono.just(receivedMessage), Mono.just(receivedMessage2));

        // Act & Assert
        StepVerifier.create(receiver.peekMessage())
            .expectNext(receivedMessage)
            .verifyComplete();

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
     * Verifies that when no messages are returned, that it does not error.
     */
    @Test
    void peekEmptyEntity() {
        // Arrange
        when(managementNode.peek(0, null, null))
            .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(receiver.peekMessage())
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

        when(managementNode.peek(fromSequenceNumber, null, null)).thenReturn(Mono.just(receivedMessage));

        // Act & Assert
        StepVerifier.create(receiver.peekMessage(fromSequenceNumber))
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
        final List<Message> messages = getMessages();
        final String lockToken = UUID.randomUUID().toString();

        ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);
        when(receivedMessage.getLockedUntil()).thenReturn(OffsetDateTime.now());
        when(receivedMessage.getLockToken()).thenReturn(lockToken);

        when(messageSerializer.deserialize(any(Message.class), eq(ServiceBusReceivedMessage.class)))
            .thenReturn(receivedMessage);

        // Act & Assert
        StepVerifier.create(receiver.receiveMessages().take(numberOfEvents))
            .then(() -> messages.forEach(m -> messageSink.next(m)))
            .expectNextCount(numberOfEvents)
            .verifyComplete();

        // Add credit for each time 'onNext' is called, plus once when publisher is subscribed.
        verify(amqpReceiveLink, atMost(numberOfEvents + 1)).addCredits(PREFETCH);
        verify(amqpReceiveLink, never()).updateDisposition(eq(lockToken), any());
    }

    /**
     * Verifies that session receiver does not start 'FluxAutoLockRenew' for each message because a session is already
     * locked.
     */
    @Test
    void receivesMessageLockRenewSessionOnly() {
        // Arrange
        final int numberOfEvents = 1;
        final List<Message> messages = getMessages();
        final String lockToken = UUID.randomUUID().toString();
        final Duration maxLockRenewDuration = Duration.ofMinutes(1);
        final ServiceBusSessionManager sessionManager = mock(ServiceBusSessionManager.class);
        ServiceBusReceiverAsyncClient mySessionReceiver = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE,
            createNamedSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, PREFETCH, maxLockRenewDuration,
                false, SESSION_ID), connectionProcessor,
            CLEANUP_INTERVAL, instrumentation, messageSerializer, onClientClose, sessionManager);

        // This needs to be used with "try with resource" : https://javadoc.io/static/org.mockito/mockito-core/3.9.0/org/mockito/Mockito.html#static_mocks
        try (
            MockedConstruction<FluxAutoLockRenew> mockedAutoLockRenew = Mockito.mockConstructionWithAnswer(FluxAutoLockRenew.class,
                invocationOnMock -> new FluxAutoLockRenew(Flux.empty(),
                    createNonSessionOptions(ServiceBusReceiveMode.RECEIVE_AND_DELETE, 1, Duration.ofSeconds(30),
                        true),
                    new LockContainer<>(Duration.ofSeconds(30)), i -> Mono.empty(), NOOP_TRACER))) {

            ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);
            when(receivedMessage.getLockedUntil()).thenReturn(OffsetDateTime.now());
            when(receivedMessage.getLockToken()).thenReturn(lockToken);

            final List<ServiceBusReceivedMessage> sessionMessages = new ArrayList<>();
            sessionMessages.add(receivedMessage);
            when(sessionManager.receive()).thenReturn(Flux.fromIterable(sessionMessages).map(ServiceBusMessageContext::new));

            // Act & Assert
            StepVerifier.create(mySessionReceiver.receiveMessages().take(numberOfEvents))
                .then(() -> messages.forEach(messageSink::next))
                .expectNextCount(numberOfEvents)
                .verifyComplete();

            // Message onNext should not trigger `FluxAutoLockRenew` for each message because this is session entity.
            Assertions.assertEquals(0, mockedAutoLockRenew.constructed().size());
        }
    }

    public static Stream<DispositionStatus> settleWithNullTransactionId() {
        return Stream.of(DispositionStatus.DEFERRED, DispositionStatus.ABANDONED, DispositionStatus.COMPLETED,
            DispositionStatus.SUSPENDED);
    }

    /**
     * Verifies that we error if we try to settle a message with null transaction-id.
     *
     * Transactions are not used in {@link ServiceBusReceiverAsyncClient#release(ServiceBusReceivedMessage)} since this
     * is package-private, so we skip this case.
     */
    @ParameterizedTest
    @MethodSource
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
                operation = receiver.defer(receivedMessage, new DeferOptions().setTransactionContext(nullTransactionId));
                break;
            case ABANDONED:
                operation = receiver.abandon(receivedMessage, new AbandonOptions().setTransactionContext(nullTransactionId));
                break;
            case COMPLETED:
                operation = receiver.complete(receivedMessage, new CompleteOptions().setTransactionContext(nullTransactionId));
                break;
            case SUSPENDED:
                operation = receiver.deadLetter(receivedMessage, new DeadLetterOptions().setTransactionContext(nullTransactionId));
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
        final ReceiverOptions options = createNonSessionOptions(ServiceBusReceiveMode.RECEIVE_AND_DELETE, PREFETCH, null, false);
        ServiceBusReceiverAsyncClient client = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH,
            MessagingEntityType.QUEUE, options, connectionProcessor, CLEANUP_INTERVAL, instrumentation,
            messageSerializer, onClientClose, CLIENT_IDENTIFIER);

        final String lockToken1 = UUID.randomUUID().toString();

        when(receivedMessage.getLockToken()).thenReturn(lockToken1);

        try {
            StepVerifier.create(client.complete(receivedMessage))
                .expectError(UnsupportedOperationException.class)
                .verify();
        } finally {
            client.close();
        }
    }

    @Test
    void throwsExceptionAboutSettlingPeekedMessagesWithNullLockToken() {
        final ReceiverOptions options = createNonSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, PREFETCH, null, false);
        ServiceBusReceiverAsyncClient client = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH,
            MessagingEntityType.QUEUE, options, connectionProcessor, CLEANUP_INTERVAL, instrumentation,
            messageSerializer, onClientClose, CLIENT_IDENTIFIER);

        when(receivedMessage.getLockToken()).thenReturn(null);

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
    void peekMessages() {
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
     * Verifies that this peek batch of messages.
     */
    @Test
    void peekMessagesEmptyEntity() {
        // Arrange
        final int numberOfEvents = 2;

        when(managementNode.peek(0, null, null, numberOfEvents))
            .thenReturn(Flux.fromIterable(Collections.emptyList()));

        // Act & Assert
        StepVerifier.create(receiver.peekMessages(numberOfEvents))
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
        StepVerifier.create(receiver.peekMessages(numberOfEvents, fromSequenceNumber))
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

        final OffsetDateTime expiration = OffsetDateTime.now().plus(Duration.ofMinutes(5));

        final MessageWithLockToken message = mock(MessageWithLockToken.class);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);

        when(receivedMessage.getLockToken()).thenReturn(lockToken1);
        when(receivedMessage.getLockedUntil()).thenReturn(expiration);

        when(amqpReceiveLink.updateDisposition(eq(lockToken1), argThat(e -> e.getType() == DeliveryStateType.Rejected))).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(receiver.receiveMessages()
            .take(1)
            .flatMap(receivedMessage -> receiver.deadLetter(receivedMessage, deadLetterOptions)))
            .then(() -> messageSink.next(message))
            .expectNext()
            .verifyComplete();

        verify(amqpReceiveLink).updateDisposition(eq(lockToken1), isA(Rejected.class));
    }

    /**
     * Verifies that error source is populated when any error happened while renewing lock.
     */
    @Test
    void errorSourceOnRenewMessageLock() {
        // Arrange
        final Duration maxDuration = Duration.ofSeconds(8);
        final String lockToken = "some-token";

        when(receivedMessage.getLockToken()).thenReturn(lockToken);
        when(receivedMessage.getContext()).thenReturn(Context.NONE);
        when(managementNode.renewMessageLock(lockToken, null))
            .thenReturn(Mono.error(new AzureException("some error occurred.")));

        final ReceiverOptions receiverOptions = createNonSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, PREFETCH, null, true);
        final ServiceBusReceiverAsyncClient receiver2 = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH,
            MessagingEntityType.QUEUE, receiverOptions, connectionProcessor, CLEANUP_INTERVAL, instrumentation,
            messageSerializer, onClientClose, CLIENT_IDENTIFIER);

        // Act & Assert
        StepVerifier.create(receiver2.renewMessageLock(receivedMessage, maxDuration))
            .verifyErrorSatisfies(throwable -> {
                Assertions.assertTrue(throwable instanceof ServiceBusException);
                final ServiceBusErrorSource actual = ((ServiceBusException) throwable).getErrorSource();
                Assertions.assertEquals(ServiceBusErrorSource.RENEW_LOCK, actual);
            });

        verify(managementNode, times(1)).renewMessageLock(lockToken, null);
    }

    /**
     * Verifies that error source is populated when any error happened while renewing lock.
     */
    @Test
    void errorSourceOnSessionLock() {
        // Arrange
        when(managementNode.renewSessionLock(SESSION_ID, null)).thenReturn(Mono.error(new AzureException("some error occurred.")));

        final ReceiverOptions receiverOptions = createNamedSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, PREFETCH, null, true, SESSION_ID);
        final ServiceBusReceiverAsyncClient sessionReceiver2 = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE,
            receiverOptions, connectionProcessor, CLEANUP_INTERVAL, instrumentation, messageSerializer, onClientClose, mock(ServiceBusSessionManager.class));

        // Act & Assert
        StepVerifier.create(sessionReceiver2.renewSessionLock())
            .verifyErrorSatisfies(throwable -> {
                Assertions.assertTrue(throwable instanceof ServiceBusException);
                final ServiceBusErrorSource actual = ((ServiceBusException) throwable).getErrorSource();
                Assertions.assertEquals(ServiceBusErrorSource.RENEW_LOCK, actual);
            });
    }

    /**
     * Verifies that error source is populated when there is no autoComplete.
     */
    @ParameterizedTest
    @MethodSource
    void errorSourceNoneOnSettlement(DispositionStatus dispositionStatus, DeliveryStateType expectedDeliveryState,
        ServiceBusErrorSource errorSource) {

        final UUID lockTokenUuid = UUID.randomUUID();
        final String lockToken1 = lockTokenUuid.toString();

        final MessageWithLockToken message = mock(MessageWithLockToken.class);

        when(receivedMessage.getLockToken()).thenReturn(lockToken1);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);

        when(amqpReceiveLink.updateDisposition(eq(lockToken1), argThat(e -> e.getType() == expectedDeliveryState)))
            .thenReturn(Mono.error(new AzureException("some error occurred.")));

        // Act & Assert
        StepVerifier.create(receiver.receiveMessages().take(1)
            .flatMap(receivedMessage -> {
                final Mono<Void> operation;
                switch (dispositionStatus) {
                    case ABANDONED:
                        operation = receiver.abandon(receivedMessage);
                        break;
                    case COMPLETED:
                        operation = receiver.complete(receivedMessage);
                        break;
                    default:
                        throw new IllegalArgumentException("Unrecognized operation: " + dispositionStatus);
                }
                return operation;
            })
            )
            .then(() -> messageSink.next(message))
            .expectNext()
            .verifyErrorSatisfies(throwable -> {
                Assertions.assertTrue(throwable instanceof ServiceBusException);
                final ServiceBusErrorSource actual = ((ServiceBusException) throwable).getErrorSource();
                Assertions.assertEquals(errorSource, actual);
            });

        verify(amqpReceiveLink).updateDisposition(eq(lockToken1), any(DeliveryState.class));
    }

    /**
     * Ensure that we throw right error source when there is any issue during autocomplete. Error source should be
     * {@link ServiceBusErrorSource#COMPLETE}
     */
    @Test
    void errorSourceAutoCompleteMessage() {
        // Arrange
        final int numberOfEvents = 2;
        final int messagesToReceive = 1;
        final List<Message> messages = getMessages();
        final String lockToken = UUID.randomUUID().toString();
        final ReceiverOptions receiverOptions = createNonSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, PREFETCH, null, true);
        final ServiceBusReceiverAsyncClient receiver2 = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH,
            MessagingEntityType.QUEUE, receiverOptions, connectionProcessor, CLEANUP_INTERVAL, instrumentation,
            messageSerializer, onClientClose, CLIENT_IDENTIFIER);

        when(receivedMessage.getLockToken()).thenReturn(lockToken);
        when(messageSerializer.deserialize(any(Message.class), eq(ServiceBusReceivedMessage.class)))
            .thenReturn(receivedMessage);

        when(amqpReceiveLink.updateDisposition(lockToken, Accepted.getInstance())).thenReturn(Mono.error(new AmqpException(false, AmqpErrorCondition.MESSAGE_LOCK_LOST, "some error occurred.", null)));

        try {
            // Act & Assert
            StepVerifier.create(receiver2.receiveMessages().take(numberOfEvents))
                .then(() -> messages.forEach(m -> messageSink.next(m)))
                .expectNextCount(messagesToReceive)
                .verifyErrorSatisfies(throwable -> {
                    Assertions.assertTrue(throwable instanceof ServiceBusException);

                    ServiceBusException serviceBusException = (ServiceBusException) throwable;
                    final ServiceBusErrorSource actual = serviceBusException.getErrorSource();

                    Assertions.assertEquals(ServiceBusErrorSource.COMPLETE, actual);
                    Assertions.assertEquals(ServiceBusFailureReason.MESSAGE_LOCK_LOST, serviceBusException.getReason());
                });
        } finally {
            receiver2.close();
        }

        verify(amqpReceiveLink, atLeast(messagesToReceive)).updateDisposition(lockToken, Accepted.getInstance());
    }

    /**
     * Verifies that error source is populated when there is any error during receiving of message.
     */
    @Test
    void errorSourceOnReceiveMessage() {
        final String lockToken = UUID.randomUUID().toString();

        final OffsetDateTime expiration = OffsetDateTime.now().plus(Duration.ofMinutes(5));

        final MessageWithLockToken message = mock(MessageWithLockToken.class);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);

        when(receivedMessage.getLockToken()).thenReturn(lockToken);
        when(receivedMessage.getLockedUntil()).thenReturn(expiration);

        final ReceiverOptions receiverOptions = createNonSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, PREFETCH, null, true);
        final ServiceBusReceiverAsyncClient receiver2 = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH,
            MessagingEntityType.QUEUE, receiverOptions, connectionProcessor, CLEANUP_INTERVAL, instrumentation,
            messageSerializer, onClientClose, CLIENT_IDENTIFIER);

        when(connection.createReceiveLink(anyString(), anyString(), any(ServiceBusReceiveMode.class), any(),
            any(MessagingEntityType.class), anyString())).thenReturn(Mono.error(new AzureException("some receive link Error.")));

        // Act & Assert
        StepVerifier.create(receiver2.receiveMessages().take(1))
            .verifyErrorSatisfies(throwable -> {
                Assertions.assertTrue(throwable instanceof ServiceBusException);
                final ServiceBusErrorSource actual = ((ServiceBusException) throwable).getErrorSource();
                Assertions.assertEquals(ServiceBusErrorSource.RECEIVE, actual);
            });

        verify(amqpReceiveLink, never()).updateDisposition(eq(lockToken), any(DeliveryState.class));
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
        final OffsetDateTime expiration = OffsetDateTime.now().plus(Duration.ofMinutes(5));
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

        when(managementNode.receiveDeferredMessages(eq(ServiceBusReceiveMode.PEEK_LOCK), isNull(), isNull(), argThat(arg -> {
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
                operation = receiver.defer(receivedMessage);
                break;
            case ABANDONED:
                operation = receiver.abandon(receivedMessage);
                break;
            case COMPLETED:
                operation = receiver.complete(receivedMessage);
                break;
            case SUSPENDED:
                operation = receiver.deadLetter(receivedMessage);
                break;
            case RELEASED:
                operation = receiver.release(receivedMessage);
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
     * Verifies that managementNodeLocks was closed.
     */
    @Test
    void callsManagementNodeLocksCloseWhenClientIsClosed() {
        // Given
        Assertions.assertFalse(receiver.isManagementNodeLocksClosed());

        // Act
        receiver.close();

        // Assert
        Assertions.assertTrue(receiver.isManagementNodeLocksClosed());
    }

    /**
     * Verifies that renewalContainer was closed.
     */
    @Test
    void callsRenewalContainerCloseWhenClientIsClosed() {
        // Given
        Assertions.assertFalse(receiver.isRenewalContainerClosed());

        // Act
        receiver.close();

        // Assert
        Assertions.assertTrue(receiver.isRenewalContainerClosed());
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
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK);


        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.prefetchCount(-1));
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
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .buildAsyncClient();

        // Act
        final String actual = receiver.getEntityPath();
        final String actualNamespace = receiver.getFullyQualifiedNamespace();

        // Assert
        Assertions.assertEquals(entityPath, actual);
        Assertions.assertEquals(NAMESPACE, actualNamespace);
    }

    /**
     * Verifies that client can call multiple receiveMessages on same receiver instance.
     */
    @Test
    void canPerformMultipleReceive() {
        // Arrange
        final int numberOfEvents = 1;
        final List<Message> messages = getMessages();

        ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);
        when(receivedMessage.getLockedUntil()).thenReturn(OffsetDateTime.now());
        when(receivedMessage.getLockToken()).thenReturn(UUID.randomUUID().toString());
        when(messageSerializer.deserialize(any(Message.class), eq(ServiceBusReceivedMessage.class)))
            .thenReturn(receivedMessage);

        // Act & Assert

        StepVerifier.create(receiver.receiveMessages().take(numberOfEvents))
            .then(() -> messages.forEach(m -> messageSink.next(m)))
            .expectNextCount(numberOfEvents)
            .verifyComplete();

        // TODO: Yijun and Srikanta are thinking of using two links for two subscribers.
        //  We may not want to support multiple subscribers by using publish and autoConnect.
        //  After the autoConnect was removed from ServiceBusAsyncConsumer.processor, the receiver doesn't support
        //  multiple calls of receiver.receiveMessages().
        //  For more discussions.
        //  StepVerifier.create(receiver.receiveMessages().take(numberOfEvents))
        //      .then(() -> messages.forEach(m -> messageSink.next(m)))
        //      .expectNextCount(numberOfEvents)
        //      .verifyComplete();

        // Add credit for each time 'onNext' is called, plus once when publisher is subscribed.
        verify(amqpReceiveLink, atMost(numberOfEvents + 1)).addCredits(PREFETCH);
    }

    /**
     * Cannot get session state for non-session receiver.
     */
    @Test
    void cannotPerformGetSessionState() {
        StepVerifier.create(receiver.getSessionState())
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
        StepVerifier.create(receiver.setSessionState(sessionState))
            .expectError(IllegalStateException.class)
            .verify();
    }

    /**
     * Cannot get session state for non-session receiver.
     */
    @Test
    void cannotPerformRenewSessionLock() {
        // Act & Assert
        StepVerifier.create(receiver.renewSessionLock())
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
        ServiceBusReceiverAsyncClient mySessionReceiver = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE,
            createNamedSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, PREFETCH, CLEANUP_INTERVAL,
                false, SESSION_ID), connectionProcessor,
            CLEANUP_INTERVAL, instrumentation, messageSerializer, onClientClose, mock(ServiceBusSessionManager.class));

        when(managementNode.getSessionState(SESSION_ID, null))
            .thenReturn(Mono.just(bytes));

        // Act & Assert
        StepVerifier.create(mySessionReceiver.getSessionState())
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
        StepVerifier.create(sessionReceiver.setSessionState(bytes))
            .expectComplete()
            .verify();
    }

    /**
     * Get null session id for non-session receiver and gets valid session id for a session receiver.
     */
    @Test
    void getSessionIdTests() {
        // Act & Assert
        Assertions.assertNull(receiver.getSessionId(), "Non-null session Id for a non-session receiver");
        Assertions.assertEquals(SESSION_ID, sessionReceiver.getSessionId());
    }

    /**
     * Verifies that we can renew a session state.
     */
    @Test
    void renewSessionLock() {
        // Arrange
        final OffsetDateTime expiry = Instant.ofEpochSecond(1588011761L).atOffset(ZoneOffset.UTC);

        when(managementNode.renewSessionLock(SESSION_ID, null)).thenReturn(Mono.just(expiry));

        // Act & Assert
        StepVerifier.create(sessionReceiver.renewSessionLock())
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
        when(receivedMessage.getLockToken()).thenReturn("lock-token");
        when(receivedMessage.getSessionId()).thenReturn("fo");

        // Act & Assert
        StepVerifier.create(sessionReceiver.renewMessageLock(receivedMessage))
            .expectError(IllegalStateException.class)
            .verify();
    }

    /**
     * Verifies that we can auto-renew a message lock.
     */
    @Test
    void autoRenewMessageLock() {
        // Arrange
        final Duration maxDuration = Duration.ofSeconds(8);
        final Duration renewalPeriod = Duration.ofSeconds(3);

        final UUID lockTokenUUID = UUID.randomUUID();
        final String lockToken = lockTokenUUID.toString();
        final ServiceBusReceivedMessage message = new ServiceBusReceivedMessage(BinaryData.fromString("foo"));
        message.setLockToken(lockTokenUUID);

        // At most 4 times because we renew the lock before it expires (by some seconds).
        final int atMost = 6;
        final Duration totalSleepPeriod = maxDuration.plusMillis(500);

        when(managementNode.renewMessageLock(lockToken, null))
            .thenReturn(Mono.fromCallable(() -> OffsetDateTime.now().plus(renewalPeriod)));

        // Act & Assert
        StepVerifier.create(receiver.renewMessageLock(message, maxDuration))
            .thenAwait(totalSleepPeriod)
            .then(() -> LOGGER.info("Finished renewals for first sleep."))
            .expectComplete()
            .verify(Duration.ofSeconds(5));

        verify(managementNode, Mockito.atMost(atMost)).renewMessageLock(lockToken, null);
    }

    /**
     * Verifies that it errors when we try a null lock token.
     */
    @Test
    void autoRenewMessageLockErrorNull() {
        // Arrange
        final Duration maxDuration = Duration.ofSeconds(8);
        final Duration renewalPeriod = Duration.ofSeconds(3);

        when(receivedMessage.getLockToken()).thenReturn(null);
        when(managementNode.renewMessageLock(anyString(), isNull()))
            .thenReturn(Mono.fromCallable(() -> OffsetDateTime.now().plus(renewalPeriod)));

        // Act & Assert
        StepVerifier.create(receiver.renewMessageLock(receivedMessage, maxDuration))
            .expectError(NullPointerException.class)
            .verify();

        verify(managementNode, never()).renewMessageLock(anyString(), isNull());
    }

    /**
     * Verifies that it errors when we try an empty string lock token.
     */
    @Test
    void autoRenewMessageLockErrorEmptyString() {
        // Arrange
        final Duration maxDuration = Duration.ofSeconds(8);
        final Duration renewalPeriod = Duration.ofSeconds(3);
        final String lockToken = "";

        when(receivedMessage.getLockToken()).thenReturn("");
        when(managementNode.renewMessageLock(anyString(), isNull()))
            .thenReturn(Mono.fromCallable(() -> OffsetDateTime.now().plus(renewalPeriod)));

        // Act & Assert
        StepVerifier.create(receiver.renewMessageLock(receivedMessage, maxDuration))
            .expectError(IllegalArgumentException.class)
            .verify();

        verify(managementNode, never()).renewMessageLock(anyString(), isNull());
    }

    /**
     * Verifies that we can auto-renew a session lock.
     */
    @Test
    void autoRenewSessionLock() {
        // Arrange
        final Duration maxDuration = Duration.ofSeconds(8);
        final Duration renewalPeriod = Duration.ofSeconds(3);
        final String sessionId = "some-token";

        // At most 4 times because we renew the lock before it expires (by some seconds).
        final int atMost = 6;
        final Duration totalSleepPeriod = maxDuration.plusMillis(500);

        when(managementNode.renewSessionLock(SESSION_ID, null))
            .thenReturn(Mono.fromCallable(() -> OffsetDateTime.now().plus(renewalPeriod)));

        // Act & Assert
        StepVerifier.create(sessionReceiver.renewSessionLock(maxDuration))
            .thenAwait(totalSleepPeriod)
            .then(() -> LOGGER.info("Finished renewals for first sleep."))
            .expectComplete()
            .verify(Duration.ofSeconds(5));

        verify(managementNode, Mockito.atMost(atMost)).renewSessionLock(sessionId, null);
    }

    /**
     * Verifies that it errors if session renewal is tried on session unaware receiver.
     */
    @Test
    void cannotRenewSessionLockForNonSessionReceiver() {
        StepVerifier.create(receiver.renewSessionLock())
            .expectError(IllegalStateException.class)
            .verify();
    }

    @Test
    void autoCompleteMessage() {
        // Arrange
        final int numberOfEvents = 3;
        final List<Message> messages = getMessages();
        final String lockToken = UUID.randomUUID().toString();
        final ReceiverOptions receiverOptions = createNonSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, PREFETCH, null, true);
        final ServiceBusReceiverAsyncClient receiver2 = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH,
            MessagingEntityType.QUEUE, receiverOptions, connectionProcessor, CLEANUP_INTERVAL, instrumentation,
            messageSerializer, onClientClose, CLIENT_IDENTIFIER);

        when(receivedMessage.getLockToken()).thenReturn(lockToken);
        when(messageSerializer.deserialize(any(Message.class), eq(ServiceBusReceivedMessage.class)))
            .thenReturn(receivedMessage);

        when(amqpReceiveLink.updateDisposition(lockToken, Accepted.getInstance())).thenReturn(Mono.empty());

        try {
            // Act & Assert
            StepVerifier.create(receiver2.receiveMessages().take(numberOfEvents))
                .then(() -> messages.forEach(m -> messageSink.next(m)))
                .expectNextCount(numberOfEvents)
                .verifyComplete();
        } finally {
            receiver2.close();
        }

        verify(amqpReceiveLink, times(numberOfEvents)).updateDisposition(lockToken, Accepted.getInstance());
    }

    @Test
    void autoCompleteMessageSessionReceiver() {
        // Arrange
        final int numberOfEvents = 3;
        final List<Message> messages = getMessages();
        final String lockToken = "token1";
        final String lockToken2 = "token2";
        final String lockToken3 = "token3";

        final ServiceBusSessionManager sessionManager = mock(ServiceBusSessionManager.class);
        final ReceiverOptions receiverOptions = createNamedSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, PREFETCH, null,
            true, SESSION_ID);
        final ServiceBusReceiverAsyncClient sessionReceiver2 = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH,
            MessagingEntityType.QUEUE, receiverOptions, connectionProcessor, CLEANUP_INTERVAL, instrumentation,
            messageSerializer, onClientClose, sessionManager);

        final ServiceBusReceivedMessage receivedMessage3 = mock(ServiceBusReceivedMessage.class);

        when(receivedMessage.getLockToken()).thenReturn(lockToken);
        when(receivedMessage2.getLockToken()).thenReturn(lockToken2);
        when(receivedMessage3.getLockToken()).thenReturn(lockToken3);
        final List<ServiceBusReceivedMessage> sessionMessages = new ArrayList<>();
        sessionMessages.add(receivedMessage);
        sessionMessages.add(receivedMessage2);
        sessionMessages.add(receivedMessage3);
        when(sessionManager.receive()).thenReturn(Flux.fromIterable(sessionMessages).map(ServiceBusMessageContext::new));

        when(sessionManager.updateDisposition(eq(lockToken), any(), eq(DispositionStatus.COMPLETED), any(), any(), any(), any())).thenReturn(Mono.empty());
        when(sessionManager.updateDisposition(eq(lockToken2), any(), eq(DispositionStatus.COMPLETED), any(), any(), any(), any())).thenReturn(Mono.empty());
        when(sessionManager.updateDisposition(eq(lockToken3), any(), eq(DispositionStatus.COMPLETED), any(), any(), any(), any())).thenReturn(Mono.empty());

        try {
            // Act & Assert
            StepVerifier.create(sessionReceiver2.receiveMessages().take(numberOfEvents))
                .then(() -> messages.forEach(m -> messageSink.next(m)))
                .expectNextCount(numberOfEvents)
                .verifyComplete();
        } finally {
            sessionReceiver2.close();
        }

        verify(sessionManager).updateDisposition(eq(lockToken), any(), any(), any(), any(), any(), any());
        verify(sessionManager).updateDisposition(eq(lockToken2), any(), any(), any(), any(), any(), any());
        verify(sessionManager).updateDisposition(eq(lockToken3), any(), any(), any(), any(), any(), any());
    }

    @Test
    void receiveMessagesReportsMetricsAsyncInstr() {
        // Arrange
        final List<Message> messages = getMessages();
        TestMeter meter = new TestMeter();
        ServiceBusReceiverInstrumentation instrumentation = new ServiceBusReceiverInstrumentation(null, meter, NAMESPACE, ENTITY_PATH,
            SUBSCRIPTION_NAME, false);
        receiver = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE,
            createNonSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, PREFETCH, null, false),
            connectionProcessor, CLEANUP_INTERVAL, instrumentation, messageSerializer, onClientClose, CLIENT_IDENTIFIER);

        // Arrange
        ServiceBusReceivedMessage receivedMessage1 = mockReceivedMessage(Instant.now().minusSeconds(1000));
        ServiceBusReceivedMessage receivedMessage2 = mockReceivedMessage(Instant.now().minusSeconds(500));
        when(messageSerializer.deserialize(any(Message.class), eq(ServiceBusReceivedMessage.class)))
            .thenReturn(receivedMessage1, receivedMessage2);

        // Act & Assert
        StepVerifier.create(receiver.receiveMessages().take(2))
            .then(() -> messages.forEach(m -> messageSink.next(m)))
            .expectNextCount(2)
            .verifyComplete();

        TestHistogram receiverLag = meter.getHistograms().get("messaging.servicebus.receiver.lag");
        assertNotNull(receiverLag);
        assertEquals(2, receiverLag.getMeasurements().size());

        TestMeasurement<Double> measurement1 = receiverLag.getMeasurements().get(0);
        TestMeasurement<Double> measurement2 = receiverLag.getMeasurements().get(1);
        assertEquals(1000d, measurement1.getValue(), 10d);
        assertEquals(500d, measurement2.getValue(), 10d);

        Map<String, Object> attributes1 = measurement1.getAttributes();
        Map<String, Object> attributes2 = measurement2.getAttributes();
        assertEquals(3, attributes1.size());
        assertCommonMetricAttributes(attributes1, SUBSCRIPTION_NAME);
        assertEquals(3, attributes2.size());
        assertCommonMetricAttributes(attributes2, SUBSCRIPTION_NAME);
    }

    @ParameterizedTest()
    @EnumSource(DispositionStatus.class)
    void settlementMessagesReportsMetrics(DispositionStatus status) {
        // Arrange
        final List<Message> messages = getMessages();
        TestMeter meter = new TestMeter();
        ServiceBusReceiverInstrumentation instrumentation = new ServiceBusReceiverInstrumentation(null, meter, NAMESPACE, ENTITY_PATH,
            SUBSCRIPTION_NAME, false);
        receiver = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE,
            createNonSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, PREFETCH, null, false),
            connectionProcessor, CLEANUP_INTERVAL, instrumentation, messageSerializer, onClientClose, CLIENT_IDENTIFIER);
        when(receivedMessage.getLockToken()).thenReturn("mylockToken");
        when(receivedMessage.getSequenceNumber()).thenReturn(42L);
        when(receivedMessage.getContext()).thenReturn(Context.NONE);
        when(managementNode.updateDisposition(any(), any(), isNull(), isNull(), isNull(),
            isNull(), isNull(), isNull())).thenReturn(Mono.empty());

        // Act & Assert
        Mono<Void> settle;
        switch (status) {
            case COMPLETED: {
                settle = receiver.complete(receivedMessage);
                break;
            }
            case ABANDONED: {
                settle = receiver.abandon(receivedMessage);
                break;
            }
            case DEFERRED: {
                settle = receiver.defer(receivedMessage);
                break;
            }
            case SUSPENDED: {
                settle = receiver.deadLetter(receivedMessage);
                break;
            }
            default: {
                return;
            }
        }

        StepVerifier.create(settle).verifyComplete();

        TestHistogram settlementDuration = meter.getHistograms().get("messaging.servicebus.settlement.request.duration");
        assertNotNull(settlementDuration);
        assertEquals(1, settlementDuration.getMeasurements().size());

        TestMeasurement<Double> measurement = settlementDuration.getMeasurements().get(0);
        assertEquals(5000d, measurement.getValue(), 5000d);

        Map<String, Object> attributes = measurement.getAttributes();
        assertEquals(5, attributes.size());
        assertCommonMetricAttributes(attributes, SUBSCRIPTION_NAME);
        assertEquals(status.getValue(), attributes.get("dispositionStatus"));
        assertEquals("ok", attributes.get("status"));

        TestGauge settlementSeqNo = meter.getGauges().get("messaging.servicebus.settlement.sequence_number");

        // one for each disposition status
        assertEquals(10, settlementSeqNo.getSubscriptions().size());

        settlementSeqNo.getSubscriptions().forEach(s -> s.measure());

        boolean measurementFound = false;
        for (TestGauge.Subscription subs : settlementSeqNo.getSubscriptions()) {
            assertEquals(1, subs.getMeasurements().size());
            TestMeasurement<Long> seqNoMeasurement = subs.getMeasurements().get(0);

            if (seqNoMeasurement.getAttributes().get("dispositionStatus").equals(status.getValue())
                && seqNoMeasurement.getAttributes().get("status").equals("ok")) {
                measurementFound = true;
                assertEquals(42, seqNoMeasurement.getValue());
                assertEquals(5, seqNoMeasurement.getAttributes().size());
                assertCommonMetricAttributes(seqNoMeasurement.getAttributes(), SUBSCRIPTION_NAME);
            } else {
                assertEquals(0, seqNoMeasurement.getValue());
                assertEquals(5, seqNoMeasurement.getAttributes().size());
                assertCommonMetricAttributes(seqNoMeasurement.getAttributes(), SUBSCRIPTION_NAME);
            }
        }

        assertTrue(measurementFound);
    }

    @Test
    void receiveWithTracesAndMetrics() {
        // Arrange
        final List<Message> messages = getMessages();
        TestMeter meter = new TestMeter();
        Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);
        ServiceBusReceiverInstrumentation instrumentation = new ServiceBusReceiverInstrumentation(tracer, meter, NAMESPACE, ENTITY_PATH,
            SUBSCRIPTION_NAME, false);
        receiver = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE,
            createNonSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, PREFETCH, null, false),
            connectionProcessor, CLEANUP_INTERVAL, instrumentation, messageSerializer, onClientClose, CLIENT_IDENTIFIER);

        // Arrange
        Context spanReceive1 = new Context("marker1", true);
        Context spanReceive2 = new Context("marker2", true);
        Context spanSettle = new Context("marker3", true);
        when(tracer.start(eq("ServiceBus.process"), any(StartSpanOptions.class), any(Context.class))).thenReturn(spanReceive1, spanReceive2);
        when(tracer.start(eq("ServiceBus.complete"), any(StartSpanOptions.class), any(Context.class))).thenReturn(spanSettle);

        when(receivedMessage.getLockToken()).thenReturn("mylockToken");
        when(receivedMessage.getSequenceNumber()).thenReturn(42L);
        when(receivedMessage.getContext()).thenReturn(Context.NONE);

        when(receivedMessage2.getLockToken()).thenReturn("mylockToken");
        when(receivedMessage2.getSequenceNumber()).thenReturn(43L);
        when(receivedMessage2.getContext()).thenReturn(Context.NONE);

        when(messageSerializer.deserialize(any(Message.class), eq(ServiceBusReceivedMessage.class)))
            .thenReturn(receivedMessage, receivedMessage2);
        when(managementNode.updateDisposition(any(), any(), isNull(), isNull(), isNull(),
            isNull(), isNull(), isNull())).thenReturn(Mono.empty());
        when(amqpReceiveLink.updateDisposition(any(), any())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(receiver.receiveMessages().take(2).flatMap(msg -> receiver.complete(msg)))
            .then(() -> messages.forEach(m -> messageSink.next(m)))
            .verifyComplete();

        TestHistogram receiverLag = meter.getHistograms().get("messaging.servicebus.receiver.lag");
        assertEquals(2, receiverLag.getMeasurements().size());

        TestMeasurement<Double> measurement1 = receiverLag.getMeasurements().get(0);
        TestMeasurement<Double> measurement2 = receiverLag.getMeasurements().get(1);
        assertEquals(spanReceive1, measurement1.getContext());
        assertEquals(spanReceive2, measurement2.getContext());

        TestHistogram settlementDuration = meter.getHistograms().get("messaging.servicebus.settlement.request.duration");
        assertEquals(spanSettle, settlementDuration.getMeasurements().get(0).getContext());
        TestGauge settlementSeqNo = meter.getGauges().get("messaging.servicebus.settlement.sequence_number");
        TestGauge.Subscription subs = settlementSeqNo.getSubscriptions().get(0);
        subs.measure();
        assertSame(Context.NONE, subs.getMeasurements().get(0).getContext());
    }

    @Test
    void receiveMessageNegativeLagReportsMetricsAsyncInstr() {
        // Arrange
        final List<Message> messages = getMessages();
        TestMeter meter = new TestMeter();
        ServiceBusReceiverInstrumentation instrumentation = new ServiceBusReceiverInstrumentation(null, meter, NAMESPACE, ENTITY_PATH,
            null, false);
        receiver = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE,
            createNonSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, PREFETCH, null, false),
            connectionProcessor, CLEANUP_INTERVAL, instrumentation, messageSerializer, onClientClose, CLIENT_IDENTIFIER);

        // Arrange
        ServiceBusReceivedMessage receivedMessage1 = mockReceivedMessage(Instant.now().plusSeconds(1000));
        when(messageSerializer.deserialize(any(Message.class), eq(ServiceBusReceivedMessage.class)))
            .thenReturn(receivedMessage1);

        // Act & Assert
        StepVerifier.create(receiver.receiveMessages().take(1))
            .then(() -> messages.forEach(m -> messageSink.next(m)))
            .expectNextCount(1)
            .verifyComplete();

        TestHistogram receiverLag = meter.getHistograms().get("messaging.servicebus.receiver.lag");
        assertEquals(1, receiverLag.getMeasurements().size());

        TestMeasurement<Double> measurement = receiverLag.getMeasurements().get(0);
        assertEquals(0d, measurement.getValue(), 1d);
        Map<String, Object> attributes = measurement.getAttributes();
        assertEquals(2, attributes.size());
        assertCommonMetricAttributes(attributes, null);
    }

    @Test
    void receiveMessageNegativeLagReportsMetricsSyncInstr() {
        // Arrange
        final List<Message> messages = getMessages();
        TestMeter meter = new TestMeter();
        ServiceBusReceiverInstrumentation instrumentation = new ServiceBusReceiverInstrumentation(null, meter, NAMESPACE, ENTITY_PATH,
            null, true);
        receiver = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE,
            createNonSessionOptions(ServiceBusReceiveMode.PEEK_LOCK, PREFETCH, null, false),
            connectionProcessor, CLEANUP_INTERVAL, instrumentation, messageSerializer, onClientClose, CLIENT_IDENTIFIER);

        // Arrange
        ServiceBusReceivedMessage receivedMessage1 = mockReceivedMessage(Instant.now());
        when(messageSerializer.deserialize(any(Message.class), eq(ServiceBusReceivedMessage.class)))
            .thenReturn(receivedMessage1);

        // Act & Assert
        StepVerifier.create(receiver.receiveMessages().take(1))
            .then(() -> messages.forEach(m -> messageSink.next(m)))
            .expectNextCount(1)
            .verifyComplete();

        TestHistogram receiverLag = meter.getHistograms().get("messaging.servicebus.receiver.lag");
        assertEquals(1, receiverLag.getMeasurements().size());

        TestMeasurement<Double> measurement = receiverLag.getMeasurements().get(0);
        Map<String, Object> attributes = measurement.getAttributes();
        assertEquals(2, attributes.size());
        assertCommonMetricAttributes(attributes, null);
    }

    private ServiceBusReceivedMessage mockReceivedMessage(Instant enqueuedTime) {
        ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);
        when(receivedMessage.getLockedUntil()).thenReturn(OffsetDateTime.now());
        when(receivedMessage.getLockToken()).thenReturn(UUID.randomUUID().toString());
        when(receivedMessage.getEnqueuedTime()).thenReturn(enqueuedTime.atOffset(ZoneOffset.UTC));
        return receivedMessage;
    }

    private List<Message> getMessages() {
        final Map<String, String> map = Collections.singletonMap("SAMPLE_HEADER", "foo");

        return IntStream.range(0, 10)
            .mapToObj(index -> getMessage(PAYLOAD_BYTES, messageTrackingUUID, map))
            .collect(Collectors.toList());
    }

    private static Stream<Arguments> errorSourceNoneOnSettlement() {
        return Stream.of(
            Arguments.of(DispositionStatus.COMPLETED, DeliveryStateType.Accepted, ServiceBusErrorSource.COMPLETE),
            Arguments.of(DispositionStatus.ABANDONED, DeliveryStateType.Modified, ServiceBusErrorSource.ABANDON));
    }

    private void assertCommonMetricAttributes(Map<String, Object> attributes, String subscriptionName) {
        assertEquals(NAMESPACE, attributes.get("hostName"));
        assertEquals(ENTITY_PATH, attributes.get("entityName"));
        assertEquals(subscriptionName, attributes.get("subscriptionName"));
    }
}
