// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.test.support;

import com.azure.spring.integration.core.converter.AzureMessageConverter;
import com.azure.spring.integration.test.support.pojo.User;
import org.junit.Before;
import org.junit.Test;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

public abstract class AzureMessageConverterTest<I, O> {
    protected String payload = "payload";
    protected String headerProperties = "headerProperties";
    private AzureMessageConverter<I, O> converter = null;
 //   private Class<T> targetClass; TODO

    public static void main(String[] args) {

    }

    @Before
    public void setUp() {
      //  converter = getConverter();
      //  targetClass = getTargetClass(); TODO
    }

    @Test
    public void payloadAsString() {
        convertAndBack(payload, String.class);
    }

    @Test
    public void payloadAsByte() {
        convertAndBack(payload.getBytes(StandardCharsets.UTF_8), byte[].class);
    }

    @Test
    public void payloadAsTargetType() {
      //  convertAndBack(getInstance(), targetClass); //TODO
    }

    @Test
    public void payloadAsUserClass() {
        convertAndBack(new User(payload), User.class);
    }

    private <U> void convertAndBack(U payload, Class<U> payloadClass) {
        //TODO
       /* Message<U> message = MessageBuilder.withPayload(payload).setHeader(headerProperties, headerProperties).build();
        T azureMessage = converter.fromMessage(message, targetClass);
        Message<U> convertedMessage = converter.toMessage(azureMessage, payloadClass);
        assertNotNull(convertedMessage);
        assertMessagePayloadEquals(convertedMessage.getPayload(), payload);
        assertMessageHeadersEqual(azureMessage, convertedMessage);*/
    }

    private <U> void assertMessagePayloadEquals(U convertedPayload, U payload) {
        if (convertedPayload.getClass().equals(byte[].class)) {
            assertTrue(Arrays.equals((byte[]) convertedPayload, (byte[]) payload));
        } else {
            assertEquals(convertedPayload, payload);
        }
    }

    protected void assertMessageHeadersEqual(O azureMessage, Message<?> message) {
    }

    protected abstract O getInstance();

    protected abstract AzureMessageConverter<I, O> getConverter();

    protected abstract Class<O> getTargetClass();
}
