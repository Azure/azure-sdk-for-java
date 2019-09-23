package com.azure.messaging.eventhubs;

import org.apache.qpid.proton.message.Message;
import org.junit.Assert;
import org.junit.Test;

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
    private EventHubMessageSerializer serializer = new EventHubMessageSerializer();

    @Test(expected = NullPointerException.class)
    public void messageNotNull() {
        serializer.deserialize(null, EventData.class);
    }

    /**
     * Verify that we can deserialize a proton-j message with all the correct contents.
     */
    @Test
    public void deserializeProtonJMessage() {
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
}
