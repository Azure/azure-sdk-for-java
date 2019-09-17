// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
import static org.apache.qpid.proton.amqp.Symbol.getSymbol;

public class EventDataTest {
    // Create a giant payload with 10000 characters that are "a".
    private static final String PAYLOAD = new String(new char[10000]).replace("\0", "a");
    private static final byte[] PAYLOAD_BYTES = PAYLOAD.getBytes(UTF_8);

    @Test(expected = NullPointerException.class)
    public void byteArrayNotNull() {
        new EventData((byte[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void byteBufferNotNull() {
        new EventData((ByteBuffer) null);
    }

    @Test(expected = NullPointerException.class)
    public void messageNotNull() {
        new EventData((Message) null);
    }

    @Test
    public void eventPropertiesShouldNotBeNull() {
        // Act
        final EventData eventData = new EventData("Test".getBytes());

        // Assert
        Assert.assertNotNull(eventData.getSystemProperties());
        Assert.assertNotNull(eventData.getBody());
        Assert.assertNotNull(eventData.getProperties());
    }

    /**
     * Verify that we can create an EventData with an empty byte array.
     */
    @Test
    public void canCreateWithEmptyArray() {
        // Arrange
        byte[] byteArray = new byte[0];

        // Act
        final EventData eventData = new EventData(byteArray);

        // Assert
        final byte[] actual = eventData.getBody().array();
        Assert.assertNotNull(actual);
        Assert.assertEquals(0, actual.length);
    }

    /**
     * Verify that we can create an EventData with the correct body contents.
     */
    @Test
    public void canCreateWithPayload() {
        // Act
        final EventData eventData = new EventData(PAYLOAD_BYTES);

        // Assert
        Assert.assertNotNull(eventData.getBody());
        Assert.assertEquals(PAYLOAD, UTF_8.decode(eventData.getBody()).toString());
    }

    /**
     * Verify that the Comparable interface is implemented correctly for EventData by sorting events by their squence
     * numbers.
     */
    @Test
    public void comparableEventDataSequenceNumbers() {
        // Arrange
        final EventData[] events = new EventData[]{
            constructMessage(19),
            constructMessage(22),
            constructMessage(25),
            constructMessage(88),
        };

        final List<EventData> unordered = new ArrayList<>();
        unordered.add(events[1]);
        unordered.add(events[0]);
        unordered.add(events[3]);
        unordered.add(events[2]);

        // Act
        Collections.sort(unordered);

        // Assert
        for (int i = 0; i < events.length; i++) {
            Assert.assertSame(events[i], unordered.get(i));
        }
    }

    /**
     * Verify that we can deserialize a proton-j message with all the correct contents.
     */
    @Test
    public void deserializeProtonJMessage() {
        // Arrange
        final String[] systemPropertyNames = new String[] {
            PARTITION_KEY_ANNOTATION_NAME.getValue(),
            OFFSET_ANNOTATION_NAME.getValue(),
            ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue(),
            SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(),
        };
        final Message message = getMessage(PAYLOAD_BYTES);

        // Act
        final EventData eventData = new EventData(message);

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
     * Creates an event with the sequence number set.
     */
    private static EventData constructMessage(long sequenceNumber) {
        final HashMap<Symbol, Object> properties = new HashMap<>();
        properties.put(getSymbol(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()), sequenceNumber);
        properties.put(getSymbol(OFFSET_ANNOTATION_NAME.getValue()), String.valueOf(OFFSET));
        properties.put(getSymbol(PARTITION_KEY_ANNOTATION_NAME.getValue()), PARTITION_KEY);
        properties.put(getSymbol(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue()), Date.from(ENQUEUED_TIME));

        final Message message = Proton.message();
        message.setMessageAnnotations(new MessageAnnotations(properties));

        return new EventData(message);
    }
}
