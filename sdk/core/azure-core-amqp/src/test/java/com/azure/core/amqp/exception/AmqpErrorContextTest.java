// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AmqpErrorContextTest {
    /**
     * Verifies properties set correctly.
     */
    @Test
    public void constructor() {
        // Arrange
        String namespace = "an-namespace-test";

        // Act
        AmqpErrorContext context = new AmqpErrorContext(namespace);

        // Assert
        Assertions.assertEquals(namespace, context.getNamespace());
    }

    /**
     * Verifies an exception is thrown if namespace is an empty string.
     */
    @Test
    public void constructorEmptyString() {
        // Act
        assertThrows(IllegalArgumentException.class, () -> {
            AmqpErrorContext context = new AmqpErrorContext("");
        });
    }

    /**
     * Verifies an exception is thrown if namespace is null.
     */
    @Test
    public void constructorNull() {
        // Act
        assertThrows(IllegalArgumentException.class, () -> {
            AmqpErrorContext context = new AmqpErrorContext(null);
        });
    }
}
