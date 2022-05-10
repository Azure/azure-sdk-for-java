// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.IterableStream;
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

        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        // The shareConnection() toggle tells the builder to use the same AMQP connection for multiple producer clients.
        EventHubClientBuilder builder = new EventHubClientBuilder()
            .connectionString(connectionString)
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
