// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.ClientEvent;
import com.azure.ai.voicelive.models.ClientEventInputAudioBufferAppend;
import com.azure.ai.voicelive.models.SessionUpdate;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Base64;
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
 */
public final class VoiceLiveSession implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(VoiceLiveSession.class);
    private static final int AUDIO_BUFFER_SIZE = 16 * 1024; // 16KB chunks
    private static final String COGNITIVE_SERVICES_SCOPE = "https://cognitiveservices.azure.com/.default";
    private static final HttpHeaderName API_KEY = HttpHeaderName.fromString("api-key");
    private final URI endpoint;
    private final AzureKeyCredential keyCredential;
    private final TokenCredential tokenCredential;
    private final SerializerAdapter serializer;

    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final AtomicBoolean isSendingAudioStream = new AtomicBoolean(false);

    // Sink for receiving messages
    private final Sinks.Many<BinaryData> receiveSink = Sinks.many().multicast().onBackpressureBuffer();

    // Sink for sending messages
    private final Sinks.Many<WebSocketFrame> sendSink = Sinks.many().unicast().onBackpressureBuffer();

    // WebSocket references
    private final AtomicReference<WebsocketInbound> inboundRef = new AtomicReference<>();
    private final AtomicReference<WebsocketOutbound> outboundRef = new AtomicReference<>();

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

        return getAuthorizationHeaders().flatMap(authHeaders -> {
            HttpClient httpClient = HttpClient.create().followRedirect(true);

            return httpClient.headers(headers -> {
                // Add authentication headers
                authHeaders.forEach(header -> headers.set(header.getName(), header.getValue()));

                // Add additional headers
                if (additionalHeaders != null) {
                    additionalHeaders.forEach(header -> headers.set(header.getName(), header.getValue()));
                }

                // Add User-Agent
                headers.set("User-Agent", "Azure-VoiceLive-SDK/Java");
            }).websocket().uri(endpoint.toString()).handle((inbound, outbound) -> {
                inboundRef.set(inbound);
                outboundRef.set(outbound);
                isConnected.set(true);

                LOGGER.info("WebSocket connection established");

                // Setup receive stream - receive and convert ByteBuf to BinaryData
                Flux<BinaryData> receiveFlux = inbound.receive()
                    .retain() // Retain to prevent premature release
                    .map(this::byteBufToBinaryData)
                    .doOnNext(data -> {
                        LOGGER.verbose("Received message: {} bytes", data.toBytes().length);
                        receiveSink.tryEmitNext(data);
                    })
                    .doOnError(error -> {
                        LOGGER.error("Error receiving message", error);
                        receiveSink.tryEmitError(error);
                    })
                    .doFinally(signal -> {
                        LOGGER.info("WebSocket receive stream completed: {}", signal);
                        isConnected.set(false);
                        receiveSink.tryEmitComplete();
                    });

                // Setup send stream from sink
                Flux<WebSocketFrame> sendFlux = sendSink.asFlux()
                    .doOnNext(frame -> LOGGER.verbose("Sending WebSocket frame"))
                    .doOnError(error -> LOGGER.error("Error in send sink", error));

                // Keep connection alive and send/receive simultaneously
                return Mono.zip(outbound.sendObject(sendFlux).then(), receiveFlux.then()).then();
            }).doOnError(error -> {
                LOGGER.error("WebSocket connection error", error);
                isConnected.set(false);
            }).then();
        }).doOnSuccess(v -> LOGGER.info("WebSocket session ready")).doOnError(error -> {
            LOGGER.error("Failed to establish WebSocket connection", error);
            isConnected.set(false);
        });
    }

    /**
     * Sends audio from a stream to the service.
     *
     * @param audioStream The audio input stream.
     * @return A Mono that completes when all audio has been sent.
     */
    public Mono<Void> sendAudio(InputStream audioStream) {
        Objects.requireNonNull(audioStream, "'audioStream' cannot be null");
        throwIfNotConnected();

        if (!isSendingAudioStream.compareAndSet(false, true)) {
            return Mono.error(new IllegalStateException("Only one stream of audio may be sent at once"));
        }

        return Flux.create(sink -> {
            try {
                byte[] buffer = new byte[AUDIO_BUFFER_SIZE];
                int bytesRead;

                while ((bytesRead = audioStream.read(buffer)) != -1) {
                    if (isClosed.get()) {
                        sink.complete();
                        return;
                    }

                    byte[] audioChunk = new byte[bytesRead];
                    System.arraycopy(buffer, 0, audioChunk, 0, bytesRead);
                    String base64Audio = Base64.getEncoder().encodeToString(audioChunk);

                    ClientEventInputAudioBufferAppend appendCommand
                        = new ClientEventInputAudioBufferAppend(base64Audio);

                    sink.next(appendCommand);
                }
                sink.complete();
            } catch (IOException e) {
                sink.error(e);
            }
        }).flatMap(event -> sendEvent((ClientEvent) event)).then().doFinally(signal -> isSendingAudioStream.set(false));
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
        return receive().flatMap(this::parseToSessionUpdate)
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
     *
     * @param data The raw binary data from the service.
     * @return A Mono containing the parsed SessionUpdate.
     */
    private Mono<SessionUpdate> parseToSessionUpdate(BinaryData data) {
        return Mono.fromCallable(() -> {
            try {
                String jsonString = data.toString();
                LOGGER.verbose("Parsing session update: {}", jsonString);

                // Use the existing SessionUpdate.fromJson method for polymorphic deserialization
                try (JsonReader jsonReader = JsonProviders.createReader(jsonString)) {
                    return SessionUpdate.fromJson(jsonReader);
                }
            } catch (Exception e) {
                throw LOGGER.logExceptionAsError(new RuntimeException("Failed to parse SessionUpdate", e));
            }
        }).subscribeOn(Schedulers.boundedElastic());
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
     * Closes the session gracefully.
     *
     * @return A Mono that completes when the connection is closed.
     */
    public Mono<Void> closeAsync() {
        if (isClosed.compareAndSet(false, true)) {
            sendSink.tryEmitComplete();

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
        }

        return Mono.empty();
    }

    @Override
    public void close() {
        this.closeAsync().block();
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

            return Mono.fromCallable(() -> tokenCredential.getTokenSync(tokenRequest)).map(token -> {
                headers.set(HttpHeaderName.AUTHORIZATION, "Bearer " + token.getToken());
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
