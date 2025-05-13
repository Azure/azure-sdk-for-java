// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.models;

import com.azure.core.annotation.Immutable;

import java.util.Map;

/**
 * Model class containing properties of a specific queue in the storage Queue service.
 */
@Immutable
public final class QueueProperties {
    private final Map<String, String> metadata;
    private final int approximateMessagesCount;
    private final long approximateMessagesCountLong;

    /**
     * Creates an instance that contains properties of a queue.
     *
     * @param metadata Metadata associated with the queue.
     * @param approximateMessagesCount Approximate number of messages contained in the queue.
     */
    public QueueProperties(Map<String, String> metadata, long approximateMessagesCount) {
        this.metadata = metadata;
        this.approximateMessagesCount = Math.toIntExact(approximateMessagesCount);
        this.approximateMessagesCountLong = approximateMessagesCount;
    }

    /**
     * Gets the user-defined metadata associated with the queue.
     *
     * @return The user-defined metadata associated with the queue.
     */
    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Gets the approximate number of messages contained in the queue at the time of properties retrieval.
     *
     * @return the approximate number of messages contained in the queue at the time of properties retrieval.
     */
    public int getApproximateMessagesCount() {
        return approximateMessagesCount;
    }

    /**
     * Gets the approximate number of messages contained in the queue at the time of properties retrieval.
     *
     * @return the approximate number of messages contained in the queue at the time of properties retrieval.
     */
    public long getApproximateMessagesCountLong() {
        return approximateMessagesCountLong;
    }
}
