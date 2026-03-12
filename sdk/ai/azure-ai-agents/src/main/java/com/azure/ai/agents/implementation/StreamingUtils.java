// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.core.util.IterableStream;
import com.openai.core.http.AsyncStreamResponse;
import com.openai.core.http.StreamResponse;
import reactor.core.publisher.Flux;

import java.util.Iterator;
import java.util.Optional;

/**
 * Utility methods for bridging OpenAI streaming types to Azure SDK / Reactor types.
 */
public final class StreamingUtils {

    private StreamingUtils() {
    }

    /**
     * Converts an OpenAI {@link StreamResponse} into an {@link IterableStream} that automatically
     * closes the underlying stream resource when all events have been consumed.
     *
     * @param streamResponse The OpenAI stream response.
     * @param <T> The type of the streamed items.
     * @return An IterableStream wrapping the given StreamResponse.
     */
    public static <T> IterableStream<T> toIterableStream(StreamResponse<T> streamResponse) {
        Iterator<T> inner = streamResponse.stream().iterator();
        Iterable<T> closingIterable = () -> new Iterator<T>() {
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
                return inner.next();
            }
        };
        return new IterableStream<>(closingIterable);
    }

    /**
     * Bridges an OpenAI {@link AsyncStreamResponse} into a Reactor {@link Flux}, forwarding
     * events, errors, and completion signals while closing the underlying stream on disposal.
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
