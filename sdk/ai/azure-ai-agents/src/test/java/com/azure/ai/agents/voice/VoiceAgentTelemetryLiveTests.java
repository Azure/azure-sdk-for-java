// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.BetaVoiceAgentWebSocketSessionClient;
import com.azure.ai.agents.models.RealtimeSessionCreatedEvent;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/** Live verification that synchronous voice-agent WebSocket sessions emit OpenTelemetry spans. */
@Execution(ExecutionMode.SAME_THREAD)
public class VoiceAgentTelemetryLiveTests extends VoiceAgentTelemetryLiveTestBase {
    private static final Duration EVENT_TIMEOUT = Duration.ofSeconds(30);

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE")
    void websocketSessionExportsTelemetry() {
        try (TelemetryTestContext context = createTestContext()) {
            RealtimeSessionCreatedEvent sessionCreated;
            try (BetaVoiceAgentWebSocketSessionClient session = context.getBuilder()
                .beta()
                .buildBetaVoiceAgentWebSocketClient()
                .openWebSocketSession(context.getAgentName())) {
                sessionCreated = assertInstanceOf(RealtimeSessionCreatedEvent.class,
                    session.receiveEvents(EVENT_TIMEOUT).iterator().next());
            }
            context.assertTelemetry(sessionCreated);
        }
    }
}
