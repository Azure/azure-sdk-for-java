// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.annotation.Immutable;

import java.time.Instant;

import static com.azure.messaging.eventhubs.implementation.ClientConstants.DEFAULT_REPLICATION_SEGMENT;

/**
 * A set of information about the enqueued state of a partition, as observed by the consumer.
 */
@Immutable
public class LastEnqueuedEventProperties {
    private final Long lastSequenceNumber;
    private final String lastOffset;
    private final Instant lastEnqueuedTime;
    private final Instant retrievalTime;
    private final Integer lastEnqueuedReplicationSegment;

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
     */
    public LastEnqueuedEventProperties(Long lastSequenceNumber, Long lastOffset, Instant lastEnqueuedTime,
        Instant retrievalTime) {
        this(lastSequenceNumber, String.valueOf(lastOffset), lastEnqueuedTime, retrievalTime, DEFAULT_REPLICATION_SEGMENT);
    }

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
     * @param lastEnqueuedReplicationSegment The replication segment for the last event. {@code null} or -1 if
     *     geo-disaster recovery is not enabled.
     */
    public LastEnqueuedEventProperties(Long lastSequenceNumber, String lastOffset, Instant lastEnqueuedTime,
        Instant retrievalTime, Integer lastEnqueuedReplicationSegment) {
        this.lastSequenceNumber = lastSequenceNumber;
        this.lastOffset = lastOffset;
        this.lastEnqueuedTime = lastEnqueuedTime;
        this.retrievalTime = retrievalTime;
        this.lastEnqueuedReplicationSegment = lastEnqueuedReplicationSegment;
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
     *     been retrieved, yet.
     */
    public String getOffset() {
        return lastOffset;
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

    /**
     * Gets the replication segment number for the last enqueued event.
     *
     * @return The replication segment for the last enqueued event.  {@code null} or -1 is returned if geo-disaster
     *     recovery is not enabled in the Event Hub namespace.
     */
    public Integer getReplicationSegment() {
        return lastEnqueuedReplicationSegment;
    }
}
