// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.eventhubs.implementation.ApiTestBase;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Flux;

import java.util.Optional;

public class EventReceiverOptionsTest extends ApiTestBase {

    private static final String PARTITION_ID = "0";
    private static final int DEFAULT_PREFETCH_COUNT = 500; // will delete it if EventReceiver make it public

    // since we cannot test receiving very large prefetch like 10000 - in a unit test
    // defaultPrefetchCount * 3 was chosen
    private static final int EVENT_COUNT = DEFAULT_PREFETCH_COUNT * 3;
    private static final int MAX_RETRY_TO_DECLARE_RECEIVE_STUCK = 3;

    private static EventHubClient ehClient;
    private static EventReceiver receiver;


    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @BeforeClass
    public static void initialize() {
        ehClient = ApiTestBase.getEventHubClientBuilder().build();
    }

    @AfterClass
    public static void cleanup() {
        if (ehClient != null) {
            ehClient.close();
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

    @Test
    public void setLargePrefetchCount() {
        receiver = ehClient.createReceiver(PARTITION_ID, EventPosition.latest(),
            new EventReceiverOptions()
                .consumerGroup(ApiTestBase.getConsumerGroupName())
                .prefetchCount(2000));
        // TODO: receive time out missing?

        int eventReceived = 0;
        int retryCount = 0;
        while (eventReceived < EVENT_COUNT && retryCount < MAX_RETRY_TO_DECLARE_RECEIVE_STUCK) {
            final Flux<EventData> receivedData = receiver.receive();
            ApiTestBase.pushEventsToPartition(ehClient, PARTITION_ID, EVENT_COUNT);
            if (receivedData == null || !receivedData.toIterable().iterator().hasNext()) {
                retryCount++;
            } else {
                eventReceived += receivedData.count().block();
            }
        }
        Assert.assertTrue(eventReceived >= EVENT_COUNT);
    }

    @Test
    public void setSmallFrefetchCount() {
        receiver = ehClient.createReceiver(PARTITION_ID, EventPosition.latest(),
            new EventReceiverOptions()
                .consumerGroup(ApiTestBase.getConsumerGroupName())
                .prefetchCount(11));
        // TODO: receive time out missing?

        int eventReceived = 0;
        int retryCount = 0;
        while (eventReceived < EVENT_COUNT && retryCount < MAX_RETRY_TO_DECLARE_RECEIVE_STUCK) {
            final Flux<EventData> receivedData = receiver.receive();
            ApiTestBase.pushEventsToPartition(ehClient, PARTITION_ID, EVENT_COUNT);
            if (receivedData == null || !receivedData.toIterable().iterator().hasNext()) {
                retryCount++;
            } else {
                eventReceived += receivedData.count().block();
            }
        }
        Assert.assertTrue(eventReceived >= EVENT_COUNT);
    }
}
