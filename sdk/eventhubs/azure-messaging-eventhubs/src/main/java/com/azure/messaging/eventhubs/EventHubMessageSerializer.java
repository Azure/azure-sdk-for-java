// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpMessageConstant;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.exception.AzureException;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ManagementChannel;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.DeliveryAnnotations;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.Message;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
        final Message message = Proton.message();

        if (eventData.getProperties() != null && !eventData.getProperties().isEmpty()) {
            message.setApplicationProperties(new ApplicationProperties(eventData.getProperties()));
        }

        setSystemProperties(eventData, message);

        message.setBody(new Data(new Binary(eventData.getBody())));

        return message;
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
        final Map<Symbol, Object> messageAnnotations = message.getMessageAnnotations().getValue();
        final HashMap<String, Object> receiveProperties = new HashMap<>();

        for (Map.Entry<Symbol, Object> annotation : messageAnnotations.entrySet()) {
            receiveProperties.put(annotation.getKey().toString(), annotation.getValue());
        }

        if (message.getProperties() != null) {
            addMapEntry(receiveProperties, AmqpMessageConstant.MESSAGE_ID, message.getMessageId());
            addMapEntry(receiveProperties, AmqpMessageConstant.USER_ID, message.getUserId());
            addMapEntry(receiveProperties, AmqpMessageConstant.TO, message.getAddress());
            addMapEntry(receiveProperties, AmqpMessageConstant.SUBJECT, message.getSubject());
            addMapEntry(receiveProperties, AmqpMessageConstant.REPLY_TO, message.getReplyTo());
            addMapEntry(receiveProperties, AmqpMessageConstant.CORRELATION_ID, message.getCorrelationId());
            addMapEntry(receiveProperties, AmqpMessageConstant.CONTENT_TYPE, message.getContentType());
            addMapEntry(receiveProperties, AmqpMessageConstant.CONTENT_ENCODING, message.getContentEncoding());
            addMapEntry(receiveProperties, AmqpMessageConstant.ABSOLUTE_EXPIRY_TIME, message.getExpiryTime());
            addMapEntry(receiveProperties, AmqpMessageConstant.CREATION_TIME, message.getCreationTime());
            addMapEntry(receiveProperties, AmqpMessageConstant.GROUP_ID, message.getGroupId());
            addMapEntry(receiveProperties, AmqpMessageConstant.GROUP_SEQUENCE, message.getGroupSequence());
            addMapEntry(receiveProperties, AmqpMessageConstant.REPLY_TO_GROUP_ID, message.getReplyToGroupId());
        }

        final Section bodySection = message.getBody();
        byte[] body;
        if (bodySection instanceof Data) {
            Data bodyData = (Data) bodySection;
            body = bodyData.getValue().getArray();
        } else {
            logger.warning(String.format(Messages.MESSAGE_NOT_OF_TYPE,
                bodySection != null ? bodySection.getType() : "null"));

            body = new byte[0];
        }

        final EventData.SystemProperties systemProperties = new EventData.SystemProperties(receiveProperties);
        final EventData eventData = new EventData(body, systemProperties, Context.NONE);
        final Map<String, Object> properties = message.getApplicationProperties() == null
            ? new HashMap<>()
            : message.getApplicationProperties().getValue();

        properties.forEach((key, value) -> eventData.getProperties().put(key, value));

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

    /*
     * Sets AMQP protocol header values on the AMQP message.
     */
    private static void setSystemProperties(EventData eventData, Message message) {
        if (eventData.getSystemProperties() == null || eventData.getSystemProperties().isEmpty()) {
            return;
        }

        eventData.getSystemProperties().forEach((key, value) -> {
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
                        throw new IllegalArgumentException(
                            String.format(
                                "Property is not a recognized reserved property name: %s",
                                key));
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

        throw new IllegalArgumentException(String.format(Messages.ENCODING_TYPE_NOT_SUPPORTED,
            obj.getClass()));
    }

    private static void addMapEntry(Map<String, Object> map, AmqpMessageConstant key, Object content) {
        if (content == null) {
            return;
        }

        map.put(key.getValue(), content);
    }
}
