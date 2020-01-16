// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.exception.AzureException;
import com.azure.messaging.eventhubs.implementation.ManagementChannel;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static com.azure.messaging.eventhubs.TestUtils.APPLICATION_PROPERTIES;
import static com.azure.messaging.eventhubs.TestUtils.ENQUEUED_TIME;
import static com.azure.messaging.eventhubs.TestUtils.OFFSET;
import static com.azure.messaging.eventhubs.TestUtils.OTHER_SYSTEM_PROPERTY;
import static com.azure.messaging.eventhubs.TestUtils.PARTITION_KEY;
import static com.azure.messaging.eventhubs.TestUtils.SEQUENCE_NUMBER;
import static com.azure.messaging.eventhubs.TestUtils.getMessage;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EventHubMessageSerializerTest {
    private final EventHubMessageSerializer serializer = new EventHubMessageSerializer();

    @Test
    public void deserializeMessageNotNull() {
        assertThrows(NullPointerException.class, () -> serializer.deserialize(null, EventData.class));
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
     * Verify that we cannot serialize something that is not of type EventData.
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
        final Message message = getMessage("hello-world".getBytes(UTF_8));
        assertThrows(IllegalArgumentException.class, () -> serializer.deserialize(message, EventHubAsyncClient.class));
    }

    /**
     * Verify that we can deserialize a proton-j message with all the correct contents to {@link EventData}.
     */
    @Test
    public void deserializeEventData() {
        // Arrange
        final String[] systemPropertyNames = new String[]{
            PARTITION_KEY_ANNOTATION_NAME.getValue(),
            OFFSET_ANNOTATION_NAME.getValue(),
            ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue(),
            SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(),
        };
        final Message message = getMessage("hello-world".getBytes(UTF_8));

        // Act
        final EventData eventData = serializer.deserialize(message, EventData.class);

        // Assert
        // Verifying all our system properties were properly deserialized.
        Assertions.assertEquals(ENQUEUED_TIME, eventData.getEnqueuedTime());
        Assertions.assertEquals(OFFSET, eventData.getOffset());
        Assertions.assertEquals(PARTITION_KEY, eventData.getPartitionKey());
        Assertions.assertEquals(SEQUENCE_NUMBER, eventData.getSequenceNumber());

        Assertions.assertTrue(eventData.getSystemProperties().containsKey(OTHER_SYSTEM_PROPERTY));
        final Object otherPropertyValue = eventData.getSystemProperties().get(OTHER_SYSTEM_PROPERTY);
        Assertions.assertTrue(otherPropertyValue instanceof Boolean);
        Assertions.assertTrue((Boolean) otherPropertyValue);

        // Verifying our application properties are the same.
        Assertions.assertEquals(APPLICATION_PROPERTIES.size(), eventData.getProperties().size());
        APPLICATION_PROPERTIES.forEach((key, value) -> {
            Assertions.assertTrue(eventData.getProperties().containsKey(key));
            Assertions.assertEquals(value, eventData.getProperties().get(key));
        });

        // Verify that the partitionKey, offset, enqueued time, sequenceNumber properties are no longer in the system
        // properties map.
        for (String property : systemPropertyNames) {
            Assertions.assertFalse(eventData.getSystemProperties().containsKey(property), property + " should not be in system properties map.");
        }

        // Verifying the contents of our message is the same.
    }

    /**
     * Verify we can deserialize a message to {@link PartitionProperties}.
     */
    @Test
    public void deserializePartitionProperties() {
        // Arrange
        final String eventHubName = "my-event-hub";
        final String id = "partition-id-test";
        final long beginningSequenceNumber = 1343L;
        final long lastEnqueuedSequenceNumber = 1500L;
        final String lastEnqueuedOffset = "102";
        final Date lastEnqueuedTimeAsDate  = new Date(1569275540L);
        final Instant lastEnqueuedTime = lastEnqueuedTimeAsDate.toInstant();
        final boolean isEmpty = true;

        final Map<String, Object> values = new HashMap<>();
        values.put(ManagementChannel.MANAGEMENT_ENTITY_NAME_KEY, eventHubName);
        values.put(ManagementChannel.MANAGEMENT_PARTITION_NAME_KEY, id);
        values.put(ManagementChannel.MANAGEMENT_RESULT_BEGIN_SEQUENCE_NUMBER, beginningSequenceNumber);
        values.put(ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_SEQUENCE_NUMBER, lastEnqueuedSequenceNumber);
        values.put(ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_OFFSET, lastEnqueuedOffset);
        values.put(ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_TIME_UTC, lastEnqueuedTimeAsDate);
        values.put(ManagementChannel.MANAGEMENT_RESULT_PARTITION_IS_EMPTY, isEmpty);

        final AmqpValue amqpValue = new AmqpValue(values);

        final Message message = Proton.message();
        message.setBody(amqpValue);

        // Act
        final PartitionProperties partitionProperties = serializer.deserialize(message, PartitionProperties.class);

        // Assert
        Assertions.assertNotNull(partitionProperties);
        Assertions.assertEquals(eventHubName, partitionProperties.getEventHubName());
        Assertions.assertEquals(id, partitionProperties.getId());
        Assertions.assertEquals(beginningSequenceNumber, partitionProperties.getBeginningSequenceNumber());
        Assertions.assertEquals(lastEnqueuedSequenceNumber, partitionProperties.getLastEnqueuedSequenceNumber());
        Assertions.assertEquals(lastEnqueuedOffset, partitionProperties.getLastEnqueuedOffset());
        Assertions.assertEquals(lastEnqueuedTime, partitionProperties.getLastEnqueuedTime());
        Assertions.assertEquals(isEmpty, partitionProperties.isEmpty());
    }

    /**
     * Verify we can deserialize a message to {@link EventHubProperties}.
     */
    @Test
    public void deserializeEventHubProperties() {
        // Arrange
        final String eventHubName = "my-event-hub";
        final Date createdAtAsDate  = new Date(1569275540L);
        final Instant createdAt = createdAtAsDate.toInstant();
        final String[] partitionIds = new String[]{ "1", "foo", "bar", "baz" };

        final Map<String, Object> values = new HashMap<>();
        values.put(ManagementChannel.MANAGEMENT_ENTITY_NAME_KEY, eventHubName);
        values.put(ManagementChannel.MANAGEMENT_RESULT_CREATED_AT, createdAtAsDate);
        values.put(ManagementChannel.MANAGEMENT_RESULT_PARTITION_IDS, partitionIds);

        final AmqpValue amqpValue = new AmqpValue(values);

        final Message message = Proton.message();
        message.setBody(amqpValue);

        // Act
        final EventHubProperties properties = serializer.deserialize(message, EventHubProperties.class);

        // Assert
        Assertions.assertNotNull(properties);
        Assertions.assertEquals(eventHubName, properties.getName());
        Assertions.assertEquals(createdAt, properties.getCreatedAt());
        Assertions.assertArrayEquals(partitionIds, properties.getPartitionIds().stream().toArray(String[]::new));
    }

    /**
     * Verify that it throws if the value is not what we expect. In this case, eventHubName is not a string.
     */
    @Test
    public void throwsWhenIncorrectTypeInResponse() {
        // Arrange
        final Long eventHubName = 100L;
        final Date createdAtAsDate = new Date(1569275540L);
        final String[] partitionIds = new String[]{"1", "foo", "bar", "baz"};

        final Map<String, Object> values = new HashMap<>();
        values.put(ManagementChannel.MANAGEMENT_ENTITY_NAME_KEY, eventHubName);
        values.put(ManagementChannel.MANAGEMENT_RESULT_CREATED_AT, createdAtAsDate);
        values.put(ManagementChannel.MANAGEMENT_RESULT_PARTITION_IDS, partitionIds);

        final AmqpValue amqpValue = new AmqpValue(values);

        final Message message = Proton.message();
        message.setBody(amqpValue);

        assertThrows(AzureException.class, () -> {
            // Act
            serializer.deserialize(message, EventHubProperties.class);
        });
    }

    /**
     * Verify that it throws if the value in the map is null.
     */
    @Test
    public void throwsWhenNullValueInResponse() {
        // Arrange
        final String eventHubName = "event-hub-name-test";
        final Date createdAtAsDate = new Date(1569275540L);

        final Map<String, Object> values = new HashMap<>();
        values.put(ManagementChannel.MANAGEMENT_ENTITY_NAME_KEY, eventHubName);
        values.put(ManagementChannel.MANAGEMENT_RESULT_CREATED_AT, createdAtAsDate);
        values.put(ManagementChannel.MANAGEMENT_RESULT_PARTITION_IDS, null);

        final AmqpValue amqpValue = new AmqpValue(values);

        final Message message = Proton.message();
        message.setBody(amqpValue);

        assertThrows(AzureException.class, () -> {
            // Act
            serializer.deserialize(message, EventHubProperties.class);
        });
    }
}
