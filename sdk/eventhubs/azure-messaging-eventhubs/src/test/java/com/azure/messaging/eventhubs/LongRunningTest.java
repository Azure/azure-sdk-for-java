// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Long running tests that are manually executed.
 */
@Tag(TestUtils.INTEGRATION)
class LongRunningTest extends IntegrationTestBase {
    private final Duration duration = Duration.ofHours(8);

    LongRunningTest() {
        super(new ClientLogger(LongRunningTest.class));
    }

    @Disabled("Testing long running operations and disconnections.")
    @Test
    void twoConsumersAndSender() throws InterruptedException {
        final EventPosition firstPosition = EventPosition.fromEnqueuedTime(Instant.now());

        EventHubProducerAsyncClient producer = toClose(new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .retry(RETRY_OPTIONS)
            .buildAsyncProducerClient());
        EventHubConsumerAsyncClient consumer = toClose(new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .retry(RETRY_OPTIONS)
            .buildAsyncConsumerClient());

        toClose(consumer.receiveFromPartition("0", firstPosition)
            .subscribe(event -> {
                logger.info("[#0]: [{}]: {}", event.getData().getEnqueuedTime(),
                    event.getData().getSequenceNumber());
            }, error -> {
                    logger.error("Exception occurred in receive.", error);
                }, () -> logger.info("Completed receiving.")));

        toClose(consumer.receiveFromPartition("1", firstPosition)
            .subscribe(event -> {
                logger.info("[#1]: [{}]: {}", event.getData().getEnqueuedTime(),
                    event.getData().getSequenceNumber());
            }, error -> {
                    logger.error("Exception occurred in receive.", error);
                }, () -> logger.info("Completed receiving.")));

        toClose(Flux.interval(Duration.ofSeconds(1))
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
                }));

        System.out.println("Sleeping while performing work.");
        TimeUnit.MINUTES.sleep(duration.toMinutes());
        System.out.println("Complete.");
    }

    @Disabled("Testing idle clients. Connections are timed out at 30 mins.")
    @Test
    void idleConnection() throws InterruptedException {
        try (EventHubProducerAsyncClient idleProducer = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .buildAsyncProducerClient()) {
            for (int i = 0; i < 4; i++) {
                System.out.println("Iteration: " + i);

                toClose(idleProducer.createBatch().flatMap(batch -> {
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
                    }));

                System.out.println("Sleeping 40 mins.");
                TimeUnit.MINUTES.sleep(40);
                System.out.println("Complete.");
            }
        }
    }

    @Disabled("Testing idle clients. Connections are timed out at 30 mins.")
    @Test
    void idleSendLinks() throws InterruptedException {
        try (EventHubProducerAsyncClient idleProducer = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .buildAsyncProducerClient()) {

            for (int i = 0; i < 4; i++) {
                System.out.println("Iteration: " + i);

                toClose(idleProducer.getEventHubProperties().subscribe(properties -> {
                    System.out.printf("[%s]: ids[%s]. Received: %s%n",
                        properties.getName(),
                        String.join(",", properties.getPartitionIds()),
                        Instant.now());
                }, error -> System.err.println("Error receiving ids: " + error)));

                toClose(idleProducer.createBatch().flatMap(batch -> {
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
                }, error -> System.err.println("Error sending batch: " + error)));

                System.out.println("Sleeping 15 mins.");
                TimeUnit.MINUTES.sleep(15);
                System.out.println("Completed sleep.");
            }
        }
    }

    @Disabled("Testing long running operations and disconnections.")
    @Test
    void worksAfterReconnection() throws InterruptedException {
        final String partitionId = "0";
        final CreateBatchOptions options = new CreateBatchOptions().setPartitionId(partitionId);

        EventHubProducerAsyncClient producer = toClose(new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .retry(RETRY_OPTIONS)
            .buildAsyncProducerClient());

        EventHubAsyncClient client = toClose(new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .buildAsyncClient());

        toClose(Flux.interval(Duration.ofSeconds(1))
            .flatMap(position -> client.getPartitionIds().collectList())
            .subscribe(partitionIds -> {
                System.out.printf("Ids %s: {%s}%n", Instant.now(), String.join(",", partitionIds));
            }, error -> {
                    logger.error("Error fetching info.", error);
                }, () -> {
                    logger.info("Complete.");
                }));
        toClose(Flux.interval(Duration.ofSeconds(5))
            .flatMap(position -> producer.createBatch(options).flatMap(batch -> {
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
                System.out.println("---- Sent batch at: " + instant);
            }, error -> {
                    logger.error("---- Error sending batch: ", error);
                }, () -> {
                    logger.info("---- Complete.");
                }));

        System.out.println("Sleeping while performing work.");
        TimeUnit.MINUTES.sleep(30);
        System.out.println("Complete.");
    }
}
