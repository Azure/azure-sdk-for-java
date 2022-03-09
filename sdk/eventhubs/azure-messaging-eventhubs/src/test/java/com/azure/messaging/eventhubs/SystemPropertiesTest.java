// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpMessageConstant;
import com.azure.core.amqp.models.AmqpAddress;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.amqp.models.AmqpMessageId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link SystemProperties}.
 */
public class SystemPropertiesTest {
    private final byte[] data = "hello-world".getBytes(StandardCharsets.UTF_8);
    private final String partitionKey = "my-partition-key";
    private final Instant enqueuedTime = Instant.ofEpochSecond(1625810878);
    private final long offset = 102L;
    private final long sequenceNumber = 12345L;

    private AmqpAnnotatedMessage message;

    @BeforeEach
    public void beforeEach() {
        message = new AmqpAnnotatedMessage(AmqpMessageBody.fromData(data));
        message.getApplicationProperties().put("app1", true);
        message.getDeliveryAnnotations().put("delivery", false);
        message.getFooter().put("footer1", 10);
        message.getFooter().put("footer2", "bars");
        message.getFooter().put("footer3", "zoom");

        message.getHeader()
            .setTimeToLive(Duration.ofSeconds(830))
            .setPriority((short) 12)
            .setDurable(true)
            .setFirstAcquirer(false)
            .setDeliveryCount(19L);

        final Instant expiryTime = Instant.ofEpochSecond(1925644760L);
        final Instant creationTime = Instant.ofEpochSecond(1825644760L);
        message.getProperties()
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

        final Map<String, Object> messageAnnotations = message.getMessageAnnotations();
        messageAnnotations.put(PARTITION_KEY_ANNOTATION_NAME.getValue(), partitionKey);
        messageAnnotations.put(OFFSET_ANNOTATION_NAME.getValue(), offset);
        messageAnnotations.put(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue(), enqueuedTime);
        messageAnnotations.put(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(), sequenceNumber);
        messageAnnotations.put("foo", "bar");
        messageAnnotations.put("baz", 1L);
    }

    /**
     * Asserts that there are no items when the message is new.
     */
    @Test
    public void emptyMessage() {
        // Act
        final SystemProperties systemProperties = new SystemProperties();
        final String defaultValue = "defaultValue";

        // Assert
        assertNull(systemProperties.getPartitionKey());
        assertNull(systemProperties.getEnqueuedTime());
        assertNull(systemProperties.getSequenceNumber());
        assertNull(systemProperties.getOffset());

        assertTrue(systemProperties.isEmpty());

        EventData.RESERVED_SYSTEM_PROPERTIES.forEach(name -> {
            assertNull(systemProperties.get(name));
            assertEquals(defaultValue, systemProperties.getOrDefault(name, defaultValue));
            assertFalse(systemProperties.containsKey(name));
        });
    }

    /**
     * Verifies this view is a read-only view.
     */
    @Test
    public void cannotModifyProperties() {
        // Act
        final SystemProperties properties = new SystemProperties(message, offset, enqueuedTime, sequenceNumber,
            partitionKey);
        final HashMap<String, Object> testMap = new HashMap<>();
        testMap.put("one", 1L);
        testMap.put("two", 2);

        // Assert
        assertEquals(enqueuedTime, properties.getEnqueuedTime());
        assertEquals(sequenceNumber, properties.getSequenceNumber());
        assertEquals(offset, properties.getOffset());
        assertEquals(partitionKey, properties.getPartitionKey());

        assertThrows(UnsupportedOperationException.class, () -> properties.put("foo", "bar"));
        assertThrows(UnsupportedOperationException.class, () -> properties.putAll(testMap));
        assertThrows(UnsupportedOperationException.class, () -> properties.putIfAbsent("unknown_key", "boom"));
        assertThrows(UnsupportedOperationException.class, () -> properties.clear());

        assertThrows(UnsupportedOperationException.class,
            () -> properties.remove(PARTITION_KEY_ANNOTATION_NAME.getValue()));
        assertThrows(UnsupportedOperationException.class,
            () -> properties.remove(PARTITION_KEY_ANNOTATION_NAME.getValue(), "boom"));
        assertThrows(UnsupportedOperationException.class,
            () -> properties.replaceAll((key, value) -> "replaced " + value));
        assertThrows(UnsupportedOperationException.class,
            () -> properties.replace(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(), sequenceNumber, "baz"));

        assertThrows(UnsupportedOperationException.class,
            () -> properties.computeIfAbsent("test", (key) -> "new value"));
        assertThrows(UnsupportedOperationException.class,
            () -> properties.computeIfPresent("baz", (key, existing) -> "new value2"));

        // Key exists but the new value is null.
        assertThrows(UnsupportedOperationException.class,
            () -> properties.compute("baz", (key, existing) -> existing));

        // Key exists and the new value is something else.
        assertThrows(UnsupportedOperationException.class,
            () -> properties.compute("baz", (key, existing) -> "baz + 3"));
    }

    @Test
    public void queryProperties() {
        // Act
        final SystemProperties properties = new SystemProperties(message, offset, enqueuedTime, sequenceNumber,
            partitionKey);

        // Assert
        assertEquals(enqueuedTime, properties.getEnqueuedTime());
        assertEquals(sequenceNumber, properties.getSequenceNumber());
        assertEquals(offset, properties.getOffset());
        assertEquals(partitionKey, properties.getPartitionKey());

        assertEquals(message.getProperties().getMessageId().toString(),
            properties.get(AmqpMessageConstant.MESSAGE_ID.getValue()));

        assertEquals(message.getProperties().getTo().toString(), properties.get(AmqpMessageConstant.TO.getValue()));

        final Object actualUserId = properties.get(AmqpMessageConstant.USER_ID.getValue());
        assertTrue(actualUserId instanceof byte[]);
        assertArrayEquals(message.getProperties().getUserId(), (byte[]) actualUserId);
        assertEquals(message.getProperties().getAbsoluteExpiryTime(),
            properties.get(AmqpMessageConstant.ABSOLUTE_EXPIRY_TIME.getValue()));
        assertEquals(message.getProperties().getCreationTime(),
            properties.get(AmqpMessageConstant.CREATION_TIME.getValue()));
        assertEquals(message.getProperties().getReplyTo().toString(),
            properties.get(AmqpMessageConstant.REPLY_TO.getValue()));
        assertEquals(message.getProperties().getReplyToGroupId(),
            properties.get(AmqpMessageConstant.REPLY_TO_GROUP_ID.getValue()));
        assertEquals(message.getProperties().getContentType(),
            properties.get(AmqpMessageConstant.CONTENT_TYPE.getValue()));
        assertEquals(message.getProperties().getContentEncoding(),
            properties.get(AmqpMessageConstant.CONTENT_ENCODING.getValue()));

        assertEquals(message.getProperties().getSubject(), properties.get(AmqpMessageConstant.SUBJECT.getValue()));
        assertEquals(message.getProperties().getGroupSequence(),
            properties.get(AmqpMessageConstant.GROUP_SEQUENCE.getValue()));
        assertEquals(message.getProperties().getGroupId(), properties.get(AmqpMessageConstant.GROUP_ID.getValue()));
        assertEquals(message.getProperties().getCorrelationId().toString(),
            properties.get(AmqpMessageConstant.CORRELATION_ID.getValue()));

        // Getting one from message annotations
        assertEquals(message.getMessageAnnotations().get("foo"), properties.get("foo"));
        assertEquals(partitionKey, properties.get(PARTITION_KEY_ANNOTATION_NAME.getValue()));
    }
}
