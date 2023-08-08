// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.messaging.servicebus.implementation.ServiceBusDescribedType;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.DeliveryAnnotations;
import org.apache.qpid.proton.amqp.messaging.Footer;
import org.apache.qpid.proton.amqp.messaging.Header;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.messaging.servicebus.TestUtils.APPLICATION_PROPERTIES;
import static com.azure.messaging.servicebus.TestUtils.SEQUENCE_NUMBER;
import static com.azure.messaging.servicebus.TestUtils.getMessage;
import static com.azure.messaging.servicebus.TestUtils.getServiceBusMessage;
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
     * Verifies that we can serialize OffsetDateTime, Duration and URI in application properties.
     */
    @Test
    void serializeMessageWithSpecificApplicationProperties() {
        String contents = "some contents";
        String messageId = "messageId";
        final ServiceBusMessage message = getServiceBusMessage(contents, messageId);

        HashMap<String, Object> specificMap = new HashMap<>();
        specificMap.put("uri", URI.create("https://www.github.com/"));
        specificMap.put("duration", Duration.ZERO);
        specificMap.put("offsetDateTime", OffsetDateTime.now());
        message.getApplicationProperties().putAll(specificMap);

        Message amqpMessage = serializer.serialize(message);

        assertEquals(specificMap.size(), amqpMessage.getApplicationProperties().getValue().size());

        AtomicInteger convertCount = new AtomicInteger();
        specificMap.forEach((key, value) -> {
            Assertions.assertTrue(amqpMessage.getApplicationProperties().getValue().containsKey(key));
            if (value instanceof URI) {
                assertEquals(((URI) value).toString(), ((ServiceBusDescribedType) amqpMessage.getApplicationProperties().getValue().get(key)).getDescribed());
                convertCount.getAndIncrement();
            } else if (value instanceof Duration) {
                // For align with .net SDK ticks, convert will lose 2 digit.
                convertCount.getAndIncrement();
            } else if (value instanceof OffsetDateTime) {
                // For align with .net SDK ticks, convert will lose 2 digit.
                convertCount.getAndIncrement();
            }
        });

        assertEquals(specificMap.size(), convertCount.get());

    }

    /**
     * Message with specific type send from .net SDK.
     *
     * ServiceBusMessage message = new ServiceBusMessage("Hello world!");
     * DateTime utcTime1 = DateTime.Parse("2022-02-24T08:23:23.443127200Z");
     * utcTime1 = DateTime.SpecifyKind(utcTime1, DateTimeKind.Utc);
     * message.ApplicationProperties.Add("time", utcTime2);
     * message.ApplicationProperties.Add("span", TimeSpan.FromSeconds(10));
     * message.ApplicationProperties.Add("uri", new Uri("https://www.github.com/"));
     */
    @Test
    void deserializeRealMessageFromByte() {
        byte[] data = new byte[] {
            0, 83, 112, -64, 10, 5, 64, 64, 112, 72, 25, 8, 0, 64, 67, 0, 83, 113, -63, 36, 2, -93, 16, 120, 45, 111,
            112, 116, 45, 108, 111, 99, 107, 45, 116, 111, 107, 101, 110, -104, -99, -119, 88, -41, -124, -37, 69, 10,
            -98, -95, -99, 119, -64, -61, 36, 90, 0, 83, 114, -63, 85, 6, -93, 19, 120, 45, 111, 112, 116, 45, 101, 110,
            113, 117, 101, 117, 101, 100, 45, 116, 105, 109, 101, -125, 0, 0, 1, 127, 42, -30, 45, 43, -93, 21, 120, 45,
            111, 112, 116, 45, 115, 101, 113, 117, 101, 110, 99, 101, 45, 110, 117, 109, 98, 101, 114, 85, 78, -93, 18,
            120, 45, 111, 112, 116, 45, 108, 111, 99, 107, 101, 100, 45, 117, 110, 116, 105, 108, -125, 0, 0, 1, 127,
            42, -30, -94, 106, 0, 83, 115, -64, 63, 13, -95, 32, 53, 98, 100, 50, 56, 100, 98, 97, 48, 56, 54, 99, 52,
            98, 57, 99, 98, 55, 55, 49, 99, 100, 97, 97, 101, 102, 52, 51, 102, 102, 49, 98, 64, 64, 64, 64, 64, 64, 64,
            -125, 0, 0, 1, 127, 114, -5, 53, 43, -125, 0, 0, 1, 127, 42, -30, 45, 43, 64, 64, 64, 0, 83, 116, -63, -118,
            6, -95, 4, 116, 105, 109, 101, 0, -93, 29, 99, 111, 109, 46, 109, 105, 99, 114, 111, 115, 111, 102, 116, 58,
            100, 97, 116, 101, 116, 105, 109, 101, 45, 111, 102, 102, 115, 101, 116, -127, 8, -39, -9, -79, -6, -116,
            -83, 40, -95, 4, 115, 112, 97, 110, 0, -93, 22, 99, 111, 109, 46, 109, 105, 99, 114, 111, 115, 111, 102,
            116, 58, 116, 105, 109, 101, 115, 112, 97, 110, -127, 0, 0, 0, 0, 5, -11, -31, 0, -95, 3, 117, 114, 105, 0,
            -93, 17, 99, 111, 109, 46, 109, 105, 99, 114, 111, 115, 111, 102, 116, 58, 117, 114, 105, -95, 23, 104, 116,
            116, 112, 115, 58, 47, 47, 119, 119, 119, 46, 103, 105, 116, 104, 117, 98, 46, 99, 111, 109, 47, 0, 83, 117,
            -96, 12, 72, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 33
        };
        final Message amqpMessage = Proton.message();
        amqpMessage.decode(data, 0, data.length);

        amqpMessage.setHeader(new Header());

        final ServiceBusReceivedMessage actualMessage = serializer.deserialize(amqpMessage, ServiceBusReceivedMessage.class);

        AtomicInteger convertCount = new AtomicInteger();

        HashMap<String, Object> specificMap = new HashMap<>();
        specificMap.put("uri", URI.create("https://www.github.com/"));
        specificMap.put("span", Duration.ofSeconds(2));
        specificMap.put("time", OffsetDateTime.parse("2022-02-24T08:23:23.443127200Z"));

        assertEquals(specificMap.size(), actualMessage.getApplicationProperties().size());

        specificMap.forEach((key, value) -> {
            Assertions.assertTrue(actualMessage.getApplicationProperties().containsKey(key));
            if (value instanceof URI) {
                assertEquals((URI) value, actualMessage.getApplicationProperties().get(key));
                convertCount.getAndIncrement();
            } else if (value instanceof Duration) {
                assertEquals((Duration) value, specificMap.get("span"));
                convertCount.getAndIncrement();
            } else if (value instanceof OffsetDateTime) {
                assertEquals((OffsetDateTime) value, specificMap.get("time"));
                convertCount.getAndIncrement();
            }
        });

        assertEquals(specificMap.size(), convertCount.get());

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
