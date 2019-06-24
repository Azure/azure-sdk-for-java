// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.Retry;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ApiTestBase;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Locale;

import static java.nio.charset.StandardCharsets.UTF_8;

public class EventPositionTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    private static final int NUMBER_OF_EVENTS = 10;

    private static EventData sequenceNumberEvent;
    private static EventData enqueuedEventData;
    private static EventData offsetEventData;

    private EventHubClient client;
    private EventHubProducer producer;
    private EventHubConsumer consumer;

    public EventPositionTest() {
        super(new ClientLogger(EventPositionTest.class));
    }

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        logger.asInfo().log("[{}]: Performing test set-up.", testName.getMethodName());
        final EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(PARTITION_ID).retry(Retry.getNoRetry()).timeout(Duration.ofSeconds(30));
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        client = new EventHubClient(getConnectionOptions(), getReactorProvider(), handlerProvider);
        producer = client.createProducer(producerOptions);
    }

    @Override
    protected void afterTest() {
        dispose(client, producer, consumer);
    }

    /**
     * Test for receiving message from earliest offset
     */
    @Ignore
    @Test
    public void receiveEarliestMessage() {
        skipIfNotRecordMode();

        // Arrange
        consumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.earliest());

        // Act & Assert
        producer.send(new EventData("testString".getBytes(UTF_8))).block(TIMEOUT);
        StepVerifier.create(consumer.receive().take(1))
            .expectNextCount(1)
            .verifyComplete();
    }

    /**
     * Test for receiving message from latest offset
     */
    @Ignore("Connection closed but test keeping running ")
    @Test
    public void receiveLatestMessage() {
        skipIfNotRecordMode();

        // Arrange
        consumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.latest());

        // Act & Assert
        StepVerifier.create(consumer.receive().take(1))
            .then(() -> producer.send(new EventData("test".getBytes())).block(TIMEOUT))
            .expectNextCount(1)
            .verifyComplete();
    }

    /**
     * Test for receiving messages start at enqueued time
     */
    @Ignore
    @Test
    public void receiveMessageFromEnqueuedTime() {
        skipIfNotRecordMode();

        // Arrange
        final EventPosition enqueuedTimeEventPosition = EventPosition.fromEnqueuedTime(Instant.now());
        consumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, enqueuedTimeEventPosition);

        // Act & Assert
        StepVerifier.create(consumer.receive().take(1))
            .then(() -> producer.send(new EventData("test".getBytes())).block(TIMEOUT))
            .expectNextCount(1)
            .verifyComplete();
    }

    /**
     * Test for receiving from start of stream
     */
    @Ignore
    @Test
    public void startOfStreamFilters() {
        skipIfNotRecordMode();

        // Arrange
        final EventPosition earliestEventPosition = EventPosition.earliest();
        final EventHubConsumer earliestConsumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, earliestEventPosition);
        final EventPosition enqueuedTimeEventPosition = EventPosition.fromEnqueuedTime(Instant.EPOCH);
        final EventHubConsumer enqueuedTimeConsumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, enqueuedTimeEventPosition);

        final Flux<EventData> earliestOffsetReceivedData = earliestConsumer.receive().take(NUMBER_OF_EVENTS);
        final Flux<EventData> enqueuedTimeReceivedData = enqueuedTimeConsumer.receive().take(NUMBER_OF_EVENTS);

        // Act & Assert
        StepVerifier.create(earliestOffsetReceivedData).expectNextCount(NUMBER_OF_EVENTS).verifyComplete();
        StepVerifier.create(enqueuedTimeReceivedData).expectNextCount(NUMBER_OF_EVENTS).verifyComplete();

        Iterable<EventData> earliestOffsetDataIterable = earliestOffsetReceivedData.toIterable();
        Iterator<EventData> enqueuedTimeDataIterator = enqueuedTimeReceivedData.toIterable().iterator();

        for (EventData offsetData : earliestOffsetDataIterable) {
            if (!enqueuedTimeDataIterator.hasNext()) {
                break;
            }
            EventData dateTimeEventData = enqueuedTimeDataIterator.next();
            // Check if both received data has matched offset
            Assert.assertTrue(
                String.format(Locale.US, "START_OF_STREAM offset: %s, EPOCH offset: %s", offsetData.offset(), dateTimeEventData.offset()),
                offsetData.offset().equalsIgnoreCase(dateTimeEventData.offset()));
        }

        dispose(enqueuedTimeConsumer, earliestConsumer);
    }

    /**
     * Test a consumer with inclusive offset
     */
    @Ignore
    @Test
    public void offsetInclusiveFilterFromEnqueuedTime() {
        skipIfNotRecordMode();

        // Arrange
        final EventPosition enqueuedTimeEventPosition = EventPosition.fromEnqueuedTime(Instant.now());
        final EventHubConsumer enqueuedTimeConsumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, enqueuedTimeEventPosition);
        // get the first event data
        StepVerifier.create(enqueuedTimeConsumer.receive().take(1))
            .then(() -> producer.send(new EventData("test".getBytes())).block(TIMEOUT))
            .assertNext(event -> enqueuedEventData = event)
            .verifyComplete();

        final EventPosition inclusiveSequenceNumberEventPosition = EventPosition.fromOffset(enqueuedEventData.offset(), true);
        final EventHubConsumer offsetConsumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, inclusiveSequenceNumberEventPosition);

        // Act & Assert
        StepVerifier.create(offsetConsumer.receive().take(1))
            .assertNext(event -> offsetEventData = event)
            .verifyComplete();

        Assert.assertEquals(enqueuedEventData.offset(), offsetEventData.offset());
        Assert.assertEquals(enqueuedEventData.sequenceNumber(), offsetEventData.sequenceNumber());

        dispose(enqueuedTimeConsumer, offsetConsumer);
    }

    /**
     * Test for receiving offset without inclusive filter
     */
    @Ignore
    @Test
    public void offsetNonInclusiveFilterFromEnqueuedTime() {
        skipIfNotRecordMode();

        // Arrange
        final EventPosition enqueuedTimeEventPosition = EventPosition.fromEnqueuedTime(Instant.now());
        final EventHubConsumer enqueuedTimeConsumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, enqueuedTimeEventPosition);
        // get the first event data
        StepVerifier.create(enqueuedTimeConsumer.receive().take(1))
            .then(() -> producer.send(new EventData("test".getBytes())).block(TIMEOUT))
            .assertNext(event -> enqueuedEventData = event)
            .verifyComplete();

        final EventPosition exclusiveSequenceNumberEventPosition = EventPosition.fromOffset(enqueuedEventData.offset(), false);
        final EventHubConsumer offsetConsumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, exclusiveSequenceNumberEventPosition);

        // Act & Assert
        StepVerifier.create(offsetConsumer.receive().take(1))
            .then(() -> producer.send(new EventData("test".getBytes())).block(TIMEOUT))
            .assertNext(event -> offsetEventData = event)
            .verifyComplete();

        Assert.assertEquals(enqueuedEventData.sequenceNumber() + 1, offsetEventData.sequenceNumber());

        dispose(enqueuedTimeConsumer, offsetConsumer);
    }

    /**
     * Test for receiving sequence number with inclusive filter
     */
    @Ignore
    @Test
    public void sequenceNumberInclusiveFilterFromEnqueuedTime() {
        skipIfNotRecordMode();

        // Arrange
        final EventPosition enqueuedTimeEventPosition = EventPosition.fromEnqueuedTime(Instant.now());
        final EventHubConsumer enqueuedTimeConsumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, enqueuedTimeEventPosition);
        // get the first event data
        StepVerifier.create(enqueuedTimeConsumer.receive().take(1))
            .then(() -> producer.send(new EventData("test".getBytes())).block(TIMEOUT))
            .assertNext(event -> enqueuedEventData = event)
            .verifyComplete();

        final EventPosition inclusiveSequenceNumberEventPosition = EventPosition.fromSequenceNumber(enqueuedEventData.sequenceNumber(), true);
        final EventHubConsumer sequenceNumberConsumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, inclusiveSequenceNumberEventPosition);

        // Act & Assert
        StepVerifier.create(sequenceNumberConsumer.receive().take(1))
            .assertNext(event -> offsetEventData = event)
            .verifyComplete();

        Assert.assertEquals(enqueuedEventData.offset(), offsetEventData.offset());
        Assert.assertEquals(enqueuedEventData.sequenceNumber(), offsetEventData.sequenceNumber());

        dispose(enqueuedTimeConsumer, sequenceNumberConsumer);
    }

    /**
     * Test for receiving sequence number without inclusive filter
     */
    @Ignore
    @Test
    public void sequenceNumberNonInclusiveFilterFromEnqueuedTime() {
        skipIfNotRecordMode();

        // Arrange
        final EventPosition enqueuedTimeEventPosition = EventPosition.fromEnqueuedTime(Instant.now());
        final EventHubConsumer enqueuedTimeConsumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, enqueuedTimeEventPosition);
        // get the first event data
        StepVerifier.create(enqueuedTimeConsumer.receive().take(1))
            .then(() -> producer.send(new EventData("test".getBytes())).block(TIMEOUT))
            .assertNext(event -> enqueuedEventData = event)
            .verifyComplete();

        final EventPosition exclusiveSequenceNumberEventPosition = EventPosition.fromSequenceNumber(enqueuedEventData.sequenceNumber(), false);
        final EventHubConsumer sequenceNumberConsumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, exclusiveSequenceNumberEventPosition);

        // Act & Assert
        StepVerifier.create(sequenceNumberConsumer.receive().take(1))
            .then(() -> producer.send(new EventData("test".getBytes())).block(TIMEOUT))
            .assertNext(event -> sequenceNumberEvent = event)
            .verifyComplete();

        Assert.assertEquals(enqueuedEventData.sequenceNumber() + 1, sequenceNumberEvent.sequenceNumber());

        dispose(enqueuedTimeConsumer, sequenceNumberConsumer);
    }
}
