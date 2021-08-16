// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryMode;
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
import com.azure.core.util.logging.ClientLogger;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_BUILDER_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.AZ_NAMESPACE_VALUE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class EventHubProducerAsyncClientTest {
    private static final ClientOptions CLIENT_OPTIONS = new ClientOptions();
    private static final String HOSTNAME = "my-host-name";
    private static final String EVENT_HUB_NAME = "my-event-hub-name";
    private static final String ENTITY_PATH = HOSTNAME + ".servicebus.windows.net";

    @Mock
    private AmqpSendLink sendLink;
    @Mock
    private AmqpSendLink sendLink2;
    @Mock
    private AmqpSendLink sendLink3;

    @Mock
    private EventHubAmqpConnection connection;
    @Mock
    private EventHubAmqpConnection connection2;
    @Mock
    private EventHubAmqpConnection connection3;
    @Mock
    private TokenCredential tokenCredential;
    @Mock
    private Runnable onClientClosed;

    @Captor
    private ArgumentCaptor<Message> singleMessageCaptor;
    @Captor
    private ArgumentCaptor<List<Message>> messagesCaptor;

    private final ClientLogger logger = new ClientLogger(EventHubProducerAsyncClient.class);
    private final MessageSerializer messageSerializer = new EventHubMessageSerializer();
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions()
        .setDelay(Duration.ofMillis(500))
        .setMode(AmqpRetryMode.FIXED)
        .setTryTimeout(Duration.ofSeconds(10));
    private final DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();
    private final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private EventHubProducerAsyncClient producer;
    private EventHubConnectionProcessor connectionProcessor;
    private TracerProvider tracerProvider;
    private ConnectionOptions connectionOptions;
    private final Scheduler testScheduler = Schedulers.newElastic("test");

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        MockitoAnnotations.initMocks(this);

        tracerProvider = new TracerProvider(Collections.emptyList());
        connectionOptions = new ConnectionOptions(HOSTNAME, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE,
            AmqpTransportType.AMQP_WEB_SOCKETS, retryOptions, ProxyOptions.SYSTEM_DEFAULTS, testScheduler,
            CLIENT_OPTIONS, SslDomain.VerifyMode.VERIFY_PEER_NAME,
            "client-product", "client-version");

        when(connection.getEndpointStates()).thenReturn(endpointProcessor);
        endpointSink.next(AmqpEndpointState.ACTIVE);

        when(connection.closeAsync()).thenReturn(Mono.empty());

        connectionProcessor = Mono.fromCallable(() -> connection).repeat(10).subscribeWith(
            new EventHubConnectionProcessor(connectionOptions.getFullyQualifiedNamespace(),
                "event-hub-path", connectionOptions.getRetry()));
        producer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionProcessor, retryOptions,
            tracerProvider, messageSerializer, testScheduler, false, onClientClosed);

        when(sendLink.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));
        when(sendLink2.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));
        when(sendLink3.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));
    }

    @AfterEach
    void teardown(TestInfo testInfo) {
        testScheduler.dispose();
        Mockito.framework().clearInlineMocks();
        Mockito.reset(sendLink);
        Mockito.reset(connection);
        singleMessageCaptor = null;
        messagesCaptor = null;
    }

    /**
     * Verifies that sending multiple events will result in calling producer.send(List&lt;Message&gt;).
     */
    @Test
    void sendMultipleMessages() {
        // Arrange
        final int count = 4;
        final byte[] contents = TEST_CONTENTS.getBytes(UTF_8);
        final Flux<EventData> testData = Flux.range(0, count).flatMap(number -> {
            final EventData data = new EventData(contents);
            return Flux.just(data);
        });
        final SendOptions options = new SendOptions();

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
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
    void sendSingleMessage() {
        // Arrange
        final EventData testData = new EventData(TEST_CONTENTS.getBytes(UTF_8));
        final SendOptions options = new SendOptions();

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
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
     * Verifies that sending a single event data will not throw an {@link IllegalStateException} if we block because
     * we are publishing on an elastic scheduler.
     */
    @Test
    void sendSingleMessageWithBlock() throws InterruptedException {
        // Arrange
        final Mono<Instant> saveAction = Mono.delay(Duration.ofMillis(500))
            .then(Mono.fromCallable(() -> {
                logger.info("This is saved.");
                return Instant.now();
            }));
        final EventData testData = new EventData(TEST_CONTENTS.getBytes(UTF_8));
        final SendOptions options = new SendOptions();
        final Semaphore semaphore = new Semaphore(1);
        // In our actual client builder, we allow this.
        final EventHubProducerAsyncClient flexibleProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions, tracerProvider, messageSerializer, testScheduler,
            false, onClientClosed);

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink));

        when(sendLink.send(any(Message.class))).thenReturn(Mono.<Void>empty().publishOn(Schedulers.single()));
        Assertions.assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        // Act
        final Mono<Instant> sendMono = flexibleProducer.send(testData, options).thenReturn(Instant.now());

        sendMono.subscribe(e -> {
            logger.info("Saving message: {}", e);

            // This block here should throw an IllegalStateException if we aren't publishing correctly.
            final Instant result = saveAction.block(Duration.ofSeconds(3));

            Assertions.assertNotNull(result);
            logger.info("Message saved: {}", result);
            semaphore.release();
        });

        // Assert
        Assertions.assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        verify(sendLink).send(any(Message.class));
        verify(sendLink).send(singleMessageCaptor.capture());

        final Message message = singleMessageCaptor.getValue();
        Assertions.assertEquals(Section.SectionType.Data, message.getBody().getType());

        verifyNoInteractions(onClientClosed);
    }

    /**
     * Verifies that a partitioned producer cannot also send events with a partition key.
     */
    @Test
    void partitionProducerCannotSendWithPartitionKey() {
        // Arrange
        final Flux<EventData> testData = Flux.just(
            new EventData(TEST_CONTENTS.getBytes(UTF_8)),
            new EventData(TEST_CONTENTS.getBytes(UTF_8)));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink));

        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        final SendOptions options = new SendOptions()
            .setPartitionKey("Some partition key")
            .setPartitionId("my-partition-id");

        // Act & Assert
        StepVerifier.create(producer.send(testData, options))
            .expectError(IllegalArgumentException.class)
            .verify(Duration.ofSeconds(10));

        verifyNoInteractions(sendLink);
    }

    /**
     * Verifies start and end span invoked when sending a single message.
     */
    @Test
    void sendStartSpanSingleMessage() {
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
            connectionProcessor, retryOptions, tracerProvider, messageSerializer, Schedulers.parallel(),
            false, onClientClosed);

        when(connection.createSendLink(
            argThat(name -> name.endsWith(partitionId)), argThat(name -> name.endsWith(partitionId)), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink));

        when(sendLink.send(anyList())).thenReturn(Mono.empty());

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

        // Act
        StepVerifier.create(asyncProducer.send(testData, sendOptions))
            .verifyComplete();

        // Assert
        verify(tracer1, times(1))
            .start(eq("EventHubs.send"), any(), eq(ProcessKind.SEND));
        verify(tracer1, times(2))
            .start(eq("EventHubs.message"), any(), eq(ProcessKind.MESSAGE));
        verify(tracer1, times(3)).end(eq("success"), isNull(), any());

        verifyNoInteractions(onClientClosed);
    }

    /**
     * Verifies send, message and addLink spans are only invoked once even for multiple retry attempts to send the
     * message.
     */
    @Test
    void sendMessageRetrySpanTest() {
        //Arrange
        final Tracer tracer1 = mock(Tracer.class);
        final List<Tracer> tracers = Collections.singletonList(tracer1);
        TracerProvider tracerProvider = new TracerProvider(tracers);

        producer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionProcessor, retryOptions,
            tracerProvider, messageSerializer, Schedulers.parallel(), false, onClientClosed);

        final String failureKey = "fail";
        final EventData testData = new EventData("test");
        testData.getProperties().put(failureKey, "true");

        when(tracer1.start(eq("EventHubs.send"), any(), eq(ProcessKind.SEND))).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                assertEquals(passed.getData(AZ_TRACING_NAMESPACE_KEY).get(), AZ_NAMESPACE_VALUE);
                return passed.addData(PARENT_SPAN_KEY, "value").addData(HOST_NAME_KEY, "value2");
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

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());
        final Throwable error = new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "Test-message",
            new AmqpErrorContext("test-namespace"));

        // Send a transient error to attempt retry.
        when(sendLink.send(argThat((Message message) ->
            message.getApplicationProperties().getValue().containsKey(failureKey))))
            .thenReturn(Mono.error(error))
            .thenReturn(Mono.error(error))
            .thenReturn(Mono.empty());

        StepVerifier.create(producer.send(testData)).verifyComplete();

        //Assert
        verify(tracer1, times(1))
            .start(eq("EventHubs.send"), any(), eq(ProcessKind.SEND));
        verify(tracer1, times(1))
            .start(eq("EventHubs.message"), any(), eq(ProcessKind.MESSAGE));
        verify(tracer1, times(1)).addLink(any());
        verify(tracer1, times(2)).end(eq("success"), isNull(), any());

        verifyNoMoreInteractions(onClientClosed);
    }

    /**
     * Verifies that it fails if we try to send multiple messages that cannot fit in a single message batch.
     */
    @Test
    void sendTooManyMessages() {
        // Arrange
        int maxLinkSize = 1024;
        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
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
    void createsEventDataBatch() {
        // Arrange
        int maxLinkSize = 1024;

        // Overhead when serializing an event, to figure out what the maximum size we can use for an event payload.
        int eventOverhead = 24;
        int maxEventPayload = maxLinkSize - eventOverhead;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
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
     * Verifies that message spans are started and ended on tryAdd when creating batches to send in {@link
     * EventDataBatch}.
     */
    @Test
    void startMessageSpansOnCreateBatch() {
        // Arrange
        final Tracer tracer1 = mock(Tracer.class);
        final List<Tracer> tracers = Collections.singletonList(tracer1);
        TracerProvider tracerProvider = new TracerProvider(tracers);
        final EventHubProducerAsyncClient asyncProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions, tracerProvider, messageSerializer, Schedulers.parallel(),
            false, onClientClosed);
        final AmqpSendLink link = mock(AmqpSendLink.class);

        when(link.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));
        when(link.getHostname()).thenReturn(HOSTNAME);
        when(link.getEntityPath()).thenReturn(ENTITY_PATH);

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(link));

        when(tracer1.start(eq("EventHubs.message"), any(), eq(ProcessKind.MESSAGE))).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                assertEquals(passed.getData(AZ_TRACING_NAMESPACE_KEY).get(), AZ_NAMESPACE_VALUE);
                assertEquals(passed.getData(ENTITY_PATH_KEY).get(), ENTITY_PATH);
                assertEquals(passed.getData(HOST_NAME_KEY).get(), HOSTNAME);

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
            .start(eq("EventHubs.message"), any(), eq(ProcessKind.MESSAGE));
        verify(tracer1, times(1)).end(eq("success"), isNull(), any());

        verifyNoInteractions(onClientClosed);
    }

    /**
     * Verifies we can create an EventDataBatch with partition key and link size.
     */
    @Test
    void createsEventDataBatchWithPartitionKey() {
        // Arrange
        int maxLinkSize = 1024;

        // No idea what the overhead for adding partition key is. But we know this will be smaller than the max size.
        int eventPayload = maxLinkSize - 100;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
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
    void createEventDataBatchWhenMaxSizeIsTooBig() {
        // Arrange
        int maxLinkSize = 1024;
        int batchSize = maxLinkSize + 10;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
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
    void createsEventDataBatchWithSize() {
        // Arrange
        int maxLinkSize = 10000;
        int batchSize = 1024;

        // Overhead when serializing an event, to figure out what the maximum size we can use for an event payload.
        int eventOverhead = 24;
        int maxEventPayload = batchSize - eventOverhead;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
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
    void sendEventRequired() {
        // Arrange
        final EventData event = new EventData("Event-data");
        final SendOptions sendOptions = new SendOptions();

        StepVerifier.create(producer.send(event, null))
            .verifyError(NullPointerException.class);

        StepVerifier.create(producer.send((EventData) null, sendOptions))
            .verifyError(NullPointerException.class);
    }

    @Test
    void sendEventIterableRequired() {
        // Arrange
        final List<EventData> event = Collections.singletonList(new EventData("Event-data"));
        final SendOptions sendOptions = new SendOptions();

        StepVerifier.create(producer.send(event, null))
            .verifyError(NullPointerException.class);

        StepVerifier.create(producer.send((Iterable<EventData>) null, sendOptions))
            .verifyError(NullPointerException.class);
    }

    @Test
    void sendEventFluxRequired() {
        // Arrange
        final Flux<EventData> event = Flux.just(new EventData("Event-data"));
        final SendOptions sendOptions = new SendOptions();

        StepVerifier.create(producer.send(event, null))
            .verifyError(NullPointerException.class);

        StepVerifier.create(producer.send((Flux<EventData>) null, sendOptions))
            .verifyError(NullPointerException.class);
    }

    @Test
    void batchOptionsIsCloned() {
        // Arrange
        int maxLinkSize = 1024;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
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
    void sendsAnEventDataBatch() {
        // Arrange
        int maxLinkSize = 1024;

        // Overhead when serializing an event, to figure out what the maximum size we can use for an event payload.
        int eventOverhead = 24;
        int maxEventPayload = maxLinkSize - eventOverhead;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
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
    void sendMultiplePartitions() {
        // Arrange
        final int count = 4;
        final byte[] contents = TEST_CONTENTS.getBytes(UTF_8);
        final Flux<EventData> testData = Flux.range(0, count).flatMap(number -> {
            final EventData data = new EventData(contents);
            return Flux.just(data);
        });

        final String partitionId1 = "my-partition-id";
        final String partitionId2 = "my-partition-id-2";

        when(sendLink2.send(anyList())).thenReturn(Mono.empty());
        when(sendLink2.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));
        when(sendLink3.send(anyList())).thenReturn(Mono.empty());
        when(sendLink3.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(anyString(), anyString(), any())).thenAnswer(mock -> {
            final String entityPath = mock.getArgument(1, String.class);

            if (EVENT_HUB_NAME.equals(entityPath)) {
                return Mono.just(sendLink);
            } else if (entityPath.endsWith(partitionId1)) {
                return Mono.just(sendLink3);
            } else if (entityPath.endsWith(partitionId2)) {
                return Mono.just(sendLink2);
            } else {
                return Mono.error(new IllegalArgumentException("Could not figure out entityPath: " + entityPath));
            }
        });
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

        verify(sendLink3, times(1)).send(anyList());
        verify(sendLink2, times(1)).send(anyList());
    }

    /**
     * Verifies that when we have a shared connection, the producer does not close that connection.
     */
    @Test
    void doesNotCloseSharedConnection() {
        // Arrange
        EventHubConnectionProcessor hubConnection = mock(EventHubConnectionProcessor.class);
        EventHubProducerAsyncClient sharedProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            hubConnection, retryOptions, tracerProvider, messageSerializer, Schedulers.parallel(),
            true, onClientClosed);

        // Act
        sharedProducer.close();

        // Verify
        verify(hubConnection, never()).dispose();
        verify(onClientClosed).run();
    }

    /**
     * Verifies that when we have a non-shared connection, the producer closes that connection.
     */
    @Test
    void closesDedicatedConnection() {
        // Arrange
        EventHubConnectionProcessor hubConnection = mock(EventHubConnectionProcessor.class);
        EventHubProducerAsyncClient dedicatedProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            hubConnection, retryOptions, tracerProvider, messageSerializer, Schedulers.parallel(),
            false, onClientClosed);

        // Act
        dedicatedProducer.close();

        // Verify
        verify(hubConnection, times(1)).dispose();
        verifyNoInteractions(onClientClosed);
    }

    /**
     * Verifies that when we have a non-shared connection, the producer closes that connection. Only once.
     */
    @Test
    void closesDedicatedConnectionOnlyOnce() {
        // Arrange
        EventHubConnectionProcessor hubConnection = mock(EventHubConnectionProcessor.class);
        EventHubProducerAsyncClient dedicatedProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            hubConnection, retryOptions, tracerProvider, messageSerializer, Schedulers.parallel(),
            false, onClientClosed);

        // Act
        dedicatedProducer.close();
        dedicatedProducer.close();

        // Verify
        verify(hubConnection, times(1)).dispose();
        verifyNoInteractions(onClientClosed);
    }

    /**
     * Verifies that another link is received and we can continue publishing events on a transient failure.
     */
    @Test
    void reopensOnFailure() {
        // Arrange
        when(connection.getEndpointStates()).thenReturn(endpointProcessor);
        endpointSink.next(AmqpEndpointState.ACTIVE);

        EventHubAmqpConnection[] connections = new EventHubAmqpConnection[]{
            connection, connection2, connection3
        };
        connectionProcessor = Flux.<EventHubAmqpConnection>create(sink -> {
            final AtomicInteger count = new AtomicInteger();
            sink.onRequest(request -> {
                for (int i = 0; i < request; i++) {
                    final int current = count.getAndIncrement();
                    final int index = current % connections.length;
                    sink.next(connections[index]);
                }
            });
        }).subscribeWith(
            new EventHubConnectionProcessor(EVENT_HUB_NAME, connectionOptions.getFullyQualifiedNamespace(),
                connectionOptions.getRetry()));
        producer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionProcessor, retryOptions,
            tracerProvider, messageSerializer, Schedulers.parallel(), false, onClientClosed);

        final int count = 4;
        final byte[] contents = TEST_CONTENTS.getBytes(UTF_8);
        final Flux<EventData> testData = Flux.range(0, count).flatMap(number -> {
            final EventData data = new EventData(contents);
            return Flux.just(data);
        });
        final EventData testData2 = new EventData("test");

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        final DirectProcessor<AmqpEndpointState> connectionState2 = DirectProcessor.create();
        when(connection2.getEndpointStates()).thenReturn(connectionState2);
        when(connection2.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink2));
        when(sendLink2.send(any(Message.class))).thenReturn(Mono.empty());

        final DirectProcessor<AmqpEndpointState> connectionState3 = DirectProcessor.create();
        when(connection3.getEndpointStates()).thenReturn(connectionState3);
        when(connection3.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink3));
        when(sendLink3.send(anyList())).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(producer.send(testData))
            .verifyComplete();

        // Send in an error signal like a server busy condition.
        endpointSink.error(new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "Test-message",
            new AmqpErrorContext("test-namespace")));

        StepVerifier.create(producer.send(testData2))
            .verifyComplete();

        // Assert
        verify(sendLink).send(messagesCaptor.capture());
        final List<Message> messagesSent = messagesCaptor.getValue();
        Assertions.assertEquals(count, messagesSent.size());

        verify(sendLink2, times(1)).send(any(Message.class));
        verifyNoInteractions(sendLink3);

        verifyNoInteractions(onClientClosed);
    }

    /**
     * Verifies that on a non-transient failure, no more event hub connections are recreated and we can not send events.
     * An error should be propagated back to us.
     */
    @Test
    void closesOnNonTransientFailure() {
        // Arrange
        when(connection.getEndpointStates()).thenReturn(endpointProcessor);
        endpointSink.next(AmqpEndpointState.ACTIVE);

        EventHubAmqpConnection[] connections = new EventHubAmqpConnection[]{
            connection, connection2, connection3
        };
        connectionProcessor = Flux.<EventHubAmqpConnection>create(sink -> {
            final AtomicInteger count = new AtomicInteger();
            sink.onRequest(request -> {
                for (int i = 0; i < request; i++) {
                    final int current = count.getAndIncrement();
                    final int index = current % connections.length;
                    sink.next(connections[index]);
                }
            });
        }).subscribeWith(
            new EventHubConnectionProcessor(connectionOptions.getFullyQualifiedNamespace(),
                EVENT_HUB_NAME, connectionOptions.getRetry()));
        producer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionProcessor, retryOptions,
            tracerProvider, messageSerializer, Schedulers.parallel(), false, onClientClosed);

        final int count = 4;
        final byte[] contents = TEST_CONTENTS.getBytes(UTF_8);
        final Flux<EventData> testData = Flux.range(0, count).flatMap(number -> {
            final EventData data = new EventData(contents);
            return Flux.just(data);
        });
        final EventData testData2 = new EventData("test");

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        final DirectProcessor<AmqpEndpointState> connectionState2 = DirectProcessor.create();
        when(connection2.getEndpointStates()).thenReturn(connectionState2);
        when(connection2.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink2));
        when(sendLink2.send(any(Message.class))).thenReturn(Mono.empty());

        final AmqpException nonTransientError = new AmqpException(false, AmqpErrorCondition.UNAUTHORIZED_ACCESS,
            "Test unauthorized access", new AmqpErrorContext("test-namespace"));

        // Act
        StepVerifier.create(producer.send(testData))
            .verifyComplete();

        // Send in an error signal like authorization failure.
        endpointSink.error(nonTransientError);

        StepVerifier.create(producer.send(testData2))
            .expectErrorSatisfies(error -> {
                Assertions.assertTrue(error instanceof AmqpException);

                final AmqpException actual = (AmqpException) error;
                Assertions.assertEquals(nonTransientError.isTransient(), actual.isTransient());
                Assertions.assertEquals(nonTransientError.getContext(), actual.getContext());
                Assertions.assertEquals(nonTransientError.getErrorCondition(), actual.getErrorCondition());
                Assertions.assertEquals(nonTransientError.getMessage(), actual.getMessage());
            })
            .verify(Duration.ofSeconds(10));

        // Assert
        verify(sendLink).send(messagesCaptor.capture());
        final List<Message> messagesSent = messagesCaptor.getValue();
        Assertions.assertEquals(count, messagesSent.size());

        verifyNoInteractions(sendLink2);
        verifyNoInteractions(sendLink3);
        verifyNoInteractions(onClientClosed);
    }

    /**
     * Verifies that we can resend a message when a transient error occurs.
     */
    @Test
    void resendMessageOnTransientLinkFailure() {
        // Arrange
        when(connection.getEndpointStates()).thenReturn(endpointProcessor);
        endpointSink.next(AmqpEndpointState.ACTIVE);

        EventHubAmqpConnection[] connections = new EventHubAmqpConnection[]{connection, connection2};
        connectionProcessor = Flux.<EventHubAmqpConnection>create(sink -> {
            final AtomicInteger count = new AtomicInteger();
            sink.onRequest(request -> {
                for (int i = 0; i < request; i++) {
                    final int current = count.getAndIncrement();
                    final int index = current % connections.length;
                    sink.next(connections[index]);
                }
            });
        }).subscribeWith(
            new EventHubConnectionProcessor(connectionOptions.getFullyQualifiedNamespace(),
                EVENT_HUB_NAME, connectionOptions.getRetry()));
        producer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionProcessor, retryOptions,
            tracerProvider, messageSerializer, Schedulers.parallel(), false, onClientClosed);

        final int count = 4;
        final byte[] contents = TEST_CONTENTS.getBytes(UTF_8);
        final Flux<EventData> testData = Flux.range(0, count).flatMap(number -> {
            final EventData data = new EventData(contents);
            return Flux.just(data);
        });

        final String failureKey = "fail";
        final EventData testData2 = new EventData("test");
        testData2.getProperties().put(failureKey, "true");

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        // Send a transient error, and close the original link, if we get a message that contains the "failureKey".
        // This simulates when a link is closed.
        when(sendLink.send(argThat((Message message) -> {
            return message.getApplicationProperties().getValue().containsKey(failureKey);
        }))).thenAnswer(mock -> {
            final Throwable error = new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "Test-message",
                new AmqpErrorContext("test-namespace"));

            endpointSink.error(error);
            return Mono.error(error);
        });

        final DirectProcessor<AmqpEndpointState> connectionState2 = DirectProcessor.create();
        when(connection2.getEndpointStates()).thenReturn(connectionState2);
        when(connection2.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink2));
        when(sendLink2.send(any(Message.class))).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(producer.send(testData))
            .verifyComplete();

        StepVerifier.create(producer.send(testData2))
            .verifyComplete();

        // Assert
        verify(sendLink).send(messagesCaptor.capture());
        final List<Message> messagesSent = messagesCaptor.getValue();
        Assertions.assertEquals(count, messagesSent.size());

        verify(sendLink2, times(1)).send(any(Message.class));
        verifyNoInteractions(sendLink3);

        verifyNoInteractions(onClientClosed);
    }

    private static final String TEST_CONTENTS = "SSLorem ipsum dolor sit amet, consectetur adipiscing elit. Donec "
        + "vehicula posuere lobortis. Aliquam finibus volutpat dolor, faucibus pellentesque ipsum bibendum vitae. "
        + "Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Ut sit amet "
        + "urna hendrerit, dapibus justo a, sodales justo. Mauris finibus augue id pulvinar congue. Nam maximus "
        + "luctus ipsum, at commodo ligula euismod ac. Phasellus vitae lacus sit amet diam porta placerat. \n"
        + "Ut sodales efficitur sapien ut posuere. Morbi sed tellus est. Proin eu erat purus. Proin massa nunc, "
        + "condimentum id iaculis dignissim, consectetur et odio. Cras suscipit sem eu libero aliquam tincidunt. "
        + "Nullam ut arcu suscipit, eleifend velit in, cursus libero. Ut eleifend facilisis odio sit amet feugiat. "
        + "Phasellus at nunc sit amet elit sagittis commodo ac in nisi. Fusce vitae aliquam quam. Integer vel nibh "
        + "euismod, tempus elit vitae, pharetra est. Duis vulputate enim a elementum dignissim. Morbi dictum enim id "
        + "elit scelerisque, in elementum nulla pharetra. \n"
        + "Aenean aliquet aliquet condimentum. Proin dapibus dui id libero tempus feugiat. Sed commodo ligula a "
        + "lectus mattis, vitae tincidunt velit auctor. Fusce quis semper dui. Phasellus eu efficitur sem. Ut non sem"
        + " sit amet enim condimentum venenatis id dictum massa. Nullam sagittis lacus a neque sodales, et ultrices "
        + "arcu mattis. Aliquam erat volutpat. \n"
        + "Aenean fringilla quam elit, id mattis purus vestibulum nec. Praesent porta eros in dapibus molestie. "
        + "Vestibulum orci libero, tincidunt et turpis eget, condimentum lobortis enim. Fusce suscipit ante et mauris"
        + " consequat cursus nec laoreet lorem. Maecenas in sollicitudin diam, non tincidunt purus. Nunc mauris "
        + "purus, laoreet eget interdum vitae, placerat a sapien. In mi risus, blandit eu facilisis nec, molestie "
        + "suscipit leo. Pellentesque molestie urna vitae dui faucibus bibendum. \n"
        + "Donec quis ipsum ultricies, imperdiet ex vel, scelerisque eros. Ut at urna arcu. Vestibulum rutrum odio "
        + "dolor, vitae cursus nunc pulvinar vel. Donec accumsan sapien in malesuada tempor. Maecenas in condimentum "
        + "eros. Sed vestibulum facilisis massa a iaculis. Etiam et nibh felis. Donec maximus, sem quis vestibulum "
        + "gravida, turpis risus congue dolor, pharetra tincidunt lectus nisi at velit.";
}
