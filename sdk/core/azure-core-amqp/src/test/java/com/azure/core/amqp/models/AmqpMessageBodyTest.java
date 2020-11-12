// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for {@link AmqpMessageBody}.
 */
public class AmqpMessageBodyTest {

    /**
     * Verifies we correctly set values via constructor for {@link AmqpAnnotatedMessage}.
     */
    @Test
    public void constructorValidValues() {
        // Arrange
        final byte[] expectedData = "some data 1".getBytes();

        // Act
        final AmqpMessageBody actual = AmqpMessageBody.fromData(expectedData);

        // Assert
        assertEquals(AmqpMessageBodyType.DATA, actual.getBodyType());

        // Validate Message Body
        assertArrayEquals(expectedData, actual.getFirstData());
        assertArrayEquals(expectedData, actual.getData().stream().findFirst().get());
    }

    /**
     * Verifies {@link AmqpMessageBody} constructor for null values.
     */
    @Test
    public void constructorNullValidValues() {
        // Arrange
        final byte[] binaryData = null;

        // Act & Assert
        Assertions.assertThrows(NullPointerException.class, () -> AmqpMessageBody.fromData(binaryData));
    }
}
