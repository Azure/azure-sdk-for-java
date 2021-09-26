// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.support.converter;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.converter.AbstractAzureMessageConverter;
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
import java.util.UUID;

import static com.azure.spring.servicebus.support.converter.ServiceBusMessageHeaders.CORRELATION_ID;
import static com.azure.spring.servicebus.support.converter.ServiceBusMessageHeaders.MESSAGE_ID;
import static com.azure.spring.servicebus.support.converter.ServiceBusMessageHeaders.PARTITION_KEY;
import static com.azure.spring.servicebus.support.converter.ServiceBusMessageHeaders.REPLY_TO_SESSION_ID;
import static com.azure.spring.servicebus.support.converter.ServiceBusMessageHeaders.SCHEDULED_ENQUEUE_TIME;
import static com.azure.spring.servicebus.support.converter.ServiceBusMessageHeaders.SESSION_ID;
import static com.azure.spring.servicebus.support.converter.ServiceBusMessageHeaders.TIME_TO_LIVE;
import static com.azure.spring.servicebus.support.converter.ServiceBusMessageHeaders.TO;

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

        Map<String, Object> copySpringMessageHeaders = new HashMap<String, Object>();
        copySpringMessageHeaders.putAll(headers);

        // Spring MessageHeaders
        getAndRemove(copySpringMessageHeaders, MessageHeaders.ID, UUID.class)
            .ifPresent(val -> message.setMessageId(val.toString()));
        getAndRemove(copySpringMessageHeaders, MessageHeaders.CONTENT_TYPE).ifPresent(message::setContentType);
        getAndRemove(copySpringMessageHeaders, MessageHeaders.REPLY_CHANNEL).ifPresent(message::setReplyTo);

        // AzureHeaders.
        getAndRemove(copySpringMessageHeaders, AzureHeaders.RAW_ID).ifPresent(val -> {
            message.setMessageId(val);
            logOverriddenHeaders(AzureHeaders.RAW_ID, MessageHeaders.ID, headers);
        });
        getAndRemove(copySpringMessageHeaders, AzureHeaders.SCHEDULED_ENQUEUE_MESSAGE, Integer.class)
            .map(Duration::ofMillis)
            .map(Instant.now()::plus)
            .map((ins) -> OffsetDateTime.ofInstant(ins, ZoneId.systemDefault()))
            .ifPresent(message::setScheduledEnqueueTime);

        // ServiceBusMessageHeaders, service bus headers have highest priority.
        getAndRemove(copySpringMessageHeaders, MESSAGE_ID).ifPresent(val -> {
            message.setMessageId(val);
            if (!logOverriddenHeaders(MESSAGE_ID, AzureHeaders.RAW_ID, headers)) {
                logOverriddenHeaders(MESSAGE_ID, MessageHeaders.ID, headers);
            }
        });
        getAndRemove(copySpringMessageHeaders, TIME_TO_LIVE, Duration.class).ifPresent(message::setTimeToLive);
        getAndRemove(copySpringMessageHeaders, SCHEDULED_ENQUEUE_TIME, OffsetDateTime.class)
            .ifPresent(val -> {
                message.setScheduledEnqueueTime(val);
                logOverriddenHeaders(SCHEDULED_ENQUEUE_TIME, AzureHeaders.SCHEDULED_ENQUEUE_MESSAGE, headers);
            });
        getAndRemove(copySpringMessageHeaders, SESSION_ID).ifPresent(message::setSessionId);
        getAndRemove(copySpringMessageHeaders, CORRELATION_ID).ifPresent(message::setCorrelationId);
        getAndRemove(copySpringMessageHeaders, TO).ifPresent(message::setTo);
        getAndRemove(copySpringMessageHeaders, REPLY_TO_SESSION_ID).ifPresent(message::setReplyToSessionId);

        if (StringUtils.hasText(message.getSessionId())) {
            if (!ObjectUtils.isEmpty(headers.get(PARTITION_KEY)) && !ObjectUtils.nullSafeEquals(message.getSessionId(),
                headers.get(PARTITION_KEY))) {
                LOGGER.warn("Different session id and partition key are set in the message header, and the partition "
                    + "key header will be overwritten by the session id header.");
            }
            message.setPartitionKey(message.getSessionId());
            if (copySpringMessageHeaders.containsKey(PARTITION_KEY)) {
                copySpringMessageHeaders.remove(PARTITION_KEY);
            }
        } else {
            getAndRemove(copySpringMessageHeaders, PARTITION_KEY).ifPresent(message::setPartitionKey);
        }

        copySpringMessageHeaders.forEach((key, value) -> {
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
     * Get and remove the header value as {@link String} from a copy of {@link MessageHeaders} .
     *
     * @param copySpringMessageHeaders A copy of the original {@link MessageHeaders}.
     * @param key The header key to get value.
     * @return {@link Optional} of the header value.
     */
    private Optional<String> getAndRemove(Map<String, Object> copySpringMessageHeaders, String key) {
        return getAndRemove(copySpringMessageHeaders, key, String.class).filter(StringUtils::hasText);
    }

    /**
     * Get and remove the header value from a copy of {@link MessageHeaders} and convert to the target type.
     *
     * @param copySpringMessageHeaders A copy of the original {@link MessageHeaders}.
     * @param key The header key to get value.
     * @param clazz The class that the header value converts to.
     * @param <T> The generic type of the class.
     * @return {@link Optional} of the header value.
     */
    private <T> Optional<T> getAndRemove(Map<String, Object> copySpringMessageHeaders, String key, Class<T> clazz) {
        return Optional.ofNullable(clazz.cast(copySpringMessageHeaders.remove(key)));
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
