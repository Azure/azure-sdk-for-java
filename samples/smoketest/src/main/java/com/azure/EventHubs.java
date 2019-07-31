package com.azure;

import com.azure.messaging.eventhubs.*;
import reactor.core.publisher.Flux;

import java.io.IOException;

public class EventHubs {
    private static EventHubClient client;
    private static final String EVENT_HUBS_CONNECTION_STRING = System.getenv("EVENT_HUBS_CONNECTION_STRING");

    private static String getPartitionID() {
        System.out.print("Getting partition id... ");
        Flux<String> partitions = client.getPartitionIds();
        System.out.println("\tDONE.");
        //In ths sample, the events are going to be send and consume from the first partition.
        return partitions.blockFirst();
    }

    private static void sendAndReceiveEvents(String partitionId) {
        System.out.println("Creating the consumer");
        EventHubConsumer consumer = client.createConsumer(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, "0", EventPosition.earliest());
        System.out.println("Creating the producer");
        EventHubProducer producer = client.createProducer(new EventHubProducerOptions().partitionId(partitionId));

        System.out.println("Doing some things with the event data");
        String Text = "THIS IS AN EVENT IN JAVA";
        EventData event = new EventData(Text.getBytes());

        System.out.println("Sending the event");
        producer.send(event);

        System.out.println("Consuming the events");
        Flux<EventData> received = consumer.receive();
        System.out.println(received.blockFirst());
        consumer.receive().subscribe(e -> {
            System.out.println("HELOOOOOOU ENTREE");
            System.out.println(e.body());
        });

        System.out.println("Closing everything");
        try {
            producer.close();
            consumer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.print("EVENT HUUUUBSSSSS");
        client = new EventHubClientBuilder().connectionString(EVENT_HUBS_CONNECTION_STRING).buildAsyncClient();

        String partitionId = getPartitionID();
        sendAndReceiveEvents(partitionId);

        System.out.println("DOOOOOONEEE");

    }
}
