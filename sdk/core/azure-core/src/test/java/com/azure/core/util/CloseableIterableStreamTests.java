// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CloseableIterableStreamTests {
    @Test
    public void requiresIterable() {
        assertThrows(NullPointerException.class, () -> new CloseableIterableStream<>((Iterable<String>) null, () -> {
        }));
    }

    @Test
    public void requiresCloseable() {
        assertThrows(NullPointerException.class, () -> new CloseableIterableStream<>(Arrays.asList("one"), null));
    }

    @Test
    public void delegatesIteration() {
        CloseableIterableStream<String> stream = new CloseableIterableStream<>(Arrays.asList("one", "two"), () -> {
        });

        assertEquals(Arrays.asList("one", "two"), stream.stream().collect(Collectors.toList()));
    }

    @Test
    public void closesResourceOnce() {
        AtomicInteger closeCount = new AtomicInteger();
        CloseableIterableStream<String> stream
            = new CloseableIterableStream<>(Arrays.asList("one"), closeCount::incrementAndGet);

        stream.close();
        stream.close();

        assertEquals(1, closeCount.get());
    }

    @Test
    public void closingJavaStreamClosesResource() {
        AtomicInteger closeCount = new AtomicInteger();
        CloseableIterableStream<String> iterableStream
            = new CloseableIterableStream<>(Arrays.asList("one"), closeCount::incrementAndGet);

        try (Stream<String> stream = iterableStream.stream()) {
            assertEquals(1, stream.count());
        }

        assertEquals(1, closeCount.get());
    }

    @Test
    public void wrapsCloseFailure() {
        IOException closeFailure = new IOException("close failed");
        CloseableIterableStream<String> stream = new CloseableIterableStream<>(Arrays.asList("one"), () -> {
            throw closeFailure;
        });

        IllegalStateException exception = assertThrows(IllegalStateException.class, stream::close);

        assertEquals("Failed to close the iterable stream.", exception.getMessage());
        assertSame(closeFailure, exception.getCause());
    }
}
