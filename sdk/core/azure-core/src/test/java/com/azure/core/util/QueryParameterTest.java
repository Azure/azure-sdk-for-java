// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedList;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for {@link QueryParameter}.
 */
public class QueryParameterTest {
    @Test
    public void testAddValue() {
        // Arrange
        final QueryParameter parameter = new QueryParameter("a", "b");
        // Act
        parameter.addValue("c");

        // Assert
        assertEquals("a=b,c", parameter.toString());
    }

    @ParameterizedTest
    @MethodSource
    public void testNullArgsConstructor(String name, String value) {
        // Arrange, Act & Assert
        assertThrows(NullPointerException.class, () -> new QueryParameter(name, value));
    }

    @Test
    public void testNullValuesConstructor() {
        // Arrange
        String name = "a";
        LinkedList<String> values = null;

        // Act & Assert
        assertThrows(NullPointerException.class, () -> new QueryParameter(name, values));
    }

    @Test
    public void testNameValue() {
        // Arrange
        String name = "a";
        String value = "b";

        // Act
        final QueryParameter parameter = new QueryParameter(name, value);

        // Assert
        assertEquals(value, parameter.getValue());
        assertEquals(name, parameter.getName());
    }

    @Test
    public void testGetValues() {
        // Arrange
        String name = "a";
        String[] values = { "b", "c" };

        // Act
        final QueryParameter parameter = new QueryParameter(name, values[0]);
        parameter.addValue(values[1]);

        // Assert
        assertArrayEquals(values, parameter.getValues());
    }

    private static Stream<Arguments> testNullArgsConstructor() {
        return Stream.of(Arguments.arguments(null, "a"), Arguments.arguments(null, null));
    }
}
