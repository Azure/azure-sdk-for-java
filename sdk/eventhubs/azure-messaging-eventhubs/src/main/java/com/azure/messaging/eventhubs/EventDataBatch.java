// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpConstants;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.MessageUtils;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsTracer;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;

import java.nio.BufferOverflowException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A class for aggregating {@link EventData} into a single, size-limited, batch. It is treated as a single message when
 * sent to the Azure Event Hubs service.
 *
 * @see EventHubProducerClient#createBatch()
 * @see EventHubProducerClient#createBatch(CreateBatchOptions)
 * @see EventHubProducerAsyncClient#createBatch()
 * @see EventHubProducerAsyncClient#createBatch(CreateBatchOptions)
 * @see EventHubClientBuilder See EventHubClientBuilder for examples of building an asynchronous or synchronous
 *     producer.
 */
public final class EventDataBatch {
    private static final ClientLogger LOGGER = new ClientLogger(EventDataBatch.class);
    private final int maxMessageSize;
    private final String partitionKey;
    private final ErrorContextProvider contextProvider;
    private final List<EventData> events;
    private final byte[] eventBytes;
    private final String partitionId;
    private int sizeInBytes;
    private final EventHubsTracer tracer;

    EventDataBatch(int maxMessageSize, String partitionId, String partitionKey, ErrorContextProvider contextProvider,
        EventHubsProducerInstrumentation instrumentation) {
        this.maxMessageSize = maxMessageSize;
        this.partitionKey = partitionKey;
        this.partitionId = partitionId;
        this.contextProvider = contextProvider;
        this.events = new LinkedList<>();
        this.sizeInBytes = (maxMessageSize / 65536) * 1024; // reserve 1KB for every 64KB
        this.eventBytes = new byte[maxMessageSize];
        this.tracer = instrumentation.getTracer();
    }

    /**
     * Gets the number of {@link EventData events} in the batch.
     *
     * @return The number of {@link EventData events} in the batch.
     */
    public int getCount() {
        return events.size();
    }

    /**
     * Gets the maximum size, in bytes, of the {@link EventDataBatch}.
     *
     * @return The maximum size, in bytes, of the {@link EventDataBatch}.
     */
    public int getMaxSizeInBytes() {
        return maxMessageSize;
    }

    /**
     * Gets the size of the {@link EventDataBatch} in bytes.
     *
     * @return the size of the {@link EventDataBatch} in bytes.
     */
    public int getSizeInBytes() {
        return this.sizeInBytes;
    }

    /**
     * Tries to add an {@link EventData event} to the batch.
     *
     * <p>This method is not thread-safe; make sure to synchronize the method access when using multiple threads
     * to add events.</p>
     *
     * @param eventData The {@link EventData} to add to the batch.
     * @return {@code true} if the event could be added to the batch; {@code false} if the event was too large to fit in
     *     the batch.
     * @throws IllegalArgumentException if {@code eventData} is {@code null}.
     * @throws AmqpException if {@code eventData} is larger than the maximum size of the {@link EventDataBatch}.
     */
    public boolean tryAdd(final EventData eventData) {
        if (eventData == null) {
            throw LOGGER.logExceptionAsWarning(new NullPointerException("eventData cannot be null"));
        }

        tracer.reportMessageSpan(eventData, eventData.getContext());

        final int size;
        try {
            size = getSize(eventData, events.isEmpty());
        } catch (BufferOverflowException exception) {
            throw LOGGER.logExceptionAsWarning(new AmqpException(false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED,
                String.format(Locale.US, "Size of the payload exceeded maximum message size: %s kb",
                    maxMessageSize / 1024),
                contextProvider.getErrorContext()));
        }

        if (this.sizeInBytes + size > this.maxMessageSize) {
            return false;
        }

        this.sizeInBytes += size;
        this.events.add(eventData);
        return true;
    }

    List<EventData> getEvents() {
        return events;
    }

    String getPartitionKey() {
        return partitionKey;
    }

    String getPartitionId() {
        return partitionId;
    }

    private int getSize(final EventData eventData, final boolean isFirst) {
        Objects.requireNonNull(eventData, "'eventData' cannot be null.");

        final Message amqpMessage = createAmqpMessage(eventData, partitionKey);
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

    /*
     * Creates the AMQP message represented by the event data
     */
    private static Message createAmqpMessage(EventData event, String partitionKey) {
        final AmqpAnnotatedMessage amqpAnnotatedMessage = event.getRawAmqpMessage();
        final Message protonJ = MessageUtils.toProtonJMessage(amqpAnnotatedMessage);

        if (partitionKey == null) {
            return protonJ;
        }

        if (protonJ.getMessageAnnotations() == null) {
            protonJ.setMessageAnnotations(new MessageAnnotations(new HashMap<>()));
        }

        final MessageAnnotations messageAnnotations = protonJ.getMessageAnnotations();
        messageAnnotations.getValue().put(AmqpConstants.PARTITION_KEY, partitionKey);

        return protonJ;
    }
}
