// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.core.http.rest.Response;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventProcessor;
import com.azure.messaging.eventhubs.PartitionManager;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Implementation of {@link PartitionManager} that uses
 * <a href="https://docs.microsoft.com/en-us/azure/storage/common/storage-introduction#blob-storage">Storage Blobs</a>
 * for persisting partition ownership and checkpoint information. {@link EventProcessor EventProcessors} can use this
 * implementation to load balance and update checkpoints.
 *
 * @see EventProcessor
 */
public class BlobPartitionManager implements PartitionManager {

    private static final String SEQUENCE_NUMBER = "SequenceNumber";
    private static final String OFFSET = "Offset";
    private static final String OWNER_ID = "OwnerId";
    private static final String ETAG = "eTag";
    private static final String CLAIM_ERROR = "Couldn't claim ownership of partition {}, error {}";

    private static final String BLOB_PATH_SEPARATOR = "/";
    private static final ByteBuffer UPLOAD_DATA = ByteBuffer.wrap("".getBytes(UTF_8));

    private final BlobContainerAsyncClient blobContainerAsyncClient;
    private final ClientLogger logger = new ClientLogger(BlobPartitionManager.class);
    private final Map<String, BlobAsyncClient> blobClients = new ConcurrentHashMap<>();

    /**
     * Creates an instance of BlobPartitionManager.
     *
     * @param blobContainerAsyncClient The {@link BlobContainerAsyncClient} this instance will use to read and update
     * blobs in the storage container.
     */
    public BlobPartitionManager(BlobContainerAsyncClient blobContainerAsyncClient) {
        this.blobContainerAsyncClient = blobContainerAsyncClient;
    }

    /**
     * This method is called by the {@link EventProcessor} to get the list of all existing partition ownership from the
     * Storage Blobs. Could return empty results if there are is no existing ownership information.
     *
     * @param eventHubName The Event Hub name to get ownership information.
     * @param consumerGroupName The consumer group name.
     * @return A flux of partition ownership details of all the partitions that have/had an owner.
     */
    @Override
    public Flux<PartitionOwnership> listOwnership(String eventHubName, String consumerGroupName) {
        String prefix = getBlobPrefix(eventHubName, consumerGroupName);
        BlobListDetails details = new BlobListDetails().setRetrieveMetadata(true);
        ListBlobsOptions options = new ListBlobsOptions().setPrefix(prefix).setDetails(details);
        return blobContainerAsyncClient.listBlobs(options)
            // Blob names should be of the pattern eventhub/consumergroup/<partitionId>
            // While we can further check if the partition id is numeric, it may not necessarily be the case in future.
            .filter(blobItem -> blobItem.getName().split(BLOB_PATH_SEPARATOR).length == 3)
            .map(this::convertToPartitionOwnership);
    }

    /**
     * This method is called by the {@link EventProcessor} to claim ownership of a list of partitions. This will return
     * the list of partitions that were owned successfully.
     *
     * @param requestedPartitionOwnerships Array of partition ownerships this instance is requesting to own.
     * @return A flux of partitions this instance successfully claimed ownership.
     */
    @Override
    public Flux<PartitionOwnership> claimOwnership(PartitionOwnership... requestedPartitionOwnerships) {

        return Flux.fromArray(requestedPartitionOwnerships).flatMap(partitionOwnership -> {
            String partitionId = partitionOwnership.getPartitionId();
            String blobName = getBlobName(partitionOwnership.getEventHubName(),
                partitionOwnership.getConsumerGroupName(), partitionId);

            if (!blobClients.containsKey(blobName)) {
                blobClients.put(blobName, blobContainerAsyncClient.getBlobAsyncClient(blobName));
            }

            BlobAsyncClient blobAsyncClient = blobClients.get(blobName);

            Map<String, String> metadata = new HashMap<>();
            metadata.put(OWNER_ID, partitionOwnership.getOwnerId());
            Long offset = partitionOwnership.getOffset();
            metadata.put(OFFSET, offset == null ? null : String.valueOf(offset));
            Long sequenceNumber = partitionOwnership.getSequenceNumber();
            metadata.put(SEQUENCE_NUMBER, sequenceNumber == null ? null : String.valueOf(sequenceNumber));
            BlobAccessConditions blobAccessConditions = new BlobAccessConditions();
            if (ImplUtils.isNullOrEmpty(partitionOwnership.getETag())) {
                // New blob should be created
                blobAccessConditions.setModifiedAccessConditions(new ModifiedAccessConditions().setIfNoneMatch("*"));
                return blobAsyncClient.getBlockBlobAsyncClient()
                    .uploadWithResponse(Flux.just(UPLOAD_DATA), 0, null, metadata, null, blobAccessConditions)
                    .flatMapMany(response -> updateOwnershipETag(response, partitionOwnership), error -> {
                        logger.info(CLAIM_ERROR, partitionId, error.getMessage());
                        return Mono.empty();
                    }, Mono::empty);
            } else {
                // update existing blob
                blobAccessConditions.setModifiedAccessConditions(new ModifiedAccessConditions()
                    .setIfMatch(partitionOwnership.getETag()));
                return blobAsyncClient.setMetadataWithResponse(metadata, blobAccessConditions)
                    .flatMapMany(response -> updateOwnershipETag(response, partitionOwnership), error -> {
                        logger.info(CLAIM_ERROR, partitionId, error.getMessage());
                        return Mono.empty();
                    }, Mono::empty);
            }
        });
    }

    private Mono<PartitionOwnership> updateOwnershipETag(Response<?> response, PartitionOwnership ownership) {
        return Mono.just(ownership.setETag(response.getHeaders().get(ETAG).getValue()));
    }

    /**
     * Updates the checkpoint in Storage Blobs for a partition.
     *
     * @param checkpoint Checkpoint information containing sequence number and offset to be stored for this partition.
     * @return The new ETag on successful update.
     */
    @Override
    public Mono<String> updateCheckpoint(Checkpoint checkpoint) {
        if (checkpoint.getSequenceNumber() == null && checkpoint.getOffset() == null) {
            throw logger.logExceptionAsWarning(Exceptions
                .propagate(new IllegalStateException(
                    "Both sequence number and offset cannot be null when updating a checkpoint")));
        }

        String partitionId = checkpoint.getPartitionId();
        String blobName = getBlobName(checkpoint.getEventHubName(), checkpoint.getConsumerGroupName(), partitionId);
        if (!blobClients.containsKey(blobName)) {
            blobClients.put(blobName, blobContainerAsyncClient.getBlobAsyncClient(blobName));
        }

        Map<String, String> metadata = new HashMap<>();
        String sequenceNumber = checkpoint.getSequenceNumber() == null ? null
            : String.valueOf(checkpoint.getSequenceNumber());

        String offset = checkpoint.getOffset() == null ? null : String.valueOf(checkpoint.getOffset());
        metadata.put(SEQUENCE_NUMBER, sequenceNumber);
        metadata.put(OFFSET, offset);
        metadata.put(OWNER_ID, checkpoint.getOwnerId());
        BlobAsyncClient blobAsyncClient = blobClients.get(blobName);
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions()
            .setModifiedAccessConditions(new ModifiedAccessConditions().setIfMatch(checkpoint.getETag()));

        return blobAsyncClient.setMetadataWithResponse(metadata, blobAccessConditions)
            .map(response -> response.getHeaders().get(ETAG).getValue());
    }

    private String getBlobPrefix(String eventHubName, String consumerGroupName) {
        return eventHubName + BLOB_PATH_SEPARATOR + consumerGroupName;
    }

    private String getBlobName(String eventHubName, String consumerGroupName, String partitionId) {
        return eventHubName + BLOB_PATH_SEPARATOR + consumerGroupName + BLOB_PATH_SEPARATOR + partitionId;
    }

    private PartitionOwnership convertToPartitionOwnership(BlobItem blobItem) {
        PartitionOwnership partitionOwnership = new PartitionOwnership();
        logger.info("Found blob for partition {}", blobItem.getName());

        String[] names = blobItem.getName().split(BLOB_PATH_SEPARATOR);
        partitionOwnership.setEventHubName(names[0]);
        partitionOwnership.setConsumerGroupName(names[1]);
        partitionOwnership.setPartitionId(names[2]);

        if (ImplUtils.isNullOrEmpty(blobItem.getMetadata())) {
            logger.warning("No metadata available for blob {}", blobItem.getName());
            return partitionOwnership;
        }

        blobItem.getMetadata().forEach((key, value) -> {
            switch (key) {
                case OWNER_ID:
                    partitionOwnership.setOwnerId(value);
                    break;
                case SEQUENCE_NUMBER:
                    partitionOwnership.setSequenceNumber(Long.valueOf(value));
                    break;
                case OFFSET:
                    partitionOwnership.setOffset(Long.valueOf(value));
                    break;
                default:
                    // do nothing, other metadata that we don't use
                    break;
            }
        });
        BlobItemProperties blobProperties = blobItem.getProperties();
        partitionOwnership.setLastModifiedTime(blobProperties.getLastModified().toInstant().toEpochMilli());
        partitionOwnership.setETag(blobProperties.getETag());
        return partitionOwnership;
    }

}
