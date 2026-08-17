// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.rest.Response;
import com.azure.core.implementation.util.ServerSentEventStream;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Flux;

import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Consumes a single HTTP response as a server-sent event stream.
 *
 * <p>The supplied response must implement {@link java.io.Closeable}. The stream closes the physical response when
 * consumption ends; a response that is not closeable is rejected with {@link IllegalArgumentException}.</p>
 *
 * <p>A returned {@link Flux} consumes one supplied physical response and supports exactly one subscription. Before
 * that subscription claims the response, ownership remains with the caller; if it is never subscribed, the caller
 * must close the response.</p>
 *
 * <p>If both stream processing and eager response cleanup fail, the cleanup failure is emitted and the processing
 * failure is suppressed.</p>
 *
 * <p>The stream incrementally decodes a supported {@code charset} declared by its {@code Content-Type}; UTF-8 is
 * used when no charset is declared, and an initial byte-order mark takes precedence over the declaration.</p>
 */
public final class ServerSentEventStreams {
    private ServerSentEventStreams() {
    }

    /**
     * Decodes a single server-sent event response as a {@link Flux}.
     *
     * <p>The response body is validated as {@code text/event-stream}, decoded incrementally, and closed on
     * completion, failure, or cancellation. A 204 response produces an empty {@link Flux}. Only HTTP 200 and 204
     * responses are accepted.</p>
     *
     * @param response The streaming response.
     * @param converter Converts an event name and data payload into the generated event type.
     * @param <T> The type of the event data.
     * @return A flux of decoded server-sent events.
     * @throws IllegalArgumentException If {@code response} does not implement {@link java.io.Closeable}.
     */
    public static <T> Flux<ServerSentEvent<T>> toFlux(Response<BinaryData> response,
        BiFunction<String, String, T> converter) {
        return ServerSentEventStream.toFlux(response, converter);
    }

    /**
     * Decodes a single server-sent event response as a {@link Flux} until an inclusive terminal event is emitted.
     *
     * <p>The response body is validated as {@code text/event-stream}, decoded incrementally, and closed after a
     * terminal event, on failure, or on cancellation. A 204 response does not evaluate the predicate and fails
     * because it cannot satisfy the terminal-event requirement. If the response body ends before a terminal event is
     * emitted, the flux fails. This method does not reconnect or replay a request.</p>
     *
     * @param response The streaming response.
     * @param converter Converts an event name and data payload into the generated event type.
     * @param terminalEvent Identifies the inclusive terminal event.
     * @param <T> The type of the event data.
     * @return A flux of decoded server-sent events.
     * @throws IllegalArgumentException If {@code response} does not implement {@link java.io.Closeable}.
     */
    public static <T> Flux<ServerSentEvent<T>> toFlux(Response<BinaryData> response,
        BiFunction<String, String, T> converter, Predicate<ServerSentEvent<T>> terminalEvent) {
        return ServerSentEventStream.toFlux(response, converter, terminalEvent);
    }

    /**
     * Decodes a single server-sent event response and invokes a listener for each event.
     *
     * <p>The response body is validated as {@code text/event-stream}, decoded incrementally, and closed on EOF,
     * failure, or interruption. A 204 response completes without events. Only HTTP 200 and 204 responses are
     * accepted.</p>
     *
     * @param response The streaming response.
     * @param converter Converts an event name and data payload into the generated event type.
     * @param listener The listener that receives decoded events and lifecycle notifications.
     * @param <T> The type of the event data.
     * @throws IllegalArgumentException If {@code response} does not implement {@link java.io.Closeable}.
     */
    public static <T> void listen(Response<BinaryData> response, BiFunction<String, String, T> converter,
        ServerSentEventListener<T> listener) {
        ServerSentEventStream.listen(response, converter, listener);
    }

    /**
     * Decodes a single server-sent event response until an inclusive terminal event is delivered to a listener.
     *
     * <p>The response body is validated as {@code text/event-stream}, decoded incrementally, and closed after a
     * terminal event, on failure, or on interruption. A 204 response does not evaluate the predicate and fails
     * because it cannot satisfy the terminal-event requirement. If the response body ends before a terminal event is
     * delivered, this method fails. This method does not reconnect or replay a request.</p>
     *
     * @param response The streaming response.
     * @param converter Converts an event name and data payload into the generated event type.
     * @param terminalEvent Identifies the inclusive terminal event.
     * @param listener The listener that receives decoded events and lifecycle notifications.
     * @param <T> The type of the event data.
     * @throws IllegalArgumentException If {@code response} does not implement {@link java.io.Closeable}.
     */
    public static <T> void listen(Response<BinaryData> response, BiFunction<String, String, T> converter,
        Predicate<ServerSentEvent<T>> terminalEvent, ServerSentEventListener<T> listener) {
        ServerSentEventStream.listen(response, converter, terminalEvent, listener);
    }
}
