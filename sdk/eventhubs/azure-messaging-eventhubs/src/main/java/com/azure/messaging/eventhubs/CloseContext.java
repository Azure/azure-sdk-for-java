package com.azure.messaging.eventhubs;

import com.azure.core.annotation.Immutable;
import com.azure.messaging.eventhubs.models.PartitionContext;

/**
 * This class contains information about a partition for which this {@link EventProcessor} stopped processing.
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
     */
    public CloseContext(final PartitionContext partitionContext, final CloseReason closeReason) {
        this.partitionContext = partitionContext;
        this.closeReason = closeReason;
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
