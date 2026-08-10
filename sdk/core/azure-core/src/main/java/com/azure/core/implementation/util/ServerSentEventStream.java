// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.http.ServerSentEvent;
import com.azure.core.http.ServerSentEventListener;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpRequest;
import com.azure.core.implementation.FluxInputStream;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implementation support for parsing and reconnecting server-sent event streams.
 *
 * <p>A logical stream reconnects after a response body completes or fails while being consumed when the server has
 * supplied a valid {@code retry} value. HTTP request, deserialization, and listener failures terminate the stream.
 * Reconnection continues without a client-imposed attempt limit until a terminal event, cancellation, interruption,
 * or a response that has no retry state ends the stream.</p>
 *
 * <p>Generated code must use the reconnect callback to issue a fresh, replay-safe request through the normal HTTP
 * pipeline. A {@code null} callback argument means that {@code Last-Event-Id} must be omitted.</p>
 */
public final class ServerSentEventStream {
    private static final String DEFAULT_EVENT = "message";
    private static final HttpHeaderName LAST_EVENT_ID = HttpHeaderName.fromString("Last-Event-Id");
    private static final Object RECONNECT_CONTEXT_KEY = new Object();

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
     * Decodes a logical server-sent event stream, reconnecting after response completion or a response-body failure
     * when the server has supplied a valid retry interval.
     *
     * @param response The initial response.
     * @param reconnect Creates a fresh response. Its argument is the last event identifier or {@code null} when no
     * {@code Last-Event-Id} header should be sent.
     * @param deserializer The event data deserializer.
     * @param <T> The event data type.
     * @return The decoded events.
     */
    public static <T> Flux<ServerSentEvent<T>> decode(ServerSentEventStreamResponse response,
        Function<String, Mono<ServerSentEventStreamResponse>> reconnect, BiFunction<String, String, T> deserializer) {
        Objects.requireNonNull(response, "'response' cannot be null.");
        Objects.requireNonNull(reconnect, "'reconnect' cannot be null.");
        Objects.requireNonNull(deserializer, "'deserializer' cannot be null.");

        return Flux.defer(() -> {
            StreamState state = new StreamState();
            AtomicReference<ServerSentEventStreamResponse> currentResponse = new AtomicReference<>(response);
            Flux<ServerSentEvent<T>> decodeCurrentResponse = Flux.defer(() -> {
                ServerSentEventStreamResponse responseToDecode = currentResponse.get();
                if (responseToDecode.getStatusCode() == 204) {
                    state.stopReconnecting = true;
                    responseToDecode.close();
                    return Flux.empty();
                }

                return decodeBody(responseToDecode.getBody(), state, deserializer, true)
                    .doFinally(ignored -> responseToDecode.close());
            });

            return decodeCurrentResponse
                .repeatWhen(
                    completions -> completions.takeWhile(ignored -> state.retryAfter != null && !state.stopReconnecting)
                        .concatMap(ignored -> delay(state.retryAfter)
                            .then(Mono.defer(() -> Objects.requireNonNull(reconnect.apply(state.getReconnectEventId()),
                                "'reconnect' cannot return null.")))
                            .doOnNext(currentResponse::set)))
                .doFinally(ignored -> currentResponse.get().close());
        });
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
        processInternal(body, null, deserializer, event -> false, listener);
    }

    /**
     * Processes a logical server-sent event stream, reconnecting after response completion or a response-body failure
     * when the server has supplied a valid retry interval.
     *
     * @param response The initial response.
     * @param reconnect Creates a fresh response. Its argument is the last event identifier or {@code null} when no
     * {@code Last-Event-Id} header should be sent.
     * @param deserializer The event data deserializer.
     * @param terminalEvent Identifies an inclusive terminal event.
     * @param listener The event listener.
     * @param <T> The event data type.
     */
    public static <T> void process(ServerSentEventStreamResponse response,
        Function<String, ServerSentEventStreamResponse> reconnect, BiFunction<String, String, T> deserializer,
        Predicate<ServerSentEvent<T>> terminalEvent, ServerSentEventListener<T> listener) {
        Objects.requireNonNull(reconnect, "'reconnect' cannot be null.");
        processInternal(response, reconnect, deserializer, terminalEvent, listener);
    }

    /**
     * Adds reconnect state to a request context.
     *
     * @param context The request context.
     * @param lastEventId The last event identifier, or {@code null} to omit {@code Last-Event-Id}.
     * @return The request context containing the reconnect state.
     */
    public static Context addReconnectContext(Context context, String lastEventId) {
        return Objects.requireNonNull(context, "'context' cannot be null.")
            .addData(RECONNECT_CONTEXT_KEY, new ReconnectState(lastEventId));
    }

    /**
     * Applies reconnect state after generated parameters and request options have configured the request.
     *
     * @param request The request.
     * @param context The request context.
     */
    public static void applyReconnectContext(HttpRequest request, Context context) {
        Objects.requireNonNull(request, "'request' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");

        Object value = context.getData(RECONNECT_CONTEXT_KEY).orElse(null);
        if (!(value instanceof ReconnectState)) {
            return;
        }

        String lastEventId = ((ReconnectState) value).lastEventId;
        if (lastEventId == null || lastEventId.isEmpty()) {
            request.getHeaders().remove(LAST_EVENT_ID);
        } else {
            request.getHeaders().set(LAST_EVENT_ID, lastEventId);
        }
    }

    private static <T> void processInternal(BinaryData body, Function<String, ServerSentEventStreamResponse> reconnect,
        BiFunction<String, String, T> deserializer, Predicate<ServerSentEvent<T>> terminalEvent,
        ServerSentEventListener<T> listener) {
        processInternal(new ServerSentEventStreamResponse(200, body, () -> {
        }), reconnect, deserializer, terminalEvent, listener);
    }

    private static <T> void processInternal(ServerSentEventStreamResponse response,
        Function<String, ServerSentEventStreamResponse> reconnect, BiFunction<String, String, T> deserializer,
        Predicate<ServerSentEvent<T>> terminalEvent, ServerSentEventListener<T> listener) {
        Objects.requireNonNull(response, "'response' cannot be null.");
        Objects.requireNonNull(deserializer, "'deserializer' cannot be null.");
        Objects.requireNonNull(terminalEvent, "'terminalEvent' cannot be null.");
        Objects.requireNonNull(listener, "'listener' cannot be null.");

        StreamState state = new StreamState();
        ServerSentEventStreamResponse currentResponse = response;

        try {
            while (true) {
                if (currentResponse.getStatusCode() == 204) {
                    currentResponse.close();
                    return;
                }

                boolean terminal;
                try (ServerSentEventStreamResponse responseToProcess = currentResponse) {
                    terminal = processBody(responseToProcess.getBody(), state, deserializer, terminalEvent, listener);
                } catch (IOException exception) {
                    if (reconnect == null || state.retryAfter == null) {
                        throw exception;
                    }
                    terminal = false;
                }

                if (terminal || reconnect == null || state.retryAfter == null) {
                    return;
                }

                waitForRetry(state.retryAfter);
                checkInterrupted();
                ServerSentEventStreamResponse nextResponse = Objects
                    .requireNonNull(reconnect.apply(state.getReconnectEventId()), "'reconnect' cannot return null.");
                try {
                    checkInterrupted();
                } catch (RuntimeException exception) {
                    nextResponse.close();
                    throw exception;
                }
                currentResponse = nextResponse;
            }
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
        return decodeBody(body, state, deserializer, false);
    }

    private static <T> Flux<ServerSentEvent<T>> decodeBody(BinaryData body, StreamState state,
        BiFunction<String, String, T> deserializer, boolean reconnectBodyFailures) {
        ServerSentEventDecoder decoder = new ServerSentEventDecoder(state);
        Flux<ByteBuffer> content = body.toFluxByteBuffer();
        if (reconnectBodyFailures) {
            content = content.onErrorResume(error -> state.retryAfter == null ? Flux.error(error) : Flux.empty());
        }

        Flux<ServerSentEventFrame> frames = content.concatMap(buffer -> Flux.fromIterable(decoder.feed(buffer)), 1)
            .concatWith(Flux.defer(() -> Flux.fromIterable(decoder.finish())));
        return frames.concatMap(frame -> deserializeFrame(frame, deserializer), 1);
    }

    private static <T> Flux<ServerSentEvent<T>> deserializeFrame(ServerSentEventFrame frame,
        BiFunction<String, String, T> deserializer) {
        T data = deserializer.apply(frame.event, frame.data);
        return data == null ? Flux.empty() : Flux.just(frame.toEvent(data));
    }

    private static <T> boolean processBody(BinaryData body, StreamState state,
        BiFunction<String, String, T> deserializer, Predicate<ServerSentEvent<T>> terminalEvent,
        ServerSentEventListener<T> listener) throws IOException {
        ServerSentEventDecoder decoder = new ServerSentEventDecoder(state);
        byte[] readBuffer = new byte[8192];

        try (InputStream stream = new FluxInputStream(body.toFluxByteBuffer())) {
            int read;
            while ((read = stream.read(readBuffer)) != -1) {
                if (read > 0
                    && processFrames(decoder.feed(ByteBuffer.wrap(readBuffer, 0, read)), deserializer, terminalEvent,
                        listener)) {
                    return true;
                }
            }

            return processFrames(decoder.finish(), deserializer, terminalEvent, listener);
        }
    }

    private static <T> boolean processFrames(List<ServerSentEventFrame> frames,
        BiFunction<String, String, T> deserializer, Predicate<ServerSentEvent<T>> terminalEvent,
        ServerSentEventListener<T> listener) {
        for (ServerSentEventFrame frame : frames) {
            T data = deserializer.apply(frame.event, frame.data);
            if (data != null) {
                ServerSentEvent<T> event = frame.toEvent(data);
                listener.onEvent(event);
                if (terminalEvent.test(event)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void waitForRetry(Duration retryAfter) {
        checkInterrupted();
        if (retryAfter.isZero()) {
            return;
        }

        try {
            Thread.sleep(retryAfter.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting to reconnect the server-sent event stream.",
                exception);
        }
    }

    private static void checkInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            throw new RuntimeException("Interrupted while reconnecting the server-sent event stream.",
                new InterruptedException());
        }
    }

    private static Mono<Long> delay(Duration retryAfter) {
        return Mono.create(sink -> {
            Disposable scheduled
                = Schedulers.parallel().schedule(() -> sink.success(0L), retryAfter.toMillis(), TimeUnit.MILLISECONDS);
            sink.onDispose(scheduled);
        });
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
        private boolean stopReconnecting;

        private void setLastEventId(String lastEventId) {
            this.lastEventId = lastEventId;
        }

        private void setRetryAfter(Duration retryAfter) {
            this.retryAfter = retryAfter;
        }

        private String getReconnectEventId() {
            return lastEventId == null || lastEventId.isEmpty() ? null : lastEventId;
        }
    }

    private static final class ReconnectState {
        private final String lastEventId;

        private ReconnectState(String lastEventId) {
            this.lastEventId = lastEventId;
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
