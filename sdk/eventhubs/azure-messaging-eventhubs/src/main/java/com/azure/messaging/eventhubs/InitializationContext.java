// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionContext;
import java.util.function.Function;

/**
 * The initialization context that is supplied to {@link EventProcessorBuilder#initializePartition(Function)
 * initializePartition} before the {@link EventProcessor} instance begins processing events from a partition.
 */
public class InitializationContext {

    private final PartitionContext partitionContext;
    private EventPosition initialPosition;

    /**
     * Creates an instance of InitializationContext for the partition provided in the {@link PartitionContext}.
     *
     * @param partitionContext The partition information for which the event processing is going to start.
     * @param initialPosition The default initial event position from which the processing will start in the absence of
     * a checkpoint in {@link EventProcessorStore}.
     */
    public InitializationContext(final PartitionContext partitionContext,
        final EventPosition initialPosition) {
        this.partitionContext = partitionContext;
        this.initialPosition = initialPosition;
    }

    /**
     * Returns the partition information for which the event processing is going to start.
     *
     * @return The partition information for which the event processing is going to start.
     */
    public PartitionContext getPartitionContext() {
        return partitionContext;
    }

    /**
     * Returns the default initial event position from which the processing will start in the absence of a checkpoint in
     * {@link EventProcessorStore}.
     *
     * @return The default initial event position from which the processing will start in the absence of a checkpoint in
     * {@link EventProcessorStore}.
     */
    public EventPosition getInitialPosition() {
        return initialPosition;
    }

    /**
     * If a different initial position is desirable for this partition, setting the initial position will start the
     * event processing from this position. Note that the checkpoint in {@link EventProcessorStore} is given the highest
     * priority and if there's a checkpoint in the store, that will be used regardless of what is set in this method.
     *
     * @param initialPosition The initial event position to start the event processing from.
     */
    public void setInitialPosition(final EventPosition initialPosition) {
        this.initialPosition = initialPosition;
    }
}
