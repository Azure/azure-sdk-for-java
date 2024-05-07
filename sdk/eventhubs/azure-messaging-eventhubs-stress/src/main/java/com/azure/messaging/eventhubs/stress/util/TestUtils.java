// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.util;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Utility class for EventHubs stress tests.
 */
public final class TestUtils {
    private static final byte[] PAYLOAD = "this is a circular payload that is used to fill up the message".getBytes(StandardCharsets.UTF_8);
    private static final ClientLogger LOGGER = new ClientLogger(TestUtils.class);

    public static EventProcessorClientBuilder getProcessorBuilder(ScenarioOptions options, int prefetchCount) {
        return new EventProcessorClientBuilder()
            .prefetchCount(prefetchCount == 0 ? 1 : prefetchCount)
            .consumerGroup(options.getEventHubsConsumerGroup())
            .connectionString(options.getEventHubsConnectionString(), options.getEventHubsEventHubName())
            .checkpointStore(new BlobCheckpointStore(getContainerClient(options)));
    }

    public static BinaryData createMessagePayload(int messageSize) {
        final StringBuilder body = new StringBuilder(messageSize);
        for (int i = 0; i < messageSize; i++) {
            body.append((char) PAYLOAD[i % PAYLOAD.length]);
        }
        return BinaryData.fromString(body.toString());
    }

    public static EventHubClientBuilder getBuilder(ScenarioOptions options) {
        return new EventHubClientBuilder()
            .connectionString(options.getEventHubsConnectionString())
            .retryOptions(new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(5)))
            .eventHubName(options.getEventHubsEventHubName());
    }

    private static BlobContainerAsyncClient getContainerClient(ScenarioOptions options) {
        return new BlobContainerClientBuilder()
            .connectionString(options.getStorageConnectionString())
            .containerName(options.getStorageContainerName())
            .buildAsyncClient();
    }

    public static void blockingWait(Duration duration) {
        if (duration.toMillis() > 0) {
            try {
                Thread.sleep(duration.toMillis());
            } catch (InterruptedException e) {
                LOGGER.warning("wait interrupted");
            }
        }
    }

    private static void resetCheckpoint(ScenarioOptions options) {
        String storageConnStr = options.getStorageConnectionString();
        String containerName = options.getStorageContainerName();
        String eventHubConnStr = options.getEventHubsConnectionString();
        String eventHub = options.getEventHubsEventHubName();
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

    private TestUtils() {
    }
}
