// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentWebSocketSessionClient;
import org.junit.jupiter.api.parallel.ResourceLock;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ResourceLock(VoiceAgentWebSocketConfigurationTestBase.TLS_RESOURCE_LOCK)
public class VoiceAgentWebSocketConfigurationTests extends VoiceAgentWebSocketConfigurationTestBase {
    @Override
    void openAndClose(AgentsClientBuilder builder, String agentName) {
        BetaVoiceAgentWebSocketSessionClient session
            = builder.beta().buildBetaVoiceAgentWebSocketClient().openWebSocketSession(agentName);
        session.close();
        assertFalse(session.isOpen());
    }
}
