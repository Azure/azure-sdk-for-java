// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ApiTestBase;
import com.azure.messaging.eventhubs.models.EventHubProducerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.messaging.eventhubs.EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME;

public class EventHubConsumerIntegrationTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    private static final int NUMBER_OF_EVENTS = 10;

    private EventHubClient client;
    private EventHubConsumer consumer;

    // We use these values to keep track of the events we've pushed to the service and ensure the events we receive are
    // our own.
    private static final AtomicBoolean HAS_PUSHED_EVENTS = new AtomicBoolean();
    private static final String MESSAGE_TRACKING_VALUE = UUID.randomUUID().toString();
    private static final AtomicReference<Instant> MESSAGES_PUSHED_INSTANT = new AtomicReference<>();

    public EventHubConsumerIntegrationTest() {
        super(new ClientLogger(EventHubConsumerIntegrationTest.class));
    }

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        skipIfNotRecordMode();

        super.beforeTest();
        client = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .retry(RETRY_OPTIONS)
            .buildClient();

        setupEventTestData(client);

        consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID,
            EventPosition.fromEnqueuedTime(MESSAGES_PUSHED_INSTANT.get()));
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
            .collect(Collectors.toMap(EventData::sequenceNumber, Function.identity()));
        Assert.assertEquals(numberOfEvents, asList.size());

        final Map<Long, EventData> asList2 = actual2.stream()
            .collect(Collectors.toMap(EventData::sequenceNumber, Function.identity()));
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
        final EventHubConsumer consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, partitionId,
            EventPosition.fromEnqueuedTime(Instant.now()));
        final EventHubProducerOptions options = new EventHubProducerOptions().partitionId(partitionId);
        final EventHubProducer producer = client.createProducer(options);
        final List<EventData> events = TestUtils.getEvents(numberOfEvents, TestUtils.MESSAGE_TRACKING_ID)
            .collectList().block();
        producer.send(events);

        // Act
        final IterableStream<EventData> receive = consumer.receive(100, Duration.ofSeconds(5));

        // Assert
        final List<EventData> asList = receive.stream().collect(Collectors.toList());
        Assert.assertEquals(numberOfEvents, asList.size());
    }


    /**
     * Verify that we can receive until the timeout multiple times.
     */
    @Test
    public void receiveUntilTimeoutMultipleTimes() {
        // Arrange
        final int numberOfEvents = 15;
        final int numberOfEvents2 = 3;
        final String partitionId = "1";
        final EventHubConsumer consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, partitionId,
            EventPosition.fromEnqueuedTime(Instant.now()));
        final EventHubProducerOptions options = new EventHubProducerOptions().partitionId(partitionId);
        final EventHubProducer producer = client.createProducer(options);
        final List<EventData> events = TestUtils.getEventsAsList(numberOfEvents, TestUtils.MESSAGE_TRACKING_ID);
        final List<EventData> events2 = TestUtils.getEventsAsList(numberOfEvents2, TestUtils.MESSAGE_TRACKING_ID);
        producer.send(events);

        // Act
        final IterableStream<EventData> receive = consumer.receive(100, Duration.ofSeconds(3));

        producer.send(events2);

        final IterableStream<EventData> receive2 = consumer.receive(100, Duration.ofSeconds(3));

        // Assert
        final List<EventData> asList = receive.stream().collect(Collectors.toList());
        Assert.assertEquals(numberOfEvents, asList.size());

        final List<EventData> asList2 = receive2.stream().collect(Collectors.toList());
        Assert.assertEquals(numberOfEvents2, asList2.size());
    }

    /**
     * When we run this test, we check if there have been events already pushed to the partition, if not, we push some
     * events there.
     */
    private void setupEventTestData(EventHubClient client) {
        if (HAS_PUSHED_EVENTS.getAndSet(true)) {
            logger.info("Already pushed events to partition. Skipping.");
            return;
        }

        logger.info("Pushing events to partition. Message tracking value: {}", MESSAGE_TRACKING_VALUE);

        final EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(PARTITION_ID);
        final EventHubProducer producer = client.createProducer(producerOptions);
        final Flux<EventData> events = TestUtils.getEvents(NUMBER_OF_EVENTS, MESSAGE_TRACKING_VALUE);

        try {
            // So we know what instant those messages were pushed to the service and can fetch them.
            MESSAGES_PUSHED_INSTANT.set(Instant.now());
            producer.send(events.collectList().block());
        } finally {
            dispose(producer);
        }
    }
}
