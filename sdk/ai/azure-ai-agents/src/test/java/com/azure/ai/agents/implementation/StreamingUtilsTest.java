// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.core.util.IterableStream;
import com.openai.core.http.AsyncStreamResponse;
import com.openai.core.http.StreamResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link StreamingUtils}.
 */
public class StreamingUtilsTest {

    // ========================================================================
    // toIterableStream tests
    // ========================================================================

    @Test
    public void toIterableStreamSingleItem() {
        AtomicBoolean closed = new AtomicBoolean(false);
        StreamResponse<String> streamResponse = testStreamResponse(Stream.of("hello"), closed);

        IterableStream<String> result = StreamingUtils.toIterableStream(streamResponse);

        List<String> items = collect(result);
        assertEquals(1, items.size());
        assertEquals("hello", items.get(0));
        assertTrue(closed.get(), "StreamResponse should be closed after full consumption");
    }

    @Test
    public void toIterableStreamMultipleItems() {
        AtomicBoolean closed = new AtomicBoolean(false);
        StreamResponse<String> streamResponse = testStreamResponse(Stream.of("event1", "event2", "event3"), closed);

        IterableStream<String> result = StreamingUtils.toIterableStream(streamResponse);

        List<String> items = collect(result);
        assertEquals(3, items.size());
        assertEquals("event1", items.get(0));
        assertEquals("event2", items.get(1));
        assertEquals("event3", items.get(2));
        assertTrue(closed.get());
    }

    @Test
    public void toIterableStreamEmptyClosesResource() {
        AtomicBoolean closed = new AtomicBoolean(false);
        StreamResponse<String> streamResponse = testStreamResponse(Stream.empty(), closed);

        IterableStream<String> result = StreamingUtils.toIterableStream(streamResponse);

        List<String> items = collect(result);
        assertTrue(items.isEmpty());
        assertTrue(closed.get(), "StreamResponse should be closed even when stream is empty");
    }

    @Test
    public void toIterableStreamPreservesOrder() {
        AtomicBoolean closed = new AtomicBoolean(false);
        List<String> expected = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            expected.add("item-" + i);
        }
        StreamResponse<String> streamResponse = testStreamResponse(expected.stream(), closed);

        IterableStream<String> result = StreamingUtils.toIterableStream(streamResponse);

        List<String> items = collect(result);
        assertEquals(expected, items);
        assertTrue(closed.get());
    }

    @Test
    public void toIterableStreamNotClosedBeforeFullConsumption() {
        AtomicBoolean closed = new AtomicBoolean(false);
        StreamResponse<String> streamResponse = testStreamResponse(Stream.of("a", "b", "c"), closed);

        IterableStream<String> result = StreamingUtils.toIterableStream(streamResponse);
        Iterator<String> iterator = result.iterator();

        // Consume only one item
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(closed.get(), "StreamResponse should not be closed before full consumption");

        // Consume the rest
        assertEquals("b", iterator.next());
        assertEquals("c", iterator.next());
        assertFalse(iterator.hasNext());
        assertTrue(closed.get());
    }

    @Test
    public void toIterableStreamIteratorThrowsWhenExhausted() {
        AtomicBoolean closed = new AtomicBoolean(false);
        StreamResponse<String> streamResponse = testStreamResponse(Stream.of("only"), closed);

        IterableStream<String> result = StreamingUtils.toIterableStream(streamResponse);
        Iterator<String> iterator = result.iterator();

        assertEquals("only", iterator.next());
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void toIterableStreamThrowsOnSecondIteratorCall() {
        AtomicBoolean closed = new AtomicBoolean(false);
        StreamResponse<String> streamResponse = testStreamResponse(Stream.of("a", "b"), closed);

        IterableStream<String> result = StreamingUtils.toIterableStream(streamResponse);

        // First iterator() call should succeed
        Iterator<String> first = result.iterator();
        assertEquals("a", first.next());

        // Second iterator() call should throw
        assertThrows(IllegalStateException.class, result::iterator);
    }

    @Test
    public void toIterableStreamSecondIteratorCallAfterFullConsumptionThrows() {
        AtomicBoolean closed = new AtomicBoolean(false);
        StreamResponse<String> streamResponse = testStreamResponse(Stream.of("a", "b"), closed);

        IterableStream<String> result = StreamingUtils.toIterableStream(streamResponse);

        // Fully consume the first iterator
        List<String> items = collect(result);
        assertEquals(Arrays.asList("a", "b"), items);
        assertTrue(closed.get());

        // Without the single-use guard, this would silently return an empty iterator
        // instead of failing. The guard converts that silent bug into a clear error.
        assertThrows(IllegalStateException.class, result::iterator);
    }

    // ========================================================================
    // toFlux tests
    // ========================================================================

    @Test
    public void toFluxSingleItem() {
        TestAsyncStreamResponse<String> asyncStream = new TestAsyncStreamResponse<>();

        Flux<String> flux = StreamingUtils.toFlux(asyncStream);

        StepVerifier.create(flux).then(() -> {
            asyncStream.emit("hello");
            asyncStream.complete();
        }).expectNext("hello").verifyComplete();
    }

    @Test
    public void toFluxMultipleItems() {
        TestAsyncStreamResponse<String> asyncStream = new TestAsyncStreamResponse<>();

        Flux<String> flux = StreamingUtils.toFlux(asyncStream);

        StepVerifier.create(flux).then(() -> {
            asyncStream.emit("event1");
            asyncStream.emit("event2");
            asyncStream.emit("event3");
            asyncStream.complete();
        }).expectNext("event1").expectNext("event2").expectNext("event3").verifyComplete();
    }

    @Test
    public void toFluxEmptyStream() {
        TestAsyncStreamResponse<String> asyncStream = new TestAsyncStreamResponse<>();

        Flux<String> flux = StreamingUtils.toFlux(asyncStream);

        StepVerifier.create(flux).then(asyncStream::complete).verifyComplete();
    }

    @Test
    public void toFluxPropagatesError() {
        TestAsyncStreamResponse<String> asyncStream = new TestAsyncStreamResponse<>();
        RuntimeException expectedError = new RuntimeException("stream failed");

        Flux<String> flux = StreamingUtils.toFlux(asyncStream);

        StepVerifier.create(flux).then(() -> {
            asyncStream.emit("before-error");
            asyncStream.completeWithError(expectedError);
        })
            .expectNext("before-error")
            .expectErrorMatches(e -> e instanceof RuntimeException && "stream failed".equals(e.getMessage()))
            .verify();
    }

    @Test
    public void toFluxErrorWithNoEmissions() {
        TestAsyncStreamResponse<String> asyncStream = new TestAsyncStreamResponse<>();
        RuntimeException expectedError = new RuntimeException("immediate failure");

        Flux<String> flux = StreamingUtils.toFlux(asyncStream);

        StepVerifier.create(flux)
            .then(() -> asyncStream.completeWithError(expectedError))
            .expectErrorMatches(e -> e instanceof RuntimeException && "immediate failure".equals(e.getMessage()))
            .verify();
    }

    @Test
    public void toFluxPreservesOrder() {
        TestAsyncStreamResponse<String> asyncStream = new TestAsyncStreamResponse<>();
        List<String> expected = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            expected.add("item-" + i);
        }

        Flux<String> flux = StreamingUtils.toFlux(asyncStream);

        StepVerifier.create(flux).then(() -> {
            for (String item : expected) {
                asyncStream.emit(item);
            }
            asyncStream.complete();
        })
            .recordWith(ArrayList::new)
            .thenConsumeWhile(x -> true)
            .consumeRecordedWith(recorded -> assertEquals(expected, new ArrayList<>(recorded)))
            .verifyComplete();
    }

    @Test
    public void toFluxClosesOnDispose() {
        TestAsyncStreamResponse<String> asyncStream = new TestAsyncStreamResponse<>();

        Flux<String> flux = StreamingUtils.toFlux(asyncStream);

        // Subscribe and immediately dispose
        flux.subscribe().dispose();
        assertTrue(asyncStream.isClosed(), "AsyncStreamResponse should be closed on disposal");
    }

    // ========================================================================
    // Test helpers
    // ========================================================================

    /**
     * Creates a simple StreamResponse backed by the given stream, tracking close state.
     */
    private static <T> StreamResponse<T> testStreamResponse(Stream<T> stream, AtomicBoolean closed) {
        return new StreamResponse<T>() {
            @Override
            public Stream<T> stream() {
                return stream;
            }

            @Override
            public void close() {
                closed.set(true);
            }
        };
    }

    /**
     * Collects all items from an IterableStream into a list.
     */
    private static <T> List<T> collect(IterableStream<T> iterableStream) {
        List<T> items = new ArrayList<>();
        for (T item : iterableStream) {
            items.add(item);
        }
        return items;
    }

    /**
     * A test implementation of AsyncStreamResponse that allows manual emission of items
     * and completion/error signals. Executes the handler synchronously for test simplicity.
     */
    private static class TestAsyncStreamResponse<T> implements AsyncStreamResponse<T> {
        private Handler<? super T> handler;
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final CompletableFuture<Void> completeFuture = new CompletableFuture<>();

        void emit(T item) {
            if (handler != null) {
                handler.onNext(item);
            }
        }

        void complete() {
            if (handler != null) {
                handler.onComplete(Optional.empty());
            }
            completeFuture.complete(null);
        }

        void completeWithError(Throwable error) {
            if (handler != null) {
                handler.onComplete(Optional.of(error));
            }
            completeFuture.completeExceptionally(error);
        }

        boolean isClosed() {
            return closed.get();
        }

        @Override
        public AsyncStreamResponse<T> subscribe(Handler<? super T> handler) {
            this.handler = handler;
            return this;
        }

        @Override
        public AsyncStreamResponse<T> subscribe(Handler<? super T> handler, Executor executor) {
            this.handler = handler;
            return this;
        }

        @Override
        public CompletableFuture<Void> onCompleteFuture() {
            return completeFuture;
        }

        @Override
        public void close() {
            closed.set(true);
        }
    }
}
