// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.http.ServerSentEvent;
import com.azure.core.http.ServerSentEventListener;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.FluxInputStream;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Implementation support for parsing one server-sent event response.
 *
 * <p>Response-based methods require a {@link Response} that implements {@link java.io.Closeable}. They reject
 * non-closeable responses with {@link IllegalArgumentException}, as stream cleanup cannot otherwise be guaranteed.</p>
 */
public final class ServerSentEventStream {
    private static final String DEFAULT_EVENT = "message";

    private ServerSentEventStream() {
    }

    /**
     * Decodes one server-sent event response body.
     *
     * @param body The response body.
     * @param deserializer The event data deserializer.
     * @param <T> The event data type.
     * @return The decoded events.
     */
    public static <T> Flux<ServerSentEvent<T>> decode(BinaryData body, BiFunction<String, String, T> deserializer) {
        Objects.requireNonNull(body, "'body' cannot be null.");
        Objects.requireNonNull(deserializer, "'deserializer' cannot be null.");
        return Flux.defer(() -> decodeBody(body, new StreamState(), deserializer));
    }

    /**
     * Processes one server-sent event response body.
     *
     * @param body The response body.
     * @param deserializer The event data deserializer.
     * @param listener The event listener.
     * @param <T> The event data type.
     */
    public static <T> void process(BinaryData body, BiFunction<String, String, T> deserializer,
        ServerSentEventListener<T> listener) {
        Objects.requireNonNull(body, "'body' cannot be null.");
        Objects.requireNonNull(deserializer, "'deserializer' cannot be null.");
        Objects.requireNonNull(listener, "'listener' cannot be null.");

        try {
            processBody(body, new StreamState(), deserializer, TerminalEventPolicy.endOnResponseCompletion(), listener);
        } catch (IOException exception) {
            listener.onError(exception);
            throw new UncheckedIOException(exception);
        } catch (RuntimeException exception) {
            listener.onError(exception);
            throw exception;
        } finally {
            listener.onClose();
        }
    }

    /**
     * Decodes an SSE response, closing its physical response on completion, failure, or cancellation.
     *
     * @throws IllegalArgumentException If {@code response} does not implement {@link java.io.Closeable}.
     */
    public static <T> Flux<ServerSentEvent<T>> toFlux(Response<BinaryData> response,
        BiFunction<String, String, T> deserializer) {
        Objects.requireNonNull(response, "'response' cannot be null.");
        Objects.requireNonNull(deserializer, "'deserializer' cannot be null.");

        return toFlux(response, deserializer, TerminalEventPolicy.endOnResponseCompletion());
    }

    /**
     * Decodes an SSE response until an inclusive terminal event is emitted, closing its physical response.
     *
     * @throws IllegalArgumentException If {@code response} does not implement {@link java.io.Closeable}.
     */
    public static <T> Flux<ServerSentEvent<T>> toFlux(Response<BinaryData> response,
        BiFunction<String, String, T> deserializer, Predicate<ServerSentEvent<T>> terminalEvent) {
        Objects.requireNonNull(response, "'response' cannot be null.");
        Objects.requireNonNull(deserializer, "'deserializer' cannot be null.");
        Objects.requireNonNull(terminalEvent, "'terminalEvent' cannot be null.");

        return toFlux(response, deserializer, TerminalEventPolicy.requireTerminal(terminalEvent));
    }

    private static <T> Flux<ServerSentEvent<T>> toFlux(Response<BinaryData> response,
        BiFunction<String, String, T> deserializer, TerminalEventPolicy<T> terminalPolicy) {
        AtomicBoolean subscribed = new AtomicBoolean();
        return Flux.defer(() -> {
            if (!subscribed.compareAndSet(false, true)) {
                return Flux
                    .error(new IllegalStateException("This server-sent event stream supports only one subscription."));
            }

            ServerSentEventStreamResponse streamResponse = ServerSentEventStreamResponse.fromResponse(response);
            AtomicBoolean terminalObserved = new AtomicBoolean();
            Flux<ServerSentEvent<T>> events = streamResponse.getStatusCode() == 204
                ? Flux.empty()
                : decodeBody(streamResponse.getBody(), new StreamState(), deserializer);
            if (terminalPolicy.hasTerminalPredicate()) {
                events = events.takeUntil(event -> {
                    boolean terminal = terminalPolicy.isTerminal(event);
                    if (terminal) {
                        terminalObserved.set(true);
                    }
                    return terminal;
                });
            }

            Flux<ServerSentEvent<T>> decodedEvents = events;
            return Flux.using(() -> streamResponse,
                ignored -> decodedEvents
                    .concatWith(Mono.fromRunnable(() -> terminalPolicy.validateCompletion(terminalObserved.get()))),
                ServerSentEventStreamResponse::close, true);
        });
    }

    /**
     * Processes an SSE response, closing its physical response on completion, failure, or interruption.
     *
     * @throws IllegalArgumentException If {@code response} does not implement {@link java.io.Closeable}.
     */
    public static <T> void listen(Response<BinaryData> response, BiFunction<String, String, T> deserializer,
        ServerSentEventListener<T> listener) {
        Objects.requireNonNull(response, "'response' cannot be null.");
        Objects.requireNonNull(deserializer, "'deserializer' cannot be null.");
        Objects.requireNonNull(listener, "'listener' cannot be null.");

        try (ServerSentEventStreamResponse streamResponse = ServerSentEventStreamResponse.fromResponse(response)) {
            boolean terminalObserved = streamResponse.getStatusCode() != 204
                && processBody(streamResponse.getBody(), new StreamState(), deserializer,
                    TerminalEventPolicy.endOnResponseCompletion(), listener);
            TerminalEventPolicy.<T>endOnResponseCompletion().validateCompletion(terminalObserved);
        } catch (IOException exception) {
            listener.onError(exception);
            throw new UncheckedIOException(exception);
        } catch (RuntimeException exception) {
            listener.onError(exception);
            throw exception;
        } finally {
            listener.onClose();
        }
    }

    /**
     * Processes an SSE response until an inclusive terminal event is delivered, closing its physical response.
     *
     * @throws IllegalArgumentException If {@code response} does not implement {@link java.io.Closeable}.
     */
    public static <T> void listen(Response<BinaryData> response, BiFunction<String, String, T> deserializer,
        Predicate<ServerSentEvent<T>> terminalEvent, ServerSentEventListener<T> listener) {
        Objects.requireNonNull(response, "'response' cannot be null.");
        Objects.requireNonNull(deserializer, "'deserializer' cannot be null.");
        Objects.requireNonNull(terminalEvent, "'terminalEvent' cannot be null.");
        Objects.requireNonNull(listener, "'listener' cannot be null.");

        try (ServerSentEventStreamResponse streamResponse = ServerSentEventStreamResponse.fromResponse(response)) {
            TerminalEventPolicy<T> terminalPolicy = TerminalEventPolicy.requireTerminal(terminalEvent);
            boolean terminalObserved = streamResponse.getStatusCode() != 204
                && processBody(streamResponse.getBody(), new StreamState(), deserializer, terminalPolicy, listener);
            terminalPolicy.validateCompletion(terminalObserved);
        } catch (IOException exception) {
            listener.onError(exception);
            throw new UncheckedIOException(exception);
        } catch (RuntimeException exception) {
            listener.onError(exception);
            throw exception;
        } finally {
            listener.onClose();
        }
    }

    private static <T> Flux<ServerSentEvent<T>> decodeBody(BinaryData body, StreamState state,
        BiFunction<String, String, T> deserializer) {
        ServerSentEventDecoder decoder = new ServerSentEventDecoder(state);
        Flux<ServerSentEventFrame> frames = body.toFluxByteBuffer()
            .concatMap(buffer -> Flux.fromIterable(decoder.feed(buffer)), 1)
            .concatWith(Flux.defer(() -> Flux.fromIterable(decoder.finish())));
        return frames.concatMap(frame -> deserializeFrame(frame, deserializer), 1);
    }

    private static <T> Flux<ServerSentEvent<T>> deserializeFrame(ServerSentEventFrame frame,
        BiFunction<String, String, T> deserializer) {
        T data = deserializer.apply(frame.event, frame.data);
        return data == null ? Flux.empty() : Flux.just(frame.toEvent(data));
    }

    private static <T> boolean processBody(BinaryData body, StreamState state,
        BiFunction<String, String, T> deserializer, TerminalEventPolicy<T> terminalPolicy,
        ServerSentEventListener<T> listener) throws IOException {
        ServerSentEventDecoder decoder = new ServerSentEventDecoder(state);
        byte[] readBuffer = new byte[8192];

        try (InputStream stream = new FluxInputStream(body.toFluxByteBuffer())) {
            int read;
            while (true) {
                checkInterrupted();
                read = stream.read(readBuffer);
                if (read == -1) {
                    return processFrames(decoder.finish(), deserializer, terminalPolicy, listener);
                }
                if (read > 0
                    && processFrames(decoder.feed(ByteBuffer.wrap(readBuffer, 0, read)), deserializer, terminalPolicy,
                        listener)) {
                    return true;
                }
            }
        }
    }

    private static <T> boolean processFrames(List<ServerSentEventFrame> frames,
        BiFunction<String, String, T> deserializer, TerminalEventPolicy<T> terminalPolicy,
        ServerSentEventListener<T> listener) {
        for (ServerSentEventFrame frame : frames) {
            checkInterrupted();
            T data = deserializer.apply(frame.event, frame.data);
            if (data != null) {
                ServerSentEvent<T> event = frame.toEvent(data);
                listener.onEvent(event);
                if (terminalPolicy.isTerminal(event)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final class TerminalEventPolicy<T> {
        private final Predicate<ServerSentEvent<T>> terminalPredicate;

        private TerminalEventPolicy(Predicate<ServerSentEvent<T>> terminalPredicate) {
            this.terminalPredicate = terminalPredicate;
        }

        private boolean hasTerminalPredicate() {
            return terminalPredicate != null;
        }

        private boolean isTerminal(ServerSentEvent<T> event) {
            return terminalPredicate != null && terminalPredicate.test(event);
        }

        private void validateCompletion(boolean terminalObserved) {
            if (terminalPredicate != null && !terminalObserved) {
                throw new IllegalStateException("The server-sent event stream ended before a terminal event.");
            }
        }

        private static <T> TerminalEventPolicy<T> endOnResponseCompletion() {
            return new TerminalEventPolicy<>(null);
        }

        private static <T> TerminalEventPolicy<T> requireTerminal(Predicate<ServerSentEvent<T>> terminalPredicate) {
            return new TerminalEventPolicy<>(terminalPredicate);
        }
    }

    private static void checkInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            throw new RuntimeException("Interrupted while processing the server-sent event stream.",
                new InterruptedException());
        }
    }

    private static String removeOptionalSpace(String value) {
        return value.startsWith(" ") ? value.substring(1) : value;
    }

    private static Duration parseRetryAfter(String value) {
        if (value.isEmpty()) {
            return null;
        }

        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character < '0' || character > '9') {
                return null;
            }
        }

        try {
            return Duration.ofMillis(Long.parseLong(value));
        } catch (NumberFormatException ignored) {
            // Ignore retry values that don't fit in a long.
            return null;
        }
    }

    private static final class StreamState {
        private String lastEventId;
        private Duration retryAfter;

        private void setLastEventId(String lastEventId) {
            this.lastEventId = lastEventId;
        }

        private void setRetryAfter(Duration retryAfter) {
            this.retryAfter = retryAfter;
        }

    }

    private static final class ServerSentEventDecoder {
        private final StreamState state;
        private byte[] lineBytes = new byte[256];
        private int lineLength;
        private boolean pendingCarriageReturn;
        private boolean firstLine = true;
        private String event;
        private List<String> data;
        private String comment;

        private ServerSentEventDecoder(StreamState state) {
            this.state = state;
        }

        private List<ServerSentEventFrame> feed(ByteBuffer source) {
            ByteBuffer buffer = source.duplicate();
            List<ServerSentEventFrame> events = new ArrayList<>();

            while (buffer.hasRemaining()) {
                byte value = buffer.get();

                if (pendingCarriageReturn) {
                    pendingCarriageReturn = false;
                    if (value == '\n') {
                        continue;
                    }
                }

                if (value == '\n') {
                    processLine(decodeLine(), events);
                } else if (value == '\r') {
                    processLine(decodeLine(), events);
                    pendingCarriageReturn = true;
                } else {
                    appendByte(value);
                }
            }

            return events;
        }

        private List<ServerSentEventFrame> finish() {
            // The SSE parsing algorithm discards an event that wasn't terminated by a blank line.
            return Collections.emptyList();
        }

        private void appendByte(byte value) {
            if (lineLength == lineBytes.length) {
                lineBytes = Arrays.copyOf(lineBytes, lineBytes.length * 2);
            }
            lineBytes[lineLength++] = value;
        }

        private String decodeLine() {
            String line = new String(lineBytes, 0, lineLength, StandardCharsets.UTF_8);
            lineLength = 0;

            if (firstLine) {
                firstLine = false;
                if (!line.isEmpty() && line.charAt(0) == '\uFEFF') {
                    return line.substring(1);
                }
            }

            return line;
        }

        private void processLine(String line, List<ServerSentEventFrame> events) {
            if (line.isEmpty()) {
                ServerSentEventFrame parsedEvent = buildEvent();
                if (parsedEvent != null) {
                    events.add(parsedEvent);
                }
                return;
            }

            if (line.charAt(0) == ':') {
                comment = removeOptionalSpace(line.substring(1));
                return;
            }

            int colonIndex = line.indexOf(':');
            String field = colonIndex < 0 ? line : line.substring(0, colonIndex);
            String value = colonIndex < 0 ? "" : removeOptionalSpace(line.substring(colonIndex + 1));

            switch (field) {
                case "event":
                    event = value;
                    break;

                case "data":
                    if (data == null) {
                        data = new ArrayList<>();
                    }
                    data.add(value);
                    break;

                case "id":
                    if (value.indexOf('\0') < 0) {
                        state.setLastEventId(value);
                    }
                    break;

                case "retry":
                    Duration parsedRetryAfter = parseRetryAfter(value);
                    if (parsedRetryAfter != null) {
                        state.setRetryAfter(parsedRetryAfter);
                    }
                    break;

                default:
                    break;
            }
        }

        private ServerSentEventFrame buildEvent() {
            String currentEvent = event;
            List<String> currentData = data;
            String currentComment = comment;
            resetEvent();

            if (currentData == null) {
                return null;
            }

            if (currentEvent == null || currentEvent.isEmpty()) {
                currentEvent = DEFAULT_EVENT;
            }

            return new ServerSentEventFrame(state.lastEventId, currentEvent, String.join("\n", currentData),
                currentComment, state.retryAfter);
        }

        private void resetEvent() {
            event = null;
            data = null;
            comment = null;
        }
    }

    private static final class ServerSentEventFrame {
        private final String id;
        private final String event;
        private final String data;
        private final String comment;
        private final Duration retryAfter;

        private ServerSentEventFrame(String id, String event, String data, String comment, Duration retryAfter) {
            this.id = id;
            this.event = event;
            this.data = data;
            this.comment = comment;
            this.retryAfter = retryAfter;
        }

        private <T> ServerSentEvent<T> toEvent(T data) {
            return ServerSentEventHelper.create(id, event, data, comment, retryAfter);
        }
    }
}
