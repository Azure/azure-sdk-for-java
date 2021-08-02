// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Tests that {@link EventHubConsumerAsyncClient} can be created with various {@link EventPosition EventPositions}.
 */
@Tag(TestUtils.INTEGRATION)
class EventPositionIntegrationTest extends IntegrationTestBase {
    // We use these values to keep track of the events we've pushed to the service and ensure the events we receive are
    // our own.
    private static final AtomicBoolean HAS_PUSHED_EVENTS = new AtomicBoolean();
    private static EventData[] receivedEvents;
    private static IntegrationTestEventData testData;
    private static int numberOfEvents;

    private EventHubConsumerAsyncClient consumer;
    private EventHubConsumerAsyncClient enqueuedTimeConsumer;

    EventPositionIntegrationTest() {
        super(new ClientLogger(EventPositionIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        if (!HAS_PUSHED_EVENTS.getAndSet(true)) {
            final Map<String, IntegrationTestEventData> integrationTestData = getTestData();
            for (Map.Entry<String, IntegrationTestEventData> entry : integrationTestData.entrySet()) {
                testData = entry.getValue();

                System.out.printf("Getting entry for: %s%n", testData.getPartitionId());
                break;
            }

            logger.info("Receiving the events we sent.");
            final EventHubConsumerClient consumer = createBuilder()
                .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
                .buildConsumerClient();

            numberOfEvents = testData.getEvents().size() - 1;

            final EventPosition startingPosition = EventPosition.fromSequenceNumber(
                testData.getPartitionProperties().getLastEnqueuedSequenceNumber());
            final List<EventData> received;
            try {
                final IterableStream<PartitionEvent> partitionEvents = consumer.receiveFromPartition(
                    testData.getPartitionId(), numberOfEvents, startingPosition, TIMEOUT);

                Assertions.assertNotNull(partitionEvents, "'partitionEvents' should not be null.");

                received = partitionEvents.stream().map(PartitionEvent::getData).collect(Collectors.toList());
            } finally {
                dispose(consumer);
            }

            Assertions.assertNotNull(received);
            Assertions.assertEquals(numberOfEvents, received.size());

            receivedEvents = received.toArray(new EventData[0]);
        }

        Assertions.assertNotNull(testData, "testData should not be null. Or we have set this up incorrectly.");

        consumer = createBuilder()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient();
        enqueuedTimeConsumer = createBuilder()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient();
    }

    @Override
    protected void afterTest() {
        dispose(consumer);
    }

    /**
     * Test that we receive the same messages using {@link EventPosition#earliest()} and {@link
     * EventPosition#fromEnqueuedTime(Instant)} where the enqueued time is {@link Instant#EPOCH}.
     */
    @Test
    void receiveEarliestMessages() {
        // Arrange
        final List<EventData> earliestEvents;
        final List<EventData> enqueuedEvents;
        try {
            // Act
            earliestEvents = consumer.receiveFromPartition(testData.getPartitionId(), EventPosition.earliest())
                .take(numberOfEvents)
                .map(PartitionEvent::getData)
                .collectList()
                .block(TIMEOUT);
            enqueuedEvents = enqueuedTimeConsumer.receiveFromPartition(testData.getPartitionId(), EventPosition.fromEnqueuedTime(Instant.EPOCH))
                .take(numberOfEvents)
                .map(PartitionEvent::getData)
                .collectList()
                .block(TIMEOUT);
        } finally {
            dispose(consumer, enqueuedTimeConsumer);
        }

        // Assert
        Assertions.assertNotNull(earliestEvents);
        Assertions.assertNotNull(enqueuedEvents);

        Assertions.assertEquals(numberOfEvents, earliestEvents.size());
        Assertions.assertEquals(numberOfEvents, enqueuedEvents.size());

        // EventData implements Comparable, so we can sort these and ensure that the events received are the same.
        earliestEvents.sort(Comparator.comparing(EventData::getSequenceNumber));
        enqueuedEvents.sort(Comparator.comparing(EventData::getSequenceNumber));

        for (int i = 0; i < numberOfEvents; i++) {
            final EventData event = earliestEvents.get(i);
            final EventData event2 = enqueuedEvents.get(i);
            final String eventBody = new String(event.getBody(), UTF_8);
            final String event2Body = new String(event2.getBody(), UTF_8);

            Assertions.assertEquals(event.getSequenceNumber(), event2.getSequenceNumber());
            Assertions.assertEquals(event.getOffset(), event2.getOffset());
            Assertions.assertEquals(eventBody, event2Body);
        }
    }

    /**
     * Verify that if no new items are added at the end of the stream, we don't get any events.
     */
    @Test
    void receiveLatestMessagesNoneAdded() {
        // Act & Assert
        try {
            StepVerifier.create(consumer.receiveFromPartition(testData.getPartitionId(), EventPosition.latest())
                .filter(event -> isMatchingEvent(event, testData.getMessageId()))
                .take(Duration.ofSeconds(3)))
                .expectComplete()
                .verify(TIMEOUT);
        } finally {
            dispose(consumer);
        }
    }

    /**
     * Test for receiving message from latest offset
     */
    @Test
    void receiveLatestMessages() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final SendOptions options = new SendOptions().setPartitionId(testData.getPartitionId());
        final EventHubProducerClient producer = createBuilder()
            .buildProducerClient();
        final List<EventData> events = TestUtils.getEvents(15, messageId);

        try {
            StepVerifier.create(consumer.receiveFromPartition(testData.getPartitionId(), EventPosition.latest())
                .filter(event -> isMatchingEvent(event, messageId))
                .take(numberOfEvents))
                .then(() -> producer.send(events, options))
                .expectNextCount(numberOfEvents)
                .expectComplete()
                .verify(TIMEOUT);

            // Act
        } finally {
            dispose(producer);
        }
    }

    /**
     * Test for receiving messages start at enqueued time or after the enqueued time.
     */
    @Test
    void receiveMessageFromEnqueuedTime() {
        // Arrange
        final EventPosition position = EventPosition.fromEnqueuedTime(testData.getPartitionProperties().getLastEnqueuedTime());
        final EventData expectedEvent = receivedEvents[0];

        // Act & Assert
        try {
            StepVerifier.create(consumer.receiveFromPartition(testData.getPartitionId(), position)
                .map(PartitionEvent::getData)
                .take(1))
                .assertNext(event -> {
                    Assertions.assertEquals(expectedEvent.getEnqueuedTime(), event.getEnqueuedTime());
                    Assertions.assertEquals(expectedEvent.getSequenceNumber(), event.getSequenceNumber());
                    Assertions.assertEquals(expectedEvent.getOffset(), event.getOffset());
                }).verifyComplete();
        } finally {
            dispose(consumer);
        }
    }

    /**
     * Test for receiving messages with a previously received message.
     */
    @Disabled("Investigate. We cannot use the enqueuedTime from an existing event. If we set Instant we created, like Instant.now() it works.")
    @Test
    void receiveMessageFromEnqueuedTimeReceivedMessage() {
        // Arrange
        final EventData secondEvent = receivedEvents[1];
        final EventPosition position = EventPosition.fromEnqueuedTime(secondEvent.getEnqueuedTime());
        final EventData expectedEvent = receivedEvents[2];

        // Act & Assert
        try {
            StepVerifier.create(consumer.receiveFromPartition(testData.getPartitionId(), position)
                .map(PartitionEvent::getData)
                .take(1))
                .assertNext(event -> {
                    Assertions.assertEquals(expectedEvent.getEnqueuedTime(), event.getEnqueuedTime());
                    Assertions.assertEquals(expectedEvent.getSequenceNumber(), event.getSequenceNumber());
                    Assertions.assertEquals(expectedEvent.getOffset(), event.getOffset());
                }).verifyComplete();
        } finally {
            dispose(consumer);
        }
    }

    /**
     * Tests that we can get an event using the non-inclusive offset.
     */
    @Test
    void receiveMessageFromOffsetNonInclusive() {
        // Arrange
        final EventData expectedEvent = receivedEvents[4];

        // Choose the offset before it, so we get that event back.
        final EventPosition position = EventPosition.fromOffset(expectedEvent.getOffset() - 1);

        // Act & Assert
        try {
            StepVerifier.create(consumer.receiveFromPartition(testData.getPartitionId(), position)
                .map(PartitionEvent::getData)
                .filter(event -> isMatchingEvent(event, testData.getMessageId()))
                .take(1))
                .assertNext(event -> {
                    Assertions.assertEquals(expectedEvent.getEnqueuedTime(), event.getEnqueuedTime());
                    Assertions.assertEquals(expectedEvent.getSequenceNumber(), event.getSequenceNumber());
                    Assertions.assertEquals(expectedEvent.getOffset(), event.getOffset());
                }).verifyComplete();
        } finally {
            dispose(consumer);
        }
    }

    /**
     * Test for receiving sequence number with inclusive sequence number.
     */
    @Test
    void receiveMessageFromSequenceNumberInclusive() {
        // Arrange
        final EventData expectedEvent = receivedEvents[3];
        final EventPosition position = EventPosition.fromSequenceNumber(expectedEvent.getSequenceNumber(), true);

        // Act & Assert
        try {
            StepVerifier.create(consumer.receiveFromPartition(testData.getPartitionId(), position)
                .map(PartitionEvent::getData)
                .filter(event -> isMatchingEvent(event, testData.getMessageId()))
                .take(1))
                .assertNext(event -> {
                    Assertions.assertEquals(expectedEvent.getEnqueuedTime(), event.getEnqueuedTime());
                    Assertions.assertEquals(expectedEvent.getSequenceNumber(), event.getSequenceNumber());
                    Assertions.assertEquals(expectedEvent.getOffset(), event.getOffset());
                }).verifyComplete();
        } finally {
            dispose(consumer);
        }
    }

    /**
     * Test for receiving sequence number with non-inclusive sequence number.
     */
    @Test
    void receiveMessageFromSequenceNumberNonInclusive() {
        // Arrange
        final EventData expectedEvent = receivedEvents[4];
        final EventPosition position = EventPosition.fromSequenceNumber(receivedEvents[3].getSequenceNumber());

        // Act & Assert
        try {
            StepVerifier.create(consumer.receiveFromPartition(testData.getPartitionId(), position)
                .map(PartitionEvent::getData)
                .filter(event -> isMatchingEvent(event, testData.getMessageId()))
                .take(1))
                .assertNext(event -> {
                    Assertions.assertEquals(expectedEvent.getEnqueuedTime(), event.getEnqueuedTime());
                    Assertions.assertEquals(expectedEvent.getSequenceNumber(), event.getSequenceNumber());
                    Assertions.assertEquals(expectedEvent.getOffset(), event.getOffset());
                }).verifyComplete();
        } finally {
            dispose(consumer);
        }
    }
}
