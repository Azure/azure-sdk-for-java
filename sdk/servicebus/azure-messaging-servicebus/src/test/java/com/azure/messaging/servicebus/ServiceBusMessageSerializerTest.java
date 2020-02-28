// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.apache.qpid.proton.Proton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static com.azure.messaging.servicebus.TestUtils.APPLICATION_PROPERTIES;
import static com.azure.messaging.servicebus.TestUtils.getMessage;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ServiceBusMessageSerializerTest {
    private final ServiceBusMessageSerializer serializer = new ServiceBusMessageSerializer();

    @Test
    public void deserializeMessageNotNull() {
        assertThrows(NullPointerException.class, () -> serializer.deserialize(null, Message.class));
    }
    @Test
    public void deserializeClassNotNull() {
        assertThrows(NullPointerException.class, () -> serializer.deserialize(Proton.message(), null));
    }

    @Test
    public void serializeObjectNotNull() {
        assertThrows(NullPointerException.class, () -> serializer.serialize(null));
    }

    /**
     * Verify that we cannot serialize something that is not of type Message.
     */
    @Test
    public void cannotSerializeObject() {
        String something = "oops";
        assertThrows(IllegalArgumentException.class, () -> serializer.serialize(something));
    }

    /**
     * Verify we can only deserialize supported classes.
     */
    @Test
    public void cannotDeserializeObject() {
        final org.apache.qpid.proton.message.Message message = getMessage("hello-world".getBytes(UTF_8));
        assertThrows(IllegalArgumentException.class, () -> serializer.deserialize(message, ServiceBusReceiverAsyncClient.class));
    }

    /**
     * Verify that we can deserialize a proton-j message with all the correct contents to {@link Message}.
     */
    @Test
    public void deserializeMessage() {
        // Arrange
        String payload = "hello-world";
        byte[] payloadBytes = payload.getBytes(UTF_8);

        final org.apache.qpid.proton.message.Message message = getMessage(payloadBytes);

        // Act
        final Message serviceBusMessage = serializer.deserialize(message, Message.class);

        // Assert
        // Verifying all our system properties were properly deserialized.

        Assertions.assertTrue(serviceBusMessage.getSystemProperties().containsKey(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()));
        Assertions.assertTrue(serviceBusMessage.getSystemProperties().containsKey(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue()));

        // Verifying our application properties are the same.
        Assertions.assertEquals(APPLICATION_PROPERTIES.size(), serviceBusMessage.getProperties().size());
        APPLICATION_PROPERTIES.forEach((key, value) -> {
            Assertions.assertTrue(serviceBusMessage.getProperties().containsKey(key));
            Assertions.assertEquals(value, serviceBusMessage.getProperties().get(key));
        });

        // Verifying the contents of our message is the same.
        Assertions.assertEquals(payload, new String(serviceBusMessage.getBody(), UTF_8));
    }
}
