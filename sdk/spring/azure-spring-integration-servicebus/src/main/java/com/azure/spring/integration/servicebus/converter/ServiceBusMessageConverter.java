// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.converter;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.converter.AbstractAzureMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.CORRELATION_ID;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.MESSAGE_ID;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.PARTITION_KEY;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.REPLY_TO_SESSION_ID;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.SCHEDULED_ENQUEUE_TIME;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.SESSION_ID;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.TIME_TO_LIVE;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.TO;

/**
 * A converter to turn a {@link org.springframework.messaging.Message} to {@link ServiceBusMessage} and turn a {@link
 * ServiceBusReceivedMessage} to {@link org.springframework.messaging.Message}
 *
 * @author Warren Zhu
 */
public class ServiceBusMessageConverter
    extends AbstractAzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> {

    private final ObjectMapper objectMapper;

    public ServiceBusMessageConverter() {
        objectMapper = OBJECT_MAPPER;
    }

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
        return body == null ? null : body.toBytes();
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

        Set<String> copyHeaders = new HashSet<String>();
        headers.forEach((key, value) -> {
            copyHeaders.add(key);
        });
        // Spring MessageHeaders
        getStringHeader(headers, copyHeaders, MessageHeaders.ID).ifPresent(message::setMessageId);
        getStringHeader(headers, copyHeaders, MessageHeaders.CONTENT_TYPE).ifPresent(message::setContentType);
        getStringHeader(headers, copyHeaders, MessageHeaders.REPLY_CHANNEL).ifPresent(message::setReplyTo);

        // AzureHeaders.
        getStringHeader(headers, copyHeaders, AzureHeaders.RAW_ID).ifPresent(message::setMessageId);
        Optional.of(AzureHeaders.SCHEDULED_ENQUEUE_MESSAGE)
                .filter(copyHeaders::remove)
                .map(key -> headers.get(key, Integer.class))
                .map(Duration::ofMillis)
                .map(Instant.now()::plus)
                .map((ins) -> OffsetDateTime.ofInstant(ins, ZoneId.systemDefault()))
                .ifPresent(message::setScheduledEnqueueTime);

        // ServiceBusMessageHeaders, service bus headers have highest priority.
        getStringHeader(headers, copyHeaders, MESSAGE_ID).ifPresent(message::setMessageId);

        Optional.of(TIME_TO_LIVE)
                .filter(copyHeaders::remove)
                .map(key -> headers.get(key, Duration.class))
                .ifPresent(message::setTimeToLive);

        Optional.of(SCHEDULED_ENQUEUE_TIME)
                .filter(copyHeaders::remove)
                .map(key -> headers.get(key, Instant.class))
                .map((ins) -> OffsetDateTime.ofInstant(ins, ZoneId.systemDefault()))
                .ifPresent(message::setScheduledEnqueueTime);

        getStringHeader(headers, copyHeaders, SESSION_ID).ifPresent(message::setSessionId);
        getStringHeader(headers, copyHeaders, CORRELATION_ID).ifPresent(message::setCorrelationId);
        getStringHeader(headers, copyHeaders, TO).ifPresent(message::setTo);
        getStringHeader(headers, copyHeaders, REPLY_TO_SESSION_ID).ifPresent(message::setReplyToSessionId);
        getStringHeader(headers, copyHeaders, PARTITION_KEY).ifPresent(message::setPartitionKey);

        copyHeaders.forEach(key -> {
            message.getApplicationProperties().put(key, headers.get(key).toString());
        });
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
        setValueIfHasText(headers, AzureHeaders.RAW_ID, message.getMessageId());

        // ServiceBusMessageHeaders.
        setValueIfHasText(headers, CORRELATION_ID, message.getCorrelationId());
        setValueIfHasText(headers, MESSAGE_ID, message.getMessageId());
        setValueIfHasText(headers, PARTITION_KEY, message.getPartitionKey());
        setValueIfHasText(headers, TO, message.getTo());
        setValueIfPresent(headers, TIME_TO_LIVE, message.getTimeToLive());
        setValueIfPresent(headers, SCHEDULED_ENQUEUE_TIME, message.getScheduledEnqueueTime());
        setValueIfHasText(headers, REPLY_TO_SESSION_ID, message.getReplyToSessionId());
        setValueIfHasText(headers, SESSION_ID, message.getSessionId());

        message.getApplicationProperties().forEach((key, value) -> {
            if (!headers.containsKey(key)) {
                headers.put(key, value);
            }
        });

        return Collections.unmodifiableMap(headers);
    }

    private Optional<String> getStringHeader(MessageHeaders springMessageHeaders, Set<String> copyHeaders, String key) {
        return Optional.of(key)
                       .filter(copyHeaders::remove)
                       .map(springMessageHeaders::get)
                       .map(Object::toString)
                       .filter(StringUtils::hasText);
    }

    private void setValueIfHasText(Map<String, Object> map, String key, String value) {
        Optional.ofNullable(value).filter(StringUtils::hasText).ifPresent(s -> map.put(key, s));
    }

    private void setValueIfPresent(Map<String, Object> map, String key, Object value) {
        Optional.ofNullable(value).ifPresent(s -> map.put(key, s));
    }
}
