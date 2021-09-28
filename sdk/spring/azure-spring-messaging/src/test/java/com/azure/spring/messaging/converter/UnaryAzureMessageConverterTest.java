// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.converter;

import com.azure.spring.messaging.support.pojo.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.nio.charset.StandardCharsets;

public abstract class UnaryAzureMessageConverterTest<T> {

    protected String headerProperties = "headerProperties";
    protected String payload = "payload";
    private AzureMessageConverter<T, T> converter = null;

    protected abstract AzureMessageConverter<T, T> getConverter();

    protected abstract Class<T> getTargetClass();

    protected abstract void assertMessageHeadersEqual(T azureMessage, Message<?> message);

    @BeforeEach
    public void setUp() {
        converter = getConverter();
    }

    @Test
    public void payloadAsByte() {
        convertAndBack(payload.getBytes(StandardCharsets.UTF_8), byte[].class);
    }

    @Test
    public void payloadAsString() {
        convertAndBack(payload, String.class);
    }

    @Test
    public void payloadAsUserClass() {
        convertAndBack(new User(payload), User.class);
    }

    private <U> void convertAndBack(U payload, Class<U> payloadClass) {
        Message<U> message = MessageBuilder.withPayload(payload).setHeader(headerProperties, headerProperties).build();
        T azureMessage = converter.fromMessage(message, getTargetClass());

        Message<U> convertedMessage = converter.toMessage(azureMessage, payloadClass);

        Assertions.assertNotNull(convertedMessage);
        assertMessagePayloadEquals(convertedMessage.getPayload(), payload);
        assertMessageHeadersEqual(azureMessage, convertedMessage);
    }

    private <U> void assertMessagePayloadEquals(U convertedPayload, U payload) {
        if (convertedPayload.getClass().equals(byte[].class)) {
            Assertions.assertArrayEquals((byte[]) convertedPayload, (byte[]) payload);
        } else {
            Assertions.assertEquals(convertedPayload, payload);
        }
    }
}
