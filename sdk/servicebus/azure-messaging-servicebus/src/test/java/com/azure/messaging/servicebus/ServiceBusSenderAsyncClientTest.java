// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransaction;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.CbsAuthorizationType;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.models.CreateBatchOptions;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.amqp.transaction.TransactionalState;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
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
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.azure.messaging.servicebus.ServiceBusSenderAsyncClient.MAX_MESSAGE_LENGTH_BYTES;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
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
    private static final String NAMESPACE = "my-namespace";
    private static final String ENTITY_NAME = "my-servicebus-entity";
    private static final String LINK_NAME = "my-link-name";
    private static final String TEST_CONTENTS = "My message for service bus queue!";
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
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP, retryOptions,
            ProxyOptions.SYSTEM_DEFAULTS, Schedulers.parallel());

        when(connection.getEndpointStates()).thenReturn(endpointProcessor);
        endpointSink.next(AmqpEndpointState.ACTIVE);

        connectionProcessor = Mono.fromCallable(() -> connection).repeat(10).subscribeWith(
            new ServiceBusConnectionProcessor(connectionOptions.getFullyQualifiedNamespace(),
                connectionOptions.getRetry()));

        sender = new ServiceBusSenderAsyncClient(ENTITY_NAME, MessagingEntityType.QUEUE, connectionProcessor,
            retryOptions, tracerProvider, serializer, onClientClose);

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
        StepVerifier.create(sender.createBatch(null))
            .verifyErrorMatches(error -> error instanceof NullPointerException);
    }

    /**
     * Verifies that the default batch is the same size as the message link.
     */
    @Test
    void createBatchDefault() {
        // Arrange
        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), any(AmqpRetryOptions.class)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.getLinkSize()).thenReturn(Mono.just(MAX_MESSAGE_LENGTH_BYTES));

        // Act & Assert
        StepVerifier.create(sender.createBatch())
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

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(link));

        // This event is 1024 bytes when serialized.
        final CreateBatchOptions options = new CreateBatchOptions().setMaximumSizeInBytes(batchSize);

        // Act & Assert
        StepVerifier.create(sender.createBatch(options))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    /**
     * Verifies that the producer can create a batch with a given {@link CreateBatchOptions#getMaximumSizeInBytes()}.
     */
    @Test
    void createsMessageBatchWithSize() {
        // Arrange
        int maxLinkSize = 10000;
        int batchSize = 1024;

        // Overhead when serializing an event, to figure out what the maximum size we can use for an event payload.
        int eventOverhead = 46;
        int maxEventPayload = batchSize - eventOverhead;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(link));

        // This is 1024 bytes when serialized.
        final ServiceBusMessage event = new ServiceBusMessage(new byte[maxEventPayload]);

        final ServiceBusMessage tooLargeEvent = new ServiceBusMessage(new byte[maxEventPayload + 1]);
        final CreateBatchOptions options = new CreateBatchOptions().setMaximumSizeInBytes(batchSize);

        // Act & Assert
        StepVerifier.create(sender.createBatch(options))
            .assertNext(batch -> {
                Assertions.assertEquals(batchSize, batch.getMaxSizeInBytes());
                Assertions.assertTrue(batch.tryAdd(event));
            })
            .verifyComplete();

        StepVerifier.create(sender.createBatch(options))
            .assertNext(batch -> {
                Assertions.assertEquals(batchSize, batch.getMaxSizeInBytes());
                Assertions.assertFalse(batch.tryAdd(tooLargeEvent));
            })
            .verifyComplete();
    }

    /**
     * Verifies that sending multiple message will result in calling sender.send(MessageBatch, transaction).
     */
    @Test
    void sendMultipleMessagesWithTransaction() {
        // Arrange
        final int count = 4;
        final byte[] contents = TEST_CONTENTS.getBytes(UTF_8);
        final ServiceBusMessageBatch batch = new ServiceBusMessageBatch(256 * 1024,
            errorContextProvider, tracerProvider, serializer);

        IntStream.range(0, count).forEach(index -> {
            final ServiceBusMessage message = new ServiceBusMessage(contents);
            Assertions.assertTrue(batch.tryAdd(message));
        });

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(any(Message.class), any(DeliveryState.class))).thenReturn(Mono.empty());
        when(sendLink.send(anyList(), any(DeliveryState.class))).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(sender.send(batch, transactionContext))
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
        final byte[] contents = TEST_CONTENTS.getBytes(UTF_8);
        final ServiceBusMessageBatch batch = new ServiceBusMessageBatch(256 * 1024,
            errorContextProvider, tracerProvider, serializer);

        IntStream.range(0, count).forEach(index -> {
            final ServiceBusMessage message = new ServiceBusMessage(contents);
            Assertions.assertTrue(batch.tryAdd(message));
        });

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());
        // Act
        StepVerifier.create(sender.send(batch))
            .verifyComplete();

        // Assert
        verify(sendLink).send(messagesCaptor.capture());

        final List<org.apache.qpid.proton.message.Message> messagesSent = messagesCaptor.getValue();
        Assertions.assertEquals(count, messagesSent.size());

        messagesSent.forEach(message -> Assertions.assertEquals(Section.SectionType.Data, message.getBody().getType()));
    }

    /**
     * Verifies that sending multiple message will result in calling sender.send(Message...).
     */
    @Test
    void sendMessagesListWithTransaction() {
        // Arrange
        final int count = 4;
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(count, UUID.randomUUID().toString());

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(any(Message.class), any(DeliveryState.class))).thenReturn(Mono.empty());
        when(sendLink.send(anyList(), any(DeliveryState.class))).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(sender.send(messages, transactionContext))
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
        final byte[] contents = TEST_CONTENTS.getBytes(UTF_8);
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(count, UUID.randomUUID().toString());

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(sender.send(messages))
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

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.getLinkSize()).thenReturn(linkMaxSize);

        // Act & Assert
        StepVerifier.create(sender.send(messages))
            .verifyErrorMatches(error -> error instanceof AmqpException
                && ((AmqpException) error).getErrorCondition() == AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED);

        verify(sendLink, never()).send(anyList());
    }

    /**
     * Verifies that sending a single message will result in calling sender.send(Message, transaction).
     */
    @Test
    void sendSingleMessageWithTransaction() {
        // Arrange
        final ServiceBusMessage testData = new ServiceBusMessage(TEST_CONTENTS.getBytes(UTF_8));

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink));

        when(sendLink.getLinkSize()).thenReturn(Mono.just(MAX_MESSAGE_LENGTH_BYTES));
        when(sendLink.send(any(org.apache.qpid.proton.message.Message.class), any(DeliveryState.class))).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(sender.send(testData, transactionContext))
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
            new ServiceBusMessage(TEST_CONTENTS.getBytes(UTF_8));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink));

        when(sendLink.getLinkSize()).thenReturn(Mono.just(MAX_MESSAGE_LENGTH_BYTES));
        when(sendLink.send(any(org.apache.qpid.proton.message.Message.class))).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(sender.send(testData))
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
        Instant instant = mock(Instant.class);

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), any(AmqpRetryOptions.class)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.getLinkSize()).thenReturn(Mono.just(MAX_MESSAGE_LENGTH_BYTES));
        when(managementNode.schedule(eq(message), eq(instant), any(Integer.class), any(), isNull()))
            .thenReturn(just(sequenceNumberReturned));

        // Act & Assert
        StepVerifier.create(sender.scheduleMessage(message, instant))
            .expectNext(sequenceNumberReturned)
            .verifyComplete();

        verify(managementNode).schedule(message, instant, MAX_MESSAGE_LENGTH_BYTES, LINK_NAME, null);
    }

    @Test
    void scheduleMessageWithTransaction() {
        // Arrange
        final long sequenceNumberReturned = 10;
        final Instant instant = mock(Instant.class);
        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), any(AmqpRetryOptions.class)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.getLinkSize()).thenReturn(Mono.just(MAX_MESSAGE_LENGTH_BYTES));
        when(managementNode.schedule(eq(message), eq(instant), eq(MAX_MESSAGE_LENGTH_BYTES), eq(LINK_NAME), argThat(e -> e.getTransactionId().equals(transactionContext.getTransactionId()))))
            .thenReturn(just(sequenceNumberReturned));

        // Act & Assert
        StepVerifier.create(sender.scheduleMessage(message, instant, transactionContext))
            .expectNext(sequenceNumberReturned)
            .verifyComplete();

        verify(managementNode).schedule(eq(message), eq(instant), eq(MAX_MESSAGE_LENGTH_BYTES), eq(LINK_NAME), argThat(e -> e.getTransactionId().equals(transactionContext.getTransactionId())));
    }


    @Test
    void cancelScheduleMessage() {
        // Arrange
        long sequenceNumberReturned = 10;
        when(managementNode.cancelScheduledMessage(eq(sequenceNumberReturned), isNull())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(sender.cancelScheduledMessage(sequenceNumberReturned))
            .verifyComplete();
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
