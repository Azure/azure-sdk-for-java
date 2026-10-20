// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketClientConfiguration;
import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketUtils;
import com.azure.ai.agents.implementation.utils.Beta;
import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;
import java.util.Objects;

/**
 * A synchronous client for opening native GPT Live sessions with persisted Foundry Live voice agents.
 * Create instances with {@link AgentsClientBuilder#beta()}.
 */
@ServiceClient(builder = AgentsClientBuilder.class)
@Beta(warningText = "This class is in preview and may change in future releases.")
public final class BetaVoiceAgentLiveWebSocketClient {
    private static final ClientLogger LOGGER = new ClientLogger(BetaVoiceAgentLiveWebSocketClient.class);
    private final VoiceAgentWebSocketClientConfiguration configuration;

    BetaVoiceAgentLiveWebSocketClient(VoiceAgentWebSocketClientConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "'configuration' cannot be null.");
    }

    /**
     * Opens a WebSocket session using the persisted Live agent configuration.
     * @param agentName the Live voice agent name.
     * @return the connected session.
     */
    public BetaVoiceAgentLiveWebSocketSessionClient openWebSocketSession(String agentName) {
        return openWebSocketSession(agentName, new VoiceAgentLiveWebSocketConnectionOptions());
    }

    /**
     * Opens a WebSocket session. Foundry sends the initial {@code session.start}; consume {@code session.started}
     * from the event stream before sending application events. No Realtime subprotocol is negotiated.
     * @param agentName the Live voice agent name.
     * @param options the connection options, copied before connecting.
     * @return the connected session.
     * @throws IllegalArgumentException if the agent name is empty.
     */
    public BetaVoiceAgentLiveWebSocketSessionClient openWebSocketSession(String agentName,
        VoiceAgentLiveWebSocketConnectionOptions options) {
        Objects.requireNonNull(agentName, "'agentName' cannot be null.");
        if (agentName.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'agentName' cannot be empty."));
        }
        return new BetaVoiceAgentLiveWebSocketSessionClient(BetaVoiceAgentWebSocketSessionClient.connect(configuration,
            agentName, VoiceAgentWebSocketUtils.liveOptions(options)));
    }
}
