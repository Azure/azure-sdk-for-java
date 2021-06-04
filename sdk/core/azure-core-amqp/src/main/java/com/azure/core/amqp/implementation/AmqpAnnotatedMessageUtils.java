// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.models.AmqpAddress;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.amqp.models.AmqpMessageHeader;
import com.azure.core.amqp.models.AmqpMessageId;
import com.azure.core.amqp.models.AmqpMessageProperties;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.DeliveryAnnotations;
import org.apache.qpid.proton.amqp.messaging.Footer;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.Message;

import java.time.Duration;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Converts {@link AmqpAnnotatedMessage messages} to and from proton-j messages.
 */
class AmqpAnnotatedMessageUtils {
    private static final ClientLogger CLIENT_LOGGER = new ClientLogger(AmqpAnnotatedMessageUtils.class);
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * Converts an {@link AmqpAnnotatedMessage} to a proton-j message.
     *
     * @param message The message to convert.
     *
     * @return The corresponding proton-j message.
     *
     * @throws NullPointerException if {@code message} is null.
     */
    static Message toProtonJMessage(AmqpAnnotatedMessage message) {
        Objects.requireNonNull(message, "'message' to serialize cannot be null.");

        final Message response = Proton.message();

        //TODO (conniey): support AMQP sequence and AMQP value.
        final AmqpMessageBody body = message.getBody();
        switch (body.getBodyType()) {
            case DATA:
                response.setBody(new Data(new Binary(body.getFirstData())));
                break;
            case VALUE:
            case SEQUENCE:
            default:
                throw CLIENT_LOGGER.logThrowableAsError(new UnsupportedOperationException(
                    "bodyType [" + body.getBodyType() + "] is not supported yet."));
        }

        // Setting message properties.
        final AmqpMessageProperties properties = message.getProperties();
        response.setMessageId(properties.getMessageId());
        response.setContentType(properties.getContentType());
        response.setCorrelationId(properties.getCorrelationId());
        response.setSubject(properties.getSubject());

        final AmqpAddress replyTo = properties.getReplyTo();
        response.setReplyTo(replyTo != null ? replyTo.toString() : null);

        response.setReplyToGroupId(properties.getReplyToGroupId());
        response.setGroupId(properties.getGroupId());
        response.setContentEncoding(properties.getContentEncoding());

        if (properties.getGroupSequence() != null) {
            response.setGroupSequence(properties.getGroupSequence());
        }

        final AmqpAddress messageTo = properties.getTo();
        response.getProperties().setTo(messageTo != null ? messageTo.toString() : null);

        response.getProperties().setUserId(new Binary(properties.getUserId()));

        if (properties.getAbsoluteExpiryTime() != null) {
            response.getProperties().setAbsoluteExpiryTime(
                Date.from(properties.getAbsoluteExpiryTime().toInstant()));
        }

        if (properties.getCreationTime() != null) {
            response.getProperties().setCreationTime(Date.from(properties.getCreationTime().toInstant()));
        }

        // Set header
        final AmqpMessageHeader header = message.getHeader();
        if (header.getTimeToLive() != null) {
            response.setTtl(header.getTimeToLive().toMillis());
        }
        if (header.getDeliveryCount() != null) {
            response.setDeliveryCount(header.getDeliveryCount());
        }
        if (header.getPriority() != null) {
            response.setPriority(header.getPriority());
        }
        if (header.isDurable() != null) {
            response.setDurable(header.isDurable());
        }
        if (header.isFirstAcquirer() != null) {
            response.setFirstAcquirer(header.isFirstAcquirer());
        }
        if (header.getTimeToLive() != null) {
            response.setTtl(header.getTimeToLive().toMillis());
        }

        // Set footer
        response.setFooter(new Footer(message.getFooter()));

        // Set message annotations.
        final Map<Symbol, Object> messageAnnotations = convert(message.getMessageAnnotations());
        response.setMessageAnnotations(new MessageAnnotations(messageAnnotations));

        // Set Delivery Annotations.
        final Map<Symbol, Object> deliveryAnnotations = convert(message.getDeliveryAnnotations());
        response.setDeliveryAnnotations(new DeliveryAnnotations(deliveryAnnotations));

        // Set application properties
        response.setApplicationProperties(new ApplicationProperties(message.getApplicationProperties()));

        return response;
    }

    /**
     * Converts a proton-j message to {@link AmqpAnnotatedMessage}.
     *
     * @param message The message to convert.
     *
     * @return The corresponding {@link AmqpAnnotatedMessage message}.
     *
     * @throws NullPointerException if {@code message} is null.
     */
    static AmqpAnnotatedMessage toAmqpAnnotatedMessage(Message message) {
        final byte[] bytes;
        final Section body = message.getBody();
        if (body != null) {
            //TODO (conniey): Support other AMQP types like AmqpValue and AmqpSequence.
            if (body instanceof Data) {
                final Binary messageData = ((Data) body).getValue();
                bytes = messageData.getArray();
            } else {
                CLIENT_LOGGER.warning("Message not of type Data. Actual: {}",
                    body.getType());
                bytes = EMPTY_BYTE_ARRAY;
            }
        } else {
            CLIENT_LOGGER.warning("Message does not have a body.");
            bytes = EMPTY_BYTE_ARRAY;
        }

        final AmqpAnnotatedMessage response = new AmqpAnnotatedMessage(AmqpMessageBody.fromData(bytes));

        // Application properties
        final ApplicationProperties applicationProperties = message.getApplicationProperties();
        if (applicationProperties != null) {
            final Map<String, Object> propertiesValue = applicationProperties.getValue();
            response.getApplicationProperties().putAll(propertiesValue);
        }

        // Header
        final AmqpMessageHeader responseHeader = response.getHeader();
        responseHeader.setTimeToLive(Duration.ofMillis(message.getTtl()));
        responseHeader.setDeliveryCount(message.getDeliveryCount());
        responseHeader.setDurable(message.getHeader().getDurable());
        responseHeader.setFirstAcquirer(message.getHeader().getFirstAcquirer());
        responseHeader.setPriority(message.getPriority());

        // Footer
        final Footer footer = message.getFooter();
        if (footer != null && footer.getValue() != null) {
            @SuppressWarnings("unchecked") final Map<Symbol, Object> footerValue = footer.getValue();

            setValues(footerValue, response.getFooter());
        }

        // Properties
        final AmqpMessageProperties responseProperties = response.getProperties();
        responseProperties.setReplyToGroupId(message.getReplyToGroupId());
        final String replyTo = message.getReplyTo();
        if (replyTo != null) {
            responseProperties.setReplyTo(new AmqpAddress(message.getReplyTo()));
        }
        final Object messageId = message.getMessageId();
        if (messageId != null) {
            responseProperties.setMessageId(new AmqpMessageId(messageId.toString()));
        }

        responseProperties.setContentType(message.getContentType());
        final Object correlationId = message.getCorrelationId();
        if (correlationId != null) {
            responseProperties.setCorrelationId(new AmqpMessageId(correlationId.toString()));
        }

        final Properties amqpProperties = message.getProperties();
        if (amqpProperties != null) {
            final String to = amqpProperties.getTo();
            if (to != null) {
                responseProperties.setTo(new AmqpAddress(amqpProperties.getTo()));
            }

            if (amqpProperties.getAbsoluteExpiryTime() != null) {
                responseProperties.setAbsoluteExpiryTime(amqpProperties.getAbsoluteExpiryTime().toInstant()
                    .atOffset(ZoneOffset.UTC));
            }
            if (amqpProperties.getCreationTime() != null) {
                responseProperties.setCreationTime(amqpProperties.getCreationTime().toInstant()
                    .atOffset(ZoneOffset.UTC));
            }
        }

        responseProperties.setSubject(message.getSubject());
        responseProperties.setGroupId(message.getGroupId());
        responseProperties.setContentEncoding(message.getContentEncoding());
        responseProperties.setGroupSequence(message.getGroupSequence());
        responseProperties.setUserId(message.getUserId());

        // DeliveryAnnotations
        final DeliveryAnnotations deliveryAnnotations = message.getDeliveryAnnotations();
        if (deliveryAnnotations != null) {
            setValues(deliveryAnnotations.getValue(), response.getDeliveryAnnotations());
        }

        // Message Annotations
        final MessageAnnotations messageAnnotations = message.getMessageAnnotations();
        if (messageAnnotations != null) {
            setValues(messageAnnotations.getValue(), response.getMessageAnnotations());
        }

        return response;
    }

    private static void setValues(Map<Symbol, Object> sourceMap, Map<String, Object> targetMap) {
        if (sourceMap == null) {
            return;
        }

        for (Map.Entry<Symbol, Object> entry : sourceMap.entrySet()) {
            targetMap.put(entry.getKey().toString(), entry.getValue());
        }
    }

    /**
     * Converts a map from it's string keys to use {@link Symbol}.
     *
     * @param sourceMap Source map.
     *
     * @return A map with corresponding keys as symbols.
     */
    private static Map<Symbol, Object> convert(Map<String, Object> sourceMap) {
        if (sourceMap == null) {
            return null;
        }

        return sourceMap.entrySet().stream()
            .collect(HashMap::new,
                (existing, entry) -> existing.put(Symbol.valueOf(entry.getKey()), entry.getValue()),
                (HashMap::putAll));
    }

    /**
     * Private constructor.
     */
    private AmqpAnnotatedMessageUtils() {
    }
}
