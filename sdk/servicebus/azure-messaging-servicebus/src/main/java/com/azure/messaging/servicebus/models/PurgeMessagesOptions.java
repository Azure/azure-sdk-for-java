// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import java.time.OffsetDateTime;

/**
 * Options for permanently purging eligible messages from a Service Bus entity or subqueue.
 */
public final class PurgeMessagesOptions {
    private OffsetDateTime enqueueTimeUtcOlderThan;
    private int maxMessagesPerBatch = 500;

    /**
     * Creates an instance of {@link PurgeMessagesOptions}.
     */
    public PurgeMessagesOptions() {
    }

    /**
    * Gets the enqueue-time threshold that stays unchanged for every purge request.
     *
    * @return The enqueue-time threshold, or {@code null} to use the time the purge starts.
     */
    public OffsetDateTime getEnqueueTimeUtcOlderThan() {
        return enqueueTimeUtcOlderThan;
    }

    /**
    * Sets the enqueue-time threshold. Only messages enqueued before this time can be deleted, and the value stays
    * unchanged for every purge request.
     *
    * @param enqueueTimeUtcOlderThan The enqueue-time threshold.
     * @return The updated {@link PurgeMessagesOptions}.
     */
    public PurgeMessagesOptions setEnqueueTimeUtcOlderThan(OffsetDateTime enqueueTimeUtcOlderThan) {
        this.enqueueTimeUtcOlderThan = enqueueTimeUtcOlderThan;
        return this;
    }

    /**
     * Gets the maximum number of messages requested in each batch-delete call.
     *
     * @return The maximum messages per batch. The default is 500.
     */
    public int getMaxMessagesPerBatch() {
        return maxMessagesPerBatch;
    }

    /**
    * Sets the maximum number of messages requested in each batch-delete call. The service limit is 500 for Basic and
    * Standard and 4,000 for Premium.
     *
     * @param maxMessagesPerBatch The positive maximum number of messages per batch.
     * @return The updated {@link PurgeMessagesOptions}.
    * @throws IllegalArgumentException if {@code maxMessagesPerBatch} is less than one.
     */
    public PurgeMessagesOptions setMaxMessagesPerBatch(int maxMessagesPerBatch) {
        if (maxMessagesPerBatch < 1) {
            throw new IllegalArgumentException("'maxMessagesPerBatch' must be greater than 0.");
        }
        this.maxMessagesPerBatch = maxMessagesPerBatch;
        return this;
    }
}
