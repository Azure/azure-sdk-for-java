// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.servicebus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.MessageBody;
import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.converter.AzureMessageConverter;
import com.microsoft.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.microsoft.azure.spring.integration.test.support.AzureMessageConverterTest;
import org.junit.Test;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageHeaders;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.*;

public class ServiceBusMessageConverterTest extends AzureMessageConverterTest<IMessage> {
    @Override
    protected IMessage getInstance() {
        return new Message(this.payload.getBytes());
    }

    @Override
    public AzureMessageConverter<IMessage> getConverter() {
        return new ServiceBusMessageConverter();
    }

    @Override
    protected Class<IMessage> getTargetClass() {
        return IMessage.class;
    }

    protected void assertMessageHeadersEqual(IMessage serviceBusMessage,
                                             org.springframework.messaging.Message<?> message) {
        assertEquals(serviceBusMessage.getMessageId(), message.getHeaders().get(AzureHeaders.RAW_ID));
        assertEquals(serviceBusMessage.getContentType(),
            message.getHeaders().get(MessageHeaders.CONTENT_TYPE, String.class));
        assertEquals(serviceBusMessage.getReplyTo(),
            message.getHeaders().get(MessageHeaders.REPLY_CHANNEL, String.class));
        assertNotNull(serviceBusMessage.getProperties().get(headerProperties));
        assertNotNull(message.getHeaders().get(headerProperties, String.class));
        assertEquals(serviceBusMessage.getProperties().get(headerProperties),
            message.getHeaders().get(headerProperties, String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRaiseIllegalIfPayloadNull() {
        IMessage message = new Message((MessageBody) null);
        getConverter().toMessage(message, byte[].class);
    }

    @Test
    public void shouldConvertIMessageBinaryIntoAMessage() {
        IMessage message = new Message(this.payload.getBytes());

        org.springframework.messaging.Message<byte[]> convertedPayload =
            getConverter().toMessage(message, byte[].class);

        assertNotNull(convertedPayload);
        assertArrayEquals(convertedPayload.getPayload(), payload.getBytes());
    }

    @Test
    public void shouldConvertIMessageValueIntoAMessage() {
        IMessage message = new Message(MessageBody.fromValueData(this.payload));

        org.springframework.messaging.Message<byte[]> convertedPayload =
            getConverter().toMessage(message, byte[].class);

        assertNotNull(convertedPayload);
        assertArrayEquals(convertedPayload.getPayload(), payload.getBytes());
    }

    @Test
    public void shouldConvertIMessageSequenceIntoAMessage() throws JsonProcessingException {
        List<Object> internalSequence = singletonList(payload);
        List<List<Object>> sequences = singletonList(internalSequence);

        IMessage message = new Message(MessageBody.fromSequenceData(sequences));

        org.springframework.messaging.Message<byte[]> convertedPayload =
            getConverter().toMessage(message, byte[].class);

        assertNotNull(convertedPayload);
        assertArrayEquals(
            convertedPayload.getPayload(),
            new ObjectMapper().writeValueAsBytes(internalSequence));
    }

    @Test
    public void shouldConvertSpringMessageHeaderIntoIMessage() {
        org.springframework.messaging.Message<String> springMessage =
            MessageBuilder.withPayload(payload).setHeader("x-delay", 5000).build();
        IMessage servicebusMessage = getConverter().fromMessage(springMessage, IMessage.class);
        assertNotNull(servicebusMessage);
        assertNotNull(servicebusMessage.getScheduledEnqueueTimeUtc());

    }
}
