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
 * Defines a position of an {@link EventData} in the Event Hub partition. The position can be an offset, sequence
 * number, or enqueued time.
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
    private static final EventPosition LATEST =  new EventPosition(false, END_OF_STREAM, null, null);

    private final boolean isInclusive;
    private final String offset;
    private final Long sequenceNumber;
    private final Instant enqueuedDateTime;

    private EventPosition(final boolean isInclusive, final Long offset, final Long sequenceNumber,
                          final Instant enqueuedDateTime) {
        this(isInclusive, String.valueOf(offset), sequenceNumber, enqueuedDateTime);
    }

    private EventPosition(final boolean isInclusive, final String offset, final Long sequenceNumber,
                          final Instant enqueuedDateTime) {
        this.offset = offset;
        this.sequenceNumber = sequenceNumber;
        this.enqueuedDateTime = enqueuedDateTime;
        this.isInclusive = isInclusive;
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
        return new EventPosition(false, (String) null, null, enqueuedDateTime);
    }

    /**
     * Corresponds to the event in the partition at the provided offset, inclusive of that event.
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
    public static EventPosition fromOffset(long offset) {
        return fromOffset(offset, true);
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
    private static EventPosition fromOffset(long offset, boolean isInclusive) {
        return new EventPosition(isInclusive, offset, null, null);
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
        return new EventPosition(isInclusive, (String) null, sequenceNumber, null);
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
     */
    public String getOffset() {
        return offset;
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
        return this.enqueuedDateTime;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "offset[%s], sequenceNumber[%s], enqueuedTime[%s], isInclusive[%s]",
            offset, sequenceNumber,
            enqueuedDateTime != null ? enqueuedDateTime.toEpochMilli() : "null",
            isInclusive);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EventPosition)) {
            return false;
        }

        final EventPosition other = (EventPosition) obj;

        return Objects.equals(isInclusive, other.isInclusive)
            && Objects.equals(offset, other.offset)
            && Objects.equals(sequenceNumber, other.sequenceNumber)
            && Objects.equals(enqueuedDateTime, other.enqueuedDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isInclusive, offset, sequenceNumber, enqueuedDateTime);
    }
}
