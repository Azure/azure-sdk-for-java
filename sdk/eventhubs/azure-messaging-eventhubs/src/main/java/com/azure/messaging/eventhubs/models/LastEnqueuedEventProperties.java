// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.annotation.Immutable;

import java.time.Instant;

/**
 * A set of information about the enqueued state of a partition, as observed by the consumer.
 */
@Immutable
public class LastEnqueuedEventProperties {
    private final Long lastSequenceNumber;
    private final Long lastOffset;
    private final Instant lastEnqueuedTime;
    private final Instant retrievalTime;

    public LastEnqueuedEventProperties(Long lastSequenceNumber, Long lastOffset, Instant lastEnqueuedTime,
                                       Instant retrievalTime) {
        this.lastSequenceNumber = lastSequenceNumber;
        this.lastOffset = lastOffset;
        this.lastEnqueuedTime = lastEnqueuedTime;
        this.retrievalTime = retrievalTime;
    }

    /**
     * Gets the sequence number of the last observed event to be enqueued in the partition.
     *
     * @return The sequence number of the last observed event to be enqueued in the partition.
     */
    public Long getSequenceNumber() {
        return lastSequenceNumber;
    }

    /**
     * Gets the offset of the last observed event enqueued in the partition.
     *
     * @return The offset of the last observed event enqueued in the partition.
     */
    public Long getOffset() {
        return lastOffset;
    }

    /**
     * Gets the date and time, in UTC, that the last observed event was enqueued in the partition.
     *
     * @return The date and time, in UTC, that the last observed event was enqueued in the partition.
     */
    public Instant getEnqueuedTime() {
        return lastEnqueuedTime;
    }

    /**
     * Gets the date and time, in UTC, that the information about the last enqueued event was retrieved.
     *
     * @return The date and time, in UTC, that the information about the last enqueued event was retrieved.
     */
    public Instant getRetrievalTime() {
        return retrievalTime;
    }
}
