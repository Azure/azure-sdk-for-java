// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.implementation.ManagementChannel;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.message.Message;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.azure.core.amqp.MessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.MessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.MessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.MessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static com.azure.messaging.eventhubs.TestUtils.APPLICATION_PROPERTIES;
import static com.azure.messaging.eventhubs.TestUtils.ENQUEUED_TIME;
import static com.azure.messaging.eventhubs.TestUtils.OFFSET;
import static com.azure.messaging.eventhubs.TestUtils.OTHER_SYSTEM_PROPERTY;
import static com.azure.messaging.eventhubs.TestUtils.PARTITION_KEY;
import static com.azure.messaging.eventhubs.TestUtils.SEQUENCE_NUMBER;
import static com.azure.messaging.eventhubs.TestUtils.getMessage;
import static java.nio.charset.StandardCharsets.UTF_8;

public class EventHubMessageSerializerTest {
    private final EventHubMessageSerializer serializer = new EventHubMessageSerializer();

    @Test(expected = NullPointerException.class)
    public void deserializeMessageNotNull() {
        serializer.deserialize(null, EventData.class);
    }

    @Test(expected = NullPointerException.class)
    public void deserializeClassNotNull() {
        serializer.deserialize(Proton.message(), null);
    }

    @Test(expected = NullPointerException.class)
    public void serializeObjectNotNull() {
        serializer.serialize(null);
    }

    /**
     * Verify that we cannot serialize something that is not of type EventData.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cannotSerializeObject() {
        String something = "oops";

        serializer.serialize(something);
    }

    /**
     * Verify we can only deserialize supported classes.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cannotDeserializeObject() {
        final Message message = getMessage("hello-world".getBytes(UTF_8));

        serializer.deserialize(message, EventHubAsyncClient.class);
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
        Assert.assertEquals(ENQUEUED_TIME, eventData.getEnqueuedTime());
        Assert.assertEquals(OFFSET, eventData.getOffset());
        Assert.assertEquals(PARTITION_KEY, eventData.getPartitionKey());
        Assert.assertEquals(SEQUENCE_NUMBER, eventData.getSequenceNumber());

        Assert.assertTrue(eventData.getSystemProperties().containsKey(OTHER_SYSTEM_PROPERTY));
        final Object otherPropertyValue = eventData.getSystemProperties().get(OTHER_SYSTEM_PROPERTY);
        Assert.assertTrue(otherPropertyValue instanceof Boolean);
        Assert.assertTrue((Boolean) otherPropertyValue);

        // Verifying our application properties are the same.
        Assert.assertEquals(APPLICATION_PROPERTIES.size(), eventData.getProperties().size());
        APPLICATION_PROPERTIES.forEach((key, value) -> {
            Assert.assertTrue(eventData.getProperties().containsKey(key));
            Assert.assertEquals(value, eventData.getProperties().get(key));
        });

        // Verify that the partitionKey, offset, enqueued time, sequenceNumber properties are no longer in the system
        // properties map.
        for (String property : systemPropertyNames) {
            Assert.assertFalse(property + " should not be in system properties map.",
                eventData.getSystemProperties().containsKey(property));
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
        Assert.assertNotNull(partitionProperties);
        Assert.assertEquals(eventHubName, partitionProperties.getEventHubName());
        Assert.assertEquals(id, partitionProperties.getId());
        Assert.assertEquals(beginningSequenceNumber, partitionProperties.getBeginningSequenceNumber());
        Assert.assertEquals(lastEnqueuedSequenceNumber, partitionProperties.getLastEnqueuedSequenceNumber());
        Assert.assertEquals(lastEnqueuedOffset, partitionProperties.getLastEnqueuedOffset());
        Assert.assertEquals(lastEnqueuedTime, partitionProperties.getLastEnqueuedTime());
        Assert.assertEquals(isEmpty, partitionProperties.isEmpty());
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
        Assert.assertNotNull(properties);
        Assert.assertEquals(eventHubName, properties.getName());
        Assert.assertEquals(createdAt, properties.getCreatedAt());
        Assert.assertArrayEquals(partitionIds, properties.getPartitionIds());
    }
}
