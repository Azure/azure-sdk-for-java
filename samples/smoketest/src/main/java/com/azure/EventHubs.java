package com.azure;

import com.azure.messaging.eventhubs.*;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.util.Date;

public class EventHubs {
    private static final String EVENT_HUBS_CONNECTION_STRING = System.getenv("EVENT_HUBS_CONNECTION_STRING");
    private static EventHubClient client;

    private static String getPartitionID() {
        System.out.print("Getting partition id... ");
        Flux<String> partitions = client.getPartitionIds();
        System.out.println("\tDONE.");
        //In ths sample, the events are going to be send and consume from the first partition.
        return partitions.blockFirst();
    }

    private static void sendAndReceiveEvents(String partitionId) {
        System.out.print("Creating consumer... ");
        EventHubConsumer consumer = client.createConsumer(
            EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
            partitionId,
            EventPosition.latest());
        System.out.println("\tDONE.");

        System.out.print("Creating producer... ");
        EventHubProducer producer = client.createProducer(new EventHubProducerOptions().partitionId(partitionId));
        System.out.println("\tDONE.");

        System.out.print("Sending Events... ");
        Flux<EventData> events = Flux.just(
            new EventData(("Test event 1 in Java").getBytes(StandardCharsets.UTF_8)),
            new EventData(("Test event 2 in Java").getBytes(StandardCharsets.UTF_8)),
            new EventData(("Test event 3 in Java").getBytes(StandardCharsets.UTF_8))
        );

        producer.send(events).subscribe(
            (ignored) -> System.out.println("sent"),
            error -> System.err.println("Error received:" + error),
            () -> {
                //Closing the producer once is done with sending the events
                try {
                    producer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        );
        System.out.println("\tDONE.");

        System.out.println("Consuming Events... ");
        final int maxSeconds = 5;
        CountDownLatch countDownLatch = new CountDownLatch(3);
        Disposable consumerSubscription = consumer.receive().subscribe(e -> {
            System.out.println("\tEvent received: " + StandardCharsets.UTF_8.decode(e.body()));
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

        System.out.println("DONE.");

    }

    public static void main(String[] args) {
        System.out.println("\n---------------------");
        System.out.println("EVENT HUBS");
        System.out.println("---------------------\n");

        client = new EventHubClientBuilder().connectionString(EVENT_HUBS_CONNECTION_STRING).buildAsyncClient();

        String partitionId = getPartitionID();
        sendAndReceiveEvents(partitionId);
    }
}
