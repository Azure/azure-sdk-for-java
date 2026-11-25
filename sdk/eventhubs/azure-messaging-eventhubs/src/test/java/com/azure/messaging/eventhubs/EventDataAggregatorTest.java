// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient.BufferedProducerClientOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.MonoSink;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import reactor.test.scheduler.VirtualTimeScheduler;
import reactor.util.context.Context;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@Isolated
@Execution(ExecutionMode.SAME_THREAD)
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

    @Test
    public void batchDeadlineIsNotExtendedByContinuousArrivals() {
        final TestPublisher<EventData> publisher = TestPublisher.createCold();

        StepVerifier.withVirtualTime(() -> timedAggregator(publisher, 1024 * 1024))
            .then(() -> publisher.next(event1))
            .expectNoEvent(Duration.ofMillis(50))
            .then(() -> publisher.next(event2))
            .expectNoEvent(Duration.ofMillis(50))
            .then(() -> publisher.next(event3))
            .expectNoEvent(Duration.ofMillis(50))
            .then(() -> publisher.next(event1))
            .expectNoEvent(Duration.ofMillis(50))
            .then(() -> publisher.next(event2))
            .expectNoEvent(Duration.ofMillis(49))
            .thenAwait(Duration.ofMillis(1))
            .assertNext(b -> assertEquals(Arrays.asList(event1, event2, event3, event1, event2), b.getEvents()))
            .expectNoEvent(Duration.ofMillis(50))
            .then(() -> publisher.next(event3))
            .expectNoEvent(Duration.ofMillis(50))
            .then(() -> publisher.next(event1))
            .expectNoEvent(Duration.ofMillis(50))
            .then(() -> publisher.next(event2))
            .expectNoEvent(Duration.ofMillis(50))
            .then(() -> publisher.next(event3))
            .expectNoEvent(Duration.ofMillis(50))
            .then(() -> publisher.next(event1))
            .expectNoEvent(Duration.ofMillis(49))
            .thenAwait(Duration.ofMillis(1))
            .assertNext(b -> assertEquals(Arrays.asList(event3, event1, event2, event3, event1), b.getEvents()))
            .thenCancel()
            .verify(Duration.ofSeconds(10));
    }

    @Test
    public void batchDeadlineStartsWithFirstEventAfterIdle() {
        final TestPublisher<EventData> publisher = TestPublisher.createCold();

        StepVerifier.withVirtualTime(() -> timedAggregator(publisher, 1024 * 1024))
            .then(() -> publisher.next(event1))
            .thenAwait(Duration.ofMillis(250))
            .assertNext(b -> assertEquals(Arrays.asList(event1), b.getEvents()))
            .expectNoEvent(Duration.ofMillis(625))
            .then(() -> publisher.next(event2))
            .expectNoEvent(Duration.ofMillis(100))
            .then(() -> publisher.next(event3))
            .expectNoEvent(Duration.ofMillis(149))
            .thenAwait(Duration.ofMillis(1))
            .assertNext(b -> assertEquals(Arrays.asList(event2, event3), b.getEvents()))
            .thenCancel()
            .verify(Duration.ofSeconds(10));
    }

    @Test
    public void batchDeadlineStartsWithFirstEventAfterFlush() {
        final TestPublisher<EventData> publisher = TestPublisher.createCold();

        StepVerifier.withVirtualTime(() -> timedAggregator(publisher, 1024 * 1024))
            .then(() -> publisher.next(event1))
            .expectNoEvent(Duration.ofMillis(100))
            .then(() -> publisher.next(new FlushSignal(new NoopMonoSink())))
            .assertNext(b -> assertEquals(Arrays.asList(event1), b.getEvents()))
            .then(() -> publisher.next(new FlushSignal(new NoopMonoSink())))
            .expectNext(EventDataBatch.EMPTY)
            .expectNoEvent(Duration.ofMillis(50))
            .then(() -> publisher.next(event2))
            .expectNoEvent(Duration.ofMillis(50))
            .then(() -> publisher.next(event3))
            .expectNoEvent(Duration.ofMillis(199))
            .thenAwait(Duration.ofMillis(1))
            .assertNext(b -> assertEquals(Arrays.asList(event2, event3), b.getEvents()))
            .thenCancel()
            .verify(Duration.ofSeconds(10));
    }

    @Test
    public void batchDeadlineStartsWithSizeRolloverEvent() {
        final TestPublisher<EventData> publisher = TestPublisher.createCold();
        final EventData first = new EventData(new byte[600]);
        final EventData second = new EventData(new byte[600]);

        StepVerifier.withVirtualTime(() -> timedAggregator(publisher, 1024))
            .then(() -> publisher.next(first))
            .expectNoEvent(Duration.ofMillis(100))
            .then(() -> publisher.next(second))
            .assertNext(b -> assertEquals(Arrays.asList(first), b.getEvents()))
            .expectNoEvent(Duration.ofMillis(50))
            .then(() -> publisher.next(event3))
            .expectNoEvent(Duration.ofMillis(199))
            .thenAwait(Duration.ofMillis(1))
            .assertNext(b -> assertEquals(Arrays.asList(second, event3), b.getEvents()))
            .thenCancel()
            .verify(Duration.ofSeconds(10));
    }

    @Test
    public void batchDeadlineIgnoresTickFromFlushedBatch() {
        final TestPublisher<EventData> publisher = TestPublisher.createCold();
        final List<EventDataBatch> published = new ArrayList<>();
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofMillis(250));
        final ClientLogger logger = Mockito.spy(LOGGER);
        final VirtualTimeScheduler clock = VirtualTimeScheduler.getOrSet();
        final EventDataAggregator.EventDataAggregatorMain subscriber
            = new EventDataAggregator.EventDataAggregatorMain(new BaseSubscriber<EventDataBatch>() {
                @Override
                protected void hookOnNext(EventDataBatch value) {
                    published.add(value);
                }
            }, NAMESPACE, options, () -> new EventDataBatch(1024 * 1024, PARTITION_ID, null, null,
                EventHubsProducerInstrumentation.NOOP_INSTANCE), PARTITION_ID, logger);

        try {
            publisher.flux().subscribe(subscriber);
            publisher.next(event1);
            final AtomicBoolean replaceBatch = new AtomicBoolean(true);
            doAnswer(invocation -> {
                // Replace the batch after the old tick is selected, before it acquires the batch lock.
                if (replaceBatch.compareAndSet(true, false)) {
                    publisher.next(new FlushSignal(new NoopMonoSink()), event2);
                }
                return invocation.callRealMethod();
            }).when(logger).atVerbose();

            clock.advanceTimeBy(Duration.ofMillis(250));
            assertEquals(1, published.size());
            assertEquals(Arrays.asList(event1), published.get(0).getEvents());
            clock.advanceTimeBy(Duration.ofMillis(249));
            assertEquals(1, published.size());
            clock.advanceTimeBy(Duration.ofMillis(1));
            assertEquals(2, published.size());
            assertEquals(Arrays.asList(event2), published.get(1).getEvents());
        } finally {
            subscriber.cancel();
            VirtualTimeScheduler.reset();
        }
    }

    private static EventDataAggregator timedAggregator(TestPublisher<EventData> publisher, int maxBatchSize) {
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofMillis(250));
        return new EventDataAggregator(publisher.flux(), () -> new EventDataBatch(maxBatchSize, PARTITION_ID, null,
            null, EventHubsProducerInstrumentation.NOOP_INSTANCE), NAMESPACE, options, PARTITION_ID);
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
     * Verifies that an error is propagated when it is too large for the link.
     */
    @Test
    public void errorsOnEventThatDoesNotFit() {
        // Arrange
        final List<EventData> batchEvents = new ArrayList<>();
        setupBatchMock(batch, batchEvents);

        final Duration waitTime = Duration.ofSeconds(30);
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(waitTime);

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
            publisher.next(event1);
        }).consumeErrorWith(error -> {
            assertTrue(error instanceof AmqpException);
            assertEquals(AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, ((AmqpException) error).getErrorCondition());
        }).verify(Duration.ofSeconds(20));
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
