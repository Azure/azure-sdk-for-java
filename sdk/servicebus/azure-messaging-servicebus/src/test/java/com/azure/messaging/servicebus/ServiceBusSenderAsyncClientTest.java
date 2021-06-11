// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransaction;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusConstants;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.models.CreateMessageBatchOptions;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.amqp.transaction.TransactionalState;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_BUILDER_KEY;
import static com.azure.messaging.servicebus.ServiceBusSenderAsyncClient.MAX_MESSAGE_LENGTH_BYTES;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.AZ_TRACING_NAMESPACE_VALUE;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.AZ_TRACING_SERVICE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Mono.just;

/**
 * Unit tests for {@link ServiceBusSenderAsyncClient}.
 */
class ServiceBusSenderAsyncClientTest {
    private static final ClientOptions CLIENT_OPTIONS = new ClientOptions();
    private static final String NAMESPACE = "my-namespace";
    private static final String ENTITY_NAME = "my-servicebus-entity";
    private static final String LINK_NAME = "my-link-name";
    private static final BinaryData TEST_CONTENTS = BinaryData.fromString("My message for service bus queue!");
    private static final  String TXN_ID_STRING = "1";

    @Mock
    private AmqpSendLink sendLink;
    @Mock
    private ServiceBusAmqpConnection connection;
    @Mock
    private TokenCredential tokenCredential;
    @Mock
    private ErrorContextProvider errorContextProvider;
    @Mock
    private ServiceBusManagementNode managementNode;
    @Mock
    private ServiceBusMessage message;
    @Mock
    private Runnable onClientClose;
    @Mock
    ServiceBusTransactionContext transactionContext;
    @Mock
    AmqpTransaction amqpTransaction;

    @Captor
    private ArgumentCaptor<Message> singleMessageCaptor;
    @Captor
    private ArgumentCaptor<DeliveryState> amqpDeliveryStateCaptor;
    @Captor
    private ArgumentCaptor<List<Message>> messagesCaptor;
    @Captor
    private ArgumentCaptor<ServiceBusMessage> singleSBMessageCaptor;
    @Captor
    private ArgumentCaptor<List<ServiceBusMessage>> sbMessagesCaptor;
    @Captor
    private ArgumentCaptor<Iterable<Long>> sequenceNumberCaptor;

    private final MessageSerializer serializer = new ServiceBusMessageSerializer();
    private final TracerProvider tracerProvider = new TracerProvider(Collections.emptyList());
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions()
        .setDelay(Duration.ofMillis(500))
        .setMode(AmqpRetryMode.FIXED)
        .setTryTimeout(Duration.ofSeconds(10));
    private final DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();
    private final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private ServiceBusSenderAsyncClient sender;
    private ServiceBusConnectionProcessor connectionProcessor;
    private ConnectionOptions connectionOptions;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        connectionOptions = new ConnectionOptions(NAMESPACE, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, ServiceBusConstants.AZURE_ACTIVE_DIRECTORY_SCOPE,
            AmqpTransportType.AMQP, retryOptions, ProxyOptions.SYSTEM_DEFAULTS, Schedulers.parallel(), CLIENT_OPTIONS,
            SslDomain.VerifyMode.VERIFY_PEER_NAME, "test-product", "test-version");

        when(connection.getEndpointStates()).thenReturn(endpointProcessor);
        endpointSink.next(AmqpEndpointState.ACTIVE);

        connectionProcessor = Mono.fromCallable(() -> connection).repeat(10).subscribeWith(
            new ServiceBusConnectionProcessor(connectionOptions.getFullyQualifiedNamespace(),
                connectionOptions.getRetry()));

        sender = new ServiceBusSenderAsyncClient(ENTITY_NAME, MessagingEntityType.QUEUE, connectionProcessor,
            retryOptions, tracerProvider, serializer, onClientClose, null);

        when(connection.getManagementNode(anyString(), any(MessagingEntityType.class)))
            .thenReturn(just(managementNode));

        when(sendLink.getLinkSize()).thenReturn(Mono.just(ServiceBusSenderAsyncClient.MAX_MESSAGE_LENGTH_BYTES));
        when(sendLink.getLinkName()).thenReturn(LINK_NAME);

        doNothing().when(onClientClose).run();

        ByteBuffer txnId = ByteBuffer.wrap(TXN_ID_STRING.getBytes());
        when((transactionContext.getTransactionId())).thenReturn(txnId);
        when(amqpTransaction.getTransactionId()).thenReturn(txnId);
    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMocks();
    }

    /**
     * Verifies that the correct Service Bus properties are set.
     */
    @Test
    void verifyProperties() {
        Assertions.assertEquals(ENTITY_NAME, sender.getEntityPath());
        Assertions.assertEquals(NAMESPACE, sender.getFullyQualifiedNamespace());
    }

    /**
     * Verifies that an exception is thrown when we create a batch with null options.
     */
    @Test
    void createBatchNull() {
        StepVerifier.create(sender.createMessageBatch(null))
            .verifyErrorMatches(error -> error instanceof NullPointerException);
    }

    /**
     * Verifies that the default batch is the same size as the message link.
     */
    @Test
    void createBatchDefault() {
        // Arrange
        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), any(AmqpRetryOptions.class), isNull()))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.getLinkSize()).thenReturn(Mono.just(MAX_MESSAGE_LENGTH_BYTES));

        // Act & Assert
        StepVerifier.create(sender.createMessageBatch())
            .assertNext(batch -> {
                Assertions.assertEquals(MAX_MESSAGE_LENGTH_BYTES, batch.getMaxSizeInBytes());
                Assertions.assertEquals(0, batch.getCount());
            })
            .verifyComplete();
    }

    /**
     * Verifies we cannot create a batch if the options size is larger than the link.
     */
    @Test
    void createBatchWhenSizeTooBig() {
        // Arrange
        int maxLinkSize = 1024;
        int batchSize = maxLinkSize + 10;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull()))
            .thenReturn(Mono.just(link));

        // This event is 1024 bytes when serialized.
        final CreateMessageBatchOptions options = new CreateMessageBatchOptions().setMaximumSizeInBytes(batchSize);

        // Act & Assert
        StepVerifier.create(sender.createMessageBatch(options))
            .expectError(ServiceBusException.class)
            .verify();
    }

    /**
     * Verifies that the producer can create a batch with a given {@link CreateMessageBatchOptions#getMaximumSizeInBytes()}.
     */
    @Test
    void createsMessageBatchWithSize() {
        // Arrange
        int maxLinkSize = 10000;
        int batchSize = 1024;

        // Overhead when serializing an event, to figure out what the maximum size we can use for an event payload.
        int eventOverhead = 75;
        int maxEventPayload = batchSize - eventOverhead;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull()))
            .thenReturn(Mono.just(link));

        // This is 1024 bytes when serialized.
        final ServiceBusMessage event = new ServiceBusMessage(BinaryData.fromBytes(new byte[maxEventPayload]));

        final ServiceBusMessage tooLargeEvent = new ServiceBusMessage(BinaryData.fromBytes(new byte[maxEventPayload + 1]));
        final CreateMessageBatchOptions options = new CreateMessageBatchOptions().setMaximumSizeInBytes(batchSize);

        // Act & Assert
        StepVerifier.create(sender.createMessageBatch(options))
            .assertNext(batch -> {
                Assertions.assertEquals(batchSize, batch.getMaxSizeInBytes());
                Assertions.assertTrue(batch.tryAddMessage(event));
            })
            .verifyComplete();

        StepVerifier.create(sender.createMessageBatch(options))
            .assertNext(batch -> {
                Assertions.assertEquals(batchSize, batch.getMaxSizeInBytes());
                Assertions.assertFalse(batch.tryAddMessage(tooLargeEvent));
            })
            .verifyComplete();
    }

    @Test
    void scheduleMessageSizeTooBig() {
        // Arrange
        int maxLinkSize = 1024;
        int batchSize = maxLinkSize + 10;

        OffsetDateTime instant = mock(OffsetDateTime.class);
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(batchSize, UUID.randomUUID().toString());

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), any(AmqpRetryOptions.class), isNull()))
            .thenReturn(Mono.just(link));
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // Act & Assert
        StepVerifier.create(sender.scheduleMessages(messages, instant))
            .verifyError(IllegalArgumentException.class);

        verify(managementNode, never()).schedule(any(), eq(instant), anyInt(), eq(LINK_NAME), isNull());
    }


    /**
     * Verifies that sending multiple message will result in calling sender.send(MessageBatch, transaction).
     */
    @Test
    void sendMultipleMessagesWithTransaction() {
        // Arrange
        final int count = 4;
        final byte[] contents = TEST_CONTENTS.toBytes();
        final ServiceBusMessageBatch batch = new ServiceBusMessageBatch(256 * 1024,
            errorContextProvider, tracerProvider, serializer, null, null);

        IntStream.range(0, count).forEach(index -> {
            final ServiceBusMessage message = new ServiceBusMessage(BinaryData.fromBytes(contents));
            Assertions.assertTrue(batch.tryAddMessage(message));
        });

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull()))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(any(Message.class), any(DeliveryState.class))).thenReturn(Mono.empty());
        when(sendLink.send(anyList(), any(DeliveryState.class))).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(sender.sendMessages(batch, transactionContext))
            .verifyComplete();

        // Assert
        verify(sendLink).send(messagesCaptor.capture(), amqpDeliveryStateCaptor.capture());

        final List<org.apache.qpid.proton.message.Message> messagesSent = messagesCaptor.getValue();
        Assertions.assertEquals(count, messagesSent.size());

        messagesSent.forEach(message -> Assertions.assertEquals(Section.SectionType.Data, message.getBody().getType()));

        final DeliveryState delivery = amqpDeliveryStateCaptor.getValue();
        Assertions.assertNotNull(delivery);
        Assertions.assertTrue(delivery instanceof TransactionalState);
        Assertions.assertEquals(TXN_ID_STRING, ((TransactionalState) delivery).getTxnId().toString());
    }

    /**
     * Verifies that sending multiple message will result in calling sender.send(MessageBatch).
     */
    @Test
    void sendMultipleMessages() {
        // Arrange
        final int count = 4;
        final byte[] contents = TEST_CONTENTS.toBytes();
        final ServiceBusMessageBatch batch = new ServiceBusMessageBatch(256 * 1024,
            errorContextProvider, tracerProvider, serializer, null, null);

        IntStream.range(0, count).forEach(index -> {
            final ServiceBusMessage message = new ServiceBusMessage(BinaryData.fromBytes(contents));
            Assertions.assertTrue(batch.tryAddMessage(message));
        });

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull()))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());
        // Act
        StepVerifier.create(sender.sendMessages(batch))
            .verifyComplete();

        // Assert
        verify(sendLink).send(messagesCaptor.capture());

        final List<org.apache.qpid.proton.message.Message> messagesSent = messagesCaptor.getValue();
        Assertions.assertEquals(count, messagesSent.size());

        messagesSent.forEach(message -> Assertions.assertEquals(Section.SectionType.Data, message.getBody().getType()));
    }

    /**
     * Verifies that sending multiple message will result in calling sender.send(MessageBatch).
     */
    @Test
    void sendMultipleMessagesTracerSpans() {
        // Arrange
        final int count = 4;
        final byte[] contents = TEST_CONTENTS.toBytes();
        final Tracer tracer1 = mock(Tracer.class);
        TracerProvider tracerProvider1 = new TracerProvider(Arrays.asList(tracer1));

        final ServiceBusMessageBatch batch = new ServiceBusMessageBatch(256 * 1024,
            errorContextProvider, tracerProvider1, serializer, null, null);
        sender = new ServiceBusSenderAsyncClient(ENTITY_NAME, MessagingEntityType.QUEUE, connectionProcessor,
            retryOptions, tracerProvider1, serializer, onClientClose, null);

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull()))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());
        when(tracer1.start(eq(AZ_TRACING_SERVICE_NAME + "send"), any(Context.class), eq(ProcessKind.SEND)))
            .thenAnswer(invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                assertEquals(passed.getData(AZ_TRACING_NAMESPACE_KEY).get(), AZ_TRACING_NAMESPACE_VALUE);
                return passed.addData(PARENT_SPAN_KEY, "value");
            });

        when(tracer1.start(eq(AZ_TRACING_SERVICE_NAME + "message"), any(Context.class), eq(ProcessKind.MESSAGE)))
            .thenAnswer(invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                assertEquals(passed.getData(AZ_TRACING_NAMESPACE_KEY).get(), AZ_TRACING_NAMESPACE_VALUE);
                return passed.addData(PARENT_SPAN_KEY, "value").addData(DIAGNOSTIC_ID_KEY, "value2");
            });

        when(tracer1.getSharedSpanBuilder(eq(AZ_TRACING_SERVICE_NAME + "send"), any(Context.class))).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(SPAN_BUILDER_KEY, "value");
            }
        );

        when(tracer1.getSharedSpanBuilder(eq(AZ_TRACING_SERVICE_NAME + "send"), any(Context.class))).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(SPAN_BUILDER_KEY, "value");
            }
        );

        IntStream.range(0, count).forEach(index -> {
            final ServiceBusMessage message = new ServiceBusMessage(BinaryData.fromBytes(contents));
            Assertions.assertTrue(batch.tryAddMessage(message));
        });

        // Act
        StepVerifier.create(sender.sendMessages(batch))
            .verifyComplete();

        // Assert
        verify(tracer1, times(4))
            .start(eq(AZ_TRACING_SERVICE_NAME + "message"), any(Context.class), eq(ProcessKind.MESSAGE));
        verify(tracer1, times(1))
            .start(eq(AZ_TRACING_SERVICE_NAME + "send"), any(Context.class), eq(ProcessKind.SEND));
        verify(tracer1, times(5)).end(eq("success"), isNull(), any(Context.class));
    }

    /**
     * Verifies that sending multiple message will result in calling sender.send(Message...).
     */
    @Test
    void sendMessagesListWithTransaction() {
        // Arrange
        final int count = 4;
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(count, UUID.randomUUID().toString());

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull()))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(any(Message.class), any(DeliveryState.class))).thenReturn(Mono.empty());
        when(sendLink.send(anyList(), any(DeliveryState.class))).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(sender.sendMessages(messages, transactionContext))
            .verifyComplete();

        // Assert
        verify(sendLink).send(messagesCaptor.capture(), amqpDeliveryStateCaptor.capture());

        final List<Message> messagesSent = messagesCaptor.getValue();
        Assertions.assertEquals(count, messagesSent.size());

        messagesSent.forEach(message -> Assertions.assertEquals(Section.SectionType.Data, message.getBody().getType()));

        final DeliveryState delivery = amqpDeliveryStateCaptor.getValue();
        Assertions.assertNotNull(delivery);
        Assertions.assertTrue(delivery instanceof TransactionalState);
        Assertions.assertEquals(TXN_ID_STRING, ((TransactionalState) delivery).getTxnId().toString());
    }

    /**
     * Verifies that sending multiple message will result in calling sender.send(Message...).
     */
    @Test
    void sendMessagesList() {
        // Arrange
        final int count = 4;
        final byte[] contents = TEST_CONTENTS.toBytes();
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(count, UUID.randomUUID().toString());

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull()))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(sender.sendMessages(messages))
            .verifyComplete();

        // Assert
        verify(sendLink).send(messagesCaptor.capture());

        final List<Message> messagesSent = messagesCaptor.getValue();
        Assertions.assertEquals(count, messagesSent.size());

        messagesSent.forEach(message -> Assertions.assertEquals(Section.SectionType.Data, message.getBody().getType()));
    }

    /**
     * Verifies that sending multiple message which does not fit in single batch will throw exception.
     */
    @Test
    void sendMessagesListExceedSize() {
        // Arrange
        final int count = 4;
        final Mono<Integer> linkMaxSize = Mono.just(1);
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(count, UUID.randomUUID().toString());

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull()))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.getLinkSize()).thenReturn(linkMaxSize);

        // Act & Assert
        StepVerifier.create(sender.sendMessages(messages))
            .verifyErrorMatches(error -> error instanceof ServiceBusException
                && ((ServiceBusException) error).getReason() == ServiceBusFailureReason.MESSAGE_SIZE_EXCEEDED);

        verify(sendLink, never()).send(anyList());
    }

    @Test
    void sendSingleMessageThatExceedsSize() {
        // arrange
        ServiceBusMessage message = TestUtils.getServiceBusMessages(1, UUID.randomUUID().toString()).get(0);

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull()))
                .thenReturn(Mono.just(sendLink));
        when(sendLink.getLinkSize()).thenReturn(Mono.just(1));

        // Act & Assert
        StepVerifier.create(sender.sendMessage(message))
                .verifyErrorMatches(error -> error instanceof ServiceBusException
                        && ((ServiceBusException) error).getReason() == ServiceBusFailureReason.MESSAGE_SIZE_EXCEEDED);

        verify(sendLink, never()).send(anyList());
    }

    /**
     * Verifies that sending a single message will result in calling sender.send(Message, transaction).
     */
    @Test
    void sendSingleMessageWithTransaction() {
        // Arrange
        final ServiceBusMessage testData = new ServiceBusMessage(TEST_CONTENTS);

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull()))
            .thenReturn(Mono.just(sendLink));

        when(sendLink.getLinkSize()).thenReturn(Mono.just(MAX_MESSAGE_LENGTH_BYTES));
        when(sendLink.send(any(org.apache.qpid.proton.message.Message.class), any(DeliveryState.class))).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(sender.sendMessage(testData, transactionContext))
            .verifyComplete();

        // Assert
        verify(sendLink, times(1)).send(any(org.apache.qpid.proton.message.Message.class), any(DeliveryState.class));
        verify(sendLink).send(singleMessageCaptor.capture(), amqpDeliveryStateCaptor.capture());

        final Message message = singleMessageCaptor.getValue();
        Assertions.assertEquals(Section.SectionType.Data, message.getBody().getType());

        final DeliveryState delivery = amqpDeliveryStateCaptor.getValue();
        Assertions.assertNotNull(delivery);
        Assertions.assertTrue(delivery instanceof TransactionalState);
        Assertions.assertEquals(TXN_ID_STRING, ((TransactionalState) delivery).getTxnId().toString());
    }


    /**
     * Verifies that sending a single message will result in calling sender.send(Message).
     */
    @Test
    void sendSingleMessage() {
        // Arrange
        final ServiceBusMessage testData =
            new ServiceBusMessage(TEST_CONTENTS);

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull()))
            .thenReturn(Mono.just(sendLink));

        when(sendLink.getLinkSize()).thenReturn(Mono.just(MAX_MESSAGE_LENGTH_BYTES));
        when(sendLink.send(any(org.apache.qpid.proton.message.Message.class))).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(sender.sendMessage(testData))
            .verifyComplete();

        // Assert
        verify(sendLink, times(1)).send(any(org.apache.qpid.proton.message.Message.class));
        verify(sendLink).send(singleMessageCaptor.capture());

        final Message message = singleMessageCaptor.getValue();
        Assertions.assertEquals(Section.SectionType.Data, message.getBody().getType());
    }

    @Test
    void scheduleMessage() {
        // Arrange
        long sequenceNumberReturned = 10;
        OffsetDateTime instant = mock(OffsetDateTime.class);

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), any(AmqpRetryOptions.class), isNull()))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.getLinkSize()).thenReturn(Mono.just(MAX_MESSAGE_LENGTH_BYTES));
        when(managementNode.schedule(anyList(), eq(instant), any(Integer.class), any(), isNull()))
            .thenReturn(Flux.just(sequenceNumberReturned));

        // Act & Assert
        StepVerifier.create(sender.scheduleMessage(message, instant))
            .expectNext(sequenceNumberReturned)
            .verifyComplete();

        verify(managementNode).schedule(sbMessagesCaptor.capture(), eq(instant), eq(MAX_MESSAGE_LENGTH_BYTES), eq(LINK_NAME), isNull());
        List<ServiceBusMessage> actualMessages = sbMessagesCaptor.getValue();
        Assertions.assertNotNull(actualMessages);
        Assertions.assertEquals(1, actualMessages.size());
        Assertions.assertEquals(message, actualMessages.get(0));
    }

    @Test
    void scheduleMessageWithTransaction() {
        // Arrange
        final long sequenceNumberReturned = 10;
        final OffsetDateTime instant = mock(OffsetDateTime.class);
        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), any(AmqpRetryOptions.class), isNull()))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.getLinkSize()).thenReturn(Mono.just(MAX_MESSAGE_LENGTH_BYTES));
        when(managementNode.schedule(anyList(), eq(instant), eq(MAX_MESSAGE_LENGTH_BYTES), eq(LINK_NAME), argThat(e -> e.getTransactionId().equals(transactionContext.getTransactionId()))))
            .thenReturn(Flux.just(sequenceNumberReturned));

        // Act & Assert
        StepVerifier.create(sender.scheduleMessage(message, instant, transactionContext))
            .expectNext(sequenceNumberReturned)
            .verifyComplete();

        verify(managementNode).schedule(sbMessagesCaptor.capture(), eq(instant), eq(MAX_MESSAGE_LENGTH_BYTES), eq(LINK_NAME), argThat(e -> e.getTransactionId().equals(transactionContext.getTransactionId())));
        List<ServiceBusMessage> actualMessages = sbMessagesCaptor.getValue();
        Assertions.assertNotNull(actualMessages);
        Assertions.assertEquals(1, actualMessages.size());
        Assertions.assertEquals(message, actualMessages.get(0));
    }

    @Test
    void cancelScheduleMessage() {
        // Arrange
        final long sequenceNumberReturned = 10;
        when(managementNode.cancelScheduledMessages(anyList(), isNull())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(sender.cancelScheduledMessage(sequenceNumberReturned))
            .verifyComplete();

        verify(managementNode).cancelScheduledMessages(sequenceNumberCaptor.capture(), isNull());
        Iterable<Long> actualSequenceNumbers = sequenceNumberCaptor.getValue();
        Assertions.assertNotNull(actualSequenceNumbers);

        AtomicInteger actualTotal = new AtomicInteger();
        actualSequenceNumbers.forEach(aLong -> {
            actualTotal.incrementAndGet();
            Assertions.assertEquals(sequenceNumberReturned, aLong);
        });
        Assertions.assertEquals(1, actualTotal.get());
    }

    @Test
    void cancelScheduleMessages() {
        // Arrange
        final List<Long> sequenceNumbers = new ArrayList<>();
        sequenceNumbers.add(10L);
        sequenceNumbers.add(11L);
        sequenceNumbers.add(12L);

        when(managementNode.cancelScheduledMessages(anyList(), isNull())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(sender.cancelScheduledMessages(sequenceNumbers))
            .verifyComplete();

        verify(managementNode).cancelScheduledMessages(sequenceNumberCaptor.capture(), isNull());
        Iterable<Long> actualSequenceNumbers = sequenceNumberCaptor.getValue();
        Assertions.assertNotNull(actualSequenceNumbers);

        AtomicInteger actualTotal = new AtomicInteger();
        actualSequenceNumbers.forEach(aLong -> {
            actualTotal.incrementAndGet();
            Assertions.assertTrue(sequenceNumbers.contains(aLong));
        });
        Assertions.assertEquals(sequenceNumbers.size(), actualTotal.get());
    }

    /**
     * Verifies that the onClientClose is called.
     */
    @Test
    void callsClientClose() {
        // Act
        sender.close();

        // Assert
        verify(onClientClose).run();
    }

    /**
     * Verifies that the onClientClose is only called once.
     */
    @Test
    void callsClientCloseOnce() {
        // Act
        sender.close();
        sender.close();

        // Assert
        verify(onClientClose).run();
    }
}
