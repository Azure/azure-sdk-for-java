// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.http.ServerSentEvent;
import com.azure.core.http.ServerSentEventListener;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.FluxInputStream;
import com.azure.core.util.BinaryData;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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
        return new ServerSentEventFlux<>(body.toFluxByteBuffer(), StandardCharsets.UTF_8, deserializer);
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
            processBody(body, new StreamState(), StandardCharsets.UTF_8, deserializer,
                TerminalEventPolicy.endOnResponseCompletion(), listener);
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
                : new ServerSentEventFlux<>(streamResponse.getBody().toFluxByteBuffer(), streamResponse.getCharset(),
                    deserializer);
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
                && processBody(streamResponse.getBody(), new StreamState(), streamResponse.getCharset(), deserializer,
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
                && processBody(streamResponse.getBody(), new StreamState(), streamResponse.getCharset(), deserializer,
                    terminalPolicy, listener);
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

    private static <T> boolean processBody(BinaryData body, StreamState state, Charset charset,
        BiFunction<String, String, T> deserializer, TerminalEventPolicy<T> terminalPolicy,
        ServerSentEventListener<T> listener) throws IOException {
        ServerSentEventDecoder decoder = new ServerSentEventDecoder(state, charset);
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

    private static final class ServerSentEventFlux<T> extends Flux<ServerSentEvent<T>> {
        private final Flux<ByteBuffer> source;
        private final Charset charset;
        private final BiFunction<String, String, T> deserializer;

        private ServerSentEventFlux(Flux<ByteBuffer> source, Charset charset,
            BiFunction<String, String, T> deserializer) {
            this.source = source;
            this.charset = charset;
            this.deserializer = deserializer;
        }

        @Override
        public void subscribe(CoreSubscriber<? super ServerSentEvent<T>> actual) {
            source.subscribe(new ServerSentEventSubscriber<>(actual, charset, deserializer));
        }
    }

    private static final class ServerSentEventSubscriber<T> implements CoreSubscriber<ByteBuffer>, Subscription {
        private final CoreSubscriber<? super ServerSentEvent<T>> downstream;
        private final ServerSentEventDecoder decoder;
        private final BiFunction<String, String, T> deserializer;
        private final Queue<ServerSentEventFrame> frames = new ConcurrentLinkedQueue<>();

        private Subscription upstream;
        private volatile boolean sourceRequested;
        private volatile boolean done;
        private volatile boolean cancelled;
        private Throwable error;

        private final AtomicInteger wip = new AtomicInteger();
        private final AtomicLong requested = new AtomicLong();

        private ServerSentEventSubscriber(CoreSubscriber<? super ServerSentEvent<T>> downstream, Charset charset,
            BiFunction<String, String, T> deserializer) {
            this.downstream = downstream;
            this.decoder = new ServerSentEventDecoder(new StreamState(), charset);
            this.deserializer = deserializer;
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            if (Operators.validate(upstream, subscription)) {
                upstream = subscription;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onNext(ByteBuffer buffer) {
            if (done || cancelled) {
                Operators.onNextDropped(buffer, currentContext());
                return;
            }

            try {
                frames.addAll(decoder.feed(buffer));
            } catch (Throwable throwable) {
                Exceptions.throwIfFatal(throwable);
                upstream.cancel();
                onError(throwable);
                return;
            }
            sourceRequested = false;
            drain();
        }

        @Override
        public void onError(Throwable throwable) {
            if (done || cancelled) {
                Operators.onErrorDropped(throwable, currentContext());
                return;
            }

            error = throwable;
            done = true;
            sourceRequested = false;
            drain();
        }

        @Override
        public void onComplete() {
            if (done || cancelled) {
                return;
            }

            try {
                frames.addAll(decoder.finish());
            } catch (Throwable throwable) {
                Exceptions.throwIfFatal(throwable);
                onError(throwable);
                return;
            }
            done = true;
            sourceRequested = false;
            drain();
        }

        @Override
        public void request(long count) {
            if (Operators.validate(count)) {
                addDemand(count);
                drain();
            }
        }

        @Override
        public void cancel() {
            if (cancelled) {
                return;
            }

            cancelled = true;
            upstream.cancel();
            if (wip.getAndIncrement() == 0) {
                frames.clear();
            }
        }

        @Override
        public reactor.util.context.Context currentContext() {
            return downstream.currentContext();
        }

        private void drain() {
            if (wip.getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            while (true) {
                if (cancelled) {
                    frames.clear();
                    return;
                }

                Throwable failure = error;
                if (done && failure != null) {
                    frames.clear();
                    downstream.onError(failure);
                    return;
                }

                long demand = requested.get();
                long emitted = 0;
                while (emitted != demand) {
                    if (cancelled) {
                        frames.clear();
                        return;
                    }

                    boolean sourceDone = done;
                    ServerSentEventFrame frame = frames.poll();
                    boolean empty = frame == null;
                    if (sourceDone && empty) {
                        downstream.onComplete();
                        return;
                    }
                    if (empty) {
                        break;
                    }

                    T data;
                    try {
                        data = deserializer.apply(frame.event, frame.data);
                    } catch (Throwable throwable) {
                        Exceptions.throwIfFatal(throwable);
                        cancelled = true;
                        upstream.cancel();
                        frames.clear();
                        downstream.onError(Operators.onOperatorError(upstream, throwable, frame, currentContext()));
                        return;
                    }
                    if (data == null) {
                        continue;
                    }

                    ServerSentEvent<T> event = frame.toEvent(data);
                    try {
                        downstream.onNext(event);
                    } catch (Throwable throwable) {
                        Exceptions.throwIfFatal(throwable);
                        cancelled = true;
                        upstream.cancel();
                        frames.clear();
                        downstream.onError(Operators.onOperatorError(upstream, throwable, event, currentContext()));
                        return;
                    }
                    emitted++;
                }

                if (emitted != 0) {
                    produced(emitted);
                }

                if (done && frames.isEmpty()) {
                    downstream.onComplete();
                    return;
                }

                if (requested.get() > 0 && frames.isEmpty() && !sourceRequested) {
                    sourceRequested = true;
                    upstream.request(1);
                }

                missed = wip.addAndGet(-missed);
                if (missed == 0) {
                    return;
                }
            }
        }

        private void addDemand(long count) {
            long current;
            long updated;
            do {
                current = requested.get();
                updated = Operators.addCap(current, count);
            } while (!requested.compareAndSet(current, updated));
        }

        private void produced(long count) {
            long current;
            long updated;
            do {
                current = requested.get();
                if (current == Long.MAX_VALUE) {
                    return;
                }
                updated = current - count;
                if (updated < 0) {
                    Operators.reportMoreProduced();
                    updated = 0;
                }
            } while (!requested.compareAndSet(current, updated));
        }
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
        private final Charset declaredCharset;
        private CharsetDecoder charsetDecoder;
        private ByteBuffer remainingBytes = ByteBuffer.allocate(0);
        private final StringBuilder line = new StringBuilder();
        private boolean pendingCarriageReturn;
        private boolean firstLine = true;
        private String event;
        private List<String> data;
        private String comment;

        private ServerSentEventDecoder(StreamState state, Charset charset) {
            this.state = state;
            this.declaredCharset = charset;
        }

        private List<ServerSentEventFrame> feed(ByteBuffer source) {
            return feedCharacters(decode(source.duplicate(), false));
        }

        private List<ServerSentEventFrame> feedCharacters(CharBuffer buffer) {
            List<ServerSentEventFrame> events = new ArrayList<>();

            while (buffer.hasRemaining()) {
                char value = buffer.get();

                if (pendingCarriageReturn) {
                    pendingCarriageReturn = false;
                    if (value == '\n') {
                        continue;
                    }
                }

                if (value == '\n') {
                    processLine(consumeLine(), events);
                } else if (value == '\r') {
                    processLine(consumeLine(), events);
                    pendingCarriageReturn = true;
                } else {
                    line.append(value);
                }
            }

            return events;
        }

        private List<ServerSentEventFrame> finish() {
            // The SSE parsing algorithm discards an event that wasn't terminated by a blank line.
            decode(ByteBuffer.allocate(0), true);
            return Collections.emptyList();
        }

        private CharBuffer decode(ByteBuffer source, boolean endOfInput) {
            ByteBuffer input = ByteBuffer.allocate(remainingBytes.remaining() + source.remaining());
            input.put(remainingBytes.duplicate());
            input.put(source);
            input.flip();
            if (charsetDecoder == null && !initializeDecoder(input, endOfInput)) {
                remainingBytes = ByteBuffer.allocate(input.remaining());
                remainingBytes.put(input).flip();
                return CharBuffer.allocate(0);
            }
            CharBuffer output = CharBuffer.allocate((int) (input.remaining() * charsetDecoder.maxCharsPerByte()) + 1);
            try {
                CoderResult result = charsetDecoder.decode(input, output, endOfInput);
                if (result.isError()) {
                    result.throwException();
                }
                if (endOfInput) {
                    result = charsetDecoder.flush(output);
                    if (result.isError()) {
                        result.throwException();
                    }
                }
            } catch (CharacterCodingException exception) {
                throw new IllegalStateException("Failed to decode the server-sent event stream.", exception);
            }
            remainingBytes = ByteBuffer.allocate(input.remaining());
            remainingBytes.put(input).flip();
            output.flip();
            return output;
        }

        private boolean initializeDecoder(ByteBuffer input, boolean endOfInput) {
            Charset charset = declaredCharset;
            if (!input.hasRemaining()) {
                if (!endOfInput) {
                    return false;
                }
            } else {
                int offset = input.position();
                int remaining = input.remaining();
                int first = input.get(offset) & 0xFF;
                if (first == 0xEF) {
                    if (remaining < 3) {
                        if (!endOfInput) {
                            return false;
                        }
                    } else if ((input.get(offset + 1) & 0xFF) == 0xBB && (input.get(offset + 2) & 0xFF) == 0xBF) {
                        charset = StandardCharsets.UTF_8;
                    }
                } else if (first == 0xFE) {
                    if (remaining < 2) {
                        if (!endOfInput) {
                            return false;
                        }
                    } else if ((input.get(offset + 1) & 0xFF) == 0xFF) {
                        charset = StandardCharsets.UTF_16BE;
                    }
                } else if (first == 0xFF) {
                    if (remaining < 2) {
                        if (!endOfInput) {
                            return false;
                        }
                    } else if ((input.get(offset + 1) & 0xFF) == 0xFE) {
                        if (remaining < 4) {
                            if (!endOfInput) {
                                return false;
                            }
                            if (remaining == 2) {
                                charset = StandardCharsets.UTF_16LE;
                            }
                        } else {
                            charset = input.get(offset + 2) == 0 && input.get(offset + 3) == 0
                                ? Charset.forName("UTF-32LE")
                                : StandardCharsets.UTF_16LE;
                        }
                    }
                } else if (first == 0) {
                    if (remaining < 2) {
                        if (!endOfInput) {
                            return false;
                        }
                    } else if (input.get(offset + 1) == 0) {
                        if (remaining < 4) {
                            if (!endOfInput) {
                                return false;
                            }
                        } else if ((input.get(offset + 2) & 0xFF) == 0xFE && (input.get(offset + 3) & 0xFF) == 0xFF) {
                            charset = Charset.forName("UTF-32BE");
                        }
                    }
                }
            }
            charsetDecoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
            return true;
        }

        private String consumeLine() {
            String decodedLine = line.toString();
            line.setLength(0);

            if (firstLine) {
                firstLine = false;
                if (!decodedLine.isEmpty() && decodedLine.charAt(0) == '\uFEFF') {
                    return decodedLine.substring(1);
                }
            }

            return decodedLine;
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
