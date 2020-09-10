// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import static com.azure.core.amqp.AmqpMessageConstant.DEAD_LETTER_DESCRIPTION_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.DEAD_LETTER_REASON_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.DEAD_LETTER_SOURCE_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_SEQUENCE_NUMBER_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.LOCKED_UNTIL_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * Test for {@link ServiceBusMessage}.
 */
public class ServiceBusMessageTest {
    // Create a giant payload with 10000 characters that are "a".
    private static final String PAYLOAD = new String(new char[10000]).replace("\0", "a");
    private static final byte[] PAYLOAD_BYTES = PAYLOAD.getBytes(UTF_8);

    /**
     * Verifies we correctly set values via copy constructor for {@link ServiceBusMessage}.
     * 1. And ensure system properties are cleared.
     * 2. Ensure modifying original `ServiceBusReceivedMessage` object does not change values of new ServiceBusMessage
     * object created using original `ServiceBusReceivedMessage`.
     */
    @Test
    public void copyConstructorTest() {
        // Arrange
        final String expectedSubject = "old-subject";
        final String expectedTo = "old-to";
        final String expectedReplyTo = "old-reply-to";
        final String expectedReplyToSessionId = "old-reply-to-session-id";
        final String expectedCorrelationId = "old-d-id";
        final String expectedDeadLetterSource = "old-d-l-source";
        final Duration expectedTimeToLive = Duration.ofSeconds(20);
        final String expectedPartitionKey = "old-p-key";

        final ServiceBusReceivedMessage expected = new ServiceBusReceivedMessage(PAYLOAD_BYTES);
        expected.getAmqpAnnotatedMessage().getMessageAnnotations().put(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(), "10");
        expected.getAmqpAnnotatedMessage().getMessageAnnotations().put(DEAD_LETTER_SOURCE_KEY_ANNOTATION_NAME.getValue(), "abc");
        expected.getAmqpAnnotatedMessage().getMessageAnnotations().put(ENQUEUED_SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(), "11");
        expected.getAmqpAnnotatedMessage().getMessageAnnotations().put(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue(), "11");
        expected.getAmqpAnnotatedMessage().getApplicationProperties().put(DEAD_LETTER_DESCRIPTION_ANNOTATION_NAME.getValue(), "abc");
        expected.getAmqpAnnotatedMessage().getApplicationProperties().put(DEAD_LETTER_REASON_ANNOTATION_NAME.getValue(), "abc");
        expected.setSubject(expectedSubject);
        expected.setTo(expectedTo);
        expected.setReplyTo(expectedReplyTo);
        expected.setReplyToSessionId(expectedReplyToSessionId);
        expected.setCorrelationId(expectedCorrelationId);
        expected.setDeadLetterSource(expectedDeadLetterSource);
        expected.setTimeToLive(expectedTimeToLive);
        expected.setPartitionKey(expectedPartitionKey);

        final ServiceBusMessage actual = new ServiceBusMessage(expected);

        // Act
        // Modify the values after invoking copy constructor
        expected.setSubject("new-subject");
        expected.setTo("new-to");
        expected.setReplyTo("new-reply-to");
        expected.setReplyToSessionId("new-session-id");
        expected.setCorrelationId("new-c-id");
        expected.setTimeToLive(Duration.ofSeconds(40));
        expected.setPartitionKey("new-p-key");

        // Assert
        assertNotSame(expected.getAmqpAnnotatedMessage(), actual.getAmqpAnnotatedMessage());

        // Validate updated values
        assertEquals(expectedSubject, actual.getSubject());
        assertEquals(expectedTo, actual.getTo());
        assertEquals(expectedReplyTo, actual.getReplyTo());
        assertEquals(expectedReplyToSessionId, actual.getReplyToSessionId());
        assertEquals(expectedCorrelationId, actual.getCorrelationId());
        assertEquals(expectedTimeToLive, actual.getTimeToLive());
        assertEquals(expectedPartitionKey, actual.getPartitionKey());

        // Following values should be reset.
        assertNull(actual.getAmqpAnnotatedMessage().getMessageAnnotations().get(LOCKED_UNTIL_KEY_ANNOTATION_NAME.getValue()));
        assertNull(actual.getAmqpAnnotatedMessage().getMessageAnnotations().get(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()));
        assertNull(actual.getAmqpAnnotatedMessage().getMessageAnnotations().get(DEAD_LETTER_SOURCE_KEY_ANNOTATION_NAME.getValue()));
        assertNull(actual.getAmqpAnnotatedMessage().getMessageAnnotations().get(ENQUEUED_SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()));
        assertNull(actual.getAmqpAnnotatedMessage().getMessageAnnotations().get(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue()));

        assertNull(actual.getAmqpAnnotatedMessage().getApplicationProperties().get(DEAD_LETTER_DESCRIPTION_ANNOTATION_NAME.getValue()));
        assertNull(actual.getAmqpAnnotatedMessage().getApplicationProperties().get(DEAD_LETTER_REASON_ANNOTATION_NAME.getValue()));
        assertNull(actual.getAmqpAnnotatedMessage().getHeader().getDeliveryCount());
    }


    /**
     * Verifies we correctly set values via copy constructor for {@link ServiceBusMessage}.
     * 1. Ensure modifying original `ServiceBusReceivedMessage` object does not change values of new ServiceBusMessage
     * object changes its values.
     */
    @Test
    public void copyConstructorModifyAfterCopyTest() {
        // Arrange
        final String expectedSubject = "old-subject";
        final String expectedTo = "old-to";
        final String expectedReplyTo = "old-reply-to";
        final String expectedReplyToSessionId = "old-reply-to-session-id";
        final String expectedCorrelationId = "old-d-id";
        final String expectedDeadLetterSource = "old-d-l-source";
        final Duration expectedTimeToLive = Duration.ofSeconds(20);
        final String expectedPartitionKey = "old-p-key";

        final ServiceBusReceivedMessage originalMessage = new ServiceBusReceivedMessage(PAYLOAD_BYTES);
        originalMessage.setSubject(expectedSubject);
        originalMessage.setTo(expectedTo);
        originalMessage.setReplyTo(expectedReplyTo);
        originalMessage.setReplyToSessionId(expectedReplyToSessionId);
        originalMessage.setCorrelationId(expectedCorrelationId);
        originalMessage.setDeadLetterSource(expectedDeadLetterSource);
        originalMessage.setTimeToLive(expectedTimeToLive);
        originalMessage.setPartitionKey(expectedPartitionKey);

        final ServiceBusMessage copiedMessage = new ServiceBusMessage(originalMessage);

        // Act
        // Modify the values after invoking copy constructor
        copiedMessage.setSubject("new-subject");
        copiedMessage.setTo("new-to");
        copiedMessage.setReplyTo("new-reply-to");
        copiedMessage.setReplyToSessionId("new-session-id");
        copiedMessage.setCorrelationId("new-c-id");
        copiedMessage.setTimeToLive(Duration.ofSeconds(40));
        copiedMessage.setPartitionKey("new-p-key");

        // Assert
        // Validate updated values
        assertEquals(expectedSubject, originalMessage.getAmqpAnnotatedMessage().getProperties().getSubject());
        assertEquals(expectedTo, originalMessage.getAmqpAnnotatedMessage().getProperties().getTo());
        assertEquals(expectedReplyTo, originalMessage.getAmqpAnnotatedMessage().getProperties().getReplyTo());
        assertEquals(expectedReplyToSessionId, originalMessage.getAmqpAnnotatedMessage().getProperties().getReplyToGroupId());
        assertEquals(expectedCorrelationId, originalMessage.getAmqpAnnotatedMessage().getProperties().getCorrelationId());

        assertEquals(expectedTimeToLive, originalMessage.getAmqpAnnotatedMessage().getHeader().getTimeToLive());

        assertEquals(expectedPartitionKey, originalMessage.getAmqpAnnotatedMessage().getMessageAnnotations().get(PARTITION_KEY_ANNOTATION_NAME.getValue()));
    }

    /**
     * Verify UTF_8 encoded body is created.
     */
    @Test
    void bodyAsString() {
        // Arrange
        String body = "some-contents";
        byte[] encoded = body.getBytes(UTF_8);

        // Act
        ServiceBusMessage message = new ServiceBusMessage(body);

        // Assert
        assertArrayEquals(encoded, message.getBody());
    }

    /**
     * Verify that expected exceptions are thrown.
     */
    @Test
    void bodyNotNull() {
        assertThrows(NullPointerException.class, () -> new ServiceBusMessage((String) null));
        assertThrows(NullPointerException.class, () -> new ServiceBusMessage((byte[]) null));
    }

    @Test
    void messagePropertiesShouldNotBeNull() {
        // Act
        final ServiceBusMessage serviceBusMessageData = new ServiceBusMessage(PAYLOAD_BYTES);

        // Assert
        Assertions.assertNotNull(serviceBusMessageData.getBody());
        Assertions.assertNotNull(serviceBusMessageData.getContext());
        Assertions.assertNotNull(serviceBusMessageData.getApplicationProperties());
    }

    /**
     * Verify that we can create an Message with an empty byte array.
     */
    @Test
    void canCreateWithEmptyArray() {
        // Arrange
        byte[] byteArray = new byte[0];

        // Act
        final ServiceBusMessage serviceBusMessageData = new ServiceBusMessage(byteArray);

        // Assert
        final byte[] actual = serviceBusMessageData.getBody();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(0, actual.length);
    }

    /**
     * Verify that we can create an Message with the correct body contents.
     */
    @Test
    void canCreateWithBytePayload() {
        // Act
        final ServiceBusMessage serviceBusMessageData = new ServiceBusMessage(PAYLOAD_BYTES);

        // Assert
        Assertions.assertNotNull(serviceBusMessageData.getBody());
        Assertions.assertEquals(PAYLOAD, new String(serviceBusMessageData.getBody(), UTF_8));
    }
}
