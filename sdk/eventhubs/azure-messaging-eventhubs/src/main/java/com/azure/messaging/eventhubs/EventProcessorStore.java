// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * An interface that defines the operations for storing and retrieving partition ownership information and checkpoint
 * details for each partition.
 */
public interface EventProcessorStore {

    /**
     * Called to get the list of all existing partition ownership from the underlying data store. Could return empty
     * results if there are is no existing ownership information.
     *
     * @param fullyQualifiedNamespace The fully qualified namespace of the Event Hub.
     * @param eventHubName The Event Hub name to get ownership information.
     * @param consumerGroupName The consumer group name.
     * @return A flux of partition ownership details of all the partitions that have/had an owner.
     */
    Flux<PartitionOwnership> listOwnership(String fullyQualifiedNamespace, String eventHubName,
        String consumerGroupName);

    /**
     * Called to claim ownership of a list of partitions. This will return the list of partitions that were owned
     * successfully.
     *
     * @param requestedPartitionOwnerships Array of partition ownerships this instance is requesting to own.
     * @return A flux of partitions this instance successfully claimed ownership.
     */
    Flux<PartitionOwnership> claimOwnership(PartitionOwnership... requestedPartitionOwnerships);

    /**
     * Updates the checkpoint in the data store for a partition.
     *
     * @param checkpoint Checkpoint information containing sequence number and offset to be stored for this partition.
     * @return The new ETag on successful update.
     */
    Mono<String> updateCheckpoint(Checkpoint checkpoint);
}
