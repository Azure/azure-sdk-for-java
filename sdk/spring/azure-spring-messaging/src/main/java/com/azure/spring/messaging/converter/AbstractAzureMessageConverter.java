// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.support.MessageBuilder;
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
public abstract class AbstractAzureMessageConverter<I, O> implements AzureMessageConverter<I, O> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAzureMessageConverter.class);

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Convert payload object to byte array.
     * @param object The payload object.
     * @return The byte array.
     * @throws ConversionException When fail to convert the object to bytes.
     */
    protected byte[] toPayload(Object object) {
        try {
            return getObjectMapper().writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new ConversionException("Failed to write JSON: " + object, e);
        }
    }

    /**
     * Convert payload from byte array to object.
     *
     * @param payload The payload byte array.
     * @param payloadType The target type of payload.
     * @param <U> The type class.
     * @return The converted object.
     * @throws ConversionException When fail to convert to object from byte array.
     */
    private <U> U fromPayload(byte[] payload, Class<U> payloadType) {
        try {
            return getObjectMapper().readerFor(payloadType).readValue(payload);
        } catch (IOException e) {
            throw new ConversionException("Failed to read JSON: " + Arrays.toString(payload), e);
        }
    }

    @Override
    public O fromMessage(@NonNull Message<?> message, @NonNull Class<O> targetClass) {
        O azureMessage = internalFromMessage(message, targetClass);

        setCustomHeaders(message.getHeaders(), azureMessage);

        return azureMessage;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> Message<U> toMessage(@NonNull I azureMessage,
                                    Map<String, Object> headers,
                                    @NonNull Class<U> targetPayloadClass) {
        Map<String, Object> mergedHeaders = new HashMap<>();
        mergedHeaders.putAll(buildCustomHeaders(azureMessage));
        mergedHeaders.putAll(headers);
        return (Message<U>) internalToMessage(azureMessage, mergedHeaders, targetPayloadClass);
    }

    protected abstract byte[] getPayload(I azureMessage);

    protected abstract O fromString(String payload);

    protected abstract O fromByte(byte[] payload);

    protected void setCustomHeaders(MessageHeaders headers, O azureMessage) {
    }

    protected Map<String, Object> buildCustomHeaders(I azureMessage) {
        return emptyHeaders();
    }


    private  Map<String, Object> emptyHeaders() {
        return new HashMap<>();
    }

    private O internalFromMessage(Message<?> message, Class<O> targetClass) {
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

    private <U> Message<?> internalToMessage(I azureMessage, Map<String, Object> headers, Class<U> targetPayloadClass) {
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
     * @throws ConversionException When fail to convert.
     */
    protected <M> M readValue(String value, Class<M> targetType) {
        try {
            return getObjectMapper().readValue(value, targetType);
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
                getObjectMapper().readTree((String) value);
                return true;
            }
            LOGGER.warn("Not a valid json string: " + value);
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Convert the object to json string
     * @param value object to be converted
     * @return json string
     * @throws ConversionException When fail to convert.
     */
    protected String toJson(Object value) {
        try {
            return getObjectMapper().writeValueAsString(value);
        } catch (IOException e) {
            throw new ConversionException("Failed to convert to JSON: " + value.toString(), e);
        }
    }
}
