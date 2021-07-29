// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;

/**
 * Tests scenarios on {@link EventHubAsyncClient}.
 */
@Tag(TestUtils.INTEGRATION)
class EventHubAsyncClientIntegrationTest extends IntegrationTestBase {
    private static final int NUMBER_OF_EVENTS = 5;
    private static final String PARTITION_ID = "1";
    private IntegrationTestEventData testEventData;

    EventHubAsyncClientIntegrationTest() {
        super(new ClientLogger(EventHubAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        final Map<String, IntegrationTestEventData> testData = getTestData();
        testEventData = testData.get(PARTITION_ID);
        Assertions.assertNotNull(testEventData, PARTITION_ID + " should have been able to get data for partition.");
    }

    /**
     * Verifies that we can receive messages, and that the receiver continues to fetch messages when the prefetch queue
     * is exhausted.
     */
    @ParameterizedTest
    @EnumSource(value = AmqpTransportType.class)
    void receiveMessage(AmqpTransportType transportType) {
        // Arrange
        final EventHubConsumerAsyncClient consumer = createBuilder()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .transportType(transportType)
            .buildAsyncConsumerClient();

        final Instant lastEnqueued = testEventData.getPartitionProperties().getLastEnqueuedTime();
        final EventPosition startingPosition = EventPosition.fromEnqueuedTime(lastEnqueued);

        // Act & Assert
        try {
            StepVerifier.create(consumer.receiveFromPartition(PARTITION_ID, startingPosition)
                .take(NUMBER_OF_EVENTS))
                .expectNextCount(NUMBER_OF_EVENTS)
                .expectComplete()
                .verify();
        } finally {
            consumer.close();
        }
    }

    /**
     * Verifies that we can have multiple consumers listening to the same partition + consumer group at the same time.
     */
    @ParameterizedTest
    @EnumSource(value = AmqpTransportType.class)
    void parallelEventHubClients(AmqpTransportType transportType) throws InterruptedException {
        // Arrange
        final int numberOfClients = 3;
        final int numberOfEvents = testEventData.getEvents().size() - 2;
        final CountDownLatch countDownLatch = new CountDownLatch(numberOfClients);
        final EventHubClientBuilder builder = createBuilder()
            .transportType(transportType)
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME);

        final EventHubConsumerAsyncClient[] clients = new EventHubConsumerAsyncClient[numberOfClients];
        for (int i = 0; i < numberOfClients; i++) {
            clients[i] = builder.buildAsyncConsumerClient();
        }

        final long sequenceNumber = testEventData.getPartitionProperties().getLastEnqueuedSequenceNumber();
        final EventPosition position = EventPosition.fromSequenceNumber(sequenceNumber);

        try {

            //@formatter:off
            for (final EventHubConsumerAsyncClient consumer : clients) {
                consumer.receiveFromPartition(PARTITION_ID, position)
                    .filter(partitionEvent -> isMatchingEvent(partitionEvent.getData(), testEventData.getMessageId()))
                    .take(numberOfEvents)
                    .subscribe(partitionEvent -> {
                        EventData event = partitionEvent.getData();
                        logger.info("Event[{}] matched.", event.getSequenceNumber());
                    }, error -> Assertions.fail("An error should not have occurred:" + error.toString()),
                        () -> {
                            long count = countDownLatch.getCount();
                            logger.info("Finished consuming events. Counting down: {}", count);
                            countDownLatch.countDown();
                        });
            }
            //@formatter:on

            // Assert
            // Wait for all the events we sent to be received by each of the consumers.
            Assertions.assertTrue(countDownLatch.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS));

            logger.info("Completed successfully.");
        } finally {
            logger.info("Disposing of subscriptions, consumers and clients.");
            dispose(clients);
        }
    }

    /**
     * Sending with credentials.
     */
    @Test
    void getPropertiesWithCredentials() {
        // Arrange

        // Act & Assert
        try (EventHubAsyncClient client = createBuilder(true)
            .buildAsyncClient()) {
            StepVerifier.create(client.getProperties())
                .assertNext(properties -> {
                    Assertions.assertEquals(getEventHubName(), properties.getName());
                    Assertions.assertEquals(NUMBER_OF_PARTITIONS, properties.getPartitionIds().stream().count());
                })
                .verifyComplete();
        }
    }
}
