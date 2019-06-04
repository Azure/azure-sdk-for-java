// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventDataBatch;
import com.microsoft.azure.eventhubs.PayloadSizeExceededException;
import org.apache.qpid.proton.message.Message;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

final class EventDataBatchImpl implements EventDataBatch {

    private final int maxMessageSize;
    private final String partitionKey;
    private final List<EventData> events;
    private final byte[] eventBytes;
    private int currentSize = 0;

    EventDataBatchImpl(final int maxMessageSize, final String partitionKey) {

        this.maxMessageSize = maxMessageSize;
        this.partitionKey = partitionKey;
        this.events = new LinkedList<>();
        this.currentSize = (maxMessageSize / 65536) * 1024; // reserve 1KB for every 64KB
        this.eventBytes = new byte[maxMessageSize];
    }

    public int getSize() {
        return events.size();
    }

    public boolean tryAdd(final EventData eventData) throws PayloadSizeExceededException {

        if (eventData == null) {
            throw new IllegalArgumentException("eventData cannot be null");
        }

        final EventDataImpl eventDataImpl = (EventDataImpl) eventData;

        final int size;
        try {
            size = getSize(eventDataImpl, events.isEmpty());
        } catch (java.nio.BufferOverflowException exception) {
            throw new PayloadSizeExceededException(String.format(Locale.US, "Size of the payload exceeded Maximum message size: %s kb", this.maxMessageSize / 1024));
        }

        if (this.currentSize + size > this.maxMessageSize) {
            return false;
        }

        this.events.add(eventDataImpl);
        this.currentSize += size;
        return true;
    }

    public Iterator<EventData> iterator() {

        return this.events.iterator();
    }

    Iterable<EventData> getInternalIterable() {

        return this.events;
    }

    String getPartitionKey() {

        return this.partitionKey;
    }

    private int getSize(final EventDataImpl eventData, final boolean isFirst) {

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
