// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link HttpHeader}.
 */
public class HeaderTest {
    @Test
    public void testAddValue() {
        // Arrange
        final Header header = new Header("a", "b");
        // Act
        header.addValue("c");

        // Assert
        assertEquals("a:b,c", header.toString());
    }

    @ParameterizedTest
    @MethodSource
    public void testNullArgsConstructor(String name, String value) {
        // Arrange, Act & Assert
        assertThrows(NullPointerException.class, () -> new Header(name, value));
    }

    @Test
    public void testNameValue() {
        // Arrange
        String name = "a";
        String value = "b";

        // Act
        final Header header = new Header(name, value);

        // Assert
        assertEquals(value, header.getValue());
        assertEquals(name, header.getName());
    }

    @Test
    public void testGetValues() {
        // Arrange
        String name = "a";
        String[] values = { "b", "c" };

        // Act
        final Header header = new Header(name, values[0]);
        header.addValue(values[1]);

        // Assert
        assertArrayEquals(values, header.getValues());
    }

    private static Stream<Arguments> testNullArgsConstructor() {
        return Stream.of(Arguments.arguments(null, "a"), Arguments.arguments(null, null));
    }
}
