// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.ClientEvent;
import com.azure.ai.voicelive.models.ClientEventConversationItemCreate;
import com.azure.ai.voicelive.models.ClientEventConversationItemDelete;
import com.azure.ai.voicelive.models.ClientEventConversationItemRetrieve;
import com.azure.ai.voicelive.models.ClientEventConversationItemTruncate;
import com.azure.ai.voicelive.models.ClientEventInputAudioBufferAppend;
import com.azure.ai.voicelive.models.ClientEventInputAudioBufferClear;
import com.azure.ai.voicelive.models.ClientEventInputAudioBufferCommit;
import com.azure.ai.voicelive.models.ClientEventInputAudioClear;
import com.azure.ai.voicelive.models.ClientEventInputAudioTurnAppend;
import com.azure.ai.voicelive.models.ClientEventInputAudioTurnCancel;
import com.azure.ai.voicelive.models.ClientEventInputAudioTurnEnd;
import com.azure.ai.voicelive.models.ClientEventInputAudioTurnStart;
import com.azure.ai.voicelive.models.ClientEventResponseCancel;
import com.azure.ai.voicelive.models.ClientEventResponseCreate;
import com.azure.ai.voicelive.models.ClientEventSessionAvatarConnect;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.ConversationRequestItem;
import com.azure.ai.voicelive.models.SessionUpdate;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.util.AsyncCloseable;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import reactor.core.Disposable;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a WebSocket-based session for real-time voice communication with the Azure VoiceLive service.
 * <p>
 * This class abstracts bidirectional communication between the caller and service,
 * simultaneously sending and receiving WebSocket messages.
 * </p>
 * <p>
 * Users can obtain a VoiceLiveSession instance from {@link VoiceLiveAsyncClient#startSession(String)} or
 * {@link VoiceLiveAsyncClient#startSession(VoiceLiveSessionOptions)} and work directly with it for optimal performance.
 * Alternatively, users can use the convenience methods on {@link VoiceLiveAsyncClient} which delegate to
 * the internal session.
 * </p>
 *
 * <p><strong>Thread Safety:</strong></p>
 * This class is thread-safe and supports concurrent operations.
 *
 * <p><strong>Resource Management:</strong></p>
 * Sessions should be properly closed using {@link #close()} or {@link #closeAsync()} to release resources.
 */
public final class VoiceLiveSession implements AsyncCloseable, AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(VoiceLiveSession.class);
    private static final String COGNITIVE_SERVICES_SCOPE = "https://cognitiveservices.azure.com/.default";
    private static final HttpHeaderName API_KEY = HttpHeaderName.fromString("api-key");

    // WebSocket configuration constants
    private static final String WEBSOCKET_PROTOCOL = "realtime";
    private static final int MAX_FRAME_SIZE_BYTES = 10 * 1024 * 1024; // 10MB for large audio messages
    private static final int FRAME_AGGREGATION_SIZE_BYTES = 10 * 1024 * 1024; // 10MB for frame aggregation
    private static final int INBOUND_BUFFER_CAPACITY = 1024;

    private static final WebsocketClientSpec WEBSOCKET_CLIENT_SPEC = WebsocketClientSpec.builder()
        .protocols(WEBSOCKET_PROTOCOL)
        .maxFramePayloadLength(MAX_FRAME_SIZE_BYTES)
        .build();

    private final URI endpoint;
    private final AzureKeyCredential keyCredential;
    private final TokenCredential tokenCredential;
    private final SerializerAdapter serializer;

    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final AtomicBoolean isSendingAudioStream = new AtomicBoolean(false);

    // Reactive sinks for bidirectional message flow
    private final Sinks.Many<BinaryData> receiveSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<WebSocketFrame> sendSink = Sinks.many().multicast().onBackpressureBuffer();

    // WebSocket connection state management
    private final AtomicReference<WebsocketInbound> inboundRef = new AtomicReference<>();
    private final AtomicReference<WebsocketOutbound> outboundRef = new AtomicReference<>();
    private final AtomicReference<Sinks.One<Void>> connectionCloseSignalRef = new AtomicReference<>();

    // Subscription lifecycle management
    private final AtomicReference<Disposable> receiveSubscriptionRef = new AtomicReference<>();
    private final AtomicReference<Disposable> sendSubscriptionRef = new AtomicReference<>();
    private final AtomicReference<Disposable> closeStatusSubscriptionRef = new AtomicReference<>();
    private final AtomicReference<Disposable> connectionLifecycleSubscriptionRef = new AtomicReference<>();

    /**
     * Creates a new VoiceLiveSession with API key authentication.
     *
     * @param endpoint The WebSocket endpoint.
     * @param keyCredential The API key credential.
     */
    VoiceLiveSession(URI endpoint, AzureKeyCredential keyCredential) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null");
        this.keyCredential = Objects.requireNonNull(keyCredential, "'keyCredential' cannot be null");
        this.tokenCredential = null;
        this.serializer = JacksonAdapter.createDefaultSerializerAdapter();
    }

    /**
     * Creates a new VoiceLiveSession with token authentication.
     *
     * @param endpoint The WebSocket endpoint.
     * @param tokenCredential The token credential.
     */
    VoiceLiveSession(URI endpoint, TokenCredential tokenCredential) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null");
        this.keyCredential = null;
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null");
        this.serializer = JacksonAdapter.createDefaultSerializerAdapter();
    }

    /**
     * Connects to the VoiceLive service WebSocket endpoint.
     *
     * @param additionalHeaders Additional headers to include in the connection request.
     * @return A Mono that completes when the connection is established.
     */
    Mono<Void> connect(HttpHeaders additionalHeaders) {
        if (isConnected.get()) {
            return Mono.error(new IllegalStateException("Session is already connected"));
        }

        if (isClosed.get()) {
            return Mono.error(new IllegalStateException("Session has already been closed"));
        }

        if (connectionCloseSignalRef.get() != null) {
            return Mono.error(new IllegalStateException("Session lifecycle already active"));
        }

        Sinks.One<Void> readySink = Sinks.one();
        Sinks.One<Void> closeSignal = Sinks.one();
        connectionCloseSignalRef.set(closeSignal);
        Mono<Void> connectionMono
            = getAuthorizationHeaders().map(authHeaders -> buildConnectionHeaders(authHeaders, additionalHeaders))
                .flatMap(requestHeaders -> {
                    logConnectionDetails(requestHeaders);
                    return HttpClient.create().followRedirect(true).headers(nettyHeaders -> {
                        for (HttpHeader header : requestHeaders) {
                            nettyHeaders.set(header.getName(), header.getValue());
                        }
                    }).websocket(WEBSOCKET_CLIENT_SPEC).uri(endpoint.toString()).handle((inbound, outbound) -> {
                        inboundRef.set(inbound);
                        outboundRef.set(outbound);
                        isConnected.set(true);

                        LOGGER.info("WebSocket connection established");

                        // CRITICAL FIX: Set frame aggregation to handle large audio delta messages
                        // Without this, Netty's default 64KB limit causes "content length exceeded 65536 bytes" errors
                        inbound.aggregateFrames(FRAME_AGGREGATION_SIZE_BYTES);
                        Flux<BinaryData> receiveFlux = inbound.receive()
                            .retain()
                            .doOnSubscribe(s -> LOGGER.info("Receive flux subscribed"))
                            .map(this::byteBufToBinaryData)
                            .doOnNext(data -> {
                                receiveSink.tryEmitNext(data);
                            })
                            .doOnError(error -> {
                                LOGGER.error("Error receiving message", error);
                                receiveSink.tryEmitError(error);
                                closeSignal.tryEmitError(error);
                            })
                            .doOnComplete(() -> {
                                LOGGER.info("Receive flux completed normally");
                            })
                            .doOnCancel(() -> {
                                LOGGER.info("Receive flux cancelled");
                            })
                            .doFinally(signalType -> {
                                LOGGER.info("WebSocket receive stream completed: {}", signalType);
                                isConnected.set(false);
                                receiveSink.tryEmitComplete();
                                closeSignal.tryEmitEmpty();
                            });

                        Disposable receiveSubscription = receiveFlux.subscribe();
                        receiveSubscriptionRef.set(receiveSubscription);

                        Disposable closeStatusSubscription = inbound.receiveCloseStatus()
                            .subscribe(
                                status -> LOGGER.info("WebSocket close status received: code={} reason={}",
                                    status.code(), status.reasonText()),
                                error -> LOGGER.warning("Failed to read WebSocket close status", error));
                        closeStatusSubscriptionRef.set(closeStatusSubscription);

                        Flux<WebSocketFrame> sendFlux = sendSink.asFlux()
                            .doOnSubscribe(subscription -> LOGGER.info("Send stream subscribed"))
                            .doOnCancel(() -> LOGGER.info("Send stream cancelled"))
                            .doOnComplete(() -> LOGGER.info("Send stream completed"))
                            .doOnError(error -> LOGGER.error("Error in send stream", error))
                            .concatWith(Mono.never());  // Keep flux alive - never complete until cancelled

                        // Send frames without completing when the flux completes
                        // The connection stays open until closeSignal triggers
                        outbound.sendObject(sendFlux).then().subscribe();

                        readySink.tryEmitEmpty();

                        return closeSignal.asMono().doFinally(signalType -> {
                            LOGGER.info("WebSocket handler closing: {}", signalType);
                            Disposable send = sendSubscriptionRef.getAndSet(null);
                            if (send != null && !send.isDisposed()) {
                                send.dispose();
                            }
                            Disposable receive = receiveSubscriptionRef.getAndSet(null);
                            if (receive != null && !receive.isDisposed()) {
                                receive.dispose();
                            }
                            Disposable closeStatus = closeStatusSubscriptionRef.getAndSet(null);
                            if (closeStatus != null && !closeStatus.isDisposed()) {
                                closeStatus.dispose();
                            }
                            connectionCloseSignalRef.compareAndSet(closeSignal, null);
                        });
                    }).then();
                });

        Disposable lifecycle = connectionMono.subscribe(unused -> {
        }, error -> {
            LOGGER.error("WebSocket connection error", error);
            isConnected.set(false);
            receiveSink.tryEmitError(error);
            readySink.tryEmitError(error);
            connectionCloseSignalRef.compareAndSet(closeSignal, null);
            disposeLifecycleSubscription();
        }, () -> {
            LOGGER.info("WebSocket handler completed");
            connectionCloseSignalRef.compareAndSet(closeSignal, null);
            disposeLifecycleSubscription();
        });

        connectionLifecycleSubscriptionRef.set(lifecycle);

        return readySink.asMono()
            .doOnSuccess(v -> LOGGER.info("WebSocket session ready"))
            .doOnError(error -> LOGGER.error("Failed to establish WebSocket connection", error));
        // Note: removed doFinally handler that was incorrectly closing connection on cancel
    }

    @Override
    public Mono<Void> closeAsync() {
        if (isClosed.compareAndSet(false, true)) {
            LOGGER.info("Closing VoiceLive session");
            sendSink.tryEmitComplete();

            Sinks.One<Void> closeSignal = connectionCloseSignalRef.getAndSet(null);
            if (closeSignal != null) {
                closeSignal.tryEmitEmpty();
            }

            Disposable send = sendSubscriptionRef.getAndSet(null);
            if (send != null && !send.isDisposed()) {
                send.dispose();
            }

            Disposable receive = receiveSubscriptionRef.getAndSet(null);
            if (receive != null && !receive.isDisposed()) {
                receive.dispose();
            }

            Disposable closeStatus = closeStatusSubscriptionRef.getAndSet(null);
            if (closeStatus != null && !closeStatus.isDisposed()) {
                closeStatus.dispose();
            }

            disposeLifecycleSubscription();

            WebsocketOutbound outbound = outboundRef.get();
            if (outbound != null) {
                return outbound.sendClose().doOnSuccess(v -> {
                    isConnected.set(false);
                    receiveSink.tryEmitComplete();
                    LOGGER.info("WebSocket connection closed");
                })
                    .doOnError(error -> LOGGER.error("Error closing WebSocket", error))
                    .onErrorResume(e -> Mono.empty())
                    .then();
            }
            isConnected.set(false);
            receiveSink.tryEmitComplete();
        }

        return Mono.empty();
    }

    @Override
    public void close() {
        LOGGER.info("Closing VoiceLive session");
        this.closeAsync().block();
    }

    /**
     * Checks if the session is currently connected.
     *
     * @return true if connected, false otherwise.
     */
    public boolean isConnected() {
        return isConnected.get();
    }

    /**
     * Sends an event to the service.
     *
     * @param event The client event to send.
     * @return A Mono that completes when the command is sent.
     */
    public Mono<Void> sendEvent(ClientEvent event) {
        Objects.requireNonNull(event, "'event' cannot be null");
        throwIfNotConnected();

        return Mono.fromCallable(() -> {
            try {
                String json = serializer.serialize(event, SerializerEncoding.JSON);
                return BinaryData.fromString(json);
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException("Failed to serialize event", e));
            }
        }).flatMap(this::send);
    }

    /**
     * Sends binary data to the service.
     *
     * @param data The binary data to send.
     * @return A Mono that completes when the data is sent.
     */
    public Mono<Void> send(BinaryData data) {
        Objects.requireNonNull(data, "'data' cannot be null");
        throwIfNotConnected();

        return Mono.fromRunnable(() -> {
            byte[] bytes = data.toBytes();
            ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
            TextWebSocketFrame frame = new TextWebSocketFrame(buffer);

            Sinks.EmitResult result = sendSink.tryEmitNext(frame);
            if (result.isFailure()) {
                buffer.release(); // Release buffer if send failed
                throw LOGGER.logExceptionAsError(new RuntimeException("Failed to send message: " + result));
            }
        });
    }

    /**
     * Receives parsed events from the service as strongly-typed SessionUpdate objects.
     * <p>
     * This method provides a higher-level alternative to {@link #receive()} by automatically
     * parsing the raw BinaryData into the appropriate SessionUpdate subclass based on the
     * event type. This enables type-safe event handling and better developer experience.
     * </p>
     * <p>
     * Events that cannot be parsed will be logged and skipped, ensuring the stream continues
     * to operate even if unknown event types are received.
     * </p>
     *
     * @return A Flux of SessionUpdate objects representing parsed server events.
     */
    public Flux<SessionUpdate> receiveEvents() {
        throwIfNotConnected();
        return receive()
            .onBackpressureBuffer(INBOUND_BUFFER_CAPACITY,
                dropped -> LOGGER.error("Inbound buffer overflow; dropped {} bytes", dropped.toBytes().length),
                BufferOverflowStrategy.ERROR)
            .flatMap(this::parseToSessionUpdate)
            .doOnError(error -> LOGGER.error("Failed to parse session update", error))
            .onErrorResume(error -> {
                LOGGER.warning("Skipping unparseable event due to error: {}", error.getMessage());
                return Flux.empty();
            });
    }

    /**
     * Receives messages from the service as a stream of binary data.
     *
     * @return A Flux of BinaryData representing server events.
     */
    private Flux<BinaryData> receive() {
        throwIfNotConnected();
        return receiveSink.asFlux();
    }

    /**
     * Parses raw BinaryData into a SessionUpdate object.
     * <p>
     * The generated code now uses JsonReaderHelper to avoid bufferObject() issues.
     * This method simply delegates to SessionUpdate.fromJson() for polymorphic deserialization.
     * </p>
     *
     * @param data The raw binary data from the service.
     * @return A Mono containing the parsed SessionUpdate, or empty if parsing fails.
     */
    private Mono<SessionUpdate> parseToSessionUpdate(BinaryData data) {
        return Mono.fromCallable(() -> {
            try {
                return SessionUpdate.fromJson(com.azure.json.JsonProviders.createReader(data.toString()));
            } catch (IOException e) {
                LOGGER.atError().addKeyValue("error", e.getMessage()).log("Failed to parse SessionUpdate");
                return null;
            }
        }).filter(Objects::nonNull).subscribeOn(Schedulers.boundedElastic());
    }

    // ============================================================================
    // Audio Data Transmission
    // ============================================================================

    /**
     * Transmits audio data from a byte array.
     *
     * @param audio The audio data to transmit.
     * @return A Mono that completes when the audio is sent.
     * @throws IllegalStateException if another audio stream is already being sent.
     */
    public Mono<Void> sendInputAudio(byte[] audio) {
        Objects.requireNonNull(audio, "'audio' cannot be null");
        throwIfNotConnected();

        if (!isSendingAudioStream.compareAndSet(false, true)) {
            return Mono.error(new IllegalStateException(
                "Cannot send a standalone audio chunk while a stream is already in progress."));
        }

        String base64Audio = Base64.getEncoder().encodeToString(audio);
        ClientEventInputAudioBufferAppend appendCommand = new ClientEventInputAudioBufferAppend(base64Audio);

        return sendEvent(appendCommand).doFinally(signal -> isSendingAudioStream.set(false));
    }

    /**
     * Transmits audio data from BinaryData.
     *
     * @param audio The audio data to transmit.
     * @return A Mono that completes when the audio is sent.
     * @throws IllegalStateException if another audio stream is already being sent.
     */
    public Mono<Void> sendInputAudio(BinaryData audio) {
        Objects.requireNonNull(audio, "'audio' cannot be null");
        return sendInputAudio(audio.toBytes());
    }

    /**
     * Clears the input audio buffer.
     *
     * @return A Mono that completes when the buffer is cleared.
     */
    public Mono<Void> clearInputAudio() {
        throwIfNotConnected();
        ClientEventInputAudioBufferClear clearCommand = new ClientEventInputAudioBufferClear();
        return sendEvent(clearCommand);
    }

    /**
     * Commits the input audio buffer.
     *
     * @return A Mono that completes when the buffer is committed.
     */
    public Mono<Void> commitInputAudio() {
        throwIfNotConnected();
        ClientEventInputAudioBufferCommit commitCommand = new ClientEventInputAudioBufferCommit();
        return sendEvent(commitCommand);
    }

    /**
     * Clears all input audio currently being streamed.
     *
     * @return A Mono that completes when the streaming audio is cleared.
     */
    public Mono<Void> clearStreamingAudio() {
        throwIfNotConnected();
        ClientEventInputAudioClear clearCommand = new ClientEventInputAudioClear();
        return sendEvent(clearCommand);
    }

    // ============================================================================
    // Audio Turn Management
    // ============================================================================

    /**
     * Starts a new audio input turn.
     *
     * @param turnId Unique identifier for the input audio turn.
     * @return A Mono that completes when the turn is started.
     * @throws IllegalArgumentException if turnId is null or empty.
     */
    public Mono<Void> startAudioTurn(String turnId) {
        if (turnId == null || turnId.isEmpty()) {
            return Mono.error(new IllegalArgumentException("'turnId' cannot be null or empty"));
        }
        throwIfNotConnected();

        ClientEventInputAudioTurnStart startCommand = new ClientEventInputAudioTurnStart(turnId);
        return sendEvent(startCommand);
    }

    /**
     * Appends audio data to an ongoing input turn.
     *
     * @param turnId The ID of the turn this audio is part of.
     * @param audio The audio data to append.
     * @return A Mono that completes when the audio is appended.
     * @throws IllegalArgumentException if turnId is null or empty, or audio is null.
     */
    public Mono<Void> appendAudioToTurn(String turnId, byte[] audio) {
        if (turnId == null || turnId.isEmpty()) {
            return Mono.error(new IllegalArgumentException("'turnId' cannot be null or empty"));
        }
        Objects.requireNonNull(audio, "'audio' cannot be null");
        throwIfNotConnected();

        String base64Audio = Base64.getEncoder().encodeToString(audio);
        ClientEventInputAudioTurnAppend appendCommand = new ClientEventInputAudioTurnAppend(turnId, base64Audio);
        return sendEvent(appendCommand);
    }

    /**
     * Appends audio data to an ongoing input turn.
     *
     * @param turnId The ID of the turn this audio is part of.
     * @param audio The audio data to append.
     * @return A Mono that completes when the audio is appended.
     * @throws IllegalArgumentException if turnId is null or empty, or audio is null.
     */
    public Mono<Void> appendAudioToTurn(String turnId, BinaryData audio) {
        Objects.requireNonNull(audio, "'audio' cannot be null");
        return appendAudioToTurn(turnId, audio.toBytes());
    }

    /**
     * Marks the end of an audio input turn.
     *
     * @param turnId The ID of the audio turn being ended.
     * @return A Mono that completes when the turn is ended.
     * @throws IllegalArgumentException if turnId is null or empty.
     */
    public Mono<Void> endAudioTurn(String turnId) {
        if (turnId == null || turnId.isEmpty()) {
            return Mono.error(new IllegalArgumentException("'turnId' cannot be null or empty"));
        }
        throwIfNotConnected();

        ClientEventInputAudioTurnEnd endCommand = new ClientEventInputAudioTurnEnd(turnId);
        return sendEvent(endCommand);
    }

    /**
     * Cancels an in-progress input audio turn.
     *
     * @param turnId The ID of the turn to cancel.
     * @return A Mono that completes when the turn is cancelled.
     * @throws IllegalArgumentException if turnId is null or empty.
     */
    public Mono<Void> cancelAudioTurn(String turnId) {
        if (turnId == null || turnId.isEmpty()) {
            return Mono.error(new IllegalArgumentException("'turnId' cannot be null or empty"));
        }
        throwIfNotConnected();

        ClientEventInputAudioTurnCancel cancelCommand = new ClientEventInputAudioTurnCancel(turnId);
        return sendEvent(cancelCommand);
    }

    // ============================================================================
    // Session Configuration
    // ============================================================================

    /**
     * Updates the session configuration.
     *
     * @param sessionOptions The session configuration options.
     * @return A Mono that completes when the configuration is updated.
     */
    public Mono<Void> configureSession(VoiceLiveSessionOptions sessionOptions) {
        Objects.requireNonNull(sessionOptions, "'sessionOptions' cannot be null");
        throwIfNotConnected();

        ClientEventSessionUpdate updateCommand = new ClientEventSessionUpdate(sessionOptions);
        return sendEvent(updateCommand);
    }

    // ============================================================================
    // Item Management
    // ============================================================================

    /**
     * Adds an item to the conversation.
     *
     * @param item The item to add to the conversation.
     * @return A Mono that completes when the item is added.
     */
    public Mono<Void> addItem(ConversationRequestItem item) {
        return addItem(item, null);
    }

    /**
     * Adds an item to the conversation at a specific position.
     *
     * @param item The item to add to the conversation.
     * @param previousItemId The ID of the item after which to insert the new item.
     * @return A Mono that completes when the item is added.
     */
    public Mono<Void> addItem(ConversationRequestItem item, String previousItemId) {
        Objects.requireNonNull(item, "'item' cannot be null");
        throwIfNotConnected();

        ClientEventConversationItemCreate itemCreate
            = new ClientEventConversationItemCreate().setItem(item).setPreviousItemId(previousItemId);
        return sendEvent(itemCreate);
    }

    /**
     * Retrieves an item from the conversation.
     *
     * @param itemId The ID of the item to retrieve.
     * @return A Mono that completes when the retrieval request is sent.
     * @throws IllegalArgumentException if itemId is null or empty.
     */
    public Mono<Void> requestItemRetrieval(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return Mono.error(new IllegalArgumentException("'itemId' cannot be null or empty"));
        }
        throwIfNotConnected();

        ClientEventConversationItemRetrieve retrieveCommand = new ClientEventConversationItemRetrieve(itemId);
        return sendEvent(retrieveCommand);
    }

    /**
     * Deletes an item from the conversation.
     *
     * @param itemId The ID of the item to delete.
     * @return A Mono that completes when the item is deleted.
     * @throws IllegalArgumentException if itemId is null or empty.
     */
    public Mono<Void> deleteItem(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return Mono.error(new IllegalArgumentException("'itemId' cannot be null or empty"));
        }
        throwIfNotConnected();

        ClientEventConversationItemDelete deleteCommand = new ClientEventConversationItemDelete(itemId);
        return sendEvent(deleteCommand);
    }

    /**
     * Truncates the conversation history.
     *
     * @param itemId The ID of the item up to which to truncate the conversation.
     * @param contentIndex The content index within the item to truncate to.
     * @param audioEndMilliseconds Inclusive duration up to which audio is truncated (in milliseconds).
     * @return A Mono that completes when the conversation is truncated.
     * @throws IllegalArgumentException if itemId is null or empty.
     */
    public Mono<Void> truncateConversation(String itemId, int contentIndex, int audioEndMilliseconds) {
        if (itemId == null || itemId.isEmpty()) {
            return Mono.error(new IllegalArgumentException("'itemId' cannot be null or empty"));
        }
        throwIfNotConnected();

        ClientEventConversationItemTruncate truncateEvent
            = new ClientEventConversationItemTruncate(itemId, contentIndex, audioEndMilliseconds);
        return sendEvent(truncateEvent);
    }

    /**
     * Truncates the conversation history.
     *
     * @param itemId The ID of the item up to which to truncate the conversation.
     * @param contentIndex The content index within the item to truncate to.
     * @return A Mono that completes when the conversation is truncated.
     * @throws IllegalArgumentException if itemId is null or empty.
     */
    public Mono<Void> truncateConversation(String itemId, int contentIndex) {
        return truncateConversation(itemId, contentIndex, 0);
    }

    // ============================================================================
    // Response Management
    // ============================================================================

    /**
     * Starts a new response generation.
     *
     * @return A Mono that completes when the response generation starts.
     */
    public Mono<Void> startResponse() {
        return startResponse((VoiceLiveSessionOptions) null);
    }

    /**
     * Starts a new response generation with specific options.
     *
     * @param responseOptions The options for response generation.
     * @return A Mono that completes when the response generation starts.
     */
    public Mono<Void> startResponse(VoiceLiveSessionOptions responseOptions) {
        throwIfNotConnected();

        ClientEventResponseCreate responseEvent = new ClientEventResponseCreate();
        if (responseOptions != null && responseOptions.getInstructions() != null) {
            responseEvent.setAdditionalInstructions(responseOptions.getInstructions());
        }

        return sendEvent(responseEvent);
    }

    /**
     * Starts a new response generation with additional instructions.
     *
     * @param additionalInstructions Additional instructions for this response.
     * @return A Mono that completes when the response generation starts.
     */
    public Mono<Void> startResponse(String additionalInstructions) {
        Objects.requireNonNull(additionalInstructions, "'additionalInstructions' cannot be null");
        throwIfNotConnected();

        ClientEventResponseCreate response = new ClientEventResponseCreate();
        response.setAdditionalInstructions(additionalInstructions);

        return sendEvent(response);
    }

    /**
     * Cancels the current response generation.
     *
     * @return A Mono that completes when the response is cancelled.
     */
    public Mono<Void> cancelResponse() {
        throwIfNotConnected();

        ClientEventResponseCancel cancelCommand = new ClientEventResponseCancel();
        return sendEvent(cancelCommand);
    }

    // ============================================================================
    // Avatar Management
    // ============================================================================

    /**
     * Connects and provides the client's SDP (Session Description Protocol) for avatar-related media negotiation.
     *
     * @param clientSdp The client's SDP offer.
     * @return A Mono that completes when the avatar connection is established.
     * @throws IllegalArgumentException if clientSdp is null or empty.
     */
    public Mono<Void> connectAvatar(String clientSdp) {
        if (clientSdp == null || clientSdp.isEmpty()) {
            return Mono.error(new IllegalArgumentException("'clientSdp' cannot be null or empty"));
        }
        throwIfNotConnected();

        ClientEventSessionAvatarConnect avatarConnectCommand = new ClientEventSessionAvatarConnect(clientSdp);
        return sendEvent(avatarConnectCommand);
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private void logConnectionDetails(HttpHeaders headers) {
        if (headers == null || !headers.iterator().hasNext()) {
            LOGGER.info("WebSocket connection parameters -> endpoint: {}", endpoint);
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (HttpHeader header : headers) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            HttpHeaderName headerName = HttpHeaderName.fromString(header.getName());
            builder.append(headerName).append('=').append(sanitizeHeaderValue(headerName, header.getValue()));
        }

        LOGGER.info("WebSocket connection parameters -> endpoint: {} headers: {}", endpoint, builder);
    }

    private String sanitizeHeaderValue(HttpHeaderName headerName, String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        String normalized = headerName.toString().toLowerCase(Locale.ROOT);
        if ("api-key".equals(normalized)) {
            return maskValue(value);
        }
        if ("authorization".equals(normalized)) {
            int spaceIndex = value.indexOf(' ');
            if (spaceIndex > 0 && spaceIndex + 1 < value.length()) {
                return value.substring(0, spaceIndex + 1) + maskValue(value.substring(spaceIndex + 1));
            }
            return maskValue(value);
        }

        return value;
    }

    private String maskValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.length() <= 8) {
            return "****";
        }
        return value.substring(0, 4) + "..." + value.substring(value.length() - 4);
    }

    private HttpHeaders buildConnectionHeaders(HttpHeaders authHeaders, HttpHeaders additionalHeaders) {
        HttpHeaders requestHeaders = new HttpHeaders();

        if (authHeaders != null) {
            for (HttpHeader header : authHeaders) {
                HttpHeaderName headerName = HttpHeaderName.fromString(header.getName());
                requestHeaders.set(headerName, header.getValue());
            }
        }

        if (additionalHeaders != null) {
            for (HttpHeader header : additionalHeaders) {
                HttpHeaderName headerName = HttpHeaderName.fromString(header.getName());
                requestHeaders.set(headerName, header.getValue());
            }
        }

        // VoiceLive service doesn't need OpenAI-Beta header
        // Only add it if explicitly provided in additionalHeaders

        return requestHeaders;
    }

    private void disposeLifecycleSubscription() {
        Disposable lifecycle = connectionLifecycleSubscriptionRef.getAndSet(null);
        if (lifecycle != null && !lifecycle.isDisposed()) {
            lifecycle.dispose();
        }
    }

    /**
     * Gets authorization headers based on the configured credential.
     *
     * @return A Mono containing the HttpHeaders with authorization.
     */
    private Mono<HttpHeaders> getAuthorizationHeaders() {
        HttpHeaders headers = new HttpHeaders();

        if (keyCredential != null) {
            headers.set(API_KEY, keyCredential.getKey());
            return Mono.just(headers);
        } else if (tokenCredential != null) {
            TokenRequestContext tokenRequest = new TokenRequestContext().addScopes(COGNITIVE_SERVICES_SCOPE);
            return tokenCredential.getToken(tokenRequest).map(at -> {
                headers.set(HttpHeaderName.AUTHORIZATION, "Bearer " + at.getToken());
                return headers;
            });
        }

        return Mono.error(new IllegalStateException("No credential configured"));
    }

    /**
     * Converts a ByteBuf to BinaryData.
     *
     * @param byteBuf The ByteBuf to convert.
     * @return BinaryData representation.
     */
    private BinaryData byteBufToBinaryData(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        byteBuf.release();
        return BinaryData.fromBytes(bytes);
    }

    /**
     * Throws an exception if the session is not connected.
     */
    private void throwIfNotConnected() {
        if (!isConnected.get()) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("Session is not connected"));
        }
        if (isClosed.get()) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("Session is closed"));
        }
    }
}
