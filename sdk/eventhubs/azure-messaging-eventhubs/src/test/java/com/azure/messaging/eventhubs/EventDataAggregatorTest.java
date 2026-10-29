// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient.BufferedProducerClientOptions;
import com.azure.messaging.eventhubs.implementation.UncheckedExecutionException;
import com.azure.messaging.eventhubs.models.SendBatchFailedContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.Disposable;
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import reactor.util.context.Context;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class EventDataAggregatorTest {
    private static final ClientLogger LOGGER = new ClientLogger(EventDataAggregatorTest.class);

    private static final String NAMESPACE = "test.namespace";
    private static final String PARTITION_ID = "test-id";

    private AutoCloseable mockCloseable;

    @Mock
    private EventDataBatch batch;

    @Mock
    private EventDataBatch batch2;

    @Mock
    private EventDataBatch batch3;

    private final EventData event1 = new EventData("foo");
    private final EventData event2 = new EventData("bar");
    private final EventData event3 = new EventData("baz");

    @BeforeEach
    public void beforeEach() {
        mockCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }

        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Tests that it pushes full batches downstream (when tryAdd returns fall). Also, publishes any batches downstream
     * when upstream completes.
     */
    @Test
    public void pushesFullBatchesDownstream() {
        // Arrange
        final List<EventData> batchEvents = new ArrayList<>();
        setupBatchMock(batch, batchEvents, event1, event2);

        final List<EventData> batchEvents2 = new ArrayList<>();
        setupBatchMock(batch2, batchEvents2, event3);

        final Duration waitTime = Duration.ofSeconds(30);
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(waitTime);

        final AtomicInteger first = new AtomicInteger(0);
        final Supplier<EventDataBatch> supplier = () -> {
            final int current = first.getAndIncrement();

            switch (current) {
                case 0:
                    return batch;

                case 1:
                    return batch2;

                default:
                    throw new RuntimeException(
                        "pushesFullBatchesDownstream: Did not expect to get this many invocations.");
            }
        };

        final TestPublisher<EventData> publisher = TestPublisher.createCold();
        final EventDataAggregator aggregator
            = new EventDataAggregator(publisher.flux(), supplier, NAMESPACE, options, PARTITION_ID);

        // Act & Assert
        StepVerifier.create(aggregator).then(() -> {
            publisher.next(event1, event2, event3);
        })
            .expectNext(batch)
            .then(() -> publisher.complete())
            .expectNext(batch2)
            .expectComplete()
            .verify(Duration.ofSeconds(10));
    }

    /**
     * Tests that it pushes partial batches downstream when an error occurs.
     */
    @Test
    public void pushesBatchesAndError() {
        // Arrange
        final List<EventData> batchEvents = new ArrayList<>();
        setupBatchMock(batch, batchEvents, event1, event2);

        final Duration waitTime = Duration.ofSeconds(30);
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(waitTime);

        final AtomicBoolean first = new AtomicBoolean();
        final Supplier<EventDataBatch> supplier = () -> {
            if (first.compareAndSet(false, true)) {
                return batch;
            } else {
                return batch2;
            }
        };

        final TestPublisher<EventData> publisher = TestPublisher.createCold();
        final EventDataAggregator aggregator
            = new EventDataAggregator(publisher.flux(), supplier, NAMESPACE, options, PARTITION_ID);
        final IllegalArgumentException testException = new IllegalArgumentException("Test exception.");

        // Act & Assert
        StepVerifier.create(aggregator).then(() -> {
            publisher.next(event1, event2);
            publisher.error(testException);
        }).expectNext(batch).expectErrorMatches(e -> e.equals(testException)).verify(Duration.ofSeconds(10));

        // Verify that these events were added to the batch.
        assertEquals(2, batchEvents.size());
        assertTrue(batchEvents.contains(event1));
        assertTrue(batchEvents.contains(event2));
    }

    /**
     * Tests that batches are pushed downstream when max wait time has elapsed.
     */
    @Test
    public void pushesBatchAfterMaxTime() {
        // Arrange
        final List<EventData> batchEvents = new ArrayList<>();
        setupBatchMock(batch, batchEvents, event1, event2);

        final List<EventData> batchEvents2 = new ArrayList<>();
        setupBatchMock(batch2, batchEvents2, event1, event2, event3);

        final Duration waitTime = Duration.ofSeconds(5);
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(waitTime);

        final AtomicInteger first = new AtomicInteger(0);
        final Supplier<EventDataBatch> supplier = () -> {
            final int current = first.getAndIncrement();

            switch (current) {
                case 0:
                    return batch;

                case 1:
                    return batch2;

                default:
                    LOGGER.log(LogLevel.VERBOSE, () -> "Invoked get batch for the xth time:" + current);
                    return batch3;
            }
        };

        final TestPublisher<EventData> publisher = TestPublisher.createCold();
        final EventDataAggregator aggregator
            = new EventDataAggregator(publisher.flux(), supplier, NAMESPACE, options, PARTITION_ID);

        // Act & Assert
        StepVerifier.create(aggregator).then(() -> {
            publisher.next(event1);
            publisher.next(event2);
        }).thenAwait(waitTime).assertNext(b -> {
            assertEquals(b, batch);
            assertEquals(2, batchEvents.size());
        }).thenCancel().verify();
    }

    /**
     * Each event cancels the running max-wait interval. That must not interrupt a timer-triggered publish that is in
     * progress, otherwise creating the next batch fails and a send can close the connection's IO pipe.
     */
    @Test
    public void newEventDoesNotInterruptTimerPublish() throws InterruptedException {
        // Arrange
        final Duration waitTime = Duration.ofMillis(250);
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(waitTime);

        final List<EventData> batchEvents = new ArrayList<>();
        setupBatchMock(batch, batchEvents, event1, event2);
        setupBatchMock(batch2, new ArrayList<>(), event1, event2, event3);

        final CountDownLatch event2Emitted = new CountDownLatch(1);
        final CountDownLatch timerPublishDone = new CountDownLatch(1);
        final AtomicBoolean tickWaitedForLock = new AtomicBoolean();
        final AtomicBoolean interrupted = new AtomicBoolean();
        final AtomicInteger supplierCalls = new AtomicInteger();

        // Holds the aggregator lock (tryAdd is called under it) until the timer tick is waiting for it, so the tick
        // is in progress when event2 restarts the interval.
        Mockito.doAnswer(invocation -> {
            tickWaitedForLock.set(awaitTimerTickBlockedOnLock());
            batchEvents.add(event2);
            return true;
        }).when(batch).tryAdd(event2);

        final Supplier<EventDataBatch> supplier = () -> {
            if (supplierCalls.getAndIncrement() == 0) {
                return batch;
            }
            // Called by the timer tick after it published the batch. Wait until the event2 emission cancelled the
            // interval, which is when an interrupt would arrive.
            try {
                if (!event2Emitted.await(10, TimeUnit.SECONDS)) {
                    LOGGER.log(LogLevel.WARNING, () -> "event2 was not emitted in time.");
                }
                if (Thread.currentThread().isInterrupted()) {
                    interrupted.set(true);
                }
            } catch (InterruptedException e) {
                interrupted.set(true);
            } finally {
                timerPublishDone.countDown();
            }
            return batch2;
        };

        final TestPublisher<EventData> publisher = TestPublisher.createCold();
        final EventDataAggregator aggregator
            = new EventDataAggregator(publisher.flux(), supplier, NAMESPACE, options, PARTITION_ID);
        final List<EventDataBatch> published = Collections.synchronizedList(new ArrayList<>());
        final Disposable subscription = aggregator.subscribe(published::add);

        try {
            // Act
            publisher.next(event1);
            publisher.next(event2);
            event2Emitted.countDown();

            // Assert
            assertTrue(timerPublishDone.await(10, TimeUnit.SECONDS), "Timer did not publish the batch.");
            assertTrue(tickWaitedForLock.get(), "Timer tick did not wait for the aggregator lock.");
            assertFalse(interrupted.get(), "Timer-triggered publish was interrupted by a new event.");
            assertEquals(Collections.singletonList(batch), published);
            assertEquals(Arrays.asList(event1, event2), batchEvents);
        } finally {
            subscription.dispose();
        }
    }

    /**
     * A timer tick blocks while the next batch is created, and the producer delivers created batches on
     * {@link Schedulers#boundedElastic()}. Ticks must therefore not depend on that scheduler: when all its threads
     * are busy, ticks queued on it could wait forever for their own batches.
     */
    @Test
    public void timerPublishesWhileBoundedElasticIsSaturated() throws InterruptedException {
        // Arrange
        final Duration waitTime = Duration.ofMillis(100);
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(waitTime);
        setupBatchMock(batch, new ArrayList<>(), event1);

        final AtomicInteger supplierCalls = new AtomicInteger();
        final Supplier<EventDataBatch> supplier = () -> supplierCalls.getAndIncrement() == 0 ? batch : batch2;

        final int threads = Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE;
        final CountDownLatch blockersStarted = new CountDownLatch(threads);
        final CountDownLatch releaseBlockers = new CountDownLatch(1);
        final List<Disposable> blockers = new ArrayList<>();
        final List<EventDataBatch> published = Collections.synchronizedList(new ArrayList<>());
        Disposable subscription = null;

        try {
            for (int i = 0; i < threads; i++) {
                blockers.add(Schedulers.boundedElastic().schedule(() -> {
                    blockersStarted.countDown();
                    try {
                        releaseBlockers.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }));
            }
            // If not all started, other work already occupies threads; the scheduler is saturated either way.
            if (!blockersStarted.await(10, TimeUnit.SECONDS)) {
                LOGGER.log(LogLevel.WARNING, () -> "Not all blockers started; boundedElastic is busy.");
            }

            final TestPublisher<EventData> publisher = TestPublisher.createCold();
            final EventDataAggregator aggregator
                = new EventDataAggregator(publisher.flux(), supplier, NAMESPACE, options, PARTITION_ID);
            subscription = aggregator.subscribe(published::add);

            // Act
            publisher.next(event1);

            // Assert
            final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (published.isEmpty() && System.nanoTime() < deadline) {
                TimeUnit.MILLISECONDS.sleep(10);
            }
            assertEquals(Collections.singletonList(batch), published,
                "Timer did not publish while boundedElastic was saturated.");
        } finally {
            releaseBlockers.countDown();
            blockers.forEach(Disposable::dispose);
            if (subscription != null) {
                subscription.dispose();
            }
        }
    }

    /**
     * Called while holding the aggregator lock. Waits until a timer tick is blocked on that lock.
     *
     * @return {@code true} if a timer tick is waiting for the lock; {@code false} if none did in time.
     */
    private static boolean awaitTimerTickBlockedOnLock() throws InterruptedException {
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
                if (entry.getKey().getState() != Thread.State.BLOCKED) {
                    continue;
                }
                for (StackTraceElement frame : entry.getValue()) {
                    if (frame.getClassName().endsWith("EventDataAggregator$EventDataAggregatorMain")
                        && "publishDownstream".equals(frame.getMethodName())) {
                        return true;
                    }
                }
            }
            TimeUnit.MILLISECONDS.sleep(5);
        }
        return false;
    }

    /**
     * Tests that it pushes partial batches downstream when flush signal arrives.
     */
    @Test
    public void pushesBatchesOnFlushSignal() {
        // Arrange
        final List<EventData> batchEvents = new ArrayList<>();
        setupBatchMock(batch, batchEvents, event1, event2);

        final Duration waitTime = Duration.ofSeconds(30);
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(waitTime);

        final AtomicBoolean first = new AtomicBoolean();
        final Supplier<EventDataBatch> supplier = () -> {
            if (first.compareAndSet(false, true)) {
                return batch;
            } else {
                return batch2;
            }
        };

        final TestPublisher<EventData> publisher = TestPublisher.createCold();
        final EventDataAggregator aggregator
            = new EventDataAggregator(publisher.flux(), supplier, NAMESPACE, options, PARTITION_ID);

        // Act & Assert
        StepVerifier.create(aggregator).then(() -> {
            publisher.next(event1, event2, new FlushSignal(new NoopMonoSink()));
        }).expectNext(batch).thenCancel().verify(Duration.ofSeconds(10));

        // Verify that exactly two expected events were added to the batch.
        assertEquals(2, batchEvents.size());
        assertTrue(batchEvents.contains(event1));
        assertTrue(batchEvents.contains(event2));
    }

    /**
     * Verifies that an event too large for the link is reported through the send-failed handler and does not
     * terminate the aggregator, so later events are still published.
     */
    @Test
    public void reportsEventThatDoesNotFitAndContinues() {
        // Arrange
        final List<EventData> batchEvents = new ArrayList<>();
        when(batch.tryAdd(any(EventData.class))).thenAnswer(invocation -> {
            final EventData arg = invocation.getArgument(0);
            if (arg == event3) {
                // A real EventDataBatch throws when a single event exceeds the maximum message size.
                throw new AmqpException(false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, "too large", null);
            }
            if (arg == event2) {
                batchEvents.add(arg);
                return true;
            }
            return false;
        });
        when(batch.getEvents()).thenAnswer(invocation -> batchEvents);
        when(batch.getCount()).thenAnswer(invocation -> batchEvents.size());

        final List<SendBatchFailedContext> failures = new ArrayList<>();
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofSeconds(30));
        options.setSendFailedContext(failures::add);

        final AtomicBoolean first = new AtomicBoolean();
        final Supplier<EventDataBatch> supplier = () -> {
            if (first.compareAndSet(false, true)) {
                return batch;
            } else {
                return batch2;
            }
        };

        final TestPublisher<EventData> publisher = TestPublisher.createCold();
        final EventDataAggregator aggregator
            = new EventDataAggregator(publisher.flux(), supplier, NAMESPACE, options, PARTITION_ID);

        // Act & Assert
        StepVerifier.create(aggregator)
            .then(() -> publisher.next(event1, event3, event2))
            .then(publisher::complete)
            .expectNext(batch)
            .expectComplete()
            .verify(Duration.ofSeconds(10));

        assertEquals(Collections.singletonList(event2), batchEvents);
        assertEquals(2, failures.size());
        assertFailedContext(failures.get(0), event1, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED);
        assertFailedContext(failures.get(1), event3, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED);
    }

    /**
     * Verifies that repeated failures to create a new batch do not terminate the aggregator, that the event which
     * could not be added is reported through the send-failed handler, and that an already published batch is never
     * reused.
     */
    @Test
    public void batchCreationFailuresDoNotTerminate() {
        // Arrange
        final List<EventData> batchEvents = new ArrayList<>();
        setupBatchMock(batch, batchEvents, event1);

        final List<EventData> batchEvents2 = new ArrayList<>();
        setupBatchMock(batch2, batchEvents2, event3);

        final List<SendBatchFailedContext> failures = new ArrayList<>();
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofSeconds(30));
        options.setSendFailedContext(failures::add);

        final UncheckedExecutionException creationError
            = new UncheckedExecutionException(new ExecutionException(new IllegalStateException("link closed")));
        final AtomicInteger calls = new AtomicInteger();
        final Supplier<EventDataBatch> supplier = () -> {
            final int current = calls.getAndIncrement();
            switch (current) {
                case 0:
                    return batch;

                case 1:
                case 2:
                    throw creationError;

                case 3:
                    return batch2;

                default:
                    throw new RuntimeException("batchCreationFailuresDoNotTerminate: unexpected call " + current);
            }
        };

        final TestPublisher<EventData> publisher = TestPublisher.createCold();
        final EventDataAggregator aggregator
            = new EventDataAggregator(publisher.flux(), supplier, NAMESPACE, options, PARTITION_ID);

        // Act & Assert
        StepVerifier.create(aggregator)
            .then(() -> publisher.next(event1, event2, event3))
            .expectNext(batch)
            .then(publisher::complete)
            .expectNext(batch2)
            .expectComplete()
            .verify(Duration.ofSeconds(10));

        assertEquals(Collections.singletonList(event1), batchEvents);
        assertEquals(Collections.singletonList(event3), batchEvents2);
        assertEquals(1, failures.size());
        assertEquals(Collections.singletonList(event2), toList(failures.get(0).getEvents()));
        assertEquals(PARTITION_ID, failures.get(0).getPartitionId());
        assertSame(creationError, failures.get(0).getThrowable());
    }

    /**
     * Verifies that a flush completes (an empty batch is pushed downstream) even when a new batch could not be
     * created.
     */
    @Test
    public void flushCompletesWhenBatchCreationFails() {
        // Arrange
        final List<EventData> batchEvents = new ArrayList<>();
        setupBatchMock(batch, batchEvents, event1);

        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofSeconds(30));
        options.setSendFailedContext(context -> {
        });

        final AtomicBoolean first = new AtomicBoolean();
        final Supplier<EventDataBatch> supplier = () -> {
            if (first.compareAndSet(false, true)) {
                return batch;
            }
            throw new UncheckedExecutionException(new ExecutionException(new IllegalStateException("link closed")));
        };

        final TestPublisher<EventData> publisher = TestPublisher.createCold();
        final EventDataAggregator aggregator
            = new EventDataAggregator(publisher.flux(), supplier, NAMESPACE, options, PARTITION_ID);

        // Act & Assert
        StepVerifier.create(aggregator)
            .then(() -> publisher.next(event1, event2, new FlushSignal(new NoopMonoSink())))
            .expectNext(batch)
            .expectNext(EventDataBatch.EMPTY)
            .thenCancel()
            .verify(Duration.ofSeconds(10));
    }

    private static void assertFailedContext(SendBatchFailedContext context, EventData expectedEvent,
        AmqpErrorCondition expectedCondition) {
        assertEquals(Collections.singletonList(expectedEvent), toList(context.getEvents()));
        assertEquals(PARTITION_ID, context.getPartitionId());
        assertTrue(context.getThrowable() instanceof AmqpException);
        assertEquals(expectedCondition, ((AmqpException) context.getThrowable()).getErrorCondition());
    }

    private static List<EventData> toList(Iterable<EventData> events) {
        final List<EventData> list = new ArrayList<>();
        events.forEach(list::add);
        return list;
    }

    /**
     * Verifies that backpressure requests are supported.
     */
    @Test
    public void respectsBackpressure() {
        // Arrange
        final List<EventData> batchEvents = new ArrayList<>();
        setupBatchMock(batch, batchEvents, event1);

        final List<EventData> batchEvents2 = new ArrayList<>();
        setupBatchMock(batch2, batchEvents2);

        final Duration waitTime = Duration.ofSeconds(3);
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(waitTime);

        final AtomicInteger first = new AtomicInteger(0);
        final Supplier<EventDataBatch> supplier = () -> {
            final int current = first.getAndIncrement();

            switch (current) {
                case 0:
                    return batch;

                case 1:
                    return batch2;

                default:
                    throw new RuntimeException("respectsBackpressure: Did not expect to get this many invocations.");
            }
        };

        final TestPublisher<EventData> publisher = TestPublisher.createCold();
        final EventDataAggregator aggregator
            = new EventDataAggregator(publisher.flux(), supplier, NAMESPACE, options, PARTITION_ID);

        final long request = 1L;

        StepVerifier.create(aggregator, request).then(() -> publisher.next(event1)).assertNext(b -> {
            assertEquals(1, b.getCount());
            assertTrue(b.getEvents().contains(event1));
        }).expectNoEvent(waitTime).thenCancel().verify();

        publisher.assertMaxRequested(request);
    }

    /**
     * Helper method to set up mocked {@link EventDataBatch} to accept the given events in {@code resultSet}.
     *
     * @param batch Mocked batch.
     * @param resultSet List to store added EventData in.
     * @param acceptedEvents EventData that is accepted by the batch when {@link EventDataBatch#tryAdd(EventData)}
     *     is invoked.
     */
    static void setupBatchMock(EventDataBatch batch, List<EventData> resultSet, EventData... acceptedEvents) {
        when(batch.tryAdd(any(EventData.class))).thenAnswer(invocation -> {
            final EventData arg = invocation.getArgument(0);

            final boolean matches = Arrays.asList(acceptedEvents).contains(arg);

            if (matches) {
                resultSet.add(arg);
            }

            return matches;
        });
        when(batch.getEvents()).thenAnswer(invocation -> {
            return resultSet;
        });
        when(batch.getCount()).thenAnswer(invocation -> resultSet.size());
    }

    static final class NoopMonoSink implements MonoSink<Void> {
        @Override
        public void success() {
        }

        @Override
        public void success(Void v) {

        }

        @Override
        public void error(Throwable e) {
        }

        @Override
        public Context currentContext() {
            return Context.empty();
        }

        @Override
        public MonoSink<Void> onRequest(LongConsumer consumer) {
            return this;
        }

        @Override
        public MonoSink<Void> onCancel(Disposable d) {
            return this;
        }

        @Override
        public MonoSink<Void> onDispose(Disposable d) {
            return this;
        }
    }
}
