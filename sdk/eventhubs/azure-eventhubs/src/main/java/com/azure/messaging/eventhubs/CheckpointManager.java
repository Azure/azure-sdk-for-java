// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionContext;

import java.util.concurrent.atomic.AtomicReference;
import reactor.core.publisher.Mono;

/**
 * The checkpoint manager that clients should use to update checkpoints to track progress of events processed. Each
 * instance of a {@link PartitionProcessor} will be provided with it's own instance of a CheckpointManager.
 */
public class CheckpointManager {

    private final PartitionContext partitionContext;
    private final PartitionManager partitionManager;
    private final AtomicReference<String> eTag;
    private final String ownerId;

    /**
     * Creates a new checkpoint manager which {@link PartitionProcessor} can use to update checkpoints.
     *
     * @param ownerId The event processor identifier that is responsible for updating checkpoints.
     * @param partitionContext The partition context providing necessary partition and event hub information for updating
     * checkpoints.
     * @param partitionManager The {@link PartitionManager} implementation that will be store the checkpoint information.
     * @param eTag The last known ETag stored in {@link PartitionManager} for this partition. When the  update checkpoint
     * is called from this CheckpointManager, this ETag will be used to provide <a href="https://en.wikipedia.org/wiki/Optimistic_concurrency_control">optimistic
     * concurrency</a>.
     */
    CheckpointManager(String ownerId, PartitionContext partitionContext, PartitionManager partitionManager,
        String eTag) {
        this.ownerId = ownerId;
        this.partitionContext = partitionContext;
        this.partitionManager = partitionManager;
        this.eTag = new AtomicReference<>(eTag);
    }

    /**
     * Updates the checkpoint for this partition using the event data. This will serve as the last known successfully
     * processed event in this partition if the update is successful.
     *
     * @param eventData The event data to use for updating the checkpoint.
     * @return a representation of deferred execution of this call.
     */
    public Mono<Void> updateCheckpoint(EventData eventData) {
        String previousETag = this.eTag.get();
        Checkpoint checkpoint = new Checkpoint()
            .consumerGroupName(partitionContext.consumerGroupName())
            .eventHubName(partitionContext.eventHubName())
            .ownerId(ownerId)
            .partitionId(partitionContext.partitionId())
            .sequenceNumber(eventData.sequenceNumber())
            .offset(eventData.offset())
            .eTag(previousETag);
        return this.partitionManager.updateCheckpoint(checkpoint)
            .map(eTag -> this.eTag.compareAndSet(previousETag, eTag))
            .then();
    }

    /**
     * Updates a checkpoint using the given offset and sequence number. This will serve as the last known successfully
     * processed event in this partition if the update is successful.
     *
     * @param sequenceNumber The sequence number to update the checkpoint.
     * @param offset The offset to update the checkpoint.
     * @return a representation of deferred execution of this call.
     */
    public Mono<Void> updateCheckpoint(long sequenceNumber, String offset) {
        String previousETag = this.eTag.get();
        Checkpoint checkpoint = new Checkpoint()
            .consumerGroupName(partitionContext.consumerGroupName())
            .eventHubName(partitionContext.eventHubName())
            .ownerId(ownerId)
            .partitionId(partitionContext.partitionId())
            .sequenceNumber(sequenceNumber)
            .offset(offset)
            .eTag(previousETag);

        return this.partitionManager.updateCheckpoint(checkpoint)
            .map(eTag -> this.eTag.compareAndSet(previousETag, eTag))
            .then();
    }
}
