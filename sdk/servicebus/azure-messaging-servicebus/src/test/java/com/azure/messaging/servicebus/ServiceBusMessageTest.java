// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ServiceBusMessageTest {
    // Create a giant payload with 10000 characters that are "a".
    private static final String PAYLOAD = new String(new char[10000]).replace("\0", "a");
    private static final byte[] PAYLOAD_BYTES = PAYLOAD.getBytes(UTF_8);

    @Test
    public void byteArrayNotNull() {
        assertThrows(NullPointerException.class, () -> new ServiceBusMessage((byte[]) null));
    }

    @Test
    public void messagePropertiesShouldNotBeNull() {
        // Act
        final ServiceBusMessage serviceBusMessageData = new ServiceBusMessage(PAYLOAD_BYTES);

        // Assert
        Assertions.assertNotNull(serviceBusMessageData.getBody());
        Assertions.assertNotNull(serviceBusMessageData.getContext());
        Assertions.assertNotNull(serviceBusMessageData.getProperties());
    }

    /**
     * Verify that we can create an Message with an empty byte array.
     */
    @Test
    public void canCreateWithEmptyArray() {
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
    public void canCreateWithBytePayload() {
        // Act
        final ServiceBusMessage serviceBusMessageData = new ServiceBusMessage(PAYLOAD_BYTES);

        // Assert
        Assertions.assertNotNull(serviceBusMessageData.getBody());
        Assertions.assertEquals(PAYLOAD, new String(serviceBusMessageData.getBody(), UTF_8));
    }
}
