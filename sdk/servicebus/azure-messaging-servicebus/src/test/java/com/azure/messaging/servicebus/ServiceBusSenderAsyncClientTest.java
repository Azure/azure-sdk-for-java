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
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.credential.TokenCredential;
import com.azure.core.test.utils.metrics.TestCounter;
import com.azure.core.test.utils.metrics.TestMeasurement;
import com.azure.core.test.utils.metrics.TestMeter;
import com.azure.core.util.BinaryData;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusConstants;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusSenderInstrumentation;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static com.azure.messaging.servicebus.ServiceBusSenderAsyncClient.MAX_MESSAGE_LENGTH_BYTES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
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
    private static final String TXN_ID_STRING = "1";
    private static final String CLIENT_IDENTIFIER = "my-client-identifier";
    private static final ServiceBusSenderInstrumentation DEFAULT_INSTRUMENTATION = new ServiceBusSenderInstrumentation(null, null, NAMESPACE, ENTITY_NAME);
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
            retryOptions, DEFAULT_INSTRUMENTATION, serializer, onClientClose, null, CLIENT_IDENTIFIER);

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
        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Verifies that the correct Service Bus properties are set.
     */
    @Test
    void verifyProperties() {
        Assertions.assertEquals(ENTITY_NAME, sender.getEntityPath());
        Assertions.assertEquals(NAMESPACE, sender.getFullyQualifiedNamespace());
        Assertions.assertEquals(CLIENT_IDENTIFIER, sender.getIdentifier());
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
        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), any(AmqpRetryOptions.class), isNull(), eq(CLIENT_IDENTIFIER)))
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

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull(), eq(CLIENT_IDENTIFIER)))
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
        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull(), eq(CLIENT_IDENTIFIER)))
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

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), any(AmqpRetryOptions.class), isNull(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(link));
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // Act & Assert
        StepVerifier.create(sender.scheduleMessages(messages, instant))
            .verifyError(ServiceBusException.class);

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
            errorContextProvider, DEFAULT_INSTRUMENTATION.getTracer(), serializer);

        IntStream.range(0, count).forEach(index -> {
            final ServiceBusMessage message = new ServiceBusMessage(BinaryData.fromBytes(contents));
            Assertions.assertTrue(batch.tryAddMessage(message));
        });

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull(), eq(CLIENT_IDENTIFIER)))
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
            errorContextProvider, DEFAULT_INSTRUMENTATION.getTracer(), serializer);

        IntStream.range(0, count).forEach(index -> {
            final ServiceBusMessage message = new ServiceBusMessage(BinaryData.fromBytes(contents));
            Assertions.assertTrue(batch.tryAddMessage(message));
        });

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull(), eq(CLIENT_IDENTIFIER)))
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

    @Test
    @SuppressWarnings("unchecked")
    void sendMultipleMessagesTracesSpans() {
        // Arrange
        final int count = 4;
        final byte[] contents = TEST_CONTENTS.toBytes();
        final Tracer tracer1 = mock(Tracer.class);
        when(tracer1.isEnabled()).thenReturn(true);
        ServiceBusSenderInstrumentation instrumentation = new ServiceBusSenderInstrumentation(tracer1, null, NAMESPACE, ENTITY_NAME);

        final ServiceBusMessageBatch batch = new ServiceBusMessageBatch(256 * 1024,
            errorContextProvider, instrumentation.getTracer(), serializer);
        sender = new ServiceBusSenderAsyncClient(ENTITY_NAME, MessagingEntityType.QUEUE, connectionProcessor,
            retryOptions, instrumentation, serializer, onClientClose, null, CLIENT_IDENTIFIER);

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        when(tracer1.start(eq("ServiceBus.message"), any(), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(invocation.getArgument(1, StartSpanOptions.class), SpanKind.PRODUCER, 0);
                return invocation.getArgument(2, Context.class)
                    .addData(SPAN_CONTEXT_KEY, "span");
            });

        when(tracer1.start(eq("ServiceBus.send"), any(), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(invocation.getArgument(1, StartSpanOptions.class), SpanKind.CLIENT, count);
                return invocation.getArgument(2, Context.class)
                    .addData(PARENT_TRACE_CONTEXT_KEY, "trace-context");
            }
        );

        doAnswer(invocation -> {
            BiConsumer<String, String> injectContext = invocation.getArgument(0, BiConsumer.class);
            injectContext.accept("traceparent", "diag-id");
            return null;
        }).when(tracer1).injectContext(any(), any(Context.class));


        IntStream.range(0, count).forEach(index -> {
            final ServiceBusMessage message = new ServiceBusMessage(BinaryData.fromBytes(contents));
            Assertions.assertTrue(batch.tryAddMessage(message));
        });

        // Act
        StepVerifier.create(sender.sendMessages(batch))
            .verifyComplete();

        // Assert
        verify(tracer1, times(4))
            .start(eq("ServiceBus.message"), any(StartSpanOptions.class), any(Context.class));
        verify(tracer1, times(1))
            .start(eq("ServiceBus.send"), any(StartSpanOptions.class), any(Context.class));
        verify(tracer1, times(5)).end(isNull(), isNull(), any(Context.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendCancelledIsInstrumented() {
        // Arrange
        final Tracer tracer1 = mock(Tracer.class);
        final TestMeter meter = new TestMeter();
        when(tracer1.isEnabled()).thenReturn(true);
        ServiceBusSenderInstrumentation instrumentation = new ServiceBusSenderInstrumentation(tracer1, meter, NAMESPACE, ENTITY_NAME);

        sender = new ServiceBusSenderAsyncClient(ENTITY_NAME, MessagingEntityType.QUEUE, connectionProcessor,
            retryOptions, instrumentation, serializer, onClientClose, null, CLIENT_IDENTIFIER);

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));

        when(sendLink.send(any(Message.class))).thenAnswer(i -> Mono.delay(Duration.ofSeconds(10)).then());

        when(tracer1.start(eq("ServiceBus.message"), any(), any(Context.class))).thenAnswer(
            invocation -> invocation.getArgument(2, Context.class)
                .addData(SPAN_CONTEXT_KEY, "span"));

        when(tracer1.start(eq("ServiceBus.send"), any(), any(Context.class))).thenAnswer(
            invocation -> invocation.getArgument(2, Context.class)
                .addData(PARENT_TRACE_CONTEXT_KEY, "trace-context")
        );

        doAnswer(invocation -> null).when(tracer1).injectContext(any(), any(Context.class));

        // Act
        sender.sendMessage(new ServiceBusMessage(BinaryData.fromBytes(TEST_CONTENTS.toBytes())))
            .toFuture().cancel(true);

        // Assert
        verify(tracer1, times(1))
            .start(eq("ServiceBus.message"), any(StartSpanOptions.class), any(Context.class));
        verify(tracer1, times(1))
            .start(eq("ServiceBus.send"), any(StartSpanOptions.class), any(Context.class));
        verify(tracer1, times(1)).end(eq("cancelled"), isNull(), any(Context.class));

        TestCounter sentMessagesCounter = meter.getCounters().get("messaging.servicebus.messages.sent");
        assertNotNull(sentMessagesCounter);
        assertEquals(1, sentMessagesCounter.getMeasurements().size());

        TestMeasurement<Long> measurement1 = sentMessagesCounter.getMeasurements().get(0);
        assertEquals(1, measurement1.getValue());

        Map<String, Object> attributes1 = measurement1.getAttributes();
        assertEquals(3, attributes1.size());
        assertCommonMetricAttributes(attributes1, "cancelled");
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendCancelledMetricsOnly() {
        // Arrange
        final TestMeter meter = new TestMeter();
        ServiceBusSenderInstrumentation instrumentation = new ServiceBusSenderInstrumentation(null, meter, NAMESPACE, ENTITY_NAME);

        sender = new ServiceBusSenderAsyncClient(ENTITY_NAME, MessagingEntityType.QUEUE, connectionProcessor,
            retryOptions, instrumentation, serializer, onClientClose, null, CLIENT_IDENTIFIER);

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));

        when(sendLink.send(any(Message.class))).thenAnswer(i -> Mono.delay(Duration.ofSeconds(10)).then());

        // Act
        sender.sendMessage(new ServiceBusMessage(BinaryData.fromBytes(TEST_CONTENTS.toBytes())))
            .toFuture().cancel(true);

        TestCounter sentMessagesCounter = meter.getCounters().get("messaging.servicebus.messages.sent");
        assertNotNull(sentMessagesCounter);
        assertEquals(1, sentMessagesCounter.getMeasurements().size());

        TestMeasurement<Long> measurement1 = sentMessagesCounter.getMeasurements().get(0);
        assertEquals(1, measurement1.getValue());

        Map<String, Object> attributes1 = measurement1.getAttributes();
        assertEquals(3, attributes1.size());
        assertCommonMetricAttributes(attributes1, "cancelled");
    }

    @Test
    void sendMessageReportsMetrics() {
        // Arrange
        TestMeter meter = new TestMeter();
        ServiceBusSenderInstrumentation instrumentation = new ServiceBusSenderInstrumentation(null, meter, NAMESPACE, ENTITY_NAME);

        sender = new ServiceBusSenderAsyncClient(ENTITY_NAME, MessagingEntityType.QUEUE, connectionProcessor,
            retryOptions, instrumentation, serializer, onClientClose, null, CLIENT_IDENTIFIER);

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(any(Message.class))).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(sender.sendMessage(new ServiceBusMessage(TEST_CONTENTS))
                .then(sender.sendMessage(new ServiceBusMessage(TEST_CONTENTS))))
            .verifyComplete();

        // Assert
        TestCounter sentMessagesCounter = meter.getCounters().get("messaging.servicebus.messages.sent");
        assertNotNull(sentMessagesCounter);
        assertEquals(2, sentMessagesCounter.getMeasurements().size());

        TestMeasurement<Long> measurement1 = sentMessagesCounter.getMeasurements().get(0);
        TestMeasurement<Long> measurement2 = sentMessagesCounter.getMeasurements().get(1);
        assertEquals(1, measurement1.getValue());
        assertEquals(1, measurement2.getValue());

        Map<String, Object> attributes1 = measurement1.getAttributes();
        Map<String, Object> attributes2 = measurement2.getAttributes();
        assertEquals(3, attributes1.size());
        assertCommonMetricAttributes(attributes1, "ok");
        assertEquals(3, attributes2.size());
        assertCommonMetricAttributes(attributes2, "ok");
    }

    @Test
    void sendMessageReportsMetricsAndTraces() {
        // Arrange
        TestMeter meter = new TestMeter();
        Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);
        ServiceBusSenderInstrumentation instrumentation = new ServiceBusSenderInstrumentation(tracer, meter, NAMESPACE, ENTITY_NAME);

        sender = new ServiceBusSenderAsyncClient(ENTITY_NAME, MessagingEntityType.QUEUE, connectionProcessor,
            retryOptions, instrumentation, serializer, onClientClose, null, CLIENT_IDENTIFIER);

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(any(Message.class))).thenReturn(Mono.empty());

        Context span = new Context("marker", true);
        when(tracer.start(eq("ServiceBus.send"), any(StartSpanOptions.class), any(Context.class)))
            .thenReturn(span);

        when(tracer.extractContext(any())).thenReturn(Context.NONE);
        when(tracer.start(eq("ServiceBus.message"), any(StartSpanOptions.class), any(Context.class))).thenReturn(Context.NONE);

        // Act
        StepVerifier.create(sender.sendMessage(new ServiceBusMessage(TEST_CONTENTS)))
            .verifyComplete();

        // Assert
        TestCounter sentMessagesCounter = meter.getCounters().get("messaging.servicebus.messages.sent");
        TestMeasurement<Long> measurement = sentMessagesCounter.getMeasurements().get(0);
        assertEquals(1, measurement.getValue());

        Map<String, Object> attributes = measurement.getAttributes();
        assertEquals(3, attributes.size());
        assertCommonMetricAttributes(attributes, "ok");
        assertEquals(span, measurement.getContext());
    }

    @Test
    void sendMessageBatchReportsMetrics() {
        // Arrange
        TestMeter meter = new TestMeter();
        ServiceBusSenderInstrumentation instrumentation = new ServiceBusSenderInstrumentation(null, meter, NAMESPACE, ENTITY_NAME);

        final ServiceBusMessageBatch batch = new ServiceBusMessageBatch(256 * 1024,
            errorContextProvider, instrumentation.getTracer(), serializer);
        batch.tryAddMessage(new ServiceBusMessage(TEST_CONTENTS));
        batch.tryAddMessage(new ServiceBusMessage(TEST_CONTENTS));

        sender = new ServiceBusSenderAsyncClient(ENTITY_NAME, MessagingEntityType.QUEUE, connectionProcessor,
            retryOptions, instrumentation, serializer, onClientClose, null, CLIENT_IDENTIFIER);

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(sender.sendMessages(batch))
            .verifyComplete();

        // Assert
        TestCounter sentMessagesCounter = meter.getCounters().get("messaging.servicebus.messages.sent");
        TestMeasurement<Long> measurement = sentMessagesCounter.getMeasurements().get(0);
        assertEquals(2, measurement.getValue());

        assertEquals(3,  measurement.getAttributes().size());
        assertCommonMetricAttributes(measurement.getAttributes(), "ok");
    }

    @Test
    void failedSendMessageReportsMetrics() {
        // Arrange
        TestMeter meter = new TestMeter();
        ServiceBusSenderInstrumentation instrumentation = new ServiceBusSenderInstrumentation(null, meter, NAMESPACE, ENTITY_NAME);

        sender = new ServiceBusSenderAsyncClient(ENTITY_NAME, MessagingEntityType.QUEUE, connectionProcessor,
            retryOptions, instrumentation, serializer, onClientClose, null, CLIENT_IDENTIFIER);

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(any(Message.class))).thenThrow(new RuntimeException("foo"));

        // Act
        StepVerifier.create(sender.sendMessage(new ServiceBusMessage(TEST_CONTENTS)))
            .expectError()
            .verify();

        // Assert
        TestCounter sentMessagesCounter = meter.getCounters().get("messaging.servicebus.messages.sent");
        TestMeasurement<Long> measurement = sentMessagesCounter.getMeasurements().get(0);
        assertEquals(1, measurement.getValue());

        Map<String, Object> attributes = measurement.getAttributes();
        assertEquals(3, attributes.size());
        assertCommonMetricAttributes(attributes, "error");
    }

    /**
     * Verifies that sending multiple message will result in calling sender.send(Message...).
     */
    @Test
    void sendMessagesListWithTransaction() {
        // Arrange
        final int count = 4;
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(count, UUID.randomUUID().toString());

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull(), eq(CLIENT_IDENTIFIER)))
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
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(count, UUID.randomUUID().toString());

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull(), eq(CLIENT_IDENTIFIER)))
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

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull(), eq(CLIENT_IDENTIFIER)))
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

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull(), eq(CLIENT_IDENTIFIER)))
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

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull(), eq(CLIENT_IDENTIFIER)))
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
        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull(), eq(CLIENT_IDENTIFIER)))
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

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), any(AmqpRetryOptions.class), isNull(), eq(CLIENT_IDENTIFIER)))
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
        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), any(AmqpRetryOptions.class), isNull(), eq(CLIENT_IDENTIFIER)))
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
     * Verifies that sending multiple message will result in calling sender.send(Message...).
     */
    @Test
    void verifyMessageOrdering() {
        // Arrange
        final ServiceBusMessage firstMessage = new ServiceBusMessage("First message " + UUID.randomUUID());
        final ServiceBusMessage secondMessage = new ServiceBusMessage("Second message " + UUID.randomUUID());
        final ServiceBusMessage thirdMessage = new ServiceBusMessage("Third message " + UUID.randomUUID());
        final ServiceBusMessage fourthMessage = new ServiceBusMessage("Fourth message " + UUID.randomUUID());
        final ServiceBusMessage fifthMessage = new ServiceBusMessage("Fifth message " + UUID.randomUUID());
        final List<ServiceBusMessage> messages = new ArrayList<>();
        messages.add(firstMessage);
        messages.add(secondMessage);
        messages.add(thirdMessage);
        messages.add(fourthMessage);
        messages.add(fifthMessage);

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions), isNull(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(sender.sendMessages(messages))
            .verifyComplete();

        // Assert
        verify(sendLink).send(messagesCaptor.capture());

        final List<Message> messagesSent = messagesCaptor.getValue();
        Assertions.assertEquals(messages.size(), messagesSent.size());

        Iterator<ServiceBusMessage> iterator = messages.iterator();
        Pattern regex = Pattern.compile("\\{(.*)\\}");
        for (Message message : messagesSent) {
            Matcher matcher = regex.matcher(message.getBody().toString());
            String content = matcher.find() ? matcher.group(1) : "";
            Assertions.assertEquals(content, iterator.next().getBody().toString());
        }
        messagesSent.forEach(message -> Assertions.assertEquals(Section.SectionType.Data, message.getBody().getType()));
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

    private void assertCommonMetricAttributes(Map<String, Object> attributes, String status) {
        assertEquals(NAMESPACE, attributes.get("hostName"));
        assertEquals(ENTITY_NAME, attributes.get("entityName"));
        assertEquals(status, attributes.get("status"));
    }

    private void assertStartOptions(StartSpanOptions startOpts, SpanKind kind, int linkCount) {
        assertEquals(kind, startOpts.getSpanKind());
        assertEquals(ENTITY_NAME, startOpts.getAttributes().get(ENTITY_PATH_KEY));
        assertEquals(NAMESPACE, startOpts.getAttributes().get(HOST_NAME_KEY));

        if (linkCount == 0) {
            assertNull(startOpts.getLinks());
        } else {
            assertEquals(linkCount, startOpts.getLinks().size());
        }
    }
}
