// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.IntegrationTestBase;
import com.azure.messaging.eventhubs.implementation.IntegrationTestEventData;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.SendOptions;
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

import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;
import static com.azure.messaging.eventhubs.TestUtils.MESSAGE_TRACKING_ID;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Tests that {@link EventHubConsumerAsyncClient} can be created with various {@link EventPosition EventPositions}.
 */
public class EventPositionIntegrationTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "0";
    private static final int NUMBER_OF_EVENTS = 10;

    // We use these values to keep track of the events we've pushed to the service and ensure the events we receive are
    // our own.
    private static final AtomicBoolean HAS_PUSHED_EVENTS = new AtomicBoolean();
    private static final AtomicReference<EventData[]> EVENTS_PUSHED = new AtomicReference<>();
    private static volatile IntegrationTestEventData testData = null;

    private EventHubAsyncClient client;

    public EventPositionIntegrationTest() {
        super(new ClientLogger(EventPositionIntegrationTest.class));
    }

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String getTestName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        client = createBuilder().buildAsyncClient();

        if (!HAS_PUSHED_EVENTS.getAndSet(true)) {
            final SendOptions options = new SendOptions().setPartitionId(PARTITION_ID);
            testData = setupEventTestData(client, NUMBER_OF_EVENTS, options);

            // Receiving back those events we sent so we have something to compare to.
            logger.info("Receiving the events we sent.");
            final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME,
                EventPosition.fromEnqueuedTime(testData.getEnqueuedTime()));
            final List<EventData> receivedEvents;
            try {
                receivedEvents = consumer.receive(PARTITION_ID)
                    .filter(event -> isMatchingEvent(event, testData.getMessageTrackingId()))
                    .take(NUMBER_OF_EVENTS)
                    .map(PartitionEvent::getEventData)
                    .collectList().block(TIMEOUT);
            } finally {
                dispose(consumer);
            }

            Assert.assertNotNull(receivedEvents);
            Assert.assertEquals(NUMBER_OF_EVENTS, receivedEvents.size());

            EVENTS_PUSHED.set(receivedEvents.toArray(new EventData[0]));
        }
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
        final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, EventPosition.earliest());
        final EventHubConsumerAsyncClient enqueuedTimeConsumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, EventPosition.fromEnqueuedTime(Instant.EPOCH));

        final List<EventData> earliestEvents;
        final List<EventData> enqueuedEvents;
        try {
            // Act
            earliestEvents = consumer.receive(PARTITION_ID).take(NUMBER_OF_EVENTS).map(PartitionEvent::getEventData)
                .collectList().block(TIMEOUT);
            enqueuedEvents = enqueuedTimeConsumer.receive(PARTITION_ID).take(NUMBER_OF_EVENTS).map(PartitionEvent::getEventData)
                .collectList().block(TIMEOUT);
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
            final String eventBody = UTF_8.decode(event.getBody()).toString();
            final String event2Body = UTF_8.decode(event2.getBody()).toString();

            Assert.assertEquals(event.getSequenceNumber(), event2.getSequenceNumber());
            Assert.assertEquals(event.getOffset(), event2.getOffset());
            Assert.assertEquals(eventBody, event2Body);
        }
    }

    /**
     * Verify that if no new items are added at the end of the stream, we don't get any events.
     */
    @Test
    public void receiveLatestMessagesNoneAdded() {
        // Arrange
        final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, EventPosition.latest());

        // Act & Assert
        try {
            StepVerifier.create(consumer.receive(PARTITION_ID).filter(event -> isMatchingEvent(event, testData.getMessageTrackingId()))
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
        final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, EventPosition.latest());
        final SendOptions options = new SendOptions().setPartitionId(PARTITION_ID);
        final EventHubProducerAsyncClient producer = client.createProducer();
        final Flux<EventData> events = Flux.range(0, NUMBER_OF_EVENTS).map(number -> {
            final EventData eventData = new EventData(("Event " + number).getBytes(UTF_8));
            eventData.addProperty(MESSAGE_TRACKING_ID, messageValue);
            return eventData;
        });

        final CountDownLatch countDownLatch = new CountDownLatch(NUMBER_OF_EVENTS);
        final Disposable subscription = consumer.receive(PARTITION_ID).filter(event -> isMatchingEvent(event, messageValue))
            .take(NUMBER_OF_EVENTS).subscribe(event -> countDownLatch.countDown());

        try {
            // Act
            producer.send(events, options).block(TIMEOUT);
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
        final EventPosition position = EventPosition.fromEnqueuedTime(testData.getEnqueuedTime());
        final EventData expectedEvent = events[0];
        final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, position);

        // Act & Assert
        try {
            StepVerifier.create(consumer.receive(PARTITION_ID).map(x -> x.getEventData())
                .take(1))
                .assertNext(event -> {
                    Assert.assertEquals(expectedEvent.getEnqueuedTime(), event.getEnqueuedTime());
                    Assert.assertEquals(expectedEvent.getSequenceNumber(), event.getSequenceNumber());
                    Assert.assertEquals(expectedEvent.getOffset(), event.getOffset());
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
        final EventPosition position = EventPosition.fromEnqueuedTime(secondEvent.getEnqueuedTime());
        final EventData expectedEvent = events[2];
        final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, position);

        // Act & Assert
        try {
            StepVerifier.create(consumer.receive(PARTITION_ID).map(x -> x.getEventData())
                .take(1))
                .assertNext(event -> {
                    Assert.assertEquals(expectedEvent.getEnqueuedTime(), event.getEnqueuedTime());
                    Assert.assertEquals(expectedEvent.getSequenceNumber(), event.getSequenceNumber());
                    Assert.assertEquals(expectedEvent.getOffset(), event.getOffset());
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
        final EventPosition position = EventPosition.fromOffset(expectedEvent.getOffset());
        final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, position);

        // Act & Assert
        try {
            StepVerifier.create(consumer.receive(PARTITION_ID).map(PartitionEvent::getEventData)
                .filter(event -> isMatchingEvent(event, testData.getMessageTrackingId()))
                .take(1))
                .assertNext(event -> {
                    Assert.assertEquals(expectedEvent.getEnqueuedTime(), event.getEnqueuedTime());
                    Assert.assertEquals(expectedEvent.getSequenceNumber(), event.getSequenceNumber());
                    Assert.assertEquals(expectedEvent.getOffset(), event.getOffset());
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
        final EventPosition position = EventPosition.fromOffset(events[3].getOffset(), false);
        final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, position);

        // Act & Assert
        try {
            StepVerifier.create(consumer.receive(PARTITION_ID).map(PartitionEvent::getEventData)
                .filter(event -> isMatchingEvent(event, testData.getMessageTrackingId()))
                .take(1))
                .assertNext(event -> {
                    Assert.assertEquals(expectedEvent.getEnqueuedTime(), event.getEnqueuedTime());
                    Assert.assertEquals(expectedEvent.getSequenceNumber(), event.getSequenceNumber());
                    Assert.assertEquals(expectedEvent.getOffset(), event.getOffset());
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
        final EventPosition position = EventPosition.fromSequenceNumber(expectedEvent.getSequenceNumber(), true);
        final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, position);

        // Act & Assert
        try {
            StepVerifier.create(consumer.receive(PARTITION_ID).map(PartitionEvent::getEventData)
                .filter(event -> isMatchingEvent(event, testData.getMessageTrackingId()))
                .take(1))
                .assertNext(event -> {
                    Assert.assertEquals(expectedEvent.getEnqueuedTime(), event.getEnqueuedTime());
                    Assert.assertEquals(expectedEvent.getSequenceNumber(), event.getSequenceNumber());
                    Assert.assertEquals(expectedEvent.getOffset(), event.getOffset());
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
        final EventPosition position = EventPosition.fromSequenceNumber(events[3].getSequenceNumber());
        final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, position);

        // Act & Assert
        try {
            StepVerifier.create(consumer.receive(PARTITION_ID).map(PartitionEvent::getEventData)
                .filter(event -> isMatchingEvent(event, testData.getMessageTrackingId()))
                .take(1))
                .assertNext(event -> {
                    Assert.assertEquals(expectedEvent.getEnqueuedTime(), event.getEnqueuedTime());
                    Assert.assertEquals(expectedEvent.getSequenceNumber(), event.getSequenceNumber());
                    Assert.assertEquals(expectedEvent.getOffset(), event.getOffset());
                }).verifyComplete();
        } finally {
            dispose(consumer);
        }
    }
}
