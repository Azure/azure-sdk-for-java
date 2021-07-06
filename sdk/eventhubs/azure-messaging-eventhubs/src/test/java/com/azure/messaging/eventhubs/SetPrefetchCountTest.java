// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;

/**
 * Verifies we can use various prefetch options with {@link EventHubConsumerAsyncClient}.
 */
@Tag(TestUtils.INTEGRATION)
class SetPrefetchCountTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "3";
    // Default number of events to fetch when creating the consumer.
    private static final int DEFAULT_PREFETCH_COUNT = 500;

    // Set a large number of events to send to the service.
    private static final int NUMBER_OF_EVENTS = DEFAULT_PREFETCH_COUNT * 3;

    // We use these values to keep track of the events we've pushed to the service and ensure the events we receive are
    // our own.
    private static final AtomicBoolean HAS_PUSHED_EVENTS = new AtomicBoolean();
    private static volatile IntegrationTestEventData testData = null;

    private EventHubConsumerAsyncClient consumer;

    SetPrefetchCountTest() {
        super(new ClientLogger(SetPrefetchCountTest.class));
    }

    @Override
    protected void beforeTest() {
        if (!HAS_PUSHED_EVENTS.getAndSet(true)) {
            final CreateBatchOptions options = new CreateBatchOptions().setPartitionId(PARTITION_ID);
            final String messageId = UUID.randomUUID().toString();

            final List<EventData> events = TestUtils.getEvents(NUMBER_OF_EVENTS, messageId);
            try (EventHubProducerClient producer = createBuilder().buildProducerClient()) {
                final PartitionProperties properties = producer.getPartitionProperties(PARTITION_ID);

                EventDataBatch batch = producer.createBatch(options);
                for (EventData event : events) {
                    if (batch.tryAdd(event)) {
                        continue;
                    }

                    producer.send(batch);
                    batch = producer.createBatch(options);
                }

                producer.send(batch);

                testData = new IntegrationTestEventData(PARTITION_ID, properties, messageId, events);
                Assertions.assertNotNull(testData);
            }
        }
    }

    @Override
    protected void afterTest() {
        dispose(consumer);
    }

    /**
     * Test that we can use a very large prefetch number.
     */
    @Test
    public void setLargePrefetchCount() {
        // Arrange
        // Since we cannot test receiving very large prefetch like 10000 in a unit test, DefaultPrefetchCount * 3 was
        // chosen
        final long sequenceNumber = testData.getPartitionProperties().getLastEnqueuedSequenceNumber();
        final EventPosition position = EventPosition.fromSequenceNumber(sequenceNumber);

        consumer = createBuilder().consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .prefetchCount(2000)
            .buildAsyncConsumerClient();

        // Act & Assert
        StepVerifier.create(consumer.receiveFromPartition(PARTITION_ID, position)
            .filter(x -> isMatchingEvent(x, testData.getMessageId())))
            .expectNextCount(NUMBER_OF_EVENTS)
            .thenCancel()
            .verify(Duration.ofMinutes(2));
    }

    /**
     * Test for small prefetch count on EventHubConsumer continues to get messages.
     */
    @Test
    public void setSmallPrefetchCount() {
        // Arrange
        final int eventCount = 30;
        final long sequenceNumber = testData.getPartitionProperties().getLastEnqueuedSequenceNumber();
        final EventPosition position = EventPosition.fromSequenceNumber(sequenceNumber);

        consumer = createBuilder()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .prefetchCount(11)
            .buildAsyncConsumerClient();

        // Act & Assert
        StepVerifier.create(consumer.receiveFromPartition(PARTITION_ID, position)
            .filter(x -> isMatchingEvent(x, testData.getMessageId()))
            .take(eventCount))
            .expectNextCount(eventCount)
            .verifyComplete();
    }
}
