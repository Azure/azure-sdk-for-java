// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.realtime;

import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;
import com.azure.ai.agents.implementation.realtime.WebSocketSessionTransport.ReceivedEvent;
import com.azure.core.credential.AccessToken;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakeException;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;
import reactor.netty.transport.ProxyProvider;

/**
 * Shared asynchronous WebSocket connection and I/O. Protocol clients supply the incoming message decoder.
 * @param <T> the decoded message type.
 */
public final class WebSocketSessionAsyncTransport<T> implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(WebSocketSessionAsyncTransport.class);
    private static final int MAX_OUTSTANDING_SENDS = 256;
    private final VoiceAgentWebSocketClientConfiguration configuration;
    private final VoiceAgentWebSocketConnectionOptions options;
    private final HttpClient httpClient;
    private final URI websocketUri;
    private final String subprotocol;
    private final Function<String, HttpHeaders> headersFactory;
    private final WebSocketSessionTransport.EventDecoder<T> decoder;
    private final AtomicReference<State> state = new AtomicReference<>(State.NEW);
    private final AtomicReference<Channel> channel = new AtomicReference<>();
    private final AtomicReference<WebsocketOutbound> outbound = new AtomicReference<>();
    private final AtomicReference<Disposable> connectionOperation = new AtomicReference<>();
    private final AtomicReference<Disposable> lifecycle = new AtomicReference<>();
    private final AtomicBoolean receiveClaimed = new AtomicBoolean();
    private final Semaphore sendPermits = new Semaphore(MAX_OUTSTANDING_SENDS);
    private final Queue<ReceivedEvent<T>> eventQueue;
    private final Sinks.Many<ReceivedEvent<T>> events;
    private final Sinks.One<Void> ready = Sinks.one();
    private final Sinks.One<Void> closeSignal = Sinks.one();
    private final AtomicReference<Mono<Void>> closeOperation = new AtomicReference<>();
    private volatile Integer closeCode;
    private volatile String closeReason;

    public WebSocketSessionAsyncTransport(VoiceAgentWebSocketClientConfiguration configuration, URI websocketUri,
        VoiceAgentWebSocketConnectionOptions options, HttpClient httpClient, String subprotocol,
        Function<String, HttpHeaders> headersFactory, WebSocketSessionTransport.EventDecoder<T> decoder) {
        this.configuration = Objects.requireNonNull(configuration, "'configuration' cannot be null.");
        this.options = options == null ? new VoiceAgentWebSocketConnectionOptions() : options;
        this.eventQueue = new ArrayBlockingQueue<>(this.options.getReceiveBufferCapacity());
        this.events = Sinks.many().unicast().onBackpressureBuffer(eventQueue);
        this.httpClient = Objects.requireNonNull(httpClient, "'httpClient' cannot be null.");
        this.websocketUri = Objects.requireNonNull(websocketUri, "'websocketUri' cannot be null.");
        this.subprotocol = subprotocol;
        this.headersFactory = Objects.requireNonNull(headersFactory, "'headersFactory' cannot be null.");
        this.decoder = Objects.requireNonNull(decoder, "'decoder' cannot be null.");
    }

    public Mono<Void> connect() {
        return Mono.defer(() -> {
            if (!state.compareAndSet(State.NEW, State.CONNECTING)) {
                return Mono.error(new IllegalStateException("The voice-agent session has already been started."));
            }
            return configuration.getCredential()
                .getToken(VoiceAgentWebSocketUtils.createTokenRequestContext())
                .map(AccessToken::getToken)
                .flatMap(token -> {
                    Disposable connection = openWebSocket(token).subscribe(unused -> {
                    }, this::terminateWithError, this::terminateNormally);
                    connectionOperation.set(connection);
                    return ready.asMono().timeout(options.getHandshakeTimeout());
                })
                .doOnError(this::terminateWithError)
                .doOnCancel(() -> terminateWithError(new CancellationException("WebSocket connection cancelled.")));
        });
    }

    public String getEndpoint() {
        return websocketUri.toString();
    }

    public boolean isOpen() {
        Channel current = channel.get();
        return state.get() == State.OPEN && current != null && current.isActive();
    }

    public Integer getCloseCode() {
        return closeCode;
    }

    public String getCloseReason() {
        return closeReason;
    }

    public Flux<T> receiveEvents() {
        return receiveMessages().map(ReceivedEvent::getEvent);
    }

    public Flux<BinaryData> receiveRawEvents() {
        return receiveMessages().map(ReceivedEvent::getRawEvent);
    }

    private Flux<ReceivedEvent<T>> receiveMessages() {
        return Flux.defer(() -> receiveClaimed.compareAndSet(false, true)
            ? events.asFlux()
            : Flux.error(new IllegalStateException("Only one receiveEvents subscriber is supported per session.")));
    }

    public Mono<Void> sendEvent(BinaryData event) {
        Objects.requireNonNull(event, "'event' cannot be null.");
        return Mono.defer(() -> {
            String json = VoiceAgentWebSocketUtils.validateEvent(event);
            Channel current = requireOpenChannel();
            if (!sendPermits.tryAcquire()) {
                return Mono.error(new IllegalStateException("Too many voice-agent WebSocket sends are outstanding."));
            }
            return Mono.create(sink -> current.writeAndFlush(new TextWebSocketFrame(json)).addListener(result -> {
                sendPermits.release();
                if (result.isSuccess()) {
                    sink.success();
                } else {
                    sink.error(result.cause());
                }
            }));
        });
    }

    public Mono<Void> closeAsync() {
        return closeAsync(1000, "");
    }

    public Mono<Void> closeAsync(int code, String reason) {
        try {
            VoiceAgentWebSocketUtils.validateClose(code, reason);
        } catch (IllegalArgumentException exception) {
            return Mono.error(exception);
        }
        Mono<Void> existing = closeOperation.get();
        if (existing != null) {
            return existing;
        }
        Mono<Void> created = Mono.defer(() -> {
            State current = state.get();
            if (current == State.CLOSED || current == State.NEW) {
                state.set(State.CLOSED);
                events.tryEmitComplete();
                return Mono.empty();
            }
            state.set(State.CLOSING);
            WebsocketOutbound currentOutbound = outbound.get();
            Channel currentChannel = channel.get();
            Mono<Void> graceful = currentOutbound == null ? Mono.empty() : currentOutbound.sendClose(code, reason);
            Mono<Void> disposed = currentChannel == null ? Mono.empty() : Connection.from(currentChannel).onDispose();
            return graceful.then(disposed).timeout(options.getCloseTimeout()).onErrorResume(error -> {
                if (currentChannel != null) {
                    currentChannel.close();
                }
                return Mono.empty();
            }).doFinally(signal -> terminateNormally());
        }).cache();
        return closeOperation.compareAndSet(null, created) ? created : closeOperation.get();
    }

    @Override
    public void close() {
        closeAsync().block(options.getCloseTimeout().plusSeconds(1));
    }

    private Mono<Void> openWebSocket(String token) {
        HttpClient client = configureProxy(httpClient).followRedirect(false)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, toConnectTimeoutMillis(options.getHandshakeTimeout()))
            .doOnConnected(connection -> connection.addHandlerLast("voiceAgentHandshakeResponseObserver",
                new VoiceAgentWebSocketHandshakeHandler(this::terminateWithError)))
            .headers(headers -> {
                for (HttpHeader header : headersFactory.apply(token)) {
                    headers.set(header.getName(), header.getValue());
                }
            });
        WebsocketClientSpec.Builder specBuilder
            = WebsocketClientSpec.builder().maxFramePayloadLength(options.getMaxMessageSize());
        if (subprotocol != null) {
            specBuilder.protocols(subprotocol);
        }
        return client.websocket(specBuilder.build()).uri(websocketUri.toString()).connect().flatMap(connection -> {
            if (!(connection instanceof WebsocketInbound) || !(connection instanceof WebsocketOutbound)) {
                return Mono.error(new IllegalStateException("The WebSocket transport returned an invalid connection."));
            }
            return handleConnection((WebsocketInbound) connection, (WebsocketOutbound) connection);
        });
    }

    private static int toConnectTimeoutMillis(Duration timeout) {
        if (timeout.compareTo(Duration.ofMillis(Integer.MAX_VALUE)) >= 0) {
            return Integer.MAX_VALUE;
        }
        return Math.toIntExact(Math.max(1L, timeout.toMillis()));
    }

    private Mono<Void> handleConnection(WebsocketInbound inbound, WebsocketOutbound outbound) {
        this.outbound.set(outbound);
        inbound.withConnection(connection -> channel.set(connection.channel()));
        state.set(State.OPEN);
        ready.tryEmitEmpty();
        inbound.receiveCloseStatus().subscribe(status -> {
            closeCode = status.code();
            closeReason = status.reasonText();
        }, error -> LOGGER.atVerbose().addKeyValue("error", error.getMessage()).log("Close status unavailable."));
        Disposable receive = inbound.aggregateFrames(options.getMaxMessageSize())
            .receiveFrames()
            .subscribe(this::handleFrame, this::terminateWithError, this::terminateNormally);
        lifecycle.set(receive);
        return closeSignal.asMono();
    }

    private void handleFrame(WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame || frame instanceof BinaryWebSocketFrame) {
            try {
                byte[] bytes = new byte[frame.content().readableBytes()];
                frame.content().getBytes(frame.content().readerIndex(), bytes);
                String json = VoiceAgentWebSocketUtils.decodeEvent(bytes);
                ReceivedEvent<T> event = new ReceivedEvent<>(decoder.decode(json), BinaryData.fromString(json));
                Sinks.EmitResult result = events.tryEmitNext(event);
                if (result == Sinks.EmitResult.FAIL_OVERFLOW || result == Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER) {
                    switch (options.getOverflowStrategy()) {
                        case DROP_LATEST:
                            return;

                        case DROP_OLDEST:
                            eventQueue.poll();
                            result = events.tryEmitNext(event);
                            break;

                        default:
                            break;
                    }
                }
                if (result.isFailure()) {
                    terminateWithError(new IllegalStateException("Voice-agent event emission failed: " + result));
                }
            } catch (IOException | RuntimeException error) {
                Throwable failure = error;
                if (options.getMalformedEventHandler() != null) {
                    try {
                        options.getMalformedEventHandler().accept(error);
                        return;
                    } catch (RuntimeException callbackError) {
                        failure = callbackError;
                    }
                }
                closeWithProtocolError(1007, "Invalid JSON event", failure);
            }
        }
    }

    private void closeWithProtocolError(int code, String reason, Throwable error) {
        WebsocketOutbound current = outbound.get();
        terminateWithError(error);
        if (current != null) {
            current.sendClose(code, reason).subscribe(unused -> {
            }, ignored -> {
            });
        }
    }

    private void terminateWithError(Throwable error) {
        Throwable mappedError = mapHandshakeError(error);
        State previous = state.getAndSet(State.CLOSED);
        if (previous == State.CLOSED) {
            return;
        }
        ready.tryEmitError(mappedError);
        events.tryEmitError(mappedError);
        closeSignal.tryEmitError(mappedError);
        disposeReceive();
    }

    private Throwable mapHandshakeError(Throwable error) {
        Throwable current = error;
        while (current != null && !(current instanceof WebSocketClientHandshakeException)) {
            current = current.getCause();
        }
        if (current == null) {
            return error;
        }
        WebSocketClientHandshakeException handshakeError = (WebSocketClientHandshakeException) current;
        if (handshakeError.response() == null) {
            return error;
        }
        VoiceAgentWebSocketHttpResponse response
            = new VoiceAgentWebSocketHttpResponse(websocketUri, handshakeError.response());
        String message = "Voice-agent WebSocket handshake failed with status " + response.getStatusCode() + ".";
        switch (response.getStatusCode()) {
            case 401:
                return new ClientAuthenticationException(message, response, error);

            case 404:
                return new ResourceNotFoundException(message, response, error);

            case 409:
                return new ResourceModifiedException(message, response, error);

            default:
                return new HttpResponseException(message, response, error);
        }
    }

    private void terminateNormally() {
        State previous = state.getAndSet(State.CLOSED);
        if (previous == State.CLOSED) {
            return;
        }
        if (previous == State.CONNECTING) {
            ready.tryEmitError(new IllegalStateException("The WebSocket closed before the handshake completed."));
        }
        events.tryEmitComplete();
        closeSignal.tryEmitEmpty();
        disposeReceive();
    }

    private void disposeReceive() {
        Disposable connection = connectionOperation.getAndSet(null);
        if (connection != null && !connection.isDisposed()) {
            connection.dispose();
        }
        Disposable receive = lifecycle.getAndSet(null);
        if (receive != null && !receive.isDisposed()) {
            receive.dispose();
        }
    }

    private Channel requireOpenChannel() {
        Channel current = channel.get();
        if (state.get() != State.OPEN || current == null || !current.isActive()) {
            throw LOGGER
                .logExceptionAsError(new IllegalStateException("The voice-agent WebSocket session is not open."));
        }
        return current;
    }

    private HttpClient configureProxy(HttpClient client) {
        ProxyOptions proxy = configuration.getProxyOptions();
        if (proxy == null) {
            return client;
        }
        return client.proxy(typeSpec -> {
            ProxyProvider.Proxy proxyType;
            switch (proxy.getType()) {
                case SOCKS4:
                    proxyType = ProxyProvider.Proxy.SOCKS4;
                    break;

                case SOCKS5:
                    proxyType = ProxyProvider.Proxy.SOCKS5;
                    break;

                default:
                    proxyType = ProxyProvider.Proxy.HTTP;
                    break;
            }
            ProxyProvider.Builder builder = typeSpec.type(proxyType).socketAddress(proxy.getAddress());
            if (proxy.getUsername() != null) {
                builder.username(proxy.getUsername()).password(ignored -> proxy.getPassword());
            }
            if (proxy.getNonProxyHosts() != null) {
                builder.nonProxyHosts(proxy.getNonProxyHosts());
            }
        });
    }

    private enum State {
        NEW, CONNECTING, OPEN, CLOSING, CLOSED
    }
}
