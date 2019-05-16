// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.eventhubs.implementation.AmqpConstants;

import java.io.Serializable;
import java.time.Instant;
import java.util.Locale;

import static com.azure.eventhubs.implementation.ClientConstants.END_OF_STREAM;
import static com.azure.eventhubs.implementation.ClientConstants.START_OF_STREAM;

/**
 * Defines a position of an {@link EventData} in the event hub partition.
 * The position can be an Offset, Sequence Number, or EnqueuedTime.
 */
public final class EventPosition implements Serializable {
    private static final long serialVersionUID = 7304813338251422629L;

    private final ServiceLogger logger = new ServiceLogger(EventPosition.class);
    private final String offset;
    private final Long sequenceNumber;
    private final Instant dateTime;

    private EventPosition(String offset, Long sequenceNumber, Instant dateTime) {
        this.offset = offset;
        this.sequenceNumber = sequenceNumber;
        this.dateTime = dateTime;
    }

    /**
     * Returns the position for the start of a stream. Provide this position in receiver creation
     * to start receiving from the first available event in the partition.
     *
     * @return An {@link EventPosition} set to the start of an Event Hubs stream.
     */
    public static EventPosition fromStartOfStream() {
        return new EventPosition(START_OF_STREAM, null, null);
    }

    /**
     * Returns the position for the end of a stream. Provide this position in receiver creation
     * to start receiving from the next available event in the partition after the receiver is created.
     *
     * @return An {@link EventPosition} set to the end of an Event Hubs stream.
     */
    public static EventPosition fromEndOfSream() {
        return new EventPosition(END_OF_STREAM, null, null);
    }

    /**
     * Creates a position at the given {@link Instant}.
     *
     * @param dateTime is the enqueued time of the event.
     * @return An {@link EventPosition} object.
     */
    public static EventPosition fromEnqueuedTime(Instant dateTime) {
        return new EventPosition(null, null, dateTime);
    }

    /**
     * Creates a position at the given offset.
     *
     * @param offset is the byte offset of the event.
     * @return An {@link EventPosition} object.
     */
    public static EventPosition fromOffset(String offset) {
        return new EventPosition(offset, null, null);
    }

    /**
     * Creates a position at the given sequence number. The specified event will not be included.
     * Instead, the next event is returned.
     *
     * @param sequenceNumber is the sequence number of the event.
     * @return An {@link EventPosition} object.
     */
    public static EventPosition fromSequenceNumber(long sequenceNumber) {
        return new EventPosition(null, sequenceNumber, null);
    }

    String getExpression() {
        // order of preference
        if (this.offset != null) {
            return String.format(AmqpConstants.AMQP_ANNOTATION_FORMAT, AmqpConstants.OFFSET_ANNOTATION_NAME, "=", this.offset);
        }

        if (this.sequenceNumber != null) {
            return String.format(AmqpConstants.AMQP_ANNOTATION_FORMAT, AmqpConstants.SEQUENCE_NUMBER_ANNOTATION_NAME, "=", this.sequenceNumber);
        }

        if (this.dateTime != null) {
            String ms;
            try {
                ms = Long.toString(this.dateTime.toEpochMilli());
            } catch (ArithmeticException ex) {
                ms = Long.toString(Long.MAX_VALUE);
                logger.asWarning().log(
                    "Receiver not yet created, action[createReceiveLink], warning[starting receiver from epoch+Long.Max]");
            }

            return String.format(AmqpConstants.AMQP_ANNOTATION_FORMAT, AmqpConstants.ENQUEUED_TIME_UTC_ANNOTATION_NAME, "", ms);
        }

        throw new IllegalArgumentException("No starting position was set.");
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "offset[%s], sequenceNumber[%s], enqueuedTime[%s]",
            this.offset, this.sequenceNumber,
            (this.dateTime != null) ? this.dateTime.toEpochMilli() : "null");
    }
}
