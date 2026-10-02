// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.ai.agents.implementation.utils.Beta;
import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Options used when opening a realtime voice-agent WebSocket session.
 */
@Beta(warningText = "Preview API. VoiceAgents=V1Preview")
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

    private Boolean store;
    private String agentVersionOverride;
    private Duration handshakeTimeout = Duration.ofSeconds(30);
    private Duration closeTimeout = Duration.ofSeconds(10);
    private String agentSessionId;
    private String structuredInputs;

    /**
     * Creates options for opening a realtime voice-agent WebSocket session.
     */
    public VoiceAgentWebSocketConnectionOptions() {
    }

    /**
     * Creates a copy of the supplied connection options.
     *
     * @param source the options to copy.
     * @throws NullPointerException if {@code source} is null.
     */
    public VoiceAgentWebSocketConnectionOptions(VoiceAgentWebSocketConnectionOptions source) {
        Objects.requireNonNull(source, "'source' cannot be null.");
        this.receiveBufferCapacity = source.receiveBufferCapacity;
        this.maxMessageSize = source.maxMessageSize;
        this.overflowStrategy = source.overflowStrategy;
        this.malformedEventHandler = source.malformedEventHandler;
        this.store = source.store;
        this.agentVersionOverride = source.agentVersionOverride;
        this.handshakeTimeout = source.handshakeTimeout;
        this.closeTimeout = source.closeTimeout;
        this.agentSessionId = source.agentSessionId;
        this.structuredInputs = source.structuredInputs;
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
