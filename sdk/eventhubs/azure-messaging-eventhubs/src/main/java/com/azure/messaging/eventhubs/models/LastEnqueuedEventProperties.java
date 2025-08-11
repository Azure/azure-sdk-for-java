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
    private final String lastOffsetString;
    private final Instant lastEnqueuedTime;
    private final Instant retrievalTime;

    /**
     * Creates an instance with the last enqueued event information set.
     *
     * @param lastSequenceNumber Sequence number of the last event to be enqueued in a partition. {@code null} if
     *     the information has not been retrieved, yet.
     * @param lastOffset Offset of the last observed event enqueued in a partition. {@code null} if the information
     *     has not been retrieved, yet.
     * @param lastEnqueuedTime The date and time of the last observed event enqueued in a partition. {@code null} if
     *     the information has not been retrieved, yet.
     * @param retrievalTime The date and time that the information was retrieved. {@code null} if the information
     *     has not been retrieved, yet.
     * @deprecated Constructor is deprecated. Use {@link #LastEnqueuedEventProperties(Long, String, Instant, Instant)}
     */
    @Deprecated
    public LastEnqueuedEventProperties(Long lastSequenceNumber, Long lastOffset, Instant lastEnqueuedTime,
        Instant retrievalTime) {
        this(lastSequenceNumber, lastOffset == null ? null : String.valueOf(lastOffset), lastEnqueuedTime,
            retrievalTime);
    }

    /**
     * Creates an instance with the last enqueued event information set.
     *
     * @param lastSequenceNumber Sequence number of the last event to be enqueued in a partition. {@code null} if
     *     the information has not been retrieved, yet.
     * @param lastOffsetString Offset of the last observed event enqueued in a partition. {@code null} if the
     *     information has not been retrieved, yet.
     * @param lastEnqueuedTime The date and time of the last observed event enqueued in a partition. {@code null} if
     *     the information has not been retrieved, yet.
     * @param retrievalTime The date and time that the information was retrieved. {@code null} if the information
     *     has not been retrieved, yet.
     */
    public LastEnqueuedEventProperties(Long lastSequenceNumber, String lastOffsetString, Instant lastEnqueuedTime,
        Instant retrievalTime) {
        this.lastSequenceNumber = lastSequenceNumber;
        this.lastOffsetString = lastOffsetString;
        this.lastEnqueuedTime = lastEnqueuedTime;
        this.retrievalTime = retrievalTime;

        if (lastOffsetString != null) {
            Long parsed = null;
            try {
                parsed = Long.valueOf(lastOffsetString);
            } catch (NumberFormatException e) {
                // Offset is not a number;
            }

            this.lastOffset = parsed;
        } else {
            this.lastOffset = null;
        }
    }

    /**
     * Gets the sequence number of the last observed event to be enqueued in the partition.
     *
     * @return The sequence number of the last observed event to be enqueued in the partition. {@code null} if the
     *     information has not been retrieved, yet.
     */
    public Long getSequenceNumber() {
        return lastSequenceNumber;
    }

    /**
     * Gets the offset of the last observed event enqueued in the partition.
     *
     * @return The offset of the last observed event enqueued in the partition. {@code null} if the information has not
     *     been retrieved, or the offset cannot be represented as a long.
     * @deprecated This value is obsolete and should no longer be used. Please use {@link #getOffsetString()} instead.
     */
    @Deprecated
    public Long getOffset() {
        return lastOffset;
    }

    /**
     * Gets the offset of the last observed event enqueued in the partition.
     *
     * @return The offset of the last observed event enqueued in the partition. {@code null} if the information has not
     *     been retrieved, yet.
     */
    public String getOffsetString() {
        return lastOffsetString;
    }

    /**
     * Gets the date and time, in UTC, that the last observed event was enqueued in the partition.
     *
     * @return The date and time, in UTC, that the last observed event was enqueued in the partition. {@code null} if
     *     the information has not been retrieved, yet.
     */
    public Instant getEnqueuedTime() {
        return lastEnqueuedTime;
    }

    /**
     * Gets the date and time, in UTC, that the information about the last enqueued event was retrieved.
     *
     * @return The date and time, in UTC, that the information about the last enqueued event was retrieved. {@code null}
     *     if the information has not been retrieved, yet.
     */
    public Instant getRetrievalTime() {
        return retrievalTime;
    }
}
