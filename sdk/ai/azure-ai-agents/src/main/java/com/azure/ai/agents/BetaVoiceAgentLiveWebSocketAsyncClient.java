// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketClientConfiguration;
import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketUtils;
import com.azure.ai.agents.implementation.utils.Beta;
import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;
import com.azure.core.annotation.ServiceClient;
import java.util.Objects;
import reactor.core.publisher.Mono;

/**
 * An asynchronous client for opening native GPT Live sessions with persisted Foundry Live voice agents.
 * Create instances with {@link AgentsClientBuilder#beta()}.
 */
@ServiceClient(builder = AgentsClientBuilder.class, isAsync = true)
@Beta(warningText = "This class is in preview and may change in future releases.")
public final class BetaVoiceAgentLiveWebSocketAsyncClient {
    private final VoiceAgentWebSocketClientConfiguration configuration;

    BetaVoiceAgentLiveWebSocketAsyncClient(VoiceAgentWebSocketClientConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "'configuration' cannot be null.");
    }

    /**
     * Opens a WebSocket session using the persisted Live agent configuration.
     * @param agentName the Live voice agent name.
     * @return the connected session. Each subscription opens a new connection.
     */
    public Mono<BetaVoiceAgentLiveWebSocketSessionAsyncClient> openWebSocketSession(String agentName) {
        return openWebSocketSession(agentName, new VoiceAgentLiveWebSocketConnectionOptions());
    }

    /**
     * Opens a WebSocket session. Foundry sends the initial {@code session.start}; consume {@code session.started}
     * from the event stream before sending application events. No Realtime subprotocol is negotiated.
     * @param agentName the Live voice agent name.
     * @param options the connection options, copied when this method is called.
     * @return the connected session. Each subscription opens a new connection.
     */
    public Mono<BetaVoiceAgentLiveWebSocketSessionAsyncClient> openWebSocketSession(String agentName,
        VoiceAgentLiveWebSocketConnectionOptions options) {
        Objects.requireNonNull(agentName, "'agentName' cannot be null.");
        VoiceAgentWebSocketConnectionOptions snapshot = VoiceAgentWebSocketUtils.liveOptions(options);
        return Mono.defer(() -> {
            BetaVoiceAgentWebSocketSessionAsyncClient transport
                = new BetaVoiceAgentWebSocketSessionAsyncClient(configuration, agentName, snapshot);
            return transport.connect().thenReturn(new BetaVoiceAgentLiveWebSocketSessionAsyncClient(transport));
        });
    }
}
