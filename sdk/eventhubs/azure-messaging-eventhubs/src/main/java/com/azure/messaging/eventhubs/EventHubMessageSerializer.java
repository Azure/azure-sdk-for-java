// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.models.AmqpAddress;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageHeader;
import com.azure.core.amqp.models.AmqpMessageId;
import com.azure.core.amqp.models.AmqpMessageProperties;
import com.azure.core.exception.AzureException;
import com.azure.core.util.BinaryData;
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
import org.apache.qpid.proton.amqp.messaging.Footer;
import org.apache.qpid.proton.amqp.messaging.Header;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.Message;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.amqp.AmqpMessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SCHEDULED_ENQUEUE_UTC_TIME_NAME;
import static com.azure.messaging.eventhubs.implementation.ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_OFFSET;
import static com.azure.messaging.eventhubs.implementation.ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_SEQUENCE_NUMBER;
import static com.azure.messaging.eventhubs.implementation.ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_TIME_UTC;
import static com.azure.messaging.eventhubs.implementation.ManagementChannel.MANAGEMENT_RESULT_RUNTIME_INFO_RETRIEVAL_TIME_UTC;

/**
 * Utility class for converting {@link EventData} to {@link Message}.
 */
class EventHubMessageSerializer implements MessageSerializer {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

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
        final byte[] body = eventData.getBody();

        //TODO (conniey): support AMQP sequence and AMQP value.
        message.setBody(new Data(new Binary(body)));

        if (eventData.getProperties() != null && !eventData.getProperties().isEmpty()) {
            message.setApplicationProperties(new ApplicationProperties(eventData.getProperties()));
        }

        if (message.getProperties() == null) {
            message.setProperties(new Properties());
        }

        message.setMessageId(eventData.getMessageId());
        message.setContentType(eventData.getContentType());
        message.setCorrelationId(eventData.getCorrelationId());

        final AmqpMessageProperties brokeredProperties = eventData.getRawAmqpMessage().getProperties();

        message.setContentEncoding(brokeredProperties.getContentEncoding());
        if (brokeredProperties.getGroupSequence() != null) {
            message.setGroupSequence(brokeredProperties.getGroupSequence());
        }
        message.getProperties().setUserId(new Binary(brokeredProperties.getUserId()));

        if (brokeredProperties.getAbsoluteExpiryTime() != null) {
            message.getProperties().setAbsoluteExpiryTime(Date.from(brokeredProperties.getAbsoluteExpiryTime()
                .toInstant()));
        }
        if (brokeredProperties.getCreationTime() != null) {
            message.getProperties().setCreationTime(Date.from(brokeredProperties.getCreationTime().toInstant()));
        }

        //set footer
        message.setFooter(new Footer(eventData.getRawAmqpMessage().getFooter()));

        //set header
        AmqpMessageHeader header = eventData.getRawAmqpMessage().getHeader();
        if (header.getDeliveryCount() != null) {
            message.setDeliveryCount(header.getDeliveryCount());
        }
        if (header.getPriority() != null) {
            message.setPriority(header.getPriority());
        }
        if (header.isDurable() != null) {
            message.setDurable(header.isDurable());
        }
        if (header.isFirstAcquirer() != null) {
            message.setFirstAcquirer(header.isFirstAcquirer());
        }
        if (header.getTimeToLive() != null) {
            message.setTtl(header.getTimeToLive().toMillis());
        }

        final Map<Symbol, Object> messageAnnotationsMap = new HashMap<>();

        final String partitionKey = eventData.getPartitionKey();
        if (partitionKey != null && !partitionKey.isEmpty()) {
            messageAnnotationsMap.put(Symbol.valueOf(PARTITION_KEY_ANNOTATION_NAME.getValue()),
                eventData.getPartitionKey());
        }

        message.setMessageAnnotations(new MessageAnnotations(messageAnnotationsMap));

        // Set Delivery Annotations.
        final Map<Symbol, Object> deliveryAnnotationsMap = new HashMap<>();

        final Map<String, Object> deliveryAnnotations = eventData.getRawAmqpMessage()
            .getDeliveryAnnotations();
        for (Map.Entry<String, Object> deliveryEntry : deliveryAnnotations.entrySet()) {
            deliveryAnnotationsMap.put(Symbol.valueOf(deliveryEntry.getKey()), deliveryEntry.getValue());
        }

        message.setDeliveryAnnotations(new DeliveryAnnotations(deliveryAnnotationsMap));

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
        final byte[] bytes;
        final Section body = message.getBody();
        if (body != null) {
            //TODO (conniey): Support other AMQP types like AmqpValue and AmqpSequence.
            if (body instanceof Data) {
                final Binary messageData = ((Data) body).getValue();
                bytes = messageData.getArray();
            } else {
                logger.warning(String.format(Messages.MESSAGE_NOT_OF_TYPE, body.getType()));
                bytes = EMPTY_BYTE_ARRAY;
            }
        } else {
            logger.warning(String.format(Messages.MESSAGE_NOT_OF_TYPE, "null"));
            bytes = EMPTY_BYTE_ARRAY;
        }

        final EventData brokeredMessage = new EventData(BinaryData.fromBytes(bytes));
        AmqpAnnotatedMessage brokeredAmqpAnnotatedMessage = brokeredMessage.getRawAmqpMessage();

        // Application properties
        ApplicationProperties applicationProperties = message.getApplicationProperties();
        if (applicationProperties != null) {
            final Map<String, Object> propertiesValue = applicationProperties.getValue();
            brokeredAmqpAnnotatedMessage.getApplicationProperties().putAll(propertiesValue);
        }

        // Header
        // Footer
        final Header header = message.getHeader();
        if (header != null) {
            final AmqpMessageHeader brokeredHeader = brokeredAmqpAnnotatedMessage.getHeader();
            brokeredHeader.setTimeToLive(Duration.ofMillis(message.getTtl()));
            brokeredHeader.setDeliveryCount(message.getDeliveryCount());
            brokeredHeader.setDurable(message.getHeader().getDurable());
            brokeredHeader.setFirstAcquirer(message.getHeader().getFirstAcquirer());
            brokeredHeader.setPriority(message.getPriority());
        }

        // Footer
        final Footer footer = message.getFooter();
        if (footer != null && footer.getValue() != null) {
            @SuppressWarnings("unchecked") final Map<Symbol, Object> footerValue = footer.getValue();
            setValues(footerValue, brokeredAmqpAnnotatedMessage.getFooter());

        }

        // Properties
        final AmqpMessageProperties brokeredProperties = brokeredAmqpAnnotatedMessage.getProperties();
        brokeredProperties.setReplyToGroupId(message.getReplyToGroupId());
        final String replyTo = message.getReplyTo();
        if (replyTo != null) {
            brokeredProperties.setReplyTo(new AmqpAddress(message.getReplyTo()));
        }
        final Object messageId = message.getMessageId();
        if (messageId != null) {
            brokeredProperties.setMessageId(new AmqpMessageId(messageId.toString()));
        }

        brokeredProperties.setContentType(message.getContentType());
        final Object correlationId = message.getCorrelationId();
        if (correlationId != null) {
            brokeredProperties.setCorrelationId(new AmqpMessageId(correlationId.toString()));
        }

        final Properties amqpProperties = message.getProperties();
        if (amqpProperties != null) {
            final String to = amqpProperties.getTo();
            if (to != null) {
                brokeredProperties.setTo(new AmqpAddress(amqpProperties.getTo()));
            }

            if (amqpProperties.getAbsoluteExpiryTime() != null) {
                brokeredProperties.setAbsoluteExpiryTime(amqpProperties.getAbsoluteExpiryTime().toInstant()
                    .atOffset(ZoneOffset.UTC));
            }
            if (amqpProperties.getCreationTime() != null) {
                brokeredProperties.setCreationTime(amqpProperties.getCreationTime().toInstant()
                    .atOffset(ZoneOffset.UTC));
            }
        }

        brokeredProperties.setSubject(message.getSubject());
        brokeredProperties.setGroupId(message.getGroupId());
        brokeredProperties.setContentEncoding(message.getContentEncoding());
        brokeredProperties.setGroupSequence(message.getGroupSequence());
        brokeredProperties.setUserId(message.getUserId());

        // DeliveryAnnotations
        final DeliveryAnnotations deliveryAnnotations = message.getDeliveryAnnotations();
        if (deliveryAnnotations != null) {
            setValues(deliveryAnnotations.getValue(), brokeredAmqpAnnotatedMessage.getDeliveryAnnotations());
        }

        // Message Annotations
        final MessageAnnotations messageAnnotations = message.getMessageAnnotations();
        if (messageAnnotations != null) {
            setValues(messageAnnotations.getValue(), brokeredAmqpAnnotatedMessage.getMessageAnnotations());
        }

        return brokeredMessage;
    }

    private void setValues(Map<Symbol, Object> sourceMap, Map<String, Object> targetMap) {
        if (sourceMap != null) {
            for (Map.Entry<Symbol, Object> entry : sourceMap.entrySet()) {
                targetMap.put(entry.getKey().toString(), entry.getValue());
            }
        }
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
