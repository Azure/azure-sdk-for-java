// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;

class EventHubProducerAsyncClientIntegrationTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "1";

    private EventHubProducerAsyncClient producer;
    private EventHubConsumerAsyncClient consumer;

    EventHubProducerAsyncClientIntegrationTest() {
        super(new ClientLogger(EventHubProducerAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        producer = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .retry(RETRY_OPTIONS)
            .buildAsyncProducerClient();
    }

    @Override
    protected void afterTest() {
        dispose(producer, consumer);
    }

    /**
     * Verifies that we can create and send a message to an Event Hub partition.
     */
    @Test
    void sendMessageToPartition() {
        // Arrange
        final SendOptions sendOptions = new SendOptions().setPartitionId(PARTITION_ID);
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        StepVerifier.create(producer.send(events, sendOptions))
            .verifyComplete();
    }

    /**
     * Verifies that we can create an {@link EventHubProducerAsyncClient} that does not care about partitions and lets
     * the service distribute the events.
     */
    @Test
    void sendMessage() {
        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        StepVerifier.create(producer.send(events))
            .verifyComplete();
    }

    /**
     * Verifies we can create an {@link EventDataBatch} and send it using our EventHubProducer.
     */
    @Test
    void sendBatch() {
        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        final Mono<EventDataBatch> createBatch = producer.createBatch().map(batch -> {
            events.forEach(event -> {
                Assertions.assertTrue(batch.tryAdd(event));
            });

            return batch;
        });

        // Act & Assert
        StepVerifier.create(createBatch.flatMap(batch -> producer.send(batch)))
            .verifyComplete();
    }

    /**
     * Verifies we can create an {@link EventDataBatch} with a partition key and send it using our EventHubProducer.
     */
    @Test
    void sendBatchWithPartitionKey() {
        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        final CreateBatchOptions options = new CreateBatchOptions().setPartitionKey("my-partition-key");
        final Mono<EventDataBatch> createBatch = producer.createBatch(options)
            .map(batch -> {
                Assertions.assertEquals(options.getPartitionKey(), batch.getPartitionKey());

                events.forEach(event -> {
                    Assertions.assertTrue(batch.tryAdd(event));
                });

                return batch;
            });

        // Act & Assert
        StepVerifier.create(createBatch.flatMap(batch -> producer.send(batch)))
            .verifyComplete();
    }

    /**
     * Verify that we can send to multiple partitions, round-robin, and with a partition key, using the same producer.
     */
    @Test
    void sendEventsWithKeyAndPartition() {
        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act
        final Mono<Void> onComplete = Mono.when(producer.send(events),
            producer.send(Flux.just(events.get(0))),
            producer.send(Flux.fromIterable(events), new SendOptions().setPartitionId("1")),
            producer.send(Flux.fromIterable(events), new SendOptions().setPartitionId("0")),
            producer.send(Flux.fromIterable(events), new SendOptions().setPartitionKey("sandwiches")));

        // Assert
        StepVerifier.create(onComplete)
            .verifyComplete();
    }

    @Test
    void sendAllPartitions() {
        final List<String> partitionIds = producer.getPartitionIds().collectList().block(TIMEOUT);

        Assertions.assertNotNull(partitionIds);

        for (String partitionId : partitionIds) {
            final EventDataBatch batch =
                producer.createBatch(new CreateBatchOptions().setPartitionId(partitionId)).block(TIMEOUT);
            Assertions.assertNotNull(batch);

            Assertions.assertTrue(batch.tryAdd(TestUtils.getEvent("event", "test guid",
                Integer.parseInt(partitionId))));

            // Act & Assert
            StepVerifier.create(producer.send(batch)).expectComplete().verify(TIMEOUT);
        }
    }

    /**
     * Sending with credentials.
     */
    @Test
    void sendWithCredentials() {
        // Arrange
        final EventData event = new EventData("body");
        final SendOptions options = new SendOptions().setPartitionId(PARTITION_ID);
        final EventHubProducerAsyncClient client = createBuilder(true)
            .buildAsyncProducerClient();

        // Act & Assert
        try {
            StepVerifier.create(client.getEventHubProperties())
                .assertNext(properties -> {
                    Assertions.assertEquals(getEventHubName(), properties.getName());
                    Assertions.assertEquals(2, properties.getPartitionIds().stream().count());
                })
                .expectComplete()
                .verify(TIMEOUT);

            StepVerifier.create(client.send(event, options))
                .expectComplete()
                .verify(TIMEOUT);
        } finally {
            dispose(client);
        }
    }

    @Disabled("Testing long running operations and disconnections.")
    @Test
    void twoConsumersAndSender() throws InterruptedException {
        final EventPosition firstPosition = EventPosition.fromEnqueuedTime(Instant.now());

        consumer = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .retry(RETRY_OPTIONS)
            .buildAsyncConsumerClient();

        consumer.receiveFromPartition("0", firstPosition)
            .subscribe(event -> {
                logger.info("[#0]: [{}]: {}", event.getData().getEnqueuedTime(), event.getData().getSequenceNumber());
            }, error -> {
                logger.error("Exception occurred in receive.", error);
            }, () -> logger.info("Completed receiving."));

        consumer.receiveFromPartition("1", firstPosition)
            .subscribe(event -> {
                logger.info("[#1]: [{}]: {}", event.getData().getEnqueuedTime(), event.getData().getSequenceNumber());
            }, error -> {
                logger.error("Exception occurred in receive.", error);
            }, () -> logger.info("Completed receiving."));

        Flux.interval(Duration.ofSeconds(5))
            .flatMap(position -> producer.createBatch().flatMap(batch -> {
                IntStream.range(0, 3).mapToObj(number -> new EventData("Position" + position + ": " + number))
                    .forEach(event -> {
                        if (!batch.tryAdd(event)) {
                            logger.error("Could not add event. Size: {}. Max: {}. Content: {}",
                                batch.getSizeInBytes(), batch.getMaxSizeInBytes(), event.getBodyAsString());
                        }
                    });

                return producer.send(batch).thenReturn(Instant.now());
            }))
            .subscribe(instant -> {
                System.out.println("Sent batch at: " + instant);
            }, error -> {
                logger.error("Error sending batch: ", error);
            }, () -> {
                logger.info("Complete.");
            });

        System.out.println("Sleeping while performing work.");
        TimeUnit.MINUTES.sleep(30);
        System.out.println("Complete.");
    }

    @Disabled("Testing idle/long running operations.")
    @Test
    void idleSenders() throws InterruptedException {
        EventHubProducerAsyncClient idleProducer = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .buildAsyncProducerClient();
        try {
            for (int i = 0; i < 10; i++) {
                System.out.println("Iteration: " + i);

                idleProducer.createBatch().flatMap(batch -> {
                    IntStream.range(0, 3).mapToObj(number -> new EventData("Number : " + number))
                        .forEach(event -> {
                            if (!batch.tryAdd(event)) {
                                logger.error("Could not add event. Size: {}. Max: {}. Content: {}",
                                    batch.getSizeInBytes(), batch.getMaxSizeInBytes(), event.getBodyAsString());
                            }
                        });

                    return idleProducer.send(batch).thenReturn(Instant.now());
                }).subscribe(instant -> {
                    System.out.println("Sent batch at: " + instant);
                }, error -> {
                    logger.error("Error sending batch: ", error);
                }, () -> {
                    logger.info("Complete.");
                });

                System.out.println("Sleeping 40 mins.");
                TimeUnit.MINUTES.sleep(40);
                System.out.println("Complete.");
            }
        } finally {
            idleProducer.close();
        }
    }
}
