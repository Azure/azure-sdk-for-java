// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models.implementation.sse;

import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.search.documents.models.ServerSentEvent;
import com.azure.search.documents.models.ServerSentEventListener;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import reactor.core.publisher.Flux;

/**
 * Consumes a single HTTP response as a server-sent event stream.
 */
public final class ServerSentEventStreams {
    private ServerSentEventStreams() {
    }

    /**
     * Decodes one response until the response body ends.
     *
     * @param response The streaming response.
     * @param converter Converts an event name and data payload into the event data type.
     * @param <T> The event data type.
     * @return A flux of decoded server-sent events.
     */
    public static <T> Flux<ServerSentEvent<T>> toFlux(Response<BinaryData> response,
        BiFunction<String, String, T> converter) {
        return ServerSentEventStream.toFlux(response, converter);
    }

    /**
     * Decodes one response until an inclusive terminal event is emitted.
     *
     * @param response The streaming response.
     * @param converter Converts an event name and data payload into the event data type.
     * @param terminalEvent Identifies the inclusive terminal event.
     * @param <T> The event data type.
     * @return A flux of decoded server-sent events.
     */
    public static <T> Flux<ServerSentEvent<T>> toFlux(Response<BinaryData> response,
        BiFunction<String, String, T> converter, Predicate<ServerSentEvent<T>> terminalEvent) {
        return ServerSentEventStream.toFlux(response, converter, terminalEvent);
    }

    /**
     * Decodes one response and delivers events to a listener until the response body ends.
     *
     * @param response The streaming response.
     * @param converter Converts an event name and data payload into the event data type.
     * @param listener The listener that receives events and lifecycle notifications.
     * @param <T> The event data type.
     */
    public static <T> void listen(Response<BinaryData> response, BiFunction<String, String, T> converter,
        ServerSentEventListener<T> listener) {
        ServerSentEventStream.listen(response, converter, listener);
    }

    /**
     * Decodes one response until an inclusive terminal event is delivered to a listener.
     *
     * @param response The streaming response.
     * @param converter Converts an event name and data payload into the event data type.
     * @param terminalEvent Identifies the inclusive terminal event.
     * @param listener The listener that receives events and lifecycle notifications.
     * @param <T> The event data type.
     */
    public static <T> void listen(Response<BinaryData> response, BiFunction<String, String, T> converter,
        Predicate<ServerSentEvent<T>> terminalEvent, ServerSentEventListener<T> listener) {
        ServerSentEventStream.listen(response, converter, terminalEvent, listener);
    }
}
