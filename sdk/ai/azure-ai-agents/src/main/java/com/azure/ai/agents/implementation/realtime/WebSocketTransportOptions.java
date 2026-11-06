// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.realtime;

import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;
import com.azure.ai.agents.models.VoiceAgentWebSocketOverflowStrategy;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Immutable transport settings captured from a protocol-specific public options bag.
 */
public final class WebSocketTransportOptions {
    private final int receiveBufferCapacity;
    private final int maxMessageSize;
    private final VoiceAgentWebSocketOverflowStrategy overflowStrategy;
    private final Consumer<Throwable> malformedEventHandler;
    private final Duration handshakeTimeout;
    private final Duration closeTimeout;

    public WebSocketTransportOptions(VoiceAgentWebSocketConnectionOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        this.receiveBufferCapacity = options.getReceiveBufferCapacity();
        this.maxMessageSize = options.getMaxMessageSize();
        this.overflowStrategy = options.getOverflowStrategy();
        this.malformedEventHandler = options.getMalformedEventHandler();
        this.handshakeTimeout = options.getHandshakeTimeout();
        this.closeTimeout = options.getCloseTimeout();
    }

    public WebSocketTransportOptions(VoiceAgentLiveWebSocketConnectionOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        this.receiveBufferCapacity = options.getReceiveBufferCapacity();
        this.maxMessageSize = options.getMaxMessageSize();
        this.overflowStrategy = options.getOverflowStrategy();
        this.malformedEventHandler = options.getMalformedEventHandler();
        this.handshakeTimeout = options.getHandshakeTimeout();
        this.closeTimeout = options.getCloseTimeout();
    }

    public int getReceiveBufferCapacity() {
        return receiveBufferCapacity;
    }

    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    public VoiceAgentWebSocketOverflowStrategy getOverflowStrategy() {
        return overflowStrategy;
    }

    public Consumer<Throwable> getMalformedEventHandler() {
        return malformedEventHandler;
    }

    public Duration getHandshakeTimeout() {
        return handshakeTimeout;
    }

    public Duration getCloseTimeout() {
        return closeTimeout;
    }
}
