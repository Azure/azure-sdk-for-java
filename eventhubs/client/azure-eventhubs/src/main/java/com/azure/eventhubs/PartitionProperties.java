// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import java.time.Instant;

/**
 * Contains runtime information about an Event Hub partition.
 */
public final class PartitionProperties {
    private final String eventHubPath;
    private final String id;
    private final long beginningSequenceNumber;
    private final long lastEnqueuedSequenceNumber;
    private final String lastEnqueuedOffset;
    private final Instant lastEnqueuedTime;
    private final boolean isEmpty;

    PartitionProperties(
            final String eventHubPath,
            final String id,
            final long beginningSequenceNumber,
            final long lastEnqueuedSequenceNumber,
            final String lastEnqueuedOffset,
            final Instant lastEnqueuedTime,
            final boolean isEmpty) {
        this.eventHubPath = eventHubPath;
        this.id = id;
        this.beginningSequenceNumber = beginningSequenceNumber;
        this.lastEnqueuedSequenceNumber = lastEnqueuedSequenceNumber;
        this.lastEnqueuedOffset = lastEnqueuedOffset;
        this.lastEnqueuedTime = lastEnqueuedTime;
        this.isEmpty = isEmpty;
    }

    /**
     * Gets the Event Hub path for this partition.
     *
     * @return The Event Hub path for this partition.
     */
    public String eventHubPath() {
        return this.eventHubPath;
    }

    /**
     * Gets the identifier of the partition within the Event Hub.
     *
     * @return The identifier of the partition within the Event Hub.
     */
    public String id() {
        return this.id;
    }

    /**
     * Gets the starting sequence number of the partition's message stream.
     *
     * @return The starting sequence number of the partition's message stream.
     */
    public long beginningSequenceNumber() {
        return this.beginningSequenceNumber;
    }

    /**
     * Gets the last sequence number of the partition's message stream.
     *
     * @return the last sequence number of the partition's message stream.
     */
    public long lastEnqueuedSequenceNumber() {
        return this.lastEnqueuedSequenceNumber;
    }

    /**
     * Gets the offset of the last enqueued message in the partition's stream.
     *
     * @return the offset of the last enqueued message in the partition's stream.
     */
    public String lastEnqueuedOffset() {
        return this.lastEnqueuedOffset;
    }

    /**
     * Gets the instant, in UTC, of the last enqueued message in the partition's stream.
     *
     * @return the instant, in UTC, of the last enqueued message in the partition's stream.
     */
    public Instant lastEnqueuedTime() {
        return this.lastEnqueuedTime;
    }

    /**
     * Indicates whether or not there are events in the partition.
     *
     * @return true if there are no events, and false otherwise.
     */
    public boolean isEmpty() {
        return this.isEmpty;
    }
}
