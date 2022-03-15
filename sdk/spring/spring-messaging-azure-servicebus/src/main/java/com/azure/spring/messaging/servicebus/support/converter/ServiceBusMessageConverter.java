// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.support.converter;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.converter.AbstractAzureMessageConverter;
import com.azure.spring.messaging.servicebus.support.ServiceBusMessageHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.azure.spring.messaging.implementation.converter.ObjectMapperHolder.OBJECT_MAPPER;

/**
 * A converter to turn a {@link org.springframework.messaging.Message} to {@link ServiceBusMessage} and turn a {@link
 * ServiceBusReceivedMessage} to {@link org.springframework.messaging.Message}
 *
 */
public class ServiceBusMessageConverter
    extends AbstractAzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusMessageConverter.class);
    private final ObjectMapper objectMapper;

    /**
     * Construct the message converter with default {@code ObjectMapper}.
     */
    public ServiceBusMessageConverter() {
        this(OBJECT_MAPPER);
    }

    /**
     * Construct the message converter with customized {@code ObjectMapper}.
     * @param objectMapper the object mapper.
     */
    public ServiceBusMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    protected byte[] getPayload(ServiceBusReceivedMessage azureMessage) {
        final BinaryData body = azureMessage.getBody();
        return body.toBytes();
    }

    @Override
    protected ServiceBusMessage fromString(String payload) {
        return new ServiceBusMessage(payload);
    }

    @Override
    protected ServiceBusMessage fromByte(byte[] payload) {
        return new ServiceBusMessage(payload);
    }

    @Override
    protected void setCustomHeaders(MessageHeaders headers, ServiceBusMessage message) {

        Map<String, Object> headersCopy = new HashMap<>(headers);
        PropertyMapper propertyMapper = new PropertyMapper();

        // Spring MessageHeaders
        propertyMapper
            .from(headersCopy.remove(MessageHeaders.ID))
            .to(id -> message.setMessageId(id.toString()));
        propertyMapper
            .from(headersCopy.remove(MessageHeaders.CONTENT_TYPE))
            .to(type -> message.setContentType(type.toString()));
        propertyMapper
            .from(headersCopy.remove(MessageHeaders.REPLY_CHANNEL))
            .to(channel -> message.setReplyTo(channel.toString()));

        // AzureHeaders.
        propertyMapper
            .from(headersCopy.remove(AzureHeaders.SCHEDULED_ENQUEUE_MESSAGE))
            .to(time -> {
                Instant instant = Instant.now().plus(Duration.ofMillis((Integer) time));
                OffsetDateTime dateTime = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
                message.setScheduledEnqueueTime(dateTime);
            });

        // ServiceBusMessageHeaders, service bus headers have highest priority.
        propertyMapper
            .from(headersCopy.remove(ServiceBusMessageHeaders.MESSAGE_ID))
            .to(id -> {
                message.setMessageId(id.toString());
                logOverriddenHeaders(ServiceBusMessageHeaders.MESSAGE_ID, MessageHeaders.ID, headers);
            });
        propertyMapper
            .from(headersCopy.remove(ServiceBusMessageHeaders.TIME_TO_LIVE))
            .to(time -> message.setTimeToLive((Duration) time));
        propertyMapper
            .from(headersCopy.remove(ServiceBusMessageHeaders.SCHEDULED_ENQUEUE_TIME))
            .to(time -> {
                message.setScheduledEnqueueTime((OffsetDateTime) time);
                logOverriddenHeaders(ServiceBusMessageHeaders.SCHEDULED_ENQUEUE_TIME, AzureHeaders.SCHEDULED_ENQUEUE_MESSAGE, headers);
            });
        propertyMapper
            .from(headersCopy.remove(ServiceBusMessageHeaders.SESSION_ID))
            .to(id -> message.setSessionId(id.toString()));
        propertyMapper
            .from(headersCopy.remove(ServiceBusMessageHeaders.CORRELATION_ID))
            .to(id -> message.setCorrelationId(id.toString()));
        propertyMapper
            .from(headersCopy.remove(ServiceBusMessageHeaders.TO))
            .to(to -> message.setTo(to.toString()));
        propertyMapper
            .from(headersCopy.remove(ServiceBusMessageHeaders.REPLY_TO_SESSION_ID))
            .to(id -> message.setReplyToSessionId(id.toString()));

        if (StringUtils.hasText(message.getSessionId())) {
            if (!ObjectUtils.isEmpty(headers.get(ServiceBusMessageHeaders.PARTITION_KEY)) && !ObjectUtils.nullSafeEquals(message.getSessionId(),
                headers.get(ServiceBusMessageHeaders.PARTITION_KEY))) {
                LOGGER.warn("Different session id and partition key are set in the message header, and the partition "
                    + "key header will be overwritten by the session id header.");
            }
            message.setPartitionKey(message.getSessionId());
            headersCopy.remove(ServiceBusMessageHeaders.PARTITION_KEY);
        } else {
            propertyMapper
                .from(headersCopy.remove(ServiceBusMessageHeaders.PARTITION_KEY))
                .to(key -> message.setPartitionKey(key.toString()));
        }

        propertyMapper
            .from(headersCopy.remove(ServiceBusMessageHeaders.SUBJECT))
            .to(subject -> message.setSubject(subject.toString()));

        headersCopy.forEach((key, value) -> message.getApplicationProperties().put(key, value.toString()));
    }

    @Override
    protected Map<String, Object> buildCustomHeaders(ServiceBusReceivedMessage message) {
        Map<String, Object> headers = new HashMap<>();

        // Spring MessageHeaders
        setValueIfHasText(headers, MessageHeaders.ID, message.getMessageId());
        setValueIfHasText(headers, MessageHeaders.CONTENT_TYPE, message.getContentType());
        setValueIfHasText(headers, MessageHeaders.REPLY_CHANNEL, message.getReplyTo());

        // AzureHeaders.
        // Does not have SCHEDULED_ENQUEUE_MESSAGE, because it's meaningless in receiver side.

        // ServiceBusMessageHeaders.
        setValueIfHasText(headers, ServiceBusMessageHeaders.CORRELATION_ID, message.getCorrelationId());
        setValueIfHasText(headers, ServiceBusMessageHeaders.MESSAGE_ID, message.getMessageId());
        setValueIfHasText(headers, ServiceBusMessageHeaders.PARTITION_KEY, message.getPartitionKey());
        setValueIfHasText(headers, ServiceBusMessageHeaders.TO, message.getTo());
        setValueIfPresent(headers, ServiceBusMessageHeaders.TIME_TO_LIVE, message.getTimeToLive());
        setValueIfPresent(headers, ServiceBusMessageHeaders.SCHEDULED_ENQUEUE_TIME, message.getScheduledEnqueueTime());
        setValueIfHasText(headers, ServiceBusMessageHeaders.REPLY_TO_SESSION_ID, message.getReplyToSessionId());
        setValueIfHasText(headers, ServiceBusMessageHeaders.SESSION_ID, message.getSessionId());
        setValueIfHasText(headers, ServiceBusMessageHeaders.DEAD_LETTER_ERROR_DESCRIPTION, message.getDeadLetterErrorDescription());
        setValueIfHasText(headers, ServiceBusMessageHeaders.DEAD_LETTER_REASON, message.getDeadLetterReason());
        setValueIfHasText(headers, ServiceBusMessageHeaders.DEAD_LETTER_SOURCE, message.getDeadLetterSource());
        setValueIfPresent(headers, ServiceBusMessageHeaders.DELIVERY_COUNT, message.getDeliveryCount());
        setValueIfPresent(headers, ServiceBusMessageHeaders.ENQUEUED_SEQUENCE_NUMBER, message.getEnqueuedSequenceNumber());
        setValueIfPresent(headers, ServiceBusMessageHeaders.ENQUEUED_TIME, message.getEnqueuedTime());
        setValueIfPresent(headers, ServiceBusMessageHeaders.EXPIRES_AT, message.getExpiresAt());
        setValueIfHasText(headers, ServiceBusMessageHeaders.LOCK_TOKEN, message.getLockToken());
        setValueIfPresent(headers, ServiceBusMessageHeaders.LOCKED_UNTIL, message.getLockedUntil());
        setValueIfPresent(headers, ServiceBusMessageHeaders.SEQUENCE_NUMBER, message.getSequenceNumber());
        setValueIfPresent(headers, ServiceBusMessageHeaders.STATE, message.getState());
        setValueIfHasText(headers, ServiceBusMessageHeaders.SUBJECT, message.getSubject());

        message.getApplicationProperties().forEach(headers::putIfAbsent);
        return Collections.unmodifiableMap(headers);
    }

    private void logOverriddenHeaders(String currentHeader, String overriddenHeader,
                                         MessageHeaders springMessageHeaders) {
        if (springMessageHeaders.containsKey(overriddenHeader)) {
            LOGGER.warn("{} header detected, usage of {} header will be overridden", currentHeader,
                overriddenHeader);
        }
    }

    private void setValueIfHasText(Map<String, Object> map, String key, String value) {
        Optional.ofNullable(value).filter(StringUtils::hasText).ifPresent(s -> map.put(key, s));
    }

    private void setValueIfPresent(Map<String, Object> map, String key, Object value) {
        Optional.ofNullable(value).ifPresent(s -> map.put(key, s));
    }
}
