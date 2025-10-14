// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.ClientEvent;
import com.azure.ai.voicelive.models.ClientEventInputAudioBufferAppend;
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
 */
public final class VoiceLiveSession implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(VoiceLiveSession.class);
    private static final int AUDIO_BUFFER_SIZE = 16 * 1024; // 16KB chunks
    private static final String COGNITIVE_SERVICES_SCOPE = "https://cognitiveservices.azure.com/.default";
    private static final HttpHeaderName API_KEY = HttpHeaderName.fromString("api-key");
    private final VoiceLiveAsyncClient parentClient;
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
     * @param parentClient The parent VoiceLiveAsyncClient.
     * @param endpoint The WebSocket endpoint.
     * @param keyCredential The API key credential.
     */
    VoiceLiveSession(VoiceLiveAsyncClient parentClient, URI endpoint, AzureKeyCredential keyCredential) {
        this.parentClient = Objects.requireNonNull(parentClient, "'parentClient' cannot be null");
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null");
        this.keyCredential = Objects.requireNonNull(keyCredential, "'keyCredential' cannot be null");
        this.tokenCredential = null;
        this.serializer = JacksonAdapter.createDefaultSerializerAdapter();
    }

    /**
     * Creates a new VoiceLiveSession with token authentication.
     *
     * @param parentClient The parent VoiceLiveAsyncClient.
     * @param endpoint The WebSocket endpoint.
     * @param tokenCredential The token credential.
     */
    VoiceLiveSession(VoiceLiveAsyncClient parentClient, URI endpoint, TokenCredential tokenCredential) {
        this.parentClient = Objects.requireNonNull(parentClient, "'parentClient' cannot be null");
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
            HttpClient httpClient = HttpClient.create();

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
     * Sends input audio from a stream to the service.
     *
     * @param audioStream The audio input stream.
     * @return A Mono that completes when all audio has been sent.
     */
    public Mono<Void> sendInputAudio(InputStream audioStream) {
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
        })
            .flatMap(event -> sendCommand((ClientEvent) event))
            .then()
            .doFinally(signal -> isSendingAudioStream.set(false));
    }

    /**
     * Sends a command event to the service.
     *
     * @param command The client event to send.
     * @return A Mono that completes when the command is sent.
     */
    public Mono<Void> sendCommand(ClientEvent command) {
        Objects.requireNonNull(command, "'command' cannot be null");
        throwIfNotConnected();

        return Mono.fromCallable(() -> {
            try {
                String json = serializer.serialize(command, SerializerEncoding.JSON);
                return BinaryData.fromString(json);
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException("Failed to serialize command", e));
            }
        }).flatMap(this::sendCommand);
    }

    /**
     * Sends raw binary data to the service.
     *
     * @param data The binary data to send.
     * @return A Mono that completes when the data is sent.
     */
    public Mono<Void> sendCommand(BinaryData data) {
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
     * Receives updates from the service as a stream of binary data.
     *
     * @return A Flux of BinaryData representing server events.
     */
    public Flux<BinaryData> receiveUpdates() {
        throwIfNotConnected();
        return receiveSink.asFlux();
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
     * Closes the WebSocket connection gracefully.
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
        closeAsync().block();
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
