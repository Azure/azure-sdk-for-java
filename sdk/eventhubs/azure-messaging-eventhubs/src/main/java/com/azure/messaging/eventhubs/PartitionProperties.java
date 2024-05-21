// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.annotation.Immutable;

import java.time.Instant;

/**
 * A set of information for a single partition of an Event Hub.
 */
@Immutable
public final class PartitionProperties {
    private final String eventHubName;
    private final String id;
    private final long beginningSequenceNumber;
    private final long lastEnqueuedSequenceNumber;
    private final Integer beginningReplicationSegment;
    private final Integer lastEnqueuedReplicationSegment;
    private final String lastEnqueuedOffset;
    private final Instant lastEnqueuedTime;
    private final boolean isEmpty;

    PartitionProperties(
        final String eventHubName,
        final String id,
        final long beginningSequenceNumber,
        final long lastEnqueuedSequenceNumber,
        final String lastEnqueuedOffset,
        final Instant lastEnqueuedTime,
        final boolean isEmpty,
        final Integer beginningReplicationSegment,
        final Integer lastEnqueuedReplicationSegment) {
        this.eventHubName = eventHubName;
        this.id = id;
        this.beginningSequenceNumber = beginningSequenceNumber;
        this.lastEnqueuedSequenceNumber = lastEnqueuedSequenceNumber;
        this.lastEnqueuedOffset = lastEnqueuedOffset;
        this.lastEnqueuedTime = lastEnqueuedTime;
        this.isEmpty = isEmpty;
        this.beginningReplicationSegment = beginningReplicationSegment;
        this.lastEnqueuedReplicationSegment = lastEnqueuedReplicationSegment;
    }

    /**
     * Gets the name of the Event Hub that contains the partition.
     *
     * @return The name of the Event Hub that contains the partition.
     */
    public String getEventHubName() {
        return this.eventHubName;
    }

    /**
     * Gets the identifier of the partition within the Event Hub.
     *
     * @return The identifier of the partition within the Event Hub.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the first replication segment.
     *
     * @return The first replication segment or null if geo-disaster recovery is not enabled.
     */
    public Integer getBeginningReplicationSegment() {
        return beginningReplicationSegment;
    }

    /**
     * Gets the starting sequence number of the partition's message stream.
     *
     * @return The starting sequence number of the partition's message stream.
     */
    public long getBeginningSequenceNumber() {
        return this.beginningSequenceNumber;
    }

    /**
     * Get the last replication segment for the partition.
     *
     * @return The last replication segment or null if geo-disaster recovery is not enabled.
     */
    public Integer getLastEnqueuedReplicationSegment() {
        return this.lastEnqueuedReplicationSegment;
    }

    /**
     * Gets the last sequence number of the partition's message stream.
     *
     * @return The last sequence number of the partition's message stream.
     */
    public long getLastEnqueuedSequenceNumber() {
        return this.lastEnqueuedSequenceNumber;
    }

    /**
     * Gets the offset of the last enqueued message in the partition's stream.
     *
     * <p>
     * The offset is the relative position for event in the context of the stream. The offset should not be considered a
     * stable value, as the same offset may refer to a different event as events reach the age limit for retention and
     * are no longer visible within the stream.
     * </p>
     *
     * @return The offset of the last enqueued message in the partition's stream.
     */
    public String getLastEnqueuedOffset() {
        return this.lastEnqueuedOffset;
    }

    /**
     * Gets the instant, in UTC, of the last enqueued message in the partition's stream.
     *
     * @return The instant, in UTC, of the last enqueued message in the partition's stream.
     */
    public Instant getLastEnqueuedTime() {
        return this.lastEnqueuedTime;
    }

    /**
     * Indicates whether the partition is currently empty.
     *
     * @return {@code true} if there are no events, and {@code false} otherwise.
     */
    public boolean isEmpty() {
        return this.isEmpty;
    }
}
