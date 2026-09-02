// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import java.time.OffsetDateTime;

/**
 * Options for permanently deleting eligible messages from a Service Bus entity or subqueue.
 */
public final class DeleteMessagesOptions {
    private OffsetDateTime enqueueTimeUtcOlderThan;

    /**
     * Creates an instance of {@link DeleteMessagesOptions}.
     */
    public DeleteMessagesOptions() {
    }

    /**
        * Gets the enqueue-time cutoff. Only messages enqueued before this time are eligible for deletion.
     *
     * @return The enqueue-time cutoff, or {@code null} to use the time the operation starts.
     */
    public OffsetDateTime getEnqueueTimeUtcOlderThan() {
        return enqueueTimeUtcOlderThan;
    }

    /**
        * Sets the enqueue-time cutoff. Only messages enqueued before this time are eligible for deletion.
     *
        * @param enqueueTimeUtcOlderThan The enqueue-time cutoff.
     * @return The updated {@link DeleteMessagesOptions}.
     */
    public DeleteMessagesOptions setEnqueueTimeUtcOlderThan(OffsetDateTime enqueueTimeUtcOlderThan) {
        this.enqueueTimeUtcOlderThan = enqueueTimeUtcOlderThan;
        return this;
    }
}
