// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.Retry;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ApiTestBase;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Verifies we can use various prefetch options with {@link EventHubConsumer}.
 */
@Ignore("This test creates too many resources. We need to fix this test.")
public class SetPrefetchCountTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    // Default number of events to fetch when creating the consumer.
    private static final int DEFAULT_PREFETCH_COUNT = 500;

    private EventHubAsyncClient client;
    private EventHubProducer producer;
    private EventHubConsumer consumer;

    @Rule
    public TestName testName = new TestName();

    public SetPrefetchCountTest() {
        super(new ClientLogger(SetPrefetchCountTest.class));
    }

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        skipIfNotRecordMode();

        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        client = new EventHubAsyncClient(getConnectionOptions(), getReactorProvider(), handlerProvider);
        producer = client.createProducer();
    }

    @Override
    protected void afterTest() {
        dispose(producer, consumer, client);
    }

    /**
     * Test that we can use a very large prefetch number with {@link EventHubConsumerOptions}
     */
    @Test
    public void setLargePrefetchCount() throws InterruptedException {
        // Arrange
        // Since we cannot test receiving very large prefetch like 10000 in a unit test, DefaultPrefetchCount * 3 was
        // chosen
        final int eventCount = DEFAULT_PREFETCH_COUNT * 3;
        final CountDownLatch countDownLatch = new CountDownLatch(eventCount);
        final EventHubConsumerOptions options = new EventHubConsumerOptions()
            .retry(Retry.getDefaultRetry())
            .prefetchCount(2000);

        consumer = client.createConsumer(EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID,
            EventPosition.latest(), options);

        final Disposable subscription = consumer.receive()
            .take(eventCount + 1).subscribe(event -> countDownLatch.countDown());

        // Act
        try {
            final Flux<EventData> events = Flux.range(0, eventCount).map(number -> new EventData("c".getBytes(UTF_8)));
            producer.send(events).block();

            countDownLatch.await(45, TimeUnit.SECONDS);

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
        final EventHubConsumerOptions options = new EventHubConsumerOptions().prefetchCount(11);

        consumer = client.createConsumer(EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID,
            EventPosition.latest(), options);

        final Disposable subscription = consumer.receive()
            .take(eventCount + 1).subscribe(event -> countDownLatch.countDown());

        try {
            // Act
            final Flux<EventData> events = Flux.range(0, eventCount)
                .map(number -> new EventData("testString".getBytes(UTF_8)));
            producer.send(events).block(TIMEOUT);

            countDownLatch.await(45, TimeUnit.SECONDS);

            // Assert
            Assert.assertEquals(0, countDownLatch.getCount());
        } finally {
            subscription.dispose();
        }
    }
}
