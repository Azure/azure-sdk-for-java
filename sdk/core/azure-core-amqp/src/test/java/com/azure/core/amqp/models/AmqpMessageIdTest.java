// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link AmqpMessageId}.
 */
public class AmqpMessageIdTest {
    /**
     * Verifies {@link AmqpMessageId} constructor for null values.
     */
    @Test
    public void constructorNullValue() {
        // Arrange, Act & Assert
        Assertions.assertThrows(NullPointerException.class, () -> new AmqpMessageId(null));
    }

    /**
     * Verifies {@link AmqpMessageId} with same value makes instances equal.
     */
    @Test
    public void equalityTest() {
        // Arrange
        final AmqpMessageId id1 = new AmqpMessageId("a");
        final AmqpMessageId id2 = new AmqpMessageId("a");

        // Act & Assert
        Assertions.assertEquals(id1, id2);
    }

    /**
     * Verifies {@link AmqpMessageId} , two different values are not equal.
     */
    @Test
    public void nonEqualityTest() {
        // Arrange
        final AmqpMessageId id1 = new AmqpMessageId("a");
        final AmqpMessageId id2 = new AmqpMessageId("a1");

        // Act & Assert
        Assertions.assertNotEquals(id1, id2);
    }
}
