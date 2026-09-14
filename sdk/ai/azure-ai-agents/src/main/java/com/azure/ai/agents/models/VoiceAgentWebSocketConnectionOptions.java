// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.ai.agents.implementation.utils.Beta;
import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import okhttp3.OkHttpClient;
import reactor.netty.http.client.HttpClient;

/**
 * Options used when opening a realtime voice-agent WebSocket session.
 */
@Beta(warningText = "This class is in preview and may change in future releases.")
@Fluent
public final class VoiceAgentWebSocketConnectionOptions {
    private static final ClientLogger LOGGER = new ClientLogger(VoiceAgentWebSocketConnectionOptions.class);
    private int receiveBufferCapacity = 256;
    private int maxMessageSize = 32 * 1024 * 1024;
    private VoiceAgentWebSocketOverflowStrategy overflowStrategy = VoiceAgentWebSocketOverflowStrategy.ERROR;
    private Consumer<Throwable> malformedEventHandler;

    /**
     * Gets the maximum number of queued events.
     * @return the capacity, default 256.
     */
    public int getReceiveBufferCapacity() {
        return receiveBufferCapacity;
    }

    /**
     * Sets the bounded receive queue capacity. Configure before connecting.
     * @param capacity number of events, between 1 and 65536.
     * @return this options instance.
    * @throws IllegalArgumentException if capacity is outside the supported range.
     */
    public VoiceAgentWebSocketConnectionOptions setReceiveBufferCapacity(int capacity) {
        if (capacity < 1 || capacity > 65536) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("Receive capacity must be between 1 and 65536."));
        }
        this.receiveBufferCapacity = capacity;
        return this;
    }

    /**
     * Gets the maximum accepted JSON message size.
     * @return the size in bytes, default 32 MiB.
     */
    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    /**
     * Sets the maximum accepted JSON message size. Oversized messages terminate the connection.
     * @param bytes positive size in bytes.
     * @return this options instance.
    * @throws IllegalArgumentException if bytes is not positive.
     */
    public VoiceAgentWebSocketConnectionOptions setMaxMessageSize(int bytes) {
        if (bytes <= 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Message size must be positive."));
        }
        this.maxMessageSize = bytes;
        return this;
    }

    /**
     * Gets the receive queue overflow action.
     * @return the strategy, default ERROR.
     */
    public VoiceAgentWebSocketOverflowStrategy getOverflowStrategy() {
        return overflowStrategy;
    }

    /**
     * Sets the receive queue overflow action. Drop strategies explicitly permit data loss.
     * @param strategy the non-null strategy.
     * @return this options instance.
     */
    public VoiceAgentWebSocketConnectionOptions setOverflowStrategy(VoiceAgentWebSocketOverflowStrategy strategy) {
        this.overflowStrategy = Objects.requireNonNull(strategy, "'strategy' cannot be null.");
        return this;
    }

    /**
     * Gets the callback for skipping malformed events.
     * @return the callback, or null to terminate on malformed events.
     */
    public Consumer<Throwable> getMalformedEventHandler() {
        return malformedEventHandler;
    }

    /**
     * Sets a callback that reports and skips malformed events without terminating reception.
     * The callback runs on the receive thread and must not block. If it throws, the session terminates.
     * This does not recover from transport errors or oversized messages.
     * @param handler callback, or null to terminate on malformed events (the default).
     * @return this options instance.
     */
    public VoiceAgentWebSocketConnectionOptions setMalformedEventHandler(Consumer<Throwable> handler) {
        this.malformedEventHandler = handler;
        return this;
    }

    private Consumer<OkHttpClient.Builder> httpClientConfiguration;
    private UnaryOperator<HttpClient> asyncHttpClientConfiguration;

    /**
     * Sets synchronous transport customization, for example certificate trust or ping interval.
     * Redirects and handshake timeouts remain SDK-controlled. Do not disable TLS hostname verification.
     * @param configure callback applied to the per-session transport, or null for defaults.
     * @return this options instance.
     */
    public VoiceAgentWebSocketConnectionOptions setHttpClientConfiguration(Consumer<OkHttpClient.Builder> configure) {
        this.httpClientConfiguration = configure;
        return this;
    }

    /**
     * Gets synchronous transport customization.
     * @return the callback, or null.
     */
    public Consumer<OkHttpClient.Builder> getHttpClientConfiguration() {
        return httpClientConfiguration;
    }

    /**
     * Sets asynchronous transport customization, for example certificate trust or channel handlers.
     * Redirects, authentication, subprotocol and handshake timeouts remain SDK-controlled.
     * Do not disable TLS hostname verification.
     * @param configure callback returning a configured transport, or null for defaults.
     * @return this options instance.
     */
    public VoiceAgentWebSocketConnectionOptions setAsyncHttpClientConfiguration(UnaryOperator<HttpClient> configure) {
        this.asyncHttpClientConfiguration = configure;
        return this;
    }

    /**
     * Gets asynchronous transport customization.
     * @return the callback, or null.
     */
    public UnaryOperator<HttpClient> getAsyncHttpClientConfiguration() {
        return asyncHttpClientConfiguration;
    }

    private VoiceAgentTransport transport = VoiceAgentTransport.WEBSOCKET;
    private Boolean store;
    private String agentVersionOverride;
    private Duration handshakeTimeout = Duration.ofSeconds(30);
    private Duration closeTimeout = Duration.ofSeconds(10);
    private String agentSessionId;
    private String structuredInputs;
    private URI connectionUrl;
    private String apiVersion;
    private String foundryFeatures = "VoiceAgents=V1Preview";
    private List<String> credentialScopes;
    private Map<String, String> extraQuery = Collections.emptyMap();
    private Map<String, String> extraHeaders = Collections.emptyMap();

    /**
     * Creates options for opening a realtime voice-agent WebSocket session.
     */
    public VoiceAgentWebSocketConnectionOptions() {
    }

    /**
     * Gets the session correlation identifier.
     * @return the session identifier, or null.
     */
    public String getAgentSessionId() {
        return agentSessionId;
    }

    /**
     * Sets the session correlation identifier.
     * @param agentSessionId the session identifier, or null.
     * @return this options instance.
     */
    public VoiceAgentWebSocketConnectionOptions setAgentSessionId(String agentSessionId) {
        this.agentSessionId = agentSessionId;
        return this;
    }

    /**
     * Gets the structured inputs JSON object sent in the handshake header.
     * @return the structured inputs, or null.
     */
    public String getStructuredInputs() {
        return structuredInputs;
    }

    /**
     * Sets the structured inputs JSON object sent in the handshake header.
     * @param structuredInputs the JSON object, or null.
     * @return this options instance.
     */
    public VoiceAgentWebSocketConnectionOptions setStructuredInputs(String structuredInputs) {
        this.structuredInputs = structuredInputs;
        return this;
    }

    /**
     * Gets the full WebSocket URL override.
     * @return the URL override, or null.
     */
    public URI getConnectionUrl() {
        return connectionUrl;
    }

    /**
     * Sets a full WebSocket URL override. It must use wss and the project endpoint's host and port.
     * User information and fragments are not supported. Existing query parameters are preserved unless overridden.
     * @param connectionUrl the URL override, or null to use the agent route.
     * @return this options instance.
     */
    public VoiceAgentWebSocketConnectionOptions setConnectionUrl(URI connectionUrl) {
        this.connectionUrl = connectionUrl;
        return this;
    }

    /**
     * Gets the handshake API version override.
     * @return the API version, or null.
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Sets the handshake API version override.
     * @param apiVersion the API version, or null to use the client's version.
     * @return this options instance.
     */
    public VoiceAgentWebSocketConnectionOptions setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    /**
     * Gets the preview feature header value.
     * @return the preview feature header value.
     */
    public String getFoundryFeatures() {
        return foundryFeatures;
    }

    /**
     * Sets the preview feature header value.
     * @param foundryFeatures comma-separated preview features, or an empty string to suppress opt-in.
     * @return this options instance.
     */
    public VoiceAgentWebSocketConnectionOptions setFoundryFeatures(String foundryFeatures) {
        this.foundryFeatures = Objects.requireNonNull(foundryFeatures, "'foundryFeatures' cannot be null.");
        return this;
    }

    /**
     * Gets credential scopes for the handshake.
     * @return an unmodifiable list, or null to use the default Foundry scope.
     */
    public List<String> getCredentialScopes() {
        return credentialScopes;
    }

    /**
     * Sets credential scopes for the handshake.
     * @param credentialScopes the scopes, or null to use the default Foundry scope.
     * @return this options instance.
     */
    public VoiceAgentWebSocketConnectionOptions setCredentialScopes(List<String> credentialScopes) {
        this.credentialScopes
            = credentialScopes == null ? null : Collections.unmodifiableList(new ArrayList<>(credentialScopes));
        return this;
    }

    /**
     * Gets additional handshake query parameters.
     * @return an unmodifiable map of query parameters.
     */
    public Map<String, String> getExtraQuery() {
        return extraQuery;
    }

    /**
     * Sets additional handshake query parameters, taking precedence over defaults.
     * @param extraQuery unencoded query names and values, or null to clear.
     * @return this options instance.
     */
    public VoiceAgentWebSocketConnectionOptions setExtraQuery(Map<String, String> extraQuery) {
        this.extraQuery = extraQuery == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(new LinkedHashMap<>(extraQuery));
        return this;
    }

    /**
     * Gets additional handshake headers.
     * @return an unmodifiable map of headers.
     */
    public Map<String, String> getExtraHeaders() {
        return extraHeaders;
    }

    /**
     * Sets additional handshake headers. Authorization, host, connection, upgrade, and WebSocket protocol headers
     * remain transport-controlled. Other headers override defaults case-insensitively, including empty values.
     * @param extraHeaders the additional headers, or null to clear.
     * @return this options instance.
     */
    public VoiceAgentWebSocketConnectionOptions setExtraHeaders(Map<String, String> extraHeaders) {
        this.extraHeaders = extraHeaders == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(new LinkedHashMap<>(extraHeaders));
        return this;
    }

    /**
     * Gets the session transport.
     *
     * @return the session transport.
     */
    public VoiceAgentTransport getTransport() {
        return transport;
    }

    /**
     * Sets the session transport. WebRTC transport performs signaling only; the SDK does not provide a WebRTC media
     * implementation.
     *
     * @param transport the session transport.
     * @return this options instance.
     */
    public VoiceAgentWebSocketConnectionOptions setTransport(VoiceAgentTransport transport) {
        this.transport = transport;
        return this;
    }

    /**
     * Gets whether the conversation is persisted for this session.
     *
     * @return whether the conversation is persisted, or {@code null} to use the agent definition.
     */
    public Boolean isStoreEnabled() {
        return store;
    }

    /**
     * Sets whether the conversation is persisted for this session.
     *
     * @param store whether the conversation is persisted, or {@code null} to use the agent definition.
     * @return this options instance.
     */
    public VoiceAgentWebSocketConnectionOptions setStoreEnabled(Boolean store) {
        this.store = store;
        return this;
    }

    /**
     * Gets the agent version override.
     *
     * @return the agent version override.
     */
    public String getAgentVersionOverride() {
        return agentVersionOverride;
    }

    /**
     * Sets the agent version override.
     *
     * @param agentVersionOverride the agent version to use for this session.
     * @return this options instance.
     */
    public VoiceAgentWebSocketConnectionOptions setAgentVersionOverride(String agentVersionOverride) {
        this.agentVersionOverride = agentVersionOverride;
        return this;
    }

    /**
     * Gets the WebSocket handshake timeout.
     *
     * @return the WebSocket handshake timeout.
     */
    public Duration getHandshakeTimeout() {
        return handshakeTimeout;
    }

    /**
     * Sets the WebSocket handshake timeout.
     *
     * @param handshakeTimeout the positive handshake timeout.
     * @return this options instance.
     */
    public VoiceAgentWebSocketConnectionOptions setHandshakeTimeout(Duration handshakeTimeout) {
        validatePositive(handshakeTimeout, "handshakeTimeout");
        this.handshakeTimeout = handshakeTimeout;
        return this;
    }

    /**
     * Gets the graceful close timeout.
     *
     * @return the graceful close timeout.
     */
    public Duration getCloseTimeout() {
        return closeTimeout;
    }

    /**
     * Sets the graceful close timeout.
     *
     * @param closeTimeout the positive close timeout.
     * @return this options instance.
     */
    public VoiceAgentWebSocketConnectionOptions setCloseTimeout(Duration closeTimeout) {
        validatePositive(closeTimeout, "closeTimeout");
        this.closeTimeout = closeTimeout;
        return this;
    }

    private static void validatePositive(Duration duration, String name) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("'" + name + "' must be positive.");
        }
    }
}
