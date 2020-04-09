// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;
import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_PREFETCH_COUNT;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Tests scenarios on {@link EventHubAsyncClient}.
 */
class EventHubAsyncClientIntegrationTest extends IntegrationTestBase {
    private static final int NUMBER_OF_EVENTS = 5;
    private static final String PARTITION_ID = "1";
    private static final AtomicBoolean HAS_PUSHED_EVENTS = new AtomicBoolean();
    private static volatile IntegrationTestEventData testData = null;

    EventHubAsyncClientIntegrationTest() {
        super(new ClientLogger(EventHubAsyncClientIntegrationTest.class));
    }

    protected void beforeTest(AmqpTransportType transportType) {
        if (HAS_PUSHED_EVENTS.getAndSet(true)) {
            logger.info("Already pushed events to partition. Skipping.");
        } else {
            logger.info("Pushing... events to partition.");

            try (EventHubProducerAsyncClient testClient = createBuilder()
                .transportType(transportType)
                .buildAsyncProducerClient()) {
                final SendOptions options = new SendOptions().setPartitionId(PARTITION_ID);
                testData = setupEventTestData(testClient, NUMBER_OF_EVENTS, options);
                logger.warning("Pushed events to partition.");
            }
        }
    }

    /**
     * Verifies that we can receive messages, and that the receiver continues to fetch messages when the prefetch queue
     * is exhausted.
     */
    @ParameterizedTest
    @EnumSource(value = AmqpTransportType.class)
    void receiveMessage(AmqpTransportType transportType) {
        beforeTest(transportType);

        // Arrange
        final EventHubConsumerAsyncClient consumer = createBuilder()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .prefetchCount(DEFAULT_PREFETCH_COUNT)
            .transportType(transportType)
            .buildAsyncConsumerClient();

        final EventPosition startingPosition = EventPosition.fromEnqueuedTime(
            testData.getEnqueuedTime().minus(Duration.ofMinutes(10)));

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
    @Disabled("Works part of the time: https://github.com/Azure/azure-sdk-for-java/issues/9659")
    void parallelEventHubClients(AmqpTransportType transportType) throws InterruptedException {
        beforeTest(transportType);

        // Arrange
        final int numberOfClients = 4;
        final int numberOfEvents = 10;
        final String messageTrackingId = "message-tracking-id";
        final String messageTrackingValue = UUID.randomUUID().toString();

        final List<EventData> events = IntStream.range(0, numberOfEvents)
            .mapToObj(number -> {
                final EventData eventData = new EventData("testString".getBytes(UTF_8));
                eventData.getProperties().put(messageTrackingId, messageTrackingValue);
                return eventData;
            }).collect(Collectors.toList());

        final CountDownLatch countDownLatch = new CountDownLatch(numberOfClients);
        final EventHubClientBuilder builder = createBuilder()
            .transportType(transportType)
            .shareConnection()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .prefetchCount(DEFAULT_PREFETCH_COUNT);

        final EventHubProducerClient producer = builder.buildProducerClient();
        final EventHubConsumerAsyncClient[] clients = new EventHubConsumerAsyncClient[numberOfClients];
        for (int i = 0; i < numberOfClients; i++) {
            clients[i] = builder.buildAsyncConsumerClient();
        }

        final SendOptions sendOptions = new SendOptions().setPartitionId(PARTITION_ID);
        final EventPosition position = EventPosition.fromEnqueuedTime(Instant.now().minus(Duration.ofMinutes(10)));

        try {

            //@formatter:off
            for (final EventHubConsumerAsyncClient consumer : clients) {
                consumer.receiveFromPartition(PARTITION_ID, position)
                    .filter(partitionEvent -> TestUtils.isMatchingEvent(partitionEvent.getData(), messageTrackingValue))
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

            // Act
            producer.send(events, sendOptions);

            // Assert
            // Wait for all the events we sent to be received by each of the consumers.
            countDownLatch.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            Assertions.assertEquals(0, countDownLatch.getCount());

            logger.info("Completed successfully.");
        } finally {
            logger.info("Disposing of subscriptions, consumers and clients.");

            dispose(producer);
            dispose(clients);
        }
    }

    /**
     * Sending with credentials.
     */
    @Test
    void getPropertiesWithCredentials() {
        // Arrange
        final EventHubAsyncClient client = createBuilder(true)
            .buildAsyncClient();

        // Act & Assert
        try {
            StepVerifier.create(client.getProperties())
                .assertNext(properties -> {
                    Assertions.assertEquals(getEventHubName(), properties.getName());
                    Assertions.assertEquals(3, properties.getPartitionIds().stream().count());
                })
                .expectComplete()
                .verify(TIMEOUT);
        } finally {
            client.close();
        }
    }

    /**
     * Verifies that we can get partition properties.
     */
    @Test
    void getMultipleProperties() {
        // Arrange
        final EventHubAsyncClient theClient = createBuilder(true)
            .buildAsyncClient();

        try {
            for (int i = 0; i < 10; i++) {
                // Act & Assert
                StepVerifier.create(theClient.getProperties())
                    .assertNext(properties -> {
                        Assertions.assertEquals(getEventHubName(), properties.getName());
                        Assertions.assertEquals(3, properties.getPartitionIds().stream().count());
                    })
                    .verifyComplete();
            }
        } finally {
            theClient.close();
        }
    }
}
