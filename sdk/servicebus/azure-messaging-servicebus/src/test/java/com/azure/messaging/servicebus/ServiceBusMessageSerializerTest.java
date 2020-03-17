// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpResponseCode;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.messaging.servicebus.TestUtils.APPLICATION_PROPERTIES;
import static com.azure.messaging.servicebus.TestUtils.SEQUENCE_NUMBER;
import static com.azure.messaging.servicebus.TestUtils.getMessage;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceBusMessageSerializerTest {
    private final ServiceBusMessageSerializer serializer = new ServiceBusMessageSerializer();

    @Test
    void deserializeMessageNotNull() {
        assertThrows(NullPointerException.class, () -> serializer.deserialize(null, ServiceBusMessage.class));
    }
    @Test
    void deserializeClassNotNull() {
        assertThrows(NullPointerException.class, () -> serializer.deserialize(Proton.message(), null));
    }

    @Test
    void serializeObjectNotNull() {
        assertThrows(NullPointerException.class, () -> serializer.serialize(null));
    }

    /**
     * Verify that we cannot serialize something that is not of type Message.
     */
    @Test
    void cannotSerializeObject() {
        String something = "oops";
        assertThrows(IllegalArgumentException.class, () -> serializer.serialize(something));
    }

    /**
     * Verify we can only deserialize supported classes.
     */
    @Test
    void cannotDeserializeObject() {
        final org.apache.qpid.proton.message.Message message = getMessage("hello-world".getBytes(UTF_8));
        assertThrows(IllegalArgumentException.class, () -> serializer.deserialize(message, ServiceBusReceiverAsyncClient.class));

        assertThrows(IllegalArgumentException.class, () -> serializer.deserializeList(message, ServiceBusReceiverAsyncClient.class));
    }

    /**
     * Verify that we can deserialize a proton-j message with all the correct contents to {@link ServiceBusMessage}.
     */
    @Test
    void deserializeMessage() {
        // Arrange
        final String payload = "hello-world";
        final byte[] payloadBytes = payload.getBytes(UTF_8);

        final org.apache.qpid.proton.message.Message message = getMessage(payloadBytes);
        message.setAddress("a-to-address");
        message.setContentType("some-content-type");
        message.setCorrelationId("correlation-id-test");
        message.setDeliveryCount(10);
        message.setTtl(1045);
        message.setMessageId("a-test-message-id");
        message.setSubject("this is a label");
        message.getProperties().setTo("this is a to property");
        message.setReplyTo("reply-to-property");
        message.setReplyToGroupId("reply-to-session-id-property");
        message.setGroupId("session-id-as-a-group-id");

        // Act
        final ServiceBusReceivedMessage serviceBusMessage = serializer.deserialize(message, ServiceBusReceivedMessage.class);

        // Assert
        // Verifying all our system properties were properly deserialized.
        assertNotNull(serviceBusMessage.getEnqueuedTime());
        assertEquals(SEQUENCE_NUMBER, serviceBusMessage.getSequenceNumber());

        // Verifying that all our properties are set.
        assertEquals(message.getTtl(), serviceBusMessage.getTimeToLive().toMillis());
        assertEquals(message.getSubject(), serviceBusMessage.getLabel());
        assertEquals(message.getReplyTo(), serviceBusMessage.getReplyTo());
        assertEquals(message.getDeliveryCount(), serviceBusMessage.getDeliveryCount());
        assertEquals(message.getProperties().getTo(), serviceBusMessage.getTo());
        assertEquals(message.getReplyToGroupId(), serviceBusMessage.getReplyToSessionId());
        assertEquals(message.getGroupId(), serviceBusMessage.getSessionId());
        assertEquals(message.getContentType(), serviceBusMessage.getContentType());
        assertEquals(message.getCorrelationId(), serviceBusMessage.getCorrelationId());

        // Verifying our application properties are the same.
        assertEquals(APPLICATION_PROPERTIES.size(), serviceBusMessage.getProperties().size());
        APPLICATION_PROPERTIES.forEach((key, value) -> {
            Assertions.assertTrue(serviceBusMessage.getProperties().containsKey(key));
            assertEquals(value, serviceBusMessage.getProperties().get(key));
        });

        // Verifying the contents of our message is the same.
        assertEquals(payload, new String(serviceBusMessage.getBody(), UTF_8));
    }

    /**
     * Verifies that an empty collection is returned if the status code was not {@link AmqpResponseCode#ACCEPTED}.
     */
    @Test
    void deserializeListMessagesNotOK() {
        // Arrange
        final Map<String, Object> properties = new HashMap<>();
        properties.put("status-code", AmqpResponseCode.FORBIDDEN.getValue());

        final Message message = Proton.message();
        message.setBody(new AmqpValue("test"));
        message.setApplicationProperties(new ApplicationProperties(properties));

        // Act
        final List<ServiceBusReceivedMessage> actual = serializer.deserializeList(message, ServiceBusReceivedMessage.class);

        // Assert
        Assertions.assertNotNull(actual);
        Assertions.assertTrue(actual.isEmpty());
    }
}
