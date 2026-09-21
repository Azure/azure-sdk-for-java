// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentWebSocketSessionAsyncClient;
import java.time.Duration;
import org.junit.jupiter.api.parallel.ResourceLock;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ResourceLock(VoiceAgentWebSocketConfigurationTestBase.TLS_RESOURCE_LOCK)
public class VoiceAgentWebSocketConfigurationAsyncTests extends VoiceAgentWebSocketConfigurationTestBase {
    @Override
    void openAndClose(AgentsClientBuilder builder, String agentName) {
        BetaVoiceAgentWebSocketSessionAsyncClient session = builder.beta()
            .buildBetaVoiceAgentWebSocketAsyncClient()
            .openWebSocketSession(agentName)
            .block(Duration.ofSeconds(5));
        session.closeAsync().block(Duration.ofSeconds(5));
        assertFalse(session.isOpen());
    }
}
