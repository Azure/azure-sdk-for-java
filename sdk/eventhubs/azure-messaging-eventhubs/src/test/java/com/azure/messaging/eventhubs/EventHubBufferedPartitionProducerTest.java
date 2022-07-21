// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient.BufferedProducerClientOptions;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.SendBatchFailedContext;
import com.azure.messaging.eventhubs.models.SendBatchSucceededContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.azure.messaging.eventhubs.EventDataAggregatorTest.setupBatchMock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests {@link EventHubBufferedPartitionProducer}
 */
public class EventHubBufferedPartitionProducerTest {
    private static final String PARTITION_ID = "10";
    private static final String NAMESPACE = "test-eventhubs-namespace";
    private static final String EVENT_HUB_NAME = "test-hub";
    private static final List<String> PARTITION_IDS = Arrays.asList("one", "two", PARTITION_ID, "four");

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
            options);

        // Act & Assert
        StepVerifier.create(producer.enqueueEvent(event1))
            .verifyComplete();

        StepVerifier.create(producer.enqueueEvent(event2))
            .verifyComplete();

        assertTrue(successSemaphore.tryAcquire(waitTime.toMillis(), TimeUnit.MILLISECONDS),
            "Should have been able to get a successful batch pushed downstream.");

        assertEquals(1, holder.succeededContexts.size());

        final SendBatchSucceededContext first = holder.succeededContexts.get(0);
        assertEquals(PARTITION_ID, first.getPartitionId());
        assertEquals(batchEvents, first.getEvents());

        assertEquals(2, batchEvents.size());

        assertTrue(holder.failedContexts.isEmpty());
    }

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
            options);

        // Act & Assert
        StepVerifier.create(Mono.when(producer.enqueueEvent(event1), producer.enqueueEvent(event2)))
            .verifyComplete();

        StepVerifier.create(Mono.when(producer.enqueueEvent(event3)))
            .thenAwait(options.getMaxWaitTime())
            .verifyComplete();

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
            options);

        // Act & Assert
        StepVerifier.create(Mono.when(producer.enqueueEvent(event1), producer.enqueueEvent(event2)))
            .verifyComplete();

        StepVerifier.create(producer.enqueueEvent(event3))
            .thenAwait(options.getMaxWaitTime())
            .verifyComplete();

        StepVerifier.create(Mono.when(producer.enqueueEvent(event4), producer.enqueueEvent(event5)))
            .thenAwait(options.getMaxWaitTime())
            .verifyComplete();

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
        final CountDownLatch success = new CountDownLatch(2);
        failedSemaphore.acquire();

        final InvocationHolder holder = new InvocationHolder();
        final BufferedProducerClientOptions options = new BufferedProducerClientOptions();
        options.setMaxWaitTime(Duration.ofSeconds(3));
        options.setSendSucceededContext(context -> {
            System.out.println("Batch received.");
            holder.onSucceed(context);
            success.countDown();
        });
        options.setSendFailedContext(context -> holder.onFailed(context));

        final Duration waitTime = options.getMaxWaitTime().plus(options.getMaxWaitTime());

        final List<EventData> batchEvents = new ArrayList<>();
        setupBatchMock(batch, batchEvents, event1);

        final List<EventData> batchEvents2 = new ArrayList<>();
        setupBatchMock(batch2, batchEvents2, event2, event3);

        final List<EventData> batchEvents3 = new ArrayList<>();
        final EventData event5 = new EventData("five");
        setupBatchMock(batch3, batchEvents3, event4, event5);

        // Delaying send operation.
        when(client.send(any(EventDataBatch.class))).thenAnswer(invocation -> Mono.delay(options.getMaxWaitTime()).then());

        final EventHubBufferedPartitionProducer producer = new EventHubBufferedPartitionProducer(client, PARTITION_ID,
            options);

        // Act & Assert
        StepVerifier.create(Mono.when(producer.enqueueEvent(event1), producer.enqueueEvent(event2), producer.enqueueEvent(event3)), 1L)
            .then(() -> {
                // event1 was enqueued, event2 is in a batch, and event3 is currently in the queue waiting to be
                // pushed downstream.
                // batch1 (with event1) is being sent at the moment with the delay of options.getMaxWaitTime(), so the
                // buffer doesn't drain so quickly.
                final int bufferedEventCount = producer.getBufferedEventCount();
                assertEquals(1, bufferedEventCount);
            })
            .verifyComplete();

        StepVerifier.create(Mono.when(producer.enqueueEvent(event4), producer.enqueueEvent(event5)))
            .verifyComplete();

        final long totalTime = waitTime.toMillis() + waitTime.toMillis();
        assertTrue(success.await(totalTime, TimeUnit.MILLISECONDS),
            "Should have been able to get a successful signal downstream.");

        assertEquals(2, holder.succeededContexts.size());

        // Verify the completed ones.
        final SendBatchSucceededContext first = holder.succeededContexts.get(0);
        assertEquals(PARTITION_ID, first.getPartitionId());
        assertEquals(batchEvents, first.getEvents());

        final SendBatchSucceededContext second = holder.succeededContexts.get(1);
        assertEquals(PARTITION_ID, second.getPartitionId());
        assertEquals(batchEvents2, second.getEvents());
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
