// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.Retry;
import com.azure.core.util.logging.ClientLogger;
import com.azure.eventhubs.implementation.ApiTestBase;
import com.azure.eventhubs.implementation.ReactorHandlerProvider;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SetPrefetchCountTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    // since we cannot test receiving very large prefetch like 10000 - in a unit test
    // defaultPrefetchCount * 3 was chosen
    private static final int EVENT_COUNT = EventHubConsumerOptions.DEFAULT_PREFETCH_COUNT * 3;
    private static final int MAX_RETRY_TO_DECLARE_RECEIVE_STUCK = 3;

    private final ClientLogger logger = new ClientLogger(SetPrefetchCountTest.class);

    private EventHubClient client;
    private EventHubProducer sender;
    private EventHubConsumer receiver;

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
    }

    @Override
    protected void afterTest() {
        logger.asInfo().log("[{}]: Performing test clean-up.", testName.getMethodName());

        if (client != null) {
            client.close();
        }

        if (sender != null) {
            try {
                sender.close();
            } catch (IOException e) {
                logger.asError().log("[{}]: Sender doesn't close properly.", testName.getMethodName(), e);
            }
        }

        if (receiver != null) {
            try {
                receiver.close();
            } catch (IOException e) {
                logger.asError().log("[{}]: Receiver doesn't close properly.", testName.getMethodName(), e);
            }
        }
    }

    /**
     * Test for large prefetch count on EventHubConsumer
     */
    @Ignore
    @Test
    public void setLargePrefetchCount() {
        // Arrange
        receiver = client.createConsumer(getConsumerGroupName(), PARTITION_ID, EventPosition.latest(),
            new EventHubConsumerOptions().retry(Retry.getDefaultRetry()).prefetchCount(2000));

        int eventReceived = 0;
        int retryCount = 0;
        // Act
        while (eventReceived < EVENT_COUNT && retryCount < MAX_RETRY_TO_DECLARE_RECEIVE_STUCK) {
            // TODO: refactor it to trigger receiver to create connection
            final Flux<EventData> receivedData = receiver.receive();
            pushEventsToPartition(client, EVENT_COUNT);
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
        receiver = client.createConsumer(getConsumerGroupName(), PARTITION_ID, EventPosition.latest(),
            new EventHubConsumerOptions().prefetchCount(11));
        int eventReceived = 0;
        int retryCount = 0;
        // Act
        while (eventReceived < EVENT_COUNT && retryCount < MAX_RETRY_TO_DECLARE_RECEIVE_STUCK) {
            final Flux<EventData> receivedData = receiver.receive();
            pushEventsToPartition(client, EVENT_COUNT);
            if (receivedData == null || !receivedData.toIterable().iterator().hasNext()) {
                retryCount++;
            } else {
                eventReceived += receivedData.count().block();
            }
        }
        // Assert
        Assert.assertTrue(eventReceived >= EVENT_COUNT);
    }

    private Mono<Void> pushEventsToPartition(final EventHubClient client, final int numberOfEvents) {
        final Flux<EventData> events = Flux.range(0, numberOfEvents).map(number -> {
            final EventData data = new EventData("testString".getBytes(UTF_8));
            return data;
        });

        final EventHubProducerOptions senderOptions = new EventHubProducerOptions();
        sender = client.createProducer(senderOptions);
        return sender.send(events);
    }
}




