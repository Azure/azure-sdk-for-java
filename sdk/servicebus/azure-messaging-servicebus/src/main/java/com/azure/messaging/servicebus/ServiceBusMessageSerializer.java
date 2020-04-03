// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RequestResponseUtils;
import com.azure.core.util.logging.ClientLogger;
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
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.amqp.transaction.Declare;
import org.apache.qpid.proton.amqp.transaction.Discharge;
import org.apache.qpid.proton.message.Message;

import java.lang.reflect.Array;
import java.time.Duration;
import java.time.Instant;
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

/**
 * Deserializes and serializes messages to and from Azure Service Bus.
 */
class ServiceBusMessageSerializer implements MessageSerializer {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final String ENQUEUED_TIME_UTC_NAME = "x-opt-enqueued-time";
    private static final String SCHEDULED_ENQUEUE_TIME_NAME = "x-opt-scheduled-enqueue-time";
    private static final String SEQUENCE_NUMBER_NAME = "x-opt-sequence-number";
    private static final String LOCKED_UNTIL_NAME = "x-opt-locked-until";
    private static final String PARTITION_KEY_NAME = "x-opt-partition-key";
    private static final String VIA_PARTITION_KEY_NAME = "x-opt-via-partition-key";
    private static final String DEAD_LETTER_SOURCE_NAME = "x-opt-deadletter-source";
    private static final String REQUEST_RESPONSE_MESSAGES = "messages";
    private static final String REQUEST_RESPONSE_MESSAGE = "message";
    private static final String REQUEST_RESPONSE_EXPIRATIONS = "expirations";
    private static final String LOCK_TOKEN_KEY = "lock-token";
    private static final String SEQUENCE_NUMBERS = "sequence-numbers";

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
        final Message amqpMessage = Proton.message();
        final byte[] body = brokeredMessage.getBody();

        //TODO (conniey): support AMQP sequence and AMQP value.
        amqpMessage.setBody(new Data(new Binary(body)));

        if (brokeredMessage.getProperties() != null) {
            amqpMessage.setApplicationProperties(new ApplicationProperties(brokeredMessage.getProperties()));
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
        amqpMessage.setSubject(brokeredMessage.getLabel());
        amqpMessage.getProperties().setTo(brokeredMessage.getTo());
        amqpMessage.setReplyTo(brokeredMessage.getReplyTo());
        amqpMessage.setReplyToGroupId(brokeredMessage.getReplyToSessionId());
        amqpMessage.setGroupId(brokeredMessage.getSessionId());

        final Map<Symbol, Object> messageAnnotationsMap = new HashMap<>();
        if (brokeredMessage.getScheduledEnqueueTime() != null) {
            messageAnnotationsMap.put(Symbol.valueOf(SCHEDULED_ENQUEUE_TIME_NAME),
                Date.from(brokeredMessage.getScheduledEnqueueTime()));
        }

        final String partitionKey = brokeredMessage.getPartitionKey();
        if (partitionKey != null && !partitionKey.isEmpty()) {
            messageAnnotationsMap.put(Symbol.valueOf(PARTITION_KEY_NAME), brokeredMessage.getPartitionKey());
        }

        final String viaPartitionKey = brokeredMessage.getViaPartitionKey();
        if (viaPartitionKey != null && !viaPartitionKey.isEmpty()) {
            messageAnnotationsMap.put(Symbol.valueOf(VIA_PARTITION_KEY_NAME), viaPartitionKey);
        }

        amqpMessage.setMessageAnnotations(new MessageAnnotations(messageAnnotationsMap));

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
                "Deserialization only supports ServiceBusReceivedMessage."));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> deserializeList(Message message, Class<T> clazz) {
        if (clazz == ServiceBusReceivedMessage.class) {
            return (List<T>) deserializeListOfMessages(message);
        } else if (clazz == Instant.class) {
            return (List<T>) deserializeListOfInstant(message);
        } else if (clazz == Long.class) {
            return (List<T>) deserializeListOfLong(message);
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Deserialization only supports ServiceBusReceivedMessage."));
        }
    }

    private List<Long> deserializeListOfLong(Message amqpMessage) {
        if (amqpMessage.getBody() instanceof AmqpValue) {
            AmqpValue amqpValue = ((AmqpValue) amqpMessage.getBody());
            if (amqpValue.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) amqpValue.getValue();
                Object expirationListObj = responseBody.get(SEQUENCE_NUMBERS);

                if (expirationListObj instanceof long[]) {
                    return Arrays.stream((long[]) expirationListObj)
                        .boxed()
                        .collect(Collectors.toList());
                }
            }
        }
        return Collections.emptyList();
    }

    private List<Instant> deserializeListOfInstant(Message amqpMessage) {

        if (amqpMessage.getBody() instanceof AmqpValue) {
            AmqpValue amqpValue = ((AmqpValue) amqpMessage.getBody());
            if (amqpValue.getValue() instanceof  Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) amqpValue.getValue();
                Object expirationListObj = responseBody.get(REQUEST_RESPONSE_EXPIRATIONS);

                if (expirationListObj instanceof Date[]) {
                    return Arrays.stream((Date[]) expirationListObj)
                        .map(Date::toInstant)
                        .collect(Collectors.toList());
                }
            }
        }
        return Collections.emptyList();
    }

    private List<ServiceBusReceivedMessage> deserializeListOfMessages(Message amqpMessage) {
        final List<ServiceBusReceivedMessage> messageList = new ArrayList<>();
        final int statusCode = RequestResponseUtils.getResponseStatusCode(amqpMessage);

        if (AmqpResponseCode.fromValue(statusCode) != AmqpResponseCode.OK) {
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

        final Object messages = ((Map) responseBodyMap).get(REQUEST_RESPONSE_MESSAGES);
        if (messages == null) {
            logger.warning("Response body did not contain key: {}", REQUEST_RESPONSE_MESSAGES);
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
            final Binary messagePayLoad = (Binary) ((Map) message).get(REQUEST_RESPONSE_MESSAGE);

            responseMessage.decode(messagePayLoad.getArray(), messagePayLoad.getArrayOffset(),
                messagePayLoad.getLength());

            final ServiceBusReceivedMessage receivedMessage = deserializeMessage(responseMessage);

            // if amqp message have lockToken
            if (((Map) message).containsKey(LOCK_TOKEN_KEY)) {
                receivedMessage.setLockToken((UUID) ((Map) message).get(LOCK_TOKEN_KEY));
            }

            messageList.add(receivedMessage);
        }

        return messageList;
    }

    private ServiceBusReceivedMessage deserializeMessage(Message amqpMessage) {
        final ServiceBusReceivedMessage brokeredMessage;
        final Section body = amqpMessage.getBody();
        if (body != null) {
            //TODO (conniey): Support other AMQP types like AmqpValue and AmqpSequence.
            if (body instanceof Data) {
                final Binary messageData = ((Data) body).getValue();
                final byte[] bytes = messageData.getArray();
                brokeredMessage = new ServiceBusReceivedMessage(bytes);
            } else {
                logger.warning(String.format(Messages.MESSAGE_NOT_OF_TYPE, body.getType()));
                brokeredMessage = new ServiceBusReceivedMessage(EMPTY_BYTE_ARRAY);
            }
        } else {
            logger.warning(String.format(Messages.MESSAGE_NOT_OF_TYPE, "null"));
            brokeredMessage = new ServiceBusReceivedMessage(EMPTY_BYTE_ARRAY);
        }

        // Application properties
        ApplicationProperties applicationProperties = amqpMessage.getApplicationProperties();
        if (applicationProperties != null) {
            brokeredMessage.getProperties().putAll(applicationProperties.getValue());
        }

        // Header
        brokeredMessage.setTimeToLive(Duration.ofMillis(amqpMessage.getTtl()));
        brokeredMessage.setDeliveryCount(amqpMessage.getDeliveryCount());

        // Properties
        final Object messageId = amqpMessage.getMessageId();
        if (messageId != null) {
            brokeredMessage.setMessageId(messageId.toString());
        }

        brokeredMessage.setContentType(amqpMessage.getContentType());
        final Object correlationId = amqpMessage.getCorrelationId();
        if (correlationId != null) {
            brokeredMessage.setCorrelationId(correlationId.toString());
        }

        final Properties properties = amqpMessage.getProperties();
        if (properties != null) {
            brokeredMessage.setTo(properties.getTo());
        }

        brokeredMessage.setLabel(amqpMessage.getSubject());
        brokeredMessage.setReplyTo(amqpMessage.getReplyTo());
        brokeredMessage.setReplyToSessionId(amqpMessage.getReplyToGroupId());
        brokeredMessage.setSessionId(amqpMessage.getGroupId());

        // Message Annotations
        final MessageAnnotations messageAnnotations = amqpMessage.getMessageAnnotations();
        if (messageAnnotations != null) {
            Map<Symbol, Object> messageAnnotationsMap = messageAnnotations.getValue();
            if (messageAnnotationsMap != null) {
                for (Map.Entry<Symbol, Object> entry : messageAnnotationsMap.entrySet()) {
                    final String key = entry.getKey().toString();
                    final Object value = entry.getValue();

                    switch (key) {
                        case ENQUEUED_TIME_UTC_NAME:
                            brokeredMessage.setEnqueuedTime(((Date) value).toInstant());
                            break;
                        case SCHEDULED_ENQUEUE_TIME_NAME:
                            brokeredMessage.setScheduledEnqueueTime(((Date) value).toInstant());
                            break;
                        case SEQUENCE_NUMBER_NAME:
                            brokeredMessage.setSequenceNumber((long) value);
                            break;
                        case LOCKED_UNTIL_NAME:
                            brokeredMessage.setLockedUntil(((Date) value).toInstant());
                            break;
                        case PARTITION_KEY_NAME:
                            brokeredMessage.setPartitionKey((String) value);
                            break;
                        case VIA_PARTITION_KEY_NAME:
                            brokeredMessage.setViaPartitionKey((String) value);
                            break;
                        case DEAD_LETTER_SOURCE_NAME:
                            brokeredMessage.setDeadLetterSource((String) value);
                            break;
                        default:
                            logger.info("Unrecognised key: {}, value: {}", key, value);
                            break;
                    }
                }
            }
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
