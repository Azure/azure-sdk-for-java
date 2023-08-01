// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * An interface that defines the operations for storing and retrieving partition ownership information and checkpoint
 * details for each partition.
 *
 * @see EventProcessorClientBuilder#checkpointStore(CheckpointStore)
 */
public interface CheckpointStore {

    /**
     * Called to get the list of all existing partition ownership from the underlying data store. Could return empty
     * results if there are is no existing ownership information.
     *
     * @param fullyQualifiedNamespace The fully qualified namespace of the Event Hub. This is likely to be similar to
     * <strong>{@literal "{your-namespace}.servicebus.windows.net}"</strong>.
     * @param eventHubName The Event Hub name to get ownership information.
     * @param consumerGroup The consumer group name.
     * @return A {@link Flux} of partition ownership details of all the partitions that have/had an owner.
     */
    Flux<PartitionOwnership> listOwnership(String fullyQualifiedNamespace, String eventHubName,
        String consumerGroup);

    /**
     * Called to claim ownership of a list of partitions. This will return the list of partitions that were owned
     * successfully.
     *
     * @param requestedPartitionOwnerships List of partition ownerships this instance is requesting to own.
     * @return A {@link Flux} of partitions this instance successfully claimed ownership.
     */
    Flux<PartitionOwnership> claimOwnership(List<PartitionOwnership> requestedPartitionOwnerships);

    /**
     * Called to get the list of checkpoints from the underlying data store. This method could return empty results if
     * there are no checkpoints available in the data store.
     *
     * @param fullyQualifiedNamespace The fully qualified namespace of the Event Hub.
     * @param eventHubName The Event Hub name to get checkpoint information.
     * @param consumerGroup The consumer group name associated with the checkpoint.
     * @return A {@link Flux} of checkpoints associated with the partitions of the Event Hub.
     */
    Flux<Checkpoint> listCheckpoints(String fullyQualifiedNamespace, String eventHubName, String consumerGroup);

    /**
     * Updates the checkpoint in the data store for a partition.
     *
     * @param checkpoint Checkpoint information containing sequence number and offset to be stored for this partition.
     * @return A {@link Mono} that completes when the checkpoint is updated.
     */
    Mono<Void> updateCheckpoint(Checkpoint checkpoint);
}
