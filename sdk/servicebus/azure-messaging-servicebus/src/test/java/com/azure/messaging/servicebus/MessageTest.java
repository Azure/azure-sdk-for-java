// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MessageTest {
    // Create a giant payload with 10000 characters that are "a".
    private static final String PAYLOAD = new String(new char[10000]).replace("\0", "a");
    private static final byte[] PAYLOAD_BYTES = PAYLOAD.getBytes(UTF_8);
    private static final String PAYLOAD_STRING = new String(PAYLOAD_BYTES);

    @Test
    public void byteArrayNotNull() {
        assertThrows(NullPointerException.class, () -> new Message((byte[]) null));
    }

    @Test
    public void byteBufferNotNull() {
        assertThrows(NullPointerException.class, () -> new Message((ByteBuffer) null));
    }

    @Test
    public void messagePropertiesShouldNotBeNull() {
        // Act
        final Message messageData = new Message(PAYLOAD_BYTES);

        // Assert
        Assertions.assertNotNull(messageData.getSystemProperties());
        Assertions.assertNotNull(messageData.getBody());
        Assertions.assertNotNull(messageData.getContext());
        Assertions.assertNotNull(messageData.getProperties());
    }

    @Test
    public void stringMessagePropertiesShouldNotBeNull() {
        // Act
        final Message messageData = new Message(PAYLOAD);

        // Assert
        Assertions.assertNotNull(messageData.getSystemProperties());
        Assertions.assertNotNull(messageData.getBody());
        Assertions.assertNotNull(messageData.getContext());
        Assertions.assertNotNull(messageData.getProperties());
    }
    /**
     * Verify that we can create an Message with an empty byte array.
     */
    @Test
    public void canCreateWithEmptyArray() {
        // Arrange
        byte[] byteArray = new byte[0];

        // Act
        final Message messageData = new Message(byteArray);

        // Assert
        final byte[] actual = messageData.getBody();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(0, actual.length);
    }

    /**
     * Verify that we can create an Message with the correct body contents.
     */
    @Test
    public void canCreateWithBytePayload() {
        // Act
        final Message messageData = new Message(PAYLOAD_BYTES);

        // Assert
        Assertions.assertNotNull(messageData.getBody());
        Assertions.assertEquals(PAYLOAD, new String(messageData.getBody(), UTF_8));
    }

    /**
     * Verify that we can create an Message with the correct body contents.
     */
    @Test
    public void canCreateWithStringPayload() {
        // Act
        final Message messageData = new Message(PAYLOAD_STRING);

        // Assert
        Assertions.assertNotNull(messageData.getBody());
        Assertions.assertEquals(PAYLOAD, new String(messageData.getBody(), UTF_8));
    }

    /**
     * Verify that we can create an Message with the correct body contents.
     */
    @Test
    public void canCreateWithStringPayloadAndContentType() {
        // Act
        String contentType = "contentType";
        final Message messageData = new Message(PAYLOAD_STRING, contentType);

        // Assert
        Assertions.assertNotNull(messageData.getBody());
        Assertions.assertEquals(PAYLOAD, new String(messageData.getBody(), UTF_8));
        Assertions.assertEquals(contentType, messageData.getContentType());
    }
}
