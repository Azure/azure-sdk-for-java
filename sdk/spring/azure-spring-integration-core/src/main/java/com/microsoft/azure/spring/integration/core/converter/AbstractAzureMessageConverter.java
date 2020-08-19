// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.core.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class handles common conversion logic between &lt;T&gt; and {@link Message}
 *
 * @author Warren Zhu
 */
public abstract class AbstractAzureMessageConverter<T> implements AzureMessageConverter<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractAzureMessageConverter.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected static byte[] toPayload(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new ConversionException("Failed to write JSON: " + object, e);
        }
    }

    private static <U> U fromPayload(byte[] payload, Class<U> payloadType) {
        try {
            return OBJECT_MAPPER.readerFor(payloadType).readValue(payload);
        } catch (IOException e) {
            throw new ConversionException("Failed to read JSON: " + Arrays.toString(payload), e);
        }
    }

    @Override
    public T fromMessage(@NonNull Message<?> message, @NonNull Class<T> targetClass) {
        T azureMessage = internalFromMessage(message, targetClass);

        setCustomHeaders(message.getHeaders(), azureMessage);

        return azureMessage;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> Message<U> toMessage(@NonNull T azureMessage, Map<String, Object> headers,
                                    @NonNull Class<U> targetPayloadClass) {
        Map<String, Object> mergedHeaders = new HashMap<>();
        mergedHeaders.putAll(buildCustomHeaders(azureMessage));
        mergedHeaders.putAll(headers);
        return (Message<U>) internalToMessage(azureMessage, mergedHeaders, targetPayloadClass);
    }

    protected abstract byte[] getPayload(T azureMessage);

    protected abstract T fromString(String payload);

    protected abstract T fromByte(byte[] payload);

    protected void setCustomHeaders(MessageHeaders headers, T azureMessage) {
    }

    protected Map<String, Object> buildCustomHeaders(T azureMessage) {
        return new HashMap<>();
    }

    private T internalFromMessage(Message<?> message, Class<T> targetClass) {
        Object payload = message.getPayload();

        if (targetClass.isInstance(payload)) {
            return targetClass.cast(payload);
        }

        if (payload instanceof String) {
            return fromString((String) payload);
        }

        if (payload instanceof byte[]) {
            return fromByte((byte[]) payload);
        }

        return fromByte(toPayload(payload));
    }

    private <U> Message<?> internalToMessage(T azureMessage, Map<String, Object> headers, Class<U> targetPayloadClass) {
        byte[] payload = getPayload(azureMessage);
        Assert.isTrue(payload != null && payload.length > 0, "payload must not be null");
        if (targetPayloadClass.isInstance(azureMessage)) {
            return MessageBuilder.withPayload(azureMessage).copyHeaders(headers).build();
        }

        if (targetPayloadClass == String.class) {
            return MessageBuilder.withPayload(new String(payload, StandardCharsets.UTF_8)).copyHeaders(headers).build();
        }

        if (targetPayloadClass == byte[].class) {
            return MessageBuilder.withPayload(payload).copyHeaders(headers).build();
        }

        return MessageBuilder.withPayload(fromPayload(payload, targetPayloadClass)).copyHeaders(headers).build();
    }

    /**
     * Convert the json string to class targetType instance.
     * @param value json string
     * @param targetType target class to convert
     * @param <M> Target class type
     * @return Return the corresponding class instance
     */
    protected <M> M readValue(String value, Class<M> targetType) {
        try {
            return OBJECT_MAPPER.readValue(value, targetType);
        } catch (IOException e) {
            throw new ConversionException("Failed to read JSON: " + value, e);
        }
    }

    /**
     * Check value is valid json string.
     * @param value json string to check
     * @return true if it's json string.
     */
    protected boolean isValidJson(Object value) {
        try {
            if (value instanceof String) {
                OBJECT_MAPPER.readTree((String) value);
                return true;
            }
            LOG.warn("Not a valid json string: " + value);
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Convert the object to json string
     * @param value object to be converted
     * @return json string
     */
    protected String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (IOException e) {
            throw new ConversionException("Failed to convert to JSON: " + value.toString(), e);
        }
    }
}
