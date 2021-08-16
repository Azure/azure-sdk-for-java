// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.eventhubs.models.SendOptions;

/**
 * Sample demonstrates how to use an intermediary service to connect to Azure Event Hubs. In this demo, an application
 * gateway.
 */
public class PublishEventsCustomEndpoint {
    /**
     * Main method to invoke this demo about how to use an intermediary service to connect to Azure Event Hubs.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 3. Creating a "Shared access policy" for your Event Hubs namespace.
        // 4. Copying the connection string from the policy's properties.
        //    (The default policy name is "RootManageSharedAccessKey".)
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey={sharedAccessKey}";
        String eventHubName = "<< my-event-hub-name >>";

        // The address of our intermediary service.
        String customEndpoint = "<< https://my-application-gateway.cloudapp.azure.com >>";

        // Instantiate a client that will be used to call the service.
        // We are using WEB_SOCKETS.
        EventHubProducerClient producer = new EventHubClientBuilder()
            .connectionString(connectionString, eventHubName)
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
            .customEndpointAddress(customEndpoint)
            .buildProducerClient();

        // Querying the partition identifiers for the Event Hub. Then calling client.getPartitionProperties with the
        // identifier to get information about each partition.
        final EventHubProperties properties = producer.getEventHubProperties();
        System.out.printf("Event Hub Information: %s; Created: %s; PartitionIds: [%s]%n",
            properties.getName(),
            properties.getCreatedAt(),
            String.join(", ", properties.getPartitionIds()));

        // Sending an event to a specific partition.
        final EventData event = new EventData("Hello world");
        final SendOptions sendOptions = new SendOptions()
            .setPartitionId("0");

        System.out.println("Sending event to partition: " + sendOptions.getPartitionId());
        producer.send(event, sendOptions);

        System.out.println("Disposing of producer");
        producer.close();
    }
}
