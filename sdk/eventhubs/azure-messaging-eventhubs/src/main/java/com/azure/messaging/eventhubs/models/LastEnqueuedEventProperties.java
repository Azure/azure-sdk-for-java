// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import java.time.Instant;
import java.util.Objects;

/**
 * A set of information about the enqueued state of a partition, as observed by the consumer.
 */
public class LastEnqueuedEventProperties {
    private Long lastSequenceNumber;
    private Long lastOffset;
    private Instant lastEnqueuedTime;
    private Instant retrievalTime;

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

    /**
     * Updates the current set of properties for this partition.
     *
     * @param lastSequenceNumber The sequence number observed the last event to be enqueued in the partition.
     * @param lastOffset The offset of the last event enqueued in the partition.
     * @param lastEnqueuedTime The date and time, in UTC, that the last event was enqueued in the partition.
     * @param retrievalTime The date and time, in UTC, that the properties were retrieved.
     *
     * @throws NullPointerException if {@code lastEnqueuedTime} or {@code retrievalTime} is null.
     */
    public void updateProperties(long lastSequenceNumber, long lastOffset, Instant lastEnqueuedTime,
                                 Instant retrievalTime) {
        this.lastSequenceNumber = lastSequenceNumber;
        this.lastOffset = lastOffset;
        this.lastEnqueuedTime = Objects.requireNonNull(lastEnqueuedTime, "'lastEnqueuedTime' is required.");
        this.retrievalTime = Objects.requireNonNull(retrievalTime, "'retrievalTime' is required.");
    }
}
