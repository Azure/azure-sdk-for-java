// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.concurrent.Queues;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
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
import static org.mockito.Mockito.never;
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

    @ParameterizedTest
    @ValueSource(strings = { "success", "sendFailure", "oversized" })
    public void continuesPublishingAfterHandlerThrows(String scenario) throws InterruptedException {
        final CountDownLatch callbackInvoked = new CountDownLatch(1);
        final CountDownLatch recovered = new CountDownLatch(1);
        final List<EventData> published = new CopyOnWriteArrayList<>();
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofMinutes(1));
        options.setSendSucceededContext(context -> {
            if (context.getEvents().iterator().next() == event1) {
                callbackInvoked.countDown();
                throw new IllegalStateException("Success handler failed.");
            }
            context.getEvents().forEach(published::add);
            recovered.countDown();
        });
        options.setSendFailedContext(context -> {
            callbackInvoked.countDown();
            throw new IllegalStateException("Failure handler failed.");
        });

        final List<EventData> firstEvents = new ArrayList<>();
        setupBatchMock(batch, firstEvents, event1);
        setupBatchMock(batch2, new ArrayList<>(), event2);
        if ("oversized".equals(scenario)) {
            when(batch.tryAdd(event1)).thenThrow(new AmqpException(false, "Event is too large.", null));
            when(batch.tryAdd(event2)).thenAnswer(invocation -> firstEvents.add(event2));
        }
        when(client.send(any(EventDataBatch.class))).thenAnswer(invocation -> {
            final EventDataBatch sent = invocation.getArgument(0);
            return "sendFailure".equals(scenario) && sent == batch
                ? Mono.error(new AmqpException(true, "Transient send failure.", null))
                : Mono.empty();
        });
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(3));

        try (EventHubBufferedPartitionProducer producer
            = new EventHubBufferedPartitionProducer(client, PARTITION_ID, options, retryOptions, eventSink, null)) {
            producer.enqueueEvent(event1).block(Duration.ofSeconds(5));
            producer.flush().block(Duration.ofSeconds(5));
            assertTrue(callbackInvoked.await(5, TimeUnit.SECONDS));

            StepVerifier.create(producer.enqueueEvent(event2)).expectComplete().verify(Duration.ofSeconds(5));
            producer.flush().block(Duration.ofSeconds(5));
            assertTrue(recovered.await(5, TimeUnit.SECONDS));
            assertEquals(Arrays.asList(event2), published);
        }
    }

    @Test
    public void serializesEventFailuresWithoutCompletingPendingFlush() throws InterruptedException {
        final Sinks.Empty<Void> send = Sinks.empty();
        final CountDownLatch sendStarted = new CountDownLatch(1);
        final CountDownLatch failureStarted = new CountDownLatch(1);
        final CountDownLatch releaseFailure = new CountDownLatch(1);
        final CountDownLatch failureReturned = new CountDownLatch(1);
        final AtomicInteger active = new AtomicInteger();
        final AtomicInteger maximum = new AtomicInteger();
        final List<Thread> callbackThreads = new CopyOnWriteArrayList<>();
        final Thread enqueuingThread = Thread.currentThread();
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofMillis(20));
        options.setSendSucceededContext(ignored -> {
        });
        options.setSendFailedContext(context -> {
            callbackThreads.add(Thread.currentThread());
            maximum.accumulateAndGet(active.incrementAndGet(), Math::max);
            try {
                if (context.getEvents().iterator().next() == event2) {
                    failureStarted.countDown();
                    assertTrue(releaseFailure.await(5, TimeUnit.SECONDS));
                    failureReturned.countDown();
                }
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(error);
            } finally {
                active.decrementAndGet();
            }
        });
        setupBatchMock(batch, new ArrayList<>(), event1);
        setupBatchMock(batch2, new ArrayList<>(), event3);
        when(batch2.tryAdd(event2)).thenThrow(new AmqpException(false, "Event is too large.", null));
        when(client.send(batch)).thenAnswer(invocation -> {
            sendStarted.countDown();
            return send.asMono();
        });
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(3));
        try (EventHubBufferedPartitionProducer producer
            = new EventHubBufferedPartitionProducer(client, PARTITION_ID, options, retryOptions, eventSink, null)) {
            producer.enqueueEvent(event1).block(Duration.ofSeconds(5));
            assertTrue(sendStarted.await(5, TimeUnit.SECONDS));
            producer.enqueueEvent(event2).block(Duration.ofSeconds(5));
            assertTrue(failureStarted.await(5, TimeUnit.SECONDS));

            StepVerifier.create(producer.flush()).expectSubscription().then(() -> {
                releaseFailure.countDown();
                try {
                    assertTrue(failureReturned.await(5, TimeUnit.SECONDS));
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(error);
                }
            })
                .expectNoEvent(Duration.ofMillis(100))
                .then(() -> send.tryEmitError(new AmqpException(true, "Send failed.", null)))
                .expectComplete()
                .verify(Duration.ofSeconds(5));
            assertEquals(2, callbackThreads.size());
            assertEquals(1, maximum.get());
            assertFalse(callbackThreads.contains(enqueuingThread));
        } finally {
            releaseFailure.countDown();
            send.tryEmitEmpty();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void failedHandlerBackpressuresInvalidEvents(boolean afterTimerSends) throws InterruptedException {
        final CountDownLatch handlerEntered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final AtomicInteger failures = new AtomicInteger();
        final Semaphore successes = new Semaphore(0);
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(afterTimerSends ? Duration.ofMillis(20) : Duration.ofMinutes(1));
        options.setSendSucceededContext(ignored -> successes.release());
        options.setSendFailedContext(context -> {
            failures.incrementAndGet();
            handlerEntered.countDown();
            try {
                assertTrue(release.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(error);
            }
        });
        final EventDataBatch rejectingBatch = afterTimerSends ? batch4 : batch;
        if (afterTimerSends) {
            setupBatchMock(batch, new ArrayList<>(), event1);
            setupBatchMock(batch2, new ArrayList<>(), event2);
            setupBatchMock(batch3, new ArrayList<>(), event3);
            setupBatchMock(batch4, new ArrayList<>());
            when(client.send(any(EventDataBatch.class))).thenReturn(Mono.empty());
        }
        when(rejectingBatch.tryAdd(any(EventData.class)))
            .thenThrow(new AmqpException(false, "Event is too large.", null));
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(3));
        try (EventHubBufferedPartitionProducer producer
            = new EventHubBufferedPartitionProducer(client, PARTITION_ID, options, retryOptions, eventSink, null)) {
            if (afterTimerSends) {
                for (EventData event : Arrays.asList(event1, event2, event3)) {
                    producer.enqueueEvent(event).block(Duration.ofSeconds(5));
                    assertTrue(successes.tryAcquire(5, TimeUnit.SECONDS));
                }
            }
            producer.enqueueEvent(event1).block(Duration.ofSeconds(5));
            assertTrue(handlerEntered.await(5, TimeUnit.SECONDS));
            producer.enqueueEvent(event2).block(Duration.ofSeconds(5));
            producer.enqueueEvent(event3).block(Duration.ofSeconds(5));
            verify(rejectingBatch, never()).tryAdd(event2);
            verify(rejectingBatch, never()).tryAdd(event3);
            assertEquals(2, eventQueue.size());
            release.countDown();
            producer.flush().block(Duration.ofSeconds(5));
            assertEquals(3, failures.get());
        } finally {
            release.countDown();
        }
    }

    @Test
    public void resumesFailedEventDemandOutsideCompletionScheduler() throws InterruptedException {
        final Scheduler completionScheduler = Schedulers.newBoundedElastic(1, 10, "batch-completion");
        final CountDownLatch handlerEntered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final CountDownLatch recovered = new CountDownLatch(1);
        final AtomicInteger attempts = new AtomicInteger();
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofMillis(20));
        options.setSendSucceededContext(ignored -> recovered.countDown());
        options.setSendFailedContext(context -> {
            handlerEntered.countDown();
            try {
                assertTrue(release.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(error);
            }
        });
        setupBatchMock(batch, new ArrayList<>(), event2);
        setupBatchMock(batch2, new ArrayList<>());
        when(client.createBatch(any(CreateBatchOptions.class))).thenAnswer(invocation -> {
            final int attempt = attempts.getAndIncrement();
            if (attempt < 2) {
                return Mono.error(new IllegalStateException("Batch creation failed."));
            }
            return Mono.just(attempt == 2 ? batch : batch2).publishOn(completionScheduler);
        });
        when(client.send(batch)).thenReturn(Mono.empty());
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(3));
        EventHubBufferedPartitionProducer producer = null;
        try {
            producer = new EventHubBufferedPartitionProducer(client, PARTITION_ID, options, retryOptions, eventSink,
                null, completionScheduler);
            producer.enqueueEvent(event1).block(Duration.ofSeconds(5));
            assertTrue(handlerEntered.await(5, TimeUnit.SECONDS));
            producer.enqueueEvent(event2).block(Duration.ofSeconds(5));
            release.countDown();
            assertTrue(recovered.await(5, TimeUnit.SECONDS), "Batch creation must complete on the callback scheduler.");
            producer.flush().block(Duration.ofSeconds(5));
        } finally {
            release.countDown();
            completionScheduler.dispose();
            if (producer != null) {
                producer.close();
            }
        }
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
