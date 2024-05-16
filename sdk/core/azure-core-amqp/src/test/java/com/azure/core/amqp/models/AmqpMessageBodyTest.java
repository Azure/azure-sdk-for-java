// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link AmqpMessageBody}.
 */
public class AmqpMessageBodyTest {

    /**
     * Verifies we correctly set DATA type via constructor for {@link AmqpAnnotatedMessage}.
     */
    @Test
    public void constructorValidData() {
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
     * Verifies we correctly set VALUE type via constructor for {@link AmqpAnnotatedMessage}.
     */
    @Test
    public void constructorValidValue() {
        // Arrange
        final Long expectedData = 10L;

        // Act
        final AmqpMessageBody actual = AmqpMessageBody.fromValue(expectedData);

        // Assert
        assertEquals(AmqpMessageBodyType.VALUE, actual.getBodyType());

        // Validate Message Body
        assertTrue(actual.getValue() instanceof Long);
        assertEquals(expectedData.longValue(), ((Long) actual.getValue()).longValue());
    }

    /**
     * Verifies we correctly set SEQUENCE type via constructor for {@link AmqpAnnotatedMessage}.
     */
    @Test
    public void constructorValidSequence() {
        // Arrange
        final List<Object> expectedData = new ArrayList<>();
        expectedData.add(1L);
        expectedData.add("One");

        // Act
        final AmqpMessageBody actual = AmqpMessageBody.fromSequence(expectedData);

        // Assert
        assertEquals(AmqpMessageBodyType.SEQUENCE, actual.getBodyType());

        // Validate Message Body
        assertEquals(expectedData.size(), actual.getSequence().size());
        assertArrayEquals(expectedData.toArray(), actual.getSequence().toArray());
    }

    /**
     * Verifies we can not modify the the SEQUENCE returned from 'AmqpMessageBody'.
     */
    @Test
    public void getSequenceUnmodifiable() {
        // Arrange
        final List<Object> expectedData = new ArrayList<>();
        expectedData.add(1L);
        expectedData.add("One");

        // Act
        final List<Object> actual = AmqpMessageBody.fromSequence(expectedData).getSequence();

        // Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            actual.add("Not Allowed");
        });
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
