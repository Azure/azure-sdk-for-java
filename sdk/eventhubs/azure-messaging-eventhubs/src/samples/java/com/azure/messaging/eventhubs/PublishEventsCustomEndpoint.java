// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.models.SendOptions;

import java.util.Collections;

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
        // The credential used is DefaultAzureCredential because it combines commonly used credentials
        // in deployment and development and chooses the credential to used based on its running environment.
        // More information can be found at: https://learn.microsoft.com/java/api/overview/azure/identity-readme
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        String fullyQualifiedNamespace = "<<fully-qualified-namespace>>";
        String eventHubName = "<<event-hub-name>>";

        // The address of our intermediary service.
        String customEndpoint = "<< https://my-application-gateway.cloudapp.azure.com >>";

        // Instantiate a client that will be used to call the service.
        // We are using WEB_SOCKETS.
        EventHubProducerClient producer = new EventHubClientBuilder()
            .credential(fullyQualifiedNamespace, eventHubName, tokenCredential)
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
        producer.send(Collections.singletonList(event), sendOptions);

        System.out.println("Disposing of producer");
        producer.close();
    }
}
