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

import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.amqp.models.AmqpMessageBodyType;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Test for {@link ServiceBusMessage}.
 */
public class ServiceBusMessageTest {
    // Create a giant payload with 10000 characters that are "a".
    private static final String PAYLOAD = new String(new char[10000]).replace("\0", "a");
    private static final BinaryData PAYLOAD_BINARY = BinaryData.fromString(PAYLOAD);

    /**
     * Verifies {@link ServiceBusMessage} created using AmqpMessageBody.
     * 1. properly set message body.
     * 2. properly set message body type.
     */
    @Test
    public void copyAmqpConstructorTest() {
        // Arrange
        final List<Object> expected = new ArrayList<>();
        expected.add(1L);
        expected.add("one");

        // Act
        final ServiceBusMessage actual = new ServiceBusMessage(AmqpMessageBody.fromSequence(expected));
        List<Object> actualSequence = actual.getRawAmqpMessage().getBody().getSequence();
        assertEquals(AmqpMessageBodyType.SEQUENCE, actual.getRawAmqpMessage().getBody().getBodyType());
        assertEquals(expected.size(), actualSequence.size());
        assertArrayEquals(expected.toArray(), actualSequence.toArray());
        assertThrows(IllegalStateException.class, () -> {
            actual.getBody();
        });

    }

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

        final short expectedPriority = 10;
        final String expectedFooterValue = "foo-value1";
        final String expectedDeliveryAnnotationsValue = "da-value1";
        final String expectedApplicationValue = "ap-value1";


        final ServiceBusReceivedMessage expected = new ServiceBusReceivedMessage(PAYLOAD_BINARY);
        expected.getRawAmqpMessage().getMessageAnnotations().put(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(), "10");
        expected.getRawAmqpMessage().getMessageAnnotations().put(DEAD_LETTER_SOURCE_KEY_ANNOTATION_NAME.getValue(), "abc");
        expected.getRawAmqpMessage().getMessageAnnotations().put(ENQUEUED_SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(), "11");
        expected.getRawAmqpMessage().getMessageAnnotations().put(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue(), "11");
        expected.getRawAmqpMessage().getApplicationProperties().put(DEAD_LETTER_DESCRIPTION_ANNOTATION_NAME.getValue(), "abc");
        expected.getRawAmqpMessage().getApplicationProperties().put(DEAD_LETTER_REASON_ANNOTATION_NAME.getValue(), "abc");
        expected.setSubject(expectedSubject);
        expected.setTo(expectedTo);
        expected.setReplyTo(expectedReplyTo);
        expected.setReplyToSessionId(expectedReplyToSessionId);
        expected.setCorrelationId(expectedCorrelationId);
        expected.setDeadLetterSource(expectedDeadLetterSource);
        expected.setTimeToLive(expectedTimeToLive);
        expected.setPartitionKey(expectedPartitionKey);

        expected.getRawAmqpMessage().getHeader().setPriority(expectedPriority);

        final Map<String, Object> expectedFooter = expected.getRawAmqpMessage().getFooter();
        expectedFooter.put("foo-1", expectedFooterValue);

        final Map<String, Object> expectedDeliveryAnnotations = expected.getRawAmqpMessage().getDeliveryAnnotations();
        expectedDeliveryAnnotations.put("da-1", expectedDeliveryAnnotationsValue);

        final Map<String, Object> expectedApplicationProperties = expected.getApplicationProperties();
        expectedApplicationProperties.put("ap-1", expectedApplicationValue);

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

        // Change original values
        expected.getRawAmqpMessage().getHeader().setPriority((short) (expectedPriority + 1));
        expectedFooter.put("foo-1", expectedFooterValue + "-changed");
        expected.getRawAmqpMessage().getDeliveryAnnotations().put("da-1", expectedDeliveryAnnotationsValue + "-changed");
        expected.getRawAmqpMessage().getApplicationProperties().put("ap-1", expectedApplicationValue + "-changed");


        // Assert
        assertNotSame(expected.getRawAmqpMessage(), actual.getRawAmqpMessage());

        // Validate updated values
        assertEquals(expectedSubject, actual.getSubject());
        assertEquals(expectedTo, actual.getTo());
        assertEquals(expectedReplyTo, actual.getReplyTo());
        assertEquals(expectedReplyToSessionId, actual.getReplyToSessionId());
        assertEquals(expectedCorrelationId, actual.getCorrelationId());
        assertEquals(expectedTimeToLive, actual.getTimeToLive());
        assertEquals(expectedPartitionKey, actual.getPartitionKey());

        // Following values should be reset.
        assertNull(actual.getRawAmqpMessage().getMessageAnnotations().get(LOCKED_UNTIL_KEY_ANNOTATION_NAME.getValue()));
        assertNull(actual.getRawAmqpMessage().getMessageAnnotations().get(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()));
        assertNull(actual.getRawAmqpMessage().getMessageAnnotations().get(DEAD_LETTER_SOURCE_KEY_ANNOTATION_NAME.getValue()));
        assertNull(actual.getRawAmqpMessage().getMessageAnnotations().get(ENQUEUED_SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()));
        assertNull(actual.getRawAmqpMessage().getMessageAnnotations().get(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue()));

        assertNull(actual.getRawAmqpMessage().getApplicationProperties().get(DEAD_LETTER_DESCRIPTION_ANNOTATION_NAME.getValue()));
        assertNull(actual.getRawAmqpMessage().getApplicationProperties().get(DEAD_LETTER_REASON_ANNOTATION_NAME.getValue()));
        assertNull(actual.getRawAmqpMessage().getHeader().getDeliveryCount());

        // Testing , updating original message did not change copied message values..
        assertEquals(expectedPriority, actual.getRawAmqpMessage().getHeader().getPriority());
        assertEquals(expectedFooterValue, actual.getRawAmqpMessage().getFooter().get("foo-1").toString());
        assertEquals(expectedDeliveryAnnotationsValue, actual.getRawAmqpMessage().getDeliveryAnnotations().get("da-1").toString());
        assertEquals(expectedApplicationValue, actual.getRawAmqpMessage().getApplicationProperties().get("ap-1").toString());

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

        final ServiceBusReceivedMessage originalMessage = new ServiceBusReceivedMessage(PAYLOAD_BINARY);
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
        assertEquals(expectedSubject, originalMessage.getRawAmqpMessage().getProperties().getSubject());
        assertEquals(expectedTo, originalMessage.getRawAmqpMessage().getProperties().getTo().toString());
        assertEquals(expectedReplyTo, originalMessage.getRawAmqpMessage().getProperties().getReplyTo().toString());
        assertEquals(expectedReplyToSessionId, originalMessage.getRawAmqpMessage().getProperties().getReplyToGroupId());
        assertEquals(expectedCorrelationId, originalMessage.getRawAmqpMessage().getProperties().getCorrelationId().toString());

        assertEquals(expectedTimeToLive, originalMessage.getRawAmqpMessage().getHeader().getTimeToLive());

        assertEquals(expectedPartitionKey, originalMessage.getRawAmqpMessage().getMessageAnnotations().get(PARTITION_KEY_ANNOTATION_NAME.getValue()));
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
        assertArrayEquals(encoded, message.getBody().toBytes());
    }

    /**
     * Verify body is created.
     */
    @Test
    void bodyAsBytes() {
        // Arrange
        byte[] expected = "some-contents".getBytes(UTF_8);

        // Act
        ServiceBusMessage message = new ServiceBusMessage(expected);

        // Assert
        assertArrayEquals(expected, message.getBody().toBytes());
    }

    /**
     * Verify that expected exceptions are thrown.
     */
    @Test
    void bodyNotNull() {
        assertThrows(NullPointerException.class, () -> new ServiceBusMessage((String) null));
        assertThrows(NullPointerException.class, () -> new ServiceBusMessage((BinaryData) null));
        assertThrows(NullPointerException.class, () -> new ServiceBusMessage((byte[]) null));
    }

    @Test
    void messagePropertiesShouldNotBeNull() {
        // Arrange
        final ServiceBusMessage serviceBusMessageData = new ServiceBusMessage(PAYLOAD_BINARY);

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
        final ServiceBusMessage serviceBusMessageData = new ServiceBusMessage(BinaryData.fromBytes(byteArray));

        // Assert
        final byte[] actual = serviceBusMessageData.getBody().toBytes();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(0, actual.length);
    }

    /**
     * Verify that we can create an Message with the correct body contents.
     */
    @Test
    void canCreateWithBytePayload() {
        // Arrange
        final ServiceBusMessage serviceBusMessageData = new ServiceBusMessage(PAYLOAD_BINARY);

        // Assert
        Assertions.assertNotNull(serviceBusMessageData.getBody());
        Assertions.assertEquals(PAYLOAD, serviceBusMessageData.getBody().toString());
    }

    @Test
    void sessionIdMustMatchPartitionKeyAndViceVersa() {
        // Arrange
        final ServiceBusMessage serviceBusMessageData = new ServiceBusMessage("not-used-for-test");

        // Setting them up to already be matching.
        serviceBusMessageData.setSessionId("this must match with the other field");
        serviceBusMessageData.setPartitionKey("this must match with the other field");

        // Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            serviceBusMessageData.setSessionId("something inconsistent!");
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            serviceBusMessageData.setPartitionKey("something inconsistent!");
        });
    }

    @Test
    void sessionIdAndPartitionIdCanBeSetToNull() {
        // Arrange
        final ServiceBusMessage serviceBusMessageData = new ServiceBusMessage("not-used-for-test");

        // Act/Assert (ie, throw no exceptions)
        serviceBusMessageData.setPartitionKey(null);
        serviceBusMessageData.setSessionId(null);

        // and the ordering doesn't matter
        serviceBusMessageData.setPartitionKey("hello");
        serviceBusMessageData.setSessionId(null);

        // reset
        serviceBusMessageData.setPartitionKey(null);
        serviceBusMessageData.setSessionId(null);

        serviceBusMessageData.setSessionId("hello");
        serviceBusMessageData.setPartitionKey(null);
    }

    @Test
    void idsCannotBeTooLong() {
        // Arrange
        final ServiceBusMessage serviceBusMessageData = new ServiceBusMessage("not-used-for-test");
        final String longId = new String(new char[128 + 1]).replace("\0", "a");
        final String justRightId = new String(new char[128]).replace("\0", "a");

        // Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            serviceBusMessageData.setMessageId(longId);
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            serviceBusMessageData.setPartitionKey(longId);
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            serviceBusMessageData.setSessionId(longId);
        });

        serviceBusMessageData.setMessageId(justRightId);
        Assertions.assertEquals(justRightId, serviceBusMessageData.getMessageId());

        serviceBusMessageData.setPartitionKey(justRightId);
        Assertions.assertEquals(justRightId, serviceBusMessageData.getPartitionKey());

        serviceBusMessageData.setSessionId(justRightId);
        Assertions.assertEquals(justRightId, serviceBusMessageData.getSessionId());
    }
}
