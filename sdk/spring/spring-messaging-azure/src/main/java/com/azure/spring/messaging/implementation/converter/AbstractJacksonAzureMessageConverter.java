// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.converter;

import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.converter.ConversionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class handles common conversion logic between &lt;T&gt; and {@link Message}
 *
 */
public abstract class AbstractJacksonAzureMessageConverter<I, O> implements AzureMessageConverter<I, O> {

    /**
     * Get the object mapper.
     * @return the object mapper.
     */
    protected abstract ObjectMapper getObjectMapper();

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
    protected <U> U fromPayload(Object payload, Class<U> payloadType) {
        try {
            return getObjectMapper().readerFor(payloadType).readValue((byte[]) payload);
        } catch (IOException e) {
            throw new ConversionException("Failed to read JSON: " + Arrays.toString((byte[]) payload), e);
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

    /**
     * Get the payload of the received Azure message.
     * @param azureMessage the received Azure message.
     * @return message payload.
     */
    protected abstract Object getPayload(I azureMessage);

    /**
     * Build an Azure message from the Spring {@link Message}'s payload, when the payload is a String.
     * @param payload the String payload.
     * @return the Azure message.
     */
    protected abstract O fromString(String payload);

    /**
     * Build an Azure message from the Spring {@link Message}'s payload, when the payload is a byte array.
     * @param payload the byte array payload.
     * @return the Azure message.
     */
    protected abstract O fromByte(byte[] payload);

    /**
     * Set the custom headers for messages to be sent to different brokers.
     * @param headers the custom headers.
     * @param azureMessage the message to be sent.
     */
    protected void setCustomHeaders(MessageHeaders headers, O azureMessage) {
    }

    /**
     * Build custom headers from messages received from brokers.
     * @param azureMessage the received Azure message.
     * @return the headers.
     */
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

    /**
     * Convert the message received from Azure service to Spring {@link Message} according to the target payload class.
     * @param azureMessage the message received from Azure service
     * @param headers the headers built from the received Azure message.
     * @param targetPayloadClass the target payload class.
     * @param <U> the target payload class.
     * @return the converted Spring message.
     */
    protected  <U> Message<?> internalToMessage(I azureMessage, Map<String, Object> headers, Class<U> targetPayloadClass) {
        Object payload = getPayload(azureMessage);
        Assert.isTrue(payload != null, "payload must not be null");
        if (targetPayloadClass.isInstance(azureMessage)) {
            return MessageBuilder.withPayload(azureMessage).copyHeaders(headers).build();
        }

        if (targetPayloadClass == String.class) {
            return MessageBuilder.withPayload(new String((byte[]) payload, StandardCharsets.UTF_8)).copyHeaders(headers).build();
        }

        if (targetPayloadClass == byte[].class) {
            return MessageBuilder.withPayload(payload).copyHeaders(headers).build();
        }

        return MessageBuilder.withPayload(fromPayload(payload, targetPayloadClass)).copyHeaders(headers).build();
    }
}
