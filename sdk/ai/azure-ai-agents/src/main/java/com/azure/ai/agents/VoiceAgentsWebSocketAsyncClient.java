// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketClientConfiguration;
import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketUtils;
import com.azure.ai.agents.implementation.realtime.WebSocketSessionAsyncTransport;
import com.azure.ai.agents.implementation.realtime.WebSocketTransportOptions;
import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;
import com.azure.core.annotation.ServiceClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.Objects;

/**
 * An asynchronous client for opening realtime WebSocket sessions with Foundry voice agents.
 */
@ServiceClient(builder = AgentsClientBuilder.class, isAsync = true)
public final class VoiceAgentsWebSocketAsyncClient {
    private final VoiceAgentWebSocketClientConfiguration configuration;

    VoiceAgentsWebSocketAsyncClient(VoiceAgentWebSocketClientConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "'configuration' cannot be null.");
    }

    /**
     * Opens a realtime WebSocket session using the voice agent's persisted configuration.
     *
     * @param agentName the voice agent name.
     * @return a connected session.
     */
    public Mono<VoiceAgentsWebSocketSessionAsyncClient> openWebSocketSession(String agentName) {
        return openWebSocketSession(agentName, new VoiceAgentWebSocketConnectionOptions());
    }

    /**
     * Opens a realtime WebSocket session.
     *
     * @param agentName the voice agent name.
     * @param options connection options.
     * @return a connected session.
     */
    public Mono<VoiceAgentsWebSocketSessionAsyncClient> openWebSocketSession(String agentName,
        VoiceAgentWebSocketConnectionOptions options) {
        Objects.requireNonNull(agentName, "'agentName' cannot be null.");
        Objects.requireNonNull(options, "'options' cannot be null.");
        VoiceAgentWebSocketConnectionOptions optionsSnapshot = new VoiceAgentWebSocketConnectionOptions(options);
        return Mono.defer(() -> {
            WebSocketSessionAsyncTransport transport = new WebSocketSessionAsyncTransport(configuration,
                VoiceAgentWebSocketUtils.buildWebSocketUri(configuration, agentName, optionsSnapshot),
                new WebSocketTransportOptions(optionsSnapshot), HttpClient.create(),
                VoiceAgentWebSocketUtils.SUBPROTOCOL,
                token -> VoiceAgentWebSocketUtils.buildHeaders(configuration, optionsSnapshot, token));
            return transport.connect()
                .thenReturn(
                    new VoiceAgentsWebSocketSessionAsyncClient(transport, optionsSnapshot.getMalformedEventHandler()));
        });
    }
}
