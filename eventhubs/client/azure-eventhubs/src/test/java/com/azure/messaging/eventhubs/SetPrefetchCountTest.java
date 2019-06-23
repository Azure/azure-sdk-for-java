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

import static java.nio.charset.StandardCharsets.UTF_8;

public class SetPrefetchCountTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    // since we cannot test receiving very large prefetch like 10000 - in a unit test
    // defaultPrefetchCount * 3 was chosen
    private static final int EVENT_COUNT = EventHubConsumerOptions.DEFAULT_PREFETCH_COUNT * 3;
    private static final int MAX_RETRY_TO_DECLARE_RECEIVE_STUCK = 3;

    private final ClientLogger logger = new ClientLogger(SetPrefetchCountTest.class);

    private EventHubClient client;
    private EventHubProducer producer;
    private EventHubConsumer consumer;

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        logger.asInfo().log("[{}]: Performing test set-up.", testName.getMethodName());

        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        client = new EventHubClient(getConnectionOptions(), getReactorProvider(), handlerProvider);
        producer = client.createProducer();
    }

    @Override
    protected void afterTest() {
        logger.asInfo().log("[{}]: Performing test clean-up.", testName.getMethodName());
        closeClient(client, producer, consumer, testName, logger);
    }

    /**
     * Test for large prefetch count on EventHubConsumer
     */
    @Ignore
    @Test
    public void setLargePrefetchCount() {
        // Arrange
        consumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.latest(),
            new EventHubConsumerOptions().retry(Retry.getDefaultRetry()).prefetchCount(2000));

        int eventReceived = 0;
        int retryCount = 0;

        // Act
        while (eventReceived < EVENT_COUNT && retryCount < MAX_RETRY_TO_DECLARE_RECEIVE_STUCK) {
            final Flux<EventData> events = Flux.range(0, EVENT_COUNT).map(number -> new EventData("testString".getBytes(UTF_8)));
            producer.send(events);
            // TODO: refactor it to trigger consumer to create connection
            final Flux<EventData> receivedData = consumer.receive();
            if (receivedData == null || !receivedData.toIterable().iterator().hasNext()) {
                retryCount++;
            } else {
                eventReceived += receivedData.count().block();
            }
        }

        // Assert
        Assert.assertTrue(eventReceived >= EVENT_COUNT);
    }

    /**
     * Test for small prefetch count on EventHubConsumer
     */
    @Ignore
    @Test
    public void setSmallPrefetchCount() {
        // Arrange
        consumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.latest(),
            new EventHubConsumerOptions().prefetchCount(11));
        int eventReceived = 0;
        int retryCount = 0;

        // Act
        while (eventReceived < EVENT_COUNT && retryCount < MAX_RETRY_TO_DECLARE_RECEIVE_STUCK) {
            final Flux<EventData> events = Flux.range(0, EVENT_COUNT).map(number -> new EventData("testString".getBytes(UTF_8)));
            producer.send(events);
            // TODO: refactor it to trigger consumer to create connection
            final Flux<EventData> receivedData = consumer.receive();
            if (receivedData == null || !receivedData.toIterable().iterator().hasNext()) {
                retryCount++;
            } else {
                eventReceived += receivedData.count().block();
            }
        }

        // Assert
        Assert.assertTrue(eventReceived >= EVENT_COUNT);
    }
}
