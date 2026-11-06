// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link Header}.
 */
public class HeaderTest {
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void cachedMultiValueHeaderIsInvalidated(boolean useList) {
        Header header = useList ? new Header("name", Arrays.asList("one", "two")) : new Header("name", "one", "two");

        String cachedValue = header.getValue();
        assertEquals("one,two", cachedValue);
        assertSame(cachedValue, header.getValue());
        assertEquals("name:one,two", header.toString());

        header.addValue("three");

        assertEquals("name:one,two,three", header.toString());
        assertEquals("one,two,three", header.getValue());
        assertSame(header.getValue(), header.getValue());
    }

    @Test
    public void emptyHeaderTransitionsToCachedMultiValueHeader() {
        Header header = new Header("name");
        assertEquals("", header.getValue());
        assertEquals("", header.toString());

        header.addValue("one");
        assertEquals("one", header.getValue());
        assertEquals("name:one", header.toString());

        header.addValue("two");
        assertEquals("one,two", header.getValue());
        header.addValue("three");
        assertEquals("one,two,three", header.getValue());
    }

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
