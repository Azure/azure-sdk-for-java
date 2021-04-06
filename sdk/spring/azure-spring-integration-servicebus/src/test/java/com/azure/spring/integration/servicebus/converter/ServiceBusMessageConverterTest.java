// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.converter;

import com.azure.core.http.ContentType;
import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.converter.AzureMessageConverter;
import com.azure.spring.integration.test.support.AzureMessageConverterTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.MessageBody;
import org.junit.Test;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageHeaders;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.CORRELATION_ID;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.MESSAGE_ID;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.PARTITION_KEY;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.REPLY_TO_SESSION_ID;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.SCHEDULED_ENQUEUE_TIME;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.SESSION_ID;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.TIME_TO_LIVE;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.TO;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

    String springMessageContent = ContentType.APPLICATION_JSON;
    String springReplyChannel = "SpringReplyChannel";
    @Test
    public void springMessageHeaderTest() {
        org.springframework.messaging.Message<String> springMessage;
        IMessage serviceBusMessage;
        org.springframework.messaging.Message<String> convertedSpringMessage;

        springMessage = MessageBuilder.withPayload(payload)
                                      //.setHeader(MessageHeaders.ID, springMessageId) // MessageHeaders.ID header is
                                      // read-only
                                      .setHeader(MessageHeaders.CONTENT_TYPE, springMessageContent)
                                      .setHeader(MessageHeaders.REPLY_CHANNEL, springReplyChannel)
                                      .build();
        serviceBusMessage = getConverter().fromMessage(springMessage, IMessage.class);
        assertEquals(springMessageContent, serviceBusMessage.getContentType());
        assertEquals(springReplyChannel, serviceBusMessage.getReplyTo());

        convertedSpringMessage = getConverter().toMessage(serviceBusMessage, String.class);
        assertEquals(springMessageContent, convertedSpringMessage.getHeaders().get(MessageHeaders.CONTENT_TYPE));
        assertEquals(springReplyChannel, convertedSpringMessage.getHeaders().get(MessageHeaders.REPLY_CHANNEL));
    }

    String azureMessageRawId = UUID.randomUUID().toString();
    @Test
    public void azureMessageHeaderTest() {
        org.springframework.messaging.Message<String> springMessage;
        IMessage serviceBusMessage;
        org.springframework.messaging.Message<String> convertedSpringMessage;

        springMessage = MessageBuilder.withPayload(payload)
                                      .setHeader(AzureHeaders.RAW_ID, azureMessageRawId)
                                      .build();
        serviceBusMessage = getConverter().fromMessage(springMessage, IMessage.class);
        assertEquals(azureMessageRawId, serviceBusMessage.getMessageId());

        convertedSpringMessage = getConverter().toMessage(serviceBusMessage, String.class);
        assertEquals(azureMessageRawId, convertedSpringMessage.getHeaders().get(AzureHeaders.RAW_ID));
    }

    String serviceBusMessageId = UUID.randomUUID().toString();
    Duration serviceBusTimeToLive = Duration.ofSeconds(1234);
    Instant serviceBusScheduledEnqueueTimeUtc = Instant.now();
    String serviceBusSessionId = UUID.randomUUID().toString();
    String serviceBusCorrelationId = UUID.randomUUID().toString();
    String serviceBusTo = UUID.randomUUID().toString();
    String serviceBusLabel = UUID.randomUUID().toString();
    String serviceBusReplyToSessionId = UUID.randomUUID().toString();
    String serviceBusPartitionKey = serviceBusSessionId; // partitionKey should same to sessionId
    String serviceBusViaPartitionKey = UUID.randomUUID().toString();
    @Test
    public void serviceBusMessageHeaderTest() {
        org.springframework.messaging.Message<String> springMessage;
        IMessage serviceBusMessage;
        org.springframework.messaging.Message<String> convertedSpringMessage;

        springMessage = MessageBuilder.withPayload(payload)
                                      .setHeader(MessageHeaders.CONTENT_TYPE, springMessageContent)
                                      .setHeader(MessageHeaders.REPLY_CHANNEL, springReplyChannel)
                                      .setHeader(AzureHeaders.RAW_ID, azureMessageRawId)
                                      .setHeader(MESSAGE_ID, serviceBusMessageId)
                                      .setHeader(TIME_TO_LIVE, serviceBusTimeToLive)
                                      .setHeader(SCHEDULED_ENQUEUE_TIME, serviceBusScheduledEnqueueTimeUtc)
                                      .setHeader(SESSION_ID, serviceBusSessionId)
                                      .setHeader(CORRELATION_ID, serviceBusCorrelationId)
                                      .setHeader(TO, serviceBusTo)
                                      .setHeader(REPLY_TO_SESSION_ID, serviceBusReplyToSessionId)
                                      .setHeader(PARTITION_KEY, serviceBusPartitionKey)
                                      .build();
        serviceBusMessage = getConverter().fromMessage(springMessage, IMessage.class);
        assertEquals(springMessageContent, serviceBusMessage.getContentType());
        assertEquals(springReplyChannel, serviceBusMessage.getReplyTo());
        assertEquals(azureMessageRawId, serviceBusMessage.getProperties().get(AzureHeaders.RAW_ID));
        assertEquals(serviceBusMessageId, serviceBusMessage.getMessageId());
        assertEquals(serviceBusTimeToLive, serviceBusMessage.getTimeToLive());
        assertEquals(serviceBusScheduledEnqueueTimeUtc, serviceBusMessage.getScheduledEnqueueTimeUtc());
        assertEquals(serviceBusSessionId, serviceBusMessage.getSessionId());
        assertEquals(serviceBusCorrelationId, serviceBusMessage.getCorrelationId());
        assertEquals(serviceBusTo, serviceBusMessage.getTo());
        assertEquals(serviceBusLabel, serviceBusMessage.getLabel());
        assertEquals(serviceBusReplyToSessionId, serviceBusMessage.getReplyToSessionId());
        assertEquals(serviceBusPartitionKey, serviceBusMessage.getPartitionKey());
        assertEquals(serviceBusViaPartitionKey, serviceBusMessage.getViaPartitionKey());

        convertedSpringMessage = getConverter().toMessage(serviceBusMessage, String.class);
        assertEquals(springMessageContent, convertedSpringMessage.getHeaders().get(MessageHeaders.CONTENT_TYPE));
        assertEquals(springReplyChannel, convertedSpringMessage.getHeaders().get(MessageHeaders.REPLY_CHANNEL));
        assertEquals(azureMessageRawId, convertedSpringMessage.getHeaders().get(AzureHeaders.RAW_ID));
        assertEquals(serviceBusMessageId, convertedSpringMessage.getHeaders().get(MESSAGE_ID));
        assertEquals(serviceBusTimeToLive.toString(), convertedSpringMessage.getHeaders().get(TIME_TO_LIVE));
        assertEquals(serviceBusScheduledEnqueueTimeUtc.toString(),
            convertedSpringMessage.getHeaders().get(SCHEDULED_ENQUEUE_TIME));
        assertEquals(serviceBusSessionId, convertedSpringMessage.getHeaders().get(SESSION_ID));
        assertEquals(serviceBusCorrelationId, convertedSpringMessage.getHeaders().get(CORRELATION_ID));
        assertEquals(serviceBusTo, convertedSpringMessage.getHeaders().get(TO));
        assertEquals(serviceBusReplyToSessionId, convertedSpringMessage.getHeaders().get(REPLY_TO_SESSION_ID));
        assertEquals(serviceBusPartitionKey, convertedSpringMessage.getHeaders().get(PARTITION_KEY));
    }
}
