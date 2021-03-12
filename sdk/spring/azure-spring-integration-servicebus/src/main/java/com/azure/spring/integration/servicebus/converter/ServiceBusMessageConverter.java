// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.converter;

import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.converter.AbstractAzureMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.MessageBody;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
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
 * A converter to turn a {@link org.springframework.messaging.Message} to {@link IMessage} and vice versa.
 *
 * @author Warren Zhu
 */
public class ServiceBusMessageConverter extends AbstractAzureMessageConverter<IMessage> {

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
    protected byte[] getPayload(IMessage azureMessage) {
        MessageBody messageBody = azureMessage.getMessageBody();
        if (messageBody == null) {
            return new byte[0];
        }

        switch (messageBody.getBodyType()) {
            case BINARY:
                return messageBody.getBinaryData().stream().findFirst().orElse(null);
            case VALUE:
                return String.valueOf(messageBody.getValueData()).getBytes(StandardCharsets.UTF_8);
            case SEQUENCE:
                return toPayload(messageBody.getSequenceData().stream().findFirst().orElse(null));
            default:
                return new byte[0];
        }
    }

    @Override
    protected IMessage fromString(String payload) {
        return new Message(payload);
    }

    @Override
    protected IMessage fromByte(byte[] payload) {
        return new Message(payload);
    }

    @Override
    protected void setCustomHeaders(MessageHeaders headers, IMessage message) {

        // Spring MessageHeaders
        getStringHeader(headers, MessageHeaders.ID).ifPresent(message::setMessageId);
        getStringHeader(headers, MessageHeaders.CONTENT_TYPE).ifPresent(message::setContentType);
        getStringHeader(headers, MessageHeaders.REPLY_CHANNEL).ifPresent(message::setReplyTo);

        // AzureHeaders.
        getStringHeader(headers, AzureHeaders.RAW_ID).ifPresent(message::setMessageId);
        Optional.ofNullable(headers.get(AzureHeaders.SCHEDULED_ENQUEUE_MESSAGE, Integer.class))
                .map(Duration::ofMillis)
                .map(Instant.now()::plus)
                .ifPresent(message::setScheduledEnqueueTimeUtc);

        // ServiceBusMessageHeaders, service bus headers have highest priority.
        getStringHeader(headers, MESSAGE_ID).ifPresent(message::setMessageId);
        Optional.ofNullable((Duration) headers.get(TIME_TO_LIVE))
                .ifPresent(message::setTimeToLive);
        Optional.ofNullable((Instant) headers.get(SCHEDULED_ENQUEUE_TIME))
                .ifPresent(message::setScheduledEnqueueTimeUtc);
        getStringHeader(headers, SESSION_ID).ifPresent(message::setSessionId);
        getStringHeader(headers, CORRELATION_ID).ifPresent(message::setCorrelationId);
        getStringHeader(headers, TO).ifPresent(message::setTo);
        getStringHeader(headers, REPLY_TO_SESSION_ID).ifPresent(message::setReplyToSessionId);
        getStringHeader(headers, PARTITION_KEY).ifPresent(message::setPartitionKey);

        headers.forEach((key, value) -> message.getProperties().put(key, value.toString()));
    }

    private Optional<String> getStringHeader(MessageHeaders springMessageHeaders, String key) {
        return Optional.ofNullable(springMessageHeaders.get(key))
                       .map(Object::toString)
                       .filter(StringUtils::hasText);
    }

    @Override
    protected Map<String, Object> buildCustomHeaders(IMessage message) {
        Map<String, Object> headers = new HashMap<>();

        // Spring MessageHeaders
        Optional.ofNullable(message.getMessageId())
                .filter(StringUtils::hasText)
                .ifPresent(s -> headers.put(MessageHeaders.ID, s));
        Optional.ofNullable(message.getContentType())
                .filter(StringUtils::hasText)
                .ifPresent(s -> headers.put(MessageHeaders.CONTENT_TYPE, s));
        Optional.ofNullable(message.getReplyTo())
                .filter(StringUtils::hasText)
                .ifPresent(s -> headers.put(MessageHeaders.REPLY_CHANNEL, s));

        // AzureHeaders.
        // Does not have SCHEDULED_ENQUEUE_MESSAGE, because it's meaningless in receiver side.
        Optional.ofNullable(message.getMessageId())
                .filter(StringUtils::hasText)
                .ifPresent(s -> headers.put(AzureHeaders.RAW_ID, s));

        // ServiceBusMessageHeaders.
        Optional.ofNullable(message.getMessageId())
                .filter(StringUtils::hasText)
                .ifPresent(s -> headers.put(MESSAGE_ID, s));
        Optional.ofNullable(message.getTimeToLive())
                .ifPresent(s -> headers.put(TIME_TO_LIVE, s));
        Optional.ofNullable(message.getScheduledEnqueueTimeUtc())
                .ifPresent(s -> headers.put(SCHEDULED_ENQUEUE_TIME, s));
        Optional.ofNullable(message.getSessionId())
                .filter(StringUtils::hasText)
                .ifPresent(s -> headers.put(SESSION_ID, s));
        Optional.ofNullable(message.getCorrelationId())
                .filter(StringUtils::hasText)
                .ifPresent(s -> headers.put(CORRELATION_ID, s));
        Optional.ofNullable(message.getTo())
                .filter(StringUtils::hasText)
                .ifPresent(s -> headers.put(TO, s));
        Optional.ofNullable(message.getReplyToSessionId())
                .filter(StringUtils::hasText)
                .ifPresent(s -> headers.put(REPLY_TO_SESSION_ID, s));
        Optional.ofNullable(message.getPartitionKey())
                .filter(StringUtils::hasText)
                .ifPresent(s -> headers.put(PARTITION_KEY, s));

        headers.putAll(message.getProperties());

        return Collections.unmodifiableMap(headers);
    }
}
