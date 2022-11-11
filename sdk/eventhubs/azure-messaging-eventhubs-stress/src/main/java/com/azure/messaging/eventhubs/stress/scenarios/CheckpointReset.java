// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.scenarios;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import org.springframework.stereotype.Service;

/**
 * Test to reset blob checkpoint
 */
@Service("CheckpointReset")
public class CheckpointReset extends EventHubsScenario {
    private static final ClientLogger LOGGER = new ClientLogger(CheckpointReset.class);

    @Override
    public void run() {
        String storageConnStr = options.getStorageConnectionString();
        String containerName = options.getStorageContainerName();
        String eventHubConnStr = options.getEventhubsConnectionString();
        String eventHub = options.getEventhubsEventHubName();
        String consumerGroup = options.getEventHubsConsumerGroup();
        BlobContainerClient containerClient = new BlobContainerClientBuilder()
            .connectionString(storageConnStr)
            .containerName(containerName)
            .buildClient();

        PagedIterable<BlobItem> blobs = containerClient.listBlobs();
        try {
            for (BlobItem blob : blobs) {
                String namespace = eventHubConnStr.substring(0, eventHubConnStr.indexOf("/"));
                if (blob.getName().contains(String.format("%s/%s/%s",
                    namespace, eventHub, consumerGroup))) {
                    BlobClient blobClient = containerClient.getBlobClient(blob.getName());
                    blobClient.delete();
                    LOGGER.info("Blob deleted: {}, {}", blob.getName(), blob.getMetadata());
                }
            }
        } catch (Exception exp) {
            LOGGER.warning("Exception deleting blobs: {}", exp.getMessage());
        }

        LOGGER.info("Finished Checkpoint Reset");
    }
}
