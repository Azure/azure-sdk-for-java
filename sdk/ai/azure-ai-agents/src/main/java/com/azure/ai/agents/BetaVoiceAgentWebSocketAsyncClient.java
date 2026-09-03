// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketClientConfiguration;
import com.azure.ai.agents.implementation.utils.Beta;
import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;
import com.azure.core.annotation.ServiceClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * An asynchronous client for opening realtime WebSocket sessions with Foundry voice agents.
 */
@ServiceClient(builder = AgentsClientBuilder.class, isAsync = true)
@Beta(warningText = "This class is in preview and may change in future releases.")
public final class BetaVoiceAgentWebSocketAsyncClient {
    private final VoiceAgentWebSocketClientConfiguration configuration;

    BetaVoiceAgentWebSocketAsyncClient(VoiceAgentWebSocketClientConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "'configuration' cannot be null.");
    }

    /**
     * Opens a realtime WebSocket session using the voice agent's persisted configuration.
     *
     * @param agentName the voice agent name.
     * @return a connected session.
     */
    public Mono<VoiceAgentWebSocketSessionAsyncClient> connect(String agentName) {
        return connect(agentName, new VoiceAgentWebSocketConnectionOptions());
    }

    /**
     * Opens a realtime WebSocket session.
     *
     * @param agentName the voice agent name.
     * @param options connection options.
     * @return a connected session.
     */
    public Mono<VoiceAgentWebSocketSessionAsyncClient> connect(String agentName,
        VoiceAgentWebSocketConnectionOptions options) {
        Objects.requireNonNull(agentName, "'agentName' cannot be null.");
        Objects.requireNonNull(options, "'options' cannot be null.");
        return Mono.defer(() -> {
            VoiceAgentWebSocketSessionAsyncClient session
                = new VoiceAgentWebSocketSessionAsyncClient(configuration, agentName, options);
            return session.connect().thenReturn(session);
        });
    }
}
