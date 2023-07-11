// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.IterableStream;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Demonstrates how to share a connection using a producer and consumer.
 */
public class ShareConnectionBetweenClients {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Some events to publish.
        List<EventData> producerEvents1 = Stream.of("Roast beef", "Cheese", "Tofu", "Turkey")
            .map(topping -> {
                EventData e = new EventData(topping);
                e.getProperties().put("producerId", 1);
                return e;
            })
            .collect(Collectors.toList());

        // The credential used is DefaultAzureCredential because it combines commonly used credentials
        // in deployment and development and chooses the credential to used based on its running environment.
        // More information can be found at: https://learn.microsoft.com/java/api/overview/azure/identity-readme
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .build();

        // The shareConnection() toggle tells the builder to use the same AMQP connection for multiple producer clients.
        //
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventHubClientBuilder builder = new EventHubClientBuilder()
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                credential)
            .shareConnection();

        EventHubProducerClient producer = builder.buildProducerClient();
        EventHubConsumerClient consumer = builder.buildConsumerClient();

        producer.send(producerEvents1);
        IterableStream<PartitionEvent> partitionEvents = consumer.receiveFromPartition("0", 2, EventPosition.earliest());

        partitionEvents.forEach(event -> {
            System.out.println("Received: " + event.getData().getBodyAsString());
        });

        // Dispose of the resources.
        producer.close();
        consumer.close();
    }
}
