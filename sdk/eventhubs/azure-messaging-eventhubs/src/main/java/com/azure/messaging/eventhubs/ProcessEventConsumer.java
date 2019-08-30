// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.PartitionContext;

/**
 * A functional interface for processing events as they are received by the {@link EventProcessor}.
 */
@FunctionalInterface
public interface ProcessEventConsumer {

    /**
     * The functional method for processing events as they are received by the {@link EventProcessor}. This method is
     * provided with a {@link CheckpointManager} that can be used for updating checkpoints after processing events. This
     * method also has the partition information for the event that is received.
     *
     * @param eventData The event received by the {@link EventProcessor} that should be processed.
     * @param partitionContext The partition information related to the received event.
     * @param checkpointManager An instance of {@link CheckpointManager} that can be used for updating checkpoints.
     */
    void processEvent(EventData eventData, PartitionContext partitionContext, CheckpointManager checkpointManager);
}
