// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.EventPosition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import java.time.Duration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class EventHubs {
    private static final String EVENT_HUBS_CONNECTION_STRING = System.getenv("AZURE_EVENT_HUBS_CONNECTION_STRING");

    private static EventHubProducerAsyncClient producer;
    private static EventHubConsumerAsyncClient consumer;

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubs.class);

    public static void main(String[] args) throws Exception {
        LOGGER.info("---------------------");
        LOGGER.info("EVENT HUBS");
        LOGGER.info("---------------------");

        // TODO: Manage the dependency graph involving the clients and partition fetching...
        createClients();
        String partitionId = getPartitionID();
        sendAndReceiveEvents(partitionId);
    }

    private static String getPartitionID() {
        LOGGER.info("Getting partition id... ");
        Flux<String> partitions = producer.getPartitionIds();
        LOGGER.info("\tDONE.");
        //In ths sample, the events are going to be send and consume from the first partition.
        return partitions.blockFirst();
    }

    private static void createClients() {
        LOGGER.info("Creating consumer... ");

        consumer = new EventHubClientBuilder()
            .connectionString(EVENT_HUBS_CONNECTION_STRING)
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient();
            // TODO: Do we need to specify position?

        LOGGER.info("\tDONE.");

        LOGGER.info("Creating producer... ");
        producer = new EventHubClientBuilder()
            .connectionString(EVENT_HUBS_CONNECTION_STRING)
            .buildAsyncProducerClient();
            // TODO: Do we specify an event hub name?

        LOGGER.info("\tDONE.");
    }

    private static void sendAndReceiveEvents(String partitionId) throws Exception {
        LOGGER.info("Sending Events... ");
        List<EventData> events = Arrays.asList(
            new EventData(("Test event 1 in Java")),
            new EventData(("Test event 2 in Java")),
            new EventData(("Test event 3 in Java"))
        );

        CreateBatchOptions options = new CreateBatchOptions()
            .setPartitionId(partitionId);
        producer
            .createBatch(options)
            .flatMap(batch -> {
                for (EventData event : events) {
                    if (!batch.tryAdd(event)) {
                        return Mono.error(new Exception("Could not add event to batch"));
                    }
                }

                return producer.send(batch);
            }).subscribe(
                unused -> LOGGER.info("events sent"),
                error -> LOGGER.error("Error received: " + error),
                () -> producer.close()
            );

        LOGGER.info("Consuming Events... ");
        final int maxSeconds = 5;
        final int numOfEventsExpected = events.size(); // TODO: Validate that this actually errors out the program
        CountDownLatch countDownLatch = new CountDownLatch(numOfEventsExpected);

        Disposable consumerSubscription = consumer
            .receive(true)
            .subscribe(e -> {
                LOGGER.info("\tEvent received: " + e.getData().getBodyAsString());
                countDownLatch.countDown();
            });

        //Wait to get all the events
        try {
            boolean isSuccessful = countDownLatch
                .await(Duration.ofSeconds(maxSeconds).getSeconds(), TimeUnit.SECONDS);

            if (!isSuccessful) {
                throw new Exception("Error, expecting 3 events but " + countDownLatch.getCount() + " are missing.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //Dispose both subscriptions and close the clients
            consumerSubscription.dispose();
            producer.close();
            consumer.close();
        }

        LOGGER.info("DONE.");

    }
}
