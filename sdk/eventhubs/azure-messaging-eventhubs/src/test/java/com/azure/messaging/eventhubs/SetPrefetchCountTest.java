// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;

/**
 * Verifies we can use various prefetch options with {@link EventHubConsumerAsyncClient}.
 */
@Disabled("Set prefetch tests do not work because they try to send very large number of events at once.")
public class SetPrefetchCountTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "1";
    // Default number of events to fetch when creating the consumer.
    private static final int DEFAULT_PREFETCH_COUNT = 500;

    // Set a large number of events to send to the service.
    private static final int NUMBER_OF_EVENTS = DEFAULT_PREFETCH_COUNT * 3;

    // We use these values to keep track of the events we've pushed to the service and ensure the events we receive are
    // our own.
    private static final AtomicBoolean HAS_PUSHED_EVENTS = new AtomicBoolean();
    private static volatile IntegrationTestEventData testData = null;

    private EventHubAsyncClient client;
    private EventHubConsumerAsyncClient consumer;

    public SetPrefetchCountTest() {
        super(new ClientLogger(SetPrefetchCountTest.class));
    }

    @Override
    protected void beforeTest() {
        client = createBuilder().buildAsyncClient();

        if (!HAS_PUSHED_EVENTS.getAndSet(true)) {
            final SendOptions options = new SendOptions().setPartitionId(PARTITION_ID);

            final EventHubProducerAsyncClient producer = client.createProducer();
            testData = setupEventTestData(producer, NUMBER_OF_EVENTS, options);
        }
    }

    @Override
    protected void afterTest() {
        dispose(consumer, client);
    }

    /**
     * Test that we can use a very large prefetch number.
     */
    @Test
    public void setLargePrefetchCount() throws InterruptedException {
        // Arrange
        // Since we cannot test receiving very large prefetch like 10000 in a unit test, DefaultPrefetchCount * 3 was
        // chosen
        final int eventCount = NUMBER_OF_EVENTS;
        final CountDownLatch countDownLatch = new CountDownLatch(eventCount);

        consumer = client.createConsumer(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME, 2000);

        final Disposable subscription = consumer.receiveFromPartition(PARTITION_ID, EventPosition.fromEnqueuedTime(testData.getEnqueuedTime()))
            .filter(x -> isMatchingEvent(x, testData.getMessageTrackingId()))
            .take(eventCount).subscribe(event -> countDownLatch.countDown());

        // Act
        try {
            countDownLatch.await(1, TimeUnit.MINUTES);

            // Assert
            Assertions.assertEquals(0, countDownLatch.getCount());
        } finally {
            subscription.dispose();
        }
    }

    /**
     * Test for small prefetch count on EventHubConsumer continues to get messages.
     */
    @Test
    public void setSmallPrefetchCount() throws InterruptedException {
        // Arrange
        final int eventCount = 30;
        final CountDownLatch countDownLatch = new CountDownLatch(eventCount);

        consumer = client.createConsumer(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME, 11);

        final Disposable subscription = consumer.receiveFromPartition(PARTITION_ID, EventPosition.fromEnqueuedTime(testData.getEnqueuedTime()))
            .filter(x -> isMatchingEvent(x, testData.getMessageTrackingId()))
            .take(eventCount).subscribe(event -> countDownLatch.countDown());

        try {
            // Act
            countDownLatch.await(45, TimeUnit.SECONDS);

            // Assert
            Assertions.assertEquals(0, countDownLatch.getCount());
        } finally {
            subscription.dispose();
        }
    }
}
