// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.eventprocessor.models.Checkpoint;
import com.azure.messaging.eventhubs.eventprocessor.models.PartitionContext;

import reactor.core.publisher.Mono;

/**
 * The checkpoint manager that clients should use to update checkpoints to track progress of events processed.
 */
public class CheckpointManager {

    private final PartitionContext partitionContext;
    private final PartitionManager partitionManager;
    private String eTag;

    /**
     * Creates a new checkpoint manager which clients of {@link EventProcessorAsyncClient} can use to update checkpoints
     * of a partition.
     *
     * @param partitionContext The partition context.
     * @param partitionManager The partition manager.
     * @param eTag The last known eTag for this partition.
     */
    CheckpointManager(PartitionContext partitionContext, PartitionManager partitionManager,
        String eTag) {
        this.partitionContext = partitionContext;
        this.partitionManager = partitionManager;
        this.eTag = eTag;
    }

    /**
     * Updates a checkpoint using the event data.
     *
     * @param eventData The event data to use for updating the checkpoint.
     * @return A mono void.
     */
    public Mono<Void> updateCheckpoint(EventData eventData) {
        Checkpoint checkpoint = new Checkpoint()
            .consumerGroupName(partitionContext.consumerGroupName())
            .eventHubName(partitionContext.eventHubName())
            .instanceId(partitionContext.instanceId())
            .partitionId(partitionContext.partitionId())
            .sequenceNumber(eventData.sequenceNumber())
            .offset(eventData.offset())
            .eTag(eTag);

        return this.partitionManager.updateCheckpoint(checkpoint).map(eTag -> this.eTag = eTag)
            .then();
    }

    /**
     * Updates a checkpoint using the given offset and sequence number.
     *
     * @param sequenceNumber The sequence number to update the checkpoint.
     * @param offset The offset to update the checkpoint.
     * @return A mono void.
     */
    public Mono<Void> updateCheckpoint(long sequenceNumber, String offset) {
        Checkpoint checkpoint = new Checkpoint()
            .consumerGroupName(partitionContext.consumerGroupName())
            .eventHubName(partitionContext.eventHubName())
            .instanceId(partitionContext.instanceId())
            .partitionId(partitionContext.partitionId())
            .sequenceNumber(sequenceNumber)
            .offset(offset)
            .eTag(eTag);

        return this.partitionManager.updateCheckpoint(checkpoint).flatMap(eTag -> {
            this.eTag = eTag;
            return Mono.empty();
        });
    }
}
