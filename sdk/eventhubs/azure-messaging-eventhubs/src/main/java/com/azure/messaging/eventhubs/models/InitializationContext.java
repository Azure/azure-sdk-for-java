// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * The initialization context that is supplied to {@link
 * EventProcessorClientBuilder#processPartitionInitialization(Consumer)} before the {@link EventProcessorClient}
 * instance begins processing events from a partition.
 */
public class InitializationContext {

    private final PartitionContext partitionContext;
    private EventPosition initialPosition;

    /**
     * Creates an instance of InitializationContext for the partition provided in the {@link PartitionContext}.
     *
     * @param partitionContext The partition information for which the event processing is going to start.
     * @param initialPosition The default initial event position from which the processing will start in the absence of
     * a checkpoint in {@link CheckpointStore}.
     * @throws NullPointerException if {@code partitionContext} or {@code initialPosition}is {@code null}.
     */
    public InitializationContext(final PartitionContext partitionContext, final EventPosition initialPosition) {
        this.partitionContext = Objects.requireNonNull(partitionContext, "'partitionContext' cannot be null");
        this.initialPosition = Objects.requireNonNull(initialPosition, "'initialPosition' cannot be null");
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
     * {@link CheckpointStore}.
     *
     * @return The default initial event position from which the processing will start in the absence of a checkpoint in
     * {@link CheckpointStore}.
     */
    public EventPosition getInitialPosition() {
        return initialPosition;
    }

    /**
     * If a different initial position is desirable for this partition, setting the initial position will start the
     * event processing from this position. Note that the checkpoint in {@link CheckpointStore} is given the highest
     * priority and if there's a checkpoint in the store, that will be used regardless of what is set in this method.
     *
     * @param initialPosition The initial event position to start the event processing from.
     * @throws NullPointerException if {@code initialPosition} is {@code null}.
     */
    public void setInitialPosition(final EventPosition initialPosition) {
        this.initialPosition = Objects.requireNonNull(initialPosition, "'initialPosition' cannot be null");
    }
}
