// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient.BufferedProducerClientOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import reactor.core.Disposable;
import reactor.test.publisher.TestPublisher;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies batch deadlines with virtual time. Isolation prevents the global Reactor scheduler replacement from
 * affecting other tests.
 */
@Isolated
public class EventDataAggregatorTimingTest {
    private final TestPublisher<EventData> publisher = TestPublisher.createCold();
    private final List<EventDataBatch> published = new ArrayList<>();
    private VirtualTimeScheduler clock;
    private Disposable subscription;

    @BeforeEach
    public void beforeEach() {
        clock = VirtualTimeScheduler.getOrSet();
    }

    @AfterEach
    public void afterEach() {
        try {
            if (subscription != null) {
                subscription.dispose();
            }
        } finally {
            VirtualTimeScheduler.reset();
        }
    }

    @Test
    public void publishesAtFirstEventDeadlineWhileEventsKeepArriving() {
        subscribe(1024 * 1024);
        final List<EventData> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (i > 0) {
                clock.advanceTimeBy(Duration.ofMillis(200));
            }
            final EventData event = new EventData("event-" + i);
            events.add(event);
            publisher.next(event);
            assertTrue(published.isEmpty(), "The partial batch must wait for its first event's deadline.");
        }

        // Events arrived at 0, 200, 400, 600 and 800 ms. The deadline remains 1000 ms.
        clock.advanceTimeBy(Duration.ofMillis(199));
        assertTrue(published.isEmpty());
        clock.advanceTimeBy(Duration.ofMillis(1));
        assertEquals(1, published.size());
        assertEquals(events, published.get(0).getEvents());
    }

    @Test
    public void startsNewDeadlineForBatchAfterIdle() {
        subscribe(1024 * 1024);
        final EventData first = new EventData("first");
        final EventData second = new EventData("second");
        final EventData third = new EventData("third");
        publisher.next(first);
        clock.advanceTimeBy(Duration.ofSeconds(1));
        assertEquals(1, published.size());
        assertEquals(Collections.singletonList(first), published.get(0).getEvents());

        clock.advanceTimeBy(Duration.ofMillis(4500));
        assertEquals(1, published.size(), "An empty batch must not be published during idle time.");
        publisher.next(second);
        clock.advanceTimeBy(Duration.ofMillis(400));
        publisher.next(third);

        // The new batch starts at 5500 ms, so neither an old periodic tick nor the later event sets its deadline.
        clock.advanceTimeBy(Duration.ofMillis(599));
        assertEquals(1, published.size());
        clock.advanceTimeBy(Duration.ofMillis(1));
        assertEquals(2, published.size());
        assertEquals(Arrays.asList(second, third), published.get(1).getEvents());
    }

    @Test
    public void startsNewDeadlineForBatchAfterExplicitFlush() {
        subscribe(1024 * 1024);
        final EventData first = new EventData("first");
        final EventData second = new EventData("second");
        final EventData third = new EventData("third");
        publisher.next(first);
        clock.advanceTimeBy(Duration.ofMillis(400));
        publisher.next(new FlushSignal(new EventDataAggregatorTest.NoopMonoSink()));
        assertEquals(1, published.size());
        assertEquals(Collections.singletonList(first), published.get(0).getEvents());

        clock.advanceTimeBy(Duration.ofMillis(200));
        publisher.next(second);
        clock.advanceTimeBy(Duration.ofMillis(200));
        publisher.next(third);

        // Flush occurred at 400 ms, but the next batch's first event arrived at 600 ms.
        clock.advanceTimeBy(Duration.ofMillis(799));
        assertEquals(1, published.size());
        clock.advanceTimeBy(Duration.ofMillis(1));
        assertEquals(2, published.size());
        assertEquals(Arrays.asList(second, third), published.get(1).getEvents());
    }

    @Test
    public void startsNewDeadlineForBatchAfterSizeRollover() {
        subscribe(1024);
        final EventData first = new EventData(new byte[600]);
        final EventData second = new EventData(new byte[600]);
        final EventData third = new EventData("third");
        publisher.next(first);
        clock.advanceTimeBy(Duration.ofMillis(400));
        publisher.next(second);
        assertEquals(1, published.size(), "The second event must force the first batch to be published.");
        assertEquals(Collections.singletonList(first), published.get(0).getEvents());
        clock.advanceTimeBy(Duration.ofMillis(100));
        publisher.next(third);

        // The second event starts a batch at 400 ms; the smaller third event must not extend its deadline.
        clock.advanceTimeBy(Duration.ofMillis(899));
        assertEquals(1, published.size());
        clock.advanceTimeBy(Duration.ofMillis(1));
        assertEquals(2, published.size());
        assertEquals(Arrays.asList(second, third), published.get(1).getEvents());
    }

    private void subscribe(int maxBatchSize) {
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofSeconds(1));
        final EventDataAggregator aggregator = new EventDataAggregator(publisher.flux(),
            () -> new EventDataBatch(maxBatchSize, "0", null, null, EventHubsProducerInstrumentation.NOOP_INSTANCE),
            "test.namespace", options, "0");
        subscription = aggregator.subscribe(published::add);
    }
}
