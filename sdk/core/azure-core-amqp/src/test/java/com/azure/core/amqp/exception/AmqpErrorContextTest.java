// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

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

        final Map<String, Object> actual = context.getErrorInfo();
        Assertions.assertNotNull(actual);
        Assertions.assertTrue(actual.isEmpty());
    }

    /**
     * Verifies properties set correctly.
     */
    @Test
    public void constructorErrorInfo() {
        // Arrange
        String namespace = "an-namespace-test";
        Map<String, Object> expected = new HashMap<>();
        expected.put("foo", 1);
        expected.put("bar", 11);

        // Act
        AmqpErrorContext context = new AmqpErrorContext(namespace, expected);

        // Assert
        Assertions.assertEquals(namespace, context.getNamespace());

        final Map<String, Object> actual = context.getErrorInfo();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected.size(), actual.size());

        expected.forEach((key, value) -> {
            Assertions.assertTrue(actual.containsKey(key));
            Assertions.assertEquals(value, actual.get(key));
        });
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
