// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tests for {@link IterableStream}.
 */
public class IterableStreamTests {
    @Test
    public void requiresFlux() {
        Assertions.assertThrows(NullPointerException.class, () -> new IterableStream<>((Flux<String>) null));
    }

    @Test
    public void requiresIterable() {
        Assertions.assertThrows(NullPointerException.class, () -> new IterableStream<>((Iterable<String>) null));
    }

    /**
     * Tests that we can stream using a Flux. Subscribing on single, to ensure we don't hit an IllegalStateException.
     */
    @Test
    public void streamFlux() {
        // Arrange
        final Set<String> expected = new HashSet<>();
        expected.add("Something");
        expected.add("Foo");
        expected.add("Bar");

        final Flux<String> flux = Flux.<String>create(sink -> {
            for (String c : expected) {
                sink.next(c);
            }
            sink.complete();
        }).subscribeOn(Schedulers.parallel());
        final IterableStream<String> iterableStream = new IterableStream<>(flux);

        // Act
        final Set<String> actual = iterableStream.stream().collect(Collectors.toSet());

        // Assert
        assertSets(expected, actual);
    }

    /**
     * Tests that we can stream over the Flux multiple times. Subscribing on single, to ensure we don't hit an
     * IllegalStateException.
     */
    @Test
    public void streamFluxMultipleTimes() {
        // Arrange
        final Set<String> expected = new HashSet<>();
        expected.add("Something");
        expected.add("Foo");
        expected.add("Bar");

        final Flux<String> flux = Flux.<String>create(sink -> {
            for (String c : expected) {
                sink.next(c);
            }
            sink.complete();
        }).subscribeOn(Schedulers.single());
        final IterableStream<String> iterableStream = new IterableStream<>(flux);

        // Act
        final Set<String> actual = iterableStream.stream().collect(Collectors.toSet());
        final Set<String> actual2 = iterableStream.stream().collect(Collectors.toSet());

        // Assert
        assertSets(expected, actual);
        assertSets(expected, actual2);
    }

    /**
     * Tests that we can iterate over the Flux. Subscribing on a parallel scheduler to see if it throws an
     * IllegalStateException.
     */
    @Test
    public void iteratorFlux() {
        // Arrange
        final Set<String> expected = new HashSet<>();
        expected.add("Something");
        expected.add("Foo");
        expected.add("Bar");

        final Flux<String> flux = Flux.<String>create(sink -> {
            for (String c : expected) {
                sink.next(c);
            }
            sink.complete();
        }).subscribeOn(Schedulers.parallel());
        final IterableStream<String> iterableStream = new IterableStream<>(flux);

        // Act & Assert
        int counter = 0;
        for (String actual : iterableStream) {
            Assertions.assertTrue(expected.contains(actual));
            counter++;
        }

        Assertions.assertEquals(expected.size(), counter);
    }

    /**
     * Tests that we can iterate over the Flux multiple times. Subscribing on a parallel scheduler to see if it throws
     * an IllegalStateException.
     */
    @Test
    public void iteratorFluxMultipleTimes() {
        // Arrange
        final Set<String> expected = new HashSet<>();
        expected.add("Something");
        expected.add("Foo");
        expected.add("Bar");

        final Flux<String> flux = Flux.<String>create(sink -> {
            for (String c : expected) {
                sink.next(c);
            }
            sink.complete();
        }).subscribeOn(Schedulers.parallel());
        final IterableStream<String> iterableStream = new IterableStream<>(flux);

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
