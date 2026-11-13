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
 * Connection and buffering options for a persisted GPT Live voice agent.
 * Configure these options before opening a session; clients snapshot them when opening.
 */
@Beta(warningText = "Preview API. VoiceAgents=V1Preview")
@Fluent
public final class VoiceAgentLiveWebSocketConnectionOptions {
    private static final ClientLogger LOGGER = new ClientLogger(VoiceAgentLiveWebSocketConnectionOptions.class);
    private int receiveBufferCapacity = 256;
    private int maxMessageSize = 32 * 1024 * 1024;
    private VoiceAgentWebSocketOverflowStrategy overflowStrategy = VoiceAgentWebSocketOverflowStrategy.ERROR;
    private Consumer<Throwable> malformedEventHandler;
    private Duration handshakeTimeout = Duration.ofSeconds(30);
    private Duration closeTimeout = Duration.ofSeconds(10);
    private Boolean store;
    private String agentVersionOverride;
    private String structuredInputs;

    /**
     * Creates options with a 30-second handshake timeout and a 256-event receive buffer.
     */
    public VoiceAgentLiveWebSocketConnectionOptions() {
    }

    /**
     * Copies connection options.
     *
     * @param source the options to copy.
     */
    public VoiceAgentLiveWebSocketConnectionOptions(VoiceAgentLiveWebSocketConnectionOptions source) {
        Objects.requireNonNull(source, "'source' cannot be null.");
        this.receiveBufferCapacity = source.receiveBufferCapacity;
        this.maxMessageSize = source.maxMessageSize;
        this.overflowStrategy = source.overflowStrategy;
        this.malformedEventHandler = source.malformedEventHandler;
        this.handshakeTimeout = source.handshakeTimeout;
        this.closeTimeout = source.closeTimeout;
        this.store = source.store;
        this.agentVersionOverride = source.agentVersionOverride;
        this.structuredInputs = source.structuredInputs;
    }

    /**
     * Gets the receive queue capacity.
     *
     * @return the maximum queued events, default 256.
     */
    public int getReceiveBufferCapacity() {
        return receiveBufferCapacity;
    }

    /**
     * Sets the receive queue capacity.
     *
     * @param capacity the capacity, between 1 and 65536.
     * @return this options instance.
     * @throws IllegalArgumentException if the capacity is outside the supported range.
     */
    public VoiceAgentLiveWebSocketConnectionOptions setReceiveBufferCapacity(int capacity) {
        if (capacity < 1 || capacity > 65536) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("Receive capacity must be between 1 and 65536."));
        }
        this.receiveBufferCapacity = capacity;
        return this;
    }

    /**
     * Gets the maximum accepted message size.
     *
     * @return the size in bytes, default 32 MiB.
     */
    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    /**
     * Sets the maximum accepted message size.
     *
     * @param bytes the positive maximum message size in bytes.
     * @return this options instance.
     * @throws IllegalArgumentException if the size is not positive.
     */
    public VoiceAgentLiveWebSocketConnectionOptions setMaxMessageSize(int bytes) {
        if (bytes <= 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Message size must be positive."));
        }
        this.maxMessageSize = bytes;
        return this;
    }

    /**
     * Gets the receive queue overflow strategy.
     *
     * @return the strategy, default ERROR.
     */
    public VoiceAgentWebSocketOverflowStrategy getOverflowStrategy() {
        return overflowStrategy;
    }

    /**
     * Sets the overflow strategy. Drop strategies explicitly permit event loss.
     *
     * @param strategy the non-null strategy.
     * @return this options instance.
     */
    public VoiceAgentLiveWebSocketConnectionOptions setOverflowStrategy(VoiceAgentWebSocketOverflowStrategy strategy) {
        this.overflowStrategy = Objects.requireNonNull(strategy, "'strategy' cannot be null.");
        return this;
    }

    /**
     * Gets the callback for malformed events.
     *
     * @return the callback, or null to terminate the session.
     */
    public Consumer<Throwable> getMalformedEventHandler() {
        return malformedEventHandler;
    }

    /**
     * Sets a callback to report and skip malformed events. It runs while receiving events and must not block.
     * JSON decoding failures are reported only by typed receivers; raw receivers do not parse JSON.
     * Invalid UTF-8 is reported by the transport for either receiver type.
     * If the callback throws, the session terminates. Transport errors and oversized messages are not skipped.
     *
     * @param handler the callback, or null to terminate on malformed events.
     * @return this options instance.
     */
    public VoiceAgentLiveWebSocketConnectionOptions setMalformedEventHandler(Consumer<Throwable> handler) {
        this.malformedEventHandler = handler;
        return this;
    }

    /**
     * Gets the handshake timeout.
     *
     * @return the timeout, default 30 seconds.
     */
    public Duration getHandshakeTimeout() {
        return handshakeTimeout;
    }

    /**
     * Sets the handshake timeout.
     *
     * @param timeout a non-null positive timeout.
     * @return this options instance.
     * @throws IllegalArgumentException if the timeout is not positive.
     */
    public VoiceAgentLiveWebSocketConnectionOptions setHandshakeTimeout(Duration timeout) {
        validatePositive(timeout, "handshakeTimeout");
        this.handshakeTimeout = timeout;
        return this;
    }

    /**
     * Gets the close timeout.
     *
     * @return the timeout, default 10 seconds.
     */
    public Duration getCloseTimeout() {
        return closeTimeout;
    }

    /**
     * Sets the close timeout.
     *
     * @param timeout a non-null positive timeout.
     * @return this options instance.
     * @throws IllegalArgumentException if the timeout is not positive.
     */
    public VoiceAgentLiveWebSocketConnectionOptions setCloseTimeout(Duration timeout) {
        validatePositive(timeout, "closeTimeout");
        this.closeTimeout = timeout;
        return this;
    }

    /**
     * Gets the persistence override.
     *
     * @return the override, or null to use the persisted agent configuration.
     */
    public Boolean isStoreEnabled() {
        return store;
    }

    /**
     * Overrides Foundry conversation and audio persistence for this session.
     *
     * @param store the override, or null to use the persisted agent configuration.
     * @return this options instance.
     */
    public VoiceAgentLiveWebSocketConnectionOptions setStoreEnabled(Boolean store) {
        this.store = store;
        return this;
    }

    /**
     * Gets the agent version override.
     *
     * @return the version override, or null.
     */
    public String getAgentVersionOverride() {
        return agentVersionOverride;
    }

    /**
     * Sets the agent version to connect to.
     *
     * @param version the version override, or null to use the default version.
     * @return this options instance.
     */
    public VoiceAgentLiveWebSocketConnectionOptions setAgentVersionOverride(String version) {
        this.agentVersionOverride = version;
        return this;
    }

    /**
     * Gets structured inputs for instruction and greeting templates.
     *
     * @return the JSON inputs, or null.
     */
    public String getStructuredInputs() {
        return structuredInputs;
    }

    /**
     * Sets the JSON inputs for instruction and greeting templates.
     * The SDK URL-encodes this value into the {@code structured_inputs} query parameter.
     *
     * @param inputs the unencoded JSON inputs, or null.
     * @return this options instance.
     */
    public VoiceAgentLiveWebSocketConnectionOptions setStructuredInputs(String inputs) {
        this.structuredInputs = inputs;
        return this;
    }

    private static void validatePositive(Duration duration, String name) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("'" + name + "' must be positive.");
        }
    }
}
