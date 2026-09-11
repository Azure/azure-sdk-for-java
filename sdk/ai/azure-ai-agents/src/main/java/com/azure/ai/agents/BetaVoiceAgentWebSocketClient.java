// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketClientConfiguration;
import com.azure.ai.agents.implementation.utils.Beta;
import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * A synchronous client for opening realtime WebSocket sessions with Foundry voice agents.
 */
@ServiceClient(builder = AgentsClientBuilder.class)
@Beta(warningText = "This class is in preview and may change in future releases.")
public final class BetaVoiceAgentWebSocketClient {
    private static final ClientLogger LOGGER = new ClientLogger(BetaVoiceAgentWebSocketClient.class);
    private final VoiceAgentWebSocketClientConfiguration configuration;

    BetaVoiceAgentWebSocketClient(VoiceAgentWebSocketClientConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "'configuration' cannot be null.");
    }

    /**
     * Opens a realtime WebSocket session using the voice agent's persisted configuration.
     *
     * @param agentName the voice agent name.
     * @return a connected session.
     */
    public VoiceAgentWebSocketSessionClient connect(String agentName) {
        return connect(agentName, new VoiceAgentWebSocketConnectionOptions());
    }

    /**
     * Opens a realtime WebSocket session.
     *
     * @param agentName the voice agent name.
     * @param options connection options.
    * @throws IllegalArgumentException if {@code agentName} is empty.
     * @return a connected session.
     */
    public VoiceAgentWebSocketSessionClient connect(String agentName, VoiceAgentWebSocketConnectionOptions options) {
        Objects.requireNonNull(agentName, "'agentName' cannot be null.");
        if (agentName.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'agentName' cannot be empty."));
        }
        Objects.requireNonNull(options, "'options' cannot be null.");
        return VoiceAgentWebSocketSessionClient.connect(configuration, agentName, options);
    }
}
