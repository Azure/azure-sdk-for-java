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
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;
import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_PREFETCH_COUNT;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Tests scenarios on {@link EventHubAsyncClient}.
 */
public class EventHubAsyncClientIntegrationTest extends IntegrationTestBase {
    private static final int NUMBER_OF_EVENTS = 5;
    private static final String PARTITION_ID = "1";
    private static final AtomicBoolean HAS_PUSHED_EVENTS = new AtomicBoolean();
    private static volatile IntegrationTestEventData testData = null;

    private EventHubAsyncClient client;
    private AmqpTransportType transportType;

    public EventHubAsyncClientIntegrationTest() {
        super(new ClientLogger(EventHubAsyncClientIntegrationTest.class));
    }

    protected void beforeTest(AmqpTransportType transportType) {
        client = createBuilder()
            .transportType(transportType)
            .shareConnection()
            .buildAsyncClient();
        this.transportType = transportType;

        if (HAS_PUSHED_EVENTS.getAndSet(true)) {
            logger.warning("Already pushed events to partition. Skipping.");
        } else {
            logger.warning("Pushing... events to partition.");

            final SendOptions options = new SendOptions().setPartitionId(PARTITION_ID);
            testData = setupEventTestData(client.createProducer(), NUMBER_OF_EVENTS, options);
            logger.warning("Pushed events to partition.");
        }
    }

    @Override
    protected void afterTest() {
        dispose(client);
    }

    /**
     * Verifies that we can receive messages, and that the receiver continues to fetch messages when the prefetch queue
     * is exhausted.
     */
    @ParameterizedTest
    @EnumSource(value = AmqpTransportType.class)
    public void receiveMessage(AmqpTransportType transportType) {
        beforeTest(transportType);
        // Arrange
        final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, 2);
        final EventPosition startingPosition = EventPosition.fromEnqueuedTime(testData.getEnqueuedTime());

        // Act & Assert
        StepVerifier.create(consumer.receiveFromPartition(PARTITION_ID, startingPosition)
            .filter(x -> isMatchingEvent(x, testData.getMessageTrackingId()))
            .take(NUMBER_OF_EVENTS))
            .expectNextCount(NUMBER_OF_EVENTS)
            .expectComplete()
            .verify(Duration.ofMinutes(1));
    }

    /**
     * Verifies that we can have multiple consumers listening to the same partition + consumer group at the same time.
     */
    @Disabled("Investigate. Only 2 of the 4 consumers get the events. The other two consumers do not.")
    @Test
    public void parallelEventHubClients() throws InterruptedException {
        // Arrange
        final int numberOfClients = 4;
        final int numberOfEvents = 10;
        final String messageTrackingId = "message-tracking-id";
        final String messageTrackingValue = UUID.randomUUID().toString();
        final Flux<EventData> events = Flux.range(0, numberOfEvents).map(number -> {
            final EventData eventData = new EventData("testString".getBytes(UTF_8));
            eventData.getProperties().put(messageTrackingId, messageTrackingValue);
            return eventData;
        });

        final CountDownLatch countDownLatch = new CountDownLatch(numberOfClients);
        final EventHubAsyncClient[] clients = new EventHubAsyncClient[numberOfClients];
        for (int i = 0; i < numberOfClients; i++) {
            clients[i] = createBuilder()
                .transportType(transportType)
                .shareConnection()
                .buildAsyncClient();
        }

        final SendOptions sendOptions = new SendOptions().setPartitionId(PARTITION_ID);
        final EventHubProducerAsyncClient producer = clients[0].createProducer();
        final List<EventHubConsumerAsyncClient> consumers = new ArrayList<>();

        try {
            for (final EventHubAsyncClient hubClient : clients) {
                final EventHubConsumerAsyncClient consumer = hubClient.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, DEFAULT_PREFETCH_COUNT);
                consumers.add(consumer);

                consumer.receiveFromPartition(PARTITION_ID, EventPosition.latest())
                    .filter(partitionEvent -> TestUtils.isMatchingEvent(partitionEvent.getData(), messageTrackingValue))
                    .take(numberOfEvents).subscribe(partitionEvent -> {
                        EventData event = partitionEvent.getData();
                        logger.info("Event[{}] matched.", event.getSequenceNumber());
                    }, error -> Assertions.fail("An error should not have occurred:" + error.toString()),
                        () -> {
                            long count = countDownLatch.getCount();
                            logger.info("Finished consuming events. Counting down: {}", count);
                            countDownLatch.countDown();
                        });
            }

            // Act
            producer.send(events, sendOptions).block(TIMEOUT);

            // Assert
            // Wait for all the events we sent to be received by each of the consumers.
            countDownLatch.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            Assertions.assertEquals(0, countDownLatch.getCount());

            logger.info("Completed successfully.");
        } finally {
            logger.info("Disposing of subscriptions, consumers and clients.");

            dispose(producer);
            dispose(consumers.toArray(new EventHubConsumerAsyncClient[0]));
            dispose(clients);
        }
    }

    /**
     * Sending with credentials.
     */
    @Test
    public void getPropertiesWithCredentials() {
        // Arrange
        final EventHubAsyncClient client = createBuilder(true)
            .buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client.getProperties())
            .assertNext(properties -> {
                Assertions.assertEquals(getEventHubName(), properties.getName());
                Assertions.assertEquals(2, properties.getPartitionIds().stream().count());
            })
            .expectComplete()
            .verify(TIMEOUT);
    }
}
