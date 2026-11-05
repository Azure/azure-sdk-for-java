// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    public void requiresOwnedResource() {
        assertThrows(NullPointerException.class, () -> new CloseableIterableStream<>(Arrays.asList("one"), null));
    }

    @Test
    public void delegatesIteration() {
        CloseableIterableStream<String> stream = new CloseableIterableStream<>(Arrays.asList("one", "two"), () -> {
        });

        assertEquals(Arrays.asList("one", "two"), stream.stream().collect(Collectors.toList()));
    }

    @Test
    public void delegatesIteratorCreation() {
        AtomicInteger iteratorCount = new AtomicInteger();
        Iterable<String> iterable = () -> {
            iteratorCount.incrementAndGet();
            return Arrays.asList("one").iterator();
        };
        CloseableIterableStream<String> stream = new CloseableIterableStream<>(iterable, () -> {
        });

        stream.iterator();
        stream.iterator();

        assertEquals(2, iteratorCount.get());
    }

    @Test
    public void closesResourceOnce() throws IOException {
        AtomicInteger closeCount = new AtomicInteger();
        CloseableIterableStream<String> stream
            = new CloseableIterableStream<>(Arrays.asList("one"), closeCount::incrementAndGet);

        stream.close();
        stream.close();

        assertEquals(1, closeCount.get());
    }

    @Test
    public void concurrentCloseClosesResourceOnce() throws Exception {
        AtomicInteger closeCount = new AtomicInteger();
        CloseableIterableStream<String> stream
            = new CloseableIterableStream<>(Arrays.asList("one"), closeCount::incrementAndGet);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<?>> closes = new ArrayList<>();

        try {
            for (int i = 0; i < 16; i++) {
                closes.add(executor.submit(() -> {
                    start.await();
                    stream.close();
                    return null;
                }));
            }

            start.countDown();
            for (Future<?> close : closes) {
                close.get(5, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }

        assertEquals(1, closeCount.get());
    }

    @Test
    public void tryWithResourcesClosesAfterEarlyExit() throws IOException {
        AtomicInteger closeCount = new AtomicInteger();

        try (CloseableIterableStream<String> stream
            = new CloseableIterableStream<>(Arrays.asList("one", "two"), closeCount::incrementAndGet)) {
            for (String ignored : stream) {
                break;
            }
        }

        assertEquals(1, closeCount.get());
    }

    @Test
    public void tryWithResourcesClosesAfterProcessingFailure() {
        AtomicInteger closeCount = new AtomicInteger();

        assertThrows(IllegalStateException.class, () -> {
            try (CloseableIterableStream<String> stream
                = new CloseableIterableStream<>(Arrays.asList("one"), closeCount::incrementAndGet)) {
                for (String ignored : stream) {
                    throw new IllegalStateException("processing failed");
                }
            }
        });

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
    public void directClosePropagatesIOException() {
        IOException closeFailure = new IOException("close failed");
        CloseableIterableStream<String> stream = new CloseableIterableStream<>(Arrays.asList("one"), () -> {
            throw closeFailure;
        });

        IOException exception = assertThrows(IOException.class, stream::close);

        assertSame(closeFailure, exception);
    }

    @Test
    public void closingJavaStreamWrapsIOException() {
        IOException closeFailure = new IOException("close failed");
        CloseableIterableStream<String> iterableStream = new CloseableIterableStream<>(Arrays.asList("one"), () -> {
            throw closeFailure;
        });

        UncheckedIOException exception = assertThrows(UncheckedIOException.class, iterableStream.stream()::close);

        assertSame(closeFailure, exception.getCause());
    }

    @Test
    public void closeFailureIsNotRetried() {
        AtomicInteger closeCount = new AtomicInteger();
        IOException closeFailure = new IOException("close failed");
        CloseableIterableStream<String> stream = new CloseableIterableStream<>(Arrays.asList("one"), () -> {
            closeCount.incrementAndGet();
            throw closeFailure;
        });

        assertSame(closeFailure, assertThrows(IOException.class, stream::close));
        assertDoesNotThrow(stream::close);
        assertEquals(1, closeCount.get());
    }

    @Test
    public void directClosePropagatesRuntimeException() {
        IllegalStateException closeFailure = new IllegalStateException("close failed");
        CloseableIterableStream<String> stream = new CloseableIterableStream<>(Arrays.asList("one"), () -> {
            throw closeFailure;
        });

        assertSame(closeFailure, assertThrows(IllegalStateException.class, stream::close));
    }

    @Test
    public void closingJavaStreamPropagatesRuntimeException() {
        IllegalStateException closeFailure = new IllegalStateException("close failed");
        CloseableIterableStream<String> iterableStream = new CloseableIterableStream<>(Arrays.asList("one"), () -> {
            throw closeFailure;
        });

        assertSame(closeFailure, assertThrows(IllegalStateException.class, iterableStream.stream()::close));
    }
}
