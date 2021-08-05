// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.implementation.EventHubAmqpConnection;
import com.azure.messaging.eventhubs.implementation.EventHubConnectionProcessor;
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
import java.util.Collections;
import java.util.List;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_BUILDER_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.AZ_NAMESPACE_VALUE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(30));
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
    private EventHubConnectionProcessor connectionProcessor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(sendLink.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));
        when(sendLink.getErrorContext()).thenReturn(new AmqpErrorContext("test-namespace"));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());
        when(sendLink.send(any(Message.class))).thenReturn(Mono.empty());

        final TracerProvider tracerProvider = new TracerProvider(Collections.emptyList());

        ConnectionOptions connectionOptions = new ConnectionOptions(HOSTNAME, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE,
            AmqpTransportType.AMQP_WEB_SOCKETS, retryOptions, ProxyOptions.SYSTEM_DEFAULTS, Schedulers.parallel(),
            new ClientOptions(), SslDomain.VerifyMode.ANONYMOUS_PEER, "test-product",
            "test-client-version");
        connectionProcessor = Flux.<EventHubAmqpConnection>create(sink -> sink.next(connection))
            .subscribeWith(new EventHubConnectionProcessor(connectionOptions.getFullyQualifiedNamespace(),
                "event-hub-path", connectionOptions.getRetry()));
        asyncProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionProcessor, retryOptions,
            tracerProvider, messageSerializer, Schedulers.parallel(), false, onClientClosed);

        when(connection.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));
        when(connection.closeAsync()).thenReturn(Mono.empty());
    }

    @AfterEach
    public void teardown() {
        Mockito.framework().clearInlineMocks();
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
        final EventHubProducerClient producer = new EventHubProducerClient(asyncProducer, retryOptions.getTryTimeout());
        final EventData eventData = new EventData("hello-world".getBytes(UTF_8));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any()))
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
    public void sendStartSpanSingleMessage() {
        //Arrange
        final Tracer tracer1 = mock(Tracer.class);
        final List<Tracer> tracers = Collections.singletonList(tracer1);
        final TracerProvider tracerProvider = new TracerProvider(tracers);
        final EventHubProducerAsyncClient asyncProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions, tracerProvider, messageSerializer, Schedulers.parallel(),
            false, onClientClosed);
        final EventHubProducerClient producer = new EventHubProducerClient(asyncProducer, retryOptions.getTryTimeout());
        final EventData eventData = new EventData("hello-world".getBytes(UTF_8));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any()))
            .thenReturn(Mono.just(sendLink));

        when(tracer1.start(eq("EventHubs.send"), any(), eq(ProcessKind.SEND))).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                assertEquals(passed.getData(AZ_TRACING_NAMESPACE_KEY).get(), AZ_NAMESPACE_VALUE);
                return passed.addData(PARENT_SPAN_KEY, "value");
            }
        );
        when(tracer1.start(eq("EventHubs.message"), any(), eq(ProcessKind.MESSAGE))).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                assertEquals(passed.getData(AZ_TRACING_NAMESPACE_KEY).get(), AZ_NAMESPACE_VALUE);
                return passed.addData(PARENT_SPAN_KEY, "value").addData(DIAGNOSTIC_ID_KEY, "value2");
            }
        );
        when(tracer1.getSharedSpanBuilder(eq("EventHubs.send"), any())).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(SPAN_BUILDER_KEY, "value");
            }
        );

        //Act
        try {
            producer.send(eventData);
        } finally {
            producer.close();
        }

        //Assert
        verify(tracer1, times(1))
            .start(eq("EventHubs.send"), any(), eq(ProcessKind.SEND));
        verify(tracer1, times(1))
            .start(eq("EventHubs.message"), any(), eq(ProcessKind.MESSAGE));
        verify(tracer1, times(2)).end(eq("success"), isNull(), any());

        verifyNoInteractions(onClientClosed);
    }

    /**
     * Verifies addLink method is not invoked and message/event is not stamped with context on retry (span context already present on event).
     */
    @Test
    public void sendMessageRetrySpanTest() {
        //Arrange
        final Tracer tracer1 = mock(Tracer.class);
        final List<Tracer> tracers = Collections.singletonList(tracer1);
        TracerProvider tracerProvider = new TracerProvider(tracers);

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any()))
            .thenReturn(Mono.just(sendLink));

        final EventHubProducerAsyncClient asyncProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions, tracerProvider, messageSerializer, Schedulers.parallel(), false, onClientClosed);
        final EventHubProducerClient producer = new EventHubProducerClient(asyncProducer, retryOptions.getTryTimeout());
        final EventData eventData = new EventData("hello-world".getBytes(UTF_8))
            .addContext(SPAN_CONTEXT_KEY, Context.NONE);

        when(tracer1.start(eq("EventHubs.send"), any(), eq(ProcessKind.SEND))).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                assertEquals(passed.getData(AZ_TRACING_NAMESPACE_KEY).get(), AZ_NAMESPACE_VALUE);
                return passed.addData(PARENT_SPAN_KEY, "value");
            }
        );

        when(tracer1.getSharedSpanBuilder(eq("EventHubs.send"), any())).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(SPAN_BUILDER_KEY, "value");
            }
        );

        //Act
        try {
            producer.send(eventData);
        } finally {
            producer.close();
        }

        //Assert
        verify(tracer1, times(1)).start(eq("EventHubs.send"), any(), eq(ProcessKind.SEND));
        verify(tracer1, never()).start(eq("EventHubs.message"), any(), eq(ProcessKind.MESSAGE));
        verify(tracer1, times(1)).addLink(any());
        verify(tracer1, times(1)).end(eq("success"), isNull(), any());

        verifyNoInteractions(onClientClosed);
    }

    /**
     * Verifies that sending an iterable of events that exceeds batch size throws exception.
     */
    @Test
    public void sendEventsExceedsBatchSize() {
        //Arrange
        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any()))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.getLinkSize()).thenReturn(Mono.just(1024));
        TracerProvider tracerProvider = new TracerProvider(Collections.emptyList());
        final EventHubProducerAsyncClient asyncProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions, tracerProvider, messageSerializer, Schedulers.parallel(), false, onClientClosed);
        final EventHubProducerClient producer = new EventHubProducerClient(asyncProducer, retryOptions.getTryTimeout());

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
        final EventHubProducerClient producer = new EventHubProducerClient(asyncProducer, retryOptions.getTryTimeout());

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(argThat(name -> name.endsWith(partitionId)),
            argThat(name -> name.endsWith(partitionId)), any()))
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
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any()))
            .thenReturn(Mono.just(link));

        // This event is 1024 bytes when serialized.
        final EventData event = new EventData(new byte[maxEventPayload]);

        // This event will be 1025 bytes when serialized.
        final EventData tooLargeEvent = new EventData(new byte[maxEventPayload + 1]);
        final EventHubProducerClient hubProducer = new EventHubProducerClient(asyncProducer, retryOptions.getTryTimeout());

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
    public void startsMessageSpanOnEventBatch() {
        // Arrange
        final Tracer tracer1 = mock(Tracer.class);
        final List<Tracer> tracers = Collections.singletonList(tracer1);
        final TracerProvider tracerProvider = new TracerProvider(tracers);
        final EventHubProducerAsyncClient asyncProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions, tracerProvider, messageSerializer, Schedulers.parallel(),
            false, onClientClosed);
        final EventHubProducerClient producer = new EventHubProducerClient(asyncProducer, retryOptions.getTryTimeout());

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any()))
            .thenReturn(Mono.just(sendLink));

        when(tracer1.start(eq("EventHubs.message"), any(), eq(ProcessKind.MESSAGE))).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(PARENT_SPAN_KEY, "value").addData(DIAGNOSTIC_ID_KEY, "value2");
            }
        );

        // Act & Assert
        try {
            final EventDataBatch batch = producer.createBatch();
            Assertions.assertTrue(batch.tryAdd(new EventData("Hello World".getBytes(UTF_8))));
            Assertions.assertTrue(batch.tryAdd(new EventData("Test World".getBytes(UTF_8))));
        } finally {
            producer.close();
        }


        verify(tracer1, times(2))
            .start(eq("EventHubs.message"), any(), eq(ProcessKind.MESSAGE));
        verify(tracer1, times(2)).end(eq("success"), isNull(), any());

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
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any()))
            .thenReturn(Mono.just(sendLink));

        // This event is 1024 bytes when serialized.
        final EventData event = new EventData(new byte[maxEventPayload]);

        // No idea what the overhead for adding partition key is. But we know this will be smaller than the max size.
        final CreateBatchOptions options = new CreateBatchOptions()
            .setPartitionKey("some-key")
            .setMaximumSizeInBytes(maxBatchSize);
        final EventHubProducerClient producer = new EventHubProducerClient(asyncProducer, retryOptions.getTryTimeout());

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
            argThat(name -> name.endsWith(partitionId)), any()))
            .thenReturn(Mono.just(sendLink));

        // This event is 1024 bytes when serialized.
        final EventData event = new EventData(new byte[maxEventPayload]);

        // No idea what the overhead for adding partition key is. But we know this will be smaller than the max size.
        final CreateBatchOptions options = new CreateBatchOptions()
            .setPartitionId(partitionId)
            .setMaximumSizeInBytes(maxBatchSize);
        final EventHubProducerClient producer = new EventHubProducerClient(asyncProducer, retryOptions.getTryTimeout());

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
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any()))
            .thenReturn(Mono.just(sendLink));


        // This event is 1025 bytes when serialized.
        final EventData event = new EventData(new byte[maxEventPayload + 1]);

        // No idea what the overhead for adding partition key is. But we know this will be smaller than the max size.
        final CreateBatchOptions options = new CreateBatchOptions()
            .setMaximumSizeInBytes(maxBatchSize);
        final EventHubProducerClient producer = new EventHubProducerClient(asyncProducer, retryOptions.getTryTimeout());
        final EventDataBatch batch = producer.createBatch(options);

        // Act & Assert
        try {
            batch.tryAdd(event);
        } catch (AmqpException e) {
            Assertions.assertEquals(AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, e.getErrorCondition());
        }
    }
}
