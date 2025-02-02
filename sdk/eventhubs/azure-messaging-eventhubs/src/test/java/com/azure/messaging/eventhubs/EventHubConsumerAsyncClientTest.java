// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorConnectionCache;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.test.utils.metrics.TestHistogram;
import com.azure.core.test.utils.metrics.TestMeasurement;
import com.azure.core.test.utils.metrics.TestMeter;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.implementation.EventHubManagementNode;
import com.azure.messaging.eventhubs.implementation.EventHubReactorAmqpConnection;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsConsumerInstrumentation;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;
import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_PREFETCH_COUNT;
import static com.azure.messaging.eventhubs.TestUtils.assertAllAttributes;
import static com.azure.messaging.eventhubs.TestUtils.getMessage;
import static com.azure.messaging.eventhubs.TestUtils.getSpanName;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.GET_EVENT_HUB_PROPERTIES;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.GET_PARTITION_PROPERTIES;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
 * Unit tests to verify functionality of {@link EventHubConsumerAsyncClient}.
 */
class EventHubConsumerAsyncClientTest {
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
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final String DEFAULT_NAMESPACE_SUFFIX = ".servicebus.windows.net";
    private static final AmqpRetryOptions RETRY_OPTIONS = new AmqpRetryOptions().setDelay(Duration.ofMillis(200))
        .setMode(AmqpRetryMode.FIXED)
        .setTryTimeout(Duration.ofMillis(100));

    private static final ClientLogger LOGGER = new ClientLogger(EventHubConsumerAsyncClientTest.class);
    private static final EventHubsConsumerInstrumentation DEFAULT_INSTRUMENTATION
        = new EventHubsConsumerInstrumentation(null, null, HOSTNAME, EVENT_HUB_NAME, CONSUMER_GROUP, false);
    private final String messageTrackingUUID = CoreUtils.randomUuid().toString();
    private final MessageSerializer messageSerializer = new EventHubMessageSerializer();

    @Mock
    private Runnable onClientClosed;
    private AutoCloseable mockCloseable;

    @BeforeEach
    void setup() {
        mockCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);
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
        EventHubConsumerAsyncClient consumer = null;
        try (MockConnection connection = new MockConnection();
            MockReceiveLink receiveLink = new MockReceiveLink(PARTITION_ID, messageTrackingUUID)) {
            final int numberOfEvents = 10;
            final int numberToReceive = 3;
            receiveLink.arrange(numberOfEvents);
            connection.arrange(receiveLink);
            final ConnectionCacheWrapper connectionCache = connection.wrapInCache();
            consumer = createConsumer(connectionCache, DEFAULT_PREFETCH_COUNT);

            // Act and assert
            //
            connection.setEndpointActive();
            receiveLink.setEndpointActive();

            StepVerifier
                .create(consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest()).take(numberToReceive))
                .then(() -> receiveLink.emitMessages(numberOfEvents))
                .assertNext(event -> Assertions.assertNull(event.getLastEnqueuedEventProperties()))
                .assertNext(event -> Assertions.assertNull(event.getLastEnqueuedEventProperties()))
                .assertNext(event -> Assertions.assertNull(event.getLastEnqueuedEventProperties()))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);
            verifyNoInteractions(onClientClosed);
        } finally {
            close(consumer);
        }
    }

    /**
     * Verify that the default information is set and is null because no information has been received.
     */
    @Test
    void lastEnqueuedEventInformationCreated() {
        EventHubConsumerAsyncClient consumer = null;
        try (MockConnection connection = new MockConnection();
            MockReceiveLink receiveLink = new MockReceiveLink(PARTITION_ID, messageTrackingUUID)) {
            final int numberOfEvents = 10;
            final ReceiveOptions receiveOptions = new ReceiveOptions().setTrackLastEnqueuedEventProperties(true);
            receiveLink.arrange(numberOfEvents);
            connection.arrange(receiveLink);
            final ConnectionCacheWrapper connectionCache = connection.wrapInCache();
            consumer = createConsumer(connectionCache, DEFAULT_PREFETCH_COUNT);

            // Act and assert
            //
            connection.setEndpointActive();
            receiveLink.setEndpointActive();

            StepVerifier
                .create(consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest(), receiveOptions).take(1))
                .then(() -> receiveLink.emitMessages(numberOfEvents))
                .assertNext(event -> {
                    LastEnqueuedEventProperties properties = event.getLastEnqueuedEventProperties();
                    assertNotNull(properties);
                    Assertions.assertNull(properties.getOffset());
                    Assertions.assertNull(properties.getSequenceNumber());
                    Assertions.assertNull(properties.getRetrievalTime());
                    Assertions.assertNull(properties.getEnqueuedTime());
                })
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);

            verifyNoInteractions(onClientClosed);
        } finally {
            close(consumer);
        }
    }

    /**
     * Verifies that this receives a number of events. Verifies that the initial credits we add are equal to the
     * prefetch value.
     */
    @Test
    void receivesNumberOfEvents() {
        EventHubConsumerAsyncClient consumer = null;
        try (MockConnection connection = new MockConnection();
            MockReceiveLink receiveLink = new MockReceiveLink(PARTITION_ID, messageTrackingUUID)) {
            final int numberOfEvents = 10;
            receiveLink.arrange(numberOfEvents);
            connection.arrange(receiveLink);
            final ConnectionCacheWrapper connectionCache = connection.wrapInCache();
            consumer = createConsumer(connectionCache, PREFETCH);

            // Act and assert
            //
            connection.setEndpointActive();
            receiveLink.setEndpointActive();

            StepVerifier
                .create(consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest()).take(numberOfEvents))
                .then(() -> receiveLink.emitMessages(numberOfEvents))
                .expectNextCount(numberOfEvents)
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);

            final List<Long> creditsList = receiveLink.getCreditsList();
            Assertions.assertFalse(creditsList.isEmpty());
            Assertions.assertEquals(PREFETCH, creditsList.get(0));
        } finally {
            close(consumer);
        }
    }

    /**
     * Verifies that this receives a number of events. Verifies that the initial credits we add are equal to the
     * prefetch value.
     */
    @Test
    void receivesNumberOfEventsAllowsBlock() throws InterruptedException {
        EventHubConsumerAsyncClient consumer = null;
        try (MockConnection connection = new MockConnection();
            MockReceiveLink receiveLink = new MockReceiveLink(PARTITION_ID, messageTrackingUUID)) {
            final int numberOfEvents = 10;
            final CountDownLatch countDownLatch = new CountDownLatch(numberOfEvents);
            receiveLink.arrange(numberOfEvents);
            connection.arrange(receiveLink);
            final ConnectionCacheWrapper connectionCache = connection.wrapInCache();
            consumer = createConsumer(connectionCache, PREFETCH);

            // Act and assert
            //
            connection.setEndpointActive();
            receiveLink.setEndpointActive();

            final Flux<PartitionEvent> events
                = consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest()).take(numberOfEvents);
            events.publishOn(Schedulers.boundedElastic()).subscribe(event -> {
                LOGGER.info("Current count: {}", countDownLatch.getCount());
                saveAction(event.getData()).block(Duration.ofSeconds(2));
                countDownLatch.countDown();
            });
            receiveLink.emitMessages(numberOfEvents);
            Assertions.assertTrue(countDownLatch.await(30, TimeUnit.SECONDS));
            verifyNoInteractions(onClientClosed);
        } finally {
            close(consumer);
        }
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
    @Test
    void returnsNewListener() {
        EventHubConsumerAsyncClient consumer = null;
        try (MockConnection connection = new MockConnection();
            MockReceiveLink receiveLink0 = new MockReceiveLink(PARTITION_ID, messageTrackingUUID);
            MockReceiveLink receiveLink1 = new MockReceiveLink(PARTITION_ID, messageTrackingUUID)) {
            final int numberOfEvents = 10;
            receiveLink0.arrange(numberOfEvents);
            receiveLink1.arrange(numberOfEvents);
            connection.arrange(receiveLink0, receiveLink1);
            final ConnectionCacheWrapper connectionCache = connection.wrapInCache();
            consumer = createConsumer(connectionCache, PREFETCH);

            // Act and assert
            //
            connection.setEndpointActive();
            receiveLink0.setEndpointActive();
            receiveLink1.setEndpointActive();

            StepVerifier
                .create(consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest()).take(numberOfEvents))
                .then(() -> receiveLink0.emitMessages(numberOfEvents))
                .expectNextCount(numberOfEvents)
                .then(receiveLink0::setEndpointCompleted)
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);
            // ^ Here the un-subscription will cause receiveLink0 to be closed. The next receive() below will
            //   obtain receiveLink1.

            StepVerifier
                .create(consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest()).take(numberOfEvents))
                .then(() -> receiveLink1.emitMessages(numberOfEvents))
                .expectNextCount(numberOfEvents)
                .then(receiveLink1::setEndpointCompleted)
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);

            final List<Long> creditsList0 = receiveLink0.getCreditsList();
            Assertions.assertFalse(creditsList0.isEmpty());
            Assertions.assertEquals(PREFETCH, creditsList0.get(0));

            final List<Long> creditsList1 = receiveLink1.getCreditsList();
            Assertions.assertFalse(creditsList1.isEmpty());
            Assertions.assertEquals(PREFETCH, creditsList1.get(0));

            verifyNoInteractions(onClientClosed);
        } finally {
            close(consumer);
        }
    }

    /**
     * Verifies that the credit placed on the receiver link is inline with thr backpressure requested.
     */
    @Test
    void shouldHonorBackpressure() throws InterruptedException {
        EventHubConsumerAsyncClient consumer = null;
        try (MockConnection connection = new MockConnection();
            MockReceiveLink receiveLink = new MockReceiveLink(PARTITION_ID, messageTrackingUUID)) {
            final int numberOfEvents = 10;
            final int backpressureRequest = 2;
            final CountDownLatch countDownLatch = new CountDownLatch(numberOfEvents);
            receiveLink.arrange(PREFETCH);
            connection.arrange(receiveLink);
            final ConnectionCacheWrapper connectionCache = connection.wrapInCache();
            consumer = createConsumer(connectionCache, PREFETCH);

            // Act and assert
            //
            connection.setEndpointActive();
            receiveLink.setEndpointActive();

            consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest())
                .subscribe(new BaseSubscriber<PartitionEvent>() {
                    final AtomicInteger count = new AtomicInteger();

                    @Override
                    protected void hookOnSubscribe(Subscription sub) {
                        sub.request(backpressureRequest);
                        super.hookOnSubscribe(sub);
                    }

                    @Override
                    protected void hookOnNext(PartitionEvent event) {
                        if (count.incrementAndGet() == backpressureRequest) {
                            request(backpressureRequest);
                            count.set(0);
                        }
                        final long count = countDownLatch.getCount();
                        LOGGER.verbose("Event Received {}", count);
                        if (count == 1) {
                            // closes the events source link when we have received expected number of events.
                            receiveLink.setEndpointCompleted();
                        }
                        countDownLatch.countDown();
                        super.hookOnNext(event);
                    }
                });

            receiveLink.emitMessages(numberOfEvents);
            countDownLatch.await(30, TimeUnit.SECONDS);

            Assertions.assertEquals(0, countDownLatch.getCount());
            final List<Long> creditsList = receiveLink.getCreditsList();
            Assertions.assertFalse(creditsList.isEmpty());
            Assertions.assertEquals(PREFETCH, creditsList.get(0));
            final long totalCredit = creditsList.stream().reduce(0L, Long::sum);
            final long min = numberOfEvents;
            final long max = numberOfEvents + PREFETCH;
            Assertions.assertTrue(totalCredit >= min && totalCredit <= max,
                "total credit " + totalCredit + " should be in the range [" + min + ", " + max + "]");
        } finally {
            close(consumer);
        }
    }

    /**
     * Verify that backpressure is respected.
     */
    @Test
    void suppliesCreditsWhenSubscribers() throws InterruptedException {
        EventHubConsumerAsyncClient consumer = null;
        final Disposable.Composite disposable = Disposables.composite();
        try (MockConnection connection = new MockConnection();
            MockReceiveLink receiveLink = new MockReceiveLink(PARTITION_ID, messageTrackingUUID)) {
            final int requested = 8;
            final CountDownLatch semaphore = new CountDownLatch(requested);
            final AtomicInteger counter = new AtomicInteger();
            receiveLink.arrange(PREFETCH);
            connection.arrange(receiveLink);
            final ConnectionCacheWrapper connectionCache = connection.wrapInCache();
            consumer = createConsumer(connectionCache, PREFETCH);

            // Act and assert
            connection.setEndpointActive();
            receiveLink.setEndpointActive();

            final Consumer<Subscription> onSubscribe = subscription -> {
                LOGGER.info("requesting backpressure: {}", requested);
                subscription.request(requested);
            };

            final Consumer<PartitionEvent> onNext = event -> {
                LOGGER.info("Event received");
                final int count = counter.incrementAndGet();
                if (count > requested) {
                    Assertions.fail("Shouldn't have more than " + requested + " events. Count: " + count);
                }
                if (count == requested) {
                    receiveLink.setEndpointCompleted();
                }
                semaphore.countDown();
            };

            final Consumer<Throwable> onError = error -> Assertions.fail(error.toString());
            final Runnable onComplete = () -> LOGGER.info("Complete");

            final Disposable subscription = consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest())
                .subscribe(onNext, onError, onComplete, onSubscribe);
            disposable.add(subscription);

            receiveLink.emitMessages(11);
            Assertions.assertTrue(semaphore.await(10, TimeUnit.SECONDS));
            Assertions.assertEquals(requested, counter.get());
        } finally {
            disposable.dispose();
            close(consumer);
        }
    }

    @Test
    void setsCorrectProperties() {
        final String endpointPrefix = "contoso";
        final String endpointSuffix
            = Configuration.getGlobalConfiguration().get("AZURE_EVENTHUBS_ENDPOINT_SUFFIX", DEFAULT_NAMESPACE_SUFFIX);
        final String entityPath = "orders-eh";
        final String con
            = String.format("Endpoint=sb://%s%s/;SharedAccessKeyName=nnn;SharedAccessKey=kkk;EntityPath=%s",
                endpointPrefix, endpointSuffix, entityPath);

        // Act and assert
        //
        try (EventHubConsumerAsyncClient consumer = new EventHubClientBuilder().connectionString(con)
            .consumerGroup(CONSUMER_GROUP)
            .buildAsyncConsumerClient()) {
            Assertions.assertEquals(entityPath, consumer.getEventHubName());
            Assertions.assertEquals(String.format("%s%s", endpointPrefix, endpointSuffix),
                consumer.getFullyQualifiedNamespace());
            Assertions.assertEquals(CONSUMER_GROUP, consumer.getConsumerGroup());
        }
    }

    @Test
    void receivesMultiplePartitions() {
        EventHubConsumerAsyncClient consumer = null;
        final String[] partitions = new String[] { "partition-0", "partition-1", "partition-2" };
        try (MockConnection connection = new MockConnection();
            MockManagementNode node = new MockManagementNode();
            MockReceiveLink receiveLink0 = new MockReceiveLink(partitions[0], messageTrackingUUID);
            MockReceiveLink receiveLink1 = new MockReceiveLink(partitions[1], messageTrackingUUID);
            MockReceiveLink receiveLink2 = new MockReceiveLink(partitions[2], messageTrackingUUID)) {
            final int numberOfEvents = 10;
            receiveLink0.arrange(numberOfEvents);
            receiveLink1.arrange(numberOfEvents);
            receiveLink2.arrange(numberOfEvents);
            node.arrange(new EventHubProperties(EVENT_HUB_NAME, Instant.EPOCH, partitions), null);
            connection.arrange(node, receiveLink0, receiveLink1, receiveLink2);
            final ConnectionCacheWrapper connectionCache = connection.wrapInCache();
            consumer = createConsumer(connectionCache, PREFETCH);

            // Act and assert
            connection.setEndpointActive();
            receiveLink0.setEndpointActive();
            receiveLink1.setEndpointActive();
            receiveLink2.setEndpointActive();

            StepVerifier.create(consumer.receive(true).filter(e -> isMatchingEvent(e, messageTrackingUUID)))
                .then(() -> receiveLink1.emitMessages(2))
                .assertNext(event -> assertPartition(partitions[1], event))
                .assertNext(event -> assertPartition(partitions[1], event))
                .then(() -> receiveLink2.emitMessages(1))
                .assertNext(event -> assertPartition(partitions[2], event))
                .then(() -> receiveLink1.emitMessages(1))
                .assertNext(event -> assertPartition(partitions[1], event))
                .thenCancel()
                .verify(TIMEOUT);

            verifyNoInteractions(onClientClosed);
        } finally {
            close(consumer);
        }
    }

    /**
     * Verifies that even if one link closes, it still continues to receive.
     */
    @Test
    void receivesMultiplePartitionsWhenOneCloses() {
        EventHubConsumerAsyncClient consumer = null;
        final String[] partitions = new String[] { "partition-0", "partition-1", "partition-2" };
        try (MockConnection connection = new MockConnection();
            MockManagementNode node = new MockManagementNode();
            MockReceiveLink receiveLink0 = new MockReceiveLink(partitions[0], messageTrackingUUID);
            MockReceiveLink receiveLink1 = new MockReceiveLink(partitions[1], messageTrackingUUID);
            MockReceiveLink receiveLink2 = new MockReceiveLink(partitions[2], messageTrackingUUID)) {
            final int numberOfEvents = 10;
            receiveLink0.arrange(numberOfEvents);
            receiveLink1.arrange(numberOfEvents);
            receiveLink2.arrange(numberOfEvents);
            node.arrange(new EventHubProperties(EVENT_HUB_NAME, Instant.EPOCH, partitions), null);
            connection.arrange(node, receiveLink0, receiveLink1, receiveLink2);
            final ConnectionCacheWrapper connectionCache = connection.wrapInCache();
            consumer = createConsumer(connectionCache, PREFETCH);

            // Act and assert
            //
            connection.setEndpointActive();
            receiveLink0.setEndpointActive();
            receiveLink1.setEndpointActive();
            receiveLink2.setEndpointActive();

            StepVerifier.create(consumer.receive(true).filter(e -> isMatchingEvent(e, messageTrackingUUID)))
                .then(() -> receiveLink1.emitMessages(2))
                .assertNext(event -> assertPartition(partitions[1], event))
                .assertNext(event -> assertPartition(partitions[1], event))
                .then(() -> receiveLink2.emitMessages(1))
                .assertNext(event -> assertPartition(partitions[2], event))
                .then(() -> {
                    receiveLink1.setEndpointCompleted();
                    // ^ terminate "partition1" and emit message to "partition0".
                    receiveLink0.emitMessages(1);
                })
                .assertNext(event -> assertPartition(partitions[0], event))
                .thenCancel()
                .verify(TIMEOUT);

            verifyNoInteractions(onClientClosed);
        } finally {
            close(consumer);
        }
    }

    /**
     * Verifies that when we have a shared connection, the consumer does not close that connection.
     */
    @Test
    void doesNotCloseSharedConnection() {
        try (MockConnection connection = new MockConnection()) {
            final boolean isSharedConnection = true;
            connection.arrange();
            final ConnectionCacheWrapper connectionCache = connection.wrapInCache();

            // Act and assert
            //
            final EventHubConsumerAsyncClient consumer = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
                connectionCache, messageSerializer, CONSUMER_GROUP, PREFETCH, isSharedConnection, onClientClosed,
                CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);

            consumer.close();

            Assertions.assertFalse(connectionCache.isDisposed());
            verify(connection.getInner(), never()).dispose();
            verify(onClientClosed).run();
        }
    }

    /**
     * Verifies that when we have a non-shared connection, the consumer closes that connection.
     */
    @Test
    void closesDedicatedConnection() {
        try (MockConnection connection = new MockConnection()) {
            final boolean isSharedConnection = false;
            connection.arrange();
            final ConnectionCacheWrapper connectionCache = connection.wrapInCache();

            // Act and assert
            //
            final EventHubConsumerAsyncClient consumer = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
                connectionCache, messageSerializer, CONSUMER_GROUP, PREFETCH, isSharedConnection, onClientClosed,
                CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);

            consumer.close();

            Assertions.assertTrue(connectionCache.isDisposed());
            verifyNoInteractions(onClientClosed);
        }
    }

    @Test
    void receiveReportsMetrics() {
        EventHubConsumerAsyncClient consumer = null;
        try (MockConnection connection = new MockConnection();
            MockReceiveLink receiveLink = new MockReceiveLink(PARTITION_ID, messageTrackingUUID)) {
            final int numberOfEvents = 2;
            receiveLink.arrange(numberOfEvents);
            connection.arrange(receiveLink);
            final ConnectionCacheWrapper connectionCache = connection.wrapInCache();
            final TestMeter meter = new TestMeter();
            final EventHubsConsumerInstrumentation instrumentation
                = new EventHubsConsumerInstrumentation(null, meter, HOSTNAME, EVENT_HUB_NAME, CONSUMER_GROUP, false);
            consumer = createConsumer(connectionCache, instrumentation, PREFETCH);

            // Act and assert
            //
            connection.setEndpointActive();
            receiveLink.setEndpointActive();

            final Flux<PartitionEvent> events = consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest())
                .filter(e -> isMatchingEvent(e, messageTrackingUUID))
                .take(numberOfEvents); // take(n) will cancels the stream (i.e. closes link) after n event.

            final Instant enqueuedTime = Instant.now().minusSeconds(1000);
            receiveLink.emitMessages(numberOfEvents, enqueuedTime);

            StepVerifier.create(events).thenConsumeWhile(e -> {
                final TestHistogram consumerLag = meter.getHistograms().get("messaging.eventhubs.consumer.lag");
                assertNotNull(consumerLag);
                final Instant afterReceived = Instant.now();

                final List<TestMeasurement<Double>> measurements = consumerLag.getMeasurements();
                final TestMeasurement<Double> last = measurements.get(measurements.size() - 1);
                assertEquals(Duration.between(enqueuedTime, afterReceived).toMillis() / 1000d, last.getValue(), 1);
                assertAllAttributes(HOSTNAME, EVENT_HUB_NAME, e.getPartitionContext().getPartitionId(), CONSUMER_GROUP,
                    null, null, last.getAttributes());
                return true;
            }).expectComplete().verify(DEFAULT_TIMEOUT);

            assertEquals(numberOfEvents,
                meter.getHistograms().get("messaging.eventhubs.consumer.lag").getMeasurements().size());
        } finally {
            close(consumer);
        }
    }

    @Test
    void receiveReportsMetricsNegativeLag() {
        EventHubConsumerAsyncClient consumer = null;
        try (MockConnection connection = new MockConnection();
            MockReceiveLink receiveLink = new MockReceiveLink(PARTITION_ID, messageTrackingUUID)) {
            receiveLink.arrange(1);
            connection.arrange(receiveLink);
            final ConnectionCacheWrapper connectionCache = connection.wrapInCache();
            final TestMeter meter = new TestMeter();
            final EventHubsConsumerInstrumentation instrumentation
                = new EventHubsConsumerInstrumentation(null, meter, HOSTNAME, EVENT_HUB_NAME, CONSUMER_GROUP, false);
            consumer = createConsumer(connectionCache, instrumentation, PREFETCH);

            // Act and assert
            //
            connection.setEndpointActive();
            receiveLink.setEndpointActive();

            final Flux<PartitionEvent> events = consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest())
                .filter(e -> isMatchingEvent(e, messageTrackingUUID))
                .take(1); // take(1) will cancels the stream (i.e. closes link) after 1 event.

            final Instant enqueuedTime = Instant.now().plusSeconds(1000);
            receiveLink.emitMessages(1, enqueuedTime);

            StepVerifier.create(events).consumeNextWith(e -> {
                final TestHistogram consumerLag = meter.getHistograms().get("messaging.eventhubs.consumer.lag");
                assertNotNull(consumerLag);

                final List<TestMeasurement<Double>> measurements = consumerLag.getMeasurements();
                final TestMeasurement<Double> last = measurements.get(measurements.size() - 1);
                assertEquals(0, last.getValue());
                assertAllAttributes(HOSTNAME, EVENT_HUB_NAME, e.getPartitionContext().getPartitionId(), CONSUMER_GROUP,
                    null, null, last.getAttributes());
            }).expectComplete().verify(DEFAULT_TIMEOUT);

            assertEquals(1, meter.getHistograms().get("messaging.eventhubs.consumer.lag").getMeasurements().size());
        } finally {
            close(consumer);
        }
    }

    @Test
    void receiveDoesNotReportDisabledMetrics() {
        EventHubConsumerAsyncClient consumer = null;
        try (MockConnection connection = new MockConnection();
            MockReceiveLink receiveLink = new MockReceiveLink(PARTITION_ID, messageTrackingUUID)) {
            receiveLink.arrange(1);
            connection.arrange(receiveLink);
            final ConnectionCacheWrapper connectionCache = connection.wrapInCache();
            final TestMeter meter = new TestMeter(false);
            final EventHubsConsumerInstrumentation instrumentation
                = new EventHubsConsumerInstrumentation(null, meter, HOSTNAME, EVENT_HUB_NAME, CONSUMER_GROUP, false);
            consumer = createConsumer(connectionCache, instrumentation, PREFETCH);

            // Act and assert
            //
            connection.setEndpointActive();
            receiveLink.setEndpointActive();

            Flux<PartitionEvent> receive = consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest())
                .filter(e -> isMatchingEvent(e, messageTrackingUUID))
                .take(1);

            receiveLink.emitMessages(1);
            StepVerifier.create(receive).expectNextCount(1).expectComplete().verify(DEFAULT_TIMEOUT);
            assertFalse(meter.getHistograms().containsKey("messaging.eventhubs.consumer.lag"));
        } finally {
            close(consumer);
        }
    }

    @Test
    void receiveNullMeterDoesNotThrow() {
        EventHubConsumerAsyncClient consumer = null;
        try (MockConnection connection = new MockConnection();
            MockReceiveLink receiveLink = new MockReceiveLink(PARTITION_ID, messageTrackingUUID)) {
            receiveLink.arrange(1);
            connection.arrange(receiveLink);
            final ConnectionCacheWrapper connectionCache = connection.wrapInCache();
            final EventHubsConsumerInstrumentation instrumentation
                = new EventHubsConsumerInstrumentation(null, null, HOSTNAME, EVENT_HUB_NAME, CONSUMER_GROUP, false);
            consumer = createConsumer(connectionCache, instrumentation, PREFETCH);

            // Act and assert
            //
            connection.setEndpointActive();
            receiveLink.setEndpointActive();

            Flux<PartitionEvent> receive = consumer.receiveFromPartition(PARTITION_ID, EventPosition.earliest())
                .filter(e -> isMatchingEvent(e, messageTrackingUUID))
                .take(1);

            receiveLink.emitMessages(1);
            StepVerifier.create(receive).expectNextCount(1).expectComplete().verify(DEFAULT_TIMEOUT);
        } finally {
            close(consumer);
        }
    }

    /**
     * Verifies tracing for getEventHubsProperties and getPartitionProperties
     */
    @Test
    void startSpanForGetProperties() {
        EventHubConsumerAsyncClient consumer = null;
        try (MockConnection connection = new MockConnection(); MockManagementNode node = new MockManagementNode()) {
            final EventHubProperties eventHubProperties
                = new EventHubProperties(EVENT_HUB_NAME, Instant.now(), new String[] { "0" });
            final PartitionProperties partitionProperties = new PartitionProperties(EVENT_HUB_NAME, "0", 1L, 2L,
                OffsetDateTime.now().toString(), Instant.now(), false);
            node.arrange(eventHubProperties, partitionProperties);
            connection.arrange(node, (MockReceiveLink) null);
            final ConnectionCacheWrapper connectionCache = connection.wrapInCache();

            final Tracer tracer = mock(Tracer.class);
            when(tracer.isEnabled()).thenReturn(true);
            EventHubsConsumerInstrumentation instrumentation
                = new EventHubsConsumerInstrumentation(tracer, null, HOSTNAME, EVENT_HUB_NAME, CONSUMER_GROUP, false);
            consumer = createConsumer(connectionCache, instrumentation, PREFETCH);

            final String expectedPartitionSpanName = getSpanName(GET_PARTITION_PROPERTIES, EVENT_HUB_NAME);
            when(tracer.start(eq(expectedPartitionSpanName), any(StartSpanOptions.class), any(Context.class)))
                .thenAnswer(invocation -> {
                    StartSpanOptions startOpts = invocation.getArgument(1, StartSpanOptions.class);
                    assertEquals(SpanKind.CLIENT, startOpts.getSpanKind());
                    assertAllAttributes(HOSTNAME, EVENT_HUB_NAME, PARTITION_ID, CONSUMER_GROUP, null,
                        GET_PARTITION_PROPERTIES, startOpts.getAttributes());
                    return invocation.getArgument(2, Context.class)
                        .addData(PARENT_TRACE_CONTEXT_KEY, "getPartitionProperties");
                });

            final String expectedHubSpanName = getSpanName(GET_EVENT_HUB_PROPERTIES, EVENT_HUB_NAME);
            when(tracer.start(eq(expectedHubSpanName), any(StartSpanOptions.class), any(Context.class)))
                .thenAnswer(invocation -> {
                    StartSpanOptions startOpts = invocation.getArgument(1, StartSpanOptions.class);
                    assertEquals(SpanKind.CLIENT, startOpts.getSpanKind());
                    assertAllAttributes(HOSTNAME, EVENT_HUB_NAME, null, CONSUMER_GROUP, null, GET_EVENT_HUB_PROPERTIES,
                        startOpts.getAttributes());
                    return invocation.getArgument(2, Context.class)
                        .addData(PARENT_TRACE_CONTEXT_KEY, "getEventHubsProperties");
                });

            // Act and assert
            //
            connection.setEndpointActive();

            StepVerifier.create(consumer.getEventHubProperties())
                .consumeNextWith(p -> assertSame(eventHubProperties, p))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);

            StepVerifier.create(consumer.getPartitionProperties(PARTITION_ID))
                .consumeNextWith(p -> assertSame(partitionProperties, p))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);

            //Assert
            verify(tracer, times(1)).start(eq(expectedPartitionSpanName), any(StartSpanOptions.class),
                any(Context.class));
            verify(tracer, times(1)).start(eq(expectedHubSpanName), any(StartSpanOptions.class), any(Context.class));
            verify(tracer, times(2)).end(isNull(), isNull(), any());

            verifyNoInteractions(onClientClosed);
        } finally {
            close(consumer);
        }
    }

    /**
     * Verifies that getPartitionProperties and getEventHubProperties retry transient errors
     */
    @Test
    void getPropertiesWithRetries() {
        EventHubConsumerAsyncClient consumer = null;
        final AtomicInteger tryCount = new AtomicInteger();
        final Function<EventHubManagementNode, Answer<Mono<EventHubManagementNode>>> nodeAnswerFunction = node -> {
            return (Answer<Mono<EventHubManagementNode>>) invocation -> {
                int count = tryCount.getAndIncrement();
                if (count == 0) {
                    return Mono.error(new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "Test-message",
                        new AmqpErrorContext("test-namespace")));
                } else if (count == 1) {
                    // Simulate a timeout on the second attempt, test should never wait for it to end anyway.
                    return Mono.delay(Duration.ofSeconds(100))
                        .then(Mono.error(new RuntimeException("should never happen")));
                } else {
                    return Mono.just(node);
                }
            };
        };

        try (MockConnection connection = new MockConnection();
            MockManagementNode node = new MockManagementNode(nodeAnswerFunction)) {
            final EventHubProperties properties
                = new EventHubProperties(EVENT_HUB_NAME, Instant.now(), new String[] { "0" });
            final PartitionProperties partitionProperties = new PartitionProperties(EVENT_HUB_NAME, "0", 1L, 2L,
                OffsetDateTime.now().toString(), Instant.now(), false);
            node.arrange(properties, partitionProperties);
            connection.arrange(node, (MockReceiveLink) null);
            final ConnectionCacheWrapper connectionCache = connection.wrapInCache();
            consumer = createConsumer(connectionCache, PREFETCH);

            // Act and assert
            //
            connection.setEndpointActive();

            tryCount.set(0);
            StepVerifier.create(consumer.getPartitionProperties("0"))
                .consumeNextWith(p -> assertSame(partitionProperties, p))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);
            assertEquals(3, tryCount.get());

            tryCount.set(0);
            StepVerifier.create(consumer.getEventHubProperties())
                .consumeNextWith(eh -> assertSame(properties, eh))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);

            assertEquals(3, tryCount.get());
        } finally {
            close(consumer);
        }
    }

    private static void close(EventHubConsumerAsyncClient consumer) {
        if (consumer != null) {
            consumer.close();
        }
    }

    private static void assertPartition(String partitionId, PartitionEvent event) {
        LOGGER.log(LogLevel.VERBOSE, () -> "Event received: " + event.getPartitionContext().getPartitionId());
        final Object value = event.getData().getProperties().get(PARTITION_ID_HEADER);
        Assertions.assertInstanceOf(String.class, value);
        Assertions.assertEquals(partitionId, value);

        Assertions.assertEquals(partitionId, event.getPartitionContext().getPartitionId());
        Assertions.assertEquals(EVENT_HUB_NAME, event.getPartitionContext().getEventHubName());
        Assertions.assertEquals(CONSUMER_GROUP, event.getPartitionContext().getConsumerGroup());
    }

    private EventHubConsumerAsyncClient createConsumer(ConnectionCacheWrapper connectionCache, int prefetch) {
        return new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionCache, messageSerializer,
            CONSUMER_GROUP, prefetch, true, onClientClosed, CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);
    }

    private EventHubConsumerAsyncClient createConsumer(ConnectionCacheWrapper connectionCache,
        EventHubsConsumerInstrumentation instrumentation, int prefetch) {
        return new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionCache, messageSerializer,
            CONSUMER_GROUP, prefetch, true, onClientClosed, CLIENT_IDENTIFIER, instrumentation);
    }

    private static final class MockConnection implements Closeable {
        private final EventHubReactorAmqpConnection connection;
        private final TestPublisher<AmqpEndpointState> endpointStates;

        MockConnection() {
            this.connection = mock(EventHubReactorAmqpConnection.class);
            this.endpointStates = TestPublisher.createCold();
        }

        void arrange() {
            when(connection.getEndpointStates()).thenReturn(endpointStates.flux());
            when(connection.connectAndAwaitToActive()).thenReturn(Mono.just(connection));
            when(connection.closeAsync()).thenReturn(Mono.empty());
        }

        void arrange(MockReceiveLink... links) {
            arrange();
            if (links != null) {
                final CreateReceiveLinkAnswer createLinkAnswer = new CreateReceiveLinkAnswer(links);
                when(connection.createReceiveLink(anyString(), anyString(), any(EventPosition.class),
                    any(ReceiveOptions.class), anyString())).thenAnswer(createLinkAnswer);
            }
        }

        void arrange(MockManagementNode node, MockReceiveLink... links) {
            arrange(links);
            when(connection.getManagementNode()).thenAnswer(node.getNodeAnswer());
        }

        EventHubReactorAmqpConnection getInner() {
            return connection;
        }

        ConnectionCacheWrapper wrapInCache() {
            final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(RETRY_OPTIONS);
            final ReactorConnectionCache<EventHubReactorAmqpConnection> cache = new ReactorConnectionCache<>(
                () -> connection, HOSTNAME, EVENT_HUB_NAME, retryPolicy, new HashMap<>(0));
            return new ConnectionCacheWrapper(cache);
        }

        void setEndpointActive() {
            endpointStates.next(AmqpEndpointState.ACTIVE);
        }

        @Override
        public void close() {
            Mockito.framework().clearInlineMock(connection);
        }

        private static final class CreateReceiveLinkAnswer implements Answer<Mono<AmqpReceiveLink>> {
            private final List<MockReceiveLink> links = new ArrayList<>();

            CreateReceiveLinkAnswer(MockReceiveLink... links) {
                Collections.addAll(this.links, links);
            }

            @Override
            public Mono<AmqpReceiveLink> answer(InvocationOnMock invocation) {
                final String name = invocation.getArgument(1);
                boolean foundTerminated = false;
                for (MockReceiveLink link : links) {
                    if (name.endsWith(link.getPartitionId())) {
                        if (link.isTerminated()) {
                            foundTerminated = true;
                            continue;
                        }
                        return Mono.just(link.getInner());
                    }
                }
                if (foundTerminated) {
                    return Mono.error(new IllegalArgumentException(
                        "link with entityPath " + name + " was found but was terminated."));
                } else {
                    return Mono.error(new IllegalArgumentException("No link with entityPath " + name));
                }
            }
        }
    }

    private static final class MockManagementNode implements AutoCloseable {
        private final EventHubManagementNode node;
        private final Answer<Mono<EventHubManagementNode>> getNodeAnswer;

        MockManagementNode() {
            this.node = mock(EventHubManagementNode.class);
            this.getNodeAnswer = invocation -> Mono.just(node);
        }

        MockManagementNode(Function<EventHubManagementNode, Answer<Mono<EventHubManagementNode>>> function) {
            this.node = mock(EventHubManagementNode.class);
            this.getNodeAnswer = function.apply(node);
        }

        void arrange(EventHubProperties properties, PartitionProperties partitionProperties) {
            if (properties != null) {
                when(node.getEventHubProperties()).thenReturn(Mono.just(properties));
            }
            if (partitionProperties != null) {
                when(node.getPartitionProperties(anyString())).thenReturn(Mono.just(partitionProperties));
            }
        }

        Answer<Mono<EventHubManagementNode>> getNodeAnswer() {
            return getNodeAnswer;
        }

        @Override
        public void close() {
            Mockito.framework().clearInlineMock(node);
        }
    }

    private static final class MockReceiveLink implements Closeable {
        private final AmqpReceiveLink link;
        private final String partitionId;
        private final String messageTrackingUUID;
        private final TestPublisher<AmqpEndpointState> endpointStates;
        private final TestPublisher<Message> messages;
        private final AddCreditAnswer addCreditAnswer;
        private final AtomicBoolean isTerminated = new AtomicBoolean(false);

        MockReceiveLink(String partitionId, String messageTrackingUUID) {
            this.link = mock(AmqpReceiveLink.class);
            this.partitionId = partitionId;
            this.messageTrackingUUID = messageTrackingUUID;
            this.endpointStates = TestPublisher.createCold();
            this.messages = TestPublisher.createCold();
            this.addCreditAnswer = new AddCreditAnswer();
        }

        @SuppressWarnings("unchecked")
        void arrange(int credits) {
            when(link.receive()).thenReturn(messages.flux().publishOn(Schedulers.boundedElastic()));
            when(link.getEndpointStates()).thenReturn(endpointStates.flux());
            when(link.getLinkName()).thenReturn(partitionId);
            when(link.getCredits()).thenReturn(credits);
            when(link.addCredits(anyInt())).thenReturn(Mono.empty());
            doAnswer(addCreditAnswer).when(link).addCredit(any(Supplier.class));
            when(link.closeAsync()).thenReturn(Mono.defer(() -> {
                isTerminated.set(true);
                return Mono.empty();
            }));
        }

        AmqpReceiveLink getInner() {
            return link;
        }

        String getPartitionId() {
            return partitionId;
        }

        List<Long> getCreditsList() {
            return addCreditAnswer.getCreditsList();
        }

        boolean isTerminated() {
            return isTerminated.get();
        }

        void setEndpointActive() {
            endpointStates.next(AmqpEndpointState.ACTIVE);
        }

        void setEndpointCompleted() {
            isTerminated.set(true);
            endpointStates.next(AmqpEndpointState.CLOSED);
            endpointStates.complete();
        }

        void setEndpointError(Throwable e) {
            isTerminated.set(true);
            endpointStates.error(e);
        }

        void emitMessages(int numberOfEvents) {
            emitMessages(numberOfEvents, null);
        }

        void emitMessages(int numberOfEvents, Instant enqueueTime) {
            for (int i = 0; i < numberOfEvents; i++) {
                final Message message = getMessage(PAYLOAD_BYTES, messageTrackingUUID);
                message.getApplicationProperties().getValue().put(PARTITION_ID_HEADER, partitionId);
                if (enqueueTime != null) {
                    message.getMessageAnnotations()
                        .getValue()
                        .put(Symbol.valueOf(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue()), enqueueTime);
                }
                messages.next(message);
            }
        }

        @Override
        public void close() {
            Mockito.framework().clearInlineMock(link);
        }

        private static final class AddCreditAnswer implements Answer<Void> {
            private final List<Long> creditsList = Collections.synchronizedList(new ArrayList<>());

            @Override
            public Void answer(InvocationOnMock invocation) {
                final Supplier<Long> creditSupplier = invocation.getArgument(0);
                final long credits = creditSupplier.get();
                creditsList.add(credits);
                return null;
            }

            List<Long> getCreditsList() {
                return creditsList;
            }
        }
    }
}
