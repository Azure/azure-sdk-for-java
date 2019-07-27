// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.eventprocessor.models.PartitionContext;
import org.reactivestreams.Subscriber;

/**
 * A functional interface to create new instances of partition processors when provided with a {@link PartitionContext}
 * and {@link CheckpointManager}.
 */
@FunctionalInterface
public interface PartitionProcessorFactory {

    /**
     * Method to create a new instance of partition processor for a partition
     *
     * @param partitionContext The partition context
     * @param checkpointManager The checkpoint manager
     * @return A subscriber that can process {@link EventData} from a single partition of an event hub
     */
    Subscriber<EventData> createPartitionProcessor(PartitionContext partitionContext,
        CheckpointManager checkpointManager);
}
