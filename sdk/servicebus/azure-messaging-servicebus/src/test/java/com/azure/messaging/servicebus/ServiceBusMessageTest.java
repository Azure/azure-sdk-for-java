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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ServiceBusMessage}.
 */
public class ServiceBusMessageTest {
    // Create a giant payload with 10000 characters that are "a".
    private static final String PAYLOAD = new String(new char[10000]).replace("\0", "a");
    private static final byte[] PAYLOAD_BYTES = PAYLOAD.getBytes(UTF_8);

    /**
     * Verifies we correctly set values via copy constructor for {@link ServiceBusMessage}. And ensure system properties
     * are cleared.
     */
    @Test
    public void copyConstructorTest() {
        // Arrange
        final ServiceBusReceivedMessage expected = new ServiceBusReceivedMessage(PAYLOAD_BYTES);
        expected.getAmqpAnnotatedMessage().getMessageAnnotations().put(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(), "10");
        expected.getAmqpAnnotatedMessage().getMessageAnnotations().put(DEAD_LETTER_SOURCE_KEY_ANNOTATION_NAME.getValue(), "abc");
        expected.getAmqpAnnotatedMessage().getMessageAnnotations().put(ENQUEUED_SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(), "11");
        expected.getAmqpAnnotatedMessage().getMessageAnnotations().put(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue(), "11");
        expected.getAmqpAnnotatedMessage().getApplicationProperties().put(DEAD_LETTER_DESCRIPTION_ANNOTATION_NAME.getValue(), "abc");
        expected.getAmqpAnnotatedMessage().getApplicationProperties().put(DEAD_LETTER_REASON_ANNOTATION_NAME.getValue(), "abc");

        // Act
        final ServiceBusMessage actual = new ServiceBusMessage(expected);

        // Assert
        assertNotSame(expected.getAmqpAnnotatedMessage(), actual.getAmqpAnnotatedMessage());
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
