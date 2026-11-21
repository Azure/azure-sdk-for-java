// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient.BufferedProducerClientOptions;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.SendBatchFailedContext;
import com.azure.messaging.eventhubs.models.SendBatchSucceededContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import reactor.util.concurrent.Queues;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.azure.messaging.eventhubs.EventDataAggregatorTest.setupBatchMock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link EventHubBufferedPartitionProducer}
 */
@Isolated
public class EventHubBufferedPartitionProducerTest {
    private static final ClientLogger LOGGER = new ClientLogger(EventHubBufferedPartitionProducerTest.class);

    private static final String PARTITION_ID = "10";
    private static final String NAMESPACE = "test-eventhubs-namespace";
    private static final String EVENT_HUB_NAME = "test-hub";
    private static final List<String> PARTITION_IDS = Arrays.asList("one", "two", PARTITION_ID, "four");
    private static final AmqpRetryOptions DEFAULT_RETRY_OPTIONS = new AmqpRetryOptions();
    private static final int QUEUE_SIZE = 5;

    private AutoCloseable mockCloseable;

    private final Semaphore successSemaphore = new Semaphore(1);
    private final Semaphore failedSemaphore = new Semaphore(1);
    private final EventData event1 = new EventData("foo");
    private final EventData event2 = new EventData("bar");
    private final EventData event3 = new EventData("baz");
    private final EventData event4 = new EventData("bart");

    private final Queue<EventDataBatch> returnedBatches = new LinkedList<>();

    @Mock
    private EventHubProducerAsyncClient client;

    @Mock
    private EventDataBatch batch;

    @Mock
    private EventDataBatch batch2;

    @Mock
    private EventDataBatch batch3;

    @Mock
    private EventDataBatch batch4;

    @Mock
    private EventDataBatch batch5;

    @Mock
    private Sinks.Many<EventData> mockedEventSink;

    private Sinks.Many<EventData> eventSink;
    private Queue<EventData> eventQueue;

    @BeforeEach
    public void beforeEach() {
        mockCloseable = MockitoAnnotations.openMocks(this);

        returnedBatches.add(batch);
        returnedBatches.add(batch2);
        returnedBatches.add(batch3);
        returnedBatches.add(batch4);
        returnedBatches.add(batch5);

        when(client.getFullyQualifiedNamespace()).thenReturn(NAMESPACE);
        when(client.getEventHubName()).thenReturn(EVENT_HUB_NAME);
        when(client.getPartitionIds()).thenReturn(Flux.fromIterable(PARTITION_IDS));

        when(client.createBatch(any(CreateBatchOptions.class))).thenAnswer(invocation -> {
            final EventDataBatch returned = returnedBatches.poll();
            assertNotNull(returned, "there should be more batches to be returned.");
            return Mono.just(returned);
        });

        this.eventQueue = Queues.<EventData>get(QUEUE_SIZE).get();
        this.eventSink = Sinks.many().unicast().onBackpressureBuffer(eventQueue);
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }

        Mockito.framework().clearInlineMock(this);
    }

    @Test
    public void publishesEvents() throws InterruptedException {
        // Arrange
        successSemaphore.acquire();
        failedSemaphore.acquire();

        final InvocationHolder holder = new InvocationHolder();
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofSeconds(5));
        options.setSendSucceededContext(holder::onSucceed);
        options.setSendFailedContext(holder::onFailed);

        final Duration waitTime = options.getMaxWaitTime().plus(options.getMaxWaitTime());

        final List<EventData> batchEvents = new ArrayList<>();
        setupBatchMock(batch, batchEvents, event1, event2);

        when(client.send(any(EventDataBatch.class))).thenReturn(Mono.empty());

        final EventHubBufferedPartitionProducer producer = new EventHubBufferedPartitionProducer(client, PARTITION_ID,
            options, DEFAULT_RETRY_OPTIONS, eventSink, null);

        // Act & Assert
        StepVerifier.create(producer.enqueueEvent(event1))
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        StepVerifier.create(producer.enqueueEvent(event2))
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        assertTrue(successSemaphore.tryAcquire(waitTime.toMillis(), TimeUnit.MILLISECONDS),
            "Should have been able to get a successful batch pushed downstream.");

        assertEquals(1, holder.succeededContexts.size());

        final SendBatchSucceededContext first = holder.succeededContexts.get(0);
        assertEquals(PARTITION_ID, first.getPartitionId());
        assertEquals(batchEvents, first.getEvents());

        assertEquals(2, batchEvents.size());

        assertTrue(holder.failedContexts.isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void publishesErrors() throws InterruptedException {
        // Arrange
        successSemaphore.acquire();
        failedSemaphore.acquire();

        final InvocationHolder holder = new InvocationHolder();
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofSeconds(5));
        options.setSendSucceededContext(holder::onSucceed);
        options.setSendFailedContext(holder::onFailed);

        final Duration waitTime = options.getMaxWaitTime().plus(options.getMaxWaitTime());

        final List<EventData> batchEvents = new ArrayList<>();
        setupBatchMock(batch, batchEvents, event1, event2);

        final List<EventData> batchEvents2 = new ArrayList<>();
        setupBatchMock(batch2, batchEvents2, event3, event4);

        final Throwable error = new IllegalStateException("test-options.");
        when(client.send(any(EventDataBatch.class))).thenReturn(Mono.empty(), Mono.error(error));

        final EventHubBufferedPartitionProducer producer = new EventHubBufferedPartitionProducer(client, PARTITION_ID,
            options, DEFAULT_RETRY_OPTIONS, eventSink, null);

        // Act & Assert
        StepVerifier.create(Mono.when(producer.enqueueEvent(event1), producer.enqueueEvent(event2)))
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        StepVerifier.create(Mono.when(producer.enqueueEvent(event3)))
            .thenAwait(options.getMaxWaitTime())
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        assertTrue(successSemaphore.tryAcquire(waitTime.toMillis(), TimeUnit.MILLISECONDS),
            "Should have been able to get a successful signal downstream.");

        assertTrue(failedSemaphore.tryAcquire(waitTime.toMillis(), TimeUnit.MILLISECONDS),
            "Should have been able to get a successful error downstream.");

        assertEquals(1, holder.succeededContexts.size());

        final SendBatchSucceededContext first = holder.succeededContexts.get(0);
        assertEquals(PARTITION_ID, first.getPartitionId());
        assertEquals(batchEvents, first.getEvents());

        assertEquals(2, batchEvents.size());

        assertEquals(1, holder.failedContexts.size());
    }

    /**
     * Checks that after an error publishing one batch, it can still publish subsequent batches successfully.
     */
    @Test
    public void canPublishAfterErrors() throws InterruptedException {
        // Arrange
        final CountDownLatch success = new CountDownLatch(2);
        failedSemaphore.acquire();

        final InvocationHolder holder = new InvocationHolder();
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofSeconds(5));
        options.setSendSucceededContext(context -> {
            holder.onSucceed(context);
            success.countDown();
        });
        options.setSendFailedContext(context -> holder.onFailed(context));

        final Duration waitTime = options.getMaxWaitTime().plus(options.getMaxWaitTime());

        final List<EventData> batchEvents = new ArrayList<>();
        setupBatchMock(batch, batchEvents, event1, event2);

        final List<EventData> batchEvents2 = new ArrayList<>();
        setupBatchMock(batch2, batchEvents2, event3);

        final List<EventData> batchEvents3 = new ArrayList<>();
        final EventData event5 = new EventData("five");
        setupBatchMock(batch3, batchEvents3, event4, event5);

        final Throwable error = new IllegalStateException("test-options.");

        final Queue<Mono<Void>> responses = new LinkedList<>();
        responses.add(Mono.empty());
        responses.add(Mono.error(error));
        responses.add(Mono.empty());
        when(client.send(any(EventDataBatch.class))).thenAnswer(invocation -> {
            return responses.poll();
        });

        final EventHubBufferedPartitionProducer producer = new EventHubBufferedPartitionProducer(client, PARTITION_ID,
            options, DEFAULT_RETRY_OPTIONS, eventSink, null);

        // Act & Assert
        StepVerifier.create(Mono.when(producer.enqueueEvent(event1), producer.enqueueEvent(event2)))
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        StepVerifier.create(producer.enqueueEvent(event3))
            .thenAwait(options.getMaxWaitTime())
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        StepVerifier.create(Mono.when(producer.enqueueEvent(event4), producer.enqueueEvent(event5)))
            .thenAwait(options.getMaxWaitTime())
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        assertTrue(success.await(waitTime.toMillis(), TimeUnit.MILLISECONDS),
            "Should have been able to get a successful signal downstream.");

        assertTrue(failedSemaphore.tryAcquire(waitTime.toMillis(), TimeUnit.MILLISECONDS),
            "Should have been able to get a successful error downstream.");

        assertEquals(2, holder.succeededContexts.size());

        // Verify the completed ones.
        final SendBatchSucceededContext first = holder.succeededContexts.get(0);
        assertEquals(PARTITION_ID, first.getPartitionId());
        assertEquals(batchEvents, first.getEvents());

        final SendBatchSucceededContext second = holder.succeededContexts.get(1);
        assertEquals(PARTITION_ID, second.getPartitionId());
        assertEquals(batchEvents3, second.getEvents());

        // Verify the failed ones.
        assertEquals(1, holder.failedContexts.size());
    }

    @Test
    public void getBufferedEventCounts() throws InterruptedException {
        // Arrange
        final CountDownLatch success = new CountDownLatch(1);
        failedSemaphore.acquire();

        final InvocationHolder holder = new InvocationHolder();
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofSeconds(5));
        options.setSendSucceededContext(context -> {
            LOGGER.log(LogLevel.VERBOSE, () -> "Batch received.");
            holder.onSucceed(context);
            success.countDown();
        });
        options.setSendFailedContext(context -> holder.onFailed(context));

        final List<EventData> batchEvents = new ArrayList<>();
        setupBatchMock(batch, batchEvents, event1);

        final List<EventData> batchEvents2 = new ArrayList<>();
        setupBatchMock(batch2, batchEvents2, event2, event3);

        final List<EventData> batchEvents3 = new ArrayList<>();
        final EventData event5 = new EventData("five");
        setupBatchMock(batch3, batchEvents3, event4, event5);

        final List<EventData> batchEvents4 = new ArrayList<>();
        setupBatchMock(batch4, batchEvents4, event2, event3, event4, event5);

        final List<EventData> batchEvents5 = new ArrayList<>();
        setupBatchMock(batch5, batchEvents5, event2, event3, event4, event5);

        // Delaying send operation.
        when(client.send(any(EventDataBatch.class)))
            .thenAnswer(invocation -> Mono.delay(options.getMaxWaitTime()).then());

        final EventHubBufferedPartitionProducer producer = new EventHubBufferedPartitionProducer(client, PARTITION_ID,
            options, DEFAULT_RETRY_OPTIONS, eventSink, null);

        // Act & Assert
        StepVerifier
            .create(
                Mono.when(producer.enqueueEvent(event1), producer.enqueueEvent(event2), producer.enqueueEvent(event3)),
                1L)
            .then(() -> {
                // event1 was enqueued, event2 is in a batch, and event3 is currently in the queue waiting to be
                // pushed downstream.
                final int bufferedEventCount = producer.getBufferedEventCount();
                assertEquals(2, bufferedEventCount);
            })
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        StepVerifier.create(Mono.when(producer.enqueueEvent(event4), producer.enqueueEvent(event5)))
            .thenAwait(options.getMaxWaitTime())
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        assertTrue(success.await(DEFAULT_RETRY_OPTIONS.getTryTimeout().toMillis(), TimeUnit.MILLISECONDS),
            "Should have been able to get a successful signal downstream.");

        assertTrue(1 <= holder.succeededContexts.size(),
            "Expected at least 1 succeeded contexts. Actual: " + holder.succeededContexts.size());

        // Verify the completed ones.
        final SendBatchSucceededContext first = holder.succeededContexts.get(0);
        assertEquals(PARTITION_ID, first.getPartitionId());
        assertEquals(batchEvents, first.getEvents());

        if (holder.succeededContexts.size() > 1) {
            final SendBatchSucceededContext second = holder.succeededContexts.get(1);
            assertEquals(PARTITION_ID, second.getPartitionId());
            assertEquals(batchEvents2, second.getEvents());
        }
    }

    public static Stream<Sinks.EmitResult> retryAfterEmitResultError() {
        return Stream.of(Sinks.EmitResult.FAIL_NON_SERIALIZED, Sinks.EmitResult.FAIL_OVERFLOW);
    }

    /**
     * Checks that if there are errors when emitting to sink, will try again.
     */
    @MethodSource("retryAfterEmitResultError")
    @ParameterizedTest
    public void retryAfterEmitResultError(Sinks.EmitResult emitResult) {
        // Arrange
        final InvocationHolder holder = new InvocationHolder();
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofSeconds(5));
        options.setSendSucceededContext(context -> holder.onSucceed(context));
        options.setSendFailedContext(context -> holder.onFailed(context));

        when(mockedEventSink.asFlux()).thenReturn(Flux.never());
        when(mockedEventSink.tryEmitNext(event1)).thenReturn(Sinks.EmitResult.OK);

        // Return an error emit result, then try again
        when(mockedEventSink.tryEmitNext(event2)).thenReturn(emitResult, Sinks.EmitResult.OK);
        when(mockedEventSink.tryEmitNext(event3)).thenReturn(Sinks.EmitResult.OK);

        final List<EventData> batchEvents = new ArrayList<>();
        setupBatchMock(batch, batchEvents, event1, event2);

        when(client.send(any(EventDataBatch.class))).thenAnswer(invocation -> Mono.empty());

        final EventHubBufferedPartitionProducer producer = new EventHubBufferedPartitionProducer(client, PARTITION_ID,
            options, DEFAULT_RETRY_OPTIONS, mockedEventSink, null);

        // Act & Assert
        StepVerifier.create(producer.enqueueEvent(event1))
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        StepVerifier.create(producer.enqueueEvent(event2))
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        StepVerifier.create(producer.enqueueEvent(event3))
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        // Assert
        verify(mockedEventSink).tryEmitNext(event1);
        verify(mockedEventSink, times(2)).tryEmitNext(event2);
        verify(mockedEventSink).tryEmitNext(event3);
    }

    @Test
    public void exhaustsRetries() {
        // Arrange
        final InvocationHolder holder = new InvocationHolder();
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofSeconds(5));
        options.setSendSucceededContext(context -> holder.onSucceed(context));
        options.setSendFailedContext(context -> holder.onFailed(context));

        when(mockedEventSink.asFlux()).thenReturn(Flux.never());
        when(mockedEventSink.tryEmitNext(event1)).thenReturn(Sinks.EmitResult.OK);

        // Return an error emit result, then try again
        when(mockedEventSink.tryEmitNext(event2)).thenReturn(Sinks.EmitResult.FAIL_OVERFLOW,
            Sinks.EmitResult.FAIL_NON_SERIALIZED, Sinks.EmitResult.FAIL_OVERFLOW, Sinks.EmitResult.OK);
        when(mockedEventSink.tryEmitNext(event3)).thenReturn(Sinks.EmitResult.OK);

        final List<EventData> batchEvents = new ArrayList<>();
        setupBatchMock(batch, batchEvents, event1, event2);

        when(client.send(any(EventDataBatch.class))).thenAnswer(invocation -> Mono.empty());

        final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setMaxRetries(2).setDelay(Duration.ofSeconds(2));

        final EventHubBufferedPartitionProducer producer
            = new EventHubBufferedPartitionProducer(client, PARTITION_ID, options, retryOptions, mockedEventSink, null);

        // Act & Assert
        StepVerifier.create(producer.enqueueEvent(event1))
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        StepVerifier.create(producer.enqueueEvent(event2)).consumeErrorWith(error -> {
            assertTrue(error instanceof AmqpException);

            final AmqpException amqpException = (AmqpException) error;
            assertTrue(amqpException.isTransient());
        }).verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        StepVerifier.create(producer.enqueueEvent(event3))
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        // Assert
        verify(mockedEventSink).tryEmitNext(event1);
        verify(mockedEventSink, times(3)).tryEmitNext(event2);
        verify(mockedEventSink).tryEmitNext(event3);
    }

    /**
     * Checks that an exception thrown by the user's send-succeeded handler does not terminate publishing to the
     * partition.
     */
    @Test
    public void continuesPublishingWhenHandlerThrows() throws InterruptedException {
        // Arrange
        final CountDownLatch success = new CountDownLatch(2);
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofSeconds(2));
        options.setSendSucceededContext(context -> {
            success.countDown();
            throw new IllegalStateException("handler failure");
        });
        options.setSendFailedContext(context -> {
        });

        final List<EventData> batchEvents = new ArrayList<>();
        setupBatchMock(batch, batchEvents, event1);

        final List<EventData> batchEvents2 = new ArrayList<>();
        setupBatchMock(batch2, batchEvents2, event2);

        when(client.send(any(EventDataBatch.class))).thenReturn(Mono.empty());

        final EventHubBufferedPartitionProducer producer = new EventHubBufferedPartitionProducer(client, PARTITION_ID,
            options, DEFAULT_RETRY_OPTIONS, eventSink, null);

        // Act
        StepVerifier.create(producer.enqueueEvent(event1))
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        // event2 does not fit into the first batch, so the first batch is sent and its handler throws.
        StepVerifier.create(producer.enqueueEvent(event2))
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        // Assert
        assertTrue(success.await(10, TimeUnit.SECONDS),
            "The second batch should be published even though the handler threw for the first one.");
        assertEquals(Collections.singletonList(event2), batchEvents2);
    }

    /**
     * Checks that the send-failed handler is never invoked concurrently for a partition, nor on the enqueuing thread,
     * when a batch fails to send while another event fails to be added to a batch.
     */
    @Test
    public void sendFailedHandlerIsNotInvokedConcurrently() throws InterruptedException {
        // Arrange
        final AtomicInteger inFlight = new AtomicInteger();
        final AtomicInteger maxInFlight = new AtomicInteger();
        final List<Thread> handlerThreads = Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch failures = new CountDownLatch(2);
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofSeconds(30));
        options.setSendSucceededContext(context -> {
        });
        options.setSendFailedContext(context -> {
            maxInFlight.accumulateAndGet(inFlight.incrementAndGet(), Math::max);
            handlerThreads.add(Thread.currentThread());
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                inFlight.decrementAndGet();
                failures.countDown();
            }
        });

        // event3 is too large for any batch, so it fills batch, sends it (which fails), then fails batch2 as well.
        setupOversizedEventBatchMock(batch, new ArrayList<>(), event3, event1);
        setupOversizedEventBatchMock(batch2, new ArrayList<>(), event3);

        when(client.send(any(EventDataBatch.class)))
            .thenReturn(Mono.error(new AmqpException(false, "send failed", null)));

        final EventHubBufferedPartitionProducer producer = new EventHubBufferedPartitionProducer(client, PARTITION_ID,
            options, DEFAULT_RETRY_OPTIONS, eventSink, null);

        // Act
        StepVerifier.create(producer.enqueueEvent(event1))
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());
        StepVerifier.create(producer.enqueueEvent(event3))
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        // Assert
        assertTrue(failures.await(10, TimeUnit.SECONDS), "Both failures should be reported.");
        assertEquals(1, maxInFlight.get(), "onSendBatchFailed should not be invoked concurrently.");
        assertFalse(handlerThreads.contains(Thread.currentThread()),
            "onSendBatchFailed should not be invoked on the enqueuing thread.");
    }

    /**
     * Checks that an exception thrown by the user's send-failed handler after a failed send does not terminate
     * publishing to the partition.
     */
    @Test
    public void continuesPublishingWhenFailedHandlerThrowsAfterSendFailure() throws InterruptedException {
        // Arrange
        final CountDownLatch failed = new CountDownLatch(1);
        final CountDownLatch succeeded = new CountDownLatch(1);
        final List<SendBatchSucceededContext> succeededContexts = Collections.synchronizedList(new ArrayList<>());
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofSeconds(2));
        options.setSendSucceededContext(context -> {
            succeededContexts.add(context);
            succeeded.countDown();
        });
        options.setSendFailedContext(context -> {
            failed.countDown();
            throw new IllegalStateException("handler failure");
        });

        setupBatchMock(batch, new ArrayList<>(), event1);
        final List<EventData> batchEvents2 = new ArrayList<>();
        setupBatchMock(batch2, batchEvents2, event2);

        when(client.send(batch)).thenReturn(Mono.error(new AmqpException(false, "send failed", null)));
        when(client.send(batch2)).thenReturn(Mono.empty());

        final EventHubBufferedPartitionProducer producer = new EventHubBufferedPartitionProducer(client, PARTITION_ID,
            options, DEFAULT_RETRY_OPTIONS, eventSink, null);

        // Act
        StepVerifier.create(producer.enqueueEvent(event1))
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        // event2 does not fit into the first batch, so the first batch is sent; the send and then its handler fail.
        StepVerifier.create(producer.enqueueEvent(event2))
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        // Assert
        assertTrue(failed.await(10, TimeUnit.SECONDS), "The failed send should be reported.");
        assertTrue(succeeded.await(10, TimeUnit.SECONDS),
            "A later event should be published even though onSendBatchFailed threw.");
        assertEquals(Collections.singletonList(event2), succeededContexts.get(0).getEvents());
    }

    /**
     * Checks that an exception thrown by the user's send-failed handler for an event that is too large for an empty
     * batch does not terminate publishing to the partition.
     */
    @Test
    public void continuesPublishingWhenFailedHandlerThrowsForOversizedEvent() throws InterruptedException {
        // Arrange
        final List<SendBatchFailedContext> failedContexts = Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch failed = new CountDownLatch(1);
        final CountDownLatch succeeded = new CountDownLatch(1);
        final List<SendBatchSucceededContext> succeededContexts = Collections.synchronizedList(new ArrayList<>());
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofSeconds(2));
        options.setSendSucceededContext(context -> {
            succeededContexts.add(context);
            succeeded.countDown();
        });
        options.setSendFailedContext(context -> {
            failedContexts.add(context);
            failed.countDown();
            throw new IllegalStateException("handler failure");
        });

        setupOversizedEventBatchMock(batch, new ArrayList<>(), event3, event1);
        setupOversizedEventBatchMock(batch2, new ArrayList<>(), event3, event1);

        when(client.send(any(EventDataBatch.class))).thenReturn(Mono.empty());

        final EventHubBufferedPartitionProducer producer = new EventHubBufferedPartitionProducer(client, PARTITION_ID,
            options, DEFAULT_RETRY_OPTIONS, eventSink, null);

        // Act
        StepVerifier.create(producer.enqueueEvent(event3))
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());
        assertTrue(failed.await(10, TimeUnit.SECONDS), "The oversized event should be reported.");

        StepVerifier.create(producer.enqueueEvent(event1))
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        // Assert
        assertTrue(succeeded.await(10, TimeUnit.SECONDS),
            "A later event should be published even though onSendBatchFailed threw.");
        assertEquals(Collections.singletonList(event1), succeededContexts.get(0).getEvents());
        assertEquals(Collections.singletonList(event3), failedContexts.get(0).getEvents());
    }

    /**
     * Checks that reporting an event that could not be added to a batch does not complete a pending flush before the
     * flushed batch is sent.
     */
    @Test
    public void unbatchedEventFailureDoesNotCompleteFlush() throws Exception {
        // Arrange
        final CountDownLatch handlerEntered = new CountDownLatch(1);
        final CountDownLatch releaseHandler = new CountDownLatch(1);
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofSeconds(30));
        options.setSendSucceededContext(context -> {
        });
        options.setSendFailedContext(context -> {
            handlerEntered.countDown();
            try {
                releaseHandler.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        setupOversizedEventBatchMock(batch, new ArrayList<>(), event3, event1);
        setupOversizedEventBatchMock(batch2, new ArrayList<>(), event3, event1);

        final Sinks.Empty<Void> sendResult = Sinks.empty();
        when(client.send(any(EventDataBatch.class))).thenReturn(sendResult.asMono());

        final EventHubBufferedPartitionProducer producer = new EventHubBufferedPartitionProducer(client, PARTITION_ID,
            options, DEFAULT_RETRY_OPTIONS, eventSink, null);

        StepVerifier.create(producer.enqueueEvent(event3))
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());
        assertTrue(handlerEntered.await(10, TimeUnit.SECONDS), "The oversized event should be reported.");
        StepVerifier.create(producer.enqueueEvent(event1))
            .expectComplete()
            .verify(DEFAULT_RETRY_OPTIONS.getTryTimeout());

        // Act
        final CompletableFuture<Void> flush = producer.flush().toFuture();
        verify(client, timeout(10000)).send(any(EventDataBatch.class));
        releaseHandler.countDown();

        // Assert
        TimeUnit.MILLISECONDS.sleep(500);
        assertFalse(flush.isDone(), "The flush should wait until the flushed batch is sent.");

        sendResult.tryEmitEmpty();
        flush.get(10, TimeUnit.SECONDS);
    }

    /**
     * Sets up a batch that throws for {@code oversizedEvent}, like a real batch does for an event that exceeds the
     * maximum message size, and otherwise accepts {@code acceptedEvents}.
     */
    private static void setupOversizedEventBatchMock(EventDataBatch batch, List<EventData> resultSet,
        EventData oversizedEvent, EventData... acceptedEvents) {
        when(batch.tryAdd(any(EventData.class))).thenAnswer(invocation -> {
            final EventData arg = invocation.getArgument(0);
            if (arg == oversizedEvent) {
                throw new AmqpException(false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, "too large", null);
            }
            final boolean matches = Arrays.asList(acceptedEvents).contains(arg);
            if (matches) {
                resultSet.add(arg);
            }
            return matches;
        });
        when(batch.getEvents()).thenAnswer(invocation -> resultSet);
        when(batch.getCount()).thenAnswer(invocation -> resultSet.size());
    }

    private class InvocationHolder {
        private final List<SendBatchSucceededContext> succeededContexts = new ArrayList<>();
        private final List<SendBatchFailedContext> failedContexts = new ArrayList<>();

        void onSucceed(SendBatchSucceededContext result) {
            succeededContexts.add(result);
            successSemaphore.release();
        }

        void onFailed(SendBatchFailedContext result) {
            failedContexts.add(result);
            failedSemaphore.release();
        }
    }
}
