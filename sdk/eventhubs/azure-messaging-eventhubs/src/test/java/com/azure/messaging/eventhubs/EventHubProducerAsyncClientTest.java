// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.CbsAuthorizationType;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.implementation.EventHubAmqpConnection;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.apache.qpid.proton.amqp.messaging.Section;
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
import reactor.test.StepVerifier;

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
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class EventHubProducerAsyncClientTest {
    private static final String HOSTNAME = "my-host-name";
    private static final String EVENT_HUB_NAME = "my-event-hub-name";

    @Mock
    private AmqpSendLink sendLink;
    @Mock
    private AmqpSession session;
    @Mock
    private EventHubAmqpConnection connection;

    @Captor
    private ArgumentCaptor<Message> singleMessageCaptor;

    @Captor
    private ArgumentCaptor<List<Message>> messagesCaptor;

    private final MessageSerializer messageSerializer = new EventHubMessageSerializer();
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(10));
    private EventHubProducerAsyncClient producer;
    private EventHubConnection eventHubConnection;
    @Mock
    private TokenCredential tokenCredential;
    private TracerProvider tracerProvider;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        tracerProvider = new TracerProvider(Collections.emptyList());
        ConnectionOptions connectionOptions = new ConnectionOptions(HOSTNAME, "event-hub-path", tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP_WEB_SOCKETS, retryOptions,
            ProxyOptions.SYSTEM_DEFAULTS, Schedulers.parallel());
        eventHubConnection = new EventHubConnection(Mono.just(connection), connectionOptions);
        producer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME, eventHubConnection, retryOptions, tracerProvider,
            messageSerializer, false);

        when(sendLink.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));
    }

    @AfterEach
    public void teardown() {
        Mockito.framework().clearInlineMocks();
        sendLink = null;
        connection = null;
        singleMessageCaptor = null;
        messagesCaptor = null;
    }

    /**
     * Verifies that sending multiple events will result in calling producer.send(List&lt;Message&gt;).
     */
    @Test
    public void sendMultipleMessages() {
        // Arrange
        final int count = 4;
        final byte[] contents = TEST_CONTENTS.getBytes(UTF_8);
        final Flux<EventData> testData = Flux.range(0, count).flatMap(number -> {
            final EventData data = new EventData(contents);
            return Flux.just(data);
        });
        final SendOptions options = new SendOptions();

        when(connection.createSession(EVENT_HUB_NAME)).thenReturn(Mono.just(session));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(session.createProducer(argThat(name -> name.startsWith("EC")), eq(EVENT_HUB_NAME),
            eq(retryOptions.getTryTimeout()), any()))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(producer.send(testData, options))
            .verifyComplete();

        // Assert
        verify(sendLink).send(messagesCaptor.capture());

        final List<Message> messagesSent = messagesCaptor.getValue();
        Assertions.assertEquals(count, messagesSent.size());

        messagesSent.forEach(message -> Assertions.assertEquals(Section.SectionType.Data, message.getBody().getType()));
    }

    /**
     * Verifies that sending a single event data will result in calling producer.send(Message).
     */
    @Test
    public void sendSingleMessage() {
        // Arrange
        final EventData testData = new EventData(TEST_CONTENTS.getBytes(UTF_8));
        final SendOptions options = new SendOptions();

        when(connection.createSession(EVENT_HUB_NAME)).thenReturn(Mono.just(session));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(session.createProducer(argThat(name -> name.startsWith("EC")), eq(EVENT_HUB_NAME),
            eq(retryOptions.getTryTimeout()), any()))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(any(Message.class))).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(producer.send(testData, options))
            .verifyComplete();

        // Assert
        verify(sendLink, times(1)).send(any(Message.class));
        verify(sendLink).send(singleMessageCaptor.capture());

        final Message message = singleMessageCaptor.getValue();
        Assertions.assertEquals(Section.SectionType.Data, message.getBody().getType());
    }

    /**
     * Verifies that a partitioned producer cannot also send events with a partition key.
     */
    @Test
    public void partitionProducerCannotSendWithPartitionKey() {
        // Arrange
        final Flux<EventData> testData = Flux.just(
            new EventData(TEST_CONTENTS.getBytes(UTF_8)),
            new EventData(TEST_CONTENTS.getBytes(UTF_8)));

        when(connection.createSession(EVENT_HUB_NAME)).thenReturn(Mono.just(session));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(session.createProducer(argThat(name -> name.startsWith("EC")), eq(EVENT_HUB_NAME),
            eq(retryOptions.getTryTimeout()), any()))
            .thenReturn(Mono.just(sendLink));

        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        final SendOptions options = new SendOptions()
            .setPartitionKey("Some partition key")
            .setPartitionId("my-partition-id");

        // Act & Assert
        StepVerifier.create(producer.send(testData, options))
            .expectError(IllegalArgumentException.class)
            .verify(Duration.ofSeconds(10));

        verifyZeroInteractions(sendLink);
    }

    /**
     * Verifies start and end span invoked when sending a single message.
     */
    @Test
    public void sendStartSpanSingleMessage() {
        // Arrange
        final Tracer tracer1 = mock(Tracer.class);
        final List<Tracer> tracers = Collections.singletonList(tracer1);
        TracerProvider tracerProvider = new TracerProvider(tracers);
        final Flux<EventData> testData = Flux.just(
            new EventData(TEST_CONTENTS.getBytes(UTF_8)),
            new EventData(TEST_CONTENTS.getBytes(UTF_8)));

        final String partitionId = "my-partition-id";
        final SendOptions sendOptions = new SendOptions()
            .setPartitionId(partitionId);
        final EventHubProducerAsyncClient asyncProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            eventHubConnection, retryOptions, tracerProvider, messageSerializer, false);

        when(connection.createSession(argThat(name -> name.endsWith(partitionId))))
            .thenReturn(Mono.just(session));
        when(session.createProducer(
            argThat(name -> name.startsWith("PS")),
            argThat(name -> name.endsWith(partitionId)),
            eq(retryOptions.getTryTimeout()), any()))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());

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

        // Act
        StepVerifier.create(asyncProducer.send(testData, sendOptions))
            .verifyComplete();

        // Assert
        verify(tracer1, times(1))
            .start(eq("Azure.eventhubs.send"), any(), eq(ProcessKind.SEND));
        verify(tracer1, times(2))
            .start(eq("Azure.eventhubs.message"), any(), eq(ProcessKind.MESSAGE));
        verify(tracer1, times(3)).end(eq("success"), isNull(), any());
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

        final EventData eventData = new EventData(TEST_CONTENTS.getBytes(UTF_8))
            .addContext(SPAN_CONTEXT_KEY, Context.NONE);
        final EventData eventData2 =  new EventData(TEST_CONTENTS.getBytes(UTF_8))
            .addContext(SPAN_CONTEXT_KEY, Context.NONE);
        final Flux<EventData> testData = Flux.just(eventData, eventData2);

        final String partitionId = "my-partition-id";
        final SendOptions sendOptions = new SendOptions()
            .setPartitionId(partitionId);
        final EventHubProducerAsyncClient asyncProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            eventHubConnection, retryOptions, tracerProvider, messageSerializer, false);

        when(connection.createSession(argThat(name -> name.endsWith(partitionId))))
            .thenReturn(Mono.just(session));
        when(session.createProducer(
            argThat(name -> name.startsWith("PS")),
            argThat(name -> name.endsWith(partitionId)),
            eq(retryOptions.getTryTimeout()), any()))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        when(tracer1.start(eq("Azure.eventhubs.send"), any(), eq(ProcessKind.SEND))).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(PARENT_SPAN_KEY, "value");
            }
        );

        //Act
        StepVerifier.create(asyncProducer.send(testData, sendOptions)).verifyComplete();

        //Assert
        verify(tracer1, times(1))
            .start(eq("Azure.eventhubs.send"), any(), eq(ProcessKind.SEND));
        verify(tracer1, never()).start(eq("Azure.eventhubs.message"), any(), eq(ProcessKind.MESSAGE));
        verify(tracer1, never()).addLink(any());
        verify(tracer1, times(1)).end(eq("success"), isNull(), any());
    }

    /**
     * Verifies that it fails if we try to send multiple messages that cannot fit in a single message batch.
     */
    @Test
    public void sendTooManyMessages() {
        // Arrange
        int maxLinkSize = 1024;
        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        when(connection.createSession(EVENT_HUB_NAME)).thenReturn(Mono.just(session));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(session.createProducer(argThat(name -> name.startsWith("EC")), eq(EVENT_HUB_NAME),
            eq(retryOptions.getTryTimeout()), any()))
            .thenReturn(Mono.just(link));

        // We believe 20 events is enough for that EventDataBatch to be greater than max size.
        final Flux<EventData> testData = Flux.range(0, 20).flatMap(number -> {
            final EventData data = new EventData(TEST_CONTENTS.getBytes(UTF_8));
            return Flux.just(data);
        });

        // Act & Assert
        StepVerifier.create(producer.send(testData))
            .verifyErrorMatches(error -> error instanceof AmqpException
                && ((AmqpException) error).getErrorCondition() == AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED);

        verify(link, times(0)).send(any(Message.class));
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

        // Act & Assert
        StepVerifier.create(producer.createBatch())
            .assertNext(batch -> {
                Assertions.assertNull(batch.getPartitionKey());
                Assertions.assertTrue(batch.tryAdd(event));
            })
            .verifyComplete();

        StepVerifier.create(producer.createBatch())
            .assertNext(batch -> {
                Assertions.assertNull(batch.getPartitionKey());
                Assertions.assertFalse(batch.tryAdd(tooLargeEvent));
            })
            .verifyComplete();

        verify(link, times(2)).getLinkSize();
    }

    /**
     * Verifies that message spans are started and ended on tryAdd when creating batches to send in
     * {@link EventDataBatch}.
     */
    @Test
    public void startMessageSpansOnCreateBatch() {
        // Arrange
        final Tracer tracer1 = mock(Tracer.class);
        final List<Tracer> tracers = Collections.singletonList(tracer1);
        TracerProvider tracerProvider = new TracerProvider(tracers);
        final EventHubProducerAsyncClient asyncProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            eventHubConnection, retryOptions, tracerProvider, messageSerializer, false);
        final AmqpSendLink link = mock(AmqpSendLink.class);

        when(link.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));
        when(connection.createSession(EVENT_HUB_NAME)).thenReturn(Mono.just(session));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(session.createProducer(argThat(name -> name.startsWith("EC")), eq(EVENT_HUB_NAME),
            eq(retryOptions.getTryTimeout()), any()))
            .thenReturn(Mono.just(link));

        when(tracer1.start(eq("Azure.eventhubs.message"), any(), eq(ProcessKind.MESSAGE))).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(PARENT_SPAN_KEY, "value").addData(DIAGNOSTIC_ID_KEY, "value2");
            }
        );

        // Act & Assert
        StepVerifier.create(asyncProducer.createBatch())
            .assertNext(batch -> {
                Assertions.assertTrue(batch.tryAdd(new EventData("Hello World".getBytes(UTF_8))));
            })
            .verifyComplete();

        verify(tracer1, times(1))
            .start(eq("Azure.eventhubs.message"), any(), eq(ProcessKind.MESSAGE));
        verify(tracer1, times(1)).end(eq("success"), isNull(), any());
    }

    /**
     * Verifies we can create an EventDataBatch with partition key and link size.
     */
    @Test
    public void createsEventDataBatchWithPartitionKey() {
        // Arrange
        int maxLinkSize = 1024;

        // No idea what the overhead for adding partition key is. But we know this will be smaller than the max size.
        int eventPayload = maxLinkSize - 100;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));
        when(connection.createSession(EVENT_HUB_NAME)).thenReturn(Mono.just(session));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(session.createProducer(argThat(name -> name.startsWith("EC")), eq(EVENT_HUB_NAME),
            eq(retryOptions.getTryTimeout()), any()))
            .thenReturn(Mono.just(link));

        // This event is 1024 bytes when serialized.
        final EventData event = new EventData(new byte[eventPayload]);
        final CreateBatchOptions options = new CreateBatchOptions().setPartitionKey("some-key");

        // Act & Assert
        StepVerifier.create(producer.createBatch(options))
            .assertNext(batch -> {
                Assertions.assertEquals(options.getPartitionKey(), batch.getPartitionKey());
                Assertions.assertTrue(batch.tryAdd(event));
            })
            .verifyComplete();
    }

    /**
     * Verifies we cannot create an EventDataBatch if the BatchOptions size is larger than the link.
     */
    @Test
    public void createEventDataBatchWhenMaxSizeIsTooBig() {
        // Arrange
        int maxLinkSize = 1024;
        int batchSize = maxLinkSize + 10;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));
        when(connection.createSession(EVENT_HUB_NAME)).thenReturn(Mono.just(session));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(session.createProducer(argThat(name -> name.startsWith("EC")), eq(EVENT_HUB_NAME),
            eq(retryOptions.getTryTimeout()), any()))
            .thenReturn(Mono.just(link));

        // This event is 1024 bytes when serialized.
        final CreateBatchOptions options = new CreateBatchOptions().setMaximumSizeInBytes(batchSize);

        // Act & Assert
        StepVerifier.create(producer.createBatch(options))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    /**
     * Verifies that the producer can create an {@link EventDataBatch} with a given {@link
     * CreateBatchOptions#getMaximumSizeInBytes()}.
     */
    @Test
    public void createsEventDataBatchWithSize() {
        // Arrange
        int maxLinkSize = 10000;
        int batchSize = 1024;

        // Overhead when serializing an event, to figure out what the maximum size we can use for an event payload.
        int eventOverhead = 24;
        int maxEventPayload = batchSize - eventOverhead;

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
        final CreateBatchOptions options = new CreateBatchOptions().setMaximumSizeInBytes(batchSize);


        // Act & Assert
        StepVerifier.create(producer.createBatch(options))
            .assertNext(batch -> {
                Assertions.assertNull(batch.getPartitionKey());
                Assertions.assertTrue(batch.tryAdd(event));
            })
            .verifyComplete();

        StepVerifier.create(producer.createBatch(options))
            .assertNext(batch -> {
                Assertions.assertNull(batch.getPartitionKey());
                Assertions.assertFalse(batch.tryAdd(tooLargeEvent));
            })
            .verifyComplete();
    }

    @Test
    public void sendEventRequired() {
        // Arrange
        final EventData event = new EventData("Event-data");
        final SendOptions sendOptions = new SendOptions();

        StepVerifier.create(producer.send(event, null))
            .verifyError(NullPointerException.class);

        StepVerifier.create(producer.send((EventData) null, sendOptions))
            .verifyError(NullPointerException.class);
    }

    @Test
    public void sendEventIterableRequired() {
        // Arrange
        final List<EventData> event = Collections.singletonList(new EventData("Event-data"));
        final SendOptions sendOptions = new SendOptions();

        StepVerifier.create(producer.send(event, null))
            .verifyError(NullPointerException.class);

        StepVerifier.create(producer.send((Iterable<EventData>) null, sendOptions))
            .verifyError(NullPointerException.class);
    }

    @Test
    public void sendEventFluxRequired() {
        // Arrange
        final Flux<EventData> event = Flux.just(new EventData("Event-data"));
        final SendOptions sendOptions = new SendOptions();

        StepVerifier.create(producer.send(event, null))
            .verifyError(NullPointerException.class);

        StepVerifier.create(producer.send((Flux<EventData>) null, sendOptions))
            .verifyError(NullPointerException.class);
    }

    @Test
    public void batchOptionsIsCloned() {
        // Arrange
        int maxLinkSize = 1024;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));
        when(connection.createSession(EVENT_HUB_NAME)).thenReturn(Mono.just(session));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(session.createProducer(argThat(name -> name.startsWith("EC")), eq(EVENT_HUB_NAME),
            eq(retryOptions.getTryTimeout()), any()))
            .thenReturn(Mono.just(link));

        final String originalKey = "some-key";
        final CreateBatchOptions options = new CreateBatchOptions().setPartitionKey(originalKey);

        // Act & Assert
        StepVerifier.create(producer.createBatch(options))
            .assertNext(batch -> {
                options.setPartitionKey("something-else");
                Assertions.assertEquals(originalKey, batch.getPartitionKey());
            })
            .verifyComplete();
    }

    @Test
    public void sendsAnEventDataBatch() {
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

        // Act & Assert
        StepVerifier.create(producer.createBatch())
            .assertNext(batch -> {
                Assertions.assertNull(batch.getPartitionKey());
                Assertions.assertTrue(batch.tryAdd(event));
            })
            .verifyComplete();

        StepVerifier.create(producer.createBatch())
            .assertNext(batch -> {
                Assertions.assertNull(batch.getPartitionKey());
                Assertions.assertFalse(batch.tryAdd(tooLargeEvent));
            })
            .verifyComplete();

        verify(link, times(2)).getLinkSize();
    }

    /**
     * Verify we can send messages to multiple partitionIds with same sender.
     */
    @Test
    public void sendMultiplePartitions() {
        // Arrange
        final int count = 4;
        final byte[] contents = TEST_CONTENTS.getBytes(UTF_8);
        final Flux<EventData> testData = Flux.range(0, count).flatMap(number -> {
            final EventData data = new EventData(contents);
            return Flux.just(data);
        });

        final String partitionId1 = "my-partition-id";
        final String partitionId2 = "my-partition-id-2";

        final AmqpSession partition1Session = mock(AmqpSession.class);
        final AmqpSession partition2Session = mock(AmqpSession.class);
        final AmqpSendLink sendLink1 = mock(AmqpSendLink.class);
        final AmqpSendLink sendLink2 = mock(AmqpSendLink.class);

        when(connection.createSession(any())).thenAnswer(mock -> {
            final String entityPath = mock.getArgument(0, String.class);

            if (EVENT_HUB_NAME.equals(entityPath)) {
                return Mono.just(session);
            } else if (entityPath.endsWith(partitionId1)) {
                return Mono.just(partition1Session);
            } else if (entityPath.endsWith(partitionId2)) {
                return Mono.just(partition2Session);
            } else {
                return Mono.error(new IllegalArgumentException("Could not figure out entityPath: " + entityPath));
            }
        });
        when(partition1Session.createProducer(any(), argThat(name -> name.endsWith(partitionId1)), any(), any()))
            .thenReturn(Mono.just(sendLink1));
        when(partition2Session.createProducer(any(), argThat(name -> name.endsWith(partitionId2)), any(), any()))
            .thenReturn(Mono.just(sendLink2));
        when(sendLink1.send(anyList())).thenReturn(Mono.empty());
        when(sendLink1.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));
        when(sendLink2.send(anyList())).thenReturn(Mono.empty());
        when(sendLink2.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(session.createProducer(argThat(name -> name.startsWith("EC")), eq(EVENT_HUB_NAME),
            eq(retryOptions.getTryTimeout()), any()))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(producer.send(testData, new SendOptions()))
            .verifyComplete();

        StepVerifier.create(producer.send(testData, new SendOptions().setPartitionId(partitionId1)))
            .verifyComplete();

        StepVerifier.create(producer.send(testData, new SendOptions().setPartitionId(partitionId2)))
            .verifyComplete();

        // Assert
        verify(sendLink).send(messagesCaptor.capture());

        final List<Message> messagesSent = messagesCaptor.getValue();
        Assertions.assertEquals(count, messagesSent.size());

        verify(sendLink1, times(1)).send(anyList());
        verify(sendLink2, times(1)).send(anyList());
    }


    /**
     * Verifies that when we have a shared connection, the producer does not close that connection.
     */
    @Test
    public void doesNotCloseSharedConnection() {
        // Arrange
        EventHubConnection hubConnection = mock(EventHubConnection.class);
        EventHubProducerAsyncClient sharedProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            hubConnection, retryOptions, tracerProvider, messageSerializer, true);

        // Act
        sharedProducer.close();

        // Verify
        verify(hubConnection, never()).close();
    }

    /**
     * Verifies that when we have a non-shared connection, the producer closes that connection.
     */
    @Test
    public void closesDedicatedConnection() {
        // Arrange
        EventHubConnection hubConnection = mock(EventHubConnection.class);
        EventHubProducerAsyncClient dedicatedProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            hubConnection, retryOptions, tracerProvider, messageSerializer, false);

        // Act
        dedicatedProducer.close();

        // Verify
        verify(hubConnection, times(1)).close();
    }


    /**
     * Verifies that when we have a non-shared connection, the producer closes that connection. Only once.
     */
    @Test
    public void closesDedicatedConnectionOnlyOnce() {
        // Arrange
        EventHubConnection hubConnection = mock(EventHubConnection.class);
        EventHubProducerAsyncClient dedicatedProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            hubConnection, retryOptions, tracerProvider, messageSerializer, false);

        // Act
        dedicatedProducer.close();
        dedicatedProducer.close();

        // Verify
        verify(hubConnection, times(1)).close();
    }


    static final String TEST_CONTENTS = "SSLorem ipsum dolor sit amet, consectetur adipiscing elit. Donec vehicula posuere lobortis. Aliquam finibus volutpat dolor, faucibus pellentesque ipsum bibendum vitae. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Ut sit amet urna hendrerit, dapibus justo a, sodales justo. Mauris finibus augue id pulvinar congue. Nam maximus luctus ipsum, at commodo ligula euismod ac. Phasellus vitae lacus sit amet diam porta placerat. \n"
        + "Ut sodales efficitur sapien ut posuere. Morbi sed tellus est. Proin eu erat purus. Proin massa nunc, condimentum id iaculis dignissim, consectetur et odio. Cras suscipit sem eu libero aliquam tincidunt. Nullam ut arcu suscipit, eleifend velit in, cursus libero. Ut eleifend facilisis odio sit amet feugiat. Phasellus at nunc sit amet elit sagittis commodo ac in nisi. Fusce vitae aliquam quam. Integer vel nibh euismod, tempus elit vitae, pharetra est. Duis vulputate enim a elementum dignissim. Morbi dictum enim id elit scelerisque, in elementum nulla pharetra. \n"
        + "Aenean aliquet aliquet condimentum. Proin dapibus dui id libero tempus feugiat. Sed commodo ligula a lectus mattis, vitae tincidunt velit auctor. Fusce quis semper dui. Phasellus eu efficitur sem. Ut non sem sit amet enim condimentum venenatis id dictum massa. Nullam sagittis lacus a neque sodales, et ultrices arcu mattis. Aliquam erat volutpat. \n"
        + "Aenean fringilla quam elit, id mattis purus vestibulum nec. Praesent porta eros in dapibus molestie. Vestibulum orci libero, tincidunt et turpis eget, condimentum lobortis enim. Fusce suscipit ante et mauris consequat cursus nec laoreet lorem. Maecenas in sollicitudin diam, non tincidunt purus. Nunc mauris purus, laoreet eget interdum vitae, placerat a sapien. In mi risus, blandit eu facilisis nec, molestie suscipit leo. Pellentesque molestie urna vitae dui faucibus bibendum. \n"
        + "Donec quis ipsum ultricies, imperdiet ex vel, scelerisque eros. Ut at urna arcu. Vestibulum rutrum odio dolor, vitae cursus nunc pulvinar vel. Donec accumsan sapien in malesuada tempor. Maecenas in condimentum eros. Sed vestibulum facilisis massa a iaculis. Etiam et nibh felis. Donec maximus, sem quis vestibulum gravida, turpis risus congue dolor, pharetra tincidunt lectus nisi at velit.";
}
