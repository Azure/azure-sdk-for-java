// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.scenarios;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.messaging.eventhubs.stress.util.Constants;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.stereotype.Service;

@Service("ProcessEvents")
public class ProcessEvents extends EventHubsScenario {

    @Override
    public void run() {
        String storageConnStr = options.get(Constants.STORAGE_CONNECTION_STRING);
        String storageContainer = options.get(Constants.STORAGE_CONTAINER_NAME);

        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
                .connectionString(storageConnStr)
                .containerName(storageContainer)
                .buildAsyncClient();

        String eventHubsConnStr = options.get(Constants.EVENT_HUBS_CONNECTION_STRING);
        String eventHub = options.get(Constants.EVENT_HUB_NAME);

        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .connectionString(eventHubsConnStr, eventHub)
                .checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient))
                .processEvent(eventContext -> {
                    System.out.println("Partition id = " + eventContext.getPartitionContext().getPartitionId() + " and "
                            + "sequence number of event = " + eventContext.getEventData().getSequenceNumber());
                })
                .processError(errorContext -> {
                    System.out
                            .println("Error occurred while processing events " + errorContext.getThrowable().getMessage());
                })
                .buildEventProcessorClient();

        eventProcessorClient.start();

    }
}
