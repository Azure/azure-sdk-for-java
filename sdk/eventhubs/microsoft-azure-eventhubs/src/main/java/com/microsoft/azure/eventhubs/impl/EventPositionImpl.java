// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.EventPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Locale;

public final class EventPositionImpl implements EventPosition {

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(EventPositionImpl.class);
    private static final long serialVersionUID = 7304813338251422629L;

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
        return new EventPositionImpl(ClientConstants.START_OF_STREAM, null, null, true);
    }

    public static EventPositionImpl fromEndOfStream() {
        return new EventPositionImpl(ClientConstants.END_OF_STREAM, null, null, false);
    }

    public Long getSequenceNumber() {
        return this.sequenceNumber;
    }

    public Instant getEnqueuedTime() {
        return this.dateTime;
    }

    public String getOffset() {
        return this.offset;
    }

    public boolean getInclusiveFlag() {
        return this.inclusiveFlag;
    }

    String getExpression() {
        // order of preference
        if (this.offset != null) {
            return this.inclusiveFlag
                    ? String.format(AmqpConstants.AMQP_ANNOTATION_FORMAT, AmqpConstants.OFFSET_ANNOTATION_NAME, "=", this.offset)
                    : String.format(AmqpConstants.AMQP_ANNOTATION_FORMAT, AmqpConstants.OFFSET_ANNOTATION_NAME, StringUtil.EMPTY, this.offset);
        }

        if (this.sequenceNumber != null) {
            return this.inclusiveFlag
                    ? String.format(AmqpConstants.AMQP_ANNOTATION_FORMAT, AmqpConstants.SEQUENCE_NUMBER_ANNOTATION_NAME, "=", this.sequenceNumber)
                    : String.format(AmqpConstants.AMQP_ANNOTATION_FORMAT, AmqpConstants.SEQUENCE_NUMBER_ANNOTATION_NAME, StringUtil.EMPTY, this.sequenceNumber);
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
        return String.format(Locale.US, "offset[%s], sequenceNumber[%s], enqueuedTime[%s], inclusiveFlag[%s]",
                this.offset, this.sequenceNumber,
                (this.dateTime != null) ? this.dateTime.toEpochMilli() : "null",
                this.inclusiveFlag);
    }
}
