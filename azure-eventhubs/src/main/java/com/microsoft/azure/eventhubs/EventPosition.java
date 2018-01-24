/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import com.microsoft.azure.eventhubs.amqp.AmqpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Instant;

/**
 * Defines a position of an {@link EventData} in the event hub partition.
 * The position can be an Offset, Sequence Number, or EnqueuedTime.
 */
public class EventPosition implements Serializable {

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(EventPosition.class);

    /**
     * This is a constant defined to represent the start of a partition stream in EventHub.
     */
    static final String START_OF_STREAM = "-1";

    /**
     * This is a constant defined to represent the current end of a partition stream in EventHub.
     * This can be used as an offset argument in receiver creation to start receiving from the latest
     * event, instead of a specific offset or point in time.
     */
    static final String END_OF_STREAM = "@latest";

    private final String offset;
    private final Long sequenceNumber;
    private final Instant dateTime;
    private final Boolean inclusiveFlag;

    private EventPosition(String o, Long s, Instant d, Boolean i) {
        this.offset = o;
        this.sequenceNumber = s;
        this.dateTime = d;
        this.inclusiveFlag = i;
    }

    /**
     * Creates a position at the given offset. The specified event will not be included.
     * Instead, the next event is returned.
     * @param offset    is the byte offset of the event.
     * @return An {@link EventPosition} object.
     */
    public static EventPosition fromOffset(String offset) {
        return EventPosition.fromOffset(offset, false);
    }

    /**
     * Creates a position at the given offset.
     * @param offset    is the byte offset of the event.
     * @param inclusiveFlag will include the specified event when set to true; otherwise, the next event is returned.
     * @return An {@link EventPosition} object.
     */
    public static EventPosition fromOffset(String offset, boolean inclusiveFlag) {
        return new EventPosition(offset, null, null, inclusiveFlag);
    }

    /**
     * Creates a position at the given sequence number. The specified event will not be included.
     * Instead, the next event is returned.
     * @param sequenceNumber is the sequence number of the event.
     * @return An {@link EventPosition} object.
     */
    public static EventPosition fromSequenceNumber(Long sequenceNumber) {
        return EventPosition.fromSequenceNumber(sequenceNumber, false);
    }

    /**
     * Creates a position at the given sequence number. The specified event will not be included.
     * Instead, the next event is returned.
     * @param sequenceNumber    is the sequence number of the event.
     * @param inclusiveFlag         will include the specified event when set to true; otherwise, the next event is returned.
     * @return An {@link EventPosition} object.
     */
    public static EventPosition fromSequenceNumber(Long sequenceNumber, boolean inclusiveFlag) {
        return new EventPosition(null, sequenceNumber, null, inclusiveFlag);
    }

    /**
     * Creates a position at the given {@link Instant}.
     * @param dateTime  is the enqueued time of the event.
     * @return An {@link EventPosition} object.
     */
    public static EventPosition fromEnqueuedTime(Instant dateTime) {
        return new EventPosition(null, null, dateTime, null);
    }

    /**
     * Returns the position for the start of a stream. Provide this position in receiver creation
     * to start receiving from the first available event in the partition.
     *
     * @return An {@link EventPosition} set to the start of an Event Hubs stream.
     */
    public static EventPosition fromStartOfStream() {
        return new EventPosition(START_OF_STREAM, null, null, true);
    }

    /**
     * Returns the position for the end of a stream. Provide this position in receiver creation
     * to start receiving from the next available event in the partition after the receiver is created.
     *
     * @return An {@link EventPosition} set to the end of an Event Hubs stream.
     */
    public static EventPosition fromEndOfStream() {
        return new EventPosition(END_OF_STREAM, null, null, false);
    }

    String getExpression() {
        // order of preference
        if (this.offset != null) {
            return this.inclusiveFlag ?
                    String.format(AmqpConstants.AMQP_ANNOTATION_FORMAT, AmqpConstants.OFFSET_ANNOTATION_NAME, "=", this.offset) :
                    String.format(AmqpConstants.AMQP_ANNOTATION_FORMAT, AmqpConstants.OFFSET_ANNOTATION_NAME, StringUtil.EMPTY, this.offset);
        }

        if (this.sequenceNumber != null) {
            return this.inclusiveFlag ?
                    String.format(AmqpConstants.AMQP_ANNOTATION_FORMAT, AmqpConstants.SEQUENCE_NUMBER_ANNOTATION_NAME, "=", this.sequenceNumber) :
                    String.format(AmqpConstants.AMQP_ANNOTATION_FORMAT, AmqpConstants.SEQUENCE_NUMBER_ANNOTATION_NAME, StringUtil.EMPTY, this.sequenceNumber);
        }

        if (this.dateTime != null) {
            String ms;
            try {
                ms = Long.toString(this.dateTime.toEpochMilli());
            } catch (ArithmeticException ex) {
                ms = Long.toString(Long.MAX_VALUE);
                if (TRACE_LOGGER.isWarnEnabled()) {
                    TRACE_LOGGER.warn(
                            "receiver not yet created, action[createReceiveLink], warning[starting receiver from epoch+Long.Max]");
                }
            }
            return String.format(AmqpConstants.AMQP_ANNOTATION_FORMAT, AmqpConstants.ENQUEUED_TIME_UTC_ANNOTATION_NAME, StringUtil.EMPTY, ms);
        }

        throw new IllegalArgumentException("No starting position was set.");
    }

    @Override
    public String toString() {
        return String.format("offset[%s], sequenceNumber[%s], enqueuedTime[%s], inclusiveFlag[%s]",
                this.offset, this.sequenceNumber,
                (this.dateTime != null) ? this.dateTime.toEpochMilli() : "null",
                this.inclusiveFlag);
    }
}
