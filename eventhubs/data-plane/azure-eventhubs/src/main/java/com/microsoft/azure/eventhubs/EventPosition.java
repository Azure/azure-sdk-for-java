// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import com.microsoft.azure.eventhubs.impl.EventPositionImpl;

import java.io.Serializable;
import java.time.Instant;

/**
 * Defines a position of an {@link EventData} in the event hub partition.
 * The position can be an Offset, Sequence Number, or EnqueuedTime.
 */
public interface EventPosition extends Serializable {

    /**
     * Creates a position at the given offset. The specified event will not be included.
     * Instead, the next event is returned.
     *
     * @param offset is the byte offset of the event.
     * @return An {@link EventPosition} object.
     */
    static EventPosition fromOffset(String offset) {
        return EventPositionImpl.fromOffset(offset);
    }

    /**
     * Creates a position at the given offset.
     *
     * @param offset        is the byte offset of the event.
     * @param inclusiveFlag will include the specified event when set to true; otherwise, the next event is returned.
     * @return An {@link EventPosition} object.
     */
    static EventPosition fromOffset(String offset, boolean inclusiveFlag) {
        return EventPositionImpl.fromOffset(offset, inclusiveFlag);
    }

    /**
     * Creates a position at the given sequence number. The specified event will not be included.
     * Instead, the next event is returned.
     *
     * @param sequenceNumber is the sequence number of the event.
     * @return An {@link EventPosition} object.
     */
    static EventPosition fromSequenceNumber(Long sequenceNumber) {
        return EventPositionImpl.fromSequenceNumber(sequenceNumber);
    }

    /**
     * Creates a position at the given sequence number. The specified event will not be included.
     * Instead, the next event is returned.
     *
     * @param sequenceNumber is the sequence number of the event.
     * @param inclusiveFlag  will include the specified event when set to true; otherwise, the next event is returned.
     * @return An {@link EventPosition} object.
     */
    static EventPosition fromSequenceNumber(Long sequenceNumber, boolean inclusiveFlag) {
        return EventPositionImpl.fromSequenceNumber(sequenceNumber, inclusiveFlag);
    }

    /**
     * Creates a position at the given {@link Instant}.
     *
     * @param dateTime is the enqueued time of the event.
     * @return An {@link EventPosition} object.
     */
    static EventPosition fromEnqueuedTime(Instant dateTime) {
        return EventPositionImpl.fromEnqueuedTime(dateTime);
    }

    /**
     * Returns the position for the start of a stream. Provide this position in receiver creation
     * to start receiving from the first available event in the partition.
     *
     * @return An {@link EventPosition} set to the start of an Event Hubs stream.
     */
    static EventPosition fromStartOfStream() {
        return EventPositionImpl.fromStartOfStream();
    }

    /**
     * Returns the position for the end of a stream. Provide this position in receiver creation
     * to start receiving from the next available event in the partition after the receiver is created.
     *
     * @return An {@link EventPosition} set to the end of an Event Hubs stream.
     */
    static EventPosition fromEndOfStream() {
        return EventPositionImpl.fromEndOfStream();
    }

    /**
     * Gets the sequence number.
     * <p>
     * @return the sequence number.
     */
    Long getSequenceNumber();

    /**
     * Gets the enqueued time.
     * <p>
     * @return the enqueued time.
     */
    Instant getEnqueuedTime();

    /**
     * Gets the offset.
     * <p>
     * @return the offset.
     */
    String getOffset();

    /**
     * Gets the inclusive value.
     * <p>
     * @return the inclusive value.
     */
    boolean getInclusiveFlag();
}
