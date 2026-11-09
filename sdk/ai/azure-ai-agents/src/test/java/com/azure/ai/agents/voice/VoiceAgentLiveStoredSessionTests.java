// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.implementation.OpenAIJsonHelper;
import com.azure.core.util.BinaryData;
import com.openai.models.live.ServerEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VoiceAgentLiveStoredSessionTests {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final String STARTED
        = "{\"type\":\"session.started\",\"conversation_id\":\"conversation-1\"," + "\"session\":{\"id\":\"live-1\"}}";
    private static final String AUDIO = "{\"type\":\"session.output_audio.delta\",\"delta\":\"AQI=\"}";
    private static final String COMPLETED = "{\"type\":\"response.event\",\"delegation_id\":\"del-1\",\"event\":"
        + "{\"type\":\"response.completed\",\"response\":{\"id\":\"resp-1\",\"output\":[]}}}";
    private static final String CLOSED = "{\"type\":\"session.closed\",\"reason\":\"close_requested\"}";

    @Test
    public void capturesStoredConversationAfterSpeechAndClosure() {
        Sinks.Many<ServerEvent> events = Sinks.many().unicast().onBackpressureBuffer();
        List<String> commands = new ArrayList<>();
        emit(events, STARTED);
        String conversationId
            = VoiceAgentLiveConversationsTestBase.runStoredSession(events.asFlux(), command -> Mono.fromRunnable(() -> {
                if (command.isResponseItemCreate()) {
                    commands.add("message");
                } else if (command.isResponseCreate()) {
                    commands.add("response");
                    emit(events, COMPLETED);
                    emit(events, AUDIO);
                } else if (command.isSessionClose()) {
                    commands.add("close");
                    emit(events, CLOSED);
                }
            })).block(TIMEOUT);

        assertEquals("conversation-1", conversationId);
        assertEquals(Arrays.asList("message", "response", "close"), commands);
    }

    private static void emit(Sinks.Many<ServerEvent> events, String json) {
        events.emitNext(OpenAIJsonHelper.fromBinaryData(BinaryData.fromString(json), ServerEvent.class),
            Sinks.EmitFailureHandler.FAIL_FAST);
    }
}
