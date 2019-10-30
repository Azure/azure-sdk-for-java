package com.azure.messaging.eventhubs;

import com.azure.core.annotation.Immutable;
import com.azure.messaging.eventhubs.models.PartitionContext;

/**
 * This class contains information about an error that occurred while processing events.
 */
@Immutable
public class ErrorContext {
    private PartitionContext partitionContext;
    private Throwable throwable;

    /**
     * Creates a new instance of ErrorContext.
     *
     * @param partitionContext The partition information where the error occurred.
     * @param throwable The {@link Throwable error} that occurred.
     */
    public ErrorContext(final PartitionContext partitionContext, final Throwable throwable) {
        this.partitionContext = partitionContext;
        this.throwable = throwable;
    }

    /**
     * Returns the partition information where the error occurred.
     *
     * @return The partition information where the error occurred.
     */
    public PartitionContext getPartitionContext() {
        return partitionContext;
    }

    /**
     * Returns the error that occurred during event processing.
     *
     * @return The error that occurred during event processing.
     */
    public Throwable getThrowable() {
        return throwable;
    }
}
