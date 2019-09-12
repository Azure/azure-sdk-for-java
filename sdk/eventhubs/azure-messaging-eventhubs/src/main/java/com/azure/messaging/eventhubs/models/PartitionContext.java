// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.implementation.annotation.Immutable;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventProcessor;
import com.azure.messaging.eventhubs.PartitionManager;
import com.azure.messaging.eventhubs.PartitionProcessor;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import reactor.core.publisher.Mono;

/**
 * A model class to contain partition information that will be provided to each instance of {@link PartitionProcessor}.
 */
@Immutable
public class PartitionContext {

    private final String partitionId;
    private final String eventHubName;
    private final String consumerGroup;
    private final String ownerId;
    private final AtomicReference<String> eTag;
    private final PartitionManager partitionManager;

    /**
     * Creates an instance of PartitionContext that contains partition information available to each
     * {@link PartitionProcessor}.
     *
     * @param partitionId The partition id of the partition processed by the {@link PartitionProcessor}.
     * @param eventHubName The Event Hub name associated with the {@link EventProcessor}.
     * @param consumerGroup The consumer group name associated with the {@link EventProcessor}.
     * @param ownerId The unique identifier of the {@link EventProcessor} instance.
     * @param eTag The last known ETag stored in {@link PartitionManager} for this partition.
     * @param partitionManager A {@link PartitionManager} implementation to read and update partition ownership and
     * checkpoint information.
     */
    public PartitionContext(String partitionId, String eventHubName, String consumerGroup,
        String ownerId, String eTag, PartitionManager partitionManager) {
        this.partitionId = Objects.requireNonNull(partitionId, "partitionId cannot be null");
        this.eventHubName = Objects.requireNonNull(eventHubName, "eventHubName cannot be null");
        this.consumerGroup = Objects.requireNonNull(consumerGroup, "consumerGroup cannot be null");
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId cannot be null");
        this.eTag = new AtomicReference<>(eTag);
        this.partitionManager = Objects.requireNonNull(partitionManager, "partitionManager cannot be null");
    }

    /**
     * Gets the partition id associated to an instance of {@link PartitionProcessor}.
     *
     * @return The partition id associated to an instance of {@link PartitionProcessor}.
     */
    public String getPartitionId() {
        return partitionId;
    }

    /**
     * Gets the Event Hub name associated to an instance of {@link PartitionProcessor}.
     *
     * @return The Event Hub name associated to an instance of {@link PartitionProcessor}.
     */
    public String getEventHubName() {
        return eventHubName;
    }

    /**
     * Gets the consumer group name associated to an instance of {@link PartitionProcessor}.
     *
     * @return The consumer group name associated to an instance of {@link PartitionProcessor}.
     */
    public String getConsumerGroup() {
        return consumerGroup;
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
            .setConsumerGroupName(consumerGroup)
            .setEventHubName(eventHubName)
            .setOwnerId(ownerId)
            .setPartitionId(partitionId)
            .setSequenceNumber(eventData.getSequenceNumber())
            .setOffset(eventData.getOffset())
            .setETag(previousETag);
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
    public Mono<Void> updateCheckpoint(long sequenceNumber, Long offset) {
        String previousETag = this.eTag.get();
        Checkpoint checkpoint = new Checkpoint()
            .setConsumerGroupName(consumerGroup)
            .setEventHubName(eventHubName)
            .setOwnerId(ownerId)
            .setPartitionId(partitionId)
            .setSequenceNumber(sequenceNumber)
            .setOffset(offset)
            .setETag(previousETag);

        return this.partitionManager.updateCheckpoint(checkpoint)
            .map(eTag -> this.eTag.compareAndSet(previousETag, eTag))
            .then();
    }
}
