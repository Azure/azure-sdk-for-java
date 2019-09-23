// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.MessageConstant;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.AmqpConstants;
import com.azure.messaging.eventhubs.implementation.ManagementChannel;
import com.azure.messaging.eventhubs.implementation.MessageSerializer;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.Message;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility class for converting {@link EventData} to {@link Message}.
 */
class EventHubMessageSerializer implements MessageSerializer {
    private final ClientLogger logger = new ClientLogger(EventHubMessageSerializer.class);

    /**
     * Maps the set of events given to a collection of AMQP messages.
     */
    public static List<Message> toAmqpMessage(String partitionKey, List<EventData> events) {
        return events.stream().map(event -> toAmqpMessage(partitionKey, event)).collect(Collectors.toList());
    }

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
     * Creates the AMQP message represented by this {@code object}. Currently, only supports serializing
     * {@link EventData}.
     *
     * @param object Concrete object to deserialize.
     * @param clazz Type of the {@code object}.
     *
     * @return A new AMQP message for this {@code object}.
     *
     * @throws IllegalArgumentException if {@code object} is not an instance of {@link EventData}.
     */
    @Override
    public <T> Message serialize(T object, Class<T> clazz) {
        if (!(object instanceof EventData)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Cannot serialize object that is not EventData. Clazz: " + clazz));
        }

        final EventData eventData = (EventData) object;
        final Message message = Proton.message();

        if (eventData.getProperties() != null && !eventData.getProperties().isEmpty()) {
            message.setApplicationProperties(new ApplicationProperties(eventData.getProperties()));
        }

        setSystemProperties(eventData, message);

        if (eventData.getBody() != null) {
            message.setBody(new Data(Binary.create(eventData.getBody())));
        }

        return message;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(Message message, Class<T> clazz) {
        Objects.requireNonNull(message, "'message' cannot be null.");
        Objects.requireNonNull(clazz, "'class' cannot be null.");

        if (clazz == PartitionProperties.class || clazz == EventHubProperties.class) {
            return deserializeManagementResponse(message, clazz);
        } else if (clazz != EventData.class) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Deserialization only supports EventData, PartitionProperties, or EventHubProperties."));
        }

        final Map<Symbol, Object> messageAnnotations = message.getMessageAnnotations().getValue();
        final HashMap<String, Object> receiveProperties = new HashMap<>();

        for (Map.Entry<Symbol, Object> annotation : messageAnnotations.entrySet()) {
            receiveProperties.put(annotation.getKey().toString(), annotation.getValue());
        }

        if (message.getProperties() != null) {
            addMapEntry(receiveProperties, MessageConstant.MESSAGE_ID, message.getMessageId());
            addMapEntry(receiveProperties, MessageConstant.USER_ID, message.getUserId());
            addMapEntry(receiveProperties, MessageConstant.TO, message.getAddress());
            addMapEntry(receiveProperties, MessageConstant.SUBJECT, message.getSubject());
            addMapEntry(receiveProperties, MessageConstant.REPLY_TO, message.getReplyTo());
            addMapEntry(receiveProperties, MessageConstant.CORRELATION_ID, message.getCorrelationId());
            addMapEntry(receiveProperties, MessageConstant.CONTENT_TYPE, message.getContentType());
            addMapEntry(receiveProperties, MessageConstant.CONTENT_ENCODING, message.getContentEncoding());
            addMapEntry(receiveProperties, MessageConstant.ABSOLUTE_EXPIRY_TIME, message.getExpiryTime());
            addMapEntry(receiveProperties, MessageConstant.CREATION_TIME, message.getCreationTime());
            addMapEntry(receiveProperties, MessageConstant.GROUP_ID, message.getGroupId());
            addMapEntry(receiveProperties, MessageConstant.GROUP_SEQUENCE, message.getGroupSequence());
            addMapEntry(receiveProperties, MessageConstant.REPLY_TO_GROUP_ID, message.getReplyToGroupId());
        }

        final Section bodySection = message.getBody();
        ByteBuffer body;
        if (bodySection instanceof Data) {
            Data bodyData = (Data) bodySection;
            body = bodyData.getValue().asByteBuffer();
        } else {
            logger.warning(String.format(Locale.US,
                "Message body type is not of type Data, but type: %s. Not setting body contents.",
                bodySection != null ? bodySection.getType() : "null"));

            body = null;
        }

        final EventData.SystemProperties systemProperties = new EventData.SystemProperties(receiveProperties);
        final EventData eventData = new EventData(body, systemProperties, Context.NONE);
        final Map<String, Object> properties = message.getApplicationProperties() == null
            ? new HashMap<>()
            : message.getApplicationProperties().getValue();

        properties.forEach((key, value) -> eventData.addProperty(key, value));

        message.clear();
        return (T) eventData;
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
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(Locale.US,
                "Class '%s' is not a supported deserializable type.", deserializedType)));
        }
    }

    private static EventHubProperties toEventHubProperties(Map<?, ?> amqpBody) {
        return new EventHubProperties(
            (String) amqpBody.get(ManagementChannel.MANAGEMENT_ENTITY_NAME_KEY),
            ((Date) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_CREATED_AT)).toInstant(),
            (String[]) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_PARTITION_IDS));
    }

    private static PartitionProperties toPartitionProperties(Map<?, ?> amqpBody) {
        return new PartitionProperties(
            (String) amqpBody.get(ManagementChannel.MANAGEMENT_ENTITY_NAME_KEY),
            (String) amqpBody.get(ManagementChannel.MANAGEMENT_PARTITION_NAME_KEY),
            (Long) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_BEGIN_SEQUENCE_NUMBER),
            (Long) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_SEQUENCE_NUMBER),
            (String) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_OFFSET),
            ((Date) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_TIME_UTC)).toInstant(),
            (Boolean) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_PARTITION_IS_EMPTY));
    }

    private static Message toAmqpMessage(String partitionKey, EventData eventData) {
        final Message message = Proton.message();

        if (eventData.getProperties() != null && !eventData.getProperties().isEmpty()) {
            message.setApplicationProperties(new ApplicationProperties(eventData.getProperties()));
        }

        if (!ImplUtils.isNullOrEmpty(partitionKey)) {
            final MessageAnnotations messageAnnotations = message.getMessageAnnotations() == null
                ? new MessageAnnotations(new HashMap<>())
                : message.getMessageAnnotations();
            messageAnnotations.getValue().put(AmqpConstants.PARTITION_KEY, partitionKey);
            message.setMessageAnnotations(messageAnnotations);
        }

        setSystemProperties(eventData, message);

        if (eventData.getBody() != null) {
            message.setBody(new Data(Binary.create(eventData.getBody())));
        }

        return message;
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
                        throw new IllegalArgumentException(
                            String.format(
                                Locale.US,
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

        throw new IllegalArgumentException(String.format(Locale.US, "Encoding Type: %s is not supported",
            obj.getClass()));
    }

    private static void addMapEntry(Map<String, Object> map, MessageConstant key, Object content) {
        if (content == null) {
            return;
        }

        map.put(key.getValue(), content);
    }
}
