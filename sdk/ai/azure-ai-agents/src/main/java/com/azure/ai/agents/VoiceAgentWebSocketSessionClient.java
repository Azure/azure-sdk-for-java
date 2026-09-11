// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketClientConfiguration;
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
import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A synchronous bidirectional realtime session connected to a Foundry voice agent.
 */
@Beta(warningText = "This class is in preview and may change in future releases.")
public final class VoiceAgentWebSocketSessionClient implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(VoiceAgentWebSocketSessionClient.class);
    private final URI websocketUri;
    private final VoiceAgentWebSocketConnectionOptions options;
    private final OkHttpClient httpClient;
    private final BlockingQueue<EventSignal> events
        = new ArrayBlockingQueue<>(VoiceAgentWebSocketUtils.INBOUND_CAPACITY + 1);
    private final CountDownLatch handshakeCompleted = new CountDownLatch(1);
    private final CountDownLatch closeCompleted = new CountDownLatch(1);
    private final AtomicReference<Throwable> connectionError = new AtomicReference<>();
    private final AtomicBoolean receiveClaimed = new AtomicBoolean();
    private final AtomicBoolean open = new AtomicBoolean();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicBoolean clientShutdown = new AtomicBoolean();

    private volatile WebSocket webSocket;
    private volatile Integer closeCode;
    private volatile String closeReason;

    private VoiceAgentWebSocketSessionClient(VoiceAgentWebSocketClientConfiguration configuration, String agentName,
        VoiceAgentWebSocketConnectionOptions options) {
        this.options = options;
        this.websocketUri = VoiceAgentWebSocketUtils.buildWebSocketUri(configuration, agentName, options);
        String token = configuration.getCredential()
            .getTokenSync(new TokenRequestContext().addScopes(VoiceAgentWebSocketUtils.TOKEN_SCOPE))
            .getToken();
        this.httpClient = createHttpClient(configuration, options);
        Request.Builder request = new Request.Builder().url(websocketUri.toString())
            .header("Authorization", "Bearer " + token)
            .header("User-Agent", configuration.getUserAgent())
            .header("Foundry-Features", VoiceAgentWebSocketUtils.PREVIEW_FEATURE)
            .header("Sec-WebSocket-Protocol", VoiceAgentWebSocketUtils.SUBPROTOCOL);
        if (configuration.getHeaders() != null) {
            for (HttpHeader header : configuration.getHeaders()) {
                if (!VoiceAgentWebSocketUtils.isProtectedHeader(header.getName())) {
                    request.header(header.getName(), header.getValue());
                }
            }
        }
        this.webSocket = httpClient.newWebSocket(request.build(), new Listener());
    }

    static VoiceAgentWebSocketSessionClient connect(VoiceAgentWebSocketClientConfiguration configuration,
        String agentName, VoiceAgentWebSocketConnectionOptions options) {
        VoiceAgentWebSocketSessionClient session
            = new VoiceAgentWebSocketSessionClient(configuration, agentName, options);
        try {
            session.awaitHandshake();
            return session;
        } catch (RuntimeException error) {
            session.webSocket.cancel();
            session.shutdownHttpClient();
            throw error;
        }
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
     * Determines whether the session is open.
     *
     * @return {@code true} when the session is open.
     */
    public boolean isOpen() {
        return open.get() && !closed.get();
    }

    /**
     * Gets the peer close code.
     *
     * @return the close code, or {@code null}.
     */
    public Integer getCloseCode() {
        return closeCode;
    }

    /**
     * Gets the peer close reason.
     *
     * @return the close reason, or {@code null}.
     */
    public String getCloseReason() {
        return closeReason;
    }

    /**
     * Receives typed server events in wire order. The returned stream may be iterated once.
     *
     * @throws IllegalStateException if the event stream has already been claimed.
     * @return the server event stream.
     */
    public IterableStream<RealtimeServerEvent> receiveEvents() {
        if (!receiveClaimed.compareAndSet(false, true)) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Only one receiveEvents iterator is supported per session."));
        }
        return IterableStream.of(() -> new EventIterator(events));
    }

    /**
     * Sends a typed realtime client event.
     *
     * @param event the event to send.
    * @throws IllegalArgumentException if the event cannot be serialized.
    * @throws IllegalStateException if the session is closed or cannot accept another event.
     */
    public void sendEvent(RealtimeClientEvent event) {
        Objects.requireNonNull(event, "'event' cannot be null.");
        ensureOpen();
        final String json;
        try {
            json = event.toJsonString();
        } catch (IOException error) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Failed to serialize the realtime client event.", error));
        }
        if (!webSocket.send(json)) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("The voice-agent WebSocket send queue is full or closed."));
        }
    }

    /**
     * Adds a conversation item.
     *
     * @param item the item to add.
     */
    public void createConversationItem(RealtimeConversationItem item) {
        createConversationItem(item, null);
    }

    /**
     * Adds a conversation item after another item.
     *
     * @param item the item to add.
     * @param previousItemId the preceding item identifier.
     */
    public void createConversationItem(RealtimeConversationItem item, String previousItemId) {
        Objects.requireNonNull(item, "'item' cannot be null.");
        sendEvent(new RealtimeClientEventConversationItemCreate(item).setPreviousItemId(previousItemId));
    }

    /**
     * Adds a user text message.
     *
     * @param text the user message.
     */
    public void sendText(String text) {
        Objects.requireNonNull(text, "'text' cannot be null.");
        RealtimeConversationItemMessageUserContent content = new RealtimeConversationItemMessageUserContent()
            .setType(RealtimeConversationItemMessageUserContentType.INPUT_TEXT)
            .setText(text);
        createConversationItem(new RealtimeConversationItemMessageUser(Collections.singletonList(content)));
    }

    /**
     * Appends audio to the input buffer.
     *
     * @param audio the audio bytes.
     */
    public void appendInputAudio(BinaryData audio) {
        Objects.requireNonNull(audio, "'audio' cannot be null.");
        sendEvent(new RealtimeClientEventInputAudioBufferAppend(Base64.getEncoder().encodeToString(audio.toBytes())));
    }

    /** Clears the input audio buffer. */
    public void clearInputAudio() {
        sendEvent(new RealtimeClientEventInputAudioBufferClear());
    }

    /** Commits the input audio buffer. */
    public void commitInputAudio() {
        sendEvent(new RealtimeClientEventInputAudioBufferCommit());
    }

    /** Requests a response using the voice agent's configuration. */
    public void createResponse() {
        sendEvent(new RealtimeClientEventResponseCreate());
    }

    /**
     * Requests a response with per-response options.
     *
     * @param responseOptions response options.
     */
    public void createResponse(VoiceAgentResponseCreateParams responseOptions) {
        Objects.requireNonNull(responseOptions, "'responseOptions' cannot be null.");
        sendEvent(new RealtimeClientEventResponseCreate().setResponse(responseOptions));
    }

    /** Cancels the active response. */
    public void cancelResponse() {
        sendEvent(new RealtimeClientEventResponseCancel());
    }

    /**
     * Cancels a specific response.
     *
     * @param responseId the response identifier.
     */
    public void cancelResponse(String responseId) {
        Objects.requireNonNull(responseId, "'responseId' cannot be null.");
        sendEvent(new RealtimeClientEventResponseCancel().setResponseId(responseId));
    }

    /**
     * Sends a function-call result and requests the next response.
     *
     * @param callId the function call identifier.
     * @param output the serialized function output.
     */
    public void sendFunctionCallOutput(String callId, String output) {
        createConversationItem(new RealtimeConversationItemFunctionCallOutput(callId, output));
        createResponse();
    }

    /** Closes the session. */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            shutdownHttpClient();
            return;
        }
        open.set(false);
        if (!webSocket.close(1000, "")) {
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
        Throwable error = connectionError.get();
        if (error instanceof RuntimeException) {
            throw LOGGER.logExceptionAsError((RuntimeException) error);
        }
        if (error != null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Failed to open the voice-agent WebSocket session.", error));
        }
    }

    private void ensureOpen() {
        if (!isOpen()) {
            throw LOGGER
                .logExceptionAsError(new IllegalStateException("The voice-agent WebSocket session is not open."));
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

    private void signal(EventSignal signal) {
        if ((signal.event != null && events.size() >= VoiceAgentWebSocketUtils.INBOUND_CAPACITY)
            || !events.offer(signal)) {
            events.clear();
            events.offer(EventSignal.error(new IllegalStateException("Voice-agent receive buffer overflow.")));
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

    private static OkHttpClient createHttpClient(VoiceAgentWebSocketClientConfiguration configuration,
        VoiceAgentWebSocketConnectionOptions options) {
        OkHttpClient.Builder builder
            = new OkHttpClient.Builder().connectTimeout(options.getHandshakeTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .followRedirects(false);
        ProxyOptions proxyOptions = configuration.getProxyOptions();
        if (proxyOptions != null) {
            Proxy.Type proxyType = proxyOptions.getType() == ProxyOptions.Type.SOCKS4
                || proxyOptions.getType() == ProxyOptions.Type.SOCKS5 ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
            builder.proxy(new Proxy(proxyType, proxyOptions.getAddress()));
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
            open.set(true);
            handshakeCompleted.countDown();
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            try {
                signal(EventSignal.event(RealtimeServerEvent.fromJson(JsonProviders.createReader(text))));
            } catch (IOException | RuntimeException error) {
                fail(new IllegalArgumentException("Invalid JSON event.", error), null);
                webSocket.close(1007, "Invalid JSON event");
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            IllegalArgumentException error
                = new IllegalArgumentException("The voice-agent protocol requires JSON text frames.");
            fail(error, null);
            webSocket.close(1003, "Binary frames are not supported");
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

    private static final class EventSignal {
        private final RealtimeServerEvent event;
        private final Throwable error;
        private final boolean complete;

        private EventSignal(RealtimeServerEvent event, Throwable error, boolean complete) {
            this.event = event;
            this.error = error;
            this.complete = complete;
        }

        private static EventSignal event(RealtimeServerEvent event) {
            return new EventSignal(event, null, false);
        }

        private static EventSignal error(Throwable error) {
            return new EventSignal(null, error, false);
        }

        private static EventSignal complete() {
            return new EventSignal(null, null, true);
        }
    }

    private static final class EventIterator implements Iterator<RealtimeServerEvent> {
        private final BlockingQueue<EventSignal> events;
        private EventSignal next;

        private EventIterator(BlockingQueue<EventSignal> events) {
            this.events = events;
        }

        @Override
        public boolean hasNext() {
            if (next == null) {
                try {
                    next = events.take();
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
        public RealtimeServerEvent next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            RealtimeServerEvent event = next.event;
            next = null;
            return event;
        }
    }
}
