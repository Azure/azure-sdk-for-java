// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.credential.TokenCredential;
import com.azure.core.test.utils.metrics.TestHistogram;
import com.azure.core.test.utils.metrics.TestMeasurement;
import com.azure.core.test.utils.metrics.TestMeter;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.implementation.EventHubAmqpConnection;
import com.azure.messaging.eventhubs.implementation.EventHubConnectionProcessor;
import com.azure.messaging.eventhubs.implementation.EventHubManagementNode;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsConsumerInstrumentation;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;
import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_PREFETCH_COUNT;
import static com.azure.messaging.eventhubs.TestUtils.getMessage;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests to verify functionality of {@link EventHubConsumerAsyncClient}.
 */
class EventHubConsumerAsyncClientTest {
    private static final ClientOptions CLIENT_OPTIONS = new ClientOptions();
    static final String PARTITION_ID_HEADER = "partition-id-sent";

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String PAYLOAD = "hello";
    private static final byte[] PAYLOAD_BYTES = PAYLOAD.getBytes(UTF_8);
    private static final int PREFETCH = 5;
    private static final String HOSTNAME = "hostname-foo";
    private static final String EVENT_HUB_NAME = "event-hub-name";
    private static final String CONSUMER_GROUP = "consumer-group-test";
    private static final String PARTITION_ID = "a-partition-id";
    private static final String CLIENT_IDENTIFIER = "my-client-identifier";
    private static final Meter DEFAULT_METER = null;

    private static final ClientLogger LOGGER = new ClientLogger(EventHubConsumerAsyncClientTest.class);
    private static final EventHubsConsumerInstrumentation DEFAULT_INSTRUMENTATION = new EventHubsConsumerInstrumentation(null, null,
        HOSTNAME, EVENT_HUB_NAME, CONSUMER_GROUP, false);
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setMaxRetries(2);
    private final String messageTrackingUUID = UUID.randomUUID().toString();
    private final TestPublisher<AmqpEndpointState> endpointProcessor = TestPublisher.createCold();
    private final TestPublisher<Message> messageProcessor = TestPublisher.createCold();
    private final Scheduler testScheduler = Schedulers.newSingle("eh-test");
    private final MessageSerializer messageSerializer = new EventHubMessageSerializer();

    @Mock
    private AmqpReceiveLink amqpReceiveLink;
    @Mock
    private EventHubAmqpConnection connection;
    @Mock
    private TokenCredential tokenCredential;
    @Mock
    private Runnable onClientClosed;

    private EventHubConsumerAsyncClient consumer;
    private EventHubConnectionProcessor connectionProcessor;
    private AutoCloseable mockCloseable;

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
        mockCloseable = MockitoAnnotations.openMocks(this);

        // Forcing us to publish the messages we receive on the AMQP link on single. Similar to how it is done
        // in ReactorExecutor.
        when(amqpReceiveLink.receive()).thenReturn(messageProcessor.flux().publishOn(testScheduler));
        when(amqpReceiveLink.getEndpointStates()).thenReturn(endpointProcessor.flux());
        when(amqpReceiveLink.getLinkName()).thenReturn(PARTITION_ID);
        when(amqpReceiveLink.addCredits(anyInt())).thenReturn(Mono.empty());

        final ConnectionOptions connectionOptions = new ConnectionOptions(HOSTNAME, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE,
            AmqpTransportType.AMQP_WEB_SOCKETS, new AmqpRetryOptions(), ProxyOptions.SYSTEM_DEFAULTS,
            Schedulers.parallel(), CLIENT_OPTIONS, SslDomain.VerifyMode.VERIFY_PEER,
            "test-product", "test-client-version");

        when(connection.getEndpointStates()).thenReturn(endpointProcessor.flux());
        endpointProcessor.next(AmqpEndpointState.ACTIVE);

        when(connection.createReceiveLink(anyString(), argThat(name -> name.endsWith(PARTITION_ID)),
            any(EventPosition.class), any(ReceiveOptions.class), anyString())).thenReturn(Mono.just(amqpReceiveLink));

        when(connection.closeAsync()).thenReturn(Mono.empty());

        connectionProcessor = Flux.<EventHubAmqpConnection>create(sink -> sink.next(connection))
            .subscribeWith(new EventHubConnectionProcessor(connectionOptions.getFullyQualifiedNamespace(),
                "event-hub-name", connectionOptions.getRetry()));

        consumer = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionProcessor, messageSerializer,
            CONSUMER_GROUP, PREFETCH, false, onClientClosed, CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);
    }

    @AfterEach
    void teardown() throws Exception {
        testScheduler.dispose();
        Mockito.framework().clearInlineMock(this);
        Mockito.clearInvocations(amqpReceiveLink, connection, tokenCredential);
        consumer.close();

        if (mockCloseable != null) {
            mockCloseable.close();
        }
    }

    /**
     * Verify that by default, lastEnqueuedInformation is null if {@link ReceiveOptions#getTrackLastEnqueuedEventProperties()}
     * is not set.
     */
    @Test
    void lastEnqueuedEventInformationIsNull() {
        final EventHubConsumerAsyncClient runtimeConsumer = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, messageSerializer, CONSUMER_GROUP, DEFAULT_PREFETCH_COUNT, false, onClientClosed,
            CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);
        final int numberOfEvents = 10;
        when(amqpReceiveLink.getCredits()).thenReturn(numberOfEvents);
        final int numberToReceive = 3;

        // Assert
        StepVerifier.create(runtimeConsumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest()).take(numberToReceive))
            .then(() -> sendMessages(messageProcessor, numberOfEvents, PARTITION_ID))
            .assertNext(event -> Assertions.assertNull(event.getLastEnqueuedEventProperties()))
            .assertNext(event -> Assertions.assertNull(event.getLastEnqueuedEventProperties()))
            .assertNext(event -> Assertions.assertNull(event.getLastEnqueuedEventProperties()))
            .verifyComplete();

        verifyNoInteractions(onClientClosed);
    }

    /**
     * Verify that the default information is set and is null because no information has been received.
     */
    @Test
    void lastEnqueuedEventInformationCreated() {
        // Arrange
        final EventHubConsumerAsyncClient runtimeConsumer = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, messageSerializer, CONSUMER_GROUP, DEFAULT_PREFETCH_COUNT, false, onClientClosed,
            CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);
        final int numberOfEvents = 10;
        final ReceiveOptions receiveOptions = new ReceiveOptions().setTrackLastEnqueuedEventProperties(true);
        when(amqpReceiveLink.getCredits()).thenReturn(numberOfEvents);

        // Assert
        StepVerifier.create(runtimeConsumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest(), receiveOptions)
            .take(1))
            .then(() -> sendMessages(messageProcessor, numberOfEvents, PARTITION_ID))
            .assertNext(event -> {
                LastEnqueuedEventProperties properties = event.getLastEnqueuedEventProperties();
                assertNotNull(properties);
                Assertions.assertNull(properties.getOffset());
                Assertions.assertNull(properties.getSequenceNumber());
                Assertions.assertNull(properties.getRetrievalTime());
                Assertions.assertNull(properties.getEnqueuedTime());
            })
            .verifyComplete();

        verifyNoInteractions(onClientClosed);
    }

    /**
     * Verifies that this receives a number of events. Verifies that the initial credits we add are equal to the
     * prefetch value.
     */
    @Test
    void receivesNumberOfEvents() {
        // Arrange
        final int numberOfEvents = 10;
        when(amqpReceiveLink.getCredits()).thenReturn(numberOfEvents);

        // Act & Assert
        StepVerifier.create(consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest()).take(numberOfEvents))
            .then(() -> sendMessages(messageProcessor, numberOfEvents, PARTITION_ID))
            .expectNextCount(numberOfEvents)
            .verifyComplete();

        verify(amqpReceiveLink, times(1)).addCredits(PREFETCH);
    }

    /**
     * Verifies that this receives a number of events. Verifies that the initial credits we add are equal to the
     * prefetch value.
     */
    @Test
    void receivesNumberOfEventsAllowsBlock() throws InterruptedException {
        // Arrange
        final int numberOfEvents = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(numberOfEvents);

        // Scheduling on elastic to simulate a user passed in scheduler (this is the default in EventHubClientBuilder).
        final EventHubConsumerAsyncClient myConsumer = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, messageSerializer, CONSUMER_GROUP, PREFETCH, false, onClientClosed, CLIENT_IDENTIFIER,
            DEFAULT_INSTRUMENTATION);
        final Flux<PartitionEvent> eventsFlux = myConsumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest())
            .take(numberOfEvents);

        // Act
        eventsFlux.publishOn(Schedulers.boundedElastic()).subscribe(event -> {
            LOGGER.info("Current count: {}", countDownLatch.getCount());
            saveAction(event.getData()).block(Duration.ofSeconds(2));
            countDownLatch.countDown();
        });

        sendMessages(messageProcessor, numberOfEvents, PARTITION_ID);

        // Assert
        Assertions.assertTrue(countDownLatch.await(30, TimeUnit.SECONDS));
        verifyNoInteractions(onClientClosed);
    }

    private Mono<Instant> saveAction(EventData event) {
        return Mono.delay(Duration.ofMillis(500)).then(Mono.fromCallable(() -> {
            LOGGER.info("Saved the event: {}", event.getBodyAsString());
            return Instant.now();
        }));
    }

    /**
     * Verifies that we can resubscribe to the receiver multiple times.
     */
    @SuppressWarnings("unchecked")
    @Test
    void returnsNewListener() {
        // Arrange
        final int numberOfEvents = 10;

        EventHubAmqpConnection connection1 = mock(EventHubAmqpConnection.class);
        EventHubConnectionProcessor eventHubConnection = Flux.<EventHubAmqpConnection>create(sink -> sink.next(connection1))
            .subscribeWith(new EventHubConnectionProcessor(HOSTNAME, EVENT_HUB_NAME, retryOptions));

        when(connection1.getEndpointStates()).thenReturn(endpointProcessor.flux());

        TestPublisher<Message> processor2 = TestPublisher.create();
        AmqpReceiveLink link2 = mock(AmqpReceiveLink.class);

        TestPublisher<Message> processor3 = TestPublisher.create();
        AmqpReceiveLink link3 = mock(AmqpReceiveLink.class);

        when(link2.receive()).thenReturn(processor2.flux());
        when(link2.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));
        when(link2.getCredits()).thenReturn(numberOfEvents);
        when(link2.addCredits(anyInt())).thenReturn(Mono.empty());

        when(link3.receive()).thenReturn(processor3.flux());
        when(link3.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));
        when(link3.getCredits()).thenReturn(numberOfEvents);
        when(link3.addCredits(anyInt())).thenReturn(Mono.empty());

        when(connection1.createReceiveLink(any(), argThat(arg -> arg.endsWith(PARTITION_ID)), any(EventPosition.class),
            any(ReceiveOptions.class), anyString())).thenReturn(Mono.just(link2), Mono.just(link3));

        EventHubConsumerAsyncClient asyncClient = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            eventHubConnection, messageSerializer, CONSUMER_GROUP, PREFETCH, false, onClientClosed, CLIENT_IDENTIFIER,
            DEFAULT_INSTRUMENTATION);

        // Act & Assert
        StepVerifier.create(asyncClient.receiveFromPartition(PARTITION_ID, EventPosition.earliest()).take(numberOfEvents))
            .then(() -> sendMessages(processor2, numberOfEvents, PARTITION_ID))
            .expectNextCount(numberOfEvents)
            .verifyComplete();

        StepVerifier.create(asyncClient.receiveFromPartition(PARTITION_ID, EventPosition.earliest()).take(numberOfEvents))
            .then(() -> sendMessages(processor3, numberOfEvents, PARTITION_ID))
            .expectNextCount(numberOfEvents)
            .verifyComplete();

        // After the initial prefetch, when we subscribe, and when we do, it'll ask for Long.MAXVALUE, which will set
        // the limit request to MAXIMUM_REQUEST = 100.
        verify(link2, times(1)).addCredits(PREFETCH);
        verify(link3, times(1)).addCredits(PREFETCH);

        verifyNoInteractions(onClientClosed);
    }

    /**
     * Verify that receive can have multiple subscribers.
     */
    @Test
    void canHaveMultipleSubscribers() throws InterruptedException {
        // Arrange
        final int numberOfEvents = 7;
        final CountDownLatch firstConsumerCountDown = new CountDownLatch(numberOfEvents);
        final CountDownLatch secondConsumerCountDown = new CountDownLatch(numberOfEvents);
        final CountDownLatch thirdCountDownEvent = new CountDownLatch(numberOfEvents);

        when(amqpReceiveLink.getCredits()).thenReturn(numberOfEvents);

        // Act
        final Disposable.Composite subscriptions = Disposables.composite(
            consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest()).filter(e -> isMatchingEvent(e, messageTrackingUUID)).take(numberOfEvents)
                .subscribe(event -> firstConsumerCountDown.countDown()),
            consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest()).filter(e -> isMatchingEvent(e, messageTrackingUUID)).take(numberOfEvents)
                .subscribe(event -> secondConsumerCountDown.countDown()),
            consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest()).filter(e -> isMatchingEvent(e, messageTrackingUUID)).take(numberOfEvents)
                .subscribe(event -> thirdCountDownEvent.countDown())
        );

        sendMessages(messageProcessor, numberOfEvents, PARTITION_ID);
        try {
            firstConsumerCountDown.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            secondConsumerCountDown.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            thirdCountDownEvent.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
        } finally {
            subscriptions.dispose();
        }

        // Assert that we were able to see all these events.
        Assertions.assertEquals(0, firstConsumerCountDown.getCount());
        Assertions.assertEquals(0, secondConsumerCountDown.getCount());
        Assertions.assertEquals(0, thirdCountDownEvent.getCount());
    }

    /**
     * Verifies that we can limit the number of deliveries added on the link at a given time.
     */
    @Test
    void canLimitRequestsBackpressure() throws InterruptedException {
        // Arrange
        final int numberOfEvents = 20;
        final int backpressureRequest = 2;
        final CountDownLatch countDownLatch = new CountDownLatch(numberOfEvents);

        when(amqpReceiveLink.getCredits()).thenReturn(PREFETCH);
        consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest()).take(numberOfEvents).subscribe(new BaseSubscriber<PartitionEvent>() {
            final AtomicInteger count = new AtomicInteger();

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                subscription.request(backpressureRequest);
                super.hookOnSubscribe(subscription);
            }

            @Override
            protected void hookOnNext(PartitionEvent value) {
                if (count.incrementAndGet() == backpressureRequest) {
                    request(backpressureRequest);
                    count.set(0);
                }

                LOGGER.verbose("Event Received. {}", countDownLatch.getCount());
                countDownLatch.countDown();
                super.hookOnNext(value);
            }
        });

        // Act
        sendMessages(messageProcessor, numberOfEvents, PARTITION_ID);
        countDownLatch.await(30, TimeUnit.SECONDS);

        // Assert
        Assertions.assertEquals(0, countDownLatch.getCount());
        verify(amqpReceiveLink, atLeastOnce()).addCredits(PREFETCH);
    }

    /**
     * Verifies that if we have limited the request, the number of credits added is the same as that limit.
     */
    @Test
    void returnsCorrectCreditRequest() throws InterruptedException {
        // Arrange
        final int numberOfEvents = 20;
        final int backpressureRequest = 2;
        final CountDownLatch countDownLatch = new CountDownLatch(numberOfEvents);

        when(amqpReceiveLink.getCredits()).thenReturn(PREFETCH);
        consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest()).take(numberOfEvents).subscribe(new BaseSubscriber<PartitionEvent>() {
            final AtomicInteger count = new AtomicInteger();

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                subscription.request(backpressureRequest);
                super.hookOnSubscribe(subscription);
            }

            @Override
            protected void hookOnNext(PartitionEvent value) {
                if (count.incrementAndGet() == backpressureRequest) {
                    request(backpressureRequest);
                    count.set(0);
                }

                LOGGER.info("Event Received. {}", countDownLatch.getCount());
                countDownLatch.countDown();
                super.hookOnNext(value);
            }
        });

        // Act
        sendMessages(messageProcessor, numberOfEvents, PARTITION_ID);
        countDownLatch.await(30, TimeUnit.SECONDS);

        // Assert
        Assertions.assertEquals(0, countDownLatch.getCount());
        verify(amqpReceiveLink, atLeastOnce()).addCredits(PREFETCH);
    }

    /**
     * Verify that backpressure is respected.
     */
    @Test
    void suppliesCreditsWhenSubscribers() throws InterruptedException {
        // Arrange
        final int backPressure = 8;
        final CountDownLatch semaphore = new CountDownLatch(8);
        final AtomicInteger counter = new AtomicInteger();

        when(amqpReceiveLink.getCredits()).thenReturn(PREFETCH);

        final Disposable subscription = consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest()).subscribe(
            e -> {
                LOGGER.info("Event received");
                final int count = counter.incrementAndGet();
                if (count > backPressure) {
                    Assertions.fail("Shouldn't have more than " + backPressure + " events. Count: " + count);
                }

                semaphore.countDown();
            },
            error -> Assertions.fail(error.toString()),
            () -> {
                LOGGER.info("Complete");
            }, sub -> {
                LOGGER.info("requesting backpressure: {}", backPressure);
                sub.request(backPressure);
            });

        try {
            // Act
            sendMessages(messageProcessor, 11, PARTITION_ID);

            // Assert
            Assertions.assertTrue(semaphore.await(10, TimeUnit.SECONDS));
            Assertions.assertEquals(backPressure, counter.get());
        } finally {
            subscription.dispose();
        }
    }

    @Test
    void setsCorrectProperties() {
        // Act
        String endpointSuffix = Configuration.getGlobalConfiguration()
            .get("AZURE_EVENTHUBS_ENDPOINT_SUFFIX", ".servicebus.windows.net");
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString(String.format("Endpoint=sb://doesnotexist%s/;SharedAccessKeyName=doesnotexist;SharedAccessKey=fakekey;EntityPath=dummy-event-hub", endpointSuffix))
            .consumerGroup(CONSUMER_GROUP)
            .buildAsyncConsumerClient();

        Assertions.assertEquals("dummy-event-hub", consumer.getEventHubName());
        Assertions.assertEquals(String.format("doesnotexist%s", endpointSuffix), consumer.getFullyQualifiedNamespace());
        Assertions.assertEquals(CONSUMER_GROUP, consumer.getConsumerGroup());
    }

    @Test
    void receivesMultiplePartitions() {
        // Arrange
        int numberOfEvents = 10;
        when(amqpReceiveLink.getCredits()).thenReturn(numberOfEvents);

        EventHubAmqpConnection connection1 = mock(EventHubAmqpConnection.class);
        EventHubConnectionProcessor eventHubConnection = Flux.<EventHubAmqpConnection>create(sink -> sink.next(connection1))
            .subscribeWith(new EventHubConnectionProcessor(HOSTNAME, EVENT_HUB_NAME, retryOptions));

        when(connection1.getEndpointStates()).thenReturn(endpointProcessor.flux());

        String id2 = "partition-2";
        String id3 = "partition-3";
        String[] partitions = new String[]{PARTITION_ID, id2, id3};

        // Set-up management node returns.
        EventHubManagementNode managementNode = mock(EventHubManagementNode.class);
        when(connection1.getManagementNode()).thenReturn(Mono.just(managementNode));
        when(managementNode.getEventHubProperties())
            .thenReturn(Mono.just(new EventHubProperties(EVENT_HUB_NAME, Instant.EPOCH, partitions)));

        EventHubConsumerAsyncClient asyncClient = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            eventHubConnection, messageSerializer, CONSUMER_GROUP, PREFETCH, false, onClientClosed, CLIENT_IDENTIFIER,
            DEFAULT_INSTRUMENTATION);

        TestPublisher<Message> processor2 = TestPublisher.createCold();
        AmqpReceiveLink link2 = mock(AmqpReceiveLink.class);

        TestPublisher<Message> processor3 = TestPublisher.createCold();
        AmqpReceiveLink link3 = mock(AmqpReceiveLink.class);

        when(link2.getLinkName()).thenReturn(id2);
        when(link2.receive()).thenReturn(processor2.flux());
        when(link2.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));
        when(link2.getCredits()).thenReturn(numberOfEvents);
        when(link2.addCredits(anyInt())).thenReturn(Mono.empty());

        when(link3.getLinkName()).thenReturn(id3);
        when(link3.receive()).thenReturn(processor3.flux());
        when(link3.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));
        when(link3.getCredits()).thenReturn(numberOfEvents);
        when(link3.addCredits(anyInt())).thenReturn(Mono.empty());

        when(connection1.createReceiveLink(any(), anyString(), any(EventPosition.class), any(ReceiveOptions.class), anyString()))
            .thenAnswer(mock -> {
                String name = mock.getArgument(1);
                if (name.endsWith(PARTITION_ID)) {
                    return Mono.just(amqpReceiveLink);
                } else if (name.endsWith(id2)) {
                    return Mono.just(link2);
                } else if (name.endsWith(id3)) {
                    return Mono.just(link3);
                } else {
                    return Mono.error(new IllegalArgumentException("Unknown entityPath: " + name));
                }
            });

        // Act & Assert
        StepVerifier.create(asyncClient.receive(true).filter(e -> isMatchingEvent(e, messageTrackingUUID)))
            .then(() -> sendMessages(processor2, 2, id2))
            .assertNext(event -> assertPartition(id2, event))
            .assertNext(event -> assertPartition(id2, event))
            .then(() -> sendMessages(processor3, 1, id3))
            .assertNext(event -> assertPartition(id3, event))
            .then(() -> sendMessages(processor2, 1, id2))
            .assertNext(event -> assertPartition(id2, event))
            .thenCancel()
            .verify(TIMEOUT);

        verifyNoInteractions(onClientClosed);
    }

    /**
     * Verifies that even if one link closes, it still continues to receive.
     */
    @Test
    void receivesMultiplePartitionsWhenOneCloses() {
        // Arrange
        int numberOfEvents = 10;
        when(amqpReceiveLink.getCredits()).thenReturn(numberOfEvents);

        EventHubAmqpConnection connection1 = mock(EventHubAmqpConnection.class);
        EventHubConnectionProcessor eventHubConnection = Flux.<EventHubAmqpConnection>create(sink -> sink.next(connection1))
            .subscribeWith(new EventHubConnectionProcessor(HOSTNAME, EVENT_HUB_NAME, retryOptions));

        when(connection1.getEndpointStates()).thenReturn(endpointProcessor.flux());

        String id2 = "partition-2";
        String id3 = "partition-3";
        String[] partitions = new String[]{PARTITION_ID, id2, id3};

        // Set-up management node returns.
        EventHubManagementNode managementNode = mock(EventHubManagementNode.class);
        when(connection1.getManagementNode()).thenReturn(Mono.just(managementNode));
        when(managementNode.getEventHubProperties())
            .thenReturn(Mono.just(new EventHubProperties(EVENT_HUB_NAME, Instant.EPOCH, partitions)));

        EventHubConsumerAsyncClient asyncClient = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            eventHubConnection, messageSerializer, CONSUMER_GROUP, PREFETCH, false, onClientClosed, CLIENT_IDENTIFIER,
            DEFAULT_INSTRUMENTATION);

        TestPublisher<Message> processor2 = TestPublisher.create();
        AmqpReceiveLink link2 = mock(AmqpReceiveLink.class);

        TestPublisher<Message> processor3 = TestPublisher.create();
        AmqpReceiveLink link3 = mock(AmqpReceiveLink.class);

        when(link2.receive()).thenReturn(processor2.flux());
        when(link2.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));
        when(link2.getCredits()).thenReturn(numberOfEvents);
        when(link2.addCredits(anyInt())).thenReturn(Mono.empty());

        when(link3.receive()).thenReturn(processor3.flux());
        when(link3.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));
        when(link3.getCredits()).thenReturn(numberOfEvents);
        when(link3.addCredits(anyInt())).thenReturn(Mono.empty());

        when(connection1.createReceiveLink(any(), anyString(), any(EventPosition.class), any(ReceiveOptions.class), anyString()))
            .thenAnswer(mock -> {
                String name = mock.getArgument(1);
                if (name.endsWith(PARTITION_ID)) {
                    return Mono.just(amqpReceiveLink);
                } else if (name.endsWith(id2)) {
                    return Mono.just(link2);
                } else if (name.endsWith(id3)) {
                    return Mono.just(link3);
                } else {
                    return Mono.error(new IllegalArgumentException("Unknown session: " + name));
                }
            });

        // Act & Assert
        StepVerifier.create(asyncClient.receive(true).filter(e -> isMatchingEvent(e, messageTrackingUUID)))
            .then(() -> sendMessages(processor2, 2, id2))
            .assertNext(event -> assertPartition(id2, event))
            .assertNext(event -> assertPartition(id2, event))
            .then(() -> sendMessages(processor3, 1, id3))
            .assertNext(event -> assertPartition(id3, event))
            .then(() -> {
                processor2.complete();
                sendMessages(messageProcessor, 1, PARTITION_ID);
            })
            .assertNext(event -> assertPartition(PARTITION_ID, event))
            .thenCancel()
            .verify(TIMEOUT);

        verifyNoInteractions(onClientClosed);
    }

    /**
     * Verifies that when we have a shared connection, the consumer does not close that connection.
     */
    @Test
    void doesNotCloseSharedConnection() {
        // Arrange
        EventHubAmqpConnection connection1 = mock(EventHubAmqpConnection.class);
        EventHubConnectionProcessor eventHubConnection = Flux.<EventHubAmqpConnection>create(sink -> sink.next(connection1))
            .subscribeWith(new EventHubConnectionProcessor(HOSTNAME, EVENT_HUB_NAME, retryOptions));
        EventHubConsumerAsyncClient sharedConsumer = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            eventHubConnection, messageSerializer, CONSUMER_GROUP, PREFETCH, true, onClientClosed, CLIENT_IDENTIFIER,
            DEFAULT_INSTRUMENTATION);

        // Act
        sharedConsumer.close();

        // Verify
        Assertions.assertFalse(eventHubConnection.isDisposed());
        verify(connection1, never()).dispose();

        verify(onClientClosed).run();
    }

    /**
     * Verifies that when we have a non-shared connection, the consumer closes that connection.
     */
    @Test
    void closesDedicatedConnection() {
        // Arrange
        EventHubConnectionProcessor hubConnection = mock(EventHubConnectionProcessor.class);
        EventHubConsumerAsyncClient dedicatedConsumer = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            hubConnection, messageSerializer, CONSUMER_GROUP, PREFETCH, false, onClientClosed, CLIENT_IDENTIFIER,
            DEFAULT_INSTRUMENTATION);

        // Act
        dedicatedConsumer.close();

        // Verify
        verify(hubConnection, times(1)).dispose();

        verifyNoInteractions(onClientClosed);
    }

    @Test
    void receiveReportsMetrics() {
        // Arrange
        final int numberOfEvents = 2;
        when(amqpReceiveLink.getCredits()).thenReturn(numberOfEvents);

        TestMeter meter = new TestMeter();
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(null, meter,
            HOSTNAME, EVENT_HUB_NAME, CONSUMER_GROUP, false);
        consumer = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionProcessor, messageSerializer,
            CONSUMER_GROUP, PREFETCH, false, onClientClosed, CLIENT_IDENTIFIER, instrumentation);

        Flux<PartitionEvent> receive = consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest())
            .filter(e -> isMatchingEvent(e, messageTrackingUUID))
            .take(numberOfEvents);

        Instant enqueuedTime = Instant.now().minusSeconds(1000);
        sendMessages(messageProcessor, numberOfEvents, PARTITION_ID, enqueuedTime);

        // Act
        StepVerifier.create(receive)
            .thenConsumeWhile(e -> {
                TestHistogram consumerLag = meter.getHistograms().get("messaging.eventhubs.consumer.lag");
                assertNotNull(consumerLag);
                Instant afterReceived = Instant.now();

                List<TestMeasurement<Double>> measurements = consumerLag.getMeasurements();
                TestMeasurement<Double> last = measurements.get(measurements.size() - 1);
                assertEquals(Duration.between(enqueuedTime, afterReceived).toMillis() / 1000d, last.getValue(), 1);
                assertAttributes(EVENT_HUB_NAME, e.getPartitionContext().getPartitionId(), last.getAttributes());
                return true;
            })
            .verifyComplete();

        assertEquals(numberOfEvents, meter.getHistograms().get("messaging.eventhubs.consumer.lag").getMeasurements().size());
    }

    @Test
    void receiveReportsMetricsNegativeLag() {
        // Arrange
        when(amqpReceiveLink.getCredits()).thenReturn(1);

        TestMeter meter = new TestMeter();
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(null, meter,
            HOSTNAME, EVENT_HUB_NAME, CONSUMER_GROUP, false);
        consumer = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionProcessor, messageSerializer,
            CONSUMER_GROUP, PREFETCH, false, onClientClosed, CLIENT_IDENTIFIER, instrumentation);

        Flux<PartitionEvent> receive = consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest())
            .filter(e -> isMatchingEvent(e, messageTrackingUUID))
            .take(1);

        Instant enqueuedTime1 = Instant.now().plusSeconds(1000);
        sendMessages(messageProcessor, 1, PARTITION_ID, enqueuedTime1);

        // Act
        StepVerifier.create(receive)
            .consumeNextWith(e -> {
                TestHistogram consumerLag = meter.getHistograms().get("messaging.eventhubs.consumer.lag");
                assertNotNull(consumerLag);
                Instant afterReceived = Instant.now();

                List<TestMeasurement<Double>> measurements = consumerLag.getMeasurements();
                TestMeasurement<Double> last = measurements.get(measurements.size() - 1);
                assertEquals(0, last.getValue());
                assertAttributes(EVENT_HUB_NAME, e.getPartitionContext().getPartitionId(), last.getAttributes());
            })
            .verifyComplete();

        assertEquals(1, meter.getHistograms().get("messaging.eventhubs.consumer.lag").getMeasurements().size());
    }

    @Test
    void receiveDoesNotReportDisabledMetrics() {
        // Arrange
        when(amqpReceiveLink.getCredits()).thenReturn(1);

        TestMeter meter = new TestMeter(false);
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(null, meter,
            HOSTNAME, EVENT_HUB_NAME, CONSUMER_GROUP, false);

        consumer = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionProcessor, messageSerializer,
            CONSUMER_GROUP, PREFETCH, false, onClientClosed, CLIENT_IDENTIFIER, instrumentation);

        Flux<PartitionEvent> receive = consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest())
            .filter(e -> isMatchingEvent(e, messageTrackingUUID))
            .take(1);

        // Act
        sendMessages(messageProcessor, 1, PARTITION_ID);
        StepVerifier.create(receive)
            .expectNextCount(1)
            .verifyComplete();

        assertFalse(meter.getHistograms().containsKey("messaging.eventhubs.consumer.lag"));
    }

    @Test
    void receiveNullMeterDoesNotThrow() {
        // Arrange
        when(amqpReceiveLink.getCredits()).thenReturn(1);

        consumer = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionProcessor, messageSerializer,
            CONSUMER_GROUP, PREFETCH, false, onClientClosed, CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);

        Flux<PartitionEvent> receive = consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest())
            .filter(e -> isMatchingEvent(e, messageTrackingUUID))
            .take(1);

        // Act
        sendMessages(messageProcessor, 1, PARTITION_ID);
        StepVerifier.create(receive)
            .expectNextCount(1)
            .verifyComplete();
    }

    /**
     * Verifies tracing for getEventHubsProperties and getPartitionProperties
     */
    @Test
    void startSpanForGetProperties() {
        // Arrange
        final Tracer tracer1 = mock(Tracer.class);
        when(tracer1.isEnabled()).thenReturn(true);
        EventHubsConsumerInstrumentation instrumentation = new EventHubsConsumerInstrumentation(tracer1, null,
            HOSTNAME, EVENT_HUB_NAME, CONSUMER_GROUP, false);
        EventHubConsumerAsyncClient consumer = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            connectionProcessor, messageSerializer, CONSUMER_GROUP, PREFETCH, false, onClientClosed, CLIENT_IDENTIFIER,
            instrumentation);

        EventHubProperties ehProperties = new EventHubProperties(EVENT_HUB_NAME, Instant.now(), new String[]{"0"});
        PartitionProperties partitionProperties = new PartitionProperties(EVENT_HUB_NAME, "0",
            1L, 2L, OffsetDateTime.now().toString(), Instant.now(), false);
        EventHubManagementNode managementNode = mock(EventHubManagementNode.class);
        when(connection.getManagementNode()).thenReturn(Mono.just(managementNode));
        when(managementNode.getEventHubProperties()).thenReturn(Mono.just(ehProperties));
        when(managementNode.getPartitionProperties(anyString())).thenReturn(Mono.just(partitionProperties));

        when(tracer1.start(eq("EventHubs.getPartitionProperties"), any(StartSpanOptions.class), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(invocation.getArgument(1, StartSpanOptions.class));
                return invocation.getArgument(2, Context.class)
                        .addData(PARENT_TRACE_CONTEXT_KEY, "getPartitionProperties");
            }
        );
        when(tracer1.start(eq("EventHubs.getEventHubProperties"), any(StartSpanOptions.class), any(Context.class))).thenAnswer(
            invocation -> {
                assertStartOptions(invocation.getArgument(1, StartSpanOptions.class));
                return invocation.getArgument(2, Context.class)
                    .addData(PARENT_TRACE_CONTEXT_KEY, "getEventHubsProperties");
            }
        );

        // Act
        StepVerifier.create(consumer.getEventHubProperties())
            .consumeNextWith(p -> assertSame(ehProperties, p))
            .verifyComplete();

        StepVerifier.create(consumer.getPartitionProperties("0"))
            .consumeNextWith(p -> assertSame(partitionProperties, p))
            .verifyComplete();

        //Assert
        verify(tracer1, times(1))
            .start(eq("EventHubs.getPartitionProperties"), any(StartSpanOptions.class), any(Context.class));
        verify(tracer1, times(1))
            .start(eq("EventHubs.getEventHubProperties"), any(StartSpanOptions.class), any(Context.class));
        verify(tracer1, times(2)).end(isNull(), isNull(), any());

        verifyNoInteractions(onClientClosed);
    }

    private void assertStartOptions(StartSpanOptions startOpts) {
        assertEquals(SpanKind.CLIENT, startOpts.getSpanKind());
        assertEquals(EVENT_HUB_NAME, startOpts.getAttributes().get(ENTITY_PATH_KEY));
        assertEquals(HOSTNAME, startOpts.getAttributes().get(HOST_NAME_KEY));
    }

    private void assertPartition(String partitionId, PartitionEvent event) {
        System.out.println("Event received: " + event.getPartitionContext().getPartitionId());
        final Object value = event.getData().getProperties().get(PARTITION_ID_HEADER);
        Assertions.assertTrue(value instanceof String);
        Assertions.assertEquals(partitionId, value);

        Assertions.assertEquals(partitionId, event.getPartitionContext().getPartitionId());
        Assertions.assertEquals(EVENT_HUB_NAME, event.getPartitionContext().getEventHubName());
        Assertions.assertEquals(CONSUMER_GROUP, event.getPartitionContext().getConsumerGroup());
    }

    private void sendMessages(TestPublisher<Message> testPublisher, int numberOfEvents, String partitionId) {
        Map<String, String> map = Collections.singletonMap(PARTITION_ID_HEADER, partitionId);

        for (int i = 0; i < numberOfEvents; i++) {
            testPublisher.next(getMessage(PAYLOAD_BYTES, messageTrackingUUID, map));
        }
    }

    private void sendMessages(TestPublisher<Message> testPublisher, int numberOfEvents, String partitionId, Instant enqueueTime) {
        Map<String, String> map = Collections.singletonMap(PARTITION_ID_HEADER, partitionId);

        for (int i = 0; i < numberOfEvents; i++) {
            Message message = getMessage(PAYLOAD_BYTES, messageTrackingUUID, map);
            message.getMessageAnnotations().getValue().put(Symbol.valueOf(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue()), enqueueTime);
            testPublisher.next(message);
        }
    }


    private void assertAttributes(String entityName, String entityPath, Map<String, Object> attributes) {
        assertEquals(4, attributes.size());
        assertEquals(HOSTNAME, attributes.get("hostName"));
        assertEquals(entityName, attributes.get("entityName"));
        assertEquals(CONSUMER_GROUP, attributes.get("consumerGroup"));
        assertEquals(entityPath, attributes.get("partitionId"));
    }
}
