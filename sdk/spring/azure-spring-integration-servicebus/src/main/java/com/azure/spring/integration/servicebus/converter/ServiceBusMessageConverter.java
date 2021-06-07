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
import java.util.Map;
import java.util.Optional;

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

        // Spring MessageHeaders
        getStringHeader(headers, MessageHeaders.ID).ifPresent(message::setMessageId);
        getStringHeader(headers, MessageHeaders.CONTENT_TYPE).ifPresent(message::setContentType);
        getStringHeader(headers, MessageHeaders.REPLY_CHANNEL).ifPresent(message::setReplyTo);

        // AzureHeaders.
        getStringHeader(headers, AzureHeaders.RAW_ID).ifPresent(message::setMessageId);
        Optional.ofNullable(headers.get(AzureHeaders.SCHEDULED_ENQUEUE_MESSAGE, Integer.class))
                .map(Duration::ofMillis)
                .map(Instant.now()::plus)
                .map((ins) -> OffsetDateTime.ofInstant(ins, ZoneId.systemDefault()))
                .ifPresent(message::setScheduledEnqueueTime);

        // ServiceBusMessageHeaders, service bus headers have highest priority.
        getStringHeader(headers, MESSAGE_ID).ifPresent(message::setMessageId);
        Optional.ofNullable((Duration) headers.get(TIME_TO_LIVE)).ifPresent(message::setTimeToLive);
        Optional.ofNullable((Instant) headers.get(SCHEDULED_ENQUEUE_TIME))
                .map((ins) -> OffsetDateTime.ofInstant(ins, ZoneId.systemDefault()))
                .ifPresent(message::setScheduledEnqueueTime);
        getStringHeader(headers, SESSION_ID).ifPresent(message::setSessionId);
        getStringHeader(headers, CORRELATION_ID).ifPresent(message::setCorrelationId);
        getStringHeader(headers, TO).ifPresent(message::setTo);
        getStringHeader(headers, REPLY_TO_SESSION_ID).ifPresent(message::setReplyToSessionId);
        getStringHeader(headers, PARTITION_KEY).ifPresent(message::setPartitionKey);

        headers.forEach((key, value) -> message.getApplicationProperties().put(key, value.toString()));
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

        headers.putAll(message.getApplicationProperties());

        return Collections.unmodifiableMap(headers);
    }

    private Optional<String> getStringHeader(MessageHeaders springMessageHeaders, String key) {
        return Optional.ofNullable(springMessageHeaders.get(key)).map(Object::toString).filter(StringUtils::hasText);
    }

    private void setValueIfHasText(Map<String, Object> map, String key, String value) {
        Optional.ofNullable(value).filter(StringUtils::hasText).ifPresent(s -> map.put(key, s));
    }

    private void setValueIfPresent(Map<String, Object> map, String key, Object value) {
        Optional.ofNullable(value).ifPresent(s -> map.put(key, s));
    }
}
