// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryMode;
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
import com.azure.core.test.utils.metrics.TestCounter;
import com.azure.core.test.utils.metrics.TestMeasurement;
import com.azure.core.test.utils.metrics.TestMeter;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.implementation.EventHubAmqpConnection;
import com.azure.messaging.eventhubs.implementation.EventHubConnectionProcessor;
import com.azure.messaging.eventhubs.implementation.EventHubManagementNode;
import com.azure.messaging.eventhubs.implementation.EventHubReactorAmqpConnection;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsTracer.DIAGNOSTIC_ID_KEY;
import static com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsTracer.TRACEPARENT_KEY;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
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
    private static final String CLIENT_IDENTIFIER = "my-client-identifier";
    private static final String ENTITY_PATH = HOSTNAME + Configuration.getGlobalConfiguration()
        .get("AZURE_EVENTHUBS_ENDPOINT_SUFFIX", ".servicebus.windows.net");
    private static final ClientLogger LOGGER = new ClientLogger(EventHubProducerAsyncClient.class);
    private static final EventHubsProducerInstrumentation DEFAULT_INSTRUMENTATION = new EventHubsProducerInstrumentation(null, null, HOSTNAME, EVENT_HUB_NAME);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    @Mock
    private AmqpSendLink sendLink;
    @Mock
    private AmqpSendLink sendLink2;
    @Mock
    private AmqpSendLink sendLink3;

    @Mock
    private EventHubReactorAmqpConnection connection;
    @Mock
    private EventHubReactorAmqpConnection connection2;
    @Mock
    private EventHubReactorAmqpConnection connection3;
    @Mock
    private TokenCredential tokenCredential;
    @Mock
    private Runnable onClientClosed;

    @Captor
    private ArgumentCaptor<Message> singleMessageCaptor;
    @Captor
    private ArgumentCaptor<List<Message>> messagesCaptor;

    private final MessageSerializer messageSerializer = new EventHubMessageSerializer();
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions()
        .setDelay(Duration.ofMillis(500))
        .setMode(AmqpRetryMode.FIXED)
        .setTryTimeout(Duration.ofSeconds(10));
    private final Sinks.Many<AmqpEndpointState> endpointStates = Sinks.many().multicast().onBackpressureBuffer();
    private EventHubProducerAsyncClient producer;
    private ConnectionCacheWrapper connectionProcessor;
    private ConnectionOptions connectionOptions;
    private final Scheduler testScheduler = Schedulers.newBoundedElastic(10, 10, "test");

    @BeforeEach
    void setup(TestInfo testInfo) {
        MockitoAnnotations.initMocks(this);

        connectionOptions = new ConnectionOptions(HOSTNAME, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE,
            AmqpTransportType.AMQP_WEB_SOCKETS, retryOptions, ProxyOptions.SYSTEM_DEFAULTS, testScheduler,
            CLIENT_OPTIONS, SslDomain.VerifyMode.VERIFY_PEER_NAME,
            "client-product", "client-version");

        when(connection.getEndpointStates()).thenReturn(endpointStates.asFlux());
        endpointStates.emitNext(AmqpEndpointState.ACTIVE, Sinks.EmitFailureHandler.FAIL_FAST);

        when(connection.closeAsync()).thenReturn(Mono.empty());

        connectionProcessor = createConnectionProcessor(connection, retryOptions, false);
        producer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionProcessor, retryOptions,
            messageSerializer, testScheduler, false, onClientClosed, CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);

        when(sendLink.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));
        when(sendLink2.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));
        when(sendLink3.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));
    }

    @AfterEach
    void teardown(TestInfo testInfo) {
        testScheduler.dispose();
        Mockito.framework().clearInlineMock(this);
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
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(producer.send(testData, options))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

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
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));

        when(sendLink.send(any(Message.class))).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(producer.send(testData, options))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

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
                LOGGER.info("This is saved.");
                return Instant.now();
            }));
        final EventData testData = new EventData(TEST_CONTENTS.getBytes(UTF_8));
        final SendOptions options = new SendOptions();
        final Semaphore semaphore = new Semaphore(1);
        // In our actual client builder, we allow this.
        final EventHubProducerAsyncClient flexibleProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions, messageSerializer, testScheduler,
            false, onClientClosed, CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));

        when(sendLink.send(any(Message.class))).thenReturn(Mono.<Void>empty().publishOn(Schedulers.single()));
        Assertions.assertTrue(semaphore.tryAcquire(30, TimeUnit.SECONDS));

        // Act
        final Mono<Instant> sendMono = flexibleProducer.send(testData, options).thenReturn(Instant.now());

        sendMono.subscribe(e -> {
            LOGGER.info("Saving message: {}", e);

            // This block here should throw an IllegalStateException if we aren't publishing correctly.
            final Instant result = saveAction.block(Duration.ofSeconds(3));

            Assertions.assertNotNull(result);
            LOGGER.info("Message saved: {}", result);
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
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
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
    @SuppressWarnings("unchecked")
    void sendStartSpanSingleMessage() {
        final Flux<EventData> testData = Flux.just(
            new EventData(TEST_CONTENTS.getBytes(UTF_8)));
        final SendOptions sendOptions = new SendOptions();

        // Arrange
        final Tracer tracer1 = mock(Tracer.class);
        when(tracer1.isEnabled()).thenReturn(true);
        final EventHubsProducerInstrumentation instrumentation = new EventHubsProducerInstrumentation(tracer1, null, HOSTNAME, EVENT_HUB_NAME);
        final EventHubProducerAsyncClient asyncProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions, messageSerializer, Schedulers.parallel(),
            false, onClientClosed, CLIENT_IDENTIFIER, instrumentation);

        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.getHostname()).thenReturn(HOSTNAME);
        when(sendLink.getEntityPath()).thenReturn(EVENT_HUB_NAME);
        when(sendLink.send(anyList())).thenReturn(Mono.empty());
        when(sendLink.send(any(Message.class))).thenReturn(Mono.empty());

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

        // Act
        StepVerifier.create(asyncProducer.send(testData, sendOptions))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        //Assert
        verify(tracer1, times(1))
            .start(eq("EventHubs.send"), any(), any(Context.class));
        verify(tracer1, times(1))
            .start(eq("EventHubs.message"), any(), any(Context.class));
        verify(tracer1, times(2)).end(isNull(), isNull(), any());
        verify(tracer1, times(1)).injectContext(any(), any());

        verifyNoInteractions(onClientClosed);
        assertEquals(2, testData.blockFirst().getProperties().size());
        assertEquals("diag-id", testData.blockFirst().getProperties().get(DIAGNOSTIC_ID_KEY));
        assertEquals("diag-id", testData.blockFirst().getProperties().get(TRACEPARENT_KEY));
    }

    /**
     * Does not attempt to modify unmodifiable properties
     */
    @Test
    void sendSingleWithUnmodifiableProperties() {
        final Flux<EventData> testData = Flux.just(fakeReceivedMessage());

        // Arrange
        final Tracer tracer1 = mock(Tracer.class);
        when(tracer1.isEnabled()).thenReturn(true);
        final EventHubsProducerInstrumentation instrumentation = new EventHubsProducerInstrumentation(tracer1, null, HOSTNAME, EVENT_HUB_NAME);
        final EventHubProducerAsyncClient asyncProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions, messageSerializer, Schedulers.parallel(),
            false, onClientClosed, CLIENT_IDENTIFIER, instrumentation);

        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any(), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.getHostname()).thenReturn(HOSTNAME);
        when(sendLink.getEntityPath()).thenReturn(EVENT_HUB_NAME);
        when(sendLink.send(anyList())).thenReturn(Mono.empty());
        when(sendLink.send(any(Message.class))).thenReturn(Mono.empty());

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

        // Act
        StepVerifier.create(asyncProducer.send(testData))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        //Assert
        verify(tracer1, times(1))
            .start(eq("EventHubs.send"), any(), any(Context.class));
        verify(tracer1, times(1))
            .start(eq("EventHubs.message"), any(), any(Context.class));
        verify(tracer1, times(1)).end(eq("failed to inject context into EventData"), isNull(), any());
        verify(tracer1, times(1)).end(isNull(), isNull(), any());
        verify(tracer1, never()).injectContext(any(), any());

        verifyNoInteractions(onClientClosed);
        assertEquals(1, testData.blockFirst().getProperties().size());
    }

    /**
     * Verifies tracing for getEventHubsProperties and getPartitionProperties
     */
    @Test
    void startSpanForGetProperties() {
        // Arrange
        final Tracer tracer1 = mock(Tracer.class);
        when(tracer1.isEnabled()).thenReturn(true);
        final EventHubsProducerInstrumentation instrumentation = new EventHubsProducerInstrumentation(tracer1, null, HOSTNAME, EVENT_HUB_NAME);
        final EventHubProducerAsyncClient asyncProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions, messageSerializer, Schedulers.parallel(),
            false, onClientClosed, CLIENT_IDENTIFIER, instrumentation);

        EventHubProperties ehProperties = new EventHubProperties(EVENT_HUB_NAME, Instant.now(), new String[]{"0"});
        PartitionProperties partitionProperties = new PartitionProperties(EVENT_HUB_NAME, "0",
            1L, 2L, OffsetDateTime.now().toString(), Instant.now(), false);
        EventHubManagementNode managementNode = mock(EventHubManagementNode.class);
        when(connection.getManagementNode()).thenReturn(Mono.just(managementNode));
        when(managementNode.getEventHubProperties()).thenReturn(Mono.just(ehProperties));
        when(managementNode.getPartitionProperties(anyString())).thenReturn(Mono.just(partitionProperties));

        when(tracer1.start(eq("EventHubs.getPartitionProperties"), any(), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(invocation.getArgument(1, StartSpanOptions.class), SpanKind.CLIENT, 0);
                return invocation.getArgument(2, Context.class).addData(PARENT_TRACE_CONTEXT_KEY, "getPartitionProperties");
            }
        );
        when(tracer1.start(eq("EventHubs.getEventHubProperties"), any(), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(invocation.getArgument(1, StartSpanOptions.class), SpanKind.CLIENT, 0);
                return invocation.getArgument(2, Context.class).addData(PARENT_TRACE_CONTEXT_KEY, "getEventHubProperties");
            }
        );

        // Act
        StepVerifier.create(asyncProducer.getEventHubProperties())
            .consumeNextWith(p -> assertSame(ehProperties, p))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        StepVerifier.create(asyncProducer.getPartitionProperties("0"))
            .consumeNextWith(p -> assertSame(partitionProperties, p))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        //Assert
        verify(tracer1, times(1))
            .start(eq("EventHubs.getPartitionProperties"), any(), any(Context.class));
        verify(tracer1, times(1))
            .start(eq("EventHubs.getEventHubProperties"), any(), any(Context.class));
        verify(tracer1, times(2)).end(isNull(), isNull(), any());

        verifyNoInteractions(onClientClosed);
    }

    /**
     * Verifies that getPartitionProperties and getEventHubProperties retry transient errors
     */
    @Test
    void getPropertiesWithRetries() {
        // Arrange
        final AmqpRetryOptions lowDelayOptions = new AmqpRetryOptions()
            .setDelay(Duration.ofMillis(200))
            .setMode(AmqpRetryMode.FIXED)
            .setTryTimeout(Duration.ofMillis(100));

        final EventHubProducerAsyncClient asyncProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, lowDelayOptions, messageSerializer, Schedulers.parallel(),
            false, onClientClosed, CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);

        final EventHubProperties ehProperties = new EventHubProperties(EVENT_HUB_NAME, Instant.now(), new String[]{"0"});
        PartitionProperties partitionProperties = new PartitionProperties(EVENT_HUB_NAME, "0",
            1L, 2L, OffsetDateTime.now().toString(), Instant.now(), false);
        EventHubManagementNode managementNode = mock(EventHubManagementNode.class);

        AtomicInteger tryCount = new AtomicInteger();
        when(connection.getManagementNode()).thenAnswer(invocation -> {
            int count = tryCount.getAndIncrement();
            if (count == 0) {
                return Mono.error(new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "Test-message",
                    new AmqpErrorContext("test-namespace")));
            } else if (count == 1) {
                // Simulate a timeout on the second attempt, test should never wait for it to end anyway.
                return Mono.delay(Duration.ofSeconds(100)).then(Mono.error(new RuntimeException("should never happen")));
            } else {
                return Mono.just(managementNode);
            }
        });
        when(managementNode.getPartitionProperties(anyString())).thenReturn(Mono.just(partitionProperties));
        when(managementNode.getEventHubProperties()).thenReturn(Mono.just(ehProperties));

        // Assert
        tryCount.set(0);
        StepVerifier.create(asyncProducer.getPartitionProperties("0"))
            .consumeNextWith(p -> assertSame(partitionProperties, p))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
        assertEquals(3, tryCount.get());

        tryCount.set(0);
        StepVerifier.create(asyncProducer.getEventHubProperties())
            .consumeNextWith(eh -> assertSame(ehProperties, eh))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        assertEquals(3, tryCount.get());
    }

    /**
     * Verifies send, message and addLink spans are only invoked once even for multiple retry attempts to send the
     * message.
     */
    @Test
    @SuppressWarnings("unchecked")
    void sendMessageRetrySpanTest() {
        //Arrange
        final Tracer tracer1 = mock(Tracer.class);
        when(tracer1.isEnabled()).thenReturn(true);
        final EventHubsProducerInstrumentation instrumentation = new EventHubsProducerInstrumentation(tracer1, null, HOSTNAME, EVENT_HUB_NAME);
        producer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionProcessor, retryOptions,
            messageSerializer, Schedulers.parallel(), false, onClientClosed, CLIENT_IDENTIFIER, instrumentation);

        final String failureKey = "fail";
        final EventData testData = new EventData("test")
            .addContext(SPAN_CONTEXT_KEY, "span-context");
        testData.getProperties().put(failureKey, "true");

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

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
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

        producer.send(testData).block();
        assertFalse(testData.getProperties().containsKey(DIAGNOSTIC_ID_KEY));

        //Assert
        verify(tracer1, times(1)).start(eq("EventHubs.send"), any(), any(Context.class));
        verify(tracer1, never()).start(eq("EventHubs.message"), any(), any(Context.class));
        verify(tracer1, times(1)).end(isNull(), isNull(), any());
        verify(tracer1, never()).extractContext(any());
        verify(tracer1, never()).injectContext(any(), any());
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
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(link));

        // We believe 20 events is enough for that EventDataBatch to be greater than max size.
        final Flux<EventData> testData = Flux.range(0, 20).flatMap(number -> {
            final EventData data = new EventData(TEST_CONTENTS.getBytes(UTF_8));
            return Flux.just(data);
        });

        // Act & Assert
        StepVerifier.create(producer.send(testData))
            .expectErrorMatches(error -> error instanceof AmqpException
                && ((AmqpException) error).getErrorCondition() == AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED)
            .verify(DEFAULT_TIMEOUT);

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
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
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
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        StepVerifier.create(producer.createBatch())
            .assertNext(batch -> {
                Assertions.assertNull(batch.getPartitionKey());
                Assertions.assertFalse(batch.tryAdd(tooLargeEvent));
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        verify(link, times(2)).getLinkSize();
    }

    /**
     * Verifies that message spans are started and ended on tryAdd when creating batches to send in {@link
     * EventDataBatch}.
     */
    @Test
    @SuppressWarnings("unchecked")
    void startMessageSpansOnCreateBatch() {
        // Arrange
        final Tracer tracer1 = mock(Tracer.class);
        when(tracer1.isEnabled()).thenReturn(true);
        final EventHubsProducerInstrumentation instrumentation = new EventHubsProducerInstrumentation(tracer1, null, HOSTNAME, EVENT_HUB_NAME);
        final EventHubProducerAsyncClient asyncProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions, messageSerializer, Schedulers.parallel(),
            false, onClientClosed, CLIENT_IDENTIFIER, instrumentation);
        final AmqpSendLink link = mock(AmqpSendLink.class);

        when(link.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));
        when(link.getHostname()).thenReturn(HOSTNAME);
        when(link.getEntityPath()).thenReturn(ENTITY_PATH);

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.getHostname()).thenReturn(HOSTNAME);
        when(sendLink.getEntityPath()).thenReturn(EVENT_HUB_NAME);
        when(sendLink.send(anyList())).thenReturn(Mono.empty());
        when(sendLink.send(any(Message.class))).thenReturn(Mono.empty());

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
        StepVerifier.create(asyncProducer.createBatch()
                .flatMap(batch -> {
                    final EventData data0 = new EventData("Hello World".getBytes(UTF_8));
                    Assertions.assertTrue(batch.tryAdd(data0));
                    assertEquals("0", data0.getProperties().get(DIAGNOSTIC_ID_KEY));

                    eventInd.set(1);
                    final EventData data1 = new EventData("Hello World".getBytes(UTF_8));
                    Assertions.assertTrue(batch.tryAdd(data1));
                    assertEquals("1", data1.getProperties().get(DIAGNOSTIC_ID_KEY));
                    return asyncProducer.send(batch);
                }))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

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
    void createsEventDataBatchWithPartitionKey() {
        // Arrange
        int maxLinkSize = 1024;

        // No idea what the overhead for adding partition key is. But we know this will be smaller than the max size.
        int eventPayload = maxLinkSize - 100;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
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
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
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
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(link));

        // This event is 1024 bytes when serialized.
        final CreateBatchOptions options = new CreateBatchOptions().setMaximumSizeInBytes(batchSize);

        // Act & Assert
        StepVerifier.create(producer.createBatch(options))
            .expectError(IllegalArgumentException.class)
            .verify(DEFAULT_TIMEOUT);
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
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
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
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        StepVerifier.create(producer.createBatch(options))
            .assertNext(batch -> {
                Assertions.assertNull(batch.getPartitionKey());
                Assertions.assertFalse(batch.tryAdd(tooLargeEvent));
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    void sendEventRequired() {
        // Arrange
        final EventData event = new EventData("Event-data");
        final SendOptions sendOptions = new SendOptions();

        StepVerifier.create(producer.send(event, null))
            .expectError(NullPointerException.class)
            .verify(DEFAULT_TIMEOUT);

        StepVerifier.create(producer.send((EventData) null, sendOptions))
            .expectError(NullPointerException.class)
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    void sendEventIterableRequired() {
        // Arrange
        final List<EventData> event = Collections.singletonList(new EventData("Event-data"));
        final SendOptions sendOptions = new SendOptions();

        StepVerifier.create(producer.send(event, null))
            .expectError(NullPointerException.class)
            .verify(DEFAULT_TIMEOUT);

        StepVerifier.create(producer.send((Iterable<EventData>) null, sendOptions))
            .expectError(NullPointerException.class)
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    void sendEventFluxRequired() {
        // Arrange
        final Flux<EventData> event = Flux.just(new EventData("Event-data"));
        final SendOptions sendOptions = new SendOptions();

        StepVerifier.create(producer.send(event, null))
            .expectError(NullPointerException.class)
            .verify(DEFAULT_TIMEOUT);

        StepVerifier.create(producer.send((Flux<EventData>) null, sendOptions))
            .expectError(NullPointerException.class)
            .verify(DEFAULT_TIMEOUT);
    }

    @Test
    void batchOptionsIsCloned() {
        // Arrange
        int maxLinkSize = 1024;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(link));

        final String originalKey = "some-key";
        final CreateBatchOptions options = new CreateBatchOptions().setPartitionKey(originalKey);

        // Act & Assert
        StepVerifier.create(producer.createBatch(options))
            .assertNext(batch -> {
                options.setPartitionKey("something-else");
                Assertions.assertEquals(originalKey, batch.getPartitionKey());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
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
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
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
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        StepVerifier.create(producer.createBatch())
            .assertNext(batch -> {
                Assertions.assertNull(batch.getPartitionKey());
                Assertions.assertFalse(batch.tryAdd(tooLargeEvent));
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        verify(link, times(2)).getLinkSize();
    }

    @Test
    void sendsAnEventDataBatchWithMetrics() {
        String eventHub1 = EVENT_HUB_NAME + "1";
        String eventHub2 = EVENT_HUB_NAME + "2";

        when(connection.createSendLink(eq(eventHub1), eq(eventHub1), any(), any())).thenReturn(Mono.just(sendLink));
        when(connection.createSendLink(eq(eventHub2), eq(eventHub2), any(), any())).thenReturn(Mono.just(sendLink2));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());
        when(sendLink.getHostname()).thenReturn(HOSTNAME);
        when(sendLink.getEntityPath()).thenReturn(eventHub1);
        when(sendLink.send(any(Message.class))).thenReturn(Mono.empty());

        when(sendLink2.send(anyList())).thenReturn(Mono.empty());
        when(sendLink2.getHostname()).thenReturn(HOSTNAME);
        when(sendLink2.getEntityPath()).thenReturn(eventHub1);
        when(sendLink2.send(any(Message.class))).thenReturn(Mono.empty());

        TestMeter meter = new TestMeter();
        EventHubsProducerInstrumentation instrumentation1 = new EventHubsProducerInstrumentation(null, meter, HOSTNAME, eventHub1);
        EventHubProducerAsyncClient producer1 = new EventHubProducerAsyncClient(HOSTNAME, eventHub1,
            connectionProcessor, retryOptions,
            messageSerializer, testScheduler, false, onClientClosed, CLIENT_IDENTIFIER, instrumentation1);

        EventHubsProducerInstrumentation instrumentation2 = new EventHubsProducerInstrumentation(null, meter, HOSTNAME, eventHub2);
        EventHubProducerAsyncClient producer2 = new EventHubProducerAsyncClient(HOSTNAME, eventHub2,
            connectionProcessor, retryOptions,
            messageSerializer, testScheduler, false, onClientClosed, CLIENT_IDENTIFIER, instrumentation2);

        StepVerifier.create(producer1.createBatch()
            .flatMap(batch -> {
                batch.tryAdd(new EventData("1"));
                return producer1.send(batch);
            })
            .then(producer2.createBatch().
                flatMap(batch -> {
                    batch.tryAdd(new EventData("2"));
                    batch.tryAdd(new EventData("3"));
                    return producer2.send(batch);
                })))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        TestCounter eventCounter = meter.getCounters().get("messaging.eventhubs.events.sent");
        assertNotNull(eventCounter);

        List<TestMeasurement<Long>> measurements = eventCounter.getMeasurements();
        assertEquals(2, measurements.size());

        assertEquals(1, measurements.get(0).getValue());
        assertEquals(2, measurements.get(1).getValue());
        assertAttributes(eventHub1, null, null, measurements.get(0).getAttributes());
        assertAttributes(eventHub2, null, null, measurements.get(1).getAttributes());
    }


    @Test
    void sendsAnEventDataBatchWithMetricsFailure() {
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any(), any())).thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());
        when(sendLink.getHostname()).thenReturn(HOSTNAME);
        when(sendLink.getEntityPath()).thenReturn(EVENT_HUB_NAME);
        when(sendLink.send(any(Message.class))).thenReturn(Mono.error(new RuntimeException("foo")));

        TestMeter meter = new TestMeter();
        EventHubsProducerInstrumentation instrumentation = new EventHubsProducerInstrumentation(null, meter, HOSTNAME, EVENT_HUB_NAME);

        EventHubProducerAsyncClient producer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions,
            messageSerializer, testScheduler, false, onClientClosed, CLIENT_IDENTIFIER, instrumentation);

        StepVerifier.create(producer.send(new EventData("1")))
            .expectErrorMessage("foo")
            .verify(DEFAULT_TIMEOUT);

        TestCounter eventCounter = meter.getCounters().get("messaging.eventhubs.events.sent");
        assertNotNull(eventCounter);

        List<TestMeasurement<Long>> measurements = eventCounter.getMeasurements();
        assertEquals(1, measurements.size());

        assertEquals(1, measurements.get(0).getValue());
        assertAttributes(EVENT_HUB_NAME, null, "error", measurements.get(0).getAttributes());
    }

    @Test
    void sendsAnEventDataBatchWithMetricsPartitionId() {
        String partitionId = "1";
        String entityPath = EVENT_HUB_NAME + "/Partitions/" + partitionId;
        when(connection.createSendLink(eq(entityPath), eq(entityPath), any(), any())).thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());
        when(sendLink.getHostname()).thenReturn(HOSTNAME);
        when(sendLink.getEntityPath()).thenReturn(entityPath);
        when(sendLink.getLinkName()).thenReturn(entityPath);
        when(sendLink.send(any(Message.class))).thenReturn(Mono.empty());

        TestMeter meter = new TestMeter();
        EventHubsProducerInstrumentation instrumentation = new EventHubsProducerInstrumentation(null, meter, HOSTNAME, EVENT_HUB_NAME);
        EventHubProducerAsyncClient producer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions, messageSerializer, testScheduler, false, onClientClosed, CLIENT_IDENTIFIER, instrumentation);

        SendOptions options = new SendOptions().setPartitionId(partitionId);
        StepVerifier.create(producer.send(new EventData("1"), options))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        TestCounter eventCounter = meter.getCounters().get("messaging.eventhubs.events.sent");
        assertNotNull(eventCounter);

        List<TestMeasurement<Long>> measurements = eventCounter.getMeasurements();
        assertEquals(1, measurements.size());

        assertEquals(1, measurements.get(0).getValue());
        assertAttributes(EVENT_HUB_NAME, partitionId, null, measurements.get(0).getAttributes());
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendsAnEventDataBatchWithMetricsAndTraces() {
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any(), any())).thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());
        when(sendLink.getHostname()).thenReturn(HOSTNAME);
        when(sendLink.getEntityPath()).thenReturn(EVENT_HUB_NAME);
        when(sendLink.getLinkName()).thenReturn(EVENT_HUB_NAME);
        when(sendLink.send(any(Message.class))).thenReturn(Mono.empty());

        TestMeter meter = new TestMeter();
        Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);
        EventHubsProducerInstrumentation instrumentation = new EventHubsProducerInstrumentation(tracer, meter, HOSTNAME, EVENT_HUB_NAME);
        EventHubProducerAsyncClient producer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions, messageSerializer, testScheduler, false, onClientClosed, CLIENT_IDENTIFIER, instrumentation);

        final AtomicReference<Integer> eventInd = new AtomicReference<>(0);

        when(tracer.start(eq("EventHubs.message"), any(), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(invocation.getArgument(1, StartSpanOptions.class), SpanKind.PRODUCER, 0);
                return invocation.getArgument(2, Context.class)
                    .addData(SPAN_CONTEXT_KEY, "span");
            });

        when(tracer.start(eq("EventHubs.send"), any(), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(invocation.getArgument(1, StartSpanOptions.class), SpanKind.CLIENT, 1);
                return invocation.getArgument(2, Context.class)
                    .addData(PARENT_TRACE_CONTEXT_KEY, "parent span");
            }
        );

        doAnswer(invocation -> {
            BiConsumer<String, String> injectContext = invocation.getArgument(0, BiConsumer.class);
            injectContext.accept("traceparent", String.valueOf(eventInd.get()));
            return null;
        }).when(tracer).injectContext(any(), any(Context.class));

        StepVerifier.create(producer.send(new EventData("1")))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        TestCounter eventCounter = meter.getCounters().get("messaging.eventhubs.events.sent");
        assertNotNull(eventCounter);

        List<TestMeasurement<Long>> measurements = eventCounter.getMeasurements();
        assertEquals(1, measurements.size());

        assertEquals(1, measurements.get(0).getValue());
        assertAttributes(EVENT_HUB_NAME, null, null, measurements.get(0).getAttributes());

        assertEquals("parent span", measurements.get(0).getContext().getData(PARENT_TRACE_CONTEXT_KEY).get());
    }

    @Test
    void sendsAnEventDataBatchWithDisabledMetrics() {
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any(), any())).thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());
        when(sendLink.getHostname()).thenReturn(HOSTNAME);
        when(sendLink.getEntityPath()).thenReturn(EVENT_HUB_NAME);
        when(sendLink.getLinkName()).thenReturn(EVENT_HUB_NAME);
        when(sendLink.send(any(Message.class))).thenReturn(Mono.empty());

        TestMeter meter = new TestMeter(false);
        EventHubsProducerInstrumentation instrumentation = new EventHubsProducerInstrumentation(null, meter, HOSTNAME, EVENT_HUB_NAME);
        EventHubProducerAsyncClient producer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions, messageSerializer, testScheduler, false, onClientClosed, CLIENT_IDENTIFIER, instrumentation);

        StepVerifier.create(producer.send(new EventData("1")))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        assertFalse(meter.getCounters().containsKey("messaging.eventhubs.events.sent"));
    }

    @Test
    void sendsAnEventDataBatchWithNullMeterDoesNotThrow() {
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), any(), any())).thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());
        when(sendLink.getHostname()).thenReturn(HOSTNAME);
        when(sendLink.getEntityPath()).thenReturn(EVENT_HUB_NAME);
        when(sendLink.getLinkName()).thenReturn(EVENT_HUB_NAME);
        when(sendLink.send(any(Message.class))).thenReturn(Mono.empty());

        EventHubProducerAsyncClient producer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, retryOptions, messageSerializer, testScheduler, false, onClientClosed, CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);

        StepVerifier.create(producer.send(new EventData("1")))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
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
        when(connection.createSendLink(anyString(), anyString(), any(), anyString())).thenAnswer(mock -> {
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
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        StepVerifier.create(producer.send(testData, new SendOptions().setPartitionId(partitionId1)))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        StepVerifier.create(producer.send(testData, new SendOptions().setPartitionId(partitionId2)))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

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
        ConnectionCacheWrapper hubConnection = mock(ConnectionCacheWrapper.class);
        EventHubProducerAsyncClient sharedProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            hubConnection, retryOptions, messageSerializer, Schedulers.parallel(),
            true, onClientClosed, CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);

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
        ConnectionCacheWrapper hubConnection = mock(ConnectionCacheWrapper.class);
        EventHubProducerAsyncClient dedicatedProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            hubConnection, retryOptions, messageSerializer, Schedulers.parallel(),
            false, onClientClosed, CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);

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
        ConnectionCacheWrapper hubConnection = mock(ConnectionCacheWrapper.class);
        EventHubProducerAsyncClient dedicatedProducer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            hubConnection, retryOptions, messageSerializer, Schedulers.parallel(),
            false, onClientClosed, CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);

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
        when(connection.getEndpointStates()).thenReturn(endpointStates.asFlux());
        endpointStates.emitNext(AmqpEndpointState.ACTIVE, Sinks.EmitFailureHandler.FAIL_FAST);

        EventHubReactorAmqpConnection[] connections = new EventHubReactorAmqpConnection[]{
            connection, connection2, connection3
        };
        connectionProcessor = createConnectionProcessor(connections, connectionOptions.getRetry(), false);
        producer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionProcessor, retryOptions,
            messageSerializer, Schedulers.parallel(), false, onClientClosed, CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);

        final int count = 4;
        final byte[] contents = TEST_CONTENTS.getBytes(UTF_8);
        final Flux<EventData> testData = Flux.range(0, count).flatMap(number -> {
            final EventData data = new EventData(contents);
            return Flux.just(data);
        });
        final EventData testData2 = new EventData("test");

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        final Sinks.Many<AmqpEndpointState> connectionState2 = Sinks.many().multicast().onBackpressureBuffer();
        when(connection2.getEndpointStates()).thenReturn(connectionState2.asFlux());
        when(connection2.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink2));
        when(sendLink2.send(any(Message.class))).thenReturn(Mono.empty());

        final Sinks.Many<AmqpEndpointState> connectionState3 = Sinks.many().multicast().onBackpressureBuffer();
        when(connection3.getEndpointStates()).thenReturn(connectionState3.asFlux());
        when(connection3.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink3));
        when(sendLink3.send(anyList())).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(producer.send(testData))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        // Send in an error signal like a server busy condition.
        endpointStates.emitError(new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "Test-message",
            new AmqpErrorContext("test-namespace")), Sinks.EmitFailureHandler.FAIL_FAST);

        StepVerifier.create(producer.send(testData2))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

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
        when(connection.getEndpointStates()).thenReturn(endpointStates.asFlux());
        endpointStates.emitNext(AmqpEndpointState.ACTIVE, Sinks.EmitFailureHandler.FAIL_FAST);

        EventHubReactorAmqpConnection[] connections = new EventHubReactorAmqpConnection[]{
            connection, connection2, connection3
        };
        connectionProcessor = createConnectionProcessor(connections, connectionOptions.getRetry(), false);
        producer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionProcessor, retryOptions,
            messageSerializer, Schedulers.parallel(), false, onClientClosed, CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);

        final int count = 4;
        final byte[] contents = TEST_CONTENTS.getBytes(UTF_8);
        final Flux<EventData> testData = Flux.range(0, count).flatMap(number -> {
            final EventData data = new EventData(contents);
            return Flux.just(data);
        });
        final EventData testData2 = new EventData("test");

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        final Sinks.Many<AmqpEndpointState> connectionState2 = Sinks.many().multicast().onBackpressureBuffer();
        when(connection2.getEndpointStates()).thenReturn(connectionState2.asFlux());
        when(connection2.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink2));
        when(sendLink2.send(any(Message.class))).thenReturn(Mono.empty());

        final AmqpException nonTransientError = new AmqpException(false, AmqpErrorCondition.UNAUTHORIZED_ACCESS,
            "Test unauthorized access", new AmqpErrorContext("test-namespace"));

        // Act
        StepVerifier.create(producer.send(testData))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        // Send in an error signal like authorization failure.
        endpointStates.emitError(nonTransientError, Sinks.EmitFailureHandler.FAIL_FAST);

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
        when(connection.getEndpointStates()).thenReturn(endpointStates.asFlux());
        endpointStates.emitNext(AmqpEndpointState.ACTIVE, Sinks.EmitFailureHandler.FAIL_FAST);

        EventHubReactorAmqpConnection[] connections = new EventHubReactorAmqpConnection[]{connection, connection2};
        connectionProcessor = createConnectionProcessor(connections, connectionOptions.getRetry(), false);
        producer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionProcessor, retryOptions,
            messageSerializer, Schedulers.parallel(), false, onClientClosed, CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);

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
        when(connection.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        // Send a transient error, and close the original link, if we get a message that contains the "failureKey".
        // This simulates when a link is closed.
        when(sendLink.send(argThat((Message message) -> message.getApplicationProperties().getValue().containsKey(failureKey))))
            .thenAnswer(mock -> {
                final Throwable error = new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "Test-message",
                    new AmqpErrorContext("test-namespace"));

                endpointStates.emitError(error, Sinks.EmitFailureHandler.FAIL_FAST);
                return Mono.error(error);
            });

        final Sinks.Many<AmqpEndpointState> connectionState2 = Sinks.many().multicast().onBackpressureBuffer();
        when(connection2.getEndpointStates()).thenReturn(connectionState2.asFlux());
        when(connection2.createSendLink(eq(EVENT_HUB_NAME), eq(EVENT_HUB_NAME), eq(retryOptions), eq(CLIENT_IDENTIFIER)))
            .thenReturn(Mono.just(sendLink2));
        when(sendLink2.send(any(Message.class))).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(producer.send(testData))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        StepVerifier.create(producer.send(testData2))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        // Assert
        verify(sendLink).send(messagesCaptor.capture());
        final List<Message> messagesSent = messagesCaptor.getValue();
        Assertions.assertEquals(count, messagesSent.size());

        verify(sendLink2, times(1)).send(any(Message.class));
        verifyNoInteractions(sendLink3);

        verifyNoInteractions(onClientClosed);
    }

    private void assertAttributes(String entityName, String entityPath, String status, Map<String, Object> attributes) {
        int expectedAttributeCount = 4;
        if (entityPath == null) {
            expectedAttributeCount--;
        }
        if (status == null) {
            expectedAttributeCount--;
        }

        assertEquals(expectedAttributeCount, attributes.size());
        assertEquals(HOSTNAME, attributes.get("hostName"));
        assertEquals(entityName, attributes.get("entityName"));
        assertEquals(entityPath, attributes.get("partitionId"));
        assertEquals(status, attributes.get("status"));
    }

    private EventData fakeReceivedMessage() {
        Message receivedMessage = Proton.message();
        receivedMessage.setApplicationProperties(new ApplicationProperties(Collections.singletonMap("foo", "bar")));

        Map<Symbol, Object> annotations = new HashMap<>();
        annotations.put(Symbol.getSymbol(OFFSET_ANNOTATION_NAME.getValue()), Instant.now().toEpochMilli());
        annotations.put(Symbol.getSymbol(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue()), Instant.now());
        annotations.put(Symbol.getSymbol(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()), 100L);
        receivedMessage.setMessageAnnotations(new MessageAnnotations(annotations));
        receivedMessage.setBody(new Data(new Binary(new byte[5])));

        EventHubMessageSerializer serializer = new EventHubMessageSerializer();
        return serializer.deserialize(receivedMessage, EventData.class);
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

    private ConnectionCacheWrapper createConnectionProcessor(EventHubReactorAmqpConnection connection, AmqpRetryOptions retryOptions, boolean isV2) {
        if (isV2) {
            final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);
            int[] calls = new int[1];
            final Supplier<EventHubReactorAmqpConnection> connectionSupplier = () -> {
                calls[0] = calls[0] + 1;
                if (calls[0] == 10) {
                    throw new UnsupportedOperationException("connection requests is limited to 10");
                }
                return connection;
            };
            final ReactorConnectionCache<EventHubReactorAmqpConnection> cache = new ReactorConnectionCache<>(null, HOSTNAME, EVENT_HUB_NAME, retryPolicy, new HashMap<>(0));
            return new ConnectionCacheWrapper(cache);
        } else {
            final EventHubConnectionProcessor processor = Mono.fromCallable(() -> connection).repeat(10)
                .subscribeWith(new EventHubConnectionProcessor(HOSTNAME, "event-hub-name", retryOptions));
            return new ConnectionCacheWrapper(processor);
        }
    }

    private ConnectionCacheWrapper createConnectionProcessor(EventHubReactorAmqpConnection[] connections, AmqpRetryOptions retryOptions, boolean isV2) {
        if (isV2) {
            final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);
            final AtomicInteger count = new AtomicInteger();
            final Supplier<EventHubReactorAmqpConnection> connectionSupplier = () -> {
                final int current = count.getAndIncrement();
                final int index = current % connections.length;
                return connections[index];
            };
            final ReactorConnectionCache<EventHubReactorAmqpConnection> cache = new ReactorConnectionCache<>(null, HOSTNAME, EVENT_HUB_NAME, retryPolicy, new HashMap<>(0));
            return new ConnectionCacheWrapper(cache);
        } else {
            final EventHubConnectionProcessor processor = Flux.<EventHubAmqpConnection>create(sink -> {
                final AtomicInteger count = new AtomicInteger();
                sink.onRequest(request -> {
                    for (int i = 0; i < request; i++) {
                        final int current = count.getAndIncrement();
                        final int index = current % connections.length;
                        sink.next(connections[index]);
                    }
                });
            }).subscribeWith(new EventHubConnectionProcessor(HOSTNAME, EVENT_HUB_NAME, retryOptions));
            return new ConnectionCacheWrapper(processor);
        }
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
