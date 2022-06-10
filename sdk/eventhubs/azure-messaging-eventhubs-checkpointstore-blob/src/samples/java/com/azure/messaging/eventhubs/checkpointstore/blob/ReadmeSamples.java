// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import java.util.concurrent.TimeUnit;

/**
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {

    /**
     * Code sample for creating an async blob container client.
     */
    public void createBlobContainerClient() {
        // BEGIN: readme-sample-createBlobContainerClient
        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
            .connectionString("<STORAGE_ACCOUNT_CONNECTION_STRING>")
            .containerName("<CONTAINER_NAME>")
            .sasToken("<SAS_TOKEN>")
            .buildAsyncClient();
        // END: readme-sample-createBlobContainerClient
    }

    /**
     * Code sample for consuming events from event processor with blob checkpoint store.
     * @throws InterruptedException If the thread is interrupted.
     */
    public void consumeEventsUsingEventProcessor() throws InterruptedException {
        // BEGIN: readme-sample-consumeEventsUsingEventProcessor
        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
            .connectionString("<STORAGE_ACCOUNT_CONNECTION_STRING>")
            .containerName("<CONTAINER_NAME>")
            .sasToken("<SAS_TOKEN>")
            .buildAsyncClient();

        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
            .consumerGroup("<< CONSUMER GROUP NAME >>")
            .connectionString("<< EVENT HUB CONNECTION STRING >>")
            .checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient))
            .processEvent(eventContext -> {
                System.out.println("Partition id = " + eventContext.getPartitionContext().getPartitionId() + " and "
                    + "sequence number of event = " + eventContext.getEventData().getSequenceNumber());
            })
            .processError(errorContext -> {
                System.out.println("Error occurred while processing events " + errorContext.getThrowable().getMessage());
            })
            .buildEventProcessorClient();

        // This will start the processor. It will start processing events from all partitions.
        eventProcessorClient.start();

        // (for demo purposes only - adding sleep to wait for receiving events)
        TimeUnit.SECONDS.sleep(2);

        // When the user wishes to stop processing events, they can call `stop()`.
        eventProcessorClient.stop();
        // END: readme-sample-consumeEventsUsingEventProcessor
    }
}
