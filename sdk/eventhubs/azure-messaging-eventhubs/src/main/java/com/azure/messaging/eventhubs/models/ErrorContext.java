// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import java.util.Objects;

/**
 * This class contains information about an error that occurred while processing events.
 */
import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;

@Metadata(properties = MetadataProperties.IMMUTABLE)
public class ErrorContext {
    private final PartitionContext partitionContext;
    private final Throwable throwable;

    /**
     * Creates a new instance of ErrorContext.
     *
     * @param partitionContext The partition information where the error occurred.
     * @param throwable The {@link Throwable error} that occurred.
     * @throws NullPointerException if {@code partitionContext} or {@code throwable} is {@code null}.
     */
    public ErrorContext(final PartitionContext partitionContext, final Throwable throwable) {
        this.partitionContext = Objects.requireNonNull(partitionContext, "'partitionContext' cannot be null");
        this.throwable = Objects.requireNonNull(throwable, "'throwable' cannot be null");
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
