// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorConnectionCache;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.implementation.EventHubAmqpConnection;
import com.azure.messaging.eventhubs.implementation.EventHubConnectionProcessor;
import com.azure.messaging.eventhubs.implementation.EventHubReactorAmqpConnection;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsTracer.DIAGNOSTIC_ID_KEY;
import static com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsTracer.TRACEPARENT_KEY;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests to verify functionality of {@link EventHubProducerClient}.
 */
public class EventHubProducerClientTest {
    private static final String HOSTNAME = "my-host-name";
    private static final String EVENT_HUB_NAME = "my-event-hub-name";
    private static final String CLIENT_IDENTIFIER = "my-client-identifier";
    private static final EventHubsProducerInstrumentation DEFAULT_INSTRUMENTATION = new EventHubsProducerInstrumentation(null, null, HOSTNAME, EVENT_HUB_NAME);
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions()
        .setTryTimeout(Duration.ofSeconds(30))
        .setDelay(Duration.ofSeconds(1));
    private final MessageSerializer messageSerializer = new EventHubMessageSerializer();
    @Mock
    private AmqpSendLink sendLink;
    @Mock
    private EventHubAmqpConnection connection;
    @Mock
    private TokenCredential tokenCredential;
    @Mock
    private Runnable onClientClosed;
    @Captor
    private ArgumentCaptor<Message> singleMessageCaptor;
    @Captor
    private ArgumentCaptor<List<Message>> messagesCaptor;

    private EventHubProducerAsyncClient asyncProducer;
    private ConnectionCacheWrapper connectionProcessor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(sendLink.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));
        when(sendLink.getErrorContext()).thenReturn(new AmqpErrorContext("test-namespace"));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());
        when(sendLink.send(any(Message.class))).thenReturn(Mono.empty());
        when(sendLink.getHostname()).thenReturn(HOSTNAME);
        when(sendLink.getEntityPath()).thenReturn(EVENT_HUB_NAME);

        ConnectionOptions connectionOptions = new ConnectionOptions(HOSTNAME, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE,
            AmqpTransportType.AMQP_WEB_SOCKETS, retryOptions, ProxyOptions.SYSTEM_DEFAULTS, Schedulers.parallel(),
            new ClientOptions(), SslDomain.VerifyMode.ANONYMOUS_PEER, "test-product",
            "test-client-version");
        connectionProcessor = createConnectionProcessor(connection, connectionOptions.getRetry(), false);
        asyncProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionProcessor, retryOptions,
            messageSerializer, Schedulers.parallel(), false, onClientClosed, CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);

        when(connection.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));
        when(connection.closeAsync()).thenReturn(Mono.empty());
    }

    @AfterEach
    public void teardown() {
        Mockito.framework().clearInlineMock(this);
        sendLink = null;
        singleMessageCaptor = null;
        messagesCaptor = null;
        asyncProducer.close();
    }

    /**
     * Verifies can send a single message.
     */
    @Test
    public void sendSingleMessage() {
        // Arrange
        final EventHubProducerClient producer = new EventHubProducerClient(asyncProducer);
        final EventData eventData = new EventData("hello-world".getBytes(UTF_8));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));

        // Act
        try {
            producer.send(eventData);
        } finally {
            producer.close();
        }

        // Assert
        verify(sendLink, times(1)).send(any(Message.class));
        verify(sendLink).send(singleMessageCaptor.capture());

        final Message message = singleMessageCaptor.getValue();
        Assertions.assertEquals(Section.SectionType.Data, message.getBody().getType());
    }

    /**
     *Verifies start and end span invoked when sending a single message.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void sendStartSpanSingleMessage() {
        //Arrange
        final Tracer tracer1 = mock(Tracer.class);
        when(tracer1.isEnabled()).thenReturn(true);
        final EventHubsProducerInstrumentation instrumentation = new EventHubsProducerInstrumentation(tracer1, null, HOSTNAME, EVENT_HUB_NAME);

        final EventHubProducerAsyncClient asyncProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions, messageSerializer, Schedulers.parallel(),
            false, onClientClosed, CLIENT_IDENTIFIER, instrumentation);
        final EventHubProducerClient producer = new EventHubProducerClient(asyncProducer);
        final EventData eventData = new EventData("hello-world".getBytes(UTF_8));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));

        when(tracer1.start(eq("EventHubs.message"), any(), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(invocation.getArgument(1, StartSpanOptions.class), SpanKind.PRODUCER, 0);
                return invocation.getArgument(2, Context.class)
                    .addData(SPAN_CONTEXT_KEY, "span");
            });

        when(tracer1.start(eq("EventHubs.send"), any(), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(invocation.getArgument(1, StartSpanOptions.class), SpanKind.CLIENT, 1);
                return invocation.getArgument(2, Context.class)
                    .addData(PARENT_TRACE_CONTEXT_KEY, "trace-context");
            }
        );

        doAnswer(invocation -> {
            BiConsumer<String, String> injectContext = invocation.getArgument(0, BiConsumer.class);
            injectContext.accept("traceparent", "diag-id");
            return null;
        }).when(tracer1).injectContext(any(), any(Context.class));

        //Act
        try {
            producer.send(eventData);
            assertEquals("diag-id", eventData.getProperties().get(DIAGNOSTIC_ID_KEY));
            assertEquals("diag-id", eventData.getProperties().get(TRACEPARENT_KEY));
        } finally {
            producer.close();
        }

        //Assert
        verify(tracer1, times(1))
            .start(eq("EventHubs.send"), any(), any(Context.class));
        verify(tracer1, times(1))
            .start(eq("EventHubs.message"), any(), any(Context.class));
        verify(tracer1, times(2)).end(isNull(), isNull(), any());
        verify(tracer1, times(1)).injectContext(any(), any());

        verifyNoInteractions(onClientClosed);
    }

    /**
     * Verifies addLink method is not invoked and message/event is not stamped with context on retry (span context already present on event).
     */
    @Test
    @SuppressWarnings("unchecked")
    public void sendMessageRetrySpanTest() {
        //Arrange
        final Tracer tracer1 = mock(Tracer.class);
        when(tracer1.isEnabled()).thenReturn(true);
        final EventHubsProducerInstrumentation instrumentation = new EventHubsProducerInstrumentation(tracer1, null, HOSTNAME, EVENT_HUB_NAME);

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));

        final EventHubProducerAsyncClient asyncProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions, messageSerializer, Schedulers.parallel(),
            false, onClientClosed, CLIENT_IDENTIFIER, instrumentation);
        final EventHubProducerClient producer = new EventHubProducerClient(asyncProducer);
        final EventData eventData = new EventData("hello-world".getBytes(UTF_8));
        eventData.getProperties().put("traceparent", "traceparent");

        when(tracer1.start(eq("EventHubs.send"), any(), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(invocation.getArgument(1, StartSpanOptions.class), SpanKind.CLIENT, 1);
                return invocation.getArgument(2, Context.class)
                    .addData(PARENT_TRACE_CONTEXT_KEY, "trace-context");
            }
        );

        when(tracer1.extractContext(any())).thenAnswer(
            invocation -> {
                Function<String, String> getter = invocation.getArgument(0, Function.class);
                assertEquals("traceparent", getter.apply("traceparent"));
                return new Context(SPAN_CONTEXT_KEY, "span-context");
            }
        );

        //Act
        try {
            producer.send(eventData);
            assertFalse(eventData.getProperties().containsKey(DIAGNOSTIC_ID_KEY));
        } finally {
            producer.close();
        }

        //Assert
        verify(tracer1, times(1)).start(eq("EventHubs.send"), any(), any(Context.class));
        verify(tracer1, times(1)).end(isNull(), isNull(), any());
        verify(tracer1, times(1)).extractContext(any());
        verify(tracer1, never()).start(eq("EventHubs.message"), any(), any(Context.class));
        verify(tracer1, never()).injectContext(any(), any());
        verifyNoInteractions(onClientClosed);
    }

    /**
     * Verifies that sending an iterable of events that exceeds batch size throws exception.
     */
    @Test
    public void sendEventsExceedsBatchSize() {
        //Arrange
        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.getLinkSize()).thenReturn(Mono.just(1024));
        final EventHubProducerAsyncClient asyncProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions, messageSerializer, Schedulers.parallel(),
            false, onClientClosed, CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);
        final EventHubProducerClient producer = new EventHubProducerClient(asyncProducer);

        //Act & Assert
        final Iterable<EventData> tooManyEvents = Flux.range(0, 1024).map(number -> {
            final String contents = "event-data-" + number;
            return new EventData(contents.getBytes(UTF_8));
        }).toIterable();

        AmqpException amqpException = Assertions.assertThrows(AmqpException.class, () -> producer.send(tooManyEvents));
        Assertions.assertTrue(amqpException.getMessage().startsWith("EventData does not fit into maximum number of "
            + "batches. '1'"));
    }

    /**
     * Verifies we can send multiple messages.
     */
    @Test
    public void sendMultipleMessages() {
        // Arrange
        final int count = 4;
        final Iterable<EventData> events = Flux.range(0, count).map(number -> {
            final String contents = "event-data-" + number;
            return new EventData(contents.getBytes(UTF_8));
        }).toIterable();

        final String partitionId = "partition-id-1";
        final SendOptions options = new SendOptions().setPartitionId(partitionId);
        final EventHubProducerClient producer = new EventHubProducerClient(asyncProducer);

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(argThat(name -> name.endsWith(partitionId)),
            argThat(name -> name.endsWith(partitionId)), any(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));

        // Act
        try {
            producer.send(events, options);
        } finally {
            producer.close();
        }

        // Assert
        verify(sendLink).send(messagesCaptor.capture());

        final List<Message> messagesSent = messagesCaptor.getValue();
        Assertions.assertEquals(count, messagesSent.size());

        messagesSent.forEach(message -> Assertions.assertEquals(Section.SectionType.Data, message.getBody().getType()));

        verifyNoInteractions(onClientClosed);
    }

    /**
     * Verifies that the producer can create an {@link EventDataBatch} with the size given by the underlying AMQP send
     * link.
     */
    @Test
    public void createsEventDataBatch() {
        // Arrange
        int maxLinkSize = 1024;

        // Overhead when serializing an event, to figure out what the maximum size we can use for an event payload.
        int eventOverhead = 24;
        int maxEventPayload = maxLinkSize - eventOverhead;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(link));

        // This event is 1024 bytes when serialized.
        final EventData event = new EventData(new byte[maxEventPayload]);

        // This event will be 1025 bytes when serialized.
        final EventData tooLargeEvent = new EventData(new byte[maxEventPayload + 1]);
        final EventHubProducerClient hubProducer = new EventHubProducerClient(asyncProducer);

        // Act
        final EventDataBatch batch = hubProducer.createBatch();

        // Assert
        Assertions.assertNull(batch.getPartitionKey());
        Assertions.assertFalse(batch.tryAdd(tooLargeEvent));
        Assertions.assertTrue(batch.tryAdd(event));

        verify(link, times(1)).getLinkSize();
    }

    /**
     * Verifies that message spans are started and ended on tryAdd when creating batches to send in
     * {@link EventDataBatch}.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void startsMessageSpanOnEventBatch() {
        // Arrange
        final Tracer tracer1 = mock(Tracer.class);
        when(tracer1.isEnabled()).thenReturn(true);
        final EventHubsProducerInstrumentation instrumentation = new EventHubsProducerInstrumentation(tracer1, null, HOSTNAME, EVENT_HUB_NAME);
        final EventHubProducerAsyncClient asyncProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions, messageSerializer, Schedulers.parallel(),
            false, onClientClosed, CLIENT_IDENTIFIER, instrumentation);
        final EventHubProducerClient producer = new EventHubProducerClient(asyncProducer);

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));

        final AtomicReference<Integer> eventInd = new AtomicReference<>(0);

        when(tracer1.start(eq("EventHubs.message"), any(), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(invocation.getArgument(1, StartSpanOptions.class), SpanKind.PRODUCER, 0);
                return invocation.getArgument(2, Context.class)
                    .addData(SPAN_CONTEXT_KEY, "span");
            });

        when(tracer1.start(eq("EventHubs.send"), any(), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(invocation.getArgument(1, StartSpanOptions.class), SpanKind.CLIENT, 2);
                return invocation.getArgument(2, Context.class)
                    .addData(PARENT_TRACE_CONTEXT_KEY, "trace-context");
            }
        );

        doAnswer(invocation -> {
            BiConsumer<String, String> injectContext = invocation.getArgument(0, BiConsumer.class);
            injectContext.accept("traceparent", String.valueOf(eventInd.get()));
            return null;
        }).when(tracer1).injectContext(any(), any(Context.class));

        // Act & Assert
        try {
            final EventDataBatch batch = producer.createBatch();
            final EventData data0 = new EventData("Hello World".getBytes(UTF_8));
            Assertions.assertTrue(batch.tryAdd(data0));
            assertEquals("0", data0.getProperties().get(DIAGNOSTIC_ID_KEY));

            eventInd.set(1);
            final EventData data1 = new EventData("Hello World".getBytes(UTF_8));
            Assertions.assertTrue(batch.tryAdd(data1));
            assertEquals("1", data1.getProperties().get(DIAGNOSTIC_ID_KEY));
            producer.send(batch);
        } finally {
            producer.close();
        }

        verify(tracer1, times(2))
            .start(eq("EventHubs.message"), any(), any(Context.class));
        verify(tracer1, times(1)).start(eq("EventHubs.send"), any(), any(Context.class));
        verify(tracer1, times(2)).start(eq("EventHubs.message"), any(), any(Context.class));
        verify(tracer1, times(3)).end(isNull(), isNull(), any());
        verify(tracer1, times(2)).injectContext(any(), any());

        verifyNoInteractions(onClientClosed);
    }

    /**
     * Verifies we can create an EventDataBatch with partition key and link size.
     */
    @Test
    public void createsEventDataBatchWithPartitionKey() {
        // Arrange
        int maxBatchSize = 1024;

        // Overhead when serializing an event, to figure out what the maximum size we can use for an event payload.
        int eventOverhead = 98;
        int maxEventPayload = maxBatchSize - eventOverhead;

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));

        // This event is 1024 bytes when serialized.
        final EventData event = new EventData(new byte[maxEventPayload]);

        // No idea what the overhead for adding partition key is. But we know this will be smaller than the max size.
        final CreateBatchOptions options = new CreateBatchOptions()
            .setPartitionKey("some-key")
            .setMaximumSizeInBytes(maxBatchSize);
        final EventHubProducerClient producer = new EventHubProducerClient(asyncProducer);

        // Act
        final EventDataBatch batch = producer.createBatch(options);

        // Arrange
        Assertions.assertEquals(options.getPartitionKey(), batch.getPartitionKey());
        Assertions.assertTrue(batch.tryAdd(event));
    }

    /**
     * Verifies we can create an EventDataBatch with partition id and link size.
     */
    @Test
    public void createsEventDataBatchWithPartitionId() {
        // Arrange
        int maxBatchSize = 1024;

        // Overhead when serializing an event, to figure out what the maximum size we can use for an event payload.
        int eventOverhead = 98;
        int maxEventPayload = maxBatchSize - eventOverhead;

        String partitionId = "my-partition-id";

        // PS is the prefix when a partition sender link is created.
        when(connection.createSendLink(argThat(name -> name.endsWith(partitionId)),
            argThat(name -> name.endsWith(partitionId)), any(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));

        // This event is 1024 bytes when serialized.
        final EventData event = new EventData(new byte[maxEventPayload]);

        // No idea what the overhead for adding partition key is. But we know this will be smaller than the max size.
        final CreateBatchOptions options = new CreateBatchOptions()
            .setPartitionId(partitionId)
            .setMaximumSizeInBytes(maxBatchSize);
        final EventHubProducerClient producer = new EventHubProducerClient(asyncProducer);

        // Act
        final EventDataBatch batch = producer.createBatch(options);

        // Arrange
        Assertions.assertEquals(options.getPartitionId(), batch.getPartitionId());
        Assertions.assertTrue(batch.tryAdd(event));
    }

    /**
     * Verifies we can create an EventDataBatch with partition key and link size.
     */
    @Test
    public void payloadTooLarge() {
        // Arrange
        int maxBatchSize = 1024;

        // Overhead when serializing an event, to figure out what the maximum size we can use for an event payload.
        int eventOverhead = 24;
        int maxEventPayload = maxBatchSize - eventOverhead;

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));


        // This event is 1025 bytes when serialized.
        final EventData event = new EventData(new byte[maxEventPayload + 1]);

        // No idea what the overhead for adding partition key is. But we know this will be smaller than the max size.
        final CreateBatchOptions options = new CreateBatchOptions()
            .setMaximumSizeInBytes(maxBatchSize);
        final EventHubProducerClient producer = new EventHubProducerClient(asyncProducer);
        final EventDataBatch batch = producer.createBatch(options);

        // Act & Assert
        try {
            batch.tryAdd(event);
        } catch (AmqpException e) {
            Assertions.assertEquals(AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, e.getErrorCondition());
        }
    }

    /**
     * Verifies can create a batch on a second try if first fails with transient error.
     */
    @Test
    public void createBatchWithRetry() {
        // Arrange
        final EventHubProducerClient producer = new EventHubProducerClient(asyncProducer);

        AtomicInteger tries = new AtomicInteger();
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any(), eq(CLIENT_IDENTIFIER)))
            .thenAnswer(invocation -> {
                if (tries.incrementAndGet() == 1) {
                    return Mono.error(new AmqpException(true, "something bad", new AmqpErrorContext("test-namespace")));
                } else {
                    return Mono.just(sendLink);
                }
            });

        // Act
        try {
            producer.createBatch();
        } finally {
            producer.close();
        }

        // Assert
        verify(sendLink, times(1)).getLinkSize();
        assertEquals(2, tries.get());
    }

    private void assertStartOptions(StartSpanOptions startOpts, SpanKind kind, int linkCount) {
        assertEquals(kind, startOpts.getSpanKind());
        assertEquals(EVENT_HUB_NAME, startOpts.getAttributes().get(ENTITY_PATH_KEY));
        assertEquals(HOSTNAME, startOpts.getAttributes().get(HOST_NAME_KEY));

        if (linkCount == 0) {
            assertNull(startOpts.getLinks());
        } else {
            assertEquals(linkCount, startOpts.getLinks().size());
        }
    }

    private ConnectionCacheWrapper createConnectionProcessor(EventHubAmqpConnection connection, AmqpRetryOptions retryOptions, boolean isV2) {
        if (isV2) {
            final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);
            final ReactorConnectionCache<EventHubReactorAmqpConnection> cache = new ReactorConnectionCache<>(null, HOSTNAME, EVENT_HUB_NAME, retryPolicy, new HashMap<>(0));
            return new ConnectionCacheWrapper(cache);
        } else {
            final EventHubConnectionProcessor processor = Flux.<EventHubAmqpConnection>create(sink -> sink.next(connection))
                .subscribeWith(new EventHubConnectionProcessor(HOSTNAME, "event-hub-path", retryOptions));
            return new ConnectionCacheWrapper(processor);
        }
    }
}
