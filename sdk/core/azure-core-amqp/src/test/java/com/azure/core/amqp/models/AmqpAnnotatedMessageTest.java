// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class for  {@link AmqpAnnotatedMessage}
 */
public class AmqpAnnotatedMessageTest {

    private static final byte[] CONTENTS_BYTES = "Some-contents".getBytes(StandardCharsets.UTF_8);
    private static final BinaryData DATA_BYTES = new BinaryData(CONTENTS_BYTES);

    /**
     * Verifies we correctly set values via constructor for {@link AmqpAnnotatedMessage}.
     */
    @Test
    public void constructorValidValues() {
        // Arrange
        final List<BinaryData> binaryDataList = Collections.singletonList(DATA_BYTES);
        final AmqpDataBody amqpDataBody = new AmqpDataBody(binaryDataList);

        // Act
        final AmqpAnnotatedMessage actual = new AmqpAnnotatedMessage(amqpDataBody);

        // Assert
        assertMessageCreation(actual, AmqpBodyType.DATA, binaryDataList.size());
    }

    /**
     * Verifies we correctly set values via constructor for {@link AmqpAnnotatedMessage}.
     */
    @Test
    public void constructorAmqpValidValues() {
        // Arrange
        final List<BinaryData> listBinaryData = Collections.singletonList(DATA_BYTES);
        final AmqpDataBody amqpDataBody = new AmqpDataBody(listBinaryData);
        final AmqpAnnotatedMessage expected = new AmqpAnnotatedMessage(amqpDataBody);

        // Act
        final AmqpAnnotatedMessage actual = new AmqpAnnotatedMessage(expected);

        // Assert
        assertMessageCreation(actual, AmqpBodyType.DATA, listBinaryData.size());
    }

    /**
     * Verifies {@link AmqpAnnotatedMessage} constructor for null values.
     */
    @Test
    public void constructorNullValidValues() {
        // Arrange
        final AmqpDataBody body = null;

        // Act & Assert
        Assertions.assertThrows(NullPointerException.class, () -> new AmqpAnnotatedMessage(body));
    }

    private void assertMessageCreation(AmqpAnnotatedMessage actual, AmqpBodyType expectedType,
        int messageSizeExpected) {
        assertEquals(expectedType, actual.getBody().getBodyType());
        assertNotNull(actual.getProperties());
        assertNotNull(actual.getHeader());
        assertNotNull(actual.getFooter());
        assertNotNull(actual.getApplicationProperties());
        assertNotNull(actual.getDeliveryAnnotations());
        assertNotNull(actual.getMessageAnnotations());
        assertNotNull(actual.getApplicationProperties());

        // Validate Message Body
        assertNotNull(actual.getBody());
        List<BinaryData> dataList = ((AmqpDataBody) actual.getBody()).getData().stream().collect(Collectors.toList());
        assertEquals(messageSizeExpected, dataList.size());
        assertArrayEquals(CONTENTS_BYTES, dataList.get(0).getData());
    }
}
