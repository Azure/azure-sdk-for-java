// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketClientConfiguration;
import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketHandshakeHandler;
import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketHttpResponse;
import com.azure.ai.agents.implementation.utils.Beta;
import com.azure.ai.agents.models.RealtimeClientEvent;
import com.azure.ai.agents.models.RealtimeClientEventConversationItemCreate;
import com.azure.ai.agents.models.RealtimeClientEventInputAudioBufferAppend;
import com.azure.ai.agents.models.RealtimeClientEventInputAudioBufferClear;
import com.azure.ai.agents.models.RealtimeClientEventInputAudioBufferCommit;
import com.azure.ai.agents.models.RealtimeClientEventResponseCancel;
import com.azure.ai.agents.models.RealtimeClientEventResponseCreate;
import com.azure.ai.agents.models.RealtimeConversationItem;
import com.azure.ai.agents.models.RealtimeConversationItemFunctionCallOutput;
import com.azure.ai.agents.models.RealtimeConversationItemMessageUser;
import com.azure.ai.agents.models.RealtimeConversationItemMessageUserContent;
import com.azure.ai.agents.models.RealtimeConversationItemMessageUserContentType;
import com.azure.ai.agents.models.RealtimeServerEvent;
import com.azure.ai.agents.models.VoiceAgentResponseCreateParams;
import com.azure.ai.agents.models.VoiceAgentTransport;
import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.AsyncCloseable;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakeException;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
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
import reactor.util.concurrent.Queues;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An asynchronous bidirectional realtime session connected to a Foundry voice agent.
 *
 * <p>Instances are created by {@link BetaVoiceAgentWebSocketAsyncClient#connect(String)}. A session supports one
 * subscriber to {@link #receiveEvents()}. Close the session when it is no longer needed.</p>
 */
@Beta(warningText = "This class is in preview and may change in future releases.")
public final class VoiceAgentWebSocketSessionAsyncClient implements AsyncCloseable, AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(VoiceAgentWebSocketSessionAsyncClient.class);
    private static final String TOKEN_SCOPE = "https://ai.azure.com/.default";
    private static final String PREVIEW_FEATURE = "VoiceAgents=V1Preview";
    private static final String SUBPROTOCOL = "realtime";
    private static final int MAX_FRAME_SIZE = 32 * 1024 * 1024;
    private static final int INBOUND_CAPACITY = 256;
    private static final int MAX_OUTSTANDING_SENDS = 256;

    private final VoiceAgentWebSocketClientConfiguration configuration;
    private final VoiceAgentWebSocketConnectionOptions options;
    private final HttpClient httpClient;
    private final URI websocketUri;
    private final AtomicReference<State> state = new AtomicReference<>(State.NEW);
    private final AtomicReference<Channel> channel = new AtomicReference<>();
    private final AtomicReference<WebsocketOutbound> outbound = new AtomicReference<>();
    private final AtomicReference<Disposable> lifecycle = new AtomicReference<>();
    private final AtomicBoolean receiveClaimed = new AtomicBoolean();
    private final Semaphore sendPermits = new Semaphore(MAX_OUTSTANDING_SENDS);
    private final Sinks.Many<RealtimeServerEvent> events
        = Sinks.many().unicast().onBackpressureBuffer(Queues.<RealtimeServerEvent>get(INBOUND_CAPACITY).get());
    private final Sinks.One<Void> ready = Sinks.one();
    private final Sinks.One<Void> closeSignal = Sinks.one();
    private final AtomicReference<Mono<Void>> closeOperation = new AtomicReference<>();

    private volatile Integer closeCode;
    private volatile String closeReason;

    VoiceAgentWebSocketSessionAsyncClient(VoiceAgentWebSocketClientConfiguration configuration, String agentName,
        VoiceAgentWebSocketConnectionOptions options) {
        this(configuration, agentName, options, HttpClient.create());
    }

    VoiceAgentWebSocketSessionAsyncClient(VoiceAgentWebSocketClientConfiguration configuration, String agentName,
        VoiceAgentWebSocketConnectionOptions options, HttpClient httpClient) {
        this.configuration = Objects.requireNonNull(configuration, "'configuration' cannot be null.");
        Objects.requireNonNull(agentName, "'agentName' cannot be null.");
        if (agentName.isEmpty()) {
            throw new IllegalArgumentException("'agentName' cannot be empty.");
        }
        this.options = options == null ? new VoiceAgentWebSocketConnectionOptions() : options;
        this.httpClient = Objects.requireNonNull(httpClient, "'httpClient' cannot be null.");
        this.websocketUri = buildWebSocketUri(configuration, agentName, this.options);
    }

    Mono<Void> connect() {
        return Mono.defer(() -> {
            if (!state.compareAndSet(State.NEW, State.CONNECTING)) {
                return Mono.error(new IllegalStateException("The voice-agent session has already been started."));
            }

            TokenRequestContext tokenContext = new TokenRequestContext().addScopes(TOKEN_SCOPE);
            configuration.getCredential()
                .getToken(tokenContext)
                .map(AccessToken::getToken)
                .flatMap(this::openWebSocket)
                .subscribe(unused -> {
                }, this::terminateWithError, this::terminateNormally);
            return ready.asMono().timeout(options.getHandshakeTimeout());
        });
    }

    /**
     * Gets the WebSocket endpoint used by this session.
     *
     * @return the WebSocket endpoint.
     */
    public URI getEndpoint() {
        return websocketUri;
    }

    /**
     * Determines whether the WebSocket session is open.
     *
     * @return {@code true} when the session is open.
     */
    public boolean isOpen() {
        Channel current = channel.get();
        return state.get() == State.OPEN && current != null && current.isActive();
    }

    /**
     * Gets the peer close code after the session closes.
     *
     * @return the close code, or {@code null} when no close frame has been received.
     */
    public Integer getCloseCode() {
        return closeCode;
    }

    /**
     * Gets the peer close reason after the session closes.
     *
     * @return the close reason, or {@code null} when no close frame has been received.
     */
    public String getCloseReason() {
        return closeReason;
    }

    /**
     * Receives typed server events in wire order. Only one subscriber is supported per session.
     *
     * @return the server event stream.
     */
    public Flux<RealtimeServerEvent> receiveEvents() {
        return Flux.defer(() -> receiveClaimed.compareAndSet(false, true)
            ? events.asFlux()
            : Flux.error(new IllegalStateException("Only one receiveEvents subscriber is supported per session.")));
    }

    /**
     * Sends a typed realtime client event.
     *
     * @param event the event to send.
     * @return a completion signal emitted after the frame is written.
     */
    public Mono<Void> sendEvent(RealtimeClientEvent event) {
        Objects.requireNonNull(event, "'event' cannot be null.");
        return Mono.defer(() -> {
            Channel current = requireOpenChannel();
            if (!sendPermits.tryAcquire()) {
                return Mono.error(new IllegalStateException("Too many voice-agent WebSocket sends are outstanding."));
            }

            final String json;
            try {
                json = serialize(event);
            } catch (IOException error) {
                sendPermits.release();
                return Mono
                    .error(new IllegalArgumentException("Failed to serialize the realtime client event.", error));
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

    /**
     * Adds an item to the session conversation.
     *
     * @param item the item to add.
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> createConversationItem(RealtimeConversationItem item) {
        return createConversationItem(item, null);
    }

    /**
     * Adds an item after a specific conversation item.
     *
     * @param item the item to add.
     * @param previousItemId the preceding item identifier, or {@code null} to append.
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> createConversationItem(RealtimeConversationItem item, String previousItemId) {
        Objects.requireNonNull(item, "'item' cannot be null.");
        return sendEvent(new RealtimeClientEventConversationItemCreate(item).setPreviousItemId(previousItemId));
    }

    /**
     * Adds a user text message to the session conversation.
     *
     * @param text the user message.
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> sendText(String text) {
        Objects.requireNonNull(text, "'text' cannot be null.");
        RealtimeConversationItemMessageUserContent content = new RealtimeConversationItemMessageUserContent()
            .setType(RealtimeConversationItemMessageUserContentType.INPUT_TEXT)
            .setText(text);
        return createConversationItem(new RealtimeConversationItemMessageUser(Collections.singletonList(content)));
    }

    /**
     * Appends PCM or encoded audio bytes to the input audio buffer.
     *
     * @param audio the bytes in the input format configured by the voice agent.
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> appendInputAudio(BinaryData audio) {
        Objects.requireNonNull(audio, "'audio' cannot be null.");
        String encoded = Base64.getEncoder().encodeToString(audio.toBytes());
        return sendEvent(new RealtimeClientEventInputAudioBufferAppend(encoded));
    }

    /**
     * Clears the input audio buffer.
     *
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> clearInputAudio() {
        return sendEvent(new RealtimeClientEventInputAudioBufferClear());
    }

    /**
     * Commits the input audio buffer.
     *
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> commitInputAudio() {
        return sendEvent(new RealtimeClientEventInputAudioBufferCommit());
    }

    /**
     * Requests a response using the persisted voice-agent configuration.
     *
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> createResponse() {
        return sendEvent(new RealtimeClientEventResponseCreate());
    }

    /**
     * Requests a response with per-response options.
     *
     * @param responseOptions the response options.
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> createResponse(VoiceAgentResponseCreateParams responseOptions) {
        Objects.requireNonNull(responseOptions, "'responseOptions' cannot be null.");
        return sendEvent(new RealtimeClientEventResponseCreate().setResponse(responseOptions));
    }

    /**
     * Cancels the response currently writing to the default conversation.
     *
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> cancelResponse() {
        return sendEvent(new RealtimeClientEventResponseCancel());
    }

    /**
     * Cancels a specific response.
     *
     * @param responseId the response identifier.
     * @return a completion signal emitted after the event is written.
     */
    public Mono<Void> cancelResponse(String responseId) {
        Objects.requireNonNull(responseId, "'responseId' cannot be null.");
        return sendEvent(new RealtimeClientEventResponseCancel().setResponseId(responseId));
    }

    /**
     * Sends a function-call result and requests the next response.
     *
     * @param callId the function call identifier.
     * @param output the serialized function result.
     * @return a completion signal emitted after both events are written.
     */
    public Mono<Void> sendFunctionCallOutput(String callId, String output) {
        RealtimeConversationItemFunctionCallOutput item
            = new RealtimeConversationItemFunctionCallOutput(callId, output);
        return createConversationItem(item).then(createResponse());
    }

    /**
     * Closes the WebSocket session.
     *
     * @return a completion signal for closing the session.
     */
    @Override
    public Mono<Void> closeAsync() {
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
            Mono<Void> graceful = currentOutbound == null ? Mono.empty() : currentOutbound.sendClose(1000, "");
            Mono<Void> disposed = currentChannel == null ? Mono.empty() : Connection.from(currentChannel).onDispose();
            return graceful.then(disposed).timeout(options.getCloseTimeout()).onErrorResume(error -> {
                if (currentChannel != null) {
                    currentChannel.close();
                }
                return Mono.empty();
            }).doFinally(signal -> terminateNormally());
        }).cache();

        if (closeOperation.compareAndSet(null, created)) {
            return created;
        }
        return closeOperation.get();
    }

    /**
     * Closes the WebSocket session synchronously.
     */
    @Override
    public void close() {
        closeAsync().block(options.getCloseTimeout().plusSeconds(1));
    }

    private Mono<Void> openWebSocket(String token) {
        HttpClient client = configureProxy(httpClient).followRedirect(false)
            .responseTimeout(options.getHandshakeTimeout())
            .doOnConnected(connection -> connection.addHandlerLast("voiceAgentHandshakeResponseObserver",
                new VoiceAgentWebSocketHandshakeHandler(this::terminateWithError)))
            .headers(headers -> {
                for (HttpHeader header : configuration.getHeaders()) {
                    if (!isProtectedHeader(header.getName())) {
                        headers.set(header.getName(), header.getValue());
                    }
                }
                headers.set(HttpHeaderName.AUTHORIZATION.getCaseInsensitiveName(), "Bearer " + token);
                headers.set(HttpHeaderName.USER_AGENT.getCaseInsensitiveName(), configuration.getUserAgent());
                headers.set("Foundry-Features", PREVIEW_FEATURE);
            });
        WebsocketClientSpec spec = WebsocketClientSpec.builder()
            .protocols(SUBPROTOCOL)
            .maxFramePayloadLength(MAX_FRAME_SIZE)
            .handlePing(false)
            .build();

        return client.websocket(spec).uri(websocketUri.toString()).connect().flatMap(connection -> {
            if (!(connection instanceof WebsocketInbound) || !(connection instanceof WebsocketOutbound)) {
                return Mono.error(new IllegalStateException("The WebSocket transport returned an invalid connection."));
            }
            return handleConnection((WebsocketInbound) connection, (WebsocketOutbound) connection);
        });
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

        Disposable receive = inbound.aggregateFrames(MAX_FRAME_SIZE)
            .receiveFrames()
            .subscribe(this::handleFrame, this::terminateWithError, this::terminateNormally);
        lifecycle.set(receive);
        return closeSignal.asMono();
    }

    private void handleFrame(WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            try {
                RealtimeServerEvent event
                    = RealtimeServerEvent.fromJson(JsonProviders.createReader(((TextWebSocketFrame) frame).text()));
                Sinks.EmitResult result = events.tryEmitNext(event);
                if (result.isFailure()) {
                    terminateWithError(new IllegalStateException("Voice-agent receive buffer overflow: " + result));
                }
            } catch (IOException | RuntimeException error) {
                closeWithProtocolError(1007, "Invalid JSON event", error);
            }
        } else if (frame instanceof BinaryWebSocketFrame) {
            closeWithProtocolError(1003, "Binary frames are not supported",
                new IllegalArgumentException("The voice-agent protocol requires JSON text frames."));
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
        if (!(current instanceof WebSocketClientHandshakeException)) {
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

    private static boolean isProtectedHeader(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return "authorization".equals(lower)
            || "host".equals(lower)
            || "upgrade".equals(lower)
            || "connection".equals(lower)
            || "foundry-features".equals(lower)
            || "sec-websocket-protocol".equals(lower)
            || lower.startsWith("sec-websocket-");
    }

    private static String serialize(RealtimeClientEvent event) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(output)) {
            event.toJson(writer);
        }
        return output.toString(StandardCharsets.UTF_8.name());
    }

    private static URI buildWebSocketUri(VoiceAgentWebSocketClientConfiguration configuration, String agentName,
        VoiceAgentWebSocketConnectionOptions options) {
        URI endpoint = configuration.getEndpoint();
        String scheme;
        if ("https".equalsIgnoreCase(endpoint.getScheme()) || "wss".equalsIgnoreCase(endpoint.getScheme())) {
            scheme = "wss";
        } else if ("http".equalsIgnoreCase(endpoint.getScheme()) || "ws".equalsIgnoreCase(endpoint.getScheme())) {
            scheme = "ws";
        } else {
            throw new IllegalArgumentException("Unsupported endpoint scheme: " + endpoint.getScheme());
        }

        String basePath = endpoint.getRawPath() == null ? "" : endpoint.getRawPath().replaceAll("/$", "");
        String path = basePath + "/agents/" + encode(agentName) + "/endpoint/protocols/voice";
        StringBuilder query = new StringBuilder("api-version=").append(encode(configuration.getApiVersion()))
            .append("&x-ms-client-sdk=")
            .append(encode(configuration.getUserAgent()));
        VoiceAgentTransport transport = options.getTransport();
        if (transport != null) {
            query.append("&transport=").append(encode(transport.toString()));
        }
        if (options.isStoreEnabled() != null) {
            query.append("&store=").append(options.isStoreEnabled());
        }
        if (options.getAgentVersionOverride() != null) {
            query.append("&x-agent-version-override=").append(encode(options.getAgentVersionOverride()));
        }

        String authority = endpoint.getRawAuthority();
        return URI.create(scheme + "://" + authority + path + "?" + query);
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (Exception error) {
            throw new IllegalStateException("UTF-8 encoding is unavailable.", error);
        }
    }

    private enum State {
        NEW, CONNECTING, OPEN, CLOSING, CLOSED
    }
}
