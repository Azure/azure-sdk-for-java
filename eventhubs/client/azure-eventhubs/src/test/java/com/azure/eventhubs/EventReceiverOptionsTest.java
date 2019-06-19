// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.implementation.logging.ServiceLogger;
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
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public class EventReceiverOptionsTest extends ApiTestBase {
    private final ServiceLogger logger = new ServiceLogger(EventReceiverOptionsTest.class);

    private static final String PARTITION_ID = "0";
    private static final int DEFAULT_PREFETCH_COUNT = 500; // will delete it if EventReceiver make it public

    // since we cannot test receiving very large prefetch like 10000 - in a unit test
    // defaultPrefetchCount * 3 was chosen
    private static final int EVENT_COUNT = DEFAULT_PREFETCH_COUNT * 3;
    private static final int MAX_RETRY_TO_DECLARE_RECEIVE_STUCK = 3;

    private EventHubClient client;
    private EventSender sender;
    private EventReceiver receiver;
    private EventSenderOptions senderOptions;

    private ReactorHandlerProvider handlerProvider;

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        logger.asInfo().log("[{}]: Performing test set-up.", testName.getMethodName());

        handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        client = new EventHubClient(getConnectionOptions(), getReactorProvider(), handlerProvider);
        senderOptions = new EventSenderOptions().partitionId(PARTITION_ID);
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
                logger.asError().log("[{}]: Sender doesn't close properly", testName.getMethodName());
            }
        }

        if (receiver != null) {
            try {
                receiver.close();
            } catch (IOException e) {
                logger.asError().log("[{}]: Receiver doesn't close properly", testName.getMethodName());
            }
        }
    }

    /**
     * Verifies we set the correct defaults.
     */
    @Test
    public void defaults() {
        // Act
        final EventReceiverOptions options = new EventReceiverOptions();

        // Assert
        Assert.assertEquals(EventReceiverOptions.DEFAULT_CONSUMER_GROUP_NAME, options.consumerGroup());
        Assert.assertEquals(EventReceiverOptions.DEFAULT_PREFETCH_COUNT, options.prefetchCount());
        Assert.assertFalse(options.keepPartitionInformationUpdated());
    }

    @Test
    public void invalidIdentifier() {
        // Arrange
        final int length = EventReceiverOptions.MAXIMUM_IDENTIFIER_LENGTH + 1;
        final String longIdentifier = new String(new char[length]).replace("\0", "f");
        final String identifier = "An Identifier";
        final EventReceiverOptions options = new EventReceiverOptions()
            .identifier(identifier);

        // Act
        try {
            options.identifier(longIdentifier);
            Assert.fail("Setting this should have failed.");
        } catch (IllegalArgumentException e) {
            // This is what we expect.
        }

        // Assert
        Assert.assertEquals(identifier, options.identifier());
    }

    @Test
    public void invalidPrefetchMinimum() {
        // Arrange
        final int prefetch = 235;
        final int invalid = EventReceiverOptions.MINIMUM_PREFETCH_COUNT - 1;
        final EventReceiverOptions options = new EventReceiverOptions()
            .prefetchCount(prefetch);

        // Act
        try {
            options.prefetchCount(invalid);
            Assert.fail("Setting this should have failed.");
        } catch (IllegalArgumentException e) {
            // This is what we expect.
        }

        // Assert
        Assert.assertEquals(prefetch, options.prefetchCount());
    }

    @Test
    public void invalidPrefetchMaximum() {
        // Arrange
        final int prefetch = 235;
        final int invalid = EventReceiverOptions.MAXIMUM_PREFETCH_COUNT + 1;
        final EventReceiverOptions options = new EventReceiverOptions()
            .prefetchCount(prefetch);

        // Act
        try {
            options.prefetchCount(invalid);
            Assert.fail("Setting this should have failed.");
        } catch (IllegalArgumentException e) {
            // This is what we expect.
        }

        // Assert
        Assert.assertEquals(prefetch, options.prefetchCount());
    }

    @Test
    public void invalidReceiverPriority() {
        // Arrange
        final long priority = 14;
        final long invalidPriority = -1;
        final EventReceiverOptions options = new EventReceiverOptions()
            .exclusiveReceiverPriority(priority);

        // Act
        try {
            options.exclusiveReceiverPriority(invalidPriority);
            Assert.fail("Setting this should have failed.");
        } catch (IllegalArgumentException e) {
            // This is what we expect.
        }

        // Assert
        final Optional<Long> setPriority = options.exclusiveReceiverPriority();
        Assert.assertTrue(setPriority.isPresent());
        Assert.assertEquals(Long.valueOf(priority), setPriority.get());
    }

    /**
     * Test for large prefetch count on EventReceiver
     */
    @Ignore
    @Test
    public void setLargePrefetchCount() {
        receiver = client.createReceiver(PARTITION_ID, EventPosition.latest(),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName())
                .prefetchCount(2000));

        int eventReceived = 0;
        int retryCount = 0;
        while (eventReceived < EVENT_COUNT && retryCount < MAX_RETRY_TO_DECLARE_RECEIVE_STUCK) {
            final Flux<EventData> receivedData = receiver.receive();
            // com.azure.core.amqp.exception.AmqpException: The messaging entity 'sb://test-event-hub.servicebus.windows.net/eventhub1/ConsumerGroups/$Default/Partitions/0' could not be found.
            pushEventsToPartition(client, senderOptions, EVENT_COUNT);
            if (receivedData == null || !receivedData.toIterable().iterator().hasNext()) {
                retryCount++;
            } else {
                eventReceived += receivedData.count().block();
            }
        }
        Assert.assertTrue(eventReceived >= EVENT_COUNT);
    }

    /**
     * Test for small prefetch count on EventReceiver
     */
    @Ignore
    @Test
    public void setSmallPrefetchCount() {
        receiver = client.createReceiver(PARTITION_ID, EventPosition.latest(),
            new EventReceiverOptions().consumerGroup(getConsumerGroupName())
                .prefetchCount(11));

        int eventReceived = 0;
        int retryCount = 0;
        while (eventReceived < EVENT_COUNT && retryCount < MAX_RETRY_TO_DECLARE_RECEIVE_STUCK) {
            final Flux<EventData> receivedData = receiver.receive();
            pushEventsToPartition(client, senderOptions, EVENT_COUNT);
            if (receivedData == null || !receivedData.toIterable().iterator().hasNext()) {
                retryCount++;
            } else {
                eventReceived += receivedData.count().block();
            }
        }
        Assert.assertTrue(eventReceived >= EVENT_COUNT);
    }

    private Mono<Void> pushEventsToPartition(final EventHubClient client, final EventSenderOptions senderOptions, final int noOfEvents) {
        final Flux<EventData> events = Flux.range(0, noOfEvents).map(number -> {
            final EventData data = new EventData("testString".getBytes(UTF_8));
            return data;
        });
        sender = client.createSender(senderOptions);
        return sender.send(events);
    }
}
