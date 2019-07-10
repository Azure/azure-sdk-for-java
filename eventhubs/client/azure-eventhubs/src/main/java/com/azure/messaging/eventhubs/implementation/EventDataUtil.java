// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.MessageConstant;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.messaging.eventhubs.EventData;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for converting {@link EventData} to {@link Message}.
 */
public class EventDataUtil {
    /**
     * Maps the set of events given to a collection of AMQP messages.
     */
    public static List<Message> toAmqpMessage(String partitionKey, List<EventData> events) {
        return events.stream().map(event -> toAmqpMessage(partitionKey, event)).collect(Collectors.toList());
    }

    /**
     * Gets the serialized size of the AMQP message.
     */
    static int getDataSerializedSize(Message amqpMessage) {

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
     * Creates the AMQP message represented by this EventData.
     *
     * @return A new AMQP message for this EventData.
     */
    private static Message toAmqpMessage(String partitionKey, EventData eventData) {
        final Message message = Proton.message();

        if (eventData.properties() != null && !eventData.properties().isEmpty()) {
            message.setApplicationProperties(new ApplicationProperties(eventData.properties()));
        }

        if (!ImplUtils.isNullOrEmpty(partitionKey)) {
            final MessageAnnotations messageAnnotations = message.getMessageAnnotations() == null
                ? new MessageAnnotations(new HashMap<>())
                : message.getMessageAnnotations();
            messageAnnotations.getValue().put(AmqpConstants.PARTITION_KEY, partitionKey);
            message.setMessageAnnotations(messageAnnotations);
        }

        setSystemProperties(eventData, message);

        if (eventData.body() != null) {
            message.setBody(new Data(Binary.create(eventData.body())));
        }

        return message;
    }

    /*
     * Sets AMQP protocol header values on the AMQP message.
     */
    private static void setSystemProperties(EventData eventData, Message message) {
        if (eventData.systemProperties() == null || eventData.systemProperties().isEmpty()) {
            return;
        }

        eventData.systemProperties().forEach((key, value) -> {
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
                        throw new IllegalArgumentException(String.format(Locale.US, "Property is not a recognized reserved property name: %s", key));
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

        throw new IllegalArgumentException(String.format(Locale.US, "Encoding Type: %s is not supported", obj.getClass()));
    }

    private EventDataUtil() {
    }
}
