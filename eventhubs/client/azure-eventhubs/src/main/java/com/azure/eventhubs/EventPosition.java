// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.eventhubs.implementation.AmqpConstants;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

import static com.azure.core.amqp.MessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.MessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.MessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;

/**
 * Defines a position of an {@link EventData} in the event hub partition.
 * The position can be an Offset, Sequence Number, or EnqueuedTime.
 */
public final class EventPosition {
    /**
     * This is a constant defined to represent the start of a partition stream in EventHub.
     */
    private static final String START_OF_STREAM = "-1";

    /**
     * This is a constant defined to represent the current end of a partition stream in EventHub.
     * This can be used as an offset argument in receiver creation to start receiving from the latest
     * event, instead of a specific offset or point in time.
     */
    private static final String END_OF_STREAM = "@latest";

    private static final EventPosition FIRST_AVAILABLE_EVENT = fromOffset(START_OF_STREAM, false);
    private static final EventPosition NEW_EVENTS_ONLY = fromOffset(END_OF_STREAM, false);

    private final ServiceLogger logger = new ServiceLogger(EventPosition.class);
    private final boolean isInclusive;
    private String offset;
    private Long sequenceNumber;
    private Instant enqueuedDateTime;

    private EventPosition(boolean isInclusive) {
        this.isInclusive = isInclusive;
    }

    /**
     * Returns the position for the start of a stream. Provide this position in receiver creation
     * to start receiving from the first available event in the partition.
     *
     * @return An {@link EventPosition} set to the start of an Event Hubs stream.
     */
    public static EventPosition firstAvailableEvent() {
        return FIRST_AVAILABLE_EVENT;
    }

    /**
     * Corresponds to the end of the partition, where no more events are currently enqueued. Use this position to begin
     * receiving from the next event to be enqueued in the partition after an {@link EventReceiver} is created with this
     * position.
     *
     * @return An {@link EventPosition} set to the end of an Event Hubs stream and listens for new events.
     */
    public static EventPosition newEventsOnly() {
        return NEW_EVENTS_ONLY;
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
        EventPosition position = new EventPosition(false);
        position.enqueuedDateTime = enqueuedDateTime;
        return position;
    }

    /**
     * Corresponds to the event in the partition at the provided offset, inclusive of that event.
     *
     * @param offset The offset of the event within that partition.
     * @return An {@link EventPosition} object.
     */
    public static EventPosition fromOffset(String offset) {
        return fromOffset(offset, true);
    }

    /**
     * Creates a position to an event in the partition at the provided offset. If {@code isInclusive} is true, the
     * event with the same offset is returned. Otherwise, the next event is received.
     *
     * @param offset The offset of an event with respect to its relative position in the
     * @param isInclusive If true, the event with the {@code offset} is included; otherwise, the next event will be
     * received.
     * @return An {@link EventPosition} object.
     */
    public static EventPosition fromOffset(String offset, boolean isInclusive) {
        Objects.requireNonNull(offset);

        EventPosition position = new EventPosition(isInclusive);
        position.offset = offset;
        return position;
    }

    /**
     * Creates a position at the given sequence number. The specified event will not be included. Instead, the next
     * event is returned.
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
     * @param isInclusive If true, the event with the {@code sequenceNumber} is included; otherwise, the next event will
     * be received.
     * @return An {@link EventPosition} object.
     */
    public static EventPosition fromSequenceNumber(long sequenceNumber, boolean isInclusive) {
        EventPosition position = new EventPosition(isInclusive);
        position.sequenceNumber = sequenceNumber;
        return position;
    }

    String getExpression() {
        final String isInclusiveFlag = isInclusive ? "=" : "";

        // order of preference
        if (this.offset != null) {
            return String.format(AmqpConstants.AMQP_ANNOTATION_FORMAT, OFFSET_ANNOTATION_NAME.getValue(), isInclusiveFlag, this.offset);
        }

        if (this.sequenceNumber != null) {
            return String.format(AmqpConstants.AMQP_ANNOTATION_FORMAT, SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(), isInclusiveFlag, this.sequenceNumber);
        }

        if (this.enqueuedDateTime != null) {
            String ms;
            try {
                ms = Long.toString(this.enqueuedDateTime.toEpochMilli());
            } catch (ArithmeticException ex) {
                ms = Long.toString(Long.MAX_VALUE);
                logger.asWarning().log(
                    "Receiver not yet created, action[createReceiveLink], warning[starting receiver from epoch+Long.Max]");
            }

            return String.format(AmqpConstants.AMQP_ANNOTATION_FORMAT, ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue(), isInclusiveFlag, ms);
        }

        throw new IllegalArgumentException("No starting position was set.");
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
