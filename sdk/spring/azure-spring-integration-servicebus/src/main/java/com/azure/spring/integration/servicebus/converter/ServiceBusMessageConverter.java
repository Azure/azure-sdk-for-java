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
        setValueIfPresent(headers, SCHEDULED_ENQUEUE_TIME, message.getScheduledEnqueueTimeUtc());
        setValueIfHasText(headers, REPLY_TO_SESSION_ID, message.getReplyToSessionId());
        setValueIfHasText(headers, SESSION_ID, message.getSessionId());

        headers.putAll(message.getProperties());

        return Collections.unmodifiableMap(headers);
    }

    private void setValueIfHasText(Map<String, Object> map, String key, String value) {
        Optional.ofNullable(value)
                .filter(StringUtils::hasText)
                .ifPresent(s -> map.put(key, s));
    }

    private void setValueIfPresent(Map<String, Object> map, String key, Object value) {
        Optional.ofNullable(value)
                .ifPresent(s -> map.put(key, s));
    }
}
