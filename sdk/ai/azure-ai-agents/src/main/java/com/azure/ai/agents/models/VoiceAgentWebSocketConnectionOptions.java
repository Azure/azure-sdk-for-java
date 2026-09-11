// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.ai.agents.implementation.utils.Beta;
import com.azure.core.annotation.Fluent;

import java.time.Duration;

/**
 * Options used when opening a realtime voice-agent WebSocket session.
 */
@Beta(warningText = "This class is in preview and may change in future releases.")
@Fluent
public final class VoiceAgentWebSocketConnectionOptions {
    private VoiceAgentTransport transport = VoiceAgentTransport.WEBSOCKET;
    private Boolean store;
    private String agentVersionOverride;
    private Duration handshakeTimeout = Duration.ofSeconds(30);
    private Duration closeTimeout = Duration.ofSeconds(10);

    /**
     * Creates options for opening a realtime voice-agent WebSocket session.
     */
    public VoiceAgentWebSocketConnectionOptions() {
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
