// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient.BufferedProducerClientOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class EventDataAggregatorTest {
    private static final String NAMESPACE = "test.namespace";

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
    private final EventData event4 = new EventData("bart");

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
                    throw new RuntimeException("pushesFullBatchesDownstream: Did not expect to get this many invocations.");
            }
        };

        final TestPublisher<EventData> publisher = TestPublisher.createCold();
        final EventDataAggregator aggregator = new EventDataAggregator(publisher.flux(), supplier, NAMESPACE, options);

        // Act & Assert
        StepVerifier.create(aggregator)
            .then(() -> {
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
        final EventDataAggregator aggregator = new EventDataAggregator(publisher.flux(), supplier, NAMESPACE, options);
        final IllegalArgumentException testException = new IllegalArgumentException("Test exception.");

        // Act & Assert
        StepVerifier.create(aggregator)
            .then(() -> {
                publisher.next(event1, event2);
                publisher.error(testException);
            })
            .expectNext(batch)
            .expectErrorMatches(e -> e.equals(testException))
            .verify(Duration.ofSeconds(10));

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
        setupBatchMock(batch2, batchEvents2, event3);

        final Duration waitTime = Duration.ofSeconds(5);
        final Duration halfWaitTime = waitTime.minusSeconds(2);
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
                    System.out.println("Invoked get batch for the xth time:" + current);
                    return batch3;
            }
        };

        final TestPublisher<EventData> publisher = TestPublisher.createCold();
        final EventDataAggregator aggregator = new EventDataAggregator(publisher.flux(), supplier, NAMESPACE, options);

        // Act & Assert
        StepVerifier.create(aggregator)
            .then(() -> publisher.next(event1))
            .thenAwait(halfWaitTime)
            .then(() -> {
                assertEquals(1, batchEvents.size());

                publisher.next(event2);
            })
            .thenAwait(waitTime)
            .assertNext(b -> {
                assertEquals(b, batch);
                assertEquals(2, batchEvents.size());
            })
            .expectNoEvent(waitTime)
            .then(() -> publisher.next(event3))
            .thenAwait(waitTime)
            .assertNext(e -> {
                assertEquals(e, batch2, "Should be equal.");
            })
            .thenCancel()
            .verify();
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
        final EventDataAggregator aggregator = new EventDataAggregator(publisher.flux(), supplier, NAMESPACE, options);

        StepVerifier.create(aggregator)
            .then(() -> {
                publisher.next(event1);
            })
            .consumeErrorWith(error -> {
                assertTrue(error instanceof AmqpException);
                assertEquals(AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, ((AmqpException) error).getErrorCondition());
            })
            .verify(Duration.ofSeconds(20));
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
        final EventDataAggregator aggregator = new EventDataAggregator(publisher.flux(), supplier, NAMESPACE, options);

        final long request = 1L;

        StepVerifier.create(aggregator, request)
            .then(() -> publisher.next(event1))
            .assertNext(b -> {
                assertEquals(1, b.getCount());
                assertTrue(b.getEvents().contains(event1));
            })
            .expectNoEvent(waitTime)
            .thenCancel()
            .verify();

        publisher.assertMaxRequested(request);
    }

    private static void setupBatchMock(EventDataBatch batch, List<EventData> resultSet, EventData... acceptedEvents) {
        when(batch.tryAdd(any(EventData.class))).thenAnswer(invocation -> {
            final EventData arg = invocation.getArgument(0);

            final boolean matches = Arrays.asList(acceptedEvents).contains(arg);

            if (matches) {
                resultSet.add(arg);
            }

            return matches;
        });
        when(batch.getEvents()).thenAnswer(invocation -> resultSet);
        when(batch.getCount()).thenAnswer(invocation -> resultSet.size());
    }
}
