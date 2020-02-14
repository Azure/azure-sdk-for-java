// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpMessageConstant;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;

import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.ProcessKind;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import reactor.core.publisher.Signal;

import java.nio.BufferOverflowException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;

/**
 * A class for aggregating {@link Message} into a single, size-limited, batch. It is treated as a single message when
 * sent to the Azure Service Bus service.
 */
final class MessageBatch {
    private final ClientLogger logger = new ClientLogger(MessageBatch.class);
    private final Object lock = new Object();
    private final int maxMessageSize;
    private final ErrorContextProvider contextProvider;
    private final List<Message> messageList;
    private final byte[] eventBytes;
    private int sizeInBytes;
    private final TracerProvider tracerProvider;

    MessageBatch(int maxMessageSize, ErrorContextProvider contextProvider,
                 TracerProvider tracerProvider) {
        this.maxMessageSize = maxMessageSize;

        this.contextProvider = contextProvider;
        this.messageList = new LinkedList<>();
        this.sizeInBytes = (maxMessageSize / 65536) * 1024; // reserve 1KB for every 64KB
        this.eventBytes = new byte[maxMessageSize];
        this.tracerProvider = tracerProvider;
    }

    /**
     * Gets the number of {@link Message events} in the batch.
     *
     * @return The number of {@link Message events} in the batch.
     */
    public int getCount() {
        return messageList.size();
    }

    /**
     * Gets the maximum size, in bytes, of the {@link MessageBatch}.
     *
     * @return The maximum size, in bytes, of the {@link MessageBatch}.
     */
    public int getMaxSizeInBytes() {
        return maxMessageSize;
    }

    /**
     * Gets the size of the {@link MessageBatch} in bytes.
     *
     * @return the size of the {@link MessageBatch} in bytes.
     */
    public int getSizeInBytes() {
        return this.sizeInBytes;
    }

    /**
     * Tries to add an {@link Message event} to the batch.
     *
     * @param message The {@link Message} to add to the batch.
     * @return {@code true} if the event could be added to the batch; {@code false} if the event was too large to fit in
     *     the batch.
     * @throws IllegalArgumentException if {@code eventData} is {@code null}.
     * @throws AmqpException if {@code eventData} is larger than the maximum size of the {@link MessageBatch}.
     */
    public boolean tryAdd(final Message message) {
        if (message == null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("eventData cannot be null"));
        }
        Message event = tracerProvider.isEnabled() ? traceMessageSpan(message) : message;

        final int size;
        try {
            size = getSize(event, messageList.isEmpty());
        } catch (BufferOverflowException exception) {
            throw logger.logExceptionAsWarning(new AmqpException(false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED,
                String.format(Locale.US, "Size of the payload exceeded maximum message size: %s kb",
                    maxMessageSize / 1024),
                contextProvider.getErrorContext()));
        }

        synchronized (lock) {
            if (this.sizeInBytes + size > this.maxMessageSize) {
                return false;
            }

            this.sizeInBytes += size;
        }

        this.messageList.add(event);
        return true;
    }

    /**
     * Method to start and end a "Azure.EventHubs.message" span and add the "DiagnosticId" as a property of the message.
     *
     * @param message The Event to add tracing span for.
     * @return the updated event data object.
     */
    private Message traceMessageSpan(Message message) {
        Optional<Object> eventContextData = message.getContext().getData(SPAN_CONTEXT_KEY);
        if (eventContextData.isPresent()) {
            // if message has context (in case of retries), don't start a message span or add a new context
            return message;
        } else {
            // Starting the span makes the sampling decision (nothing is logged at this time)
            Context eventSpanContext = tracerProvider.startSpan(message.getContext(), ProcessKind.MESSAGE);
            Optional<Object> eventDiagnosticIdOptional = eventSpanContext.getData(DIAGNOSTIC_ID_KEY);
            if (eventDiagnosticIdOptional.isPresent()) {
                message.getProperties().put(DIAGNOSTIC_ID_KEY, eventDiagnosticIdOptional.get().toString());
                tracerProvider.endSpan(eventSpanContext, Signal.complete());
                message.addContext(SPAN_CONTEXT_KEY, eventSpanContext);
            }
        }

        return message;
    }

    List<Message> getMessageList() {
        return messageList;
    }


    private int getSize(final Message message, final boolean isFirst) {
        Objects.requireNonNull(message, "'eventData' cannot be null.");

        final org.apache.qpid.proton.message.Message amqpMessage = createAmqpMessage(message);
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
    private org.apache.qpid.proton.message.Message createAmqpMessage(Message event) {
        final org.apache.qpid.proton.message.Message message = Proton.message();

        if (event.getProperties() != null && !event.getProperties().isEmpty()) {
            final ApplicationProperties applicationProperties = new ApplicationProperties(event.getProperties());
            message.setApplicationProperties(applicationProperties);
        }

        if (event.getSystemProperties() != null) {
            event.getSystemProperties().forEach((key, value) -> {
                if (Message.RESERVED_SYSTEM_PROPERTIES.contains(key)) {
                    return;
                }

                final AmqpMessageConstant constant = AmqpMessageConstant.fromString(key);

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
                            throw logger.logExceptionAsWarning(new IllegalArgumentException(String.format(Locale.US,
                                "Property is not a recognized reserved property name: %s", key)));
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


        message.setBody(new Data(new Binary(event.getBody())));

        return message;
    }
}
