// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.annotation.Immutable;
import com.azure.messaging.eventhubs.EventProcessorClient;
import java.util.Objects;

/**
 * This class contains information about a partition for which this {@link EventProcessorClient} stopped processing.
 */
@Immutable
public class CloseContext {

    private final PartitionContext partitionContext;
    private final CloseReason closeReason;

    /**
     * Creates a new instance of CloseContext.
     *
     * @param partitionContext The partition information for which the processing stopped.
     * @param closeReason The reason for stopping the event processing.
     * @throws NullPointerException if {@code partitionContext} or {@code closeReason} is {@code null}.
     */
    public CloseContext(final PartitionContext partitionContext, final CloseReason closeReason) {
        this.partitionContext = Objects.requireNonNull(partitionContext, "'partitionContext' cannot be null");
        this.closeReason = Objects.requireNonNull(closeReason, "'closeReason' cannot be null");
    }

    /**
     * Returns the partition information for which the processing stopped.
     *
     * @return The partition information for which the processing stopped.
     */
    public PartitionContext getPartitionContext() {
        return partitionContext;
    }

    /**
     * Returns the reason for stopping the event processing.
     *
     * @return The reason for stopping the event processing.
     */
    public CloseReason getCloseReason() {
        return closeReason;
    }
}
