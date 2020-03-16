// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import java.util.concurrent.CountDownLatch;

/**
 * This sample shows how to setup an Event Processor that uses an older version of blob storage service as
 * checkpoint store.
 */
public class EventProcessorWithOlderStorageVersion {

    private static final String STORAGE_CONNECTION_STRING = "";
    private static final String SAS_TOKEN = "";
    private static final String CONTAINER_NAME = "";

    private static final String EH_CONNECTION_STRING = "";
    private static final String CONSUMER_GROUP = "";
    private static final String EVENT_HUB_NAME = "";

    /**
     * The main method to run this sample.
     *
     * @param args Ignored input arguments.
     * @throws InterruptedException If the program is interrupted while running the processor.
     */
    public static void main(String[] args) throws InterruptedException {

        // Setup the container client by adding a header policy to specify older version of storage
        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
            .connectionString(STORAGE_CONNECTION_STRING)
            .containerName(CONTAINER_NAME)
            .sasToken(SAS_TOKEN)
            .addPolicy(new AddHeadersPolicy(new HttpHeaders().put("x-ms-version", "2017-11-09")))
            .buildAsyncClient();

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        CountDownLatch countDownLatch = new CountDownLatch(3);

        // Create the event processor instance
        EventProcessorClient processor = new EventProcessorClientBuilder()
            .checkpointStore(blobCheckpointStore)
            .connectionString(EH_CONNECTION_STRING, EVENT_HUB_NAME)
            .consumerGroup(CONSUMER_GROUP)
            .processEvent(eventContext -> {
                if (eventContext.getEventData().getSequenceNumber() % 100 == 0) {
                    System.out.println(
                        String.format("Event = %s, partition = %s, seq num = %d, offset = %d",
                            eventContext.getEventData().getBodyAsString(),
                            eventContext.getPartitionContext().getPartitionId(),
                            eventContext.getEventData().getSequenceNumber(),
                            eventContext.getEventData().getOffset()));
                    eventContext.updateCheckpoint();
                }
            })
            .processError(errorContext -> {
                if (!isNumeric(errorContext.getPartitionContext().getPartitionId())) {
                    countDownLatch.countDown();
                }
                System.out.println(
                    "Error " + errorContext.getPartitionContext().getPartitionId() + " " + errorContext.getThrowable()
                        .getMessage());
            })
            .buildEventProcessorClient();

        processor.start();
        countDownLatch.await();
        processor.stop();
    }

    private static boolean isNumeric(String partitionId) {
        try {
            Integer.parseInt(partitionId);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
