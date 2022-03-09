// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for synchronous {@link EventHubConsumerClient}.
 */
@Tag(TestUtils.INTEGRATION)
public class EventHubConsumerClientIntegrationTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "0";

    private IntegrationTestEventData testData = null;
    private EventHubConsumerClient consumer;
    private EventPosition startingPosition;

    public EventHubConsumerClientIntegrationTest() {
        super(new ClientLogger(EventHubConsumerClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        consumer = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .retry(RETRY_OPTIONS)
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .buildConsumerClient();

        final Map<String, IntegrationTestEventData> integrationTestData = getTestData();
        this.testData = integrationTestData.get(PARTITION_ID);
        Assertions.assertNotNull(testData, "'testData' should have been populated.");

        startingPosition = EventPosition.fromEnqueuedTime(testData.getPartitionProperties().getLastEnqueuedTime());
    }

    @Override
    protected void afterTest() {
        dispose(consumer);
    }

    /**
     * Verifies that we can receive events a single time that is up to the batch size.
     */
    @Test
    public void receiveEvents() {
        // Arrange
        final int numberOfEvents = 5;

        // Act
        final IterableStream<PartitionEvent> actual = consumer.receiveFromPartition(PARTITION_ID, numberOfEvents,
            startingPosition, Duration.ofSeconds(15));

        // Assert
        final List<PartitionEvent> asList = actual.stream().collect(Collectors.toList());
        Assertions.assertEquals(numberOfEvents, asList.size());
    }

    /**
     * Verifies that we can receive multiple times and each time, the same set is received.
     */
    @Test
    public void receiveEventsMultipleTimes() {
        // Arrange
        final int numberOfEvents = 5;
        final Duration waitTime = Duration.ofSeconds(10);

        // Act
        final IterableStream<PartitionEvent> actual = consumer.receiveFromPartition(PARTITION_ID, numberOfEvents,
            startingPosition, waitTime);
        final Map<Long, PartitionEvent> asList = actual.stream()
            .collect(Collectors.toMap(e -> e.getData().getSequenceNumber(), Function.identity()));
        Assertions.assertEquals(numberOfEvents, asList.size());

        final IterableStream<PartitionEvent> actual2 = consumer.receiveFromPartition(PARTITION_ID, numberOfEvents,
            startingPosition, waitTime);
        final Map<Long, PartitionEvent> asList2 = actual2.stream()
            .collect(Collectors.toMap(e -> e.getData().getSequenceNumber(), Function.identity()));

        // Assert
        Assertions.assertEquals(numberOfEvents, asList2.size());

        for (Long key : asList.keySet()) {
            final PartitionEvent removed = asList2.remove(key);
            Assertions.assertNotNull(removed, String.format("Expecting '%s' to be in second set. But was not.", key));
        }

        assertTrue(asList2.isEmpty(), "Expected all keys to be removed from second set.");
    }

    /**
     * Verify that we can receive until the timeout.
     */
    @Test
    public void receiveUntilTimeout() {
        // Act
        final int maximumSize = 100;
        final int eventSize = testData.getEvents().size();
        final IterableStream<PartitionEvent> receive = consumer.receiveFromPartition(PARTITION_ID,
            100, startingPosition, Duration.ofSeconds(20));

        // Assert
        final List<PartitionEvent> asList = receive.stream().collect(Collectors.toList());
        final int actual = asList.size();
        assertTrue(eventSize <= actual && actual <= maximumSize,
            String.format("Should be between %s and %s. Actual: %s", eventSize, maximumSize, actual));
    }

    /**
     * Verify that we don't continue to fetch more events when there are no listeners.
     */
    @Test
    public void doesNotContinueToReceiveEvents() {
        // Arrange
        final List<EventData> events = testData.getEvents();
        final int numberOfEvents = Math.floorDiv(events.size(), 2);
        final EventPosition position = EventPosition.fromSequenceNumber(
            testData.getPartitionProperties().getLastEnqueuedSequenceNumber());

        // Act
        final IterableStream<PartitionEvent> receive = consumer.receiveFromPartition(testData.getPartitionId(),
            numberOfEvents, position, Duration.ofSeconds(5));

        // Assert
        final List<PartitionEvent> asList = receive.stream().collect(Collectors.toList());
        assertTrue(!asList.isEmpty() && asList.size() <= numberOfEvents,
            String.format("Expected: %s. Actual: %s", numberOfEvents, asList.size()));
    }

    /**
     * Verify that we receive the same set of events when we receive twice.
     */
    @Test
    public void multipleConsumers() {
        final int receiveNumber = 10;
        final String partitionId = "1";
        final Map<String, IntegrationTestEventData> testData = getTestData();
        final IntegrationTestEventData integrationTestEventData = testData.get(partitionId);

        final long offset = Long.parseLong(integrationTestEventData.getPartitionProperties().getLastEnqueuedOffset());
        final EventPosition startingPosition = EventPosition.fromOffset(offset);
        final EventHubClientBuilder builder = createBuilder().consumerGroup(DEFAULT_CONSUMER_GROUP_NAME);
        final EventHubConsumerClient consumer = builder.buildConsumerClient();
        final EventHubConsumerClient consumer2 = builder.buildConsumerClient();
        final Duration firstReceive = Duration.ofSeconds(30);
        final Duration secondReceiveDuration = firstReceive.plus(firstReceive);

        try {

            // Act
            final IterableStream<PartitionEvent> receive = consumer.receiveFromPartition(partitionId, receiveNumber,
                startingPosition, firstReceive);
            final IterableStream<PartitionEvent> receive2 = consumer2.receiveFromPartition(partitionId, receiveNumber,
                startingPosition, secondReceiveDuration);

            // Assert
            final List<Long> asList = receive.stream().map(e -> e.getData().getSequenceNumber())
                .collect(Collectors.toList());
            final List<Long> asList2 = receive2.stream().map(e -> e.getData().getSequenceNumber())
                .collect(Collectors.toList());

            assertFalse(asList.isEmpty());
            assertFalse(asList2.isEmpty());
        } finally {
            dispose(consumer, consumer2);
        }
    }

    /**
     * Verifies that we can get the metadata about an Event Hub
     */
    @Test
    public void getEventHubProperties() {
        final EventHubConsumerClient consumer = createBuilder()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .buildConsumerClient();

        // Act & Assert
        try {
            final EventHubProperties properties = consumer.getEventHubProperties();
            Assertions.assertNotNull(properties);
            Assertions.assertEquals(consumer.getEventHubName(), properties.getName());
            Assertions.assertEquals(NUMBER_OF_PARTITIONS, properties.getPartitionIds().stream().count());
        } finally {
            dispose(consumer);
        }
    }

    /**
     * Verifies that we can get the partition identifiers of an Event Hub.
     */
    @Test
    public void getPartitionIds() {
        final EventHubConsumerClient consumer = createBuilder()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .buildConsumerClient();

        // Act & Assert
        try {
            final IterableStream<String> partitionIds = consumer.getPartitionIds();
            final List<String> collect = partitionIds.stream().collect(Collectors.toList());

            Assertions.assertEquals(NUMBER_OF_PARTITIONS, collect.size());
        } finally {
            dispose(consumer);
        }
    }

    /**
     * Verifies that we can get partition information for each of the partitions in an Event Hub.
     */
    @Test
    public void getPartitionProperties() {
        final EventHubConsumerClient consumer = createBuilder()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .buildConsumerClient();

        // Act & Assert
        try {
            for (String partitionId : EXPECTED_PARTITION_IDS) {
                final PartitionProperties properties = consumer.getPartitionProperties(partitionId);
                Assertions.assertEquals(consumer.getEventHubName(), properties.getEventHubName());
                Assertions.assertEquals(partitionId, properties.getId());
            }
        } finally {
            dispose(consumer);
        }
    }
}
