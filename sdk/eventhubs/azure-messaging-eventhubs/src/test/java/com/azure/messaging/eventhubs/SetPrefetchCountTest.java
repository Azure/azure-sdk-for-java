// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;

/**
 * Verifies we can use various prefetch options with {@link EventHubConsumerAsyncClient}.
 */
@Disabled("Set prefetch tests do not work because they try to send very large number of events at once."
    + "https://github.com/Azure/azure-sdk-for-java/issues/9659")
class SetPrefetchCountTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "1";
    // Default number of events to fetch when creating the consumer.
    private static final int DEFAULT_PREFETCH_COUNT = 500;

    // Set a large number of events to send to the service.
    private static final int NUMBER_OF_EVENTS = DEFAULT_PREFETCH_COUNT * 3;

    // We use these values to keep track of the events we've pushed to the service and ensure the events we receive are
    // our own.
    private static final AtomicBoolean HAS_PUSHED_EVENTS = new AtomicBoolean();
    private static volatile IntegrationTestEventData testData = null;

    private EventHubConsumerAsyncClient consumer;
    private EventHubClientBuilder builder;

    SetPrefetchCountTest() {
        super(new ClientLogger(SetPrefetchCountTest.class));
    }

    @Override
    protected void beforeTest() {
        builder = createBuilder()
            .shareConnection()
            .prefetchCount(DEFAULT_PREFETCH_COUNT)
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME);

        if (!HAS_PUSHED_EVENTS.getAndSet(true)) {
            final SendOptions options = new SendOptions().setPartitionId(PARTITION_ID);

            try (EventHubProducerAsyncClient producer = createBuilder().buildAsyncProducerClient()) {
                testData = setupEventTestData(producer, NUMBER_OF_EVENTS, options);
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
        final int eventCount = NUMBER_OF_EVENTS;
        final EventPosition position = EventPosition.fromEnqueuedTime(
            testData.getEnqueuedTime().minus(Duration.ofMinutes(5)));

        consumer = builder.prefetchCount(2000).buildAsyncConsumerClient();

        // Act & Assert
        StepVerifier.create(consumer.receiveFromPartition(PARTITION_ID, position)
            .filter(x -> isMatchingEvent(x, testData.getMessageTrackingId()))
            .take(eventCount))
            .expectNextCount(eventCount)
            .verifyComplete();
    }

    /**
     * Test for small prefetch count on EventHubConsumer continues to get messages.
     */
    @Test
    public void setSmallPrefetchCount() {
        // Arrange
        final int eventCount = 30;
        final EventPosition position = EventPosition.fromEnqueuedTime(
            testData.getEnqueuedTime().minus(Duration.ofMinutes(5)));

        consumer = builder.prefetchCount(11).buildAsyncConsumerClient();

        // Act & Assert
        StepVerifier.create(consumer.receiveFromPartition(PARTITION_ID, position)
            .filter(x -> isMatchingEvent(x, testData.getMessageTrackingId()))
            .take(eventCount))
            .expectNextCount(eventCount)
            .verifyComplete();
    }
}
