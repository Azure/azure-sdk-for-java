// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;
import static com.azure.messaging.eventhubs.TestUtils.getEvent;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
/**
 * Tests that {@link EventHubConsumerAsyncClient} can be created with various {@link EventPosition EventPositions}.
 */
@Tag(TestUtils.INTEGRATION)
@Execution(ExecutionMode.SAME_THREAD)
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
        final Map<String, IntegrationTestEventData> integrationTestData = getTestData();
        for (Map.Entry<String, IntegrationTestEventData> entry : integrationTestData.entrySet()) {
            testData = entry.getValue();

            logger.log(LogLevel.VERBOSE, () -> "Getting entry for: " + testData.getPartitionId());
            break;
        }

        logger.info("Receiving the events we sent.");
        final EventHubConsumerClient testConsumer = toClose(createBuilder()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .buildConsumerClient());
        numberOfEvents = testData.getEvents().size() - 1;

        final EventPosition startingPosition = EventPosition.fromSequenceNumber(
            testData.getPartitionProperties().getLastEnqueuedSequenceNumber());
        final List<EventData> received;
        try {
            final IterableStream<PartitionEvent> partitionEvents = testConsumer.receiveFromPartition(
                testData.getPartitionId(), numberOfEvents, startingPosition, TIMEOUT);

            Assertions.assertNotNull(partitionEvents, "'partitionEvents' should not be null.");

            received = partitionEvents.stream().map(PartitionEvent::getData).collect(Collectors.toList());
        } finally {
            dispose(testConsumer);
        }

        Assertions.assertNotNull(received);
        Assertions.assertEquals(numberOfEvents, received.size());

        receivedEvents = received.toArray(new EventData[0]);
        for (int i = 0; i < received.size(); i++) {
            logger.atInfo()
                .addKeyValue("index", i)
                .addKeyValue("sequenceNo", receivedEvents[i].getSequenceNumber())
                .addKeyValue("offset", receivedEvents[i].getOffset())
                .addKeyValue("enqueued", receivedEvents[i].getEnqueuedTime())
                .log("receivedEvents");
        }

        Assertions.assertNotNull(testData, "testData should not be null. Or we have set this up incorrectly.");

        consumer = toClose(createBuilder()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient());

        enqueuedTimeConsumer = toClose(createBuilder()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient());
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
        StepVerifier.create(consumer.receiveFromPartition(testData.getPartitionId(), EventPosition.latest())
                .filter(event -> isMatchingEvent(event, testData.getMessageId()))
                .take(Duration.ofSeconds(3)))
                .expectComplete()
                .verify(TIMEOUT);
    }

    /**
     * Test for receiving message from latest offset
     */
    @Test
    void receiveLatestMessages() throws InterruptedException {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final SendOptions options = new SendOptions().setPartitionId(testData.getPartitionId());
        final EventHubProducerClient producer = toClose(createBuilder().buildProducerClient());
        final List<EventData> events = TestUtils.getEvents(15, messageId);
        final CountDownLatch receivedFirst = new CountDownLatch(1);
        final CountDownLatch receivedAll = new CountDownLatch(numberOfEvents);
        toClose(consumer.receiveFromPartition(testData.getPartitionId(), EventPosition.latest())
            .doOnNext(e -> receivedFirst.countDown())
            .filter(event -> isMatchingEvent(event, messageId))
            .take(numberOfEvents)
            .subscribe(e -> receivedAll.countDown(), (ex) -> fail(ex)));

        // we don't know when receive link will open. Since we want latest events, whatever we sent
        // before link is open will not be received.
        // so we'll try sending one event per sec and wait until something is received.
        for (int i = 0; i < 20; i++) {
            producer.send(getEvent("probing", "probing" + i, i), options);
            logger.atInfo()
                .addKeyValue("index", i)
                .log("sent probing event");
            if (receivedFirst.await(1, TimeUnit.SECONDS)) {
                break;
            }
        }

        // we should have received at least 1 event at this point
        assertTrue(receivedFirst.getCount() <= 0);

        // now link is open and ready so we can start the test
        producer.send(events, options);
        logger.atInfo().log("sent events");

        assertTrue(receivedAll.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS));
    }

    /**
     * Test for receiving messages start at enqueued time or after the enqueued time.
     */
    @Test
    void receiveMessageFromEnqueuedTime() {
        // Arrange
        final EventData expectedEvent = receivedEvents[0];
        final Instant enqueuedTime = expectedEvent.getEnqueuedTime();

        logger.atInfo()
            .addKeyValue("partitionId", testData.getPartitionId())
            .addKeyValue("from", enqueuedTime)
            .log("Receiving events");

        final EventPosition position = EventPosition.fromEnqueuedTime(enqueuedTime.minusMillis(1));

        // Act & Assert
        StepVerifier.create(consumer.receiveFromPartition(testData.getPartitionId(), position)
            .map(PartitionEvent::getData)
            .take(1))
            .assertNext(event -> {
                logger.atInfo()
                    .addKeyValue("sequenceNo", event.getSequenceNumber())
                    .addKeyValue("offset", event.getOffset())
                    .addKeyValue("enqueued", event.getEnqueuedTime())
                    .log("actual");

                Assertions.assertEquals(expectedEvent.getEnqueuedTime(), event.getEnqueuedTime());
                Assertions.assertEquals(expectedEvent.getSequenceNumber(), event.getSequenceNumber());
                Assertions.assertEquals(expectedEvent.getOffset(), event.getOffset());
            })
            .expectComplete()
            .verify(TIMEOUT);
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
        StepVerifier.create(consumer.receiveFromPartition(testData.getPartitionId(), position)
            .map(PartitionEvent::getData)
            .take(1))
            .assertNext(event -> {
                Assertions.assertEquals(expectedEvent.getEnqueuedTime(), event.getEnqueuedTime());
                Assertions.assertEquals(expectedEvent.getSequenceNumber(), event.getSequenceNumber());
                Assertions.assertEquals(expectedEvent.getOffset(), event.getOffset());
            })
            .expectComplete()
            .verify(TIMEOUT);
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
        StepVerifier.create(consumer.receiveFromPartition(testData.getPartitionId(), position)
            .map(PartitionEvent::getData)
            .filter(event -> isMatchingEvent(event, testData.getMessageId()))
            .take(1))
            .assertNext(event -> {
                Assertions.assertEquals(expectedEvent.getEnqueuedTime(), event.getEnqueuedTime());
                Assertions.assertEquals(expectedEvent.getSequenceNumber(), event.getSequenceNumber());
                Assertions.assertEquals(expectedEvent.getOffset(), event.getOffset());
            })
            .expectComplete()
            .verify(TIMEOUT);
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
        StepVerifier.create(consumer.receiveFromPartition(testData.getPartitionId(), position)
            .map(PartitionEvent::getData)
            .filter(event -> isMatchingEvent(event, testData.getMessageId()))
            .take(1))
            .assertNext(event -> {
                Assertions.assertEquals(expectedEvent.getEnqueuedTime(), event.getEnqueuedTime());
                Assertions.assertEquals(expectedEvent.getSequenceNumber(), event.getSequenceNumber());
                Assertions.assertEquals(expectedEvent.getOffset(), event.getOffset());
            })
            .expectComplete()
            .verify(TIMEOUT);
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
        StepVerifier.create(consumer.receiveFromPartition(testData.getPartitionId(), position)
            .map(PartitionEvent::getData)
            .filter(event -> isMatchingEvent(event, testData.getMessageId()))
            .take(1))
            .assertNext(event -> {
                Assertions.assertEquals(expectedEvent.getEnqueuedTime(), event.getEnqueuedTime());
                Assertions.assertEquals(expectedEvent.getSequenceNumber(), event.getSequenceNumber());
                Assertions.assertEquals(expectedEvent.getOffset(), event.getOffset());
            })
            .expectComplete()
            .verify(TIMEOUT);
    }
}
