// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventProcessor;
import com.azure.messaging.eventhubs.PartitionManager;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.ContainerAsyncClient;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementation of {@link PartitionManager} that uses
 * <a href="https://docs.microsoft.com/en-us/azure/storage/common/storage-introduction#blob-storage">Storage Blobs</a>
 * for persisting partition ownership and checkpoint information. {@link EventProcessor EventProcessors} can use this
 * implementation to load balance and update checkpoints.
 *
 * @see EventProcessor
 */
public class BlobPartitionManager implements PartitionManager {

    // All languages should use the same key for metadata. Discuss name for these.
    private static final String SEQUENCE_NUMBER = "sequenceNumber";
    private static final String OFFSET = "offset";
    private static final String OWNER_ID = "ownerId";
    private static final String ETAG = "eTag";

    private static final String BLOB_PATH_SEPARATOR = "/";
    private static final ByteBuffer UPLOAD_DATA = ByteBuffer.wrap("".getBytes(UTF_8));

    private final ContainerAsyncClient containerAsyncClient;
    private final ClientLogger logger = new ClientLogger(BlobPartitionManager.class);
    private final Map<String, BlobAsyncClient> blobClients = new ConcurrentHashMap<>();

    /**
     * Creates an instance of BlobPartitionManager.
     *
     * @param containerAsyncClient The {@link ContainerAsyncClient} this instance will use to read and update blobs in
     * the storage container.
     */
    public BlobPartitionManager(ContainerAsyncClient containerAsyncClient) {
        this.containerAsyncClient = containerAsyncClient;
    }

    /**
     * {@inheritDoc}
     *
     * @param eventHubName The Event Hub name to get ownership information.
     * @param consumerGroupName The consumer group name to get ownership information.
     * @return A {@link Flux} of current partition ownership information.
     */
    @Override
    public Flux<PartitionOwnership> listOwnership(String eventHubName, String consumerGroupName) {
        String prefix = getBlobPrefix(eventHubName, consumerGroupName);
        BlobListDetails details = new BlobListDetails().metadata(true);
        ListBlobsOptions options = new ListBlobsOptions().prefix(prefix).details(details);
        return containerAsyncClient.listBlobsFlat(options)
            // Blob names should be of the pattern eventhub/consumergroup/<partitionId>
            // While we can further check if the partition id is numeric, it may not necessarily be the case in future.
            .filter(blobItem -> blobItem.name().split(BLOB_PATH_SEPARATOR).length == 3)
            .map(this::convertToPartitionOwnership);
    }

    /**
     * {@inheritDoc}
     *
     * @param requestedPartitionOwnerships Array of partition ownerships this instance is requesting to own.
     * @return A {@link Flux} of successfully claimed ownership of partitions.
     */
    @Override
    public Flux<PartitionOwnership> claimOwnership(PartitionOwnership... requestedPartitionOwnerships) {

        return Flux.fromArray(requestedPartitionOwnerships).flatMap(
            partitionOwnership -> {

                String partitionId = partitionOwnership.partitionId();
                String blobName = getBlobName(partitionOwnership.eventHubName(),
                    partitionOwnership.consumerGroupName(), partitionId);

                if (!blobClients.containsKey(blobName)) {
                    blobClients.put(blobName, containerAsyncClient.getBlobAsyncClient(blobName));
                }

                BlobAsyncClient blobAsyncClient = blobClients.get(blobName);

                Metadata metadata = new Metadata();
                metadata.put(OWNER_ID, partitionOwnership.ownerId());
                metadata.put(OFFSET, partitionOwnership.offset());
                Long sequenceNumber = partitionOwnership.sequenceNumber();
                metadata.put(SEQUENCE_NUMBER, sequenceNumber == null ? null : String.valueOf(sequenceNumber));
                BlobAccessConditions blobAccessConditions = new BlobAccessConditions();
                if (ImplUtils.isNullOrEmpty(partitionOwnership.eTag())) {
                    // New blob should be created
                    blobAccessConditions.modifiedAccessConditions(new ModifiedAccessConditions()
                        .ifNoneMatch("*"));
                    return blobAsyncClient.asBlockBlobAsyncClient()
                        .uploadWithResponse(Flux.just(UPLOAD_DATA), 0, null, metadata,
                            blobAccessConditions)
                        .flatMapMany(response -> {
                            partitionOwnership.eTag(response.headers().get(ETAG).value());
                            return Mono.just(partitionOwnership);
                        }, error -> {
                                logger.info("Couldn't claim ownership of partition {}, error {}", partitionId,
                                    error.getMessage());
                                return Mono.empty();
                            }, Mono::empty);
                } else {
                    // update existing blob
                    blobAccessConditions.modifiedAccessConditions(new ModifiedAccessConditions()
                        .ifMatch(partitionOwnership.eTag()));
                    return blobAsyncClient.setMetadataWithResponse(metadata, blobAccessConditions)
                        .flatMapMany(response -> {
                            partitionOwnership.eTag(response.headers().get(ETAG).value());
                            return Mono.just(partitionOwnership);
                        }, error -> {
                                logger.info("Couldn't claim ownership of partition {}, error {}", partitionId,
                                    error.getMessage());
                                return Mono.empty();
                            }, () -> Mono.empty());
                }
            }
        );
    }

    /**
     * {@inheritDoc}
     *
     * @param checkpoint Checkpoint information containing sequence number and offset to be stored for this partition.
     * @return A {@link Mono} containing the new ETag generated from a successful checkpoint update.
     */
    @Override
    public Mono<String> updateCheckpoint(Checkpoint checkpoint) {
        if (checkpoint.sequenceNumber() == null && checkpoint.offset() == null) {
            throw logger.logExceptionAsWarning(Exceptions
                .propagate(new IllegalStateException(
                    "Both sequence number and offset cannot be null when updating a checkpoint")));
        }

        String partitionId = checkpoint.partitionId();
        String blobName = getBlobName(checkpoint.eventHubName(), checkpoint.consumerGroupName(), partitionId);
        if (!blobClients.containsKey(blobName)) {
            blobClients.put(blobName, containerAsyncClient.getBlobAsyncClient(blobName));
        }

        Metadata metadata = new Metadata();
        String sequenceNumber = checkpoint.sequenceNumber() == null ? null
            : String.valueOf(checkpoint.sequenceNumber());

        metadata.put(SEQUENCE_NUMBER, sequenceNumber);
        metadata.put(OFFSET, checkpoint.offset());
        metadata.put(OWNER_ID, checkpoint.ownerId());
        BlobAsyncClient blobAsyncClient = blobClients.get(blobName);
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions()
            .modifiedAccessConditions(new ModifiedAccessConditions().ifMatch(checkpoint.eTag()));

        return blobAsyncClient.setMetadataWithResponse(metadata, blobAccessConditions)
            .map(response -> response.headers().get(ETAG).value());
    }

    private String getBlobPrefix(String eventHubName, String consumerGroupName) {
        return eventHubName + BLOB_PATH_SEPARATOR + consumerGroupName;
    }

    private String getBlobName(String eventHubName, String consumerGroupName, String partitionId) {
        return eventHubName + BLOB_PATH_SEPARATOR + consumerGroupName + BLOB_PATH_SEPARATOR + partitionId;
    }

    private PartitionOwnership convertToPartitionOwnership(BlobItem blobItem) {
        PartitionOwnership partitionOwnership = new PartitionOwnership();
        logger.info("Found blob for partition {}", blobItem.name());

        String[] names = blobItem.name().split(BLOB_PATH_SEPARATOR);
        partitionOwnership.eventHubName(names[0]);
        partitionOwnership.consumerGroupName(names[1]);
        partitionOwnership.partitionId(names[2]);

        if (ImplUtils.isNullOrEmpty(blobItem.metadata())) {
            logger.warning("No metadata available for blob {}", blobItem.name());
            return partitionOwnership;
        }

        blobItem.metadata().entrySet().stream()
            .forEach(entry -> {
                switch (entry.getKey()) {
                    case OWNER_ID:
                        partitionOwnership.ownerId(entry.getValue());
                        break;
                    case SEQUENCE_NUMBER:
                        partitionOwnership.sequenceNumber(Long.valueOf(entry.getValue()));
                        break;
                    case OFFSET:
                        partitionOwnership.offset(entry.getValue());
                        break;
                    default:
                        // do nothing, other metadata that we don't use
                        break;
                }
            });
        BlobProperties blobProperties = blobItem.properties();
        partitionOwnership.lastModifiedTime(blobProperties.lastModified().toInstant().toEpochMilli());
        partitionOwnership.eTag(blobProperties.etag());
        return partitionOwnership;
    }

}
