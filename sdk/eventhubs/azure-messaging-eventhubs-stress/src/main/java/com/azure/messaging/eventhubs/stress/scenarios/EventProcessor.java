// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.scenarios;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.stereotype.Component;

/**
 * Test for EventProcessorClient
 */
@Component("EventProcessor")
public class EventProcessor extends EventHubsScenario {
    private static final ClientLogger LOGGER = new ClientLogger(EventProcessor.class);

    @Override
    public void run() {
        final String storageConnStr = options.getStorageConnectionString();
        final String storageContainer = options.getStorageContainerName();

        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
            .connectionString(storageConnStr)
            .containerName(storageContainer)
            .buildAsyncClient();

        final String eventHubsConnStr = options.getEventhubsConnectionString();
        final String eventHub = options.getEventHubsEventHubName();

        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .connectionString(eventHubsConnStr, eventHub)
            .checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient))
            .processEvent(eventContext -> {
                LOGGER.verbose("Partition id = " + eventContext.getPartitionContext().getPartitionId() + " and "
                    + "sequence number of event = " + eventContext.getEventData().getSequenceNumber());
            })
            .processError(errorContext -> {
                LOGGER.error("Error occurred while processing events " + errorContext.getThrowable().getMessage());
            })
            .buildEventProcessorClient();

        eventProcessorClient.start();
    }
}
