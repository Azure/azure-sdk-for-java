// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.implementation.PartitionProcessor;
import com.azure.messaging.eventhubs.implementation.PartitionProcessorException;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsConsumerInstrumentation;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link PartitionPumpManager}
 */
public class PartitionPumpManagerTest {
    private static final String PARTITION_ID = "2";
    private static final String CONSUMER_GROUP = "consumer-group1";
    private static final String FULLY_QUALIFIED_NAME = "fully-qualified-name";
    private static final String EVENTHUB_NAME = "eventhub-name";
    private static final String OWNER_ID = "owner-id1";
    private static final String ETAG = "etag1";
    private static final PartitionContext PARTITION_CONTEXT = new PartitionContext(FULLY_QUALIFIED_NAME, EVENTHUB_NAME,
        CONSUMER_GROUP, PARTITION_ID);
    private static final EventHubsConsumerInstrumentation DEFAULT_INSTRUMENTATION = new EventHubsConsumerInstrumentation(null, null,
        FULLY_QUALIFIED_NAME, EVENTHUB_NAME, CONSUMER_GROUP, false);
    @Mock
    private CheckpointStore checkpointStore;
    @Mock
    private EventHubClientBuilder builder;
    @Mock
    private EventHubAsyncClient asyncClient;
    @Mock
    private EventHubConsumerAsyncClient consumerAsyncClient;
    @Mock
    private PartitionProcessor partitionProcessor;

    private final Map<String, EventPosition> initialPartitionPositions = new HashMap<>();
    private final TestPublisher<PartitionEvent> receivePublisher = TestPublisher.createCold();
    private final Integer prefetch = 100;
    private Checkpoint checkpoint;
    private PartitionOwnership partitionOwnership;
    private AutoCloseable autoCloseable;

    @BeforeEach
    public void beforeEach() {
        this.autoCloseable = MockitoAnnotations.openMocks(this);

        when(builder.getPrefetchCount()).thenReturn(prefetch);
        when(builder.buildAsyncClient()).thenReturn(asyncClient);

        // Consumer group and partition id don't change.
        when(asyncClient.createConsumer(eq(CONSUMER_GROUP), eq(prefetch), eq(true)))
            .thenReturn(consumerAsyncClient);
        when(consumerAsyncClient.receiveFromPartition(eq(PARTITION_ID), any(EventPosition.class),
            any(ReceiveOptions.class)))
            .thenReturn(receivePublisher.flux());

        // Pre-populating variables with information that is always the same.
        this.checkpoint = new Checkpoint()
            .setPartitionId(PARTITION_ID)
            .setConsumerGroup(CONSUMER_GROUP)
            .setEventHubName(EVENTHUB_NAME)
            .setFullyQualifiedNamespace(FULLY_QUALIFIED_NAME);
        this.partitionOwnership = new PartitionOwnership()
            .setPartitionId(PARTITION_ID)
            .setConsumerGroup(CONSUMER_GROUP)
            .setEventHubName(EVENTHUB_NAME)
            .setFullyQualifiedNamespace(FULLY_QUALIFIED_NAME)
            .setOwnerId(OWNER_ID)
            .setETag(ETAG);
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (autoCloseable != null) {
            autoCloseable.close();
        }

        Mockito.framework().clearInlineMock(this);
    }

    public static Stream<Arguments> startPartitionPumpAtCorrectPosition() {
        final EventPosition mapPosition = EventPosition.fromSequenceNumber(165L);
        final long sequenceNumber = 15L;
        final long offset = 10L;

        return Stream.of(
            // Offset is used if available.
            Arguments.of(offset, sequenceNumber, mapPosition, EventPosition.fromOffset(offset)),
            // Sequence number is the fallback.
            Arguments.of(null, sequenceNumber, mapPosition, EventPosition.fromSequenceNumber(sequenceNumber)),
            // if both are null, then use the initial Map position is used.
            Arguments.of(null, null, mapPosition, mapPosition),
            // Fallback to start listening from the latest part of the stream.
            Arguments.of(null, null, null, EventPosition.latest())
        );
    }

    /**
     * Verifies that we start our receiver client at the correct position.
     */
    @MethodSource
    @ParameterizedTest
    public void startPartitionPumpAtCorrectPosition(Long offset, Long sequenceNumber, EventPosition initialPosition,
        EventPosition expectedPosition) {

        // Arrange
        if (initialPosition != null) {
            initialPartitionPositions.put(PARTITION_ID, initialPosition);
        }

        checkpoint.setOffset(offset)
            .setSequenceNumber(sequenceNumber);
        partitionOwnership
            .setLastModifiedTime(OffsetDateTime.now().toEpochSecond());

        final Supplier<PartitionProcessor> supplier = () -> partitionProcessor;
        final boolean trackLastEnqueuedEventProperties = false;
        final int maxBatchSize = 4;
        final Duration maxWaitTime = Duration.ofSeconds(5);
        final boolean batchReceiveMode = true;
        final EventProcessorClientOptions options = new EventProcessorClientOptions()
            .setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(trackLastEnqueuedEventProperties)
            .setInitialEventPositionProvider(id -> initialPartitionPositions.get(id))
            .setMaxBatchSize(maxBatchSize)
            .setMaxWaitTime(maxWaitTime)
            .setBatchReceiveMode(batchReceiveMode)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, supplier, builder,
            DEFAULT_INSTRUMENTATION, options);

        try {
            // Act
            manager.startPartitionPump(partitionOwnership, checkpoint);

            // Assert
            verify(consumerAsyncClient).receiveFromPartition(eq(PARTITION_ID),
                argThat(x -> expectedPosition.equals(x)),
                argThat(x -> x.getTrackLastEnqueuedEventProperties() == trackLastEnqueuedEventProperties));

            final PartitionPump actualPump = manager.getPartitionPumps().get(PARTITION_ID);
            assertNotNull(actualPump);

            // Verify that the correct position was used.
            verify(consumerAsyncClient).receiveFromPartition(eq(PARTITION_ID),
                argThat(position -> expectedPosition.equals(position)),
                argThat(option -> option != null
                    && option.getTrackLastEnqueuedEventProperties() == trackLastEnqueuedEventProperties));

            // Verify that initializeContext() was called.
            verify(partitionProcessor).initialize(argThat(x -> {
                final PartitionContext context = x.getPartitionContext();
                if (context == null) {
                    return false;
                }

                return PARTITION_ID.equals(context.getPartitionId())
                    && EVENTHUB_NAME.equals(context.getEventHubName())
                    && CONSUMER_GROUP.equals(context.getConsumerGroup())
                    && FULLY_QUALIFIED_NAME.equals(context.getFullyQualifiedNamespace());
            }));
        } finally {
            // Want to make sure we dispose of resources we create. (ie. schedulers)
            manager.stopAllPartitionPumps().block();
        }
    }

    /**
     * Does not create another partition pump when one is already running.
     */
    @Test
    public void startPartitionPumpOnce() {
        // Arrange
        final Supplier<PartitionProcessor> supplier = () -> {
            fail("should not have created a another processor");
            return partitionProcessor;
        };
        final boolean trackLastEnqueuedEventProperties = false;
        final int maxBatchSize = 4;
        final Duration maxWaitTime = Duration.ofSeconds(5);
        final boolean batchReceiveMode = true;
        final EventProcessorClientOptions options = new EventProcessorClientOptions()
            .setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(trackLastEnqueuedEventProperties)
            .setInitialEventPositionProvider(id -> initialPartitionPositions.get(id))
            .setMaxBatchSize(maxBatchSize)
            .setMaxWaitTime(maxWaitTime)
            .setBatchReceiveMode(batchReceiveMode)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, supplier, builder,
            DEFAULT_INSTRUMENTATION, options);

        checkpoint.setOffset(1L).setSequenceNumber(10L);
        partitionOwnership.setLastModifiedTime(OffsetDateTime.now().toEpochSecond());

        // Adds a partition pump, as if that there is already one started.
        final PartitionPump partitionPump = mock(PartitionPump.class);
        manager.getPartitionPumps().put(checkpoint.getPartitionId(), partitionPump);

        // Act
        manager.startPartitionPump(partitionOwnership, checkpoint);

        // Assert
        final PartitionPump actual = manager.getPartitionPumps().get(checkpoint.getPartitionId());
        assertEquals(partitionPump, actual);

        verify(builder, never()).buildAsyncClient();
    }

    /**
     * Verifies that we clean up the partition pump if receive call is not successful.
     */
    @Test
    public void startPartitionPumpCleansUpOnError() {
        partitionOwnership.setLastModifiedTime(OffsetDateTime.now().toEpochSecond());

        final Supplier<PartitionProcessor> supplier = () -> partitionProcessor;
        final boolean trackLastEnqueuedEventProperties = false;
        final int maxBatchSize = 4;
        final Duration maxWaitTime = Duration.ofSeconds(5);
        final boolean batchReceiveMode = true;
        final EventProcessorClientOptions options = new EventProcessorClientOptions()
            .setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(trackLastEnqueuedEventProperties)
            .setInitialEventPositionProvider(id -> initialPartitionPositions.get(id))
            .setMaxBatchSize(maxBatchSize)
            .setMaxWaitTime(maxWaitTime)
            .setBatchReceiveMode(batchReceiveMode)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, supplier, builder,
            DEFAULT_INSTRUMENTATION, options);

        final Exception testException = new IllegalStateException("Dummy exception.");
        when(consumerAsyncClient.receiveFromPartition(
            eq(PARTITION_ID),
            argThat(position -> EventPosition.latest().equals(position)),
            argThat(option -> option.getTrackLastEnqueuedEventProperties() == trackLastEnqueuedEventProperties)))
            .thenAnswer(invocation -> {
                throw testException;
            });

        // Act
        final PartitionProcessorException error = assertThrows(PartitionProcessorException.class,
            () -> manager.startPartitionPump(partitionOwnership, checkpoint));

        // Assert
        assertNotNull(error.getCause());
        assertEquals(testException, error.getCause());
        assertFalse(manager.getPartitionPumps().containsKey(PARTITION_ID));

        verify(consumerAsyncClient).close();
    }

    /**
     * Verifies that it cleans up partition pumps and resources when stopping.
     */
    @Test
    public void stopAllPartitionPumps() {
        // Arrange
        final Supplier<PartitionProcessor> supplier = () -> {
            fail("should not have created a another processor");
            return partitionProcessor;
        };
        final boolean trackLastEnqueuedEventProperties = false;
        final int maxBatchSize = 4;
        final Duration maxWaitTime = Duration.ofSeconds(5);
        final boolean batchReceiveMode = true;
        final EventProcessorClientOptions options = new EventProcessorClientOptions()
            .setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(trackLastEnqueuedEventProperties)
            .setInitialEventPositionProvider(id -> initialPartitionPositions.get(id))
            .setMaxBatchSize(maxBatchSize)
            .setMaxWaitTime(maxWaitTime)
            .setBatchReceiveMode(batchReceiveMode)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, supplier, builder,
            DEFAULT_INSTRUMENTATION, options);

        final String partition1 = "01";
        final EventHubConsumerAsyncClient client1 = mock(EventHubConsumerAsyncClient.class);
        final Scheduler scheduler1 = mock(Scheduler.class);
        when(scheduler1.disposeGracefully()).thenReturn(Mono.empty());
        final PartitionPump pump1 = new PartitionPump(partition1, client1, scheduler1);

        final String partition2 = "02";
        final EventHubConsumerAsyncClient client2 = mock(EventHubConsumerAsyncClient.class);
        final Scheduler scheduler2 = mock(Scheduler.class);
        when(scheduler2.disposeGracefully()).thenReturn(Mono.empty());
        final PartitionPump pump2 = new PartitionPump(partition2, client2, scheduler2);

        manager.getPartitionPumps().put(partition1, pump1);
        manager.getPartitionPumps().put(partition2, pump2);

        // Act
        manager.stopAllPartitionPumps().block();

        // Assert
        verify(scheduler1).disposeGracefully();
        verify(client1).close();

        verify(scheduler2).disposeGracefully();
        verify(client2).close();

        assertTrue(manager.getPartitionPumps().isEmpty());
    }

    /**
     * Verifies that we populate the lastEnqueuedEventProperties.
     */
    @Test
    public void processesEventBatchWithLastEnqueued() throws InterruptedException {
        // Arrange
        final Supplier<PartitionProcessor> supplier = () -> partitionProcessor;
        final CountDownLatch receiveCounter = new CountDownLatch(3);
        final boolean trackLastEnqueuedEventProperties = true;
        final int maxBatchSize = 2;
        final Duration maxWaitTime = Duration.ofSeconds(1);
        final boolean batchReceiveMode = true;
        final EventProcessorClientOptions options = new EventProcessorClientOptions()
            .setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(trackLastEnqueuedEventProperties)
            .setInitialEventPositionProvider(id -> initialPartitionPositions.get(id))
            .setMaxBatchSize(maxBatchSize)
            .setMaxWaitTime(maxWaitTime)
            .setBatchReceiveMode(batchReceiveMode)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, supplier, builder,
            DEFAULT_INSTRUMENTATION, options);

        // Mock events to add.
        final Instant retrievalTime = Instant.now();
        final PartitionEvent partitionEvent1 = createEvent(retrievalTime, 1);
        final PartitionEvent partitionEvent2 = createEvent(retrievalTime, 2);
        final PartitionEvent partitionEvent3 = createEvent(retrievalTime, 3);

        final AtomicInteger eventCounter = new AtomicInteger();

        doAnswer(invocation -> {
            final EventBatchContext batch = invocation.getArgument(0);
            assertNotNull(batch.getPartitionContext());
            assertNotNull(batch.getLastEnqueuedEventProperties());

            if (batch.getEvents().isEmpty()) {
                receiveCounter.countDown();
            }

            eventCounter.addAndGet(batch.getEvents().size());
            return null;
        }).when(partitionProcessor).processEventBatch(any(EventBatchContext.class));

        try {
            // Start receiving events from the partition.
            manager.startPartitionPump(partitionOwnership, checkpoint);

            receivePublisher.next(partitionEvent1, partitionEvent2, partitionEvent3);

            final boolean await = receiveCounter.await(20, TimeUnit.SECONDS);
            assertTrue(await);

            receivePublisher.next(partitionEvent3);

            // Verify
            verify(partitionProcessor, never()).processError(any(ErrorContext.class));

            // We want to have invoked a couple of empty windowTimeout frames and actually received the 3 events.
            verify(partitionProcessor, atLeastOnce())
                .processEventBatch(argThat(context -> context.getEvents().isEmpty()));
            verify(partitionProcessor, atMost(3))
                .processEventBatch(argThat(context -> !context.getEvents().isEmpty()));

            // Verify that we have at least seen this as the last enqueued event.
            verify(partitionProcessor).processEventBatch(
                argThat(context -> !context.getEvents().isEmpty()
                    && partitionEvent3.getLastEnqueuedEventProperties().equals(context.getLastEnqueuedEventProperties())));

            assertEquals(3, eventCounter.get());
        } finally {
            manager.stopAllPartitionPumps().block();
        }
    }

    /**
     * Checks that number of prefetched events stays under allowed maximum.
     */
    @ParameterizedTest
    @ValueSource(ints = {1, 16, 64, 128})
    public void processBatchPrefetch(int maxBatchSize) throws InterruptedException {
        // Arrange
        final int batches = 5;
        final int maxExpectedPrefetched = Math.max(prefetch / maxBatchSize, 1) * maxBatchSize;

        final CountDownLatch receiveCounter = new CountDownLatch(batches);

        final EventProcessorClientOptions options = new EventProcessorClientOptions()
            .setConsumerGroup("test-consumer")
            .setMaxBatchSize(maxBatchSize)
            .setBatchReceiveMode(true);

        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, () -> partitionProcessor, builder,
            DEFAULT_INSTRUMENTATION, options);

        final AtomicInteger publishedCounter = new AtomicInteger();
        final Instant retrievalTime = Instant.now();

        Flux<PartitionEvent> events = Flux.generate(s -> {
            int publishedIndex = publishedCounter.getAndIncrement();
            if (publishedIndex <= maxBatchSize * batches + prefetch + 1000) {
                s.next(createEvent(retrievalTime, publishedIndex));
            } else {
                s.complete();
            }
        });

        when(consumerAsyncClient.receiveFromPartition(eq(PARTITION_ID), any(EventPosition.class),
            any(ReceiveOptions.class))).thenReturn(events);

        final AtomicInteger maxPrefetched = new AtomicInteger();
        final AtomicInteger processedCounter = new AtomicInteger();
        doAnswer(invocation -> {
            final EventBatchContext batch = invocation.getArgument(0);
            if (!batch.getEvents().isEmpty()) {
                receiveCounter.countDown();

                int published = publishedCounter.get();
                int processed = processedCounter.addAndGet(batch.getEvents().size());
                if (published - processed > maxPrefetched.get()) {
                    maxPrefetched.set(published - processed);
                }
            }
            return null;
        }).when(partitionProcessor).processEventBatch(any(EventBatchContext.class));

        try {
            manager.startPartitionPump(partitionOwnership, checkpoint);
            assertTrue(receiveCounter.await(10, TimeUnit.SECONDS));
            verify(partitionProcessor, never()).processError(any(ErrorContext.class));
            assertTrue(maxPrefetched.get() <= maxExpectedPrefetched,
                String.format("Expected at most %s events to be prefetched, got %s", maxExpectedPrefetched, maxPrefetched.get()));
        } finally {
            manager.stopAllPartitionPumps().block();
        }
    }

    /**
     * Checks that events are processed if batch size is higher than number of available events after max wait time is reached
     */
    @Test
    public void processBatchNotEnoughEventsAfterMaxTime() throws InterruptedException {
        // Arrange
        final CountDownLatch receiveCounter = new CountDownLatch(1);

        final int maxBatchSize = 16;
        final EventProcessorClientOptions options = new EventProcessorClientOptions()
            .setConsumerGroup("test-consumer")
            .setMaxBatchSize(maxBatchSize)
            .setMaxWaitTime(Duration.ofSeconds(3))
            .setBatchReceiveMode(true);

        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, () -> partitionProcessor, builder,
            DEFAULT_INSTRUMENTATION, options);

        final Instant retrievalTime = Instant.now();

        doAnswer(invocation -> {
            final EventBatchContext batch = invocation.getArgument(0);
            if (!batch.getEvents().isEmpty()) {
                receiveCounter.countDown();
            }
            return null;
        }).when(partitionProcessor).processEventBatch(any(EventBatchContext.class));

        try {
            manager.startPartitionPump(partitionOwnership, checkpoint);

            receivePublisher.next(createEvent(retrievalTime, 0), createEvent(retrievalTime, 1));
            assertTrue(receiveCounter.await(20, TimeUnit.SECONDS));
            verify(partitionProcessor, never()).processError(any(ErrorContext.class));
        } finally {
            manager.stopAllPartitionPumps().block();
        }
    }

    /**
     * Checks that events are NOT processed if batch size is higher than number of available events if max time is not set
     * TODO (limolkova): https://github.com/Azure/azure-sdk-for-java/issues/38586
     */
    @Test
    public void processBatchNotEnoughEventsNever() throws InterruptedException {
        // Arrange
        final CountDownLatch receiveCounter = new CountDownLatch(1);

        final int maxBatchSize = 16;
        final EventProcessorClientOptions options = new EventProcessorClientOptions()
            .setConsumerGroup("test-consumer")
            .setMaxBatchSize(maxBatchSize)
            .setMaxWaitTime(null)
            .setBatchReceiveMode(true);

        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, () -> partitionProcessor, builder,
            DEFAULT_INSTRUMENTATION, options);

        final Instant retrievalTime = Instant.now();

        doAnswer(invocation -> {
            final EventBatchContext batch = invocation.getArgument(0);
            if (!batch.getEvents().isEmpty()) {
                receiveCounter.countDown();
            }
            return null;
        }).when(partitionProcessor).processEventBatch(any(EventBatchContext.class));

        try {
            manager.startPartitionPump(partitionOwnership, checkpoint);

            receivePublisher.next(createEvent(retrievalTime, 0), createEvent(retrievalTime, 1));
            assertFalse(receiveCounter.await(10, TimeUnit.SECONDS));
            verify(partitionProcessor, never()).processError(any(ErrorContext.class));
        } finally {
            manager.stopAllPartitionPumps().block();
        }
    }

    /**
     * If no checkpoint, no map position, no default position, will use {@link EventPosition#latest()}.
     */
    @Test
    public void startPositionReturnsLatest() {
        // Arrange
        final String partitionId = "the-partition-id";
        final Supplier<PartitionProcessor> supplier = () -> partitionProcessor;
        final boolean trackLastEnqueuedEventProperties = false;
        final int maxBatchSize = 2;
        final Duration maxWaitTime = Duration.ofSeconds(1);
        final boolean batchReceiveMode = true;
        final EventProcessorClientOptions options = new EventProcessorClientOptions()
            .setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(trackLastEnqueuedEventProperties)
            .setInitialEventPositionProvider(null)
            .setMaxBatchSize(maxBatchSize)
            .setMaxWaitTime(maxWaitTime)
            .setBatchReceiveMode(batchReceiveMode)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        final EventPosition expected = EventPosition.latest();

        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, supplier, builder,
            DEFAULT_INSTRUMENTATION, options);

        // Act
        final EventPosition actual = manager.getInitialEventPosition(partitionId, null);

        // Assert
        assertEquals(expected, actual);
    }

    /**
     * Offset is preferred over sequence number if it is part of the checkpoint.
     */
    @Test
    public void startPositionReturnsCheckpointOffset() {
        // Arrange
        final String partitionId = "the-partition-id";
        initialPartitionPositions.put(partitionId, EventPosition.fromSequenceNumber(11L, true));
        initialPartitionPositions.put("another", EventPosition.earliest());

        final long offset = 242343;
        final long sequenceNumber = 150;
        checkpoint.setOffset(offset)
            .setSequenceNumber(sequenceNumber);

        final EventPosition expected = EventPosition.fromOffset(offset);

        final Supplier<PartitionProcessor> supplier = () -> partitionProcessor;
        final boolean trackLastEnqueuedEventProperties = false;
        final int maxBatchSize = 2;
        final Duration maxWaitTime = Duration.ofSeconds(1);
        final boolean batchReceiveMode = true;

        final EventProcessorClientOptions options = new EventProcessorClientOptions()
            .setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(trackLastEnqueuedEventProperties)
            .setInitialEventPositionProvider(
                unused -> EventPosition.fromEnqueuedTime(Instant.ofEpochMilli(1692830454030L)))
            .setMaxBatchSize(maxBatchSize)
            .setMaxWaitTime(maxWaitTime)
            .setBatchReceiveMode(batchReceiveMode)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);


        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, supplier, builder,
            DEFAULT_INSTRUMENTATION, options);

        // Act
        final EventPosition actual = manager.getInitialEventPosition(partitionId, checkpoint);

        // Assert
        assertEquals(expected, actual);
    }

    /**
     * Sequence number is used if offset is null.
     */
    @Test
    public void startPositionReturnsCheckpointSequenceNumber() {
        // Arrange
        final String partitionId = "the-partition-id";
        initialPartitionPositions.put(partitionId, EventPosition.fromSequenceNumber(11L, true));
        initialPartitionPositions.put("another", EventPosition.earliest());

        final long sequenceNumber = 150;
        checkpoint.setSequenceNumber(sequenceNumber);

        final Supplier<PartitionProcessor> supplier = () -> partitionProcessor;
        final boolean trackLastEnqueuedEventProperties = false;
        final int maxBatchSize = 2;
        final Duration maxWaitTime = Duration.ofSeconds(1);
        final boolean batchReceiveMode = true;
        final EventProcessorClientOptions options = new EventProcessorClientOptions()
            .setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(trackLastEnqueuedEventProperties)
            .setInitialEventPositionProvider(id -> initialPartitionPositions.get(id))
            .setMaxBatchSize(maxBatchSize)
            .setMaxWaitTime(maxWaitTime)
            .setBatchReceiveMode(batchReceiveMode)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        final EventPosition expected = EventPosition.fromSequenceNumber(sequenceNumber);

        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, supplier, builder,
            DEFAULT_INSTRUMENTATION, options);

        // Act
        final EventPosition actual = manager.getInitialEventPosition(partitionId, checkpoint);

        // Assert
        assertEquals(expected, actual);
    }

    /**
     * If no checkpoint, prefers the position found in the initialPartitionEventPosition map.
     */
    @Test
    public void startPositionReturnsMapPosition() {
        // Arrange
        final String partitionId = "the-partition-id";
        final EventPosition mapPosition = EventPosition.fromSequenceNumber(11L, true);
        initialPartitionPositions.put(partitionId, mapPosition);
        initialPartitionPositions.put("another", EventPosition.earliest());

        final Supplier<PartitionProcessor> supplier = () -> partitionProcessor;
        final boolean trackLastEnqueuedEventProperties = false;
        final int maxBatchSize = 2;
        final Duration maxWaitTime = Duration.ofSeconds(1);
        final boolean batchReceiveMode = true;
        final EventProcessorClientOptions options = new EventProcessorClientOptions()
            .setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(trackLastEnqueuedEventProperties)
            .setInitialEventPositionProvider(id -> initialPartitionPositions.get(id))
            .setMaxBatchSize(maxBatchSize)
            .setMaxWaitTime(maxWaitTime)
            .setBatchReceiveMode(batchReceiveMode)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, supplier, builder,
            DEFAULT_INSTRUMENTATION, options);

        // Act
        final EventPosition actual = manager.getInitialEventPosition(partitionId, checkpoint);

        // Assert
        assertEquals(mapPosition, actual);
    }

    /**
     * If no checkpoint, no map position, uses the default position.
     */
    @Test
    public void startPositionReturnsDefaultPosition() {
        // Arrange
        final String partitionId = "the-partition-id";
        final EventPosition defaultEventPosition = EventPosition.fromEnqueuedTime(Instant.ofEpochMilli(1692830454030L));

        final Supplier<PartitionProcessor> supplier = () -> partitionProcessor;
        final boolean trackLastEnqueuedEventProperties = false;
        final int maxBatchSize = 2;
        final Duration maxWaitTime = Duration.ofSeconds(1);
        final boolean batchReceiveMode = true;
        final EventProcessorClientOptions options = new EventProcessorClientOptions()
            .setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(trackLastEnqueuedEventProperties)
            .setInitialEventPositionProvider(unused -> defaultEventPosition)
            .setMaxBatchSize(maxBatchSize)
            .setMaxWaitTime(maxWaitTime)
            .setBatchReceiveMode(batchReceiveMode)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, supplier, builder,
            DEFAULT_INSTRUMENTATION, options);

        // Act
        final EventPosition actual = manager.getInitialEventPosition(partitionId, checkpoint);

        // Assert
        assertEquals(defaultEventPosition, actual);
    }

    /**
     * Verifies that an exception thrown from user code in {@link PartitionProcessor#processError(ErrorContext)} still
     * cleans up the partition.
     */
    @Test
    public void processErrorCleansUpPartitionOnException() throws InterruptedException {
        final Supplier<PartitionProcessor> supplier = () -> partitionProcessor;
        final CountDownLatch receiveCounter = new CountDownLatch(3);

        final boolean trackLastEnqueuedEventProperties = false;
        final int maxBatchSize = 2;
        final Duration maxWaitTime = Duration.ofSeconds(1);
        final boolean batchReceiveMode = true;
        final EventProcessorClientOptions options = new EventProcessorClientOptions()
            .setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(trackLastEnqueuedEventProperties)
            .setInitialEventPositionProvider(id -> initialPartitionPositions.get(id))
            .setMaxBatchSize(maxBatchSize)
            .setMaxWaitTime(maxWaitTime)
            .setBatchReceiveMode(batchReceiveMode)
            .setLoadBalancerUpdateInterval(Duration.ofSeconds(10))
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, supplier, builder,
            DEFAULT_INSTRUMENTATION, options);

        // Mock events to add.
        final EventData eventData1 = new EventData("1");
        final PartitionEvent partitionEvent1 = new PartitionEvent(PARTITION_CONTEXT, eventData1, null);

        final EventData eventData2 = new EventData("2");
        final PartitionEvent partitionEvent2 = new PartitionEvent(PARTITION_CONTEXT, eventData2, null);

        final EventData eventData3 = new EventData("3");
        final PartitionEvent partitionEvent3 = new PartitionEvent(PARTITION_CONTEXT, eventData3, null);

        final Exception testException = new IllegalStateException("Dummy exception.");
        final Exception processErrorException = new NumberFormatException("Test exception in process error");

        doAnswer(invocation -> {
            final EventBatchContext batch = invocation.getArgument(0);
            assertNotNull(batch.getPartitionContext());

            if (batch.getEvents().isEmpty()) {
                receiveCounter.countDown();
            }

            return null;
        }).when(partitionProcessor).processEventBatch(any(EventBatchContext.class));

        doAnswer(invocationOnMock -> {
            throw processErrorException;
        }).when(partitionProcessor).processError(any(ErrorContext.class));

        try {
            // Start receiving events from the partition.
            manager.startPartitionPump(partitionOwnership, checkpoint);

            receivePublisher.next(partitionEvent1, partitionEvent2, partitionEvent3);
            receivePublisher.error(testException);

            // We won't reach the countdown number because an exception receiving messages results in losing the
            // partition.
            final boolean await = receiveCounter.await(20, TimeUnit.SECONDS);
            assertFalse(await);

            // Verify
            // We called the user processError
            verify(partitionProcessor).processError(argThat(error -> testException.equals(error.getThrowable())));

            // The window is 2 events, we publish 3 events before throwing an error, it should only have been called
            // at most 1 time.
            verify(partitionProcessor, atMost(1))
                .processEventBatch(argThat(context -> !context.getEvents().isEmpty()));

            // Assert that we cleaned up the code.
            assertFalse(manager.getPartitionPumps().containsKey(PARTITION_ID));
            verify(consumerAsyncClient).close();

        } finally {
            manager.stopAllPartitionPumps().block();
        }
    }

    /**
     * Verifies that an exception thrown from user code in {@link PartitionProcessor#close(CloseContext)} when handling
     * an error, still cleans up the partition processor.
     */
    @Test
    public void closeOnErrorCleansUpPartitionOnException() throws InterruptedException {
        final Supplier<PartitionProcessor> supplier = () -> partitionProcessor;
        final CountDownLatch receiveCounter = new CountDownLatch(3);
        final Duration updateInterval = Duration.ofSeconds(10);

        final boolean trackLastEnqueuedEventProperties = false;
        final int maxBatchSize = 2;
        final Duration maxWaitTime = Duration.ofSeconds(1);
        final boolean batchReceiveMode = true;
        final EventProcessorClientOptions options = new EventProcessorClientOptions()
            .setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(trackLastEnqueuedEventProperties)
            .setInitialEventPositionProvider(id -> initialPartitionPositions.get(id))
            .setMaxBatchSize(maxBatchSize)
            .setMaxWaitTime(maxWaitTime)
            .setBatchReceiveMode(batchReceiveMode)
            .setLoadBalancerUpdateInterval(updateInterval)
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, supplier, builder,
            DEFAULT_INSTRUMENTATION, options);

        // Mock events to add.
        final EventData eventData1 = new EventData("1");
        final PartitionEvent partitionEvent1 = new PartitionEvent(PARTITION_CONTEXT, eventData1, null);

        final EventData eventData2 = new EventData("2");
        final PartitionEvent partitionEvent2 = new PartitionEvent(PARTITION_CONTEXT, eventData2, null);

        final EventData eventData3 = new EventData("3");
        final PartitionEvent partitionEvent3 = new PartitionEvent(PARTITION_CONTEXT, eventData3, null);

        final Exception testException = new IllegalStateException("Dummy exception.");
        final Exception processCloseException = new NumberFormatException("Test exception in process error");

        doAnswer(invocation -> {
            final EventBatchContext batch = invocation.getArgument(0);
            assertNotNull(batch.getPartitionContext());

            if (batch.getEvents().isEmpty()) {
                receiveCounter.countDown();
            }

            return null;
        }).when(partitionProcessor).processEventBatch(any(EventBatchContext.class));

        doAnswer(invocationOnMock -> {
            throw processCloseException;
        }).when(partitionProcessor).close(any(CloseContext.class));

        try {
            // Start receiving events from the partition.
            manager.startPartitionPump(partitionOwnership, checkpoint);

            receivePublisher.next(partitionEvent1, partitionEvent2, partitionEvent3);
            receivePublisher.error(testException);

            // We won't reach the countdown number because an exception receiving messages results in losing the
            // partition.
            final boolean await = receiveCounter.await(20, TimeUnit.SECONDS);
            assertFalse(await);

            // Verify
            // The window is 2 events, we publish 3 events before throwing an error, it should only have been called
            // at most 1 time.
            verify(partitionProcessor, atMost(1))
                .processEventBatch(argThat(context -> !context.getEvents().isEmpty()));

            // We called the user processError
            verify(partitionProcessor).processError(argThat(error -> testException.equals(error.getThrowable())));

            // We called the user close
            verify(partitionProcessor).close(argThat(closeContext -> closeContext.getPartitionContext() != null
                && PARTITION_ID.equals(closeContext.getPartitionContext().getPartitionId())));

            // Assert that we cleaned up the code.
            assertFalse(manager.getPartitionPumps().containsKey(PARTITION_ID));
            verify(consumerAsyncClient).close();

        } finally {
            manager.stopAllPartitionPumps().block();
        }
    }

    /**
     * Verifies that an exception thrown from user code in {@link PartitionProcessor#close(CloseContext)} when handling
     * a normal close operation.
     */
    @Test
    public void closeCleansUpPartitionOnException() throws InterruptedException {
        final Supplier<PartitionProcessor> supplier = () -> partitionProcessor;
        final CountDownLatch receiveCounter = new CountDownLatch(3);
        final Duration updateInterval = Duration.ofSeconds(10);

        final boolean trackLastEnqueuedEventProperties = false;
        final int maxBatchSize = 2;
        final Duration maxWaitTime = Duration.ofSeconds(1);
        final boolean batchReceiveMode = true;
        final EventProcessorClientOptions options = new EventProcessorClientOptions()
            .setConsumerGroup("test-consumer")
            .setTrackLastEnqueuedEventProperties(trackLastEnqueuedEventProperties)
            .setInitialEventPositionProvider(id -> initialPartitionPositions.get(id))
            .setMaxBatchSize(maxBatchSize)
            .setMaxWaitTime(maxWaitTime)
            .setBatchReceiveMode(batchReceiveMode)
            .setLoadBalancerUpdateInterval(updateInterval)
            .setPartitionOwnershipExpirationInterval(Duration.ofMinutes(1))
            .setLoadBalancingStrategy(LoadBalancingStrategy.BALANCED);

        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, supplier, builder,
            DEFAULT_INSTRUMENTATION, options);

        // Mock events to add.
        final EventData eventData1 = new EventData("1");
        final PartitionEvent partitionEvent1 = new PartitionEvent(PARTITION_CONTEXT, eventData1, null);

        final EventData eventData2 = new EventData("2");
        final PartitionEvent partitionEvent2 = new PartitionEvent(PARTITION_CONTEXT, eventData2, null);

        final EventData eventData3 = new EventData("3");
        final PartitionEvent partitionEvent3 = new PartitionEvent(PARTITION_CONTEXT, eventData3, null);

        final Exception processCloseException = new NumberFormatException("Test exception in process error");

        doAnswer(invocation -> {
            final EventBatchContext batch = invocation.getArgument(0);
            assertNotNull(batch.getPartitionContext());

            if (batch.getEvents().isEmpty()) {
                receiveCounter.countDown();
            }

            return null;
        }).when(partitionProcessor).processEventBatch(any(EventBatchContext.class));

        doAnswer(invocationOnMock -> {
            throw processCloseException;
        }).when(partitionProcessor).close(any(CloseContext.class));

        try {
            // Start receiving events from the partition.
            manager.startPartitionPump(partitionOwnership, checkpoint);

            receivePublisher.next(partitionEvent1, partitionEvent2, partitionEvent3);
            receivePublisher.complete();

            // We won't reach the countdown number because an exception receiving messages results in losing the
            // partition.
            final boolean await = receiveCounter.await(20, TimeUnit.SECONDS);
            assertFalse(await);

            // Verify
            // The window is 2 events, we publish 3 events before completing. We expect the last window emits on close.
            verify(partitionProcessor, times(2))
                .processEventBatch(argThat(context -> !context.getEvents().isEmpty()));

            // We called the user processError
            verify(partitionProcessor, never()).processError(any());

            // We called the user close
            verify(partitionProcessor).close(argThat(closeContext -> closeContext.getPartitionContext() != null
                && PARTITION_ID.equals(closeContext.getPartitionContext().getPartitionId())));

            // Assert that we cleaned up the code.
            assertFalse(manager.getPartitionPumps().containsKey(PARTITION_ID));
            verify(consumerAsyncClient).close();

        } finally {
            manager.stopAllPartitionPumps().block();
        }
    }

    private PartitionEvent createEvent(Instant retrievalTime, int index) {
        Instant lastEnqueuedTime = retrievalTime.minusSeconds(60);
        LastEnqueuedEventProperties lastEnqueuedProperties =
            new LastEnqueuedEventProperties((long) index, (long) index, retrievalTime, lastEnqueuedTime.plusSeconds(index));
        return new PartitionEvent(PARTITION_CONTEXT, new EventData(String.valueOf(index)), lastEnqueuedProperties);
    }
}
