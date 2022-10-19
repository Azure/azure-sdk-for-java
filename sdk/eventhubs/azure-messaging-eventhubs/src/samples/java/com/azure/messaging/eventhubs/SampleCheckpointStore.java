// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import java.util.List;
import java.util.Locale;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.messaging.eventhubs.implementation.ClientConstants.OWNER_ID_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.PARTITION_ID_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.SEQUENCE_NUMBER_KEY;

/**
 * A simple in-memory implementation of a {@link CheckpointStore}. This implementation keeps track of partition
 * ownership details including checkpointing information in-memory. Using this implementation will only facilitate
 * checkpointing and load balancing of Event Processors running within this process.
 */
public class SampleCheckpointStore implements CheckpointStore {

    private static final String OWNERSHIP = "ownership";
    private static final String SEPARATOR = "/";
    private static final String CHECKPOINT = "checkpoint";
    private final Map<String, PartitionOwnership> partitionOwnershipMap = new ConcurrentHashMap<>();
    private final Map<String, Checkpoint> checkpointsMap = new ConcurrentHashMap<>();
    private static final ClientLogger LOGGER = new ClientLogger(SampleCheckpointStore.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<PartitionOwnership> listOwnership(String fullyQualifiedNamespace, String eventHubName,
        String consumerGroup) {
        LOGGER.info("Listing partition ownership");

        String prefix = prefixBuilder(fullyQualifiedNamespace, eventHubName, consumerGroup, OWNERSHIP);
        return Flux.fromIterable(partitionOwnershipMap.keySet())
            .filter(key -> key.startsWith(prefix))
            .map(key -> partitionOwnershipMap.get(key));
    }

    private String prefixBuilder(String fullyQualifiedNamespace, String eventHubName, String consumerGroup,
        String type) {
        return new StringBuilder()
            .append(fullyQualifiedNamespace)
            .append(SEPARATOR)
            .append(eventHubName)
            .append(SEPARATOR)
            .append(consumerGroup)
            .append(SEPARATOR)
            .append(type)
            .toString()
            .toLowerCase(Locale.ROOT);
    }

    /**
     * Returns a {@link Flux} of partition ownership details for successfully claimed partitions. If a partition is
     * already claimed by an instance or if the ETag in the request doesn't match the previously stored ETag, then
     * ownership claim is denied.
     *
     * @param requestedPartitionOwnerships List of partition ownerships this instance is requesting to own.
     * @return Successfully claimed partition ownerships.
     */
    @Override
    public Flux<PartitionOwnership> claimOwnership(List<PartitionOwnership> requestedPartitionOwnerships) {
        if (CoreUtils.isNullOrEmpty(requestedPartitionOwnerships)) {
            return Flux.empty();
        }
        PartitionOwnership firstEntry = requestedPartitionOwnerships.get(0);
        String prefix = prefixBuilder(firstEntry.getFullyQualifiedNamespace(), firstEntry.getEventHubName(),
            firstEntry.getConsumerGroup(), OWNERSHIP);

        return Flux.fromIterable(requestedPartitionOwnerships)
            .filter(partitionOwnership -> {
                return !partitionOwnershipMap.containsKey(partitionOwnership.getPartitionId())
                    || partitionOwnershipMap.get(partitionOwnership.getPartitionId()).getETag()
                    .equals(partitionOwnership.getETag());
            })
            .doOnNext(partitionOwnership ->
                LOGGER.atInfo()
                    .addKeyValue(PARTITION_ID_KEY, partitionOwnership.getPartitionId())
                    .addKeyValue(OWNER_ID_KEY, partitionOwnership.getOwnerId())
                    .log("Ownership claimed."))
            .map(partitionOwnership -> {
                partitionOwnership.setETag(UUID.randomUUID().toString())
                    .setLastModifiedTime(System.currentTimeMillis());
                partitionOwnershipMap.put(prefix + SEPARATOR + partitionOwnership.getPartitionId(), partitionOwnership);
                return partitionOwnership;
            });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<Checkpoint> listCheckpoints(String fullyQualifiedNamespace, String eventHubName,
        String consumerGroup) {
        String prefix = prefixBuilder(fullyQualifiedNamespace, eventHubName, consumerGroup, CHECKPOINT);
        return Flux.fromIterable(checkpointsMap.keySet())
            .filter(key -> key.startsWith(prefix))
            .map(key -> checkpointsMap.get(key));
    }

    /**
     * Updates the in-memory storage with the provided checkpoint information.
     *
     * @param checkpoint The checkpoint containing the information to be stored in-memory.
     * @return A {@link Mono} that completes when the checkpoint is updated.
     */
    @Override
    public Mono<Void> updateCheckpoint(Checkpoint checkpoint) {
        if (checkpoint == null) {
            return Mono.error(LOGGER.logExceptionAsError(new NullPointerException("checkpoint cannot be null")));
        }

        String prefix = prefixBuilder(checkpoint.getFullyQualifiedNamespace(), checkpoint.getEventHubName(),
            checkpoint.getConsumerGroup(), CHECKPOINT);
        checkpointsMap.put(prefix + SEPARATOR + checkpoint.getPartitionId(), checkpoint);
        LOGGER.atInfo()
            .addKeyValue(PARTITION_ID_KEY, checkpoint.getPartitionId())
            .addKeyValue(SEQUENCE_NUMBER_KEY, checkpoint.getSequenceNumber())
            .log("Updated checkpoint.");
        return Mono.empty();
    }
}
