// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RequestResponseUtils;
import com.azure.core.amqp.models.AmqpAddress;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.amqp.models.AmqpMessageBodyType;
import com.azure.core.amqp.models.AmqpMessageHeader;
import com.azure.core.amqp.models.AmqpMessageId;
import com.azure.core.amqp.models.AmqpMessageProperties;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.ManagementConstants;
import com.azure.messaging.servicebus.implementation.MessageWithLockToken;
import com.azure.messaging.servicebus.implementation.Messages;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Decimal128;
import org.apache.qpid.proton.amqp.Decimal32;
import org.apache.qpid.proton.amqp.Decimal64;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedByte;
import org.apache.qpid.proton.amqp.UnsignedInteger;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.UnsignedShort;
import org.apache.qpid.proton.amqp.messaging.AmqpSequence;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.DeliveryAnnotations;
import org.apache.qpid.proton.amqp.messaging.Footer;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.amqp.transaction.Declare;
import org.apache.qpid.proton.amqp.transaction.Discharge;
import org.apache.qpid.proton.message.Message;

import java.lang.reflect.Array;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.azure.core.amqp.AmqpMessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SCHEDULED_ENQUEUE_UTC_TIME_NAME;

/**
 * Deserializes and serializes messages to and from Azure Service Bus.
 */
class ServiceBusMessageSerializer implements MessageSerializer {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private final ClientLogger logger = new ClientLogger(ServiceBusMessageSerializer.class);

    /**
     * Gets the serialized size of the AMQP message.
     */
    @Override
    public int getSize(Message amqpMessage) {
        if (amqpMessage == null) {
            return 0;
        }

        int payloadSize = getPayloadSize(amqpMessage);

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
     * ServiceBusMessage}.
     *
     * @param object Concrete object to deserialize.
     *
     * @return A new AMQP message for this {@code object}.
     *
     * @throws IllegalArgumentException if {@code object} is not an instance of {@link ServiceBusMessage}.
     */
    @Override
    public <T> Message serialize(T object) {
        Objects.requireNonNull(object, "'object' to serialize cannot be null.");

        if (!(object instanceof ServiceBusMessage)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Cannot serialize object that is not ServiceBusMessage. Clazz: " + object.getClass()));
        }

        final ServiceBusMessage brokeredMessage = (ServiceBusMessage) object;
        AmqpMessageBodyType brokeredBodyType = brokeredMessage.getRawAmqpMessage().getBody().getBodyType();
        final Message amqpMessage = Proton.message();

        byte[] body;

        if (brokeredBodyType == AmqpMessageBodyType.DATA || brokeredBodyType == null) {
            body = brokeredMessage.getBody().toBytes();
            amqpMessage.setBody(new Data(new Binary(body)));
        } else if (brokeredBodyType == AmqpMessageBodyType.SEQUENCE) {
            List<Object> sequenceList = brokeredMessage.getRawAmqpMessage().getBody().getSequence();
            amqpMessage.setBody(new AmqpSequence(sequenceList));
        } else if (brokeredBodyType == AmqpMessageBodyType.VALUE) {
            amqpMessage.setBody(new AmqpValue(brokeredMessage.getRawAmqpMessage().getBody().getValue()));
        }

        if (brokeredMessage.getApplicationProperties() != null) {
            amqpMessage.setApplicationProperties(new ApplicationProperties(brokeredMessage.getApplicationProperties()));
        }

        if (brokeredMessage.getTimeToLive() != null) {
            amqpMessage.setTtl(brokeredMessage.getTimeToLive().toMillis());
        }

        if (amqpMessage.getProperties() == null) {
            amqpMessage.setProperties(new Properties());
        }
        amqpMessage.setMessageId(brokeredMessage.getMessageId());
        amqpMessage.setContentType(brokeredMessage.getContentType());
        amqpMessage.setCorrelationId(brokeredMessage.getCorrelationId());
        amqpMessage.setSubject(brokeredMessage.getSubject());
        amqpMessage.setReplyTo(brokeredMessage.getReplyTo());
        amqpMessage.setReplyToGroupId(brokeredMessage.getReplyToSessionId());
        amqpMessage.setGroupId(brokeredMessage.getSessionId());

        final AmqpMessageProperties brokeredProperties = brokeredMessage.getRawAmqpMessage().getProperties();

        amqpMessage.setContentEncoding(brokeredProperties.getContentEncoding());
        if (brokeredProperties.getGroupSequence() != null) {
            amqpMessage.setGroupSequence(brokeredProperties.getGroupSequence());
        }
        amqpMessage.getProperties().setTo(brokeredMessage.getTo());
        amqpMessage.getProperties().setUserId(new Binary(brokeredProperties.getUserId()));

        if (brokeredProperties.getAbsoluteExpiryTime() != null) {
            amqpMessage.getProperties().setAbsoluteExpiryTime(Date.from(brokeredProperties.getAbsoluteExpiryTime()
                .toInstant()));
        }
        if (brokeredProperties.getCreationTime() != null) {
            amqpMessage.getProperties().setCreationTime(Date.from(brokeredProperties.getCreationTime().toInstant()));
        }

        //set footer
        amqpMessage.setFooter(new Footer(brokeredMessage.getRawAmqpMessage().getFooter()));

        //set header
        AmqpMessageHeader header = brokeredMessage.getRawAmqpMessage().getHeader();
        if (header.getDeliveryCount() != null) {
            amqpMessage.setDeliveryCount(header.getDeliveryCount());
        }
        if (header.getPriority() != null) {
            amqpMessage.setPriority(header.getPriority());
        }
        if (header.isDurable() != null) {
            amqpMessage.setDurable(header.isDurable());
        }
        if (header.isFirstAcquirer() != null) {
            amqpMessage.setFirstAcquirer(header.isFirstAcquirer());
        }
        if (header.getTimeToLive() != null) {
            amqpMessage.setTtl(header.getTimeToLive().toMillis());
        }

        final Map<Symbol, Object> messageAnnotationsMap = new HashMap<>();
        if (brokeredMessage.getScheduledEnqueueTime() != null) {
            messageAnnotationsMap.put(Symbol.valueOf(SCHEDULED_ENQUEUE_UTC_TIME_NAME.getValue()),
                Date.from(brokeredMessage.getScheduledEnqueueTime().toInstant()));
        }

        final String partitionKey = brokeredMessage.getPartitionKey();
        if (partitionKey != null && !partitionKey.isEmpty()) {
            messageAnnotationsMap.put(Symbol.valueOf(PARTITION_KEY_ANNOTATION_NAME.getValue()),
                brokeredMessage.getPartitionKey());
        }

        amqpMessage.setMessageAnnotations(new MessageAnnotations(messageAnnotationsMap));

        // Set Delivery Annotations.
        final Map<Symbol, Object> deliveryAnnotationsMap = new HashMap<>();

        final Map<String, Object> deliveryAnnotations = brokeredMessage.getRawAmqpMessage()
            .getDeliveryAnnotations();
        for (Map.Entry<String, Object> deliveryEntry : deliveryAnnotations.entrySet()) {
            deliveryAnnotationsMap.put(Symbol.valueOf(deliveryEntry.getKey()), deliveryEntry.getValue());
        }

        amqpMessage.setDeliveryAnnotations(new DeliveryAnnotations(deliveryAnnotationsMap));

        return amqpMessage;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(Message message, Class<T> clazz) {
        Objects.requireNonNull(message, "'message' cannot be null.");
        Objects.requireNonNull(clazz, "'clazz' cannot be null.");

        if (clazz == ServiceBusReceivedMessage.class) {
            return (T) deserializeMessage(message);
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                String.format(Messages.CLASS_NOT_A_SUPPORTED_TYPE, clazz)));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> deserializeList(Message message, Class<T> clazz) {
        if (clazz == ServiceBusReceivedMessage.class) {
            return (List<T>) deserializeListOfMessages(message);
        } else if (clazz == OffsetDateTime.class) {
            return (List<T>) deserializeListOfOffsetDateTime(message);
        } else if (clazz == OffsetDateTime.class) {
            return (List<T>) deserializeListOfOffsetDateTime(message);
        } else if (clazz == Long.class) {
            return (List<T>) deserializeListOfLong(message);
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                String.format(Messages.CLASS_NOT_A_SUPPORTED_TYPE, clazz)));
        }
    }

    private List<Long> deserializeListOfLong(Message amqpMessage) {
        if (amqpMessage.getBody() instanceof AmqpValue) {
            AmqpValue amqpValue = ((AmqpValue) amqpMessage.getBody());
            if (amqpValue.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) amqpValue.getValue();
                Object expirationListObj = responseBody.get(ManagementConstants.SEQUENCE_NUMBERS);

                if (expirationListObj instanceof long[]) {
                    return Arrays.stream((long[]) expirationListObj)
                        .boxed()
                        .collect(Collectors.toList());
                }
            }
        }
        return Collections.emptyList();
    }

    private List<OffsetDateTime> deserializeListOfOffsetDateTime(Message amqpMessage) {
        if (amqpMessage.getBody() instanceof AmqpValue) {
            AmqpValue amqpValue = ((AmqpValue) amqpMessage.getBody());
            if (amqpValue.getValue() instanceof  Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) amqpValue.getValue();
                Object expirationListObj = responseBody.get(ManagementConstants.EXPIRATIONS);

                if (expirationListObj instanceof Date[]) {
                    return Arrays.stream((Date[]) expirationListObj)
                        .map(date -> date.toInstant().atOffset(ZoneOffset.UTC))
                        .collect(Collectors.toList());
                }
            }
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("rawtypes")
    private List<ServiceBusReceivedMessage> deserializeListOfMessages(Message amqpMessage) {
        final List<ServiceBusReceivedMessage> messageList = new ArrayList<>();
        final AmqpResponseCode statusCode = RequestResponseUtils.getStatusCode(amqpMessage);

        if (statusCode != AmqpResponseCode.OK) {
            logger.warning("AMQP response did not contain OK status code. Actual: {}", statusCode);
            return Collections.emptyList();
        }

        final Object responseBodyMap = ((AmqpValue) amqpMessage.getBody()).getValue();

        if (responseBodyMap == null) {
            logger.warning("AMQP response did not contain a body.");
            return Collections.emptyList();
        } else if (!(responseBodyMap instanceof Map)) {
            logger.warning("AMQP response body is not correct instance. Expected: {}. Actual: {}",
                Map.class, responseBodyMap.getClass());
            return Collections.emptyList();
        }

        final Object messages = ((Map) responseBodyMap).get(ManagementConstants.MESSAGES);
        if (messages == null) {
            logger.warning("Response body did not contain key: {}", ManagementConstants.MESSAGES);
            return Collections.emptyList();
        } else if (!(messages instanceof Iterable)) {
            logger.warning("Response body contents is not the correct type. Expected: {}. Actual: {}",
                Iterable.class, messages.getClass());
            return Collections.emptyList();
        }

        for (Object message : (Iterable) messages) {
            if (!(message instanceof Map)) {
                logger.warning("Message inside iterable of message is not correct type. Expected: {}. Actual: {}",
                    Map.class, message.getClass());
                continue;
            }

            final Message responseMessage = Message.Factory.create();
            final Binary messagePayLoad = (Binary) ((Map) message).get(ManagementConstants.MESSAGE);

            responseMessage.decode(messagePayLoad.getArray(), messagePayLoad.getArrayOffset(),
                messagePayLoad.getLength());

            final ServiceBusReceivedMessage receivedMessage = deserializeMessage(responseMessage);

            // if amqp message have lockToken
            if (((Map) message).containsKey(ManagementConstants.LOCK_TOKEN_KEY)) {
                receivedMessage.setLockToken((UUID) ((Map) message).get(ManagementConstants.LOCK_TOKEN_KEY));
            }

            messageList.add(receivedMessage);
        }

        return messageList;
    }

    private ServiceBusReceivedMessage deserializeMessage(Message amqpMessage) {
        final Section body = amqpMessage.getBody();
        AmqpMessageBody amqpMessageBody;
        if (body != null) {
            if (body instanceof Data) {
                final Binary messageData = ((Data) body).getValue();
                amqpMessageBody = AmqpMessageBody.fromData(messageData.getArray());
            } else if (body instanceof AmqpValue) {
                amqpMessageBody = AmqpMessageBody.fromValue(((AmqpValue) body).getValue());
            } else if (body instanceof AmqpSequence) {
                @SuppressWarnings("unchecked")
                List<Object> messageData = ((AmqpSequence) body).getValue();
                amqpMessageBody = AmqpMessageBody.fromSequence(messageData);

            } else {
                logger.warning(String.format(Messages.MESSAGE_NOT_OF_TYPE, body.getType()));
                amqpMessageBody = AmqpMessageBody.fromData(EMPTY_BYTE_ARRAY);
            }
        } else {
            logger.warning(String.format(Messages.MESSAGE_NOT_OF_TYPE, "null"));
            amqpMessageBody = AmqpMessageBody.fromData(EMPTY_BYTE_ARRAY);
        }

        final ServiceBusReceivedMessage brokeredMessage = new ServiceBusReceivedMessage(amqpMessageBody);
        AmqpAnnotatedMessage brokeredAmqpAnnotatedMessage = brokeredMessage.getRawAmqpMessage();

        // Application properties
        ApplicationProperties applicationProperties = amqpMessage.getApplicationProperties();
        if (applicationProperties != null) {
            final Map<String, Object> propertiesValue = applicationProperties.getValue();
            brokeredAmqpAnnotatedMessage.getApplicationProperties().putAll(propertiesValue);
        }

        // Header
        final AmqpMessageHeader brokeredHeader = brokeredAmqpAnnotatedMessage.getHeader();
        brokeredHeader.setTimeToLive(Duration.ofMillis(amqpMessage.getTtl()));
        brokeredHeader.setDeliveryCount(amqpMessage.getDeliveryCount());
        brokeredHeader.setDurable(amqpMessage.getHeader().getDurable());
        brokeredHeader.setFirstAcquirer(amqpMessage.getHeader().getFirstAcquirer());
        brokeredHeader.setPriority(amqpMessage.getPriority());

        // Footer
        final Footer footer = amqpMessage.getFooter();
        if (footer != null && footer.getValue() != null) {
            @SuppressWarnings("unchecked") final Map<Symbol, Object> footerValue = footer.getValue();
            setValues(footerValue, brokeredAmqpAnnotatedMessage.getFooter());

        }

        // Properties
        final AmqpMessageProperties brokeredProperties = brokeredAmqpAnnotatedMessage.getProperties();
        brokeredProperties.setReplyToGroupId(amqpMessage.getReplyToGroupId());
        final String replyTo = amqpMessage.getReplyTo();
        if (replyTo != null) {
            brokeredProperties.setReplyTo(new AmqpAddress(amqpMessage.getReplyTo()));
        }
        final Object messageId = amqpMessage.getMessageId();
        if (messageId != null) {
            brokeredProperties.setMessageId(new AmqpMessageId(messageId.toString()));
        }

        brokeredProperties.setContentType(amqpMessage.getContentType());
        final Object correlationId = amqpMessage.getCorrelationId();
        if (correlationId != null) {
            brokeredProperties.setCorrelationId(new AmqpMessageId(correlationId.toString()));
        }

        final Properties amqpProperties = amqpMessage.getProperties();
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

        brokeredProperties.setSubject(amqpMessage.getSubject());
        brokeredProperties.setGroupId(amqpMessage.getGroupId());
        brokeredProperties.setContentEncoding(amqpMessage.getContentEncoding());
        brokeredProperties.setGroupSequence(amqpMessage.getGroupSequence());
        brokeredProperties.setUserId(amqpMessage.getUserId());

        // DeliveryAnnotations
        final DeliveryAnnotations deliveryAnnotations = amqpMessage.getDeliveryAnnotations();
        if (deliveryAnnotations != null) {
            setValues(deliveryAnnotations.getValue(), brokeredAmqpAnnotatedMessage.getDeliveryAnnotations());
        }

        // Message Annotations
        final MessageAnnotations messageAnnotations = amqpMessage.getMessageAnnotations();
        if (messageAnnotations != null) {
            setValues(messageAnnotations.getValue(), brokeredAmqpAnnotatedMessage.getMessageAnnotations());
        }

        if (amqpMessage instanceof MessageWithLockToken) {
            brokeredMessage.setLockToken(((MessageWithLockToken) amqpMessage).getLockToken());
        }

        return brokeredMessage;
    }

    private static int getPayloadSize(Message msg) {
        if (msg == null || msg.getBody() == null) {
            return 0;
        }

        final Section bodySection = msg.getBody();
        if (bodySection instanceof AmqpValue) {
            return sizeof(((AmqpValue) bodySection).getValue());
        } else if (bodySection instanceof AmqpSequence) {
            return sizeof(((AmqpSequence) bodySection).getValue());
        } else if (bodySection instanceof Data) {
            final Data payloadSection = (Data) bodySection;
            final Binary payloadBytes = payloadSection.getValue();
            return sizeof(payloadBytes);
        } else {
            return 0;
        }
    }

    private void setValues(Map<Symbol, Object> sourceMap, Map<String, Object> targetMap) {
        if (sourceMap != null) {
            for (Map.Entry<Symbol, Object> entry : sourceMap.entrySet()) {
                targetMap.put(entry.getKey().toString(), entry.getValue());
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static int sizeof(Object obj) {
        if (obj == null) {
            return 0;
        }

        if (obj instanceof String) {
            return obj.toString().length() << 1;
        }

        if (obj instanceof Symbol) {
            return ((Symbol) obj).length() << 1;
        }

        if (obj instanceof Byte || obj instanceof UnsignedByte) {
            return Byte.BYTES;
        }

        if (obj instanceof Integer || obj instanceof UnsignedInteger) {
            return Integer.BYTES;
        }

        if (obj instanceof Long || obj instanceof UnsignedLong || obj instanceof Date) {
            return Long.BYTES;
        }

        if (obj instanceof Short || obj instanceof UnsignedShort) {
            return Short.BYTES;
        }

        if (obj instanceof Boolean) {
            return 1;
        }

        if (obj instanceof Character) {
            return 4;
        }

        if (obj instanceof Float) {
            return Float.BYTES;
        }

        if (obj instanceof Double) {
            return Double.BYTES;
        }

        if (obj instanceof UUID) {
            // UUID is internally represented as 16 bytes. But how does ProtonJ encode it? To be safe..
            // we can treat it as a string of 36 chars = 72 bytes. return 72;
            return 16;
        }

        if (obj instanceof Decimal32) {
            return 4;
        }

        if (obj instanceof Decimal64) {
            return 8;
        }

        if (obj instanceof Decimal128) {
            return 16;
        }

        if (obj instanceof Binary) {
            return ((Binary) obj).getLength();
        }

        if (obj instanceof Declare) {
            // Empty declare command takes up 7 bytes.
            return 7;
        }

        if (obj instanceof Discharge) {
            Discharge discharge = (Discharge) obj;
            return 12 + discharge.getTxnId().getLength();
        }

        if (obj instanceof Map) {
            // Size and Count each take a max of 4 bytes
            int size = 8;
            Map map = (Map) obj;
            for (Object value : map.keySet()) {
                size += sizeof(value);
            }

            for (Object value : map.values()) {
                size += sizeof(value);
            }

            return size;
        }

        if (obj instanceof Iterable) {
            // Size and Count each take a max of 4 bytes
            int size = 8;
            for (Object innerObject : (Iterable) obj) {
                size += sizeof(innerObject);
            }

            return size;
        }

        if (obj.getClass().isArray()) {
            // Size and Count each take a max of 4 bytes
            int size = 8;
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                size += sizeof(Array.get(obj, i));
            }

            return size;
        }

        throw new IllegalArgumentException(String.format(Locale.US,
            "Encoding Type: %s is not supported", obj.getClass()));
    }
}
