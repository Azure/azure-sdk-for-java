// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.core.http.rest.Response;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.metrics.MeterProvider;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.ListBlobsOptions;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Implementation of {@link CheckpointStore} that uses
 * <a href="https://docs.microsoft.com/en-us/azure/storage/common/storage-introduction#blob-storage">Storage Blobs</a>
 * for persisting partition ownership and checkpoint information. {@link EventProcessorClient EventProcessors} can use
 * this implementation to load balance and update checkpoints.
 *
 * @see EventProcessorClient
 */
public class BlobCheckpointStore implements CheckpointStore {

    private static final String SEQUENCE_NUMBER = "sequencenumber";
    private static final String OFFSET = "offset";
    private static final String OWNER_ID = "ownerid";
    private static final String ETAG = "eTag";

    private static final String BLOB_PATH_SEPARATOR = "/";
    private static final String CHECKPOINT_PATH = "/checkpoint/";
    private static final String OWNERSHIP_PATH = "/ownership/";

    // logging keys, consistent across all AMQP libraries and human-readable
    private static final String PARTITION_ID_LOG_KEY = "partitionId";
    private static final String OWNER_ID_LOG_KEY = "ownerId";
    private static final String SEQUENCE_NUMBER_LOG_KEY = "sequenceNumber";
    private static final String BLOB_NAME_LOG_KEY = "blobName";
    private static final String OFFSET_LOG_KEY = "offset";

    /**
     * An empty string.
     */
    public static final String EMPTY_STRING = "";

    private static final ByteBuffer UPLOAD_DATA = ByteBuffer.wrap(EMPTY_STRING.getBytes(UTF_8));
    private static final ClientLogger LOGGER = new ClientLogger(BlobCheckpointStore.class);

    private final BlobContainerAsyncClient blobContainerAsyncClient;
    private final MetricsHelper metricsHelper;
    private final Map<String, BlobAsyncClient> blobClients = new ConcurrentHashMap<>();

    /**
     * Creates an instance of BlobCheckpointStore.
     *
     * @param blobContainerAsyncClient The {@link BlobContainerAsyncClient} this instance will use to read and update
     * blobs in the storage container.
     */
    public BlobCheckpointStore(BlobContainerAsyncClient blobContainerAsyncClient) {
        this(blobContainerAsyncClient, null);
    }


    /**
     * Creates an instance of BlobCheckpointStore.
     *
     * @param blobContainerAsyncClient The {@link BlobContainerAsyncClient} this instance will use to read and update
     * @param options The {@link ClientOptions} to configure this instance.
     * blobs in the storage container.
     */
    public BlobCheckpointStore(BlobContainerAsyncClient blobContainerAsyncClient, ClientOptions options) {
        this.blobContainerAsyncClient = blobContainerAsyncClient;
        this.metricsHelper = new MetricsHelper(options == null ? null : options.getMetricsOptions(), MeterProvider.getDefaultProvider());
    }

    /**
     * This method is called by the {@link EventProcessorClient} to get the list of all existing partition ownership
     * from the Storage Blobs. Could return empty results if there are is no existing ownership information.
     *
     * @param eventHubName The Event Hub name to get ownership information.
     * @param consumerGroup The consumer group name.
     * @return A flux of partition ownership details of all the partitions that have/had an owner.
     */
    @Override
    public Flux<PartitionOwnership> listOwnership(String fullyQualifiedNamespace, String eventHubName,
        String consumerGroup) {
        String prefix = getBlobPrefix(fullyQualifiedNamespace, eventHubName, consumerGroup, OWNERSHIP_PATH);
        return listBlobs(prefix, this::convertToPartitionOwnership);
    }

    @Override
    public Flux<Checkpoint> listCheckpoints(String fullyQualifiedNamespace, String eventHubName,
        String consumerGroup) {
        String prefix = getBlobPrefix(fullyQualifiedNamespace, eventHubName, consumerGroup, CHECKPOINT_PATH);
        return listBlobs(prefix, this::convertToCheckpoint);
    }

    private <T> Flux<T> listBlobs(String prefix, Function<BlobItem, Mono<T>> converter) {
        BlobListDetails details = new BlobListDetails().setRetrieveMetadata(true);
        ListBlobsOptions options = new ListBlobsOptions().setPrefix(prefix).setDetails(details);
        return blobContainerAsyncClient.listBlobs(options)
            .flatMap(converter)
            .filter(Objects::nonNull);
    }

    private Mono<Checkpoint> convertToCheckpoint(BlobItem blobItem) {
        String[] names = blobItem.getName().split(BLOB_PATH_SEPARATOR);
        LOGGER.atVerbose()
            .addKeyValue(BLOB_NAME_LOG_KEY, blobItem.getName())
            .log(Messages.FOUND_BLOB_FOR_PARTITION);
        if (names.length == 5) {
            // Blob names should be of the pattern
            // fullyqualifiednamespace/eventhub/consumergroup/checkpoints/<partitionId>
            // While we can further check if the partition id is numeric, it may not necessarily be the case in future.

            if (CoreUtils.isNullOrEmpty(blobItem.getMetadata())) {
                LOGGER.atWarning()
                    .addKeyValue(BLOB_NAME_LOG_KEY, blobItem.getName())
                    .log(Messages.NO_METADATA_AVAILABLE_FOR_BLOB);
                return Mono.empty();
            }

            Map<String, String> metadata = blobItem.getMetadata();
            LOGGER.atVerbose()
                .addKeyValue(BLOB_NAME_LOG_KEY, blobItem.getName())
                .addKeyValue(SEQUENCE_NUMBER_LOG_KEY, metadata.get(SEQUENCE_NUMBER))
                .addKeyValue(OFFSET_LOG_KEY, metadata.get(OFFSET))
                .log(Messages.CHECKPOINT_INFO);

            Long sequenceNumber = null;
            Long offset = null;
            if (!CoreUtils.isNullOrEmpty(metadata.get(SEQUENCE_NUMBER))) {
                sequenceNumber = Long.parseLong(metadata.get(SEQUENCE_NUMBER));
            }

            if (!CoreUtils.isNullOrEmpty(metadata.get(OFFSET))) {
                offset = Long.parseLong(metadata.get(OFFSET));
            }

            Checkpoint checkpoint = new Checkpoint()
                .setFullyQualifiedNamespace(names[0])
                .setEventHubName(names[1])
                .setConsumerGroup(names[2])
                // names[3] is "checkpoint"
                .setPartitionId(names[4])
                .setSequenceNumber(sequenceNumber)
                .setOffset(offset);

            return Mono.just(checkpoint);
        }
        return Mono.empty();
    }

    /**
     * This method is called by the {@link EventProcessorClient} to claim ownership of a list of partitions. This will
     * return the list of partitions that were owned successfully.
     *
     * @param requestedPartitionOwnerships List of partition ownerships this instance is requesting to own.
     * @return A flux of partitions this instance successfully claimed ownership.
     */
    @Override
    public Flux<PartitionOwnership> claimOwnership(List<PartitionOwnership> requestedPartitionOwnerships) {

        return Flux.fromIterable(requestedPartitionOwnerships).flatMap(partitionOwnership -> {
            try {
                String partitionId = partitionOwnership.getPartitionId();
                String blobName = getBlobName(partitionOwnership.getFullyQualifiedNamespace(),
                    partitionOwnership.getEventHubName(), partitionOwnership.getConsumerGroup(), partitionId,
                    OWNERSHIP_PATH);

                if (!blobClients.containsKey(blobName)) {
                    blobClients.put(blobName, blobContainerAsyncClient.getBlobAsyncClient(blobName));
                }

                BlobAsyncClient blobAsyncClient = blobClients.get(blobName);

                Map<String, String> metadata = new HashMap<>();
                metadata.put(OWNER_ID, partitionOwnership.getOwnerId());

                BlobRequestConditions blobRequestConditions = new BlobRequestConditions();
                if (CoreUtils.isNullOrEmpty(partitionOwnership.getETag())) {
                    // New blob should be created
                    blobRequestConditions.setIfNoneMatch("*");
                    return blobAsyncClient.getBlockBlobAsyncClient()
                        .uploadWithResponse(Flux.just(UPLOAD_DATA), 0, null, metadata, null, null,
                            blobRequestConditions)
                        .flatMapMany(response -> updateOwnershipETag(response, partitionOwnership), error -> {
                            LOGGER.atVerbose()
                                .addKeyValue(PARTITION_ID_LOG_KEY, partitionId)
                                .log(Messages.CLAIM_ERROR, error);
                            return Mono.error(error);
                        }, Mono::empty);
                } else {
                    // update existing blob
                    blobRequestConditions.setIfMatch(partitionOwnership.getETag());
                    return blobAsyncClient.setMetadataWithResponse(metadata, blobRequestConditions)
                        .flatMapMany(response -> updateOwnershipETag(response, partitionOwnership), error -> {
                            LOGGER.atVerbose()
                                .addKeyValue(PARTITION_ID_LOG_KEY, partitionId)
                                .log(Messages.CLAIM_ERROR, error);
                            return Mono.error(error);
                        }, Mono::empty);
                }
            } catch (Exception ex) {
                LOGGER.atWarning()
                    .addKeyValue(PARTITION_ID_LOG_KEY, partitionOwnership.getPartitionId())
                    .log(Messages.CLAIM_ERROR, ex);
                return Mono.error(ex);
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
    public Mono<Void> updateCheckpoint(Checkpoint checkpoint) {
        if (checkpoint == null || (checkpoint.getSequenceNumber() == null && checkpoint.getOffset() == null)) {
            throw LOGGER.logExceptionAsWarning(Exceptions
                .propagate(new IllegalStateException(
                    "Both sequence number and offset cannot be null when updating a checkpoint")));
        }

        String partitionId = checkpoint.getPartitionId();
        String blobName = getBlobName(checkpoint.getFullyQualifiedNamespace(), checkpoint.getEventHubName(),
            checkpoint.getConsumerGroup(), partitionId, CHECKPOINT_PATH);
        if (!blobClients.containsKey(blobName)) {
            blobClients.put(blobName, blobContainerAsyncClient.getBlobAsyncClient(blobName));
        }

        Map<String, String> metadata = new HashMap<>();
        String sequenceNumber = checkpoint.getSequenceNumber() == null ? null
            : String.valueOf(checkpoint.getSequenceNumber());

        String offset = checkpoint.getOffset() == null ? null : String.valueOf(checkpoint.getOffset());
        metadata.put(SEQUENCE_NUMBER, sequenceNumber);
        metadata.put(OFFSET, offset);
        BlobAsyncClient blobAsyncClient = blobClients.get(blobName);

        return blobAsyncClient.exists().flatMap(exists -> {
            if (exists) {
                return blobAsyncClient.setMetadata(metadata);
            } else {
                return blobAsyncClient.getBlockBlobAsyncClient().uploadWithResponse(Flux.just(UPLOAD_DATA), 0, null,
                    metadata, null, null, null).then();
            }
        })
        .doOnEach(signal -> {
            if (signal.isOnComplete() || signal.isOnError()) {
                metricsHelper.reportCheckpoint(checkpoint, blobName, !signal.hasError());
            }
        });
    }

    private String getBlobPrefix(String fullyQualifiedNamespace, String eventHubName, String consumerGroupName,
        String typeSuffix) {
        return fullyQualifiedNamespace + BLOB_PATH_SEPARATOR + eventHubName + BLOB_PATH_SEPARATOR + consumerGroupName
            + typeSuffix;
    }

    private String getBlobName(String fullyQualifiedNamespace, String eventHubName, String consumerGroupName,
        String partitionId, String typeSuffix) {
        return fullyQualifiedNamespace + BLOB_PATH_SEPARATOR + eventHubName + BLOB_PATH_SEPARATOR + consumerGroupName
            + typeSuffix + partitionId;
    }

    private Mono<PartitionOwnership> convertToPartitionOwnership(BlobItem blobItem) {
        LOGGER.atVerbose()
            .addKeyValue(BLOB_NAME_LOG_KEY, blobItem.getName())
            .log(Messages.FOUND_BLOB_FOR_PARTITION);

        String[] names = blobItem.getName().split(BLOB_PATH_SEPARATOR);
        if (names.length == 5) {
            // Blob names should be of the pattern
            // fullyqualifiednamespace/eventhub/consumergroup/ownership/<partitionId>
            // While we can further check if the partition id is numeric, it may not necessarily be the case in future.
            if (CoreUtils.isNullOrEmpty(blobItem.getMetadata())) {
                LOGGER.atWarning()
                    .addKeyValue(BLOB_NAME_LOG_KEY, blobItem.getName())
                    .log(Messages.NO_METADATA_AVAILABLE_FOR_BLOB);
                return Mono.empty();
            }

            BlobItemProperties blobProperties = blobItem.getProperties();

            String ownerId = blobItem.getMetadata().getOrDefault(OWNER_ID, EMPTY_STRING);
            if (ownerId == null) {
                ownerId = EMPTY_STRING;
            }

            LOGGER.atVerbose()
                .addKeyValue(BLOB_NAME_LOG_KEY, blobItem.getName())
                .addKeyValue(OWNER_ID_LOG_KEY, ownerId)
                .log(Messages.BLOB_OWNER_INFO);

            PartitionOwnership partitionOwnership = new PartitionOwnership()
                .setFullyQualifiedNamespace(names[0])
                .setEventHubName(names[1])
                .setConsumerGroup(names[2])
                // names[3] is "ownership"
                .setPartitionId(names[4])
                .setOwnerId(ownerId)
                .setLastModifiedTime(blobProperties.getLastModified().toInstant().toEpochMilli())
                .setETag(blobProperties.getETag());
            return Mono.just(partitionOwnership);
        }

        return Mono.empty();
    }

}
