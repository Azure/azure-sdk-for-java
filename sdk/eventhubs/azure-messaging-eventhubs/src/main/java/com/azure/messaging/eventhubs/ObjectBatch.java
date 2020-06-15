package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpMessageConstant;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpConstants;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.data.schemaregistry.SchemaRegistryDataSerializer;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Signal;

import java.nio.BufferOverflowException;
import java.util.*;

import static com.azure.core.util.tracing.Tracer.*;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.AZ_NAMESPACE_VALUE;

public final class ObjectBatch<T> extends Batch {
    private final ClientLogger logger = new ClientLogger(ObjectBatch.class));
    private final Object lock = new Object();
    private final Class<T> batchType;
    private final SchemaRegistryDataSerializer registrySerializer;

    ObjectBatch(int maxMessageSize, String partitionId, String partitionKey, Class<T> batchType,
                    ErrorContextProvider contextProvider, TracerProvider tracerProvider,
                    SchemaRegistryDataSerializer registrySerializer, String entityPath, String hostname) {
        super(maxMessageSize, partitionId, partitionKey, contextProvider, tracerProvider, entityPath,
            hostname, new ClientLogger(ObjectBatch.class));
        this.batchType = batchType;
        this.registrySerializer = registrySerializer;
    }

    /**
     * Tries to add an {@link EventData event} to the batch.
     *
     * @param object The {@link EventData} to add to the batch.
     * @return {@code true} if the event could be added to the batch; {@code false} if the event was too large to fit in
     *     the batch.
     * @throws IllegalArgumentException if {@code eventData} is {@code null}.
     * @throws AmqpException if {@code eventData} is larger than the maximum size of the {@link EventDataBatch}.
     */
    public boolean tryAdd(final T object) {
        if (object == null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("object cannot be null"));
        }

        EventData eventData = new EventData(registrySerializer.serialize(object).block());
        EventData event = tracerProvider.isEnabled() ? traceMessageSpan(eventData) : eventData;

        final int size;
        try {
            size = getSize(event, events.isEmpty());
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

        this.events.add(event);
        return true;
    }

    /**
     * Method to start and end a "Azure.EventHubs.message" span and add the "DiagnosticId" as a property of the message.
     *
     * @param eventData The Event to add tracing span for.
     * @return the updated event data object.
     */
    private EventData traceMessageSpan(EventData eventData) {
        Optional<Object> eventContextData = eventData.getContext().getData(SPAN_CONTEXT_KEY);
        if (eventContextData.isPresent()) {
            // if message has context (in case of retries), don't start a message span or add a new context
            return eventData;
        } else {
            // Starting the span makes the sampling decision (nothing is logged at this time)
            Context eventContext = eventData.getContext()
                .addData(AZ_TRACING_NAMESPACE_KEY, AZ_NAMESPACE_VALUE)
                .addData(ENTITY_PATH_KEY, this.entityPath)
                .addData(HOST_NAME_KEY, this.hostname);
            Context eventSpanContext = tracerProvider.startSpan(eventContext, ProcessKind.MESSAGE);
            Optional<Object> eventDiagnosticIdOptional = eventSpanContext.getData(DIAGNOSTIC_ID_KEY);
            if (eventDiagnosticIdOptional.isPresent()) {
                eventData.getProperties().put(DIAGNOSTIC_ID_KEY, eventDiagnosticIdOptional.get().toString());
                tracerProvider.endSpan(eventSpanContext, Signal.complete());
                eventData.addContext(SPAN_CONTEXT_KEY, eventSpanContext);
            }
        }

        return eventData;
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
    private Message createAmqpMessage(EventData event, String partitionKey) {
        final Message message = Proton.message();

        if (event.getProperties() != null && !event.getProperties().isEmpty()) {
            final ApplicationProperties applicationProperties = new ApplicationProperties(event.getProperties());
            message.setApplicationProperties(applicationProperties);
        }

        if (event.getSystemProperties() != null) {
            event.getSystemProperties().forEach((key, value) -> {
                if (EventData.RESERVED_SYSTEM_PROPERTIES.contains(key)) {
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

        if (partitionKey != null) {
            final MessageAnnotations messageAnnotations = (message.getMessageAnnotations() == null)
                ? new MessageAnnotations(new HashMap<>())
                : message.getMessageAnnotations();
            messageAnnotations.getValue().put(AmqpConstants.PARTITION_KEY, partitionKey);
            message.setMessageAnnotations(messageAnnotations);
        }

        message.setBody(new Data(new Binary(event.getBody())));

        return message;
    }
}
