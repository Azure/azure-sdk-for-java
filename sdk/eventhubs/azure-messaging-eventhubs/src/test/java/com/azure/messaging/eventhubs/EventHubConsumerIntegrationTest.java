// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.IntegrationTestBase;
import com.azure.messaging.eventhubs.implementation.IntegrationTestEventData;
import com.azure.messaging.eventhubs.models.EventPosition;
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

public class EventHubConsumerIntegrationTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "0";
    private static final int NUMBER_OF_EVENTS = 10;
    private static final AtomicBoolean HAS_PUSHED_EVENTS = new AtomicBoolean();
    private static volatile IntegrationTestEventData testData = null;

    private EventHubClientBuilder builder;
    private EventHubConsumer consumer;

    // We use these values to keep track of the events we've pushed to the service and ensure the events we receive are
    // our own.

    public EventHubConsumerIntegrationTest() {
        super(new ClientLogger(EventHubConsumerIntegrationTest.class));
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
        builder = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .scheduler(Schedulers.single())
            .retry(RETRY_OPTIONS);

        if (HAS_PUSHED_EVENTS.getAndSet(true)) {
            logger.info("Already pushed events to partition. Skipping.");
        } else {
            final SendOptions options = new SendOptions().setPartitionId(PARTITION_ID);
            testData = setupEventTestData(builder, NUMBER_OF_EVENTS, options);
        }

        builder.consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .startingPosition(EventPosition.fromEnqueuedTime(testData.getEnqueuedTime()))
            .partitionId(PARTITION_ID);
        consumer = builder.buildConsumer();
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
        final IterableStream<EventData> actual = consumer.receive(numberOfEvents, Duration.ofSeconds(10));

        // Assert
        final List<EventData> asList = actual.stream().collect(Collectors.toList());
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
        final IterableStream<EventData> actual = consumer.receive(numberOfEvents, waitTime);
        final IterableStream<EventData> actual2 = consumer.receive(secondNumberOfEvents, waitTime);

        // Assert
        final Map<Long, EventData> asList = actual.stream()
            .collect(Collectors.toMap(EventData::getSequenceNumber, Function.identity()));
        Assert.assertEquals(numberOfEvents, asList.size());

        final Map<Long, EventData> asList2 = actual2.stream()
            .collect(Collectors.toMap(EventData::getSequenceNumber, Function.identity()));
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

        final EventHubConnection connection = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .scheduler(Schedulers.single())
            .buildConnection();
        final EventHubClientBuilder builder = new EventHubClientBuilder()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .retry(RETRY_OPTIONS)
            .startingPosition(EventPosition.fromEnqueuedTime(Instant.now()))
            .partitionId(partitionId)
            .connection(connection);
        final EventHubConsumer consumer = builder.buildConsumer();
        final EventHubProducerClient producer = builder.buildProducer();
        final SendOptions sendOptions = new SendOptions().setPartitionId(partitionId);

        try {
            producer.send(events, sendOptions);

            // Act
            final IterableStream<EventData> receive = consumer.receive(100, Duration.ofSeconds(5));

            // Assert
            final List<EventData> asList = receive.stream().collect(Collectors.toList());
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

        final EventHubConnection connection = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .scheduler(Schedulers.single())
            .buildConnection();
        final EventHubClientBuilder builder = new EventHubClientBuilder()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .retry(RETRY_OPTIONS)
            .startingPosition(EventPosition.fromEnqueuedTime(Instant.now()))
            .partitionId(partitionId)
            .connection(connection);
        final EventHubConsumer consumer = builder.buildConsumer();
        final EventHubProducerClient producer = builder.buildProducer();
        final SendOptions sendOptions = new SendOptions().setPartitionId(partitionId);

        try {
            producer.send(events, sendOptions);

            // Act
            final IterableStream<EventData> receive = consumer.receive(receiveNumber, Duration.ofSeconds(5));

            // Assert
            final List<EventData> asList = receive.stream().collect(Collectors.toList());
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
        final EventHubConnection connection = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .scheduler(Schedulers.single())
            .buildConnection();
        final EventHubClientBuilder builder = new EventHubClientBuilder()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .retry(RETRY_OPTIONS)
            .startingPosition(EventPosition.fromEnqueuedTime(Instant.now()))
            .partitionId(partitionId)
            .connection(connection);
        final EventHubConsumer consumer = builder.buildConsumer();
        final EventHubConsumer consumer2 = builder.buildConsumer();

        final EventHubProducerClient producer = builder.buildProducer();
        final SendOptions sendOptions = new SendOptions().setPartitionId(partitionId);

        try {
            producer.send(events, sendOptions);

            // Act
            final IterableStream<EventData> receive = consumer.receive(receiveNumber, Duration.ofSeconds(5));
            final IterableStream<EventData> receive2 = consumer2.receive(receiveNumber, Duration.ofSeconds(5));

            // Assert
            final List<Long> asList = receive.stream().map(EventData::getSequenceNumber).collect(Collectors.toList());
            final List<Long> asList2 = receive2.stream().map(EventData::getSequenceNumber).collect(Collectors.toList());

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
     * Verifies we can get partition ids of an Event Hub.
     */
    @Test
    public void getPartitionIds() {
        // Act
        final IterableStream<String> response = consumer.getPartitionIds();

        // Assert
        Assert.assertNotNull(response);

        final List<String> partitionIds = response.stream().collect(Collectors.toList());
        Assert.assertTrue(partitionIds.size() > 1);
    }

    /**
     * Verifies we can get partition ids of an Event Hub.
     */
    @Test
    public void getMetadata() {
        // Arrange
        final ConnectionStringProperties connectionProperties = getConnectionStringProperties();

        // Act
        final EventHubProperties properties = consumer.getProperties();

        // Assert
        Assert.assertNotNull(properties);
        Assert.assertEquals(connectionProperties.getEntityPath(), properties.getName());
        Assert.assertTrue(properties.getCreatedAt().isBefore(Instant.now()));

        Assert.assertNotNull(properties.getPartitionIds());
        Assert.assertTrue(properties.getPartitionIds().length > 1);
    }

    /**
     * Verifies we can get partition ids of an Event Hub.
     */
    @Test
    public void getPartitionProperties() {
        // Arrange
        final ConnectionStringProperties connectionProperties = getConnectionStringProperties();
        final EventHubProperties properties = consumer.getProperties();
        final String partitionId = properties.getPartitionIds()[0];

        // Act
        final PartitionProperties partitionProperties = consumer.getPartitionProperties(partitionId);

        // Assert
        Assert.assertNotNull(partitionProperties);

        Assert.assertEquals(connectionProperties.getEntityPath(), partitionProperties.getEventHubName());
        Assert.assertEquals(partitionId, partitionProperties.getId());
    }

    private static List<EventData> getEventsAsList(int numberOfEvents) {
        return TestUtils.getEvents(numberOfEvents, TestUtils.MESSAGE_TRACKING_ID).collectList().block();
    }
}
