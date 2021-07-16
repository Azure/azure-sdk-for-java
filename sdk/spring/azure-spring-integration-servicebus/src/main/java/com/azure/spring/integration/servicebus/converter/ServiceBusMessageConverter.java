// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.converter;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.converter.AbstractAzureMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusMessageConverter.class);
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

        Map<String, Object> headersForApplicationProperties = new HashMap<String, Object>();
        headersForApplicationProperties.putAll(headers);

        // Spring MessageHeaders
        getHeaderAsStringAndRemove(headers, headersForApplicationProperties, MessageHeaders.ID).ifPresent(message::setMessageId);
        getHeaderAsStringAndRemove(headers, headersForApplicationProperties, MessageHeaders.CONTENT_TYPE).ifPresent(message::setContentType);
        getHeaderAsStringAndRemove(headers, headersForApplicationProperties, MessageHeaders.REPLY_CHANNEL).ifPresent(message::setReplyTo);

        // AzureHeaders.
        getHeaderAsStringAndRemove(headers, headersForApplicationProperties, AzureHeaders.RAW_ID).ifPresent(val -> {
            message.setMessageId(val);
            logOverriddenHeaders(AzureHeaders.RAW_ID, MessageHeaders.ID, headers);
        });
        Optional.ofNullable(headers.get(AzureHeaders.SCHEDULED_ENQUEUE_MESSAGE, Integer.class))
                .map(Duration::ofMillis)
                .map(Instant.now()::plus)
                .map((ins) -> OffsetDateTime.ofInstant(ins, ZoneId.systemDefault()))
                .ifPresent(val -> {
                    message.setScheduledEnqueueTime(val);
                    headersForApplicationProperties.remove(AzureHeaders.SCHEDULED_ENQUEUE_MESSAGE);
                });

        // ServiceBusMessageHeaders, service bus headers have highest priority.
        getHeaderAsStringAndRemove(headers, headersForApplicationProperties, MESSAGE_ID).ifPresent(val -> {
            message.setMessageId(val);
            if (!logOverriddenHeaders(MESSAGE_ID, AzureHeaders.RAW_ID, headers)) {
                logOverriddenHeaders(MESSAGE_ID, MessageHeaders.ID, headers);
            }
        });
        Optional.ofNullable(headers.get(TIME_TO_LIVE, Duration.class)).ifPresent(val -> {
            message.setTimeToLive(val);
            headersForApplicationProperties.remove(TIME_TO_LIVE);
        });

        Optional.ofNullable((Instant) headers.get(SCHEDULED_ENQUEUE_TIME))
                .map((ins) -> OffsetDateTime.ofInstant(ins, ZoneId.systemDefault()))
                .ifPresent(val -> {
                    message.setScheduledEnqueueTime(val);
                    logOverriddenHeaders(SCHEDULED_ENQUEUE_TIME, AzureHeaders.SCHEDULED_ENQUEUE_MESSAGE, headers);
                    headersForApplicationProperties.remove(SCHEDULED_ENQUEUE_TIME);
                });
        getHeaderAsStringAndRemove(headers, headersForApplicationProperties, SESSION_ID).ifPresent(message::setSessionId);
        getHeaderAsStringAndRemove(headers, headersForApplicationProperties, CORRELATION_ID).ifPresent(message::setCorrelationId);
        getHeaderAsStringAndRemove(headers, headersForApplicationProperties, TO).ifPresent(message::setTo);
        getHeaderAsStringAndRemove(headers, headersForApplicationProperties, REPLY_TO_SESSION_ID).ifPresent(message::setReplyToSessionId);
        getHeaderAsStringAndRemove(headers, headersForApplicationProperties, PARTITION_KEY).ifPresent(message::setPartitionKey);

        headersForApplicationProperties.forEach((key, value) -> {
            message.getApplicationProperties().put(key, value.toString());
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
            headers.putIfAbsent(key, value);
        });

        return Collections.unmodifiableMap(headers);
    }

    /**
     * Get the value of a header key from {@link MessageHeaders} as {@link String}, and if the value exists, remove the
     * header from a copy {@link Set} of original {@link MessageHeaders}.
     *
     * @param springMessageHeaders Original {@link MessageHeaders} to get header values.
     * @param copyHeaders A copy of keys for the original {@link MessageHeaders}.
     * @param key The header key to get value.
     * @return {@link Optional} of the header value.
     */
    private Optional<String> getHeaderAsStringAndRemove(MessageHeaders springMessageHeaders, Map<String, Object> copyHeaders,
                                                        String key) {
        copyHeaders.remove(key);
        return Optional.ofNullable(springMessageHeaders.get(key)).map(Object::toString).filter(StringUtils::hasText);
    }

    private Boolean logOverriddenHeaders(String currentHeader, String overriddenHeader,
                                      MessageHeaders springMessageHeaders) {
        Boolean isExisted = false;
        if (springMessageHeaders.containsKey(overriddenHeader)) {
            isExisted = true;
            LOGGER.warn("{} header detected, usage of {} header will be overridden", currentHeader,
                overriddenHeader);
        }
        return isExisted;
    }

    private void setValueIfHasText(Map<String, Object> map, String key, String value) {
        Optional.ofNullable(value).filter(StringUtils::hasText).ifPresent(s -> map.put(key, s));
    }

    private void setValueIfPresent(Map<String, Object> map, String key, Object value) {
        Optional.ofNullable(value).ifPresent(s -> map.put(key, s));
    }
}
