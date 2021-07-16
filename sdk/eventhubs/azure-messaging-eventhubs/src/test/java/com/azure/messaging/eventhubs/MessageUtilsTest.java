// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.models.AmqpAddress;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.amqp.models.AmqpMessageHeader;
import com.azure.core.amqp.models.AmqpMessageId;
import com.azure.core.amqp.models.AmqpMessageProperties;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.DeliveryAnnotations;
import org.apache.qpid.proton.amqp.messaging.Header;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link MessageUtils}.
 */
public class MessageUtilsTest {
    private static final String MESSAGE = "My-message";
    private static final byte[] MESSAGE_BYTES = MESSAGE.getBytes(StandardCharsets.UTF_8);

    private AmqpAnnotatedMessage expected;

    @BeforeEach
    public void beforeEach() {
        final AmqpMessageBody messageBody = AmqpMessageBody.fromData(MESSAGE_BYTES);
        expected = new AmqpAnnotatedMessage(messageBody);
        expected.getApplicationProperties().put("app1", true);
        expected.getDeliveryAnnotations().put("delivery", false);
        expected.getFooter().put("footer1", 10);
        expected.getFooter().put("footer2", "bars");
        expected.getFooter().put("footer3", "zoom");
        expected.getMessageAnnotations().put("foo", "bar");
        expected.getMessageAnnotations().put("baz", 1L);

        expected.getHeader()
            .setTimeToLive(Duration.ofSeconds(830))
            .setPriority((short) 12)
            .setDurable(true)
            .setFirstAcquirer(false)
            .setDeliveryCount(19L);

        final Instant expiryTime = Instant.ofEpochSecond(1925644760L);
        final Instant creationTime = Instant.ofEpochSecond(1825644760L);
        expected.getProperties()
            .setTo(new AmqpAddress("my-to-address"))
            .setMessageId(new AmqpMessageId("message-ids"))
            .setUserId("user-id-foo".getBytes(StandardCharsets.UTF_8))
            .setAbsoluteExpiryTime(expiryTime.atOffset(ZoneOffset.UTC))
            .setCreationTime(creationTime.atOffset(ZoneOffset.UTC))
            .setReplyTo(new AmqpAddress("reply-to-address"))
            .setReplyToGroupId("reply-to-group")
            .setContentType("my-content")
            .setContentEncoding("my-encoding")
            .setSubject("my-new-subject")
            .setGroupSequence(353L)
            .setGroupId("groups-id2")
            .setCorrelationId(new AmqpMessageId("my-correlation"));
    }

    /**
     * Tests that a message can be converted.
     */
    @Test
    public void toProtonJMessage() {
        // Act
        final Message actual = MessageUtils.toProtonJMessage(expected);

        // Assert
        assertNotNull(actual);

        assertBody(actual);
        assertApplicationAnnotations(actual);
        assertDeliveryAnnotations(actual);
        assertMessageAnnotations(actual);
        assertHeader(actual);
        assertProperties(actual);
    }

    /**
     * Verifies that application properties is null when there are none set.
     */
    @Test
    public void toProtonJMessageNoApplicationProperties() {
        // Arrange
        expected.getApplicationProperties().clear();

        // Act
        final Message actual = MessageUtils.toProtonJMessage(expected);

        // Assert
        assertNotNull(actual);

        assertBody(actual);

        assertNull(actual.getApplicationProperties());
        assertDeliveryAnnotations(actual);
        assertMessageAnnotations(actual);
        assertHeader(actual);
        assertProperties(actual);
    }

    /**
     * Verifies that application properties is null when there are none set.
     */
    @Test
    public void toProtonJMessageNoDeliveryAnnotations() {
        // Arrange
        expected.getDeliveryAnnotations().clear();

        // Act
        final Message actual = MessageUtils.toProtonJMessage(expected);

        // Assert
        assertNotNull(actual);

        assertBody(actual);
        assertApplicationAnnotations(actual);
        assertNull(actual.getDeliveryAnnotations());
        assertMessageAnnotations(actual);
        assertHeader(actual);
        assertProperties(actual);
    }

    /**
     * Message annotations are null when not set.
     */
    @Test
    public void toProtonJMessageNoMessageAnnotations() {
        // Arrange
        expected.getMessageAnnotations().clear();

        // Act
        final Message actual = MessageUtils.toProtonJMessage(expected);

        // Assert
        assertNotNull(actual);

        assertBody(actual);
        assertApplicationAnnotations(actual);
        assertDeliveryAnnotations(actual);
        assertNull(actual.getMessageAnnotations());
        assertHeader(actual);
        assertProperties(actual);
    }

    /**
     * Tests that a message can be converted with no header properties set.
     */
    @Test
    public void toProtonJMessageNoHeaderPropertiesSet() {
        // Arrange
        expected.getHeader()
            .setDurable(null)
            .setPriority(null)
            .setDeliveryCount(null)
            .setDeliveryCount(null)
            .setTimeToLive(null);

        // Act
        final Message actual = MessageUtils.toProtonJMessage(expected);

        // Assert
        assertNotNull(actual);

        assertBody(actual);
        assertApplicationAnnotations(actual);
        assertDeliveryAnnotations(actual);
        assertMessageAnnotations(actual);
        assertNull(actual.getHeader());
        assertProperties(actual);
    }

    private void assertBody(Message actual) {
        final Section body = actual.getBody();
        assertTrue(body instanceof Data);
        assertArrayEquals(MESSAGE_BYTES, ((Data) body).getValue().getArray());
    }

    private void assertApplicationAnnotations(Message actual) {
        final ApplicationProperties applicationProperties = actual.getApplicationProperties();
        assertNotNull(applicationProperties);
        assertNotNull(applicationProperties.getValue());
        assertEquals(expected.getApplicationProperties().size(), applicationProperties.getValue().size());

        assertEquals(expected.getApplicationProperties().size(), applicationProperties.getValue().size());
        expected.getApplicationProperties().forEach(
            (key, value) -> assertEquals(value, applicationProperties.getValue().get(key)));
    }

    private void assertDeliveryAnnotations(Message actual) {
        final DeliveryAnnotations deliveryAnnotations = actual.getDeliveryAnnotations();
        assertNotNull(deliveryAnnotations);
        assertNotNull(deliveryAnnotations.getValue());
        assertMapSymbol(expected.getDeliveryAnnotations(), deliveryAnnotations.getValue());
    }

    private void assertMessageAnnotations(Message actual) {
        final MessageAnnotations messageAnnotations = actual.getMessageAnnotations();
        assertNotNull(messageAnnotations);
        assertNotNull(messageAnnotations.getValue());
        assertMapSymbol(expected.getMessageAnnotations(), messageAnnotations.getValue());
    }

    /**
     * Asserts the header.
     *
     * @param actual The actual message.
     */
    private void assertHeader(Message actual) {
        final Header header = actual.getHeader();
        final AmqpMessageHeader expectedHeader = expected.getHeader();
        assertNotNull(header);

        assertEquals(expectedHeader.isDurable(), header.getDurable());
        assertEquals(expectedHeader.isFirstAcquirer(), header.getFirstAcquirer());

        assertNotNull(header.getDeliveryCount());
        assertEquals(expectedHeader.getDeliveryCount(), header.getDeliveryCount().longValue());

        assertNotNull(header.getPriority());
        assertEquals(expectedHeader.getPriority(), header.getPriority().shortValue());
        assertEquals(expectedHeader.getTimeToLive().toMillis(), header.getTtl().longValue());
    }

    /**
     * Assert properties.
     *
     * @param actual Assert properties.
     */
    private void assertProperties(Message actual) {
        final Properties actualProperties = actual.getProperties();
        final AmqpMessageProperties expectedProperties = expected.getProperties();
        assertEquals(expectedProperties.getTo().toString(), actualProperties.getTo());
        assertEquals(expectedProperties.getMessageId().toString(), actualProperties.getMessageId());

        assertNotNull(actualProperties.getUserId());
        assertArrayEquals(expectedProperties.getUserId(), actualProperties.getUserId().getArray());

        final Instant expectedAbsoluteInstant = expectedProperties.getAbsoluteExpiryTime().toInstant();
        assertEquals(expectedAbsoluteInstant.toEpochMilli(), actual.getExpiryTime());
        assertEquals(expectedAbsoluteInstant, actualProperties.getAbsoluteExpiryTime().toInstant());

        final Instant expectedCreationInstant = expectedProperties.getCreationTime().toInstant();
        assertEquals(expectedCreationInstant.toEpochMilli(), actual.getCreationTime());
        assertEquals(expectedCreationInstant, actualProperties.getCreationTime().toInstant());

        assertEquals(expectedProperties.getReplyTo().toString(), actualProperties.getReplyTo());
        assertEquals(expectedProperties.getReplyToGroupId(), actualProperties.getReplyToGroupId());

        assertNotNull(actualProperties.getContentType());
        assertEquals(expectedProperties.getContentType(), actualProperties.getContentType().toString());

        assertNotNull(actualProperties.getContentEncoding());
        assertEquals(expectedProperties.getContentEncoding(), actualProperties.getContentEncoding().toString());

        assertEquals(expectedProperties.getSubject(), actualProperties.getSubject());

        assertNotNull(actualProperties.getGroupSequence());
        assertEquals(expectedProperties.getGroupSequence(), actualProperties.getGroupSequence().longValue());

        assertEquals(expectedProperties.getGroupId(), actualProperties.getGroupId());

        assertEquals(expectedProperties.getCorrelationId().toString(), actualProperties.getCorrelationId());
    }

    private static void assertMapSymbol(Map<String, Object> expected, Map<Symbol, Object> actual) {
        assertEquals(expected.size(), actual.size());

        expected.forEach((key, value) -> {
            final Symbol symbol = Symbol.valueOf(key);
            assertEquals(value, actual.get(symbol));
        });
    }
}
