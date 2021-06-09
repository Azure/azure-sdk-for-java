// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.converter;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.test.support.pojo.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static com.azure.spring.integration.core.AzureHeaders.SCHEDULED_ENQUEUE_MESSAGE;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.CORRELATION_ID;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.MESSAGE_ID;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.PARTITION_KEY;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.REPLY_TO_SESSION_ID;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.SCHEDULED_ENQUEUE_TIME;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.SESSION_ID;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.TIME_TO_LIVE;
import static com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.TO;
import static java.time.ZoneId.systemDefault;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class ServiceBusMessageConverterTest {

    private static final String PAYLOAD = "payload";
    private static final String APPLICATION_JSON = "application/json";
    private static final String REPLY_TO = "my-reply-to";
    private static final String AZURE_MESSAGE_RAW_ID = "raw-id";
    private static final String SERVICE_BUS_MESSAGE_ID = "message-id";
    private static final String SERVICE_BUS_SESSION_ID = "session-id";
    private static final String SERVICE_BUS_CORRELATION_ID = "correlation-id";
    private static final String SERVICE_BUS_TO = "to";
    private static final String SERVICE_BUS_REPLY_TO_SESSION_ID = "reply-to-session-id";
    private static final String SERVICE_BUS_PARTITION_KEY = SERVICE_BUS_REPLY_TO_SESSION_ID; // partitionKey should same to sessionId
    private static final String SERVICE_BUS_VIA_PARTITION_KEY = "via-partition-key";
    private static final Duration SERVICE_BUS_TTL = Duration.ofSeconds(1234);

    private final ServiceBusMessageConverter messageConverter = new ServiceBusMessageConverter();

    @Mock
    private ServiceBusReceivedMessage receivedMessage;
    private AutoCloseable closeable;

    @BeforeEach
    public void setup() {
        this.closeable = MockitoAnnotations.openMocks(this);
        when(this.receivedMessage.getBody()).thenReturn(BinaryData.fromString(PAYLOAD));
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Test
    public void shouldRaiseIllegalArgExceptionIfPayloadNull() {
        when(this.receivedMessage.getBody()).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
            () -> this.messageConverter.toMessage(this.receivedMessage, byte[].class));
    }

    @Test
    public void fromPayloadAsByte() {
        final Message<byte[]> message = MessageBuilder.withPayload(PAYLOAD.getBytes(StandardCharsets.UTF_8)).build();
        final ServiceBusMessage serviceBusMessage = this.messageConverter.fromMessage(message, ServiceBusMessage.class);

        assertNotNull(serviceBusMessage);
        assertArrayEquals(PAYLOAD.getBytes(), serviceBusMessage.getBody().toBytes());
    }

    @Test
    public void fromPayloadAsString() {
        final Message<String> message = MessageBuilder.withPayload(PAYLOAD).build();
        final ServiceBusMessage serviceBusMessage = this.messageConverter.fromMessage(message, ServiceBusMessage.class);

        assertNotNull(serviceBusMessage);
        assertEquals(PAYLOAD, serviceBusMessage.getBody().toString());
    }

    @Test
    public void fromPayloadAsUserClass() {
        final User user = new User(PAYLOAD);
        final Message<User> message = MessageBuilder.withPayload(user).build();
        final ServiceBusMessage serviceBusMessage = this.messageConverter.fromMessage(message, ServiceBusMessage.class);

        assertNotNull(serviceBusMessage);
        assertEquals(PAYLOAD, serviceBusMessage.getBody().toObject(User.class).getName());
    }

    @Test
    public void toPayloadAsByte() {
        Message<byte[]> message = this.messageConverter.toMessage(receivedMessage, byte[].class);

        assertNotNull(message);
        assertArrayEquals(PAYLOAD.getBytes(), message.getPayload());
    }

    @Test
    public void toPayloadAsString() {
        Message<String> message = this.messageConverter.toMessage(receivedMessage, String.class);

        assertNotNull(message);
        assertEquals(PAYLOAD, message.getPayload());
    }

    @Test
    public void toPayloadAsUserClass() {
        final User user = new User(PAYLOAD);
        when(this.receivedMessage.getBody()).thenReturn(BinaryData.fromObject(user));

        Message<User> message = this.messageConverter.toMessage(receivedMessage, User.class);

        assertNotNull(message);
        assertEquals(PAYLOAD, message.getPayload().getName());
    }

    @Test
    public void testScheduledEnqueueTimeHeader() {
        Message<String> springMessage = MessageBuilder.withPayload(PAYLOAD)
                                                      .build();
        ServiceBusMessage servicebusMessage = this.messageConverter.fromMessage(springMessage, ServiceBusMessage.class);
        assertNotNull(servicebusMessage);
        assertNull(servicebusMessage.getScheduledEnqueueTime());

        springMessage = MessageBuilder.withPayload(PAYLOAD)
                                      .setHeader(SCHEDULED_ENQUEUE_MESSAGE, 5000)
                                      .build();
        servicebusMessage = this.messageConverter.fromMessage(springMessage, ServiceBusMessage.class);
        assertNotNull(servicebusMessage);
        assertNotNull(servicebusMessage.getScheduledEnqueueTime());
    }

    @Test
    public void testConvertSpringNativeMessageHeaders() {
        Message<String> springMessage = MessageBuilder.withPayload(PAYLOAD)
                                                      .setHeader(MessageHeaders.CONTENT_TYPE, APPLICATION_JSON)
                                                      .setHeader(MessageHeaders.REPLY_CHANNEL, REPLY_TO)
                                                      .build();
        ServiceBusMessage serviceBusMessage = this.messageConverter.fromMessage(springMessage, ServiceBusMessage.class);

        assertNotNull(serviceBusMessage);
        assertEquals(APPLICATION_JSON, serviceBusMessage.getContentType());
        assertEquals(REPLY_TO, serviceBusMessage.getReplyTo());

        when(this.receivedMessage.getBody()).thenReturn(BinaryData.fromString(PAYLOAD));
        when(this.receivedMessage.getContentType()).thenReturn(APPLICATION_JSON);
        when(this.receivedMessage.getReplyTo()).thenReturn(REPLY_TO);

        Message<String> convertedSpringMessage = this.messageConverter.toMessage(this.receivedMessage, String.class);

        assertNotNull(convertedSpringMessage);
        assertEquals(APPLICATION_JSON, convertedSpringMessage.getHeaders().get(MessageHeaders.CONTENT_TYPE));
        assertEquals(REPLY_TO, convertedSpringMessage.getHeaders().get(MessageHeaders.REPLY_CHANNEL));
    }

    @Test
    public void testAzureMessageHeader() {
        Message<String> springMessage = MessageBuilder.withPayload(PAYLOAD)
                                                      .setHeader(AzureHeaders.RAW_ID, AZURE_MESSAGE_RAW_ID)
                                                      .build();

        ServiceBusMessage serviceBusMessage = this.messageConverter.fromMessage(springMessage, ServiceBusMessage.class);

        assertNotNull(serviceBusMessage);
        assertEquals(AZURE_MESSAGE_RAW_ID, serviceBusMessage.getMessageId());

        when(this.receivedMessage.getMessageId()).thenReturn(AZURE_MESSAGE_RAW_ID);

        Message<String> convertedSpringMessage = this.messageConverter.toMessage(this.receivedMessage, String.class);

        assertNotNull(convertedSpringMessage);
        assertEquals(AZURE_MESSAGE_RAW_ID, convertedSpringMessage.getHeaders().get(AzureHeaders.RAW_ID));
    }

    @Test
    public void testServiceBusMessageHeadersSet() {
        Instant scheduledEnqueueInstant = Instant.now().plusSeconds(5);
        final OffsetDateTime scheduledEnqueueOffsetDateTime = OffsetDateTime
            .ofInstant(scheduledEnqueueInstant, systemDefault())
            .toInstant()
            .atOffset(ZoneOffset.UTC);

        Message<String> springMessage = MessageBuilder.withPayload(PAYLOAD)
                                                      .setHeader(AzureHeaders.RAW_ID, AZURE_MESSAGE_RAW_ID)
                                                      .setHeader(MESSAGE_ID, SERVICE_BUS_MESSAGE_ID)
                                                      .setHeader(TIME_TO_LIVE, SERVICE_BUS_TTL)
                                                      .setHeader(SCHEDULED_ENQUEUE_TIME, scheduledEnqueueInstant)
                                                      .setHeader(SESSION_ID, SERVICE_BUS_SESSION_ID)
                                                      .setHeader(CORRELATION_ID, SERVICE_BUS_CORRELATION_ID)
                                                      .setHeader(TO, SERVICE_BUS_TO)
                                                      .setHeader(REPLY_TO_SESSION_ID, SERVICE_BUS_REPLY_TO_SESSION_ID)
                                                      .setHeader(PARTITION_KEY, SERVICE_BUS_SESSION_ID) // when
                                                      // session id set, the partition key equals to session id
                                                      .build();

        ServiceBusMessage serviceBusMessage = this.messageConverter.fromMessage(springMessage, ServiceBusMessage.class);

        assertNotNull(serviceBusMessage);
        assertEquals(AZURE_MESSAGE_RAW_ID, serviceBusMessage.getApplicationProperties().get(AzureHeaders.RAW_ID));
        assertEquals(SERVICE_BUS_MESSAGE_ID, serviceBusMessage.getMessageId());
        assertEquals(SERVICE_BUS_TTL, serviceBusMessage.getTimeToLive());
        assertEquals(scheduledEnqueueOffsetDateTime, serviceBusMessage.getScheduledEnqueueTime());
        assertEquals(SERVICE_BUS_SESSION_ID, serviceBusMessage.getSessionId());
        assertEquals(SERVICE_BUS_CORRELATION_ID, serviceBusMessage.getCorrelationId());
        assertEquals(SERVICE_BUS_TO, serviceBusMessage.getTo());
        assertEquals(SERVICE_BUS_REPLY_TO_SESSION_ID, serviceBusMessage.getReplyToSessionId());
        assertEquals(SERVICE_BUS_SESSION_ID, serviceBusMessage.getPartitionKey());
    }

    @Test
    public void testServiceBusMessageHeadersRead() {
        OffsetDateTime serviceBusScheduledEnqueueTime = OffsetDateTime.ofInstant(Instant.now(), systemDefault());

        when(this.receivedMessage.getMessageId()).thenReturn(SERVICE_BUS_MESSAGE_ID);
        when(this.receivedMessage.getTimeToLive()).thenReturn(SERVICE_BUS_TTL);
        when(this.receivedMessage.getScheduledEnqueueTime()).thenReturn(serviceBusScheduledEnqueueTime);
        when(this.receivedMessage.getSessionId()).thenReturn(SERVICE_BUS_SESSION_ID);
        when(this.receivedMessage.getCorrelationId()).thenReturn(SERVICE_BUS_CORRELATION_ID);
        when(this.receivedMessage.getTo()).thenReturn(SERVICE_BUS_TO);
        when(this.receivedMessage.getReplyToSessionId()).thenReturn(SERVICE_BUS_REPLY_TO_SESSION_ID);
        when(this.receivedMessage.getPartitionKey()).thenReturn(SERVICE_BUS_PARTITION_KEY);

        Message<String> springMessage = this.messageConverter.toMessage(this.receivedMessage, String.class);

        assertNotNull(springMessage);
        assertEquals(SERVICE_BUS_MESSAGE_ID, springMessage.getHeaders().get(AzureHeaders.RAW_ID));
        assertEquals(SERVICE_BUS_MESSAGE_ID, springMessage.getHeaders().get(MESSAGE_ID));
        assertEquals(SERVICE_BUS_TTL, springMessage.getHeaders().get(TIME_TO_LIVE));
        assertEquals(serviceBusScheduledEnqueueTime, springMessage.getHeaders().get(SCHEDULED_ENQUEUE_TIME));
        assertEquals(SERVICE_BUS_SESSION_ID, springMessage.getHeaders().get(SESSION_ID));
        assertEquals(SERVICE_BUS_CORRELATION_ID, springMessage.getHeaders().get(CORRELATION_ID));
        assertEquals(SERVICE_BUS_TO, springMessage.getHeaders().get(TO));
        assertEquals(SERVICE_BUS_REPLY_TO_SESSION_ID, springMessage.getHeaders().get(REPLY_TO_SESSION_ID));
        assertEquals(SERVICE_BUS_PARTITION_KEY, springMessage.getHeaders().get(PARTITION_KEY));
    }
}
