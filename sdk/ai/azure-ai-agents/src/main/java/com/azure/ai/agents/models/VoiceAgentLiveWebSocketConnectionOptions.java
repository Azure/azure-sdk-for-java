// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.ai.agents.implementation.utils.Beta;
import com.azure.core.annotation.Fluent;
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
    private final VoiceAgentWebSocketConnectionOptions options;

    /** Creates options with a 30-second handshake timeout and a 256-event receive buffer. */
    public VoiceAgentLiveWebSocketConnectionOptions() {
        options = new VoiceAgentWebSocketConnectionOptions();
    }

    /**
     * Copies connection options.
     * @param source the options to copy.
     */
    public VoiceAgentLiveWebSocketConnectionOptions(VoiceAgentLiveWebSocketConnectionOptions source) {
        options = new VoiceAgentWebSocketConnectionOptions(
            Objects.requireNonNull(source, "'source' cannot be null.").options);
    }

    /**
     * Gets the receive queue capacity.
     * @return the maximum queued events, default 256.
     */
    public int getReceiveBufferCapacity() {
        return options.getReceiveBufferCapacity();
    }

    /**
     * Sets the receive queue capacity.
     * @param capacity the capacity, between 1 and 65536.
     * @return this options instance.
     * @throws IllegalArgumentException if the capacity is outside the supported range.
     */
    public VoiceAgentLiveWebSocketConnectionOptions setReceiveBufferCapacity(int capacity) {
        options.setReceiveBufferCapacity(capacity);
        return this;
    }

    /**
     * Gets the maximum accepted message size.
     * @return the size in bytes, default 32 MiB.
     */
    public int getMaxMessageSize() {
        return options.getMaxMessageSize();
    }

    /**
     * Sets the maximum accepted message size.
     * @param bytes the positive maximum message size in bytes.
     * @return this options instance.
     * @throws IllegalArgumentException if the size is not positive.
     */
    public VoiceAgentLiveWebSocketConnectionOptions setMaxMessageSize(int bytes) {
        options.setMaxMessageSize(bytes);
        return this;
    }

    /**
     * Gets the receive queue overflow strategy.
     * @return the strategy, default ERROR.
     */
    public VoiceAgentWebSocketOverflowStrategy getOverflowStrategy() {
        return options.getOverflowStrategy();
    }

    /**
     * Sets the overflow strategy. Drop strategies explicitly permit event loss.
     * @param strategy the non-null strategy.
     * @return this options instance.
     */
    public VoiceAgentLiveWebSocketConnectionOptions setOverflowStrategy(VoiceAgentWebSocketOverflowStrategy strategy) {
        options.setOverflowStrategy(strategy);
        return this;
    }

    /**
     * Gets the callback for malformed events.
     * @return the callback, or null to terminate the session.
     */
    public Consumer<Throwable> getMalformedEventHandler() {
        return options.getMalformedEventHandler();
    }

    /**
     * Sets a callback to report and skip malformed events. It runs on the receive thread and must not block.
     * If the callback throws, the session terminates. Transport errors and oversized messages are not skipped.
     * @param handler the callback, or null to terminate on malformed events.
     * @return this options instance.
     */
    public VoiceAgentLiveWebSocketConnectionOptions setMalformedEventHandler(Consumer<Throwable> handler) {
        options.setMalformedEventHandler(handler);
        return this;
    }

    /**
     * Gets the handshake timeout.
     * @return the timeout, default 30 seconds.
     */
    public Duration getHandshakeTimeout() {
        return options.getHandshakeTimeout();
    }

    /**
     * Sets the handshake timeout.
     * @param timeout a non-null positive timeout.
     * @return this options instance.
     * @throws IllegalArgumentException if the timeout is not positive.
     */
    public VoiceAgentLiveWebSocketConnectionOptions setHandshakeTimeout(Duration timeout) {
        options.setHandshakeTimeout(timeout);
        return this;
    }

    /**
     * Gets the close timeout.
     * @return the timeout, default 10 seconds.
     */
    public Duration getCloseTimeout() {
        return options.getCloseTimeout();
    }

    /**
     * Sets the close timeout.
     * @param timeout a non-null positive timeout.
     * @return this options instance.
     * @throws IllegalArgumentException if the timeout is not positive.
     */
    public VoiceAgentLiveWebSocketConnectionOptions setCloseTimeout(Duration timeout) {
        options.setCloseTimeout(timeout);
        return this;
    }

    /**
     * Gets the persistence override.
     * @return the override, or null to use the persisted agent configuration.
     */
    public Boolean isStoreEnabled() {
        return options.isStoreEnabled();
    }

    /**
     * Overrides Foundry conversation and audio persistence for this session.
     * @param store the override, or null to use the persisted agent configuration.
     * @return this options instance.
     */
    public VoiceAgentLiveWebSocketConnectionOptions setStoreEnabled(Boolean store) {
        options.setStoreEnabled(store);
        return this;
    }

    /**
     * Gets the agent version override.
     * @return the version override, or null.
     */
    public String getAgentVersionOverride() {
        return options.getAgentVersionOverride();
    }

    /**
     * Sets the agent version to connect to.
     * @param version the version override, or null to use the default version.
     * @return this options instance.
     */
    public VoiceAgentLiveWebSocketConnectionOptions setAgentVersionOverride(String version) {
        options.setAgentVersionOverride(version);
        return this;
    }

    /**
     * Gets structured inputs for instruction and greeting templates.
     * @return the JSON inputs, or null.
     */
    public String getStructuredInputs() {
        return options.getStructuredInputs();
    }

    /**
     * Sets the JSON inputs for instruction and greeting templates.
     * The SDK URL-encodes this value into the {@code structured_inputs} query parameter.
     * @param inputs the unencoded JSON inputs, or null.
     * @return this options instance.
     */
    public VoiceAgentLiveWebSocketConnectionOptions setStructuredInputs(String inputs) {
        options.setStructuredInputs(inputs);
        return this;
    }
}
