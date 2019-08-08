// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.MessageConstant;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.AmqpConstants;
import com.azure.messaging.eventhubs.implementation.ErrorContextProvider;
import com.azure.messaging.eventhubs.models.BatchOptions;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A class for aggregating EventData into a single, size-limited, batch that will be treated as a single message when
 * sent to the Azure Event Hubs service.
 *
 * @see EventHubProducer#createBatch()
 * @see EventHubProducer#createBatch(BatchOptions)
 * @see EventHubProducer See EventHubProducer for examples.
 */
public final class EventDataBatch {
    private final ClientLogger logger = new ClientLogger(EventDataBatch.class);
    private final Object lock = new Object();
    private final int maxMessageSize;
    private final String partitionKey;
    private final ErrorContextProvider contextProvider;
    private final List<EventData> events;
    private final byte[] eventBytes;
    private int sizeInBytes;

    EventDataBatch(int maxMessageSize, String partitionKey, ErrorContextProvider contextProvider) {
        this.maxMessageSize = maxMessageSize;
        this.partitionKey = partitionKey;
        this.contextProvider = contextProvider;
        this.events = new LinkedList<>();
        this.sizeInBytes = (maxMessageSize / 65536) * 1024; // reserve 1KB for every 64KB
        this.eventBytes = new byte[maxMessageSize];
    }

    /**
     * Gets the number of {@link EventData events} in the batch.
     *
     * @return The number of {@link EventData events} in the batch.
     */
    public int getSize() {
        return events.size();
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
     * Tries to add an {@link EventData eventData} to the batch.
     *
     * @param eventData The {@link EventData} to add to the batch.
     * @return {@code true} if the event could be added to the batch; {@code false} if the event was too large to fit in
     *         the batch.
     * @throws IllegalArgumentException if {@code eventData} is {@code null}.
     * @throws AmqpException if {@code eventData} is larger than the maximum size of the {@link
     *         EventDataBatch}.
     */
    public boolean tryAdd(final EventData eventData) {
        if (eventData == null) {
            logger.logAndThrow(new IllegalArgumentException("eventData cannot be null"));
            return false;
        }

        final int size;
        try {
            size = getSize(eventData, events.isEmpty());
        } catch (java.nio.BufferOverflowException exception) {
            logger.logAndThrow(new AmqpException(false, ErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED,
                String.format(Locale.US, "Size of the payload exceeded maximum message size: %s kb", maxMessageSize / 1024),
                contextProvider.getErrorContext()));

            return false;
        }

        synchronized (lock) {
            if (this.sizeInBytes + size > this.maxMessageSize) {
                return false;
            }

            this.sizeInBytes += size;
        }

        this.events.add(eventData);
        return true;
    }

    List<EventData> getEvents() {
        return events;
    }

    String getPartitionKey() {
        return this.partitionKey;
    }

    private int getSize(final EventData eventData, final boolean isFirst) {
        Objects.requireNonNull(eventData);

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
    private Message createAmqpMessage(EventData event, String partitionKey) {
        final Message message = Proton.message();

        if (event.properties() != null && !event.properties().isEmpty()) {
            final ApplicationProperties applicationProperties = new ApplicationProperties(event.properties());
            message.setApplicationProperties(applicationProperties);
        }

        if (event.systemProperties() != null) {
            event.systemProperties().forEach((key, value) -> {
                if (EventData.RESERVED_SYSTEM_PROPERTIES.contains(key)) {
                    return;
                }

                final MessageConstant constant = MessageConstant.fromString(key);

                if (constant != null) {
                    switch (constant) {
                        case MESSAGE_ID:
                            message.setMessageId(value);
                            break;
                        case USER_ID:
                            message.setUserId((byte[]) value);
                            break;
                        case TO:
                            message.setAddress((String) value);
                            break;
                        case SUBJECT:
                            message.setSubject((String) value);
                            break;
                        case REPLY_TO:
                            message.setReplyTo((String) value);
                            break;
                        case CORRELATION_ID:
                            message.setCorrelationId(value);
                            break;
                        case CONTENT_TYPE:
                            message.setContentType((String) value);
                            break;
                        case CONTENT_ENCODING:
                            message.setContentEncoding((String) value);
                            break;
                        case ABSOLUTE_EXPIRY_TIME:
                            message.setExpiryTime((long) value);
                            break;
                        case CREATION_TIME:
                            message.setCreationTime((long) value);
                            break;
                        case GROUP_ID:
                            message.setGroupId((String) value);
                            break;
                        case GROUP_SEQUENCE:
                            message.setGroupSequence((long) value);
                            break;
                        case REPLY_TO_GROUP_ID:
                            message.setReplyToGroupId((String) value);
                            break;
                        default:
                            logger.logAndThrow(new IllegalArgumentException(String.format(Locale.US, "Property is not a recognized reserved property name: %s", key)));
                    }
                } else {
                    final MessageAnnotations messageAnnotations = (message.getMessageAnnotations() == null)
                        ? new MessageAnnotations(new HashMap<>())
                        : message.getMessageAnnotations();
                    messageAnnotations.getValue().put(Symbol.getSymbol(key), value);
                    message.setMessageAnnotations(messageAnnotations);
                }
            });
        }

        if (partitionKey != null) {
            final MessageAnnotations messageAnnotations = (message.getMessageAnnotations() == null)
                ? new MessageAnnotations(new HashMap<>())
                : message.getMessageAnnotations();
            messageAnnotations.getValue().put(AmqpConstants.PARTITION_KEY, partitionKey);
            message.setMessageAnnotations(messageAnnotations);
        }

        if (event.body() != null) {
            message.setBody(new Data(Binary.create(event.body())));
        }

        return message;
    }
}
