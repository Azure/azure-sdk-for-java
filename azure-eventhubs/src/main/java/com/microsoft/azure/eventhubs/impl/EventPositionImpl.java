/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.EventPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public final class EventPositionImpl implements EventPosition {

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(EventPositionImpl.class);

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

    private EventPositionImpl(String o, Long s, Instant d, Boolean i) {
        this.offset = o;
        this.sequenceNumber = s;
        this.dateTime = d;
        this.inclusiveFlag = i;
    }

    public static EventPositionImpl fromOffset(String offset) {
        return EventPositionImpl.fromOffset(offset, false);
    }

    public static EventPositionImpl fromOffset(String offset, boolean inclusiveFlag) {
        return new EventPositionImpl(offset, null, null, inclusiveFlag);
    }

    public static EventPositionImpl fromSequenceNumber(Long sequenceNumber) {
        return EventPositionImpl.fromSequenceNumber(sequenceNumber, false);
    }

    public static EventPositionImpl fromSequenceNumber(Long sequenceNumber, boolean inclusiveFlag) {
        return new EventPositionImpl(null, sequenceNumber, null, inclusiveFlag);
    }

    public static EventPositionImpl fromEnqueuedTime(Instant dateTime) {
        return new EventPositionImpl(null, null, dateTime, null);
    }

    public static EventPositionImpl fromStartOfStream() {
        return new EventPositionImpl(START_OF_STREAM, null, null, true);
    }

    public static EventPositionImpl fromEndOfStream() {
        return new EventPositionImpl(END_OF_STREAM, null, null, false);
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
