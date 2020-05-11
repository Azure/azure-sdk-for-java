// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        Assertions.assertNotNull(receivedMessage.getProperties());
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
        final ServiceBusReceivedMessage originalMessage = new ServiceBusReceivedMessage(PAYLOAD_BYTES);
        originalMessage.setMessageId("mid");
        originalMessage.setContentType("type");
        originalMessage.setCorrelationId("cid");
        originalMessage.setReplyTo("rto");
        originalMessage.setViaPartitionKey("something");
        originalMessage.setTimeToLive(Duration.ofSeconds(10));
        originalMessage.setReplyToSessionId("rsessionid");
        originalMessage.setLabel("label");
        originalMessage.setTo("to");

        // Act
        final ServiceBusMessage messageToSend = new ServiceBusMessage(originalMessage);

        // Assert
        Assertions.assertNotNull(messageToSend);
        Assertions.assertNotNull(messageToSend.getBody());
        Assertions.assertEquals(PAYLOAD, new String(messageToSend.getBody(), UTF_8));
        Assertions.assertEquals(originalMessage.getMessageId(), messageToSend.getMessageId());
        Assertions.assertEquals(originalMessage.getContentType(), messageToSend.getContentType());
        Assertions.assertEquals(originalMessage.getCorrelationId(), messageToSend.getCorrelationId());
        Assertions.assertEquals(originalMessage.getReplyTo(), messageToSend.getReplyTo());
        Assertions.assertEquals(originalMessage.getViaPartitionKey(), messageToSend.getViaPartitionKey());
        Assertions.assertEquals(originalMessage.getTimeToLive().toMillis(), messageToSend.getTimeToLive().toMillis());
        Assertions.assertEquals(originalMessage.getLabel(), messageToSend.getLabel());
        Assertions.assertEquals(originalMessage.getReplyToSessionId(), messageToSend.getReplyToSessionId());
        Assertions.assertEquals(originalMessage.getTo(), messageToSend.getTo());
    }
}
