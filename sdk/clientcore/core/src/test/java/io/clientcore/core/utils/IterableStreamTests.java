// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tests for {@link IterableStream}.
 */
public class IterableStreamTests {
    @Test
    public void requiresIterable() {
        Assertions.assertThrows(NullPointerException.class, () -> new IterableStream<>((Iterable<String>) null));
    }

    /**
     * Tests that we can stream using an Iterable.
     */
    @Test
    public void streamIterable() {
        // Arrange
        final Set<String> expected = new HashSet<>();
        expected.add("Something");
        expected.add("Foo");
        expected.add("Bar");

        final IterableStream<String> iterableStream = new IterableStream<>(expected);

        // Act
        final Set<String> actual = iterableStream.stream().collect(Collectors.toSet());

        // Assert
        assertSets(expected, actual);
    }

    /**
     * Tests that we can stream over the Iterable multiple times.
     */
    @Test
    public void streamIterableMultipleTimes() {
        // Arrange
        final Set<String> expected = new HashSet<>();
        expected.add("Something");
        expected.add("Foo");
        expected.add("Bar");

        final IterableStream<String> iterableStream = new IterableStream<>(expected);

        // Act
        final Set<String> actual = iterableStream.stream().collect(Collectors.toSet());
        final Set<String> actual2 = iterableStream.stream().collect(Collectors.toSet());

        // Assert
        assertSets(expected, actual);
        assertSets(expected, actual2);
    }

    /**
     * Tests that we can iterate over the Iterable.
     */
    @Test
    public void iteratorIterable() {
        // Arrange
        final Set<String> expected = new HashSet<>();
        expected.add("Something");
        expected.add("Foo");
        expected.add("Bar");

        final IterableStream<String> iterableStream = new IterableStream<>(expected);

        // Act & Assert
        int counter = 0;
        for (String actual : iterableStream) {
            Assertions.assertTrue(expected.contains(actual));
            counter++;
        }

        Assertions.assertEquals(expected.size(), counter);
    }

    /**
     * Tests that we can iterate over the Iterable multiple times.
     */
    @Test
    public void iteratorIterableMultipleTimes() {
        // Arrange
        final Set<String> expected = new HashSet<>();
        expected.add("Something");
        expected.add("Foo");
        expected.add("Bar");

        final IterableStream<String> iterableStream = new IterableStream<>(expected);

        // Act & Assert
        int counter = 0;
        for (String actual : iterableStream) {
            Assertions.assertTrue(expected.contains(actual));
            counter++;
        }

        Assertions.assertEquals(expected.size(), counter);

        int counter2 = 0;
        for (String actual : iterableStream) {
            Assertions.assertTrue(expected.contains(actual));
            counter2++;
        }

        Assertions.assertEquals(expected.size(), counter2);
    }

    private static void assertSets(Set<String> expected, Set<String> actual) {
        Assertions.assertEquals(expected.size(), actual.size());
        for (String str : expected) {
            Assertions.assertTrue(actual.contains(str));
        }
    }
}
