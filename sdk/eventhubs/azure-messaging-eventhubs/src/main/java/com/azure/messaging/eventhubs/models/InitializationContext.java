// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

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

    /**
     * Creates an instance of InitializationContext for the partition provided in the {@link PartitionContext}.
     *
     * @param partitionContext The partition information for which the event processing is going to start.
     * @throws NullPointerException if {@code partitionContext} or {@code initialPosition}is {@code null}.
     */
    public InitializationContext(final PartitionContext partitionContext) {
        this.partitionContext = Objects.requireNonNull(partitionContext, "'partitionContext' cannot be null");
    }

    /**
     * Returns the partition information for which the event processing is going to start.
     *
     * @return The partition information for which the event processing is going to start.
     */
    public PartitionContext getPartitionContext() {
        return partitionContext;
    }
}
