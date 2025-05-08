// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.util;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
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
    private static final byte[] PAYLOAD
        = "this is a circular payload that is used to fill up the message".getBytes(StandardCharsets.UTF_8);
    private static final ClientLogger LOGGER = new ClientLogger(TestUtils.class);

    public static EventProcessorClientBuilder getProcessorBuilder(ScenarioOptions options, int prefetchCount) {
        final TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        final EventProcessorClientBuilder builder = new EventProcessorClientBuilder()
            .credential(options.getEventHubsFullyQualifiedNamespace(), options.getEventHubsEventHubName(),
                tokenCredential)
            .prefetchCount(prefetchCount == 0 ? 1 : prefetchCount)
            .consumerGroup(options.getEventHubsConsumerGroup())
            .checkpointStore(new BlobCheckpointStore(getContainerClient(options)));

        if (options.useV2Stack()) {
            Configuration configuration
                = new ConfigurationBuilder().putProperty("com.azure.messaging.eventhubs.v2", "true").build();

            builder.configuration(configuration);
        }
        return builder;
    }

    public static BinaryData createMessagePayload(int messageSize) {
        final StringBuilder body = new StringBuilder(messageSize);
        for (int i = 0; i < messageSize; i++) {
            body.append((char) PAYLOAD[i % PAYLOAD.length]);
        }
        return BinaryData.fromString(body.toString());
    }

    public static EventHubClientBuilder getBuilder(ScenarioOptions options) {
        final TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        final EventHubClientBuilder builder = new EventHubClientBuilder()
            .credential(options.getEventHubsFullyQualifiedNamespace(), options.getEventHubsEventHubName(),
                tokenCredential)
            .retryOptions(new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(5)))
            .eventHubName(options.getEventHubsEventHubName())
            .transportType(options.getAmqpTransportType())
            .consumerGroup(options.getEventHubsConsumerGroup());

        if (options.useV2Stack()) {
            Configuration configuration
                = new ConfigurationBuilder().putProperty("com.azure.messaging.eventhubs.v2", "true").build();

            builder.configuration(configuration);
        }

        return builder;
    }

    private static BlobContainerAsyncClient getContainerClient(ScenarioOptions options) {
        final DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        return new BlobContainerClientBuilder().endpoint(options.getStorageBlobEndpointUri())
            .containerName(options.getStorageContainerName())
            .credential(credential)
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
        String containerName = options.getStorageContainerName();
        String eventHub = options.getEventHubsEventHubName();
        String consumerGroup = options.getEventHubsConsumerGroup();
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        BlobContainerClient containerClient
            = new BlobContainerClientBuilder().endpoint(options.getStorageBlobEndpointUri())
                .containerName(options.getStorageContainerName())
                .credential(credential)
                .buildClient();

        PagedIterable<BlobItem> blobs = containerClient.listBlobs();
        try {
            for (BlobItem blob : blobs) {
                String namespace = options.getEventHubsFullyQualifiedNamespace();
                if (blob.getName().contains(String.format("%s/%s/%s", namespace, eventHub, consumerGroup))) {
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
