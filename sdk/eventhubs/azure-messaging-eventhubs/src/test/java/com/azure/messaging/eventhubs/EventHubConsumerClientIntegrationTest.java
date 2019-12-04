// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;
import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_PREFETCH_COUNT;

public class EventHubConsumerClientIntegrationTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "0";
    private static final String PARTITION_ID_HEADER = "SENT_PARTITION_ID";
    private static final String MESSAGE_TRACKING_ID = UUID.randomUUID().toString();

    private static final int NUMBER_OF_EVENTS = 10;
    private static final AtomicBoolean HAS_PUSHED_EVENTS = new AtomicBoolean();
    private final String[] expectedPartitionIds = new String[]{"0", "1"};

    private static volatile IntegrationTestEventData testData = null;

    private EventHubClient client;
    private EventHubConsumerClient consumer;
    private EventHubConnection connection;
    private EventPosition startingPosition;

    // We use these values to keep track of the events we've pushed to the service and ensure the events we receive are
    // our own.

    public EventHubConsumerClientIntegrationTest() {
        super(new ClientLogger(EventHubConsumerClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .retry(RETRY_OPTIONS)
            .shareConnection()
            .buildClient();

        if (HAS_PUSHED_EVENTS.getAndSet(true)) {
            logger.info("Already pushed events to partition. Skipping.");
        } else {
            final SendOptions options = new SendOptions().setPartitionId(PARTITION_ID);

            final EventHubProducerClient producer = client.createProducer();
            testData = setupEventTestData(producer, NUMBER_OF_EVENTS, options);
        }

        startingPosition = EventPosition.fromEnqueuedTime(testData.getEnqueuedTime());
        consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, DEFAULT_PREFETCH_COUNT);
    }

    @Override
    protected void afterTest() {
        dispose(consumer, connection, client);
    }

    /**
     * Verifies that we can receive events a single time that is up to the batch size.
     */
    @Test
    public void receiveEvents() {
        // Arrange
        final int numberOfEvents = 5;

        // Act
        final IterableStream<PartitionEvent> actual = consumer.receiveFromPartition(PARTITION_ID, numberOfEvents, startingPosition,
            Duration.ofSeconds(10));

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
        final IterableStream<PartitionEvent> actual = consumer.receiveFromPartition(PARTITION_ID, numberOfEvents, startingPosition, waitTime);
        final IterableStream<PartitionEvent> actual2 = consumer.receiveFromPartition(PARTITION_ID, numberOfEvents, startingPosition, waitTime);

        // Assert
        final Map<Long, PartitionEvent> asList = actual.stream()
            .collect(Collectors.toMap(e -> e.getData().getSequenceNumber(), Function.identity()));
        Assertions.assertEquals(numberOfEvents, asList.size());

        final Map<Long, PartitionEvent> asList2 = actual2.stream()
            .collect(Collectors.toMap(e -> e.getData().getSequenceNumber(), Function.identity()));
        Assertions.assertEquals(numberOfEvents, asList2.size());

        for (Long key : asList.keySet()) {
            final PartitionEvent removed = asList2.remove(key);
            Assertions.assertNotNull(removed, String.format("Expecting '%s' to be in second set. But was not.", key));
        }

        Assertions.assertTrue(asList2.isEmpty(), "Expected all keys to be removed from second set.");
    }

    /**
     * Verify that we can receive until the timeout.
     */
    @Test
    public void receiveUntilTimeout() {
        // Arrange
        final int numberOfEvents = 15;
        final String partitionId = "1";
        final List<EventData> events = getEventsAsList(numberOfEvents);

        final EventPosition position = EventPosition.fromEnqueuedTime(Instant.now());
        final EventHubConsumerClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, DEFAULT_PREFETCH_COUNT);
        final SendOptions sendOptions = new SendOptions().setPartitionId(partitionId);
        final EventHubProducerClient producer = client.createProducer();

        try {
            producer.send(events, sendOptions);

            // Act
            final IterableStream<PartitionEvent> receive = consumer.receiveFromPartition(partitionId, 100, position, Duration.ofSeconds(20));

            // Assert
            final List<PartitionEvent> asList = receive.stream().collect(Collectors.toList());
            Assertions.assertEquals(numberOfEvents, asList.size());
        } finally {
            dispose(producer, consumer);
        }
    }

    /**
     * Verify that we don't continue to fetch more events when there are no listeners.
     */
    @Test
    public void doesNotContinueToReceiveEvents() {
        // Arrange
        final int numberOfEvents = 15;
        final int secondSetOfEvents = 25;
        final int receiveNumber = 10;
        final String partitionId = "1";

        final List<EventData> events = getEventsAsList(numberOfEvents);
        final List<EventData> events2 = getEventsAsList(secondSetOfEvents);

        final EventHubConsumerClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, DEFAULT_PREFETCH_COUNT);
        final SendOptions sendOptions = new SendOptions().setPartitionId(partitionId);
        final EventHubProducerClient producer = client.createProducer();

        try {
            final Instant enqueuedTime = Instant.now();
            producer.send(events, sendOptions);

            // Act
            final IterableStream<PartitionEvent> receive = consumer.receiveFromPartition(partitionId, receiveNumber,
                EventPosition.fromEnqueuedTime(enqueuedTime), Duration.ofSeconds(5));

            // Assert
            final List<PartitionEvent> asList = receive.stream().collect(Collectors.toList());
            Assertions.assertEquals(receiveNumber, asList.size());

            producer.send(events2);
        } finally {
            dispose(consumer, producer);
        }
    }

    /**
     * Verify that we don't continue to fetch more events when there are no listeners.
     */
    @Test
    public void multipleConsumers() {
        final int numberOfEvents = 15;
        final int receiveNumber = 10;
        final String partitionId = "1";

        final List<EventData> events = getEventsAsList(numberOfEvents);

        final EventPosition position = EventPosition.fromEnqueuedTime(Instant.now());
        final EventHubConsumerClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, DEFAULT_PREFETCH_COUNT);
        final EventHubConsumerClient consumer2 = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, DEFAULT_PREFETCH_COUNT);
        final EventHubProducerClient producer = client.createProducer();
        final SendOptions sendOptions = new SendOptions().setPartitionId(partitionId);

        try {
            producer.send(events, sendOptions);

            // Act
            final IterableStream<PartitionEvent> receive = consumer.receiveFromPartition(partitionId, receiveNumber, position, Duration.ofSeconds(5));
            final IterableStream<PartitionEvent> receive2 = consumer2.receiveFromPartition(partitionId, receiveNumber, position, Duration.ofSeconds(5));

            // Assert
            final List<Long> asList = receive.stream().map(e -> e.getData().getSequenceNumber()).collect(Collectors.toList());
            final List<Long> asList2 = receive2.stream().map(e -> e.getData().getSequenceNumber()).collect(Collectors.toList());

            Assertions.assertEquals(receiveNumber, asList.size());
            Assertions.assertEquals(receiveNumber, asList2.size());

            Collections.sort(asList);
            Collections.sort(asList2);

            final Long[] first = asList.toArray(new Long[0]);
            final Long[] second = asList2.toArray(new Long[0]);

            Assertions.assertArrayEquals(first, second);
        } finally {
            dispose(consumer, producer);
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
            Assertions.assertEquals(2, properties.getPartitionIds().stream().count());
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

            Assertions.assertEquals(2, collect.size());
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
            for (String partitionId : expectedPartitionIds) {
                final PartitionProperties properties = consumer.getPartitionProperties(partitionId);
                Assertions.assertEquals(consumer.getEventHubName(), properties.getEventHubName());
                Assertions.assertEquals(partitionId, properties.getId());
            }
        } finally {
            dispose(consumer);
        }
    }

    private static List<EventData> getEventsAsList(int numberOfEvents) {
        return TestUtils.getEvents(numberOfEvents, MESSAGE_TRACKING_ID).collectList().block();
    }
}
