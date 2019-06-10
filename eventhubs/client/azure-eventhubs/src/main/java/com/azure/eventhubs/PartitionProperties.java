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
    private Instant propertyRetrievalTime;

    /**
     * Creates an instance with all of the partition's properties set.
     *
     * @param eventHubPath Name of the Event Hub this partition belongs to.
     * @param id Identifier of the partition, unique to the Event Hub which contains it.
     * @param beginningSequenceNumber The first sequence number available for events in the partition.
     * @param lastEnqueuedSequenceNumber The sequence number of the most recent event enqueued to this partition.
     * @param lastEnqueuedOffset The offset of the most recent event enqueued to this partition.
     * @param lastEnqueuedTime The date time (UTC) of the most recent event enqueued to this partition.
     * @param isEmpty {@code true} if there are no events in the partition; {@code false} otherwise.
     * @param propertyRetrievalTime A date time (UTC) representing when the partition's properties were retrieved.
     */
    PartitionProperties(String eventHubPath, String id, long beginningSequenceNumber,
                        long lastEnqueuedSequenceNumber, String lastEnqueuedOffset, Instant lastEnqueuedTime,
                        boolean isEmpty, Instant propertyRetrievalTime) {
        this.eventHubPath = eventHubPath;
        this.id = id;
        this.beginningSequenceNumber = beginningSequenceNumber;
        this.lastEnqueuedSequenceNumber = lastEnqueuedSequenceNumber;
        this.lastEnqueuedOffset = lastEnqueuedOffset;
        this.lastEnqueuedTime = lastEnqueuedTime;
        this.isEmpty = isEmpty;
        this.propertyRetrievalTime = propertyRetrievalTime;
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
     * @return the last sequence number of the partition's messaPropertyRetrievalTimeUtcge stream.
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
     * Gets the date time, in UTC, of the last enqueued message in the partition's stream.
     *
     * @return the time, in UTC, of the last enqueued message in the partition's stream.
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

    /**
     * The instant, in UTC, that the partition information was retrieved from the Event Hub.
     *
     * @return Instant, in UTC, that the partition information was retrieved.
     */
    public Instant propertyRetrievalTime() {
        return this.propertyRetrievalTime;
    }
}
