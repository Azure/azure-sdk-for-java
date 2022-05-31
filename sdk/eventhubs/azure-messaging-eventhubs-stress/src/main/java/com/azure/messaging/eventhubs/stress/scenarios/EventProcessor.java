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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("EventProcessor")
public class EventProcessor extends EventHubsScenario {
    private static final Logger logger = LoggerFactory.getLogger(EventProcessor.class);

    @Override
    public void run() {
        final String storageConnStr = options.get(Constants.STORAGE_CONNECTION_STRING);
        final String storageContainer = options.get(Constants.STORAGE_CONTAINER_NAME);

        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
            .connectionString(storageConnStr)
            .containerName(storageContainer)
            .buildAsyncClient();

        final String eventHubsConnStr = options.get(Constants.EVENT_HUBS_CONNECTION_STRING);
        final String eventHub = options.get(Constants.EVENT_HUB_NAME);

        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .connectionString(eventHubsConnStr, eventHub)
            .checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient))
            .processEvent(eventContext -> {
                logger.info("Partition id = " + eventContext.getPartitionContext().getPartitionId() + " and "
                    + "sequence number of event = " + eventContext.getEventData().getSequenceNumber());
            })
            .processError(errorContext -> {
                logger.info("Error occurred while processing events " + errorContext.getThrowable().getMessage());
            })
            .buildEventProcessorClient();

        eventProcessorClient.start();
    }
}
