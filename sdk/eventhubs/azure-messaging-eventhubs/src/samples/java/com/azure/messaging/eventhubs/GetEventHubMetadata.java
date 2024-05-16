// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Demonstrates how to fetch metadata from an Event Hub's partitions using synchronous client.
 */
public class GetEventHubMetadata {
    /**
     * Demonstrates how to get metadata from an Event Hub's partitions.
     *
     * @param args Unused arguments to the sample.
     */
    public static void main(String[] args) {
        // The credential used is DefaultAzureCredential because it combines commonly used credentials
        // in deployment and development and chooses the credential to used based on its running environment.
        // More information can be found at: https://learn.microsoft.com/java/api/overview/azure/identity-readme
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Instantiate a client that will be used to call the service. Using a try-resource block, so it disposes of
        // the client when we are done.
        //
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventHubProducerClient client = new EventHubClientBuilder()
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                tokenCredential)
            .buildProducerClient();

        // Querying the partition identifiers for the Event Hub. Then calling client.getPartitionProperties with the
        // identifier to get information about each partition.
        for (String partitionId : client.getPartitionIds()) {
            PartitionProperties properties = client.getPartitionProperties(partitionId);
            System.out.printf(
                "Event Hub Name: %s; Partition Id: %s; Is partition empty? %s; First Sequence Number: %s; "
                    + "Last Enqueued Time: %s; Last Enqueued Sequence Number: %s; Last Enqueued Offset: %s%n",
                properties.getEventHubName(),
                properties.getId(),
                properties.isEmpty(),
                properties.getBeginningSequenceNumber(),
                properties.getLastEnqueuedTime(),
                properties.getLastEnqueuedSequenceNumber(),
                properties.getLastEnqueuedOffset());
        }

        // Dispose of the client.
        client.close();
    }
}
