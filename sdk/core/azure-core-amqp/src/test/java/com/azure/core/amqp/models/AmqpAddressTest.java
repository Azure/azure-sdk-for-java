// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link AmqpAddress}.
 */
public class AmqpAddressTest {

    /**
     * Verifies {@link AmqpAddress} constructor for null values.
     */
    @Test
    public void constructorNullValue() {
        // Arrange, Act & Assert
        Assertions.assertThrows(NullPointerException.class, () -> new AmqpAddress(null));
    }

    /**
     * Verifies {@link AmqpAddress} with same value makes instances equal.
     */
    @Test
    public void equalityTest() {
        // Arrange
        final AmqpAddress address1 = new AmqpAddress("a");
        final AmqpAddress address2 = new AmqpAddress("a");

        // Act & Assert
        Assertions.assertEquals(address1, address2);
    }

    /**
     * Verifies {@link AmqpAddress} , two different values are not equal.
     */
    @Test
    public void nonEqualityTest() {
        // Arrange
        final AmqpAddress address1 = new AmqpAddress("a");
        final AmqpAddress address2 = new AmqpAddress("a1");

        // Act & Assert
        Assertions.assertNotEquals(address1, address2);
    }
}
