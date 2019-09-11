// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.IntegrationTestBase;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventHubProducerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;

/**
 * Verifies we can use various prefetch options with {@link EventHubAsyncConsumer}.
 */
@Ignore("Set prefetch tests do not work because they try to send very large number of events at once.")
public class SetPrefetchCountTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "1";
    // Default number of events to fetch when creating the consumer.
    private static final int DEFAULT_PREFETCH_COUNT = 500;

    // Set a large number of events to send to the service.
    private static final int NUMBER_OF_EVENTS = DEFAULT_PREFETCH_COUNT * 3;

    // We use these values to keep track of the events we've pushed to the service and ensure the events we receive are
    // our own.
    private static final AtomicBoolean HAS_PUSHED_EVENTS = new AtomicBoolean();
    private static final String MESSAGE_TRACKING_VALUE = UUID.randomUUID().toString();
    private static final AtomicReference<Instant> MESSAGES_PUSHED_INSTANT = new AtomicReference<>();

    private EventHubAsyncClient client;
    private EventHubAsyncConsumer consumer;

    @Rule
    public TestName testName = new TestName();

    public SetPrefetchCountTest() {
        super(new ClientLogger(SetPrefetchCountTest.class));
    }

    @Override
    protected String getTestName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        final TracerProvider tracerProvider = new TracerProvider(Collections.emptyList());
        client = new EventHubAsyncClient(getConnectionOptions(), getReactorProvider(), handlerProvider, tracerProvider);

        setupEventTestData(client);
    }

    @Override
    protected void afterTest() {
        dispose(consumer, client);
    }

    /**
     * Test that we can use a very large prefetch number with {@link EventHubConsumerOptions}
     */
    @Test
    public void setLargePrefetchCount() throws InterruptedException {
        // Arrange
        // Since we cannot test receiving very large prefetch like 10000 in a unit test, DefaultPrefetchCount * 3 was
        // chosen
        final int eventCount = NUMBER_OF_EVENTS;
        final CountDownLatch countDownLatch = new CountDownLatch(eventCount);
        final EventHubConsumerOptions options = new EventHubConsumerOptions()
            .setRetry(RETRY_OPTIONS)
            .setPrefetchCount(2000);

        consumer = client.createConsumer(EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID,
            EventPosition.fromEnqueuedTime(MESSAGES_PUSHED_INSTANT.get()), options);

        final Disposable subscription = consumer.receive()
            .filter(x -> isMatchingEvent(x, MESSAGE_TRACKING_VALUE))
            .take(eventCount).subscribe(event -> countDownLatch.countDown());

        // Act
        try {
            countDownLatch.await(1, TimeUnit.MINUTES);

            // Assert
            Assert.assertEquals(0, countDownLatch.getCount());
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
        final EventHubConsumerOptions options = new EventHubConsumerOptions().setPrefetchCount(11);

        consumer = client.createConsumer(EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID,
            EventPosition.fromEnqueuedTime(MESSAGES_PUSHED_INSTANT.get()), options);

        final Disposable subscription = consumer.receive().filter(x -> isMatchingEvent(x, MESSAGE_TRACKING_VALUE))
            .take(eventCount).subscribe(event -> countDownLatch.countDown());

        try {
            // Act
            countDownLatch.await(45, TimeUnit.SECONDS);

            // Assert
            Assert.assertEquals(0, countDownLatch.getCount());
        } finally {
            subscription.dispose();
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

        final EventHubProducerOptions producerOptions = new EventHubProducerOptions()
            .setPartitionId(PARTITION_ID);
        final EventHubAsyncProducer producer = client.createProducer(producerOptions);
        final Flux<EventData> events = TestUtils.getEvents(NUMBER_OF_EVENTS, MESSAGE_TRACKING_VALUE);

        try {
            MESSAGES_PUSHED_INSTANT.set(Instant.now());
            producer.send(events).block(RETRY_OPTIONS.getTryTimeout());
        } finally {
            dispose(producer);
        }
    }
}
