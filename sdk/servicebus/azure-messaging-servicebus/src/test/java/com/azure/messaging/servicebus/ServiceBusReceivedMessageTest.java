// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import static com.azure.core.amqp.AmqpMessageConstant.DEAD_LETTER_DESCRIPTION_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.DEAD_LETTER_REASON_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.DEAD_LETTER_SOURCE_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_SEQUENCE_NUMBER_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.LOCKED_UNTIL_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.azure.core.amqp.AmqpMessageConstant;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class ServiceBusReceivedMessageTest {

    // Create a giant payload with 10000 characters that are "a".
    private static final String PAYLOAD = new String(new char[10000]).replace("\0", "a");
    private static final byte[] PAYLOAD_BYTES = PAYLOAD.getBytes(UTF_8);
    private static final String PAYLOAD_STRING = new String(PAYLOAD_BYTES);

    @Test
    public void byteArrayNotNull() {
        assertThrows(NullPointerException.class, () -> new ServiceBusReceivedMessage((byte[]) null));
    }

    @Test
    public void messagePropertiesShouldNotBeNull() {
        // Act
        final ServiceBusReceivedMessage receivedMessage = new ServiceBusReceivedMessage(PAYLOAD_BYTES);

        // Assert
        Assertions.assertNotNull(receivedMessage.getBody());
        Assertions.assertNotNull(receivedMessage.getApplicationProperties());
    }


    /**
     * Verify that we can create an Message with an empty byte array.
     */
    @Test
    public void canCreateWithEmptyArray() {
        // Arrange
        byte[] byteArray = new byte[0];

        // Act
        final ServiceBusReceivedMessage serviceBusMessageData = new ServiceBusReceivedMessage(byteArray);

        // Assert
        final byte[] actual = serviceBusMessageData.getBody();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(0, actual.length);
    }

    /**
     * Verify that we can create an Message with the correct body contents.
     */
    @Test
    public void canCreateWithBytePayload() {
        // Act
        final ServiceBusReceivedMessage serviceBusMessageData = new ServiceBusReceivedMessage(PAYLOAD_BYTES);

        // Assert
        Assertions.assertNotNull(serviceBusMessageData.getBody());
        Assertions.assertEquals(PAYLOAD, new String(serviceBusMessageData.getBody(), UTF_8));
    }

    @Test
    public void toServiceBusMessageTest() {
        //Arrange
        Message amqpMessage = mock(Message.class);
        Data data = new Data(new Binary(PAYLOAD_BYTES));
        when(amqpMessage.getBody()).thenReturn(data);
        //
        final ServiceBusReceivedMessage originalMessage = new ServiceBusReceivedMessage(PAYLOAD_BYTES);
        originalMessage.setMessageId("mid");
        originalMessage.setContentType("type");
        originalMessage.setCorrelationId("cid");
        originalMessage.setReplyTo("rto");
        originalMessage.setViaPartitionKey("something");
        originalMessage.setTimeToLive(Duration.ofSeconds(10));
        originalMessage.setReplyToSessionId("rsessionid");
        originalMessage.setSubject("subject");
        originalMessage.setTo("to");
        final Map<String, Object> originalMessageAnnotations =  originalMessage.getAmqpAnnotatedMessage().getMessageAnnotations();
        originalMessageAnnotations.put(DEAD_LETTER_SOURCE_KEY_ANNOTATION_NAME.getValue(), "message annotations");
        originalMessageAnnotations.put(ENQUEUED_SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(), Long.valueOf(3));
        originalMessageAnnotations.put(LOCKED_UNTIL_KEY_ANNOTATION_NAME.getValue(), new Date(Instant.now().toEpochMilli()));
        originalMessageAnnotations.put(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue(), new Date(Instant.now().toEpochMilli()));

        originalMessageAnnotations.put(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(), Long.valueOf(3));

        final Map<String, Object> originalApplicationProperties =  originalMessage.getAmqpAnnotatedMessage().getApplicationProperties();
        originalApplicationProperties.put(DEAD_LETTER_DESCRIPTION_ANNOTATION_NAME.getValue(), "description");
        originalApplicationProperties.put(DEAD_LETTER_REASON_ANNOTATION_NAME.getValue(), "description");

        originalMessage.getAmqpAnnotatedMessage().getHeader().setDeliveryCount(Long.valueOf(5));

        // Act
        final ServiceBusMessage actual = new ServiceBusMessage(originalMessage);

        // Assert
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertEquals(PAYLOAD, new String(actual.getBody(), UTF_8));
        Assertions.assertEquals(originalMessage.getMessageId(), actual.getMessageId());
        Assertions.assertEquals(originalMessage.getContentType(), actual.getContentType());
        Assertions.assertEquals(originalMessage.getCorrelationId(), actual.getCorrelationId());
        Assertions.assertEquals(originalMessage.getReplyTo(), actual.getReplyTo());
        Assertions.assertEquals(originalMessage.getViaPartitionKey(), actual.getViaPartitionKey());
        Assertions.assertEquals(originalMessage.getTimeToLive().toMillis(), actual.getTimeToLive().toMillis());
        Assertions.assertEquals(originalMessage.getLabel(), actual.getSubject());
        Assertions.assertEquals(originalMessage.getReplyToSessionId(), actual.getReplyToSessionId());
        Assertions.assertEquals(originalMessage.getTo(), actual.getTo());

        // Following values should be cleaned up.
        assertNullValues(actual.getAmqpAnnotatedMessage().getMessageAnnotations(), DEAD_LETTER_SOURCE_KEY_ANNOTATION_NAME,
            ENQUEUED_SEQUENCE_NUMBER_ANNOTATION_NAME, LOCKED_UNTIL_KEY_ANNOTATION_NAME,
            SEQUENCE_NUMBER_ANNOTATION_NAME, ENQUEUED_TIME_UTC_ANNOTATION_NAME);

        assertNullValues(actual.getAmqpAnnotatedMessage().getApplicationProperties(), DEAD_LETTER_DESCRIPTION_ANNOTATION_NAME,
            DEAD_LETTER_REASON_ANNOTATION_NAME);

        Assertions.assertNull(actual.getAmqpAnnotatedMessage().getHeader().getDeliveryCount());
    }

    public void assertNullValues(Map<String, Object> dataMap, AmqpMessageConstant... keys) {
        for (AmqpMessageConstant key : keys) {
            Assertions.assertNull(dataMap.get(key.getValue()));
        }
    }
}
