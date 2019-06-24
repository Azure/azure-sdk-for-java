// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.MessageConstant;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.core.amqp.MessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.MessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.MessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.MessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

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

    @Test(expected = IllegalArgumentException.class)
    public void messageNotNull() {
        new EventData((Message) null);
    }

    @Test
    public void eventPropertiesShouldNotBeNull() {
        // Act
        final EventData eventData = new EventData("Test".getBytes());

        // Assert
        Assert.assertNotNull(eventData.systemProperties());
        Assert.assertNotNull(eventData.body());
        Assert.assertNotNull(eventData.properties());
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
        final byte[] actual = eventData.body().array();
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
        Assert.assertNotNull(eventData.body());
        Assert.assertEquals(PAYLOAD, UTF_8.decode(eventData.body()).toString());
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
        final Instant enqueuedTime = Instant.ofEpochSecond(1561344661);
        final String offset = "an-offset-of-sorts";
        final String partitionKey = "a-partition-key";
        final long sequenceNumber = 1025L;
        final String otherPropertyKey = "Some-other-system-property";
        final Map<Symbol, Object> systemProperties = new HashMap<>();
        systemProperties.put(getSymbol(OFFSET_ANNOTATION_NAME), offset);
        systemProperties.put(getSymbol(PARTITION_KEY_ANNOTATION_NAME), partitionKey);
        systemProperties.put(getSymbol(ENQUEUED_TIME_UTC_ANNOTATION_NAME), Date.from(enqueuedTime));
        systemProperties.put(getSymbol(SEQUENCE_NUMBER_ANNOTATION_NAME), sequenceNumber);
        systemProperties.put(Symbol.getSymbol(otherPropertyKey), Boolean.TRUE);

        final Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put("test-name", EventDataTest.class.getName());
        applicationProperties.put("a-number", 10L);

        final Message message = Proton.message();
        message.setMessageAnnotations(new MessageAnnotations(systemProperties));
        message.setApplicationProperties(new ApplicationProperties(applicationProperties));
        message.setBody(new Data(new Binary(PAYLOAD_BYTES)));

        // Act
        final EventData eventData = new EventData(message);

        // Assert
        // Verifying all our system properties were properly deserialized.
        Assert.assertEquals(enqueuedTime, eventData.enqueuedTime());
        Assert.assertEquals(offset, eventData.offset());
        Assert.assertEquals(partitionKey, eventData.partitionKey());
        Assert.assertEquals(sequenceNumber, eventData.sequenceNumber());

        Assert.assertTrue(eventData.systemProperties().containsKey(otherPropertyKey));
        final Object otherPropertyValue = eventData.systemProperties().get(otherPropertyKey);
        Assert.assertTrue(otherPropertyValue instanceof Boolean);
        Assert.assertTrue((Boolean) otherPropertyValue);

        // Verifying our application properties are the same.
        Assert.assertEquals(applicationProperties.size(), eventData.properties().size());
        applicationProperties.forEach((key, value) -> {
            Assert.assertTrue(eventData.properties().containsKey(key));
            Assert.assertEquals(value, eventData.properties().get(key));
        });

        // Verifying the contents of our message is the same.
    }

    private static Symbol getSymbol(MessageConstant messageConstant) {
        return Symbol.getSymbol(messageConstant.getValue());
    }

    /**
     * Creates an event with the sequence number set.
     */
    private static EventData constructMessage(long sequenceNumber) {
        final HashMap<Symbol, Object> properties = new HashMap<>();
        properties.put(Symbol.getSymbol(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()), sequenceNumber);

        final Message message = Proton.message();
        message.setMessageAnnotations(new MessageAnnotations(properties));

        return new EventData(message);
    }
}
