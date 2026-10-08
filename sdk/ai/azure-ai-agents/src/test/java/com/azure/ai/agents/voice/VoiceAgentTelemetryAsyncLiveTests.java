// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.models.RealtimeSessionCreatedEvent;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/** Live verification that asynchronous voice-agent WebSocket sessions emit OpenTelemetry spans. */
@Execution(ExecutionMode.SAME_THREAD)
public class VoiceAgentTelemetryAsyncLiveTests extends VoiceAgentTelemetryLiveTestBase {
    private static final Duration EVENT_TIMEOUT = Duration.ofSeconds(30);

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE")
    void websocketSessionExportsTelemetry() {
        try (TelemetryTestContext context = createTestContext()) {
            Mono<RealtimeSessionCreatedEvent> sessionCreated
                = Mono
                    .usingWhen(
                        context.getBuilder()
                            .beta()
                            .buildBetaVoiceAgentWebSocketAsyncClient()
                            .openWebSocketSession(context.getAgentName()),
                        session -> session.receiveEvents().next().cast(RealtimeSessionCreatedEvent.class),
                        session -> session.closeAsync(), (session, error) -> session.closeAsync(),
                        session -> session.closeAsync())
                    .timeout(EVENT_TIMEOUT);

            StepVerifier.create(sessionCreated)
                .assertNext(context::assertTelemetry)
                .expectComplete()
                .verify(EVENT_TIMEOUT.plusSeconds(5));
        }
    }
}
