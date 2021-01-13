// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpResponseCode;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.DeliveryAnnotations;
import org.apache.qpid.proton.amqp.messaging.Footer;
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

        // Message Annotations
        Map<Symbol, Object> expectedMessageAnnotations = message.getMessageAnnotations().getValue();
        expectedMessageAnnotations.put(Symbol.valueOf("A"), "A value");

        // Message Annotations
        Map<Symbol, Object> expectedDeliveryAnnotations = new HashMap<>();
        expectedDeliveryAnnotations.put(Symbol.valueOf("D"), "D value");
        message.setDeliveryAnnotations(new DeliveryAnnotations(expectedDeliveryAnnotations));

        Map<Symbol, Object> expectedFooterValues = new HashMap<>();
        expectedFooterValues.put(Symbol.valueOf("footer1"), "footer value");
        message.setFooter(new Footer(expectedFooterValues));

        // Act
        final ServiceBusReceivedMessage actualMessage = serializer.deserialize(message, ServiceBusReceivedMessage.class);

        // Assert
        // Verifying all our system properties were properly deserialized.
        assertNotNull(actualMessage.getEnqueuedTime());
        assertEquals(SEQUENCE_NUMBER, actualMessage.getSequenceNumber());

        // Verifying that all our properties are set.
        assertEquals(message.getTtl(), actualMessage.getTimeToLive().toMillis());
        assertEquals(message.getSubject(), actualMessage.getSubject());
        assertEquals(message.getReplyTo(), actualMessage.getReplyTo());
        assertEquals(message.getDeliveryCount(), actualMessage.getDeliveryCount());
        assertEquals(message.getProperties().getTo(), actualMessage.getTo());
        assertEquals(message.getReplyToGroupId(), actualMessage.getReplyToSessionId());
        assertEquals(message.getGroupId(), actualMessage.getSessionId());
        assertEquals(message.getContentType(), actualMessage.getContentType());
        assertEquals(message.getCorrelationId(), actualMessage.getCorrelationId());

        assertValues(expectedMessageAnnotations, actualMessage.getRawAmqpMessage().getMessageAnnotations());
        assertValues(expectedDeliveryAnnotations, actualMessage.getRawAmqpMessage().getDeliveryAnnotations());
        assertValues(expectedFooterValues, actualMessage.getRawAmqpMessage().getFooter());

        // Verifying our application properties are the same.
        assertEquals(APPLICATION_PROPERTIES.size(), actualMessage.getApplicationProperties().size());
        APPLICATION_PROPERTIES.forEach((key, value) -> {
            Assertions.assertTrue(actualMessage.getApplicationProperties().containsKey(key));
            assertEquals(value, actualMessage.getApplicationProperties().get(key));
        });

        // Verifying the contents of our message is the same.
        assertEquals(payload, actualMessage.getBody().toString());
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

    private void assertValues(Map<Symbol, Object> expected, Map<String, Object> actual) {
        assertEquals(expected.size(), actual.size());
        for (Map.Entry<Symbol, Object> expectedEntry : expected.entrySet()) {
            assertEquals(expectedEntry.getValue(), actual.get(expectedEntry.getKey().toString()));
        }
    }
}
