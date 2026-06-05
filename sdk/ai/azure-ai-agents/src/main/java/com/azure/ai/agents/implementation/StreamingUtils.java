// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.openai.core.http.AsyncStreamResponse;
import com.openai.core.http.StreamResponse;
import reactor.core.publisher.Flux;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility methods for bridging OpenAI streaming types to Azure SDK / Reactor types.
 */
public final class StreamingUtils {

    private static final ClientLogger LOGGER = new ClientLogger(StreamingUtils.class);

    private StreamingUtils() {
    }

    /**
     * Converts an OpenAI {@link StreamResponse} into an {@link IterableStream} that automatically
     * closes the underlying stream resource when all events have been consumed.
     *
     * <p>The returned {@link IterableStream} is single-use: calling {@code iterator()} or
     * {@code stream()} more than once will throw {@link IllegalStateException}, because the
     * underlying {@link StreamResponse} can only be consumed once.</p>
     *
     * @param streamResponse The OpenAI stream response.
     * @param <T> The type of the streamed items.
     * @return An IterableStream wrapping the given StreamResponse.
     */
    public static <T> IterableStream<T> toIterableStream(StreamResponse<T> streamResponse) {
        Iterator<T> inner = streamResponse.stream().iterator();
        AtomicBoolean iteratorCreated = new AtomicBoolean(false);

        Iterable<T> singleUseIterable = () -> {
            if (!iteratorCreated.compareAndSet(false, true)) {
                throw LOGGER
                    .logExceptionAsError(new IllegalStateException("This streaming response has already been consumed. "
                        + "A streaming IterableStream can only be iterated once."));
            }
            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    boolean hasNext = inner.hasNext();
                    if (!hasNext) {
                        streamResponse.close();
                    }
                    return hasNext;
                }

                @Override
                public T next() {
                    if (!inner.hasNext()) {
                        throw LOGGER.logExceptionAsError(new NoSuchElementException());
                    }
                    return inner.next();
                }
            };
        };
        return new IterableStream<>(singleUseIterable);
    }

    /**
     * Bridges an OpenAI {@link AsyncStreamResponse} into a Reactor {@link Flux}, forwarding
     * events, errors, and completion signals while closing the underlying stream on disposal.
     *
     * <p>Thread safety: {@code Flux.create} defaults to {@code PUSH_PULL} mode, which wraps the
     * sink in a {@code SerializedFluxSink} that serializes {@code next/error/complete} calls.
     * Additionally, the OpenAI {@code AsyncStreamResponse} implementation invokes the handler
     * sequentially on a single executor thread (via {@code Stream.forEach}), so concurrent
     * emission cannot occur in practice.</p>
     *
     * @param asyncStream The OpenAI async stream response.
     * @param <T> The type of the streamed items.
     * @return A Flux wrapping the given AsyncStreamResponse.
     */
    public static <T> Flux<T> toFlux(AsyncStreamResponse<T> asyncStream) {
        return Flux.create(sink -> {
            asyncStream.subscribe(new AsyncStreamResponse.Handler<T>() {
                @Override
                public void onNext(T event) {
                    sink.next(event);
                }

                @Override
                public void onComplete(Optional<Throwable> error) {
                    if (error.isPresent()) {
                        sink.error(error.get());
                    } else {
                        sink.complete();
                    }
                }
            });
            sink.onDispose(asyncStream::close);
        });
    }
}
