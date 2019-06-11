package com.azure.eventhubs.implementation;

import com.azure.core.amqp.MessageConstant;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.eventhubs.EventData;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;

import java.util.HashMap;
import java.util.Locale;

class EventDataUtil {
    /**
     * Creates the AMQP message represented by this EventData.
     *
     * @return A new AMQP message for this EventData.
     */
    private static Message toAmqpMessage(EventData eventData, String partitionKey) {
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
                    case ABSOLUTE_EXPRITY_TIME:
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

    private EventDataUtil() {
    }
}
