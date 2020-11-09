// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.Context;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static com.azure.messaging.eventhubs.TestUtils.ENQUEUED_TIME;
import static com.azure.messaging.eventhubs.TestUtils.OFFSET;
import static com.azure.messaging.eventhubs.TestUtils.PARTITION_KEY;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.qpid.proton.amqp.Symbol.getSymbol;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EventDataTest {
    // Create a giant payload with 10000 characters that are "a".
    private static final String PAYLOAD = new String(new char[10000]).replace("\0", "a");
    private static final byte[] PAYLOAD_BYTES = PAYLOAD.getBytes(UTF_8);
    private static final MessageSerializer MESSAGE_SERIALIZER = new EventHubMessageSerializer();
    private static final String KEY_NAME = "some-key-name";
    private static final String KEY_VALUE = "ctzMq410TV3wS7upTBcunJTDLEJwMAZuFPfr0mrrA08=";
    final EventData eventData = new EventData(PAYLOAD_BYTES);

    @Test
    public void byteArrayNotNull() {
        assertThrows(NullPointerException.class, () -> new EventData((byte[]) null));
    }

    @Test
    public void byteBufferNotNull() {
        assertThrows(NullPointerException.class, () -> new EventData((ByteBuffer) null));
    }

    @Test
    public void eventPropertiesShouldNotBeNull() {
        // Act
        final EventData eventData = new EventData("Test".getBytes());

        // Assert
        Assertions.assertNotNull(eventData.getSystemProperties());
        Assertions.assertNotNull(eventData.getBody());
        Assertions.assertNotNull(eventData.getProperties());
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
        final byte[] actual = eventData.getBody();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(0, actual.length);
    }

    /**
     * Verify that we can create an EventData with the correct body contents.
     */
    @Test
    public void canCreateWithPayload() {
        // Act
        final EventData eventData = new EventData(PAYLOAD_BYTES);

        // Assert
        Assertions.assertNotNull(eventData.getBody());
        Assertions.assertEquals(PAYLOAD, new String(eventData.getBody(), UTF_8));
    }

    @Test
    public void testSystemProperties() {
        EventData eventData = constructMessage(5);
        Assertions.assertEquals(3, eventData.getSystemProperties().size());
        Assertions.assertEquals(OFFSET, eventData.getSystemProperties().get(OFFSET_ANNOTATION_NAME.getValue()));
        Assertions.assertEquals(5L, eventData.getSystemProperties().get(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()));
        Assertions.assertEquals(ENQUEUED_TIME,
            eventData.getSystemProperties().get(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue()));
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

        final byte[] contents = "boo".getBytes(UTF_8);
        final Message message = Proton.message();
        message.setMessageAnnotations(new MessageAnnotations(properties));
        message.setBody(new Data(new Binary(contents)));

        return MESSAGE_SERIALIZER.deserialize(message, EventData.class);
    }

    @Test
    public void getBodyAsStringTest() {
        final EventData eventData = new EventData(PAYLOAD_BYTES);
        Assertions.assertNotNull(eventData.getBodyAsString());
        Assertions.assertEquals(PAYLOAD, eventData.getBodyAsString());
    }

    @Test
    public void getContextTest() {
        final EventData eventData = new EventData(PAYLOAD_BYTES);
        Context context = eventData.getContext();
        Assertions.assertNotNull(context);
    }

    @Test
    public void addContextTest() {
        final EventData eventData = new EventData(PAYLOAD_BYTES);
        Assertions.assertNotNull(eventData.addContext(KEY_NAME, KEY_VALUE));
        Assertions.assertNotNull(eventData);
    }

    @Test
    public void equalsTest() {
        final EventData eventData1 = new EventData(PAYLOAD_BYTES);
        boolean eventData2 = new EventData(PAYLOAD_BYTES).equals(eventData1);
        Assertions.assertTrue(eventData2);
    }

    @Test
    public void hashCodeTest() {
        int hashValue = 247131905;
        int hashCodeValue = eventData.hashCode();
        Assertions.assertEquals(hashValue, hashCodeValue);
    }

    @Test
    public void getOffsetTest() {
        Long returnLongValue = eventData.getOffset();
        Assertions.assertNotEquals(1L, returnLongValue);
    }

    @Test
    public void eventDataStringTest() {
        final EventData eventData = new EventData(PAYLOAD);
        Assertions.assertNotNull(eventData.getSystemProperties());
        Assertions.assertNotNull(eventData.getBody());
        Assertions.assertNotNull(eventData.getProperties());
    }

}
