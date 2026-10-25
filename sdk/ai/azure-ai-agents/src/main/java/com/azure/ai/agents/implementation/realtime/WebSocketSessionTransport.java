// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.realtime;

import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.function.Function;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Shared blocking WebSocket connection and I/O, independent of the asynchronous transport.
 * @param <T> the decoded message type.
 */
public final class WebSocketSessionTransport<T> implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(WebSocketSessionTransport.class);
    private final URI websocketUri;
    private final VoiceAgentWebSocketConnectionOptions options;
    private final EventDecoder<T> decoder;
    private final OkHttpClient httpClient;
    private final BlockingQueue<EventSignal<T>> events;
    private final int receiveBufferCapacity;
    private final CountDownLatch handshakeCompleted = new CountDownLatch(1);
    private final CountDownLatch closeCompleted = new CountDownLatch(1);
    private final AtomicReference<Throwable> connectionError = new AtomicReference<>();
    private final AtomicBoolean handshakeSucceeded = new AtomicBoolean();
    private final AtomicBoolean receiveClaimed = new AtomicBoolean();
    private final AtomicBoolean open = new AtomicBoolean();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicBoolean clientShutdown = new AtomicBoolean();
    private volatile WebSocket webSocket;
    private volatile Integer closeCode;
    private volatile String closeReason;

    @FunctionalInterface
    public interface EventDecoder<T> {
        T decode(String json) throws IOException;
    }

    private WebSocketSessionTransport(VoiceAgentWebSocketClientConfiguration configuration, URI websocketUri,
        VoiceAgentWebSocketConnectionOptions options, String subprotocol, Function<String, HttpHeaders> headersFactory,
        EventDecoder<T> decoder) {
        this.options = options;
        this.decoder = Objects.requireNonNull(decoder, "'decoder' cannot be null.");
        this.receiveBufferCapacity = options.getReceiveBufferCapacity();
        this.events = new ArrayBlockingQueue<>(receiveBufferCapacity + 1);
        this.websocketUri = Objects.requireNonNull(websocketUri, "'websocketUri' cannot be null.");
        String token = configuration.getCredential()
            .getTokenSync(VoiceAgentWebSocketUtils.createTokenRequestContext())
            .getToken();
        this.httpClient = createHttpClient(configuration, options);
        Request.Builder request = new Request.Builder().url(websocketUri.toString());
        if (subprotocol != null) {
            request.header("Sec-WebSocket-Protocol", subprotocol);
        }
        for (HttpHeader header : headersFactory.apply(token)) {
            request.header(header.getName(), header.getValue());
        }
        this.webSocket = httpClient.newWebSocket(request.build(), new Listener());
    }

    public static <T> WebSocketSessionTransport<T> connect(VoiceAgentWebSocketClientConfiguration configuration,
        URI websocketUri, VoiceAgentWebSocketConnectionOptions options, String subprotocol,
        Function<String, HttpHeaders> headersFactory, EventDecoder<T> decoder) {
        WebSocketSessionTransport<T> session = new WebSocketSessionTransport<>(configuration, websocketUri, options,
            subprotocol, headersFactory, decoder);
        try {
            session.awaitHandshake();
            return session;
        } catch (RuntimeException error) {
            session.webSocket.cancel();
            session.shutdownHttpClient();
            throw error;
        }
    }

    public String getEndpoint() {
        return websocketUri.toString();
    }

    public boolean isOpen() {
        return open.get() && !closed.get();
    }

    public Integer getCloseCode() {
        return closeCode;
    }

    public String getCloseReason() {
        return closeReason;
    }

    public IterableStream<T> receiveEvents(Duration timeout) {
        return receiveMessages(timeout, ReceivedEvent::getEvent);
    }

    public IterableStream<BinaryData> receiveRawEvents(Duration timeout) {
        return receiveMessages(timeout, ReceivedEvent::getRawEvent);
    }

    private <R> IterableStream<R> receiveMessages(Duration timeout, Function<ReceivedEvent<T>, R> mapper) {
        if (timeout != null && (timeout.isZero() || timeout.isNegative())) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Receive timeout must be positive."));
        }
        if (!receiveClaimed.compareAndSet(false, true)) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Only one receiveEvents iterator is supported per session."));
        }
        AtomicBoolean iteratorCreated = new AtomicBoolean();
        return IterableStream.of(() -> {
            if (!iteratorCreated.compareAndSet(false, true)) {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("The receiveEvents stream may only be iterated once."));
            }
            return new EventIterator<>(events, timeout, mapper);
        });
    }

    public void sendEvent(BinaryData event) {
        String json = VoiceAgentWebSocketUtils.validateEvent(event);
        if (!isOpen()) {
            throw LOGGER
                .logExceptionAsError(new IllegalStateException("The voice-agent WebSocket session is not open."));
        }
        if (!webSocket.send(json)) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("The voice-agent WebSocket send queue is full or closed."));
        }
    }

    @Override
    public void close() {
        close(1000, "");
    }

    public void close(int code, String reason) {
        VoiceAgentWebSocketUtils.validateClose(code, reason);
        if (!closed.compareAndSet(false, true)) {
            shutdownHttpClient();
            return;
        }
        open.set(false);
        if (!webSocket.close(code, reason)) {
            webSocket.cancel();
            closeCompleted.countDown();
        }
        try {
            if (!closeCompleted.await(options.getCloseTimeout().toMillis(), TimeUnit.MILLISECONDS)) {
                webSocket.cancel();
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            webSocket.cancel();
        } finally {
            signal(EventSignal.complete());
            shutdownHttpClient();
        }
    }

    private void awaitHandshake() {
        try {
            if (!handshakeCompleted.await(options.getHandshakeTimeout().toMillis(), TimeUnit.MILLISECONDS)) {
                webSocket.cancel();
                throw LOGGER
                    .logExceptionAsError(new IllegalStateException("Voice-agent WebSocket handshake timed out."));
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            webSocket.cancel();
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Interrupted while opening the voice-agent WebSocket session.", error));
        }
        if (handshakeSucceeded.get()) {
            return;
        }
        Throwable error = connectionError.get();
        if (error instanceof RuntimeException) {
            throw LOGGER.logExceptionAsError((RuntimeException) error);
        }
        if (error != null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Failed to open the voice-agent WebSocket session.", error));
        }
    }

    private void fail(Throwable error, Response response) {
        Throwable mapped;
        try {
            mapped = response == null ? error : mapHandshakeError(error, response);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        connectionError.compareAndSet(null, mapped);
        open.set(false);
        closed.set(true);
        handshakeCompleted.countDown();
        signal(EventSignal.error(mapped));
        closeCompleted.countDown();
        shutdownHttpClient();
    }

    private Throwable mapHandshakeError(Throwable error, Response response) {
        VoiceAgentWebSocketHttpResponse azureResponse = new VoiceAgentWebSocketHttpResponse(websocketUri, response);
        String message = "Voice-agent WebSocket handshake failed with status " + response.code() + ".";
        switch (response.code()) {
            case 401:
                return new ClientAuthenticationException(message, azureResponse, error);

            case 404:
                return new ResourceNotFoundException(message, azureResponse, error);

            case 409:
                return new ResourceModifiedException(message, azureResponse, error);

            default:
                return new HttpResponseException(message, azureResponse, error);
        }
    }

    private synchronized void signal(EventSignal<T> signal) {
        if (signal.event != null && events.size() >= receiveBufferCapacity) {
            switch (options.getOverflowStrategy()) {
                case DROP_LATEST:
                    return;

                case DROP_OLDEST:
                    events.poll();
                    break;

                default:
                    break;
            }
        }
        if ((signal.event != null && events.size() >= receiveBufferCapacity) || !events.offer(signal)) {
            events.clear();
            events.add(EventSignal.error(new IllegalStateException("Voice-agent receive buffer overflow.")));
            WebSocket current = webSocket;
            if (current != null) {
                current.cancel();
            }
            open.set(false);
            closed.set(true);
            shutdownHttpClient();
        }
    }

    private void shutdownHttpClient() {
        if (clientShutdown.compareAndSet(false, true)) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
    }

    public static OkHttpClient createHttpClient(VoiceAgentWebSocketClientConfiguration configuration,
        VoiceAgentWebSocketConnectionOptions options) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(options.getHandshakeTimeout().toMillis(), TimeUnit.MILLISECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .followRedirects(false);
        ProxyOptions proxyOptions = configuration.getProxyOptions();
        if (proxyOptions != null) {
            Proxy.Type proxyType = proxyOptions.getType() == ProxyOptions.Type.SOCKS4
                || proxyOptions.getType() == ProxyOptions.Type.SOCKS5 ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
            Proxy proxy = new Proxy(proxyType, proxyOptions.getAddress());
            if (proxyOptions.getNonProxyHosts() == null) {
                builder.proxy(proxy);
            } else {
                Pattern nonProxyHosts = Pattern.compile(proxyOptions.getNonProxyHosts(), Pattern.CASE_INSENSITIVE);
                builder.proxySelector(new ProxySelector() {
                    @Override
                    public List<Proxy> select(URI uri) {
                        return Collections
                            .singletonList(uri.getHost() != null && nonProxyHosts.matcher(uri.getHost()).matches()
                                ? Proxy.NO_PROXY
                                : proxy);
                    }

                    @Override
                    public void connectFailed(URI uri, SocketAddress address, IOException error) {
                        LOGGER.atVerbose()
                            .addKeyValue("uri", uri)
                            .addKeyValue("proxyAddress", address)
                            .log("Failed to connect through the configured proxy.");
                    }
                });
            }
            if (proxyOptions.getUsername() != null) {
                builder.proxyAuthenticator((route, response) -> response.request()
                    .newBuilder()
                    .header("Proxy-Authorization",
                        Credentials.basic(proxyOptions.getUsername(), proxyOptions.getPassword()))
                    .build());
            }
        }
        return builder.build();
    }

    private final class Listener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            handshakeSucceeded.set(true);
            open.set(true);
            handshakeCompleted.countDown();
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            if (text.getBytes(StandardCharsets.UTF_8).length > options.getMaxMessageSize()) {
                fail(new IllegalArgumentException("Voice-agent message exceeds the configured size limit."), null);
                webSocket.close(1009, "Message too large");
                return;
            }
            try {
                signal(EventSignal.event(new ReceivedEvent<>(decoder.decode(text), BinaryData.fromString(text))));
            } catch (IOException | RuntimeException error) {
                malformedEvent(webSocket, error);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            if (bytes.size() > options.getMaxMessageSize()) {
                fail(new IllegalArgumentException("Voice-agent message exceeds the configured size limit."), null);
                webSocket.close(1009, "Message too large");
                return;
            }
            try {
                onMessage(webSocket, VoiceAgentWebSocketUtils.decodeEvent(bytes.toByteArray()));
            } catch (CharacterCodingException error) {
                malformedEvent(webSocket, error);
            }
        }

        private void malformedEvent(WebSocket webSocket, Throwable error) {
            if (options.getMalformedEventHandler() != null) {
                try {
                    options.getMalformedEventHandler().accept(error);
                    return;
                } catch (RuntimeException callbackError) {
                    error = callbackError;
                }
            }
            fail(new IllegalArgumentException("Invalid JSON event.", error), null);
            webSocket.close(1007, "Invalid JSON event");
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            closeCode = code;
            closeReason = reason;
            webSocket.close(code == 1005 ? 1000 : code, reason);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            closeCode = code;
            closeReason = reason;
            open.set(false);
            closed.set(true);
            signal(EventSignal.complete());
            handshakeCompleted.countDown();
            closeCompleted.countDown();
            shutdownHttpClient();
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable error, Response response) {
            fail(error, response);
        }
    }

    static final class ReceivedEvent<T> {
        private final T event;
        private final BinaryData rawEvent;

        ReceivedEvent(T event, BinaryData rawEvent) {
            this.event = event;
            this.rawEvent = rawEvent;
        }

        T getEvent() {
            return event;
        }

        BinaryData getRawEvent() {
            return rawEvent;
        }
    }

    private static final class EventSignal<T> {
        private final ReceivedEvent<T> event;
        private final Throwable error;
        private final boolean complete;

        private EventSignal(ReceivedEvent<T> event, Throwable error, boolean complete) {
            this.event = event;
            this.error = error;
            this.complete = complete;
        }

        private static <T> EventSignal<T> event(ReceivedEvent<T> event) {
            return new EventSignal<>(event, null, false);
        }

        private static <T> EventSignal<T> error(Throwable error) {
            return new EventSignal<>(null, error, false);
        }

        private static <T> EventSignal<T> complete() {
            return new EventSignal<>(null, null, true);
        }
    }

    private static final class EventIterator<T, R> implements Iterator<R> {
        private final BlockingQueue<EventSignal<T>> events;
        private final Duration timeout;
        private final Function<ReceivedEvent<T>, R> mapper;
        private EventSignal<T> next;

        private EventIterator(BlockingQueue<EventSignal<T>> events, Duration timeout,
            Function<ReceivedEvent<T>, R> mapper) {
            this.events = events;
            this.timeout = timeout;
            this.mapper = mapper;
        }

        @Override
        public boolean hasNext() {
            if (next == null) {
                try {
                    next = timeout == null ? events.take() : events.poll(timeout.toNanos(), TimeUnit.NANOSECONDS);
                    if (next == null) {
                        throw new IllegalStateException("Timed out waiting for a voice-agent event.",
                            new TimeoutException());
                    }
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting for a voice-agent event.", error);
                }
            }
            if (next.error != null) {
                Throwable error = next.error;
                next = null;
                if (error instanceof RuntimeException) {
                    throw (RuntimeException) error;
                }
                throw new IllegalStateException("Voice-agent event stream failed.", error);
            }
            return !next.complete;
        }

        @Override
        public R next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            ReceivedEvent<T> event = next.event;
            next = null;
            return mapper.apply(event);
        }
    }
}
