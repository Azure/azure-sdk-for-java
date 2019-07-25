// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.eventprocessor.models.Checkpoint;
import com.azure.messaging.eventhubs.eventprocessor.models.PartitionContext;

import reactor.core.publisher.Mono;

public class CheckpointManager {
    private PartitionContext partitionContext;
    // The update checkpoint methods in this class will forward the request to
    // underlying partition manager
    private PartitionManager partitionManager;

    public CheckpointManager(PartitionContext partitionContext, PartitionManager partitionManager) {
        this.partitionContext = partitionContext;
        this.partitionManager = partitionManager;
    }

    /**
     * Updates a checkpoint using the event data
     */
    public Mono<Void> updateCheckpoint(EventData eventData){
        Checkpoint checkpoint = new Checkpoint()
            .consumerGroupName(partitionContext.consumerGroupName())
            .eventHubName(partitionContext.eventHubName())
            .instanceId(partitionContext.instanceId())
            .partitionId(partitionContext.partitionId())
            .sequenceNumber(eventData.sequenceNumber())
            .offset(eventData.offset());
        return this.partitionManager.updateCheckpoint(checkpoint);
    }

    /**
     * Updates a checkpoint using the given offset and sequence number
     */
    public Mono<Void> updateCheckpoint(long sequenceNumber, String offset){
        Checkpoint checkpoint = new Checkpoint()
            .consumerGroupName(partitionContext.consumerGroupName())
            .eventHubName(partitionContext.eventHubName())
            .instanceId(partitionContext.instanceId())
            .partitionId(partitionContext.partitionId())
            .sequenceNumber(sequenceNumber)
            .offset(offset);
        return this.partitionManager.updateCheckpoint(checkpoint);
    }
}
