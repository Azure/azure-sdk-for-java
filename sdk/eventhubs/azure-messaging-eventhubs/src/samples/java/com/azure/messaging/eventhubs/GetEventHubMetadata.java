// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import java.util.concurrent.Semaphore;

/**
 * Demonstrates how to fetch metadata from an Event Hub's partitions.
 */
public class GetEventHubMetadata {
    /**
     * Demonstrates how to get metadata from an Event Hub's partitions.
     *
     * @param args Unused arguments to the sample.
     * @throws InterruptedException if the semaphore could not be acquired.
     */
    public static void main(String[] args) throws InterruptedException {
        Semaphore semaphore = new Semaphore(1);

        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        // Instantiate a client that will be used to call the service.
        EventHubAsyncClient client = new EventHubClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

        // Acquiring the semaphore so that this sample does not end before all the partition properties are fetched.
        semaphore.acquire();

        // Querying the partition identifiers for the Event Hub. Then calling client.getPartitionProperties with the
        // identifier to get information about each partition.
        client.getPartitionIds().flatMap(partitionId -> client.getPartitionProperties(partitionId))
            .subscribe(properties -> {
                System.out.println("The Event Hub has the following properties:");
                System.out.printf(
                    "Event Hub Name: %s; Partition Id: %s; Is partition empty? %s; First Sequence Number: %s; "
                        + "Last Enqueued Time: %s; Last Enqueued Sequence Number: %s; Last Enqueued Offset: %s",
                    properties.eventHubName(), properties.id(), properties.isEmpty(),
                    properties.beginningSequenceNumber(),
                    properties.lastEnqueuedTime(),
                    properties.lastEnqueuedSequenceNumber(),
                    properties.lastEnqueuedOffset());
            }, error -> {
                    System.err.println("Error occurred while fetching partition properties: " + error.toString());
                }, () -> {
                    // Releasing the semaphore now that we've finished querying for partition properties.
                    semaphore.release();
                });

        System.out.println("Waiting for partition properties to complete...");
        semaphore.acquire();
        System.out.println("Finished.");
    }
}
