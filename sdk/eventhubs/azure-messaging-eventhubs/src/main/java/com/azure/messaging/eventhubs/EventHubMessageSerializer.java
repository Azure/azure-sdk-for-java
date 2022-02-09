// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.exception.AzureException;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ManagementChannel;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.DeliveryAnnotations;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static com.azure.messaging.eventhubs.implementation.ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_OFFSET;
import static com.azure.messaging.eventhubs.implementation.ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_SEQUENCE_NUMBER;
import static com.azure.messaging.eventhubs.implementation.ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_TIME_UTC;
import static com.azure.messaging.eventhubs.implementation.ManagementChannel.MANAGEMENT_RESULT_RUNTIME_INFO_RETRIEVAL_TIME_UTC;

/**
 * Utility class for converting {@link EventData} to {@link Message}.
 */
class EventHubMessageSerializer implements MessageSerializer {
    private final ClientLogger logger = new ClientLogger(EventHubMessageSerializer.class);
    private static final Symbol LAST_ENQUEUED_SEQUENCE_NUMBER =
        Symbol.getSymbol(MANAGEMENT_RESULT_LAST_ENQUEUED_SEQUENCE_NUMBER);
    private static final Symbol LAST_ENQUEUED_OFFSET = Symbol.getSymbol(MANAGEMENT_RESULT_LAST_ENQUEUED_OFFSET);
    private static final Symbol LAST_ENQUEUED_TIME_UTC = Symbol.getSymbol(MANAGEMENT_RESULT_LAST_ENQUEUED_TIME_UTC);
    private static final Symbol RETRIEVAL_TIME_UTC =
        Symbol.getSymbol(MANAGEMENT_RESULT_RUNTIME_INFO_RETRIEVAL_TIME_UTC);

    /**
     * Gets the serialized size of the AMQP message.
     */
    @Override
    public int getSize(Message amqpMessage) {
        if (amqpMessage == null) {
            return 0;
        }

        int payloadSize = getPayloadSize(amqpMessage);

        // EventData - accepts only PartitionKey - which is a String & stuffed into MessageAnnotation
        final MessageAnnotations messageAnnotations = amqpMessage.getMessageAnnotations();
        final ApplicationProperties applicationProperties = amqpMessage.getApplicationProperties();

        int annotationsSize = 0;
        int applicationPropertiesSize = 0;

        if (messageAnnotations != null) {
            final Map<Symbol, Object> map = messageAnnotations.getValue();

            for (Map.Entry<Symbol, Object> entry : map.entrySet()) {
                final int size = sizeof(entry.getKey()) + sizeof(entry.getValue());
                annotationsSize += size;
            }
        }

        if (applicationProperties != null) {
            final Map<String, Object> map = applicationProperties.getValue();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                final int size = sizeof(entry.getKey()) + sizeof(entry.getValue());
                applicationPropertiesSize += size;
            }
        }

        return annotationsSize + applicationPropertiesSize + payloadSize;
    }

    /**
     * Creates the AMQP message represented by this {@code object}. Currently, only supports serializing {@link
     * EventData}.
     *
     * @param object Concrete object to deserialize.
     *
     * @return A new AMQP message for this {@code object}.
     *
     * @throws IllegalArgumentException if {@code object} is not an instance of {@link EventData}.
     */
    @Override
    public <T> Message serialize(T object) {
        Objects.requireNonNull(object, "'object' to serialize cannot be null.");

        if (!(object instanceof EventData)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Cannot serialize object that is not EventData. Clazz: " + object.getClass()));
        }

        final EventData eventData = (EventData) object;
        final AmqpAnnotatedMessage amqpAnnotatedMessage = eventData.getRawAmqpMessage();
        final Message protonJ = MessageUtils.toProtonJMessage(amqpAnnotatedMessage);

        // Removing any system properties like ENQUEUED TIME, OFFSET, SEQUENCE NUMBER.
        // These values are populated in the case that the user received the event and is
        // resending the event.
        if (protonJ.getMessageAnnotations() != null && protonJ.getMessageAnnotations().getValue() != null) {
            EventData.RESERVED_SYSTEM_PROPERTIES.forEach(key ->
                protonJ.getMessageAnnotations().getValue().remove(Symbol.valueOf(key)));
        }

        return protonJ;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(Message message, Class<T> clazz) {
        Objects.requireNonNull(message, "'message' cannot be null.");
        Objects.requireNonNull(clazz, "'clazz' cannot be null.");

        if (clazz == PartitionProperties.class || clazz == EventHubProperties.class) {
            return deserializeManagementResponse(message, clazz);
        } else if (clazz == EventData.class) {
            return (T) deserializeEventData(message);
        } else if (clazz == LastEnqueuedEventProperties.class) {
            return (T) deserializeEnqueuedEventProperties(message);
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Deserialization only supports EventData, PartitionProperties, or EventHubProperties."));
        }
    }

    @Override
    public <T> List<T> deserializeList(Message message, Class<T> clazz) {
        return Collections.singletonList(deserialize(message, clazz));
    }

    @SuppressWarnings("unchecked")
    private <T> T deserializeManagementResponse(Message message, Class<T> deserializedType) {
        if (!(message.getBody() instanceof AmqpValue)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Expected message.getBody() to be AmqpValue, but is: " + message.getBody()));
        }

        final AmqpValue body = (AmqpValue) message.getBody();
        if (!(body.getValue() instanceof Map)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Expected message.getBody().getValue() to be of type Map"));
        }

        final Map<?, ?> amqpBody = (Map<?, ?>) body.getValue();

        if (deserializedType == PartitionProperties.class) {
            return (T) toPartitionProperties(amqpBody);
        } else if (deserializedType == EventHubProperties.class) {
            return (T) toEventHubProperties(amqpBody);
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                Messages.CLASS_NOT_A_SUPPORTED_TYPE, deserializedType)));
        }
    }

    /**
     * Tries to deserialize {@link LastEnqueuedEventProperties} from an AMQP message.
     *
     * @param message AMQP message from the message broker.
     *
     * @return An instance of {@link LastEnqueuedEventProperties} with extracted properties. Otherwise, {@code null} if
     *     there were no delivery annotations in the message.
     */
    private LastEnqueuedEventProperties deserializeEnqueuedEventProperties(Message message) {
        final DeliveryAnnotations annotations = message.getDeliveryAnnotations();
        if (annotations == null || annotations.getValue() == null) {
            return null;
        }

        final Map<Symbol, Object> deliveryAnnotations = annotations.getValue();
        final Long lastSequenceNumber = getValue(deliveryAnnotations, LAST_ENQUEUED_SEQUENCE_NUMBER, Long.class);
        final String lastEnqueuedOffset = getValue(deliveryAnnotations, LAST_ENQUEUED_OFFSET, String.class);
        final Instant lastEnqueuedTime = getValue(deliveryAnnotations, LAST_ENQUEUED_TIME_UTC, Date.class).toInstant();
        final Instant retrievalTime = getValue(deliveryAnnotations, RETRIEVAL_TIME_UTC, Date.class).toInstant();

        return new LastEnqueuedEventProperties(lastSequenceNumber, Long.valueOf(lastEnqueuedOffset), lastEnqueuedTime,
            retrievalTime);
    }

    private EventData deserializeEventData(Message message) {
        final AmqpAnnotatedMessage amqpAnnotatedMessage = MessageUtils.toAmqpAnnotatedMessage(message);

        // Convert system properties to their respective types.
        final Map<String, Object> messageAnnotations = amqpAnnotatedMessage.getMessageAnnotations();

        if (!messageAnnotations.containsKey(OFFSET_ANNOTATION_NAME.getValue())) {
            throw logger.logExceptionAsError(new IllegalStateException(String.format(Locale.US,
                "offset: %s should always be in map.", OFFSET_ANNOTATION_NAME.getValue())));
        } else if (!messageAnnotations.containsKey(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue())) {
            throw logger.logExceptionAsError(new IllegalStateException(String.format(Locale.US,
                "enqueuedTime: %s should always be in map.", ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue())));
        } else if (!messageAnnotations.containsKey(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue())) {
            throw logger.logExceptionAsError(new IllegalStateException(String.format(Locale.US,
                "enqueuedTime: %s should always be in map.", SEQUENCE_NUMBER_ANNOTATION_NAME.getValue())));
        }

        final Object enqueuedTimeObject = messageAnnotations.get(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue());
        final Instant enqueuedTime;
        if (enqueuedTimeObject instanceof Date) {
            enqueuedTime = ((Date) enqueuedTimeObject).toInstant();
        } else if (enqueuedTimeObject instanceof Instant) {
            enqueuedTime = (Instant) enqueuedTimeObject;
        } else {
            throw logger.logExceptionAsError(new IllegalStateException(new IllegalStateException(
                String.format(Locale.US, "enqueuedTime is not a known type. Value: %s. Type: %s",
                    enqueuedTimeObject, enqueuedTimeObject.getClass()))));
        }

        final String partitionKey = (String) messageAnnotations.get(PARTITION_KEY_ANNOTATION_NAME.getValue());
        final long offset = getAsLong(messageAnnotations, OFFSET_ANNOTATION_NAME.getValue());
        final long sequenceNumber = getAsLong(messageAnnotations, SEQUENCE_NUMBER_ANNOTATION_NAME.getValue());

        // Put the properly converted time back into the dictionary.
        messageAnnotations.put(OFFSET_ANNOTATION_NAME.getValue(), offset);
        messageAnnotations.put(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue(), enqueuedTime);
        messageAnnotations.put(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(), sequenceNumber);

        final SystemProperties systemProperties = new SystemProperties(amqpAnnotatedMessage, offset, enqueuedTime,
            sequenceNumber, partitionKey);
        final EventData eventData = new EventData(amqpAnnotatedMessage, systemProperties, Context.NONE);

        message.clear();
        return eventData;
    }

    private EventHubProperties toEventHubProperties(Map<?, ?> amqpBody) {
        return new EventHubProperties(
            getValue(amqpBody, ManagementChannel.MANAGEMENT_ENTITY_NAME_KEY, String.class),
            getDate(amqpBody, ManagementChannel.MANAGEMENT_RESULT_CREATED_AT),
            getValue(amqpBody, ManagementChannel.MANAGEMENT_RESULT_PARTITION_IDS, String[].class));
    }

    private PartitionProperties toPartitionProperties(Map<?, ?> amqpBody) {
        return new PartitionProperties(
            getValue(amqpBody, ManagementChannel.MANAGEMENT_ENTITY_NAME_KEY, String.class),
            getValue(amqpBody, ManagementChannel.MANAGEMENT_PARTITION_NAME_KEY, String.class),
            getValue(amqpBody, ManagementChannel.MANAGEMENT_RESULT_BEGIN_SEQUENCE_NUMBER, Long.class),
            getValue(amqpBody, MANAGEMENT_RESULT_LAST_ENQUEUED_SEQUENCE_NUMBER, Long.class),
            getValue(amqpBody, ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_OFFSET, String.class),
            getDate(amqpBody, ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_TIME_UTC),
            getValue(amqpBody, ManagementChannel.MANAGEMENT_RESULT_PARTITION_IS_EMPTY, Boolean.class));
    }

    /**
     * Gets the property value as a Long.
     *
     * @param amqpBody Map to get value from.
     * @param key The key to get value of.
     *
     * @return The corresponding long.
     *
     * @throws IllegalStateException if the property is not a long.
     */
    private long getAsLong(Map<String, Object> amqpBody, String key) {
        final Object object = amqpBody.get(key);
        final long value;
        if (object instanceof String) {
            try {
                value = Long.parseLong((String) object);
            } catch (NumberFormatException e) {
                throw logger.logExceptionAsError(new IllegalStateException("'" + key
                    + "' could not be parsed into a Long. Value: " + object, e));
            }
        } else if (object instanceof Long) {
            value = (Long) object;
        } else {
            throw logger.logExceptionAsError(new IllegalStateException(new IllegalStateException(
                String.format(Locale.US, "'" + key + "' value is not a known type. Value: %s. Type: %s",
                    object, object.getClass()))));
        }

        return value;
    }

    private <T> T getValue(Map<?, ?> amqpBody, String key, Class<T> clazz) {
        if (!amqpBody.containsKey(key)) {
            throw logger.logExceptionAsError(new AzureException(
                String.format("AMQP body did not contain expected field '%s'.", key)));
        }

        return getValue(amqpBody.get(key), key, clazz);
    }

    private <T> T getValue(Map<Symbol, Object> amqpBody, Symbol key, Class<T> clazz) {
        if (!amqpBody.containsKey(key)) {
            throw logger.logExceptionAsError(new AzureException(
                String.format("AMQP body did not contain expected field '%s'.", key)));
        }

        return getValue(amqpBody.get(key), key, clazz);
    }

    @SuppressWarnings("unchecked")
    private <T> T getValue(Object value, Object key, Class<T> clazz) {
        if (value == null) {
            throw logger.logExceptionAsError(new AzureException(
                String.format("AMQP body did not contain a value for key '%s'.", key)));
        } else if (value.getClass() != clazz) {
            throw logger.logExceptionAsError(new AzureException(String.format(
                "AMQP body did not contain correct value for key '%s'. Expected class: '%s'. Actual: '%s'",
                key, clazz, value.getClass())));
        }

        return (T) value;
    }

    private Instant getDate(Map<?, ?> amqpBody, String key) {
        final Date value = getValue(amqpBody, key, Date.class);
        return value.toInstant();
    }

    private static int getPayloadSize(Message msg) {

        if (msg == null || msg.getBody() == null) {
            return 0;
        }

        if (msg.getBody() instanceof Data) {
            final Data payloadSection = (Data) msg.getBody();
            if (payloadSection == null) {
                return 0;
            }

            final Binary payloadBytes = payloadSection.getValue();
            if (payloadBytes == null) {
                return 0;
            }

            return payloadBytes.getLength();
        }

        if (msg.getBody() instanceof AmqpValue) {
            final AmqpValue amqpValue = (AmqpValue) msg.getBody();
            if (amqpValue == null) {
                return 0;
            }

            return amqpValue.getValue().toString().length() * 2;
        }

        return 0;
    }

    private static int sizeof(Object obj) {
        if (obj instanceof String) {
            return obj.toString().length() << 1;
        }

        if (obj instanceof Symbol) {
            return ((Symbol) obj).length() << 1;
        }

        if (obj instanceof Integer) {
            return Integer.BYTES;
        }

        if (obj instanceof Long) {
            return Long.BYTES;
        }

        if (obj instanceof Short) {
            return Short.BYTES;
        }

        if (obj instanceof Character) {
            return Character.BYTES;
        }

        if (obj instanceof Float) {
            return Float.BYTES;
        }

        if (obj instanceof Double) {
            return Double.BYTES;
        }

        if (obj instanceof Date) {
            return 32;
        }

        throw new IllegalArgumentException(String.format(Messages.ENCODING_TYPE_NOT_SUPPORTED,
            obj.getClass()));
    }
}
