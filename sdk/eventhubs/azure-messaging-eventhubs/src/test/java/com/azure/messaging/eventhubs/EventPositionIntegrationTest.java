// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ApiTestBase;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.messaging.eventhubs.models.EventHubProducerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.messaging.eventhubs.EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME;
import static com.azure.messaging.eventhubs.TestUtils.MESSAGE_TRACKING_ID;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Tests that {@link EventHubConsumer} can be created with various {@link EventPosition EventPositions}.
 */
public class EventPositionIntegrationTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    private static final int NUMBER_OF_EVENTS = 10;

    // We use these values to keep track of the events we've pushed to the service and ensure the events we receive are
    // our own.
    private static final AtomicBoolean HAS_PUSHED_EVENTS = new AtomicBoolean();
    private static final AtomicReference<EventData[]> EVENTS_PUSHED = new AtomicReference<>();
    private static final String MESSAGE_TRACKING_VALUE = UUID.randomUUID().toString();
    private static final AtomicReference<Instant> MESSAGES_PUSHED_INSTANT = new AtomicReference<>();

    private EventHubAsyncClient client;

    public EventPositionIntegrationTest() {
        super(new ClientLogger(EventPositionIntegrationTest.class));
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

        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        client = new EventHubAsyncClient(getConnectionOptions(), getReactorProvider(), handlerProvider);

        setupEventTestData(client);
    }

    @Override
    protected void afterTest() {
        dispose(client);
    }

    /**
     * Test that we receive the same messages using {@link EventPosition#earliest()} and {@link
     * EventPosition#fromEnqueuedTime(Instant)} where the enqueued time is {@link Instant#EPOCH}.
     */
    @Test
    public void receiveEarliestMessages() {
        // Arrange
        final EventHubConsumer consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.earliest());
        final EventHubConsumer enqueuedTimeConsumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.EPOCH));

        final List<EventData> earliestEvents;
        final List<EventData> enqueuedEvents;
        try {
            // Act
            earliestEvents = consumer.receive().take(NUMBER_OF_EVENTS).collectList().block(TIMEOUT);
            enqueuedEvents = enqueuedTimeConsumer.receive().take(NUMBER_OF_EVENTS).collectList().block(TIMEOUT);
        } finally {
            dispose(consumer, enqueuedTimeConsumer);
        }

        // Assert
        Assert.assertNotNull(earliestEvents);
        Assert.assertNotNull(enqueuedEvents);

        Assert.assertEquals(NUMBER_OF_EVENTS, earliestEvents.size());
        Assert.assertEquals(NUMBER_OF_EVENTS, enqueuedEvents.size());

        // EventData implements Comparable, so we can sort these and ensure that the events received are the same.
        Collections.sort(earliestEvents);
        Collections.sort(enqueuedEvents);

        for (int i = 0; i < NUMBER_OF_EVENTS; i++) {
            final EventData event = earliestEvents.get(i);
            final EventData event2 = enqueuedEvents.get(i);
            final String eventBody = UTF_8.decode(event.body()).toString();
            final String event2Body = UTF_8.decode(event2.body()).toString();

            Assert.assertEquals(event.sequenceNumber(), event2.sequenceNumber());
            Assert.assertEquals(event.offset(), event2.offset());
            Assert.assertEquals(eventBody, event2Body);
        }
    }

    /**
     * Verify that if no new items are added at the end of the stream, we don't get any events.
     */
    @Test
    public void receiveLatestMessagesNoneAdded() {
        // Arrange
        final EventHubConsumer consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.latest());

        // Act & Assert
        try {
            StepVerifier.create(consumer.receive().filter(event -> isMatchingEvent(event, MESSAGE_TRACKING_VALUE))
                .take(Duration.ofSeconds(3)))
                .expectComplete()
                .verify();
        } finally {
            dispose(consumer);
        }
    }

    /**
     * Test for receiving message from latest offset
     */
    @Test
    public void receiveLatestMessages() throws InterruptedException {
        // Arrange
        final String messageValue = UUID.randomUUID().toString();
        final EventHubConsumer consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.latest());
        final EventHubProducerOptions options = new EventHubProducerOptions().partitionId(PARTITION_ID);
        final EventHubProducer producer = client.createProducer(options);
        final Flux<EventData> events = Flux.range(0, NUMBER_OF_EVENTS).map(number -> {
            final EventData eventData = new EventData(("Event " + number).getBytes(UTF_8));
            eventData.addProperty(MESSAGE_TRACKING_ID, messageValue);
            return eventData;
        });

        final CountDownLatch countDownLatch = new CountDownLatch(NUMBER_OF_EVENTS);
        final Disposable subscription = consumer.receive().filter(event -> isMatchingEvent(event, messageValue))
            .take(NUMBER_OF_EVENTS).subscribe(event -> countDownLatch.countDown());

        try {
            // Act
            producer.send(events).block(TIMEOUT);
            countDownLatch.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
        } finally {
            subscription.dispose();
            dispose(consumer, producer);
        }

        // Assert
        Assert.assertEquals(0, countDownLatch.getCount());
    }

    /**
     * Test for receiving messages start at enqueued time or after the enqueued time.
     */
    @Test
    public void receiveMessageFromEnqueuedTime() {
        // Arrange
        final EventData[] events = EVENTS_PUSHED.get();
        final EventPosition position = EventPosition.fromEnqueuedTime(MESSAGES_PUSHED_INSTANT.get());
        final EventData expectedEvent = events[0];
        final EventHubConsumer consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, position);

        // Act & Assert
        try {
            StepVerifier.create(consumer.receive()
                .take(1))
                .assertNext(event -> {
                    Assert.assertEquals(expectedEvent.enqueuedTime(), event.enqueuedTime());
                    Assert.assertEquals(expectedEvent.sequenceNumber(), event.sequenceNumber());
                    Assert.assertEquals(expectedEvent.offset(), event.offset());
                }).verifyComplete();
        } finally {
            dispose(consumer);
        }
    }

    /**
     * Test for receiving messages with a previously received message.
     */
    @Ignore("Investigate. We cannot use the enqueuedTime from an existing event. If we set Instant we created, like Instant.now() it works.")
    @Test
    public void receiveMessageFromEnqueuedTimeReceivedMessage() {
        // Arrange
        final EventData[] events = EVENTS_PUSHED.get();
        final EventData secondEvent = events[1];
        final EventPosition position = EventPosition.fromEnqueuedTime(secondEvent.enqueuedTime());
        final EventData expectedEvent = events[2];
        final EventHubConsumer consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, position);

        // Act & Assert
        try {
            StepVerifier.create(consumer.receive()
                .take(1))
                .assertNext(event -> {
                    Assert.assertEquals(expectedEvent.enqueuedTime(), event.enqueuedTime());
                    Assert.assertEquals(expectedEvent.sequenceNumber(), event.sequenceNumber());
                    Assert.assertEquals(expectedEvent.offset(), event.offset());
                }).verifyComplete();
        } finally {
            dispose(consumer);
        }
    }


    /**
     * Tests that we can get an event using the inclusive offset.
     */
    @Test
    public void receiveMessageFromOffsetInclusive() {
        // Arrange
        final EventData[] events = EVENTS_PUSHED.get();
        final EventData expectedEvent = events[4];
        final EventPosition position = EventPosition.fromOffset(expectedEvent.offset());
        final EventHubConsumer consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, position);

        // Act & Assert
        try {
            StepVerifier.create(consumer.receive()
                .filter(event -> isMatchingEvent(event, MESSAGE_TRACKING_VALUE))
                .take(1))
                .assertNext(event -> {
                    Assert.assertEquals(expectedEvent.enqueuedTime(), event.enqueuedTime());
                    Assert.assertEquals(expectedEvent.sequenceNumber(), event.sequenceNumber());
                    Assert.assertEquals(expectedEvent.offset(), event.offset());
                }).verifyComplete();
        } finally {
            dispose(consumer);
        }
    }

    /**
     * Tests that we can get an event using the non-inclusive offset.
     */
    @Test
    public void receiveMessageFromOffsetNonInclusive() {
        // Arrange
        final EventData[] events = EVENTS_PUSHED.get();
        final EventData expectedEvent = events[4];
        final EventPosition position = EventPosition.fromOffset(events[3].offset(), false);
        final EventHubConsumer consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, position);

        // Act & Assert
        try {
            StepVerifier.create(consumer.receive()
                .filter(event -> isMatchingEvent(event, MESSAGE_TRACKING_VALUE))
                .take(1))
                .assertNext(event -> {
                    Assert.assertEquals(expectedEvent.enqueuedTime(), event.enqueuedTime());
                    Assert.assertEquals(expectedEvent.sequenceNumber(), event.sequenceNumber());
                    Assert.assertEquals(expectedEvent.offset(), event.offset());
                }).verifyComplete();
        } finally {
            dispose(consumer);
        }
    }

    /**
     * Test for receiving sequence number with inclusive sequence number.
     */
    @Test
    public void receiveMessageFromSequenceNumberInclusive() {
        // Arrange
        final EventData[] events = EVENTS_PUSHED.get();
        final EventData expectedEvent = events[3];
        final EventPosition position = EventPosition.fromSequenceNumber(expectedEvent.sequenceNumber(), true);
        final EventHubConsumer consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, position);

        // Act & Assert
        try {
            StepVerifier.create(consumer.receive()
                .filter(event -> isMatchingEvent(event, MESSAGE_TRACKING_VALUE))
                .take(1))
                .assertNext(event -> {
                    Assert.assertEquals(expectedEvent.enqueuedTime(), event.enqueuedTime());
                    Assert.assertEquals(expectedEvent.sequenceNumber(), event.sequenceNumber());
                    Assert.assertEquals(expectedEvent.offset(), event.offset());
                }).verifyComplete();
        } finally {
            dispose(consumer);
        }
    }

    /**
     * Test for receiving sequence number with non-inclusive sequence number.
     */
    @Test
    public void receiveMessageFromSequenceNumberNonInclusive() {
        // Arrange
        final EventData[] events = EVENTS_PUSHED.get();
        final EventData expectedEvent = events[4];
        final EventPosition position = EventPosition.fromSequenceNumber(events[3].sequenceNumber());
        final EventHubConsumer consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, position);

        // Act & Assert
        try {
            StepVerifier.create(consumer.receive()
                .filter(event -> isMatchingEvent(event, MESSAGE_TRACKING_VALUE))
                .take(1))
                .assertNext(event -> {
                    Assert.assertEquals(expectedEvent.enqueuedTime(), event.enqueuedTime());
                    Assert.assertEquals(expectedEvent.sequenceNumber(), event.sequenceNumber());
                    Assert.assertEquals(expectedEvent.offset(), event.offset());
                }).verifyComplete();
        } finally {
            dispose(consumer);
        }
    }

    /**
     * When we run this test, we check if there have been events already pushed to the partition, if not, we push some
     * events there.
     */
    private void setupEventTestData(EventHubAsyncClient client) {
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
            producer.send(events).block(TIMEOUT);
        } finally {
            dispose(producer);
        }

        // Receiving back those events we sent so we have something to compare to.
        logger.info("Receiving the events we sent.");
        final EventHubConsumer consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID,
            EventPosition.fromEnqueuedTime(MESSAGES_PUSHED_INSTANT.get()));
        final List<EventData> receivedEvents;
        try {
            receivedEvents = consumer.receive()
                .filter(event -> isMatchingEvent(event, MESSAGE_TRACKING_VALUE))
                .take(NUMBER_OF_EVENTS).collectList().block(TIMEOUT);
        } finally {
            dispose(consumer);
        }

        Assert.assertNotNull(receivedEvents);
        Assert.assertEquals(NUMBER_OF_EVENTS, receivedEvents.size());

        EVENTS_PUSHED.set(receivedEvents.toArray(new EventData[0]));
    }
}
