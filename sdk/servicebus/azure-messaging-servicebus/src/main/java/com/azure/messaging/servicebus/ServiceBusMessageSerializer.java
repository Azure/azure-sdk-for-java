// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpMessageConstant;
import com.azure.core.amqp.implementation.MessageSerializer;


import com.azure.core.exception.AzureException;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.Messages;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Section;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class ServiceBusMessageSerializer implements MessageSerializer {
    // Well-known keys from the management service responses and requests.
    public static final String MANAGEMENT_ENTITY_NAME_KEY = "name";
    public static final String MANAGEMENT_PARTITION_NAME_KEY = "partition";
    public static final String MANAGEMENT_RESULT_PARTITION_IDS = "partition_ids";
    public static final String MANAGEMENT_RESULT_CREATED_AT = "created_at";

    private final ClientLogger logger = new ClientLogger(ServiceBusMessageSerializer.class);


    /**
     * Gets the serialized size of the AMQP message.
     */
    @Override
    public int getSize(org.apache.qpid.proton.message.Message amqpMessage) {
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
     * Message}.
     *
     * @param object Concrete object to deserialize.
     *
     * @return A new AMQP message for this {@code object}.
     *
     * @throws IllegalArgumentException if {@code object} is not an instance of {@link Message}.
     */
    @Override
    public <T> org.apache.qpid.proton.message.Message serialize(T object) {
        Objects.requireNonNull(object, "'object' to serialize cannot be null.");

        if (!(object instanceof Message)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Cannot serialize object that is not EventData. Clazz: " + object.getClass()));
        }

        final Message eventData = (Message) object;
        final org.apache.qpid.proton.message.Message message = Proton.message();

        if (eventData.getProperties() != null && !eventData.getProperties().isEmpty()) {
            message.setApplicationProperties(new ApplicationProperties(eventData.getProperties()));
        }

        setSystemProperties(eventData, message);

        message.setBody(new Data(new Binary(eventData.getBody())));

        return message;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(org.apache.qpid.proton.message.Message message, Class<T> clazz) {
        Objects.requireNonNull(message, "'message' cannot be null.");
        Objects.requireNonNull(clazz, "'clazz' cannot be null.");

        if (clazz == Message.class) {
            return (T) deserializeMessage(message);
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Deserialization only supports ServiceBusProperties."));
        }
    }

    private Message deserializeMessage(org.apache.qpid.proton.message.Message message) {
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

        final Message.SystemProperties systemProperties = new Message.SystemProperties(receiveProperties);
        final Message eventData = new Message(body, systemProperties, Context.NONE);
        final Map<String, Object> properties = message.getApplicationProperties() == null
            ? new HashMap<>()
            : message.getApplicationProperties().getValue();

        properties.forEach((key, value) -> eventData.getProperties().put(key, value));

        message.clear();
        return eventData;
    }

    private <T> T getValue(Map<?, ?> amqpBody, String key, Class<T> clazz) {
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
    private static void setSystemProperties(Message eventData, org.apache.qpid.proton.message.Message message) {
        if (eventData.getSystemProperties() == null || eventData.getSystemProperties().isEmpty()) {
            return;
        }

        eventData.getSystemProperties().forEach((key, value) -> {
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

    private static int getPayloadSize(org.apache.qpid.proton.message.Message msg) {

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

    private static void addMapEntry(Map<String, Object> map, AmqpMessageConstant key, Object content) {
        if (content == null) {
            return;
        }

        map.put(key.getValue(), content);
    }
}
