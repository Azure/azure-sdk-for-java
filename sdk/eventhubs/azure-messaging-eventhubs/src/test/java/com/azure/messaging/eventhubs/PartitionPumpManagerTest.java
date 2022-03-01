// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.messaging.eventhubs.implementation.PartitionProcessor;
import com.azure.messaging.eventhubs.implementation.PartitionProcessorException;
import com.azure.messaging.eventhubs.models.Checkpoint;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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

    @Mock
    private CheckpointStore checkpointStore;
    @Mock
    private EventHubClientBuilder builder;
    @Mock
    private EventHubAsyncClient asyncClient;
    @Mock
    private EventHubConsumerAsyncClient consumerAsyncClient;
    @Mock
    private TracerProvider tracerProvider;
    @Mock
    private PartitionProcessor partitionProcessor;

    private final Map<String, EventPosition> initialPartitionPositions = new HashMap<>();
    private final TestPublisher<PartitionEvent> receivePublisher = TestPublisher.createCold();

    private Checkpoint checkpoint;
    private PartitionOwnership partitionOwnership;
    private AutoCloseable autoCloseable;

    @BeforeEach
    public void beforeEach() {
        this.autoCloseable = MockitoAnnotations.openMocks(this);

        final Integer prefetch = 100;
        when(builder.getPrefetchCount()).thenReturn(prefetch);
        when(builder.buildAsyncClient()).thenReturn(asyncClient);

        // Consumer group and partition id don't change.
        when(asyncClient.createConsumer(eq(CONSUMER_GROUP), eq(prefetch)))
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
        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, supplier, builder,
            trackLastEnqueuedEventProperties, tracerProvider, initialPartitionPositions, maxBatchSize,
            maxWaitTime, batchReceiveMode);

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
            manager.stopAllPartitionPumps();
        }
    }

    /**
     * Does not create another partition pump when one is already running.
     */
    @Test
    public void startPartitionPumpOnce() {
        // Arrange
        final Map<String, EventPosition> initialPartitionEventPosition = new HashMap<>();
        final Supplier<PartitionProcessor> supplier = () -> {
            fail("should not have created a another processor");
            return partitionProcessor;
        };
        final boolean trackLastEnqueuedEventProperties = false;
        final int maxBatchSize = 4;
        final Duration maxWaitTime = Duration.ofSeconds(5);
        final boolean batchReceiveMode = true;
        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, supplier, builder,
            trackLastEnqueuedEventProperties, tracerProvider, initialPartitionEventPosition, maxBatchSize,
            maxWaitTime, batchReceiveMode);

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
        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, supplier, builder,
            trackLastEnqueuedEventProperties, tracerProvider, initialPartitionPositions, maxBatchSize,
            maxWaitTime, batchReceiveMode);

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
        final Map<String, EventPosition> initialPartitionEventPosition = new HashMap<>();
        final Supplier<PartitionProcessor> supplier = () -> {
            fail("should not have created a another processor");
            return partitionProcessor;
        };
        final boolean trackLastEnqueuedEventProperties = false;
        final int maxBatchSize = 4;
        final Duration maxWaitTime = Duration.ofSeconds(5);
        final boolean batchReceiveMode = true;
        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, supplier, builder,
            trackLastEnqueuedEventProperties, tracerProvider, initialPartitionEventPosition, maxBatchSize,
            maxWaitTime, batchReceiveMode);

        final String partition1 = "01";
        final EventHubConsumerAsyncClient client1 = mock(EventHubConsumerAsyncClient.class);
        final Scheduler scheduler1 = mock(Scheduler.class);
        final PartitionPump pump1 = new PartitionPump(partition1, client1, scheduler1);

        final String partition2 = "02";
        final EventHubConsumerAsyncClient client2 = mock(EventHubConsumerAsyncClient.class);
        final Scheduler scheduler2 = mock(Scheduler.class);
        final PartitionPump pump2 = new PartitionPump(partition2, client2, scheduler2);

        manager.getPartitionPumps().put(partition1, pump1);
        manager.getPartitionPumps().put(partition2, pump2);

        // Act
        manager.stopAllPartitionPumps();

        // Assert
        verify(scheduler1).dispose();
        verify(client1).close();

        verify(scheduler2).dispose();
        verify(client2).close();

        assertTrue(manager.getPartitionPumps().isEmpty());
    }

    /**
     * Verifies that we populate the lastEnqueuedEventProperties.
     */
    @Test
    public void processesEventBatchWithLastEnqueued() throws InterruptedException {
        // Arrange
        final Map<String, EventPosition> initialPartitionEventPosition = new HashMap<>();
        final Supplier<PartitionProcessor> supplier = () -> partitionProcessor;
        final CountDownLatch receiveCounter = new CountDownLatch(3);
        final boolean trackLastEnqueuedEventProperties = false;
        final int maxBatchSize = 2;
        final Duration maxWaitTime = Duration.ofSeconds(1);
        final boolean batchReceiveMode = true;
        final PartitionPumpManager manager = new PartitionPumpManager(checkpointStore, supplier, builder,
            trackLastEnqueuedEventProperties, tracerProvider, initialPartitionEventPosition, maxBatchSize,
            maxWaitTime, batchReceiveMode);

        // Mock events to add.
        final Instant retrievalTime = Instant.now();
        final Instant lastEnqueuedTime = retrievalTime.minusSeconds(60);
        final LastEnqueuedEventProperties lastEnqueuedProperties1 =
            new LastEnqueuedEventProperties(10L, 15L, retrievalTime, lastEnqueuedTime.plusSeconds(1));
        final EventData eventData1 = new EventData("1");
        final PartitionEvent partitionEvent1 = new PartitionEvent(PARTITION_CONTEXT, eventData1, lastEnqueuedProperties1);

        final LastEnqueuedEventProperties lastEnqueuedProperties2 =
            new LastEnqueuedEventProperties(20L, 25L, retrievalTime, lastEnqueuedTime.plusSeconds(2));
        final EventData eventData2 = new EventData("2");
        final PartitionEvent partitionEvent2 = new PartitionEvent(PARTITION_CONTEXT, eventData2, lastEnqueuedProperties2);

        final LastEnqueuedEventProperties lastEnqueuedProperties3 =
            new LastEnqueuedEventProperties(30L, 35L, retrievalTime, lastEnqueuedTime.plusSeconds(3));
        final EventData eventData3 = new EventData("3");
        final PartitionEvent partitionEvent3 = new PartitionEvent(PARTITION_CONTEXT, eventData3, lastEnqueuedProperties3);

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
            manager.stopAllPartitionPumps();
        }
    }
}
