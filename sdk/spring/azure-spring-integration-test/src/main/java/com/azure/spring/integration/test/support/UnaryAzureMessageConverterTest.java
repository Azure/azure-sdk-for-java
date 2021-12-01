// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.test.support;

import com.azure.spring.integration.core.converter.AzureMessageConverter;
import com.azure.spring.integration.test.support.pojo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @param <T> The type of message.
 */
public abstract class UnaryAzureMessageConverterTest<T> {

    /**
     * The header properties.
     */
    protected String headerProperties = "headerProperties";

    /**
     * The payload.
     */
    protected String payload = "payload";
    private AzureMessageConverter<T, T> converter = null;

    /**
     *
     * @return The converter.
     */
    protected abstract AzureMessageConverter<T, T> getConverter();

    /**
     *
     * @return The target class.
     */
    protected abstract Class<T> getTargetClass();

    /**
     *
     * @param azureMessage The Azure message.
     * @param message The spring message.
     */
    protected abstract void assertMessageHeadersEqual(T azureMessage, Message<?> message);

    /**
     * Set up.
     */
    @BeforeEach
    public void setUp() {
        converter = getConverter();
    }

    /**
     * Test payload as byte.
     */
    @Test
    public void payloadAsByte() {
        convertAndBack(payload.getBytes(StandardCharsets.UTF_8), byte[].class);
    }

    /**
     * Test payload as string.
     */
    @Test
    public void payloadAsString() {
        convertAndBack(payload, String.class);
    }

    /**
     * Test payload as user class.
     */
    @Test
    public void payloadAsUserClass() {
        convertAndBack(new User(payload), User.class);
    }

    private <U> void convertAndBack(U payload, Class<U> payloadClass) {
        Message<U> message = MessageBuilder.withPayload(payload).setHeader(headerProperties, headerProperties).build();
        T azureMessage = converter.fromMessage(message, getTargetClass());

        Message<U> convertedMessage = converter.toMessage(azureMessage, payloadClass);

        assertNotNull(convertedMessage);
        assertMessagePayloadEquals(convertedMessage.getPayload(), payload);
        assertMessageHeadersEqual(azureMessage, convertedMessage);
    }

    private <U> void assertMessagePayloadEquals(U convertedPayload, U payload) {
        if (convertedPayload.getClass().equals(byte[].class)) {
            assertArrayEquals((byte[]) convertedPayload, (byte[]) payload);
        } else {
            assertEquals(convertedPayload, payload);
        }
    }
}
