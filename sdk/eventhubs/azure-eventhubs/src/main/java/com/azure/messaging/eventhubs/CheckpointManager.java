// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

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
        return Mono.empty();
    }

    /**
     * Updates a checkpoint using the given offset and sequence number
     */
    public Mono<Void> updateCheckpoint(long sequenceNumber, long offset){
        return Mono.empty();
    }
}
