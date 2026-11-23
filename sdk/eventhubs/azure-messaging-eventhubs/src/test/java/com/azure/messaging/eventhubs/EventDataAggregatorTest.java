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
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.MonoSink;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import reactor.util.context.Context;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Isolated
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

    @Test
    public void recoversFromRepeatedBatchCreationFailuresWithoutResending() {
        setupBatchMock(batch, new ArrayList<>(), event1);
        setupBatchMock(batch2, new ArrayList<>(), event2);
        setupBatchMock(batch3, new ArrayList<>(), event3);
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofMinutes(1));
        final AtomicInteger creations = new AtomicInteger();
        final Supplier<EventDataBatch> supplier = () -> {
            switch (creations.getAndIncrement()) {
                case 0:
                    return batch;

                case 2:
                    return batch2;

                case 4:
                    return batch3;

                default:
                    throw new UncheckedExecutionException(
                        new ExecutionException(new IllegalStateException("Transient batch creation failure.")));
            }
        };
        final TestPublisher<EventData> publisher = TestPublisher.create();
        final EventDataAggregator aggregator
            = new EventDataAggregator(publisher.flux(), supplier, NAMESPACE, options, PARTITION_ID);

        final List<EventDataBatch> published = new ArrayList<>();
        final List<Throwable> errors = new ArrayList<>();
        final Disposable subscription = aggregator.subscribe(published::add, errors::add);
        try {
            publisher.next(event1, new FlushSignal(new NoopMonoSink()));
            publisher.next(event2, new FlushSignal(new NoopMonoSink()));
            publisher.next(event3);
            publisher.complete();
            assertEquals(Arrays.asList(batch, batch2, batch3), published);
            assertTrue(errors.isEmpty(), "Batch creation failures must not terminate the partition.");
        } finally {
            subscription.dispose();
        }
        assertEquals(5, creations.get());
    }

    @Test
    public void failsOnlyTheEventWhenBatchCreationFails() {
        setupBatchMock(batch, new ArrayList<>(), event2);
        final AtomicInteger attempts = new AtomicInteger();
        final List<EventData> failed = new ArrayList<>();
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofMinutes(1));
        options.setSendFailedContext(context -> context.getEvents().forEach(failed::add));
        final TestPublisher<EventData> publisher = TestPublisher.create();
        final EventDataAggregator aggregator = new EventDataAggregator(publisher.flux(), () -> {
            if (attempts.incrementAndGet() <= 2) {
                throw new IllegalStateException("Batch creation failed.");
            }
            return batch;
        }, NAMESPACE, options, PARTITION_ID);

        StepVerifier.create(aggregator)
            .then(() -> publisher.next(event1, event2).complete())
            .expectNext(batch)
            .expectComplete()
            .verify(Duration.ofSeconds(5));
        assertEquals(Collections.singletonList(event1), failed);
        assertEquals(3, attempts.get(), "Retry batch creation only when another event arrives.");
    }

    @Test
    public void propagatesFatalBatchCreationError() {
        final IllegalStateException failure = new IllegalStateException("Fatal Reactor error.");
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofMinutes(1));
        final EventDataAggregator aggregator = new EventDataAggregator(TestPublisher.<EventData>create().flux(), () -> {
            throw Exceptions.bubble(failure);
        }, NAMESPACE, options, PARTITION_ID);

        final RuntimeException thrown = assertThrows(RuntimeException.class, () -> aggregator.subscribe());
        assertEquals(failure, Exceptions.unwrap(thrown));
    }

    @Test
    public void restartingTimerDoesNotInterruptBatchCreation() throws InterruptedException {
        final List<EventData> events = new ArrayList<>();
        setupBatchMock(batch, events, event1);
        setupBatchMock(batch2, new ArrayList<>());
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofMillis(100));
        final CountDownLatch eventEmitted = new CountDownLatch(1);
        final CountDownLatch batchCreated = new CountDownLatch(1);
        final AtomicBoolean interrupted = new AtomicBoolean();
        final AtomicBoolean timerBlocked = new AtomicBoolean();
        when(batch.tryAdd(event2)).thenAnswer(invocation -> {
            final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (System.nanoTime() < deadline) {
                final boolean blocked = Thread.getAllStackTraces()
                    .entrySet()
                    .stream()
                    .anyMatch(entry -> entry.getKey().getState() == Thread.State.BLOCKED
                        && Arrays.stream(entry.getValue())
                            .anyMatch(frame -> frame.getClassName().contains("EventDataAggregator$")
                                && "publishDownstream".equals(frame.getMethodName())));
                if (blocked) {
                    timerBlocked.set(true);
                    break;
                }
                TimeUnit.MILLISECONDS.sleep(10);
            }
            events.add(event2);
            return true;
        });
        final AtomicInteger creations = new AtomicInteger();
        final TestPublisher<EventData> publisher = TestPublisher.create();
        final EventDataAggregator timedAggregator = new EventDataAggregator(publisher.flux(), () -> {
            if (creations.getAndIncrement() == 0) {
                return batch;
            }
            try {
                if (!eventEmitted.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Event emission did not finish.");
                }
                interrupted.set(Thread.currentThread().isInterrupted());
            } catch (InterruptedException error) {
                interrupted.set(true);
            } finally {
                batchCreated.countDown();
            }
            return batch2;
        }, NAMESPACE, options, PARTITION_ID);
        final List<EventDataBatch> published = new CopyOnWriteArrayList<>();
        final Disposable subscription = timedAggregator.subscribe(published::add);
        try {
            publisher.next(event1, event2);
            eventEmitted.countDown();
            assertTrue(batchCreated.await(5, TimeUnit.SECONDS), "Timer did not create the next batch.");
            assertTrue(timerBlocked.get(), "The timer must start before the event restarts the interval.");
            assertFalse(interrupted.get(), "Interval cancellation interrupted batch creation.");
            assertEquals(Collections.singletonList(batch), published);
        } finally {
            eventEmitted.countDown();
            subscription.dispose();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "complete", "error", "cancel" })
    public void stopsTimerThreadOnTermination(String termination) throws InterruptedException {
        setupBatchMock(batch, new ArrayList<>(), event1);
        setupBatchMock(batch2, new ArrayList<>());
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofMillis(20));
        final CountDownLatch created = new CountDownLatch(1);
        final AtomicReference<Thread> timer = new AtomicReference<>();
        final AtomicInteger creations = new AtomicInteger();
        final TestPublisher<EventData> publisher = TestPublisher.create();
        final EventDataAggregator aggregator = new EventDataAggregator(publisher.flux(), () -> {
            if (creations.getAndIncrement() == 0) {
                return batch;
            }
            timer.set(Thread.currentThread());
            created.countDown();
            return batch2;
        }, NAMESPACE, options, PARTITION_ID);
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final Disposable subscription = aggregator.subscribe(ignored -> {
        }, error::set);
        try {
            publisher.next(event1);
            assertTrue(created.await(5, TimeUnit.SECONDS), "The timer must start before termination.");
            if ("complete".equals(termination)) {
                publisher.complete();
            } else if ("error".equals(termination)) {
                publisher.error(new IllegalStateException("Source failed."));
                assertTrue(error.get() instanceof IllegalStateException);
            } else {
                subscription.dispose();
            }
            timer.get().join(TimeUnit.SECONDS.toMillis(5));
            assertFalse(timer.get().isAlive(), "The owned timer thread must stop.");
            assertEquals(2, creations.get());
        } finally {
            subscription.dispose();
        }
    }

    @Test
    public void cancellationWaitsForActiveBatchCreation() throws InterruptedException {
        setupBatchMock(batch, new ArrayList<>(), event1);
        setupBatchMock(batch2, new ArrayList<>());
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofMillis(20));
        final CountDownLatch creating = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final AtomicBoolean interrupted = new AtomicBoolean();
        final AtomicInteger creations = new AtomicInteger();
        final AtomicReference<Thread> timer = new AtomicReference<>();
        final TestPublisher<EventData> publisher = TestPublisher.create();
        final EventDataAggregator aggregator = new EventDataAggregator(publisher.flux(), () -> {
            if (creations.getAndIncrement() == 0) {
                return batch;
            }
            timer.set(Thread.currentThread());
            creating.countDown();
            try {
                if (!release.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Batch creation did not finish.");
                }
            } catch (InterruptedException error) {
                interrupted.set(true);
            }
            return batch2;
        }, NAMESPACE, options, PARTITION_ID);
        final List<EventDataBatch> published = new CopyOnWriteArrayList<>();
        final Disposable subscription = aggregator.subscribe(published::add);
        final Thread cancellation = new Thread(subscription::dispose);
        try {
            publisher.next(event1);
            assertTrue(creating.await(5, TimeUnit.SECONDS));
            cancellation.start();
            final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (cancellation.getState() != Thread.State.BLOCKED && System.nanoTime() < deadline) {
                TimeUnit.MILLISECONDS.sleep(10);
            }
            assertEquals(Thread.State.BLOCKED, cancellation.getState(), "Cancellation must wait for batch creation.");
            release.countDown();
            cancellation.join(TimeUnit.SECONDS.toMillis(5));
            timer.get().join(TimeUnit.SECONDS.toMillis(5));
            assertFalse(cancellation.isAlive());
            assertFalse(timer.get().isAlive());
            assertFalse(interrupted.get());
            assertEquals(Collections.singletonList(batch), published);
        } finally {
            release.countDown();
            subscription.dispose();
            cancellation.join(TimeUnit.SECONDS.toMillis(5));
        }
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
     * Reports an oversized event and continues to publish later events.
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void reportsEventThatDoesNotFitAndContinues(boolean throwsOnAdd) {
        // Arrange
        final List<EventData> batchEvents = new ArrayList<>();
        setupBatchMock(batch, batchEvents, event2);
        if (throwsOnAdd) {
            when(batch.tryAdd(event1)).thenThrow(
                new AmqpException(false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, "Event is too large.", null));
        }

        final Duration waitTime = Duration.ofSeconds(30);
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(waitTime);
        final List<SendBatchFailedContext> failures = new ArrayList<>();
        options.setSendFailedContext(failures::add);

        final AtomicBoolean first = new AtomicBoolean();
        final Supplier<EventDataBatch> supplier = () -> {
            if (first.compareAndSet(false, true)) {
                return batch;
            } else {
                throw new IllegalArgumentException("Did not expect another batch call.");
            }
        };

        final TestPublisher<EventData> publisher = TestPublisher.createCold();
        final EventDataAggregator aggregator
            = new EventDataAggregator(publisher.flux(), supplier, NAMESPACE, options, PARTITION_ID);

        StepVerifier.create(aggregator).then(() -> {
            publisher.next(event1, event2).complete();
        }).expectNext(batch).expectComplete().verify(Duration.ofSeconds(5));
        assertEquals(1, failures.size());
        assertEquals(Collections.singletonList(event1), failures.get(0).getEvents());
        assertEquals(AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED,
            ((AmqpException) failures.get(0).getThrowable()).getErrorCondition());
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
