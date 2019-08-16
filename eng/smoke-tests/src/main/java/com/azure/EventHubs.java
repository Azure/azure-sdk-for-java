package com.azure;

import com.azure.messaging.eventhubs.*;
import com.azure.messaging.eventhubs.models.EventHubProducerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class EventHubs {
    private static final String EVENT_HUBS_CONNECTION_STRING = System.getenv("AZURE_EVENT_HUBS_CONNECTION_STRING");
    private static EventHubAsyncClient client;

    private static final Logger logger = LoggerFactory.getLogger(EventHubs.class);


    private static String getPartitionID() {
        logger.info("Getting partition id... ");
        Flux<String> partitions = client.getPartitionIds();
        logger.info("\tDONE.");
        //In ths sample, the events are going to be send and consume from the first partition.
        return partitions.blockFirst();
    }

    private static void sendAndReceiveEvents(String partitionId) {
        logger.info("Creating consumer... ");
        EventHubConsumer consumer = client.createConsumer(
            EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME,
            partitionId,
            EventPosition.latest());
        logger.info("\tDONE.");

        logger.info("Creating producer... ");
        EventHubProducer producer = client.createProducer(new EventHubProducerOptions().partitionId(partitionId));
        logger.info("\tDONE.");

        logger.info("Sending Events... ");
        Flux<EventData> events = Flux.just(
            new EventData(("Test event 1 in Java").getBytes(StandardCharsets.UTF_8)),
            new EventData(("Test event 2 in Java").getBytes(StandardCharsets.UTF_8)),
            new EventData(("Test event 3 in Java").getBytes(StandardCharsets.UTF_8))
        );

        producer.send(events).subscribe(
            (ignored) -> logger.info("sent"),
            error -> logger.error("Error received:" + error),
            () -> {
                //Closing the producer once is done with sending the events
                try {
                    producer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        );
        logger.info("\tDONE.");

        logger.info("Consuming Events... ");
        final int maxSeconds = 5;
        final int numOfEventsExpected = 3;
        CountDownLatch countDownLatch = new CountDownLatch(numOfEventsExpected);
        Disposable consumerSubscription = consumer.receive().subscribe(e -> {
            logger.info("\tEvent received: " + StandardCharsets.UTF_8.decode(e.body()));
            countDownLatch.countDown();
        });

        //Wait to get all the events
        try {
            boolean isSuccessful = countDownLatch.await(Duration.ofSeconds(maxSeconds).getSeconds(), TimeUnit.SECONDS);
            if (!isSuccessful) {
                throw new Exception("Error, expecting 3 events but " + countDownLatch.getCount() + " are missing.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //Dispose both subscriptions and close the clients
            consumerSubscription.dispose();
            try {
                producer.close();
                consumer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            client.close();
        }

        logger.info("DONE.");

    }

    public static void main(String[] args) {
        logger.info("---------------------");
        logger.info("EVENT HUBS");
        logger.info("---------------------");

        client = new EventHubClientBuilder().connectionString(EVENT_HUBS_CONNECTION_STRING).buildAsyncClient();

        String partitionId = getPartitionID();
        sendAndReceiveEvents(partitionId);
    }
}
