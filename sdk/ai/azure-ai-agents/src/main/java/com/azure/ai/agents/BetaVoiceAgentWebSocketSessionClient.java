// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketClientConfiguration;
import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketHttpResponse;
import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketUtils;
import com.azure.ai.agents.implementation.utils.Beta;
import com.azure.ai.agents.models.RealtimeClientEvent;
import com.azure.ai.agents.models.RealtimeConversationItemCreateEvent;
import com.azure.ai.agents.models.RealtimeInputAudioBufferAppendEvent;
import com.azure.ai.agents.models.RealtimeInputAudioBufferClearEvent;
import com.azure.ai.agents.models.RealtimeInputAudioBufferCommitEvent;
import com.azure.ai.agents.models.RealtimeResponseCancelEvent;
import com.azure.ai.agents.models.RealtimeResponseCreateEvent;
import com.azure.ai.agents.models.RealtimeConversationItem;
import com.azure.ai.agents.models.RealtimeConversationItemFunctionCallOutput;
import com.azure.ai.agents.models.RealtimeConversationItemUserMessage;
import com.azure.ai.agents.models.RealtimeServerEvent;
import com.azure.ai.agents.models.VoiceAgentResponseCreateOptions;
import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.BinaryData;
import com.openai.models.realtime.RealtimeConversationItemUserMessage.Content;
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
import java.util.Base64;
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
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * A synchronous bidirectional realtime session connected to a Foundry voice agent.
 */
@Beta(warningText = "This class is in preview and may change in future releases.")
public final class BetaVoiceAgentWebSocketSessionClient implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(BetaVoiceAgentWebSocketSessionClient.class);
    private final URI websocketUri;
    private final VoiceAgentWebSocketConnectionOptions options;
    private final OkHttpClient httpClient;
    private final BlockingQueue<EventSignal> events;
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

    private BetaVoiceAgentWebSocketSessionClient(VoiceAgentWebSocketClientConfiguration configuration, String agentName,
        VoiceAgentWebSocketConnectionOptions options) {
        this.options = options;
        this.receiveBufferCapacity = options.getReceiveBufferCapacity();
        this.events = new ArrayBlockingQueue<>(receiveBufferCapacity + 1);
        this.websocketUri = VoiceAgentWebSocketUtils.buildWebSocketUri(configuration, agentName, options);
        String token = configuration.getCredential()
            .getTokenSync(VoiceAgentWebSocketUtils.createTokenRequestContext())
            .getToken();
        this.httpClient = createHttpClient(configuration, options);
        Request.Builder request = new Request.Builder().url(websocketUri.toString())
            .header("Sec-WebSocket-Protocol", VoiceAgentWebSocketUtils.SUBPROTOCOL);
        for (HttpHeader header : VoiceAgentWebSocketUtils.buildHeaders(configuration, options, token)) {
            request.header(header.getName(), header.getValue());
        }
        this.webSocket = httpClient.newWebSocket(request.build(), new Listener());
    }

    static BetaVoiceAgentWebSocketSessionClient connect(VoiceAgentWebSocketClientConfiguration configuration,
        String agentName, VoiceAgentWebSocketConnectionOptions options) {
        BetaVoiceAgentWebSocketSessionClient session
            = new BetaVoiceAgentWebSocketSessionClient(configuration, agentName, options);
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
        return receiveEvents(null);
    }

    /**
     * Receives events with a timeout for each wait. A timeout leaves the session open and the iterator can be retried.
     * @param timeout positive per-event timeout, or null to wait indefinitely.
     * @return the single-consumer event stream.
     * @throws IllegalArgumentException if timeout is zero or negative.
     * @throws IllegalStateException if another receiver exists or a wait times out (with a TimeoutException cause).
     */
    public IterableStream<RealtimeServerEvent> receiveEvents(Duration timeout) {
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
            return new EventIterator(events, timeout);
        });
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
     * Sends a JSON object, including event types and fields unknown to this SDK.
     * @param event the complete JSON event.
     * @throws IllegalArgumentException if the event is not a JSON object.
     * @throws IllegalStateException if the session cannot accept the event.
     */
    public void sendEvent(BinaryData event) {
        String json = VoiceAgentWebSocketUtils.validateEvent(event);
        ensureOpen();
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
        sendEvent(new RealtimeConversationItemCreateEvent(item).setPreviousItemId(previousItemId));
    }

    /**
     * Adds a user text message.
     *
     * @param text the user message.
     */
    public void sendText(String text) {
        Objects.requireNonNull(text, "'text' cannot be null.");
        Content content = Content.builder().type(Content.Type.INPUT_TEXT).text(text).build();
        createConversationItem(new RealtimeConversationItemUserMessage(Collections.singletonList(content)));
    }

    /**
     * Appends audio to the input buffer.
     *
     * @param audio the audio bytes.
     */
    public void appendInputAudio(BinaryData audio) {
        Objects.requireNonNull(audio, "'audio' cannot be null.");
        sendEvent(new RealtimeInputAudioBufferAppendEvent(Base64.getEncoder().encodeToString(audio.toBytes())));
    }

    /** Clears the input audio buffer. */
    public void clearInputAudio() {
        sendEvent(new RealtimeInputAudioBufferClearEvent());
    }

    /** Commits the input audio buffer. */
    public void commitInputAudio() {
        sendEvent(new RealtimeInputAudioBufferCommitEvent());
    }

    /** Requests a response using the voice agent's configuration. */
    public void createResponse() {
        sendEvent(new RealtimeResponseCreateEvent());
    }

    /**
     * Requests a response with per-response options.
     *
     * @param responseOptions response options.
     */
    public void createResponse(VoiceAgentResponseCreateOptions responseOptions) {
        Objects.requireNonNull(responseOptions, "'responseOptions' cannot be null.");
        sendEvent(new RealtimeResponseCreateEvent().setResponse(responseOptions));
    }

    /** Cancels the active response. */
    public void cancelResponse() {
        sendEvent(new RealtimeResponseCancelEvent());
    }

    /**
     * Cancels a specific response.
     *
     * @param responseId the response identifier.
     */
    public void cancelResponse(String responseId) {
        Objects.requireNonNull(responseId, "'responseId' cannot be null.");
        sendEvent(new RealtimeResponseCancelEvent().setResponseId(responseId));
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
        close(1000, "");
    }

    /**
     * Closes the session with an application-selected WebSocket close frame.
     * @param code a valid WebSocket close code.
     * @param reason non-null reason of at most 123 UTF-8 bytes.
     * @throws IllegalArgumentException if the code or reason is invalid.
     */
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

    private synchronized void signal(EventSignal signal) {
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

    static OkHttpClient createHttpClient(VoiceAgentWebSocketClientConfiguration configuration,
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
                signal(EventSignal.event(VoiceAgentWebSocketUtils.deserializeEvent(text)));
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
        private final Duration timeout;
        private EventSignal next;

        private EventIterator(BlockingQueue<EventSignal> events, Duration timeout) {
            this.events = events;
            this.timeout = timeout;
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
