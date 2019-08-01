// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.PartitionContext;

/**
 * A functional interface to create new instance(s) of {@link PartitionProcessor} when provided with a {@link
 * PartitionContext} and {@link CheckpointManager}.
 */
@FunctionalInterface
public interface PartitionProcessorFactory {

    /**
     * Factory method to create a new instance(s) of {@link PartitionProcessor} for a partition.
     *
     * @param partitionContext The partition context containing partition and Event Hub information. The new instance of
     * {@link PartitionProcessor} created by this method will be responsible for processing events only for this
     * partition.
     * @param checkpointManager The checkpoint manager for updating checkpoints when events are processed by {@link
     * PartitionProcessor}.
     * @return A new instance of {@link PartitionProcessor} responsible for processing events from a single partition.
     */
    PartitionProcessor createPartitionProcessor(PartitionContext partitionContext,
        CheckpointManager checkpointManager);
}
