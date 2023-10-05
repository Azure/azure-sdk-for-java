// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.core.http.HttpHeaderName;
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
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.ListBlobsOptions;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

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

    static final String SEQUENCE_NUMBER = "sequencenumber";
    static final String OFFSET = "offset";
    static final String OWNER_ID = "ownerid";
    private static final String ETAG = "eTag";

    static final String BLOB_PATH_SEPARATOR = "/";
    static final String CHECKPOINT_PATH = "/checkpoint/";
    static final String OWNERSHIP_PATH = "/ownership/";

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

        final String prefix = getBlobPrefix(fullyQualifiedNamespace, eventHubName, consumerGroup, OWNERSHIP_PATH,
                true);

        // If there are no existing blobs in the all-lower case format, try legacy format.
        return listBlobs(prefix, (blobItem, parts) -> convertToPartitionOwnership(blobItem, parts))
            .switchIfEmpty(Flux.defer(() -> {
                final String legacyPrefix = getBlobPrefix(fullyQualifiedNamespace, eventHubName, consumerGroup,
                        OWNERSHIP_PATH, false);

                return listBlobs(legacyPrefix, (item, p) -> convertToPartitionOwnership(item, p));
            }))
            .map(ownership -> {
                // The ones from the ownership blob path will be lowercase, so map these to the actual values.
                return ownership.setConsumerGroup(consumerGroup)
                    .setEventHubName(eventHubName)
                    .setFullyQualifiedNamespace(fullyQualifiedNamespace);
            });
    }

    @Override
    public Flux<Checkpoint> listCheckpoints(String fullyQualifiedNamespace, String eventHubName,
        String consumerGroup) {
        final String prefix = getBlobPrefix(fullyQualifiedNamespace, eventHubName, consumerGroup, CHECKPOINT_PATH,
                true);

        // If there are no existing blobs in the all-lower case format, try legacy format.
        return listBlobs(prefix, (blobItem, names) -> convertToCheckpoint(blobItem, names))
                .switchIfEmpty(Flux.defer(() -> {
                    final String legacyPrefix = getBlobPrefix(fullyQualifiedNamespace, eventHubName, consumerGroup,
                            OWNERSHIP_PATH, false);

                    return listBlobs(legacyPrefix, (item, p) -> convertToCheckpoint(item, p));
                }));
    }

    private <T> Flux<T> listBlobs(String prefix, BiFunction<BlobItem, String[], T> converter) {
        BlobListDetails details = new BlobListDetails().setRetrieveMetadata(true);
        ListBlobsOptions options = new ListBlobsOptions().setPrefix(prefix).setDetails(details);

        return blobContainerAsyncClient.listBlobs(options)
                .handle((blobItem, sink) -> {
                    final String[] names = blobItem.getName().split(BLOB_PATH_SEPARATOR);

                    if (names.length != 5) {
                        LOGGER.atWarning()
                                .addKeyValue(BLOB_NAME_LOG_KEY, blobItem.getName())
                                .log(Messages.INVALID_BLOB_NAME);
                        return;
                    }

                    // Blob names should be of the pattern
                    // <fullyQualifiedNamespace>/<eventHubName>/<consumerGroup>/ownership/<partitionId>
                    // or
                    // <fullyQualifiedNamespace>/<eventHubName>/<consumerGroup>/checkpoint/<partitionId>
                    if (CoreUtils.isNullOrEmpty(blobItem.getMetadata())) {
                        LOGGER.atWarning()
                                .addKeyValue(BLOB_NAME_LOG_KEY, blobItem.getName())
                                .log(Messages.NO_METADATA_AVAILABLE_FOR_BLOB);
                        return;
                    }

                    final T converted = converter.apply(blobItem, names);

                    if (!Objects.isNull(converted)) {
                        sink.next(converted);
                    }
                });
    }

    private static Checkpoint convertToCheckpoint(BlobItem blobItem, String[] names) {
        final Map<String, String> metadata = blobItem.getMetadata();

        Long sequenceNumber = null;
        Long offset = null;
        if (!CoreUtils.isNullOrEmpty(metadata.get(SEQUENCE_NUMBER))) {
            sequenceNumber = Long.parseLong(metadata.get(SEQUENCE_NUMBER));
        }

        if (!CoreUtils.isNullOrEmpty(metadata.get(OFFSET))) {
            offset = Long.parseLong(metadata.get(OFFSET));
        }

        return new Checkpoint()
            .setFullyQualifiedNamespace(names[0])
            .setEventHubName(names[1])
            .setConsumerGroup(names[2])
            // names[3] is "checkpoint"
            .setPartitionId(names[4])
            .setSequenceNumber(sequenceNumber)
            .setOffset(offset);
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
                final String partitionId = partitionOwnership.getPartitionId();
                final String blobName = getBlobName(partitionOwnership.getFullyQualifiedNamespace(),
                        partitionOwnership.getEventHubName(), partitionOwnership.getConsumerGroup(), partitionId,
                        OWNERSHIP_PATH, true);
                final BlobRequestConditions blobRequestConditions = new BlobRequestConditions();

                final Map<String, String> metadata = new HashMap<>();
                metadata.put(OWNER_ID, partitionOwnership.getOwnerId());

                // Blob clients are only added to the map when they have been used to create/update a blob.  So, we
                // always update in this case.
                if (blobClients.containsKey(blobName)) {
                    final BlobAsyncClient blobAsyncClient = blobClients.get(blobName);

                    blobRequestConditions.setIfMatch(partitionOwnership.getETag());
                    return updateBlob(blobAsyncClient, metadata, blobRequestConditions, partitionOwnership);
                }

                // New blob should be created.  It was not in the existing blobClients map, so we have to create it.
                if (CoreUtils.isNullOrEmpty(partitionOwnership.getETag())) {
                    final BlobAsyncClient client = blobClients.compute(blobName, (key, existing) -> {
                        if (existing != null) {
                            return existing;
                        }

                        return blobContainerAsyncClient.getBlobAsyncClient(blobName);
                    });

                    blobRequestConditions.setIfNoneMatch("*");

                    return client.getBlockBlobAsyncClient()
                        .uploadWithResponse(Flux.just(UPLOAD_DATA), 0, null, metadata, null, null,
                            blobRequestConditions)
                        .map(response -> updateOwnershipETag(response, partitionOwnership))
                        .onErrorResume(error -> {
                            LOGGER.atVerbose()
                                    .addKeyValue(PARTITION_ID_LOG_KEY, partitionId)
                                    .log(Messages.CLAIM_ERROR, error);
                            return Mono.<PartitionOwnership>empty();
                        });
                }

                // Update existing blob.  Try to update with the new blob name (lowercase), and if that does not exist,
                // then try updating with legacy blob name.  If neither work, then log a warning and return normally.
                blobRequestConditions.setIfMatch(partitionOwnership.getETag());

                final BlobAsyncClient blobClient = blobContainerAsyncClient.getBlobAsyncClient(blobName);

                return blobClient.exists().flatMap(exists -> {
                    if (exists) {
                        blobClients.put(blobName, blobClient);
                        return Mono.just(blobClient);
                    }

                    final String legacyBlobName = getBlobName(partitionOwnership.getFullyQualifiedNamespace(),
                            partitionOwnership.getEventHubName(), partitionOwnership.getConsumerGroup(), partitionId,
                            OWNERSHIP_PATH, false);
                    final BlobAsyncClient legacyBlobClient =
                            blobContainerAsyncClient.getBlobAsyncClient(legacyBlobName);

                    return legacyBlobClient.exists().handle((doesExist, sink) -> {
                        if (doesExist) {
                            blobClients.put(blobName, legacyBlobClient);
                            sink.next(legacyBlobClient);
                        } else {
                            LOGGER.atWarning()
                                    .addKeyValue(BLOB_NAME_LOG_KEY, blobName)
                                    .addKeyValue(PARTITION_ID_LOG_KEY, partitionOwnership.getPartitionId())
                                    .log(Messages.CLAIM_ERROR
                                            + ".  No existing blob with current or legacy blob name.");
                            sink.complete();
                        }
                    });
                }).flatMap(client -> {
                    return updateBlob(client, metadata, blobRequestConditions, partitionOwnership);
                });
            } catch (Exception ex) {
                LOGGER.atWarning()
                    .addKeyValue(PARTITION_ID_LOG_KEY, partitionOwnership.getPartitionId())
                    .log(Messages.CLAIM_ERROR, ex);
                return Mono.empty();
            }
        });
    }

    private static PartitionOwnership updateOwnershipETag(Response<?> response, PartitionOwnership ownership) {
        return ownership.setETag(response.getHeaders().get(HttpHeaderName.ETAG).getValue());
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

        final String sequenceNumber = checkpoint.getSequenceNumber() == null
                ? null
                : String.valueOf(checkpoint.getSequenceNumber());
        final String offset = checkpoint.getOffset() == null
                ? null
                : String.valueOf(checkpoint.getOffset());

        final Map<String, String> metadata = new HashMap<>();
        metadata.put(SEQUENCE_NUMBER, sequenceNumber);
        metadata.put(OFFSET, offset);

        final String partitionId = checkpoint.getPartitionId();
        final String blobName = getBlobName(checkpoint.getFullyQualifiedNamespace(), checkpoint.getEventHubName(),
            checkpoint.getConsumerGroup(), partitionId, CHECKPOINT_PATH, true);

        // Blob clients are only added to map when we create an entry for them.
        if (blobClients.containsKey(blobName)) {
            final BlobAsyncClient blobAsyncClient = blobClients.get(blobName);
            final Mono<Void> response = updateBlob(blobAsyncClient, metadata, true);

            return reportMetrics(response, checkpoint, blobName);
        }

        // The checkpoint either:
        // 1. Legacy format (keeps existing casing)
        // 2. Does not exist.
        final String legacyBlobName = getBlobName(checkpoint.getFullyQualifiedNamespace(),
                checkpoint.getEventHubName(), checkpoint.getConsumerGroup(), partitionId, CHECKPOINT_PATH, false);
        final BlobAsyncClient legacyBlobClient = blobContainerAsyncClient.getBlobAsyncClient(legacyBlobName);

        final Mono<Void> response = legacyBlobClient.exists().flatMap(exists -> {
            // The checkpoint is in legacy format.
            if (exists) {
                // Store it as the correct (all-lowercase) blob name, but the blob client will update the legacy
                // checkpoint.  This way we do not have to do check for existence of both legacy and new one every
                // iteration.
                blobClients.put(blobName, legacyBlobClient);

                return updateBlob(legacyBlobClient, metadata, false);
            } else {
                // Does not exist at all.  Create a new client and update the checkpoint.
                final BlobAsyncClient blobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(blobName);

                blobClients.put(blobName, blobAsyncClient);

                return blobAsyncClient.getBlockBlobAsyncClient()
                        .uploadWithResponse(Flux.just(UPLOAD_DATA), 0, null, metadata, null,
                                null, null)
                        .then();
            }
        });

        return reportMetrics(response, checkpoint, blobName);
    }

    private static Mono<Void> updateBlob(BlobAsyncClient client, Map<String, String> metadata,
            boolean createIfNotExists) {
        return client.setMetadata(metadata).onErrorResume(error -> {
            if (!(error instanceof BlobStorageException)) {
                return false;
            }

            return ((BlobStorageException) error).getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND
                    && createIfNotExists;
        }, error -> {
            return client.getBlockBlobAsyncClient()
                    .uploadWithResponse(Flux.just(UPLOAD_DATA), 0, null, metadata, null,
                            null, null)
                    .then();
        });
    }

    private static Mono<PartitionOwnership> updateBlob(BlobAsyncClient client, Map<String, String> metadata,
            BlobRequestConditions blobRequestConditions, PartitionOwnership partitionOwnership) {

        return client.setMetadataWithResponse(metadata, blobRequestConditions)
                .map(response -> {
                    return updateOwnershipETag(response, partitionOwnership);
                }).onErrorResume(error -> {
                    LOGGER.atVerbose()
                            .addKeyValue(PARTITION_ID_LOG_KEY, partitionOwnership.getPartitionId())
                            .log(Messages.CLAIM_ERROR, error);
                    return Mono.empty();
                });
    }

    private Mono<Void> reportMetrics(Mono<Void> checkpointMono, Checkpoint checkpoint, String blobName) {
        AtomicReference<Instant> startTime = metricsHelper.isCheckpointDurationEnabled() ? new AtomicReference<>() : null;
        return checkpointMono
            .doOnEach(signal ->  {
                if (signal.isOnComplete() || signal.isOnError()) {
                    metricsHelper.reportCheckpoint(checkpoint,
                            blobName,
                            !signal.hasError(),
                            startTime != null ? startTime.get() : null);
                }
            })
            .doOnSubscribe(ignored -> {
                if (startTime != null) {
                    startTime.set(Instant.now());
                }
            });
    }

    private static String getBlobPrefix(String fullyQualifiedNamespace, String eventHubName, String consumerGroupName,
        String typeSuffix, boolean isLowercase) {

        final String prefix = fullyQualifiedNamespace + BLOB_PATH_SEPARATOR + eventHubName + BLOB_PATH_SEPARATOR
            + consumerGroupName + typeSuffix;

        return isLowercase ? prefix.toLowerCase(Locale.ROOT) : prefix;
    }

    private static String getBlobName(String fullyQualifiedNamespace, String eventHubName, String consumerGroupName,
        String partitionId, String typeSuffix, boolean isLowercase) {
        final String name = fullyQualifiedNamespace + BLOB_PATH_SEPARATOR + eventHubName + BLOB_PATH_SEPARATOR
                + consumerGroupName + typeSuffix + partitionId;

        return isLowercase ? name.toLowerCase(Locale.ROOT) : name;
    }

    private static PartitionOwnership convertToPartitionOwnership(BlobItem blobItem, String[] names) {
        BlobItemProperties blobProperties = blobItem.getProperties();

        String ownerId = blobItem.getMetadata().getOrDefault(OWNER_ID, EMPTY_STRING);
        if (ownerId == null) {
            ownerId = EMPTY_STRING;
        }

        return new PartitionOwnership()
            .setFullyQualifiedNamespace(names[0])
            .setEventHubName(names[1])
            .setConsumerGroup(names[2])
            // names[3] is "ownership"
            .setPartitionId(names[4])
            .setOwnerId(ownerId)
            .setLastModifiedTime(blobProperties.getLastModified().toInstant().toEpochMilli())
            .setETag(blobProperties.getETag());
    }
}
