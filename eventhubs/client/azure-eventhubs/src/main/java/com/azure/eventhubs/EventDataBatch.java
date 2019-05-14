// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import org.apache.qpid.proton.message.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/*
 * Helper for creating a batch/collection of EventData objects to be used while sending to EventHubs
 */
class EventDataBatch {
    private final int maxMessageSize;
    private final List<EventData> events;
    private final String partitionKey;
    private int currentSize;
    private byte[] eventBytes;

    EventDataBatch(final int maxMessageSize, final String partitionKey) {
        this.partitionKey = partitionKey;
        this.maxMessageSize = maxMessageSize;
        this.events = new LinkedList<>();
        this.currentSize = (maxMessageSize / 65536) * 1024; // reserve 1KB for every 64KB
        this.eventBytes = new byte[maxMessageSize];
    }

    String partitionKey() {
        return partitionKey;
    }

    /**
     * Get the number of events present in this {@link EventDataBatch}
     *
     * @return the EventDataBatch size
     */
    int size() {
        return events.size();
    }

    /**
     * Adds {@link EventData} to {@link EventDataBatch}, if permitted by the batch's size limit.
     *
     * @param eventData The {@link EventData} to add.
     * @return A boolean value indicating if the {@link EventData} addition to this batch/collection was successful or not.
     * @throws PayloadSizeExceededException when a single {@link EventData} instance exceeds maximum allowed size of the batch
     */
    synchronized boolean tryAdd(EventData eventData) throws PayloadSizeExceededException {
        if (eventData == null) {
            throw new IllegalArgumentException("'eventData' cannot be null");
        }

        final int size;
        try {
            size = getSize(eventData, events.isEmpty());
        } catch (java.nio.BufferOverflowException exception) {
            throw new PayloadSizeExceededException(
                String.format(Locale.US, "Size of the payload exceeded Maximum message size: %s kb", this.maxMessageSize / 1024));
        }

        if (this.currentSize + size > this.maxMessageSize) {
            return false;
        }

        this.currentSize += size;
        this.events.add(eventData);

        return true;
    }

    private int getSize(final EventData eventData, final boolean isFirst) {
        final Message amqpMessage = eventData.createAmqpMessage();
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
