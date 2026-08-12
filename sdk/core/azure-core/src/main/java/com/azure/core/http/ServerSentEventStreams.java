// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.rest.Response;
import com.azure.core.implementation.util.ServerSentEventStream;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Flux;

import java.util.function.BiFunction;

/**
 * Consumes a single HTTP response as a server-sent event stream.
 */
public final class ServerSentEventStreams {
    private ServerSentEventStreams() {
    }

    /**
     * Decodes a single server-sent event response as a {@link Flux}.
     *
     * <p>The response body is validated as {@code text/event-stream}, decoded incrementally, and closed on
     * completion, failure, or cancellation. A 204 response produces an empty {@link Flux}.</p>
     *
     * @param response The streaming response.
     * @param converter Converts an event name and data payload into the generated event type.
     * @param <T> The type of the event data.
     * @return A flux of decoded server-sent events.
     */
    public static <T> Flux<ServerSentEvent<T>> toFlux(Response<BinaryData> response,
        BiFunction<String, String, T> converter) {
        return ServerSentEventStream.toFlux(response, converter);
    }

    /**
     * Decodes a single server-sent event response and invokes a listener for each event.
     *
     * <p>The response body is validated as {@code text/event-stream}, decoded incrementally, and closed on EOF,
     * failure, or interruption. A 204 response completes without events.</p>
     *
     * @param response The streaming response.
     * @param converter Converts an event name and data payload into the generated event type.
     * @param listener The listener that receives decoded events and lifecycle notifications.
     * @param <T> The type of the event data.
     */
    public static <T> void listen(Response<BinaryData> response, BiFunction<String, String, T> converter,
        ServerSentEventListener<T> listener) {
        ServerSentEventStream.listen(response, converter, listener);
    }
}
