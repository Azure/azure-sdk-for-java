// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.BinaryData;
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
import java.util.Map;

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

        // These ones are never set.
        Assertions.assertNull(eventData.getContentType());
        Assertions.assertNull(eventData.getCorrelationId());
        Assertions.assertNull(eventData.getMessageId());
    }

    /**
     * Tests that the system properties are properly populated. In addition, partition key is removed because it is not
     * a system property.
     */
    @Test
    public void testSystemProperties() {
        // Act
        final long sequenceNumber = 5L;
        final HashMap<Symbol, Object> properties = new HashMap<>();
        properties.put(getSymbol(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()), sequenceNumber);
        properties.put(getSymbol(OFFSET_ANNOTATION_NAME.getValue()), String.valueOf(OFFSET));
        properties.put(getSymbol(PARTITION_KEY_ANNOTATION_NAME.getValue()), PARTITION_KEY);
        properties.put(getSymbol(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue()), Date.from(ENQUEUED_TIME));

        final byte[] contents = "boo".getBytes(UTF_8);
        final Message message = Proton.message();
        message.setMessageAnnotations(new MessageAnnotations(properties));
        message.setBody(new Data(new Binary(contents)));

        // Act
        final EventData eventData = MESSAGE_SERIALIZER.deserialize(message, EventData.class);

        // Assert
        final Map<String, Object> systemProperties = eventData.getSystemProperties();

        Assertions.assertEquals(properties.size(), systemProperties.size());
        Assertions.assertEquals(OFFSET, systemProperties.get(OFFSET_ANNOTATION_NAME.getValue()));
        Assertions.assertEquals(sequenceNumber, systemProperties.get(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()));
        Assertions.assertEquals(ENQUEUED_TIME, systemProperties.get(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue()));

        Assertions.assertEquals(PARTITION_KEY, eventData.getPartitionKey());
        Assertions.assertEquals(OFFSET, eventData.getOffset());
        Assertions.assertEquals(sequenceNumber, eventData.getSequenceNumber());
        Assertions.assertEquals(ENQUEUED_TIME, eventData.getEnqueuedTime());
    }

    /**
     * Tests that the properties are set. In addition, checks the raw AMQP message to see that it has been added there.
     */
    @Test
    public void setAmqpProperties() {
        // Arrange
        final BinaryData binaryData = BinaryData.fromBytes(PAYLOAD_BYTES);
        final String messageId = "a-message-id";
        final String contentType = "content-type=json";
        final String correlationId = "my-correlation";

        // Act
        final EventData eventData = new EventData(binaryData)
            .setContentType(contentType)
            .setCorrelationId(correlationId)
            .setMessageId(messageId);

        // Assert
        Assertions.assertNotNull(eventData.getBody());
        Assertions.assertEquals(PAYLOAD, new String(eventData.getBody(), UTF_8));

        final BinaryData actualBinary = eventData.getBodyAsBinaryData();
        Assertions.assertNotNull(actualBinary);
        Assertions.assertArrayEquals(PAYLOAD_BYTES, actualBinary.toBytes());
    }
}
