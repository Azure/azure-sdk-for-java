/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import org.apache.qpid.proton.message.Message;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

/**
 * Helper class for creating a batch/collection of EventData objects to be used while Sending to EventHubs
 */
public final class EventDataBatch implements Iterable<EventData> {

    private final int maxMessageSize;
    private final String partitionKey;
    private final List<EventData> events;
    private final byte[] eventBytes;
    private int currentSize = 0;

    EventDataBatch(final int maxMessageSize, final String partitionKey) {

        this.maxMessageSize = maxMessageSize;
        this.partitionKey = partitionKey;
        this.events = new LinkedList<>();
        this.currentSize = (maxMessageSize / 65536) * 1024; // reserve 1KB for every 64KB
        this.eventBytes = new byte[maxMessageSize];
    }

    /**
     * Get the number of events present in this {@link EventDataBatch}
     */
    public final int getSize() {

        return events.size();
    }

    /**
     * Add's {@link EventData} to {@link EventDataBatch}, if permitted by the batch's size limit.
     * This method is not thread-safe.
     *
     * @param eventData The {@link EventData} to add.
     * @return A boolean value indicating if the {@link EventData} addition to this batch/collection was successful or not.
     */
    public final boolean tryAdd(final EventData eventData) {

        if (eventData == null) {
            throw new IllegalArgumentException("eventData cannot be null");
        }

        final int size = getSize(eventData, events.isEmpty());
        if (this.currentSize + size > this.maxMessageSize)
            return false;

        this.events.add(eventData);
        this.currentSize += size;
        return true;
    }

    @Override
    public Iterator<EventData> iterator() {

        return this.events.iterator();
    }

    Iterable<EventData> getInternalIterable() {

        return this.events;
    }

    String getPartitionKey() {

        return this.partitionKey;
    }

    private final int getSize(final EventData eventData, final boolean isFirst) {

        final Message amqpMessage = this.partitionKey != null ? eventData.toAmqpMessage(this.partitionKey) : eventData.toAmqpMessage();
        int eventSize = amqpMessage.encode(this.eventBytes, 0, maxMessageSize); // actual encoded bytes size
        eventSize += 16; // data section overhead

        if (isFirst) {
            amqpMessage.setBody(null);
            amqpMessage.setApplicationProperties(null);
            amqpMessage.setProperties(null);
            amqpMessage.setDeliveryAnnotations(null);

            eventSize += amqpMessage.encode(this.eventBytes, 0, maxMessageSize);
        }

        return eventSize;
    }
}
