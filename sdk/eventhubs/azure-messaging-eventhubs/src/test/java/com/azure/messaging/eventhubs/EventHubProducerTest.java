// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.amqp.exception.ErrorContext;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.implementation.EventHubConnection;
import com.azure.messaging.eventhubs.models.BatchOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.Message;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests to verify functionality of {@link EventHubProducer}.
 */
public class EventHubProducerTest {
    private static final String HOSTNAME = "my-host-name";
    private static final String EVENT_HUB_NAME = "my-event-hub-name";

    @Mock
    private AmqpSendLink sendLink;
    @Mock
    private AmqpSession session;
    @Mock
    private EventHubConnection connection;
    @Captor
    private ArgumentCaptor<Message> singleMessageCaptor;
    @Captor
    private ArgumentCaptor<List<Message>> messagesCaptor;

    private EventHubAsyncProducerClient asyncProducer;
    private RetryOptions retryOptions = new RetryOptions().setTryTimeout(Duration.ofSeconds(30));
    private MessageSerializer messageSerializer = new EventHubMessageSerializer();
    private EventHubLinkProvider linkProvider;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(sendLink.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));
        when(sendLink.getErrorContext()).thenReturn(new ErrorContext("test-namespace"));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());
        when(sendLink.send(any(Message.class))).thenReturn(Mono.empty());

        final TracerProvider tracerProvider = new TracerProvider(Collections.emptyList());
        linkProvider = new EventHubLinkProvider(Mono.just(connection), HOSTNAME, retryOptions);
        asyncProducer = new EventHubAsyncProducerClient(EVENT_HUB_NAME, linkProvider, retryOptions, tracerProvider,
            messageSerializer);
    }

    @After
    public void teardown() {
        Mockito.framework().clearInlineMocks();
        sendLink = null;
        singleMessageCaptor = null;
        messagesCaptor = null;
    }

    /**
     * Verifies can send a single message.
     */
    @Test
    public void sendSingleMessage() {
        // Arrange
        final EventHubProducer producer = new EventHubProducer(asyncProducer, retryOptions.getTryTimeout());
        final EventData eventData = new EventData("hello-world".getBytes(UTF_8));

        when(connection.createSession(EVENT_HUB_NAME)).thenReturn(Mono.just(session));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(session.createProducer(argThat(name -> name.startsWith("EC")), eq(EVENT_HUB_NAME),
            eq(retryOptions.getTryTimeout()), any()))
            .thenReturn(Mono.just(sendLink));

        // Act
        producer.send(eventData);

        // Assert
        verify(sendLink, times(1)).send(any(Message.class));
        verify(sendLink).send(singleMessageCaptor.capture());

        final Message message = singleMessageCaptor.getValue();
        Assert.assertEquals(Section.SectionType.Data, message.getBody().getType());
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
        final EventHubAsyncProducerClient asyncProducer = new EventHubAsyncProducerClient(EVENT_HUB_NAME, linkProvider,
            retryOptions, tracerProvider, messageSerializer);
        final EventHubProducer producer = new EventHubProducer(asyncProducer, retryOptions.getTryTimeout());
        final EventData eventData = new EventData("hello-world".getBytes(UTF_8));

        when(connection.createSession(EVENT_HUB_NAME)).thenReturn(Mono.just(session));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(session.createProducer(argThat(name -> name.startsWith("EC")), eq(EVENT_HUB_NAME),
            eq(retryOptions.getTryTimeout()), any()))
            .thenReturn(Mono.just(sendLink));

        when(tracer1.start(eq("Azure.eventhubs.send"), any(), eq(ProcessKind.SEND))).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(PARENT_SPAN_KEY, "value");
            }
        );
        when(tracer1.start(eq("Azure.eventhubs.message"), any(), eq(ProcessKind.MESSAGE))).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(PARENT_SPAN_KEY, "value").addData(DIAGNOSTIC_ID_KEY, "value2");
            }
        );
        //Act
        producer.send(eventData);

        //Assert
        verify(tracer1, times(1))
            .start(eq("Azure.eventhubs.send"), any(), eq(ProcessKind.SEND));
        verify(tracer1, times(1))
            .start(eq("Azure.eventhubs.message"), any(), eq(ProcessKind.MESSAGE));
        verify(tracer1, times(2)).end(eq("success"), isNull(), any());
    }

    /**
     * Verifies start and end span invoked when linking a single message on retry.
     */
    @Test
    public void sendMessageAddLink() {
        //Arrange
        final Tracer tracer1 = mock(Tracer.class);
        final List<Tracer> tracers = Collections.singletonList(tracer1);
        TracerProvider tracerProvider = new TracerProvider(tracers);

        when(connection.createSession(EVENT_HUB_NAME)).thenReturn(Mono.just(session));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(session.createProducer(argThat(name -> name.startsWith("EC")), eq(EVENT_HUB_NAME),
            eq(retryOptions.getTryTimeout()), any()))
            .thenReturn(Mono.just(sendLink));

        final EventHubAsyncProducerClient asyncProducer = new EventHubAsyncProducerClient(EVENT_HUB_NAME, linkProvider,
            retryOptions, tracerProvider, messageSerializer);
        final EventHubProducer producer = new EventHubProducer(asyncProducer, retryOptions.getTryTimeout());
        final EventData eventData = new EventData("hello-world".getBytes(UTF_8), new Context(SPAN_CONTEXT_KEY, Context.NONE));

        when(tracer1.start(eq("Azure.eventhubs.send"), any(), eq(ProcessKind.SEND))).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(PARENT_SPAN_KEY, "value");
            }
        );

        //Act
        producer.send(eventData);

        //Assert
        verify(tracer1, times(1)).start(eq("Azure.eventhubs.send"), any(), eq(ProcessKind.SEND));
        verify(tracer1, never()).start(eq("Azure.eventhubs.message"), any(), eq(ProcessKind.MESSAGE));
        verify(tracer1, times(1)).addLink(any());
        verify(tracer1, times(1)).end(eq("success"), isNull(), any());
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
        final EventHubProducer producer = new EventHubProducer(asyncProducer, retryOptions.getTryTimeout());

        when(connection.createSession(argThat(name -> name.endsWith(partitionId))))
            .thenReturn(Mono.just(session));

        when(session.createProducer(argThat(name -> name.startsWith("PS")), argThat(name -> name.endsWith(partitionId)),
            eq(retryOptions.getTryTimeout()), any()))
            .thenReturn(Mono.just(sendLink));

        // Act
        producer.send(events, options);

        // Assert
        verify(sendLink).send(messagesCaptor.capture());

        final List<Message> messagesSent = messagesCaptor.getValue();
        Assert.assertEquals(count, messagesSent.size());

        messagesSent.forEach(message -> Assert.assertEquals(Section.SectionType.Data, message.getBody().getType()));
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
        when(connection.createSession(EVENT_HUB_NAME)).thenReturn(Mono.just(session));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(session.createProducer(argThat(name -> name.startsWith("EC")), eq(EVENT_HUB_NAME),
            eq(retryOptions.getTryTimeout()), any()))
            .thenReturn(Mono.just(link));

        // This event is 1024 bytes when serialized.
        final EventData event = new EventData(new byte[maxEventPayload]);

        // This event will be 1025 bytes when serialized.
        final EventData tooLargeEvent = new EventData(new byte[maxEventPayload + 1]);
        final EventHubProducer hubProducer = new EventHubProducer(asyncProducer, retryOptions.getTryTimeout());

        // Act
        final EventDataBatch batch = hubProducer.createBatch();

        // Assert
        Assert.assertNull(batch.getPartitionKey());
        Assert.assertFalse(batch.tryAdd(tooLargeEvent));
        Assert.assertTrue(batch.tryAdd(event));

        verify(link, times(1)).getLinkSize();
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

        when(connection.createSession(EVENT_HUB_NAME)).thenReturn(Mono.just(session));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(session.createProducer(argThat(name -> name.startsWith("EC")), eq(EVENT_HUB_NAME),
            eq(retryOptions.getTryTimeout()), any()))
            .thenReturn(Mono.just(sendLink));

        // This event is 1024 bytes when serialized.
        final EventData event = new EventData(new byte[maxEventPayload]);

        // No idea what the overhead for adding partition key is. But we know this will be smaller than the max size.
        final BatchOptions options = new BatchOptions()
            .setPartitionKey("some-key")
            .setMaximumSizeInBytes(maxBatchSize);
        final EventHubProducer producer = new EventHubProducer(asyncProducer, retryOptions.getTryTimeout());

        // Act
        final EventDataBatch batch = producer.createBatch(options);

        // Arrange
        Assert.assertEquals(options.getPartitionKey(), batch.getPartitionKey());
        Assert.assertTrue(batch.tryAdd(event));
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
        when(connection.createSession(argThat(name -> name.endsWith(partitionId)))).thenReturn(Mono.just(session));

        // PS is the prefix when a partition sender link is created.
        when(session.createProducer(argThat(name -> name.startsWith("PS")), argThat(name -> name.endsWith(partitionId)),
            eq(retryOptions.getTryTimeout()), any()))
            .thenReturn(Mono.just(sendLink));

        // This event is 1024 bytes when serialized.
        final EventData event = new EventData(new byte[maxEventPayload]);

        // No idea what the overhead for adding partition key is. But we know this will be smaller than the max size.
        final BatchOptions options = new BatchOptions()
            .setPartitionId(partitionId)
            .setMaximumSizeInBytes(maxBatchSize);
        final EventHubProducer producer = new EventHubProducer(asyncProducer, retryOptions.getTryTimeout());

        // Act
        final EventDataBatch batch = producer.createBatch(options);

        // Arrange
        Assert.assertEquals(options.getPartitionId(), batch.getPartitionId());
        Assert.assertTrue(batch.tryAdd(event));
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

        when(connection.createSession(EVENT_HUB_NAME)).thenReturn(Mono.just(session));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(session.createProducer(argThat(name -> name.startsWith("EC")), eq(EVENT_HUB_NAME),
            eq(retryOptions.getTryTimeout()), any()))
            .thenReturn(Mono.just(sendLink));

        // This event is 1025 bytes when serialized.
        final EventData event = new EventData(new byte[maxEventPayload + 1]);

        // No idea what the overhead for adding partition key is. But we know this will be smaller than the max size.
        final BatchOptions options = new BatchOptions()
            .setMaximumSizeInBytes(maxBatchSize);
        final EventHubProducer producer = new EventHubProducer(asyncProducer, retryOptions.getTryTimeout());
        final EventDataBatch batch = producer.createBatch(options);

        // Act & Assert
        try {
            batch.tryAdd(event);
        } catch (AmqpException e) {
            Assert.assertEquals(ErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, e.getErrorCondition());
        }
    }
}
