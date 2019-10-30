// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.IntegrationTestBase;
import com.azure.messaging.eventhubs.implementation.IntegrationTestEventData;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;

public class EventHubConsumerClientIntegrationTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "0";
    private static final int NUMBER_OF_EVENTS = 10;
    private static final AtomicBoolean HAS_PUSHED_EVENTS = new AtomicBoolean();
    private final String[] expectedPartitionIds = new String[]{"0", "1"};

    private static volatile IntegrationTestEventData testData = null;

    private EventHubClient client;
    private EventHubConsumerClient consumer;

    // We use these values to keep track of the events we've pushed to the service and ensure the events we receive are
    // our own.

    public EventHubConsumerClientIntegrationTest() {
        super(new ClientLogger(EventHubConsumerClientIntegrationTest.class));
    }

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String getTestName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .scheduler(Schedulers.single())
            .retry(RETRY_OPTIONS)
            .buildClient();

        if (HAS_PUSHED_EVENTS.getAndSet(true)) {
            logger.info("Already pushed events to partition. Skipping.");
        } else {
            final SendOptions options = new SendOptions().setPartitionId(PARTITION_ID);
            testData = setupEventTestData(client, NUMBER_OF_EVENTS, options);
        }

        consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME,
            EventPosition.fromEnqueuedTime(testData.getEnqueuedTime()));
    }

    @Override
    protected void afterTest() {
        dispose(consumer, client);
    }

    /**
     * Verifies that we can receive events a single time that is up to the batch size.
     */
    @Test
    public void receiveEvents() {
        // Arrange
        final int numberOfEvents = 5;

        // Act
        final IterableStream<PartitionEvent> actual = consumer.receive(PARTITION_ID, numberOfEvents, Duration.ofSeconds(10));

        // Assert
        final List<PartitionEvent> asList = actual.stream().collect(Collectors.toList());
        Assert.assertEquals(numberOfEvents, asList.size());
    }

    /**
     * Verifies that we can receive multiple times.
     */
    @Test
    public void receiveEventsMultipleTimes() {
        // Arrange
        final int numberOfEvents = 5;
        final int secondNumberOfEvents = 2;
        final Duration waitTime = Duration.ofSeconds(10);

        // Act
        final IterableStream<PartitionEvent> actual = consumer.receive(PARTITION_ID, numberOfEvents, waitTime);
        final IterableStream<PartitionEvent> actual2 = consumer.receive(PARTITION_ID, secondNumberOfEvents, waitTime);

        // Assert
        final Map<Long, PartitionEvent> asList = actual.stream()
            .collect(Collectors.toMap(e -> e.getEventData().getSequenceNumber(), Function.identity()));
        Assert.assertEquals(numberOfEvents, asList.size());

        final Map<Long, PartitionEvent> asList2 = actual2.stream()
            .collect(Collectors.toMap(e -> e.getEventData().getSequenceNumber(), Function.identity()));
        Assert.assertEquals(secondNumberOfEvents, asList2.size());

        final Long maximumSequence = Collections.max(asList.keySet());
        final Long minimumSequence = Collections.min(asList2.keySet());

        Assert.assertTrue("The minimum in second receive should be less than first receive.",
            maximumSequence < minimumSequence);
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
        final EventHubConsumerClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, position);
        final SendOptions sendOptions = new SendOptions().setPartitionId(partitionId);
        final EventHubProducerClient producer = client.createProducer();

        try {
            producer.send(events, sendOptions);

            // Act
            final IterableStream<PartitionEvent> receive = consumer.receive(partitionId, 100, Duration.ofSeconds(5));

            // Assert
            final List<PartitionEvent> asList = receive.stream().collect(Collectors.toList());
            Assert.assertEquals(numberOfEvents, asList.size());
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

        final EventHubConsumerClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME,
            EventPosition.fromEnqueuedTime(Instant.now()));
        final SendOptions sendOptions = new SendOptions().setPartitionId(partitionId);
        final EventHubProducerClient producer = client.createProducer();

        try {
            producer.send(events, sendOptions);

            // Act
            final IterableStream<PartitionEvent> receive = consumer.receive(partitionId, receiveNumber, Duration.ofSeconds(5));

            // Assert
            final List<PartitionEvent> asList = receive.stream().collect(Collectors.toList());
            Assert.assertEquals(receiveNumber, asList.size());

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
        final EventHubConsumerClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, position);
        final EventHubConsumerClient consumer2 = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, position);
        final EventHubProducerClient producer = client.createProducer();
        final SendOptions sendOptions = new SendOptions().setPartitionId(partitionId);

        try {
            producer.send(events, sendOptions);

            // Act
            final IterableStream<PartitionEvent> receive = consumer.receive(partitionId, receiveNumber, Duration.ofSeconds(5));
            final IterableStream<PartitionEvent> receive2 = consumer2.receive(partitionId, receiveNumber, Duration.ofSeconds(5));

            // Assert
            final List<Long> asList = receive.stream().map(e -> e.getEventData().getSequenceNumber()).collect(Collectors.toList());
            final List<Long> asList2 = receive2.stream().map(e -> e.getEventData().getSequenceNumber()).collect(Collectors.toList());

            Assert.assertEquals(receiveNumber, asList.size());
            Assert.assertEquals(receiveNumber, asList2.size());

            Collections.sort(asList);
            Collections.sort(asList2);

            final Long[] first = asList.toArray(new Long[0]);
            final Long[] second = asList2.toArray(new Long[0]);

            Assert.assertArrayEquals(first, second);
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
            .startingPosition(EventPosition.earliest())
            .buildConsumer();

        // Act & Assert
        try {
            final EventHubProperties properties = consumer.getProperties();
            Assert.assertNotNull(properties);
            Assert.assertEquals(consumer.getEventHubName(), properties.getName());
            Assert.assertEquals(2, properties.getPartitionIds().length);
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
            .startingPosition(EventPosition.earliest())
            .buildConsumer();

        // Act & Assert
        try {
            final IterableStream<String> partitionIds = consumer.getPartitionIds();
            final List<String> collect = partitionIds.stream().collect(Collectors.toList());

            Assert.assertEquals(2, collect.size());
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
            .startingPosition(EventPosition.earliest())
            .buildConsumer();

        // Act & Assert
        try {
            for (String partitionId : expectedPartitionIds) {
                final PartitionProperties properties = consumer.getPartitionProperties(partitionId);
                Assert.assertEquals(consumer.getEventHubName(), properties.getEventHubName());
                Assert.assertEquals(partitionId, properties.getId());
            }
        } finally {
            dispose(consumer);
        }
    }


    private static List<EventData> getEventsAsList(int numberOfEvents) {
        return TestUtils.getEvents(numberOfEvents, TestUtils.MESSAGE_TRACKING_ID).collectList().block();
    }
}
