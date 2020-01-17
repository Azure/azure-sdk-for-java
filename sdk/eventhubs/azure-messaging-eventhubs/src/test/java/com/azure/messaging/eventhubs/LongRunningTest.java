// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Long running tests that are manually.
 */
class LongRunningTest extends IntegrationTestBase {
    private final Duration duration = Duration.ofHours(8);

    LongRunningTest() {
        super(new ClientLogger(LongRunningTest.class));
    }

    @Disabled("Testing long running operations and disconnections.")
    @Test
    void twoConsumersAndSender() throws InterruptedException {
        final EventPosition firstPosition = EventPosition.fromEnqueuedTime(Instant.now());

        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .retry(RETRY_OPTIONS)
            .buildAsyncProducerClient();
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .retry(RETRY_OPTIONS)
            .buildAsyncConsumerClient();

        try {
            consumer.receiveFromPartition("0", firstPosition)
                .subscribe(event -> {
                    logger.info("[#0]: [{}]: {}", event.getData().getEnqueuedTime(),
                        event.getData().getSequenceNumber());
                }, error -> {
                    logger.error("Exception occurred in receive.", error);
                }, () -> logger.info("Completed receiving."));

            consumer.receiveFromPartition("1", firstPosition)
                .subscribe(event -> {
                    logger.info("[#1]: [{}]: {}", event.getData().getEnqueuedTime(),
                        event.getData().getSequenceNumber());
                }, error -> {
                    logger.error("Exception occurred in receive.", error);
                }, () -> logger.info("Completed receiving."));

            Flux.interval(Duration.ofSeconds(1))
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
            TimeUnit.MINUTES.sleep(duration.toMinutes());
            System.out.println("Complete.");
        } finally {
            producer.close();
            consumer.close();
        }

    }

    @Disabled("Testing idle/long running operations. Connections are timed out at 30 mins.")
    @Test
    void idleConnection() throws InterruptedException {
        try (EventHubProducerAsyncClient idleProducer = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .buildAsyncProducerClient()) {
            for (int i = 0; i < 4; i++) {
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
        }
    }
}
