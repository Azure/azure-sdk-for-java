// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.annotation.Immutable;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * Defines a position of an {@link EventData} in the Event Hub partition stream. The position can be an offset,
 * sequence number, or enqueued time in UTC.
 *
 * @see <a href="https://learn.microsoft.com/en-us/azure/event-hubs/event-hubs-features#event-consumers">Event consumers</a>
 */
@Immutable
public final class EventPosition {
    /**
     * This is a constant defined to represent the start of a partition stream in EventHub.
     */
    private static final Long START_OF_STREAM = -1L;

    /**
     * This is a constant defined to represent the current end of a partition stream in EventHub. This can be used as an
     * offset argument in receiver creation to start receiving from the latest event, instead of a specific offset or
     * point in time.
     */
    private static final String END_OF_STREAM = "@latest";

    private static final EventPosition EARLIEST = fromOffset(START_OF_STREAM, false);
    private static final EventPosition LATEST = new EventPosition(false, END_OF_STREAM, null, null, null);

    private final boolean isInclusive;
    private final String offsetString;
    private final Long offset;
    private final Long sequenceNumber;
    private final Instant enqueuedDateTime;
    private final Integer replicationSegment;

    private EventPosition(final boolean isInclusive, final Long offset, final Long sequenceNumber,
        final Instant enqueuedDateTime) {
        this(isInclusive, String.valueOf(offset), sequenceNumber, enqueuedDateTime, null);
    }

    private EventPosition(final boolean isInclusive, final String offsetString, final Long sequenceNumber,
        final Instant enqueuedDateTime, final Integer replicationSegment) {
        this.offsetString = offsetString;

        Long parsed;
        try {
            parsed = offsetString != null && !offsetString.isEmpty() ? Long.valueOf(offsetString) : null;
        } catch (NumberFormatException e) {
            parsed = null;
        }

        this.offset = parsed;
        this.sequenceNumber = sequenceNumber;
        this.enqueuedDateTime = enqueuedDateTime;
        this.isInclusive = isInclusive;
        this.replicationSegment = replicationSegment;
    }

    /**
     * Corresponds to the location of the first event present in the partition. Use this position to begin receiving
     * from the first event that was enqueued in the partition which has not expired due to the retention policy.
     *
     * @return An {@link EventPosition} set to the start of an Event Hub stream.
     */
    public static EventPosition earliest() {
        return EARLIEST;
    }

    /**
     * Corresponds to the end of the partition, where no more events are currently enqueued. Use this position to begin
     * receiving from the next event to be enqueued in the partition when
     * {@link EventHubConsumerAsyncClient#receiveFromPartition(String, EventPosition) receiveFromPartition()} invoked.
     *
     * @return An {@link EventPosition} set to the end of an Event Hubs stream and listens for new events.
     */
    public static EventPosition latest() {
        return LATEST;
    }

    /**
     * Creates a position at the given {@link Instant}. Corresponds to a specific instance within a partition to begin
     * looking for an event. The event enqueued after the requested {@code enqueuedDateTime} becomes the current
     * position.
     *
     * @param enqueuedDateTime The instant, in UTC, from which the next available event should be chosen.
     * @return An {@link EventPosition} object.
     */
    public static EventPosition fromEnqueuedTime(Instant enqueuedDateTime) {
        return new EventPosition(false, (String) null, null, enqueuedDateTime, null);
    }

    /**
     * Creates a position to an event in the partition at the provided offset. The event at that offset will not be
     * included. Instead, the next event is returned.
     *
     * <p>
     * The offset is the relative position for event in the context of the stream. The offset should not be considered a
     * stable value, as the same offset may refer to a different event as events reach the age limit for retention and
     * are no longer visible within the stream.
     * </p>
     *
     * @param offset The offset of the event within that partition.
     * @return An {@link EventPosition} object.
     * @deprecated This method is obsolete and should no longer be used. Please use {@link #fromOffsetString(String)}.
     */
    @Deprecated
    public static EventPosition fromOffset(long offset) {
        return fromOffset(offset, false);
    }

    /**
     * Creates a position to an event in the partition at the provided offset. If {@code isInclusive} is true, the event
     * with the same offset is returned. Otherwise, the next event is received.
     *
     * @param offset The offset of an event with respect to its relative position in the
     * @param isInclusive If true, the event with the {@code offset} is included; otherwise, the next event will be
     *     received.
     * @return An {@link EventPosition} object.
     * @deprecated This method is obsolete and should no longer be used. Please use {@link #fromOffsetString(String, boolean)}.
     */
    @Deprecated
    private static EventPosition fromOffset(long offset, boolean isInclusive) {
        return new EventPosition(isInclusive, offset, null, null);
    }

    /**
     * Creates a position to an event in the partition at the provided offset. The event at that offset will not be
     * included. Instead, the next event is returned.
     *
     * <p>
     * The offset is the relative position for event in the context of the stream. The offset should not be considered a
     * stable value, as the same offset may refer to a different event as events reach the age limit for retention and
     * are no longer visible within the stream.
     * </p>
     *
     * @param offset The offset of the event within that partition.
     * @return An {@link EventPosition} object.
     */
    public static EventPosition fromOffsetString(String offset) {
        return fromOffsetString(offset, false);
    }

    /**
     * Creates a position to an event in the partition at the provided offset. If {@code isInclusive} is true, the event
     * with the same offset is returned. Otherwise, the next event is received.
     *
     * @param offset The offset of an event with respect to its relative position in the
     * @param isInclusive If true, the event with the {@code offset} is included; otherwise, the next event will be
     *     received.
     * @return An {@link EventPosition} object.
     */
    private static EventPosition fromOffsetString(String offset, boolean isInclusive) {
        return new EventPosition(isInclusive, offset, null, null, null);
    }

    /**
     * Creates a position to an event in the partition at the provided sequence number. The event with the sequence
     * number will not be included. Instead, the next event is returned.
     *
     * @param sequenceNumber is the sequence number of the event.
     * @return An {@link EventPosition} object.
     */
    public static EventPosition fromSequenceNumber(long sequenceNumber) {
        return fromSequenceNumber(sequenceNumber, false);
    }

    /**
     * Creates a position at the given sequence number. If {@code isInclusive} is true, the event with the same sequence
     * number is returned. Otherwise, the next event in the sequence is received.
     *
     * @param sequenceNumber is the sequence number of the event.
     * @param isInclusive If true, the event with the {@code sequenceNumber} is included; otherwise, the next event
     *     will be received.
     * @return An {@link EventPosition} object.
     */
    public static EventPosition fromSequenceNumber(long sequenceNumber, boolean isInclusive) {
        return new EventPosition(isInclusive, null, sequenceNumber, null, null);
    }

    /**
     * Creates a position at the given sequence number with the same replication segment. Replication segments exist
     * for geo-disaster recovery enabled Event Hub namespaces. The event with the same sequence number will not be
     * included.  Instead, the next event is returned.
     *
     * @param sequenceNumber is the sequence number of the event.
     * @param replicationSegment the replication segment.
     * @return An {@link EventPosition} object.
     */
    public static EventPosition fromSequenceNumber(long sequenceNumber, int replicationSegment) {
        return fromSequenceNumber(sequenceNumber, replicationSegment, false);
    }

    /**
     * Creates a position at the given sequence number with the same replication segment. Replication segments exist
     * for geo-disaster recovery enabled Event Hub namespaces.  If {@code isInclusive} is true, the event with the same
     * sequence number is returned. Otherwise, the next event in the sequence is received.
     *
     * @param sequenceNumber is the sequence number of the event.
     * @param isInclusive If true, the event with the {@code sequenceNumber} and {@code replicationSegment}is included;
     *     otherwise, the next event will be received.
     * @param replicationSegment the replication segment.
     * @return An {@link EventPosition} object.
     */
    public static EventPosition fromSequenceNumber(long sequenceNumber, int replicationSegment, boolean isInclusive) {
        return new EventPosition(isInclusive, null, sequenceNumber, null, replicationSegment);
    }

    /**
     * Gets the boolean value of if the event is included. If true, the event with the {@code sequenceNumber} is
     * included; otherwise, the next event will be received.
     *
     * @return The boolean if the event will be received.
     */
    public boolean isInclusive() {
        return isInclusive;
    }

    /**
     * Gets the relative position for event in the context of the stream. The offset should not be considered a stable
     * value, as the same offset may refer to a different event as events reach the age limit for retention and are no
     * longer visible within the stream.
     *
     * @return The offset of the event within that partition.
     * @deprecated This method is obsolete and should no longer be used. Please use {@link #getOffsetString()}.
     */
    @Deprecated
    public Long getOffset() {
        return offset;
    }

    /**
     * Gets the relative position for event in the context of the stream. The offset should not be considered a stable
     * value, as the same offset may refer to a different event as events reach the age limit for retention and are no
     * longer visible within the stream.
     *
     * @return The offset of the event within that partition.
     */
    public String getOffsetString() {
        return offsetString;
    }

    /**
     * Gets the sequence number of the event.
     *
     * @return The sequence number of the event.
     */
    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Gets the instant, in UTC, from which the next available event should be chosen.
     *
     * @return The instant, in UTC, from which the next available event should be chosen.
     */
    public Instant getEnqueuedDateTime() {
        return enqueuedDateTime;
    }

    /**
     * Gets the replication segment for a geo-disaster recovery enabled Event Hub namespace.
     *
     * @return The replication segment or {@code null} if geo-disaster recovery is not enabled or there is no
     * replication segment.
     */
    public Integer getReplicationSegment() {
        return this.replicationSegment;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
            "offsetString[%s], offset[%s], "
                + "sequenceNumber[%s], replicationSegment[%s], enqueuedTime[%s], isInclusive[%s]",
            offsetString, offset, sequenceNumber, replicationSegment,
            enqueuedDateTime != null ? enqueuedDateTime.toEpochMilli() : "null", isInclusive);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EventPosition)) {
            return false;
        }

        final EventPosition other = (EventPosition) obj;

        return Objects.equals(isInclusive, other.isInclusive)
            && Objects.equals(offset, other.offset)
            && Objects.equals(offsetString, other.offsetString)
            && Objects.equals(sequenceNumber, other.sequenceNumber)
            && Objects.equals(enqueuedDateTime, other.enqueuedDateTime)
            && Objects.equals(replicationSegment, other.replicationSegment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isInclusive, offset, offsetString, sequenceNumber, enqueuedDateTime, replicationSegment);
    }
}
