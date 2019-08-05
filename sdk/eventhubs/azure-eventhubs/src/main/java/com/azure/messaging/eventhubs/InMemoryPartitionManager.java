// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A simple in-memory implementation of a {@link PartitionManager}.
 */
public class InMemoryPartitionManager implements PartitionManager {

    private final Map<String, PartitionOwnership> partitionOwnershipMap = new ConcurrentHashMap<>();
    private final ClientLogger logger = new ClientLogger(InMemoryPartitionManager.class);

    /**
     * {@inheritDoc}
     *
     * @param eventHubName The name of the Event Hub to list ownership of.
     * @param consumerGroupName The name of the consumer group to list ownership of.
     * @return A {@link Flux} of partition ownership information.
     */
    @Override
    public Flux<PartitionOwnership> listOwnership(String eventHubName, String consumerGroupName) {
        logger.info("Listing partition ownership");
        return Flux.fromIterable(partitionOwnershipMap.values());
    }

    /**
     * Returns a {@link Flux} of partition ownership details for successfully claimed partitions. If a partition is
     * already claimed by an instance or if the ETag in the request doesn't match the previously stored ETag, then
     * ownership claim is denied.
     *
     * @param requestedPartitionOwnerships Array of partition ownerships this instance is requesting to own.
     * @return Successfully claimed partition ownerships.
     */
    @Override
    public Flux<PartitionOwnership> claimOwnership(PartitionOwnership... requestedPartitionOwnerships) {
        return Flux.fromArray(requestedPartitionOwnerships)
            .filter(partitionOwnership -> {
                return !partitionOwnershipMap.containsKey(partitionOwnership.partitionId())
                    || partitionOwnershipMap.get(partitionOwnership.partitionId()).eTag()
                    .equals(partitionOwnership.eTag());
            })
            .doOnNext(partitionOwnership -> logger
                .info("Ownership of partition {} claimed by {}", partitionOwnership.partitionId(),
                    partitionOwnership.ownerId()))
            .map(partitionOwnership -> {
                partitionOwnership.eTag(UUID.randomUUID().toString())
                    .lastModifiedTime(System.currentTimeMillis());
                partitionOwnershipMap.put(partitionOwnership.partitionId(), partitionOwnership);
                return partitionOwnership;
            });
    }

    /**
     * Updates the in-memory storage with the provided checkpoint information.
     *
     * @param checkpoint The checkpoint containing the information to be stored in-memory.
     * @return A new ETag associated with the updated checkpoint.
     */
    @Override
    public Mono<String> updateCheckpoint(final Checkpoint checkpoint) {
        String updatedETag = UUID.randomUUID().toString();
        partitionOwnershipMap.get(checkpoint.partitionId())
            .sequenceNumber(checkpoint.sequenceNumber())
            .offset(checkpoint.offset())
            .eTag(updatedETag);
        logger.info("Updated checkpoint for partition {} with sequence number {}", checkpoint.partitionId(),
            checkpoint.sequenceNumber());
        return Mono.just(updatedETag);
    }
}
