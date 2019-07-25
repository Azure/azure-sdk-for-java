// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.eventprocessor.models.Checkpoint;
import com.azure.messaging.eventhubs.eventprocessor.models.PartitionOwnership;

import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PartitionManager {

    /**
     * Called to get the list of all existing partition ownership from the underlying data store.
     * Could return empty results if there are is no existing ownership information.
     */
    Flux<PartitionOwnership> listOwnership(String eventHubName, String consumerGroupName);

    /**
     * Called to claim ownership of a list of partitions. This will return the list of
     * partitions that were owned successfully.
     * @param requestedPartitionOwnerships
     * @return
     */
    Flux<PartitionOwnership> claimOwnership(List<PartitionOwnership> requestedPartitionOwnerships);

    /**
     * Updates the checkpoint in the data store for a partition
     */
    Mono<Void> updateCheckpoint(Checkpoint checkpoint);
}
