// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.http.ServerSentEvent;
import com.azure.core.http.ServerSentEventListener;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.FluxInputStream;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Flux;

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
import java.util.function.BiFunction;

/**
 * Implementation support for parsing one server-sent event response.
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
            processBody(body, new StreamState(), deserializer, listener);
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
     */
    public static <T> Flux<ServerSentEvent<T>> toFlux(Response<BinaryData> response,
        BiFunction<String, String, T> deserializer) {
        Objects.requireNonNull(response, "'response' cannot be null.");
        Objects.requireNonNull(deserializer, "'deserializer' cannot be null.");

        return Flux.defer(() -> {
            ServerSentEventStreamResponse streamResponse = ServerSentEventStreamResponse.fromResponse(response);
            if (streamResponse.getStatusCode() == 204) {
                streamResponse.close();
                return Flux.empty();
            }

            return decodeBody(streamResponse.getBody(), new StreamState(), deserializer)
                .doFinally(ignored -> streamResponse.close());
        });
    }

    /**
     * Processes an SSE response, closing its physical response on completion, failure, or interruption.
     */
    public static <T> void listen(Response<BinaryData> response, BiFunction<String, String, T> deserializer,
        ServerSentEventListener<T> listener) {
        Objects.requireNonNull(response, "'response' cannot be null.");
        Objects.requireNonNull(deserializer, "'deserializer' cannot be null.");
        Objects.requireNonNull(listener, "'listener' cannot be null.");

        try (ServerSentEventStreamResponse streamResponse = ServerSentEventStreamResponse.fromResponse(response)) {
            if (streamResponse.getStatusCode() != 204) {
                processBody(streamResponse.getBody(), new StreamState(), deserializer, listener);
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

    private static <T> void processBody(BinaryData body, StreamState state, BiFunction<String, String, T> deserializer,
        ServerSentEventListener<T> listener) throws IOException {
        ServerSentEventDecoder decoder = new ServerSentEventDecoder(state);
        byte[] readBuffer = new byte[8192];

        try (InputStream stream = new FluxInputStream(body.toFluxByteBuffer())) {
            int read;
            while (true) {
                checkInterrupted();
                read = stream.read(readBuffer);
                if (read == -1) {
                    break;
                }
                if (read > 0) {
                    processFrames(decoder.feed(ByteBuffer.wrap(readBuffer, 0, read)), deserializer, listener);
                }
            }

            processFrames(decoder.finish(), deserializer, listener);
        }
    }

    private static <T> void processFrames(List<ServerSentEventFrame> frames, BiFunction<String, String, T> deserializer,
        ServerSentEventListener<T> listener) {
        for (ServerSentEventFrame frame : frames) {
            checkInterrupted();
            T data = deserializer.apply(frame.event, frame.data);
            if (data != null) {
                listener.onEvent(frame.toEvent(data));
            }
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
