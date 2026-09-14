// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.VoiceAgentWebSocketSessionAsyncClient;
import com.azure.ai.agents.VoiceAgentWebSocketSessionClient;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.RealtimeClientEvent;
import com.azure.ai.agents.models.RealtimeClientEventConversationItemCreate;
import com.azure.ai.agents.models.RealtimeClientEventResponseCreate;
import com.azure.ai.agents.models.RealtimeConversationItemFunctionCallOutput;
import com.azure.ai.agents.models.RealtimeConversationItemMessageUser;
import com.azure.ai.agents.models.RealtimeConversationItemMessageUserContent;
import com.azure.ai.agents.models.RealtimeConversationItemMessageUserContentType;
import com.azure.ai.agents.models.RealtimeServerEvent;
import com.azure.ai.agents.models.RealtimeServerEventRealtimeServerEventError;
import com.azure.ai.agents.models.RealtimeServerEventResponseAudioDelta;
import com.azure.ai.agents.models.RealtimeServerEventResponseAudioTranscriptDone;
import com.azure.ai.agents.models.RealtimeServerEventResponseDone;
import com.azure.ai.agents.models.RealtimeServerEventResponseFunctionCallArgumentsDone;
import com.azure.ai.agents.models.RealtimeServerEventResponseTextDone;
import com.azure.ai.agents.models.RealtimeServerEventSessionCreated;
import com.azure.ai.agents.models.VoiceAgentAudioConfig;
import com.azure.ai.agents.models.VoiceAgentAudioOutputConfig;
import com.azure.ai.agents.models.VoiceAgentDefinition;
import com.azure.ai.agents.models.VoiceAgentFunctionTool;
import com.azure.ai.agents.models.VoiceModelType;
import com.azure.ai.agents.models.VoiceOutputModality;
import com.azure.ai.agents.models.VoiceType;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Sync/async parity for the Python live realtime suites. Live cases require AZURE_TEST_MODE=LIVE,
 * FOUNDRY_PROJECT_ENDPOINT, FOUNDRY_VOICE_MODEL_NAME and DefaultAzureCredential authentication.
 * They create and delete real agents but require no audio hardware. Offline cases validate the
 * same event assertions with synthetic payloads, not service recordings.
 */
public class VoiceAgentRealtimeLiveTests {
    private static final Duration EVENT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(45);
    private static final String SESSION = "{\"type\":\"session.created\",\"session\":{}}";
    private static final String DONE = "{\"type\":\"response.done\",\"response\":{\"output\":[]}}";
    private static final String TOOL_DONE = "{\"type\":\"response.done\",\"response\":{\"output\":["
        + "{\"type\":\"function_call\",\"name\":\"get_weather\",\"call_id\":\"call-1\",\"arguments\":\"{}\"}]}}";

    enum Scenario {
        LIFECYCLE, AUDIO, FUNCTION
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE")
    public void realtimeLive(Scenario scenario) {
        AgentsClientBuilder builder = liveBuilder();
        AgentsClient agents = builder.buildAgentsClient();
        String agentName = "test-realtime-sync-" + UUID.randomUUID();
        boolean created = false;
        try {
            agents.createAgentVersion(agentName, new CreateAgentVersionInput(definition(scenario)));
            created = true;
            try (VoiceAgentWebSocketSessionClient session
                = builder.buildBetaVoiceAgentWebSocketClient().connect(agentName)) {
                Turn turn = new Turn(scenario);
                Iterator<RealtimeServerEvent> events = session.receiveEvents(EVENT_TIMEOUT).iterator();
                turn.accept(events.next()).forEach(session::sendEvent);
                long deadline = System.nanoTime() + RESPONSE_TIMEOUT.toNanos();
                while (!turn.done && System.nanoTime() < deadline && events.hasNext()) {
                    turn.accept(events.next()).forEach(session::sendEvent);
                }
                turn.assertComplete();
            }
        } finally {
            if (created) {
                agents.deleteAgent(agentName);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE")
    public void realtimeLiveAsync(Scenario scenario) {
        AgentsClientBuilder builder = liveBuilder();
        AgentsAsyncClient agents = builder.buildAgentsAsyncClient();
        String agentName = "test-realtime-async-" + UUID.randomUUID();
        boolean created = false;
        try {
            agents.createAgentVersion(agentName, new CreateAgentVersionInput(definition(scenario)))
                .block(EVENT_TIMEOUT);
            created = true;
            Turn turn = new Turn(scenario);
            Mono.usingWhen(builder.buildBetaVoiceAgentWebSocketAsyncClient().connect(agentName),
                session -> session.receiveEvents()
                    .timeout(EVENT_TIMEOUT)
                    .concatMap(event -> Flux.fromIterable(turn.accept(event))
                        .concatMap(session::sendEvent)
                        .then(Mono.just(turn.done)))
                    .filter(Boolean::booleanValue)
                    .next()
                    .switchIfEmpty(Mono.error(new AssertionError("Session ended before the turn completed.")))
                    .timeout(RESPONSE_TIMEOUT)
                    .doOnNext(ignored -> turn.assertComplete())
                    .then(),
                VoiceAgentWebSocketSessionAsyncClient::closeAsync, (session, error) -> session.closeAsync(),
                VoiceAgentWebSocketSessionAsyncClient::closeAsync).block(Duration.ofSeconds(90));
        } finally {
            if (created) {
                agents.deleteAgent(agentName).block(EVENT_TIMEOUT);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void syntheticEventsExerciseLiveAssertions(Scenario scenario) {
        Turn turn = new Turn(scenario);
        List<RealtimeClientEvent> initial = turn.accept(event(SESSION));
        assertEquals(scenario == Scenario.LIFECYCLE ? 0 : 2, initial.size());
        if (scenario != Scenario.LIFECYCLE) {
            assertInstanceOf(RealtimeClientEventConversationItemCreate.class, initial.get(0));
            assertInstanceOf(RealtimeClientEventResponseCreate.class, initial.get(1));
        }
        if (scenario == Scenario.AUDIO) {
            turn.accept(event("{\"type\":\"response.output_audio.delta\",\"delta\":\"AQID\"}"));
            turn.accept(event("{\"type\":\"response.output_audio_transcript.done\",\"transcript\":\"Hello\"}"));
            turn.accept(event(DONE));
        } else if (scenario == Scenario.FUNCTION) {
            assertTrue(turn.accept(functionCall("call-1")).isEmpty());
            assertTrue(turn.accept(functionCall("call-2")).isEmpty());
            assertFalse(turn.done);
            List<RealtimeClientEvent> outputs = turn.accept(event(TOOL_DONE));
            assertEquals(3, outputs.size());
            for (int index = 0; index < 2; index++) {
                RealtimeClientEventConversationItemCreate create
                    = assertInstanceOf(RealtimeClientEventConversationItemCreate.class, outputs.get(index));
                RealtimeConversationItemFunctionCallOutput output
                    = assertInstanceOf(RealtimeConversationItemFunctionCallOutput.class, create.getItem());
                assertEquals("call-" + (index + 1), output.getCallId());
                Map<?, ?> result = BinaryData.fromString(output.getOutput()).toObject(Map.class);
                assertEquals("Seattle", result.get("city"));
                assertEquals("sunny", result.get("condition"));
                assertEquals(72, result.get("temperature_f"));
            }
            assertInstanceOf(RealtimeClientEventResponseCreate.class, outputs.get(2));
            assertFalse(turn.done);
            turn.accept(event("{\"type\":\"response.output_text.done\",\"text\":\"Sunny in Seattle.\"}"));
            turn.accept(event(DONE));
        }
        turn.assertComplete();
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "missing-audio",
            "empty-audio",
            "missing-transcript",
            "empty-transcript",
            "duplicate-transcript",
            "missing-done" })
    public void incompleteAudioTurnsFail(String omission) {
        Turn turn = new Turn(Scenario.AUDIO);
        turn.accept(event(SESSION));
        assertThrows(AssertionError.class, () -> {
            if (!"missing-audio".equals(omission)) {
                String delta = "empty-audio".equals(omission) ? "" : "AQID";
                turn.accept(event("{\"type\":\"response.output_audio.delta\",\"delta\":\"" + delta + "\"}"));
            }
            if (!"missing-transcript".equals(omission)) {
                String transcript = "empty-transcript".equals(omission) ? " " : "Hello";
                RealtimeServerEvent transcriptEvent = event(
                    "{\"type\":\"response.output_audio_transcript.done\"," + "\"transcript\":\"" + transcript + "\"}");
                turn.accept(transcriptEvent);
                if ("duplicate-transcript".equals(omission)) {
                    turn.accept(transcriptEvent);
                }
            }
            if (!"missing-done".equals(omission)) {
                turn.accept(event(DONE));
            }
            turn.assertComplete();
        });
    }

    @Test
    public void missingHandshakeServiceErrorsAndMissingToolResultFail() {
        assertThrows(AssertionError.class, () -> new Turn(Scenario.LIFECYCLE).accept(event(DONE)));
        Turn audio = new Turn(Scenario.AUDIO);
        audio.accept(event(SESSION));
        assertThrows(AssertionError.class, () -> audio.accept(event(
            "{\"type\":\"error\",\"error\":{\"type\":\"server_error\",\"message\":\"boom\",\"code\":\"failed\"}}")));
        Turn tool = new Turn(Scenario.FUNCTION);
        tool.accept(event(SESSION));
        tool.accept(event(TOOL_DONE));
        assertFalse(tool.done);
        assertThrows(AssertionError.class, tool::assertComplete);
        tool.accept(event("{\"type\":\"response.output_text.done\",\"text\":\"Unverified answer\"}"));
        tool.accept(event(DONE));
        assertThrows(AssertionError.class, tool::assertComplete);
    }

    private static AgentsClientBuilder liveBuilder() {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        assertNotNull(endpoint, "FOUNDRY_PROJECT_ENDPOINT is required for live testing.");
        return new AgentsClientBuilder().endpoint(endpoint)
            .allowPreview(true)
            .credential(new DefaultAzureCredentialBuilder().build());
    }

    private static VoiceAgentDefinition definition(Scenario scenario) {
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_VOICE_MODEL_NAME");
        assertNotNull(model, "FOUNDRY_VOICE_MODEL_NAME is required for live testing.");
        return definition(scenario, model);
    }

    @Test
    public void functionToolParametersSerializeAsObject() {
        Map<?, ?> request
            = BinaryData.fromObject(new CreateAgentVersionInput(definition(Scenario.FUNCTION, "test-model")))
                .toObject(Map.class);
        Map<?, ?> agentDefinition = assertInstanceOf(Map.class, request.get("definition"));
        List<?> tools = assertInstanceOf(List.class, agentDefinition.get("tools"));
        assertEquals(1, tools.size());
        Map<?, ?> tool = assertInstanceOf(Map.class, tools.get(0));
        assertEquals("get_weather", tool.get("name"));
        Map<?, ?> parameters = assertInstanceOf(Map.class, tool.get("parameters"));
        assertEquals("object", parameters.get("type"));
        assertEquals(Collections.singletonList("city"), parameters.get("required"));
        Map<?, ?> properties = assertInstanceOf(Map.class, parameters.get("properties"));
        Map<?, ?> city = assertInstanceOf(Map.class, properties.get("city"));
        assertEquals("string", city.get("type"));
        assertEquals("City name, e.g. Seattle.", city.get("description"));
    }

    private static VoiceAgentDefinition definition(Scenario scenario, String model) {
        VoiceAgentDefinition definition = new VoiceAgentDefinition().setModelType(VoiceModelType.MANAGED)
            .setModel(model)
            .setInstructions("You are a helpful voice assistant. Keep replies short.");
        if (scenario == Scenario.FUNCTION) {
            Map<String, Object> citySchema = new LinkedHashMap<>();
            citySchema.put("type", "string");
            citySchema.put("description", "City name, e.g. Seattle.");
            Map<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("type", "object");
            parameters.put("properties", Collections.singletonMap("city", citySchema));
            parameters.put("required", Collections.singletonList("city"));
            definition
                .setInstructions("You are a helpful voice assistant. Use the get_weather tool when the "
                    + "caller asks about the weather, then answer using its result.")
                .setOutputModalities(Collections.singletonList(VoiceOutputModality.TEXT))
                .setTools(Collections.singletonList(
                    new VoiceAgentFunctionTool("get_weather").setDescription("Get the current weather for a city.")
                        .setParameters(BinaryData.fromObject(parameters))));
        } else {
            definition.setOutputModalities(Collections.singletonList(VoiceOutputModality.AUDIO))
                .setAudio(
                    new VoiceAgentAudioConfig().setOutput(new VoiceAgentAudioOutputConfig().setVoice("en-US-AvaNeural")
                        .setVoiceType(VoiceType.AZURE_STANDARD)));
        }
        return definition;
    }

    private static RealtimeServerEvent event(String json) {
        return BinaryData.fromString(json).toObject(RealtimeServerEvent.class);
    }

    private static RealtimeServerEvent functionCall(String callId) {
        return event("{\"type\":\"response.function_call_arguments.done\",\"name\":\"get_weather\"," + "\"call_id\":\""
            + callId + "\",\"arguments\":\"{\\\"city\\\":\\\"Seattle\\\"}\"}");
    }

    private static final class Turn {
        private final Scenario scenario;
        private final List<RealtimeClientEvent> pending = new ArrayList<>();
        private boolean started;
        private boolean done;
        private int audioDeltas;
        private long audioBytes;
        private int transcripts;
        private int toolCalls;
        private String finalText;

        private Turn(Scenario scenario) {
            this.scenario = scenario;
        }

        private List<RealtimeClientEvent> accept(RealtimeServerEvent event) {
            if (event instanceof RealtimeServerEventRealtimeServerEventError) {
                fail("Session error: " + ((RealtimeServerEventRealtimeServerEventError) event).getError().getMessage());
            }
            if (!started) {
                assertInstanceOf(RealtimeServerEventSessionCreated.class, event,
                    "The first event must be session.created.");
                assertEquals("session.created", event.getType().toString());
                started = true;
                done = scenario == Scenario.LIFECYCLE;
                if (!done) {
                    String prompt = scenario == Scenario.FUNCTION
                        ? "What's the weather like in Seattle right now?"
                        : "Say the word 'hello' and nothing else.";
                    return Arrays
                        .asList(new RealtimeClientEventConversationItemCreate(new RealtimeConversationItemMessageUser(
                            Collections.singletonList(new RealtimeConversationItemMessageUserContent()
                                .setType(RealtimeConversationItemMessageUserContentType.INPUT_TEXT)
                                .setText(prompt)))),
                            new RealtimeClientEventResponseCreate());
                }
            } else if (event instanceof RealtimeServerEventResponseAudioDelta) {
                audioDeltas++;
                byte[] delta = ((RealtimeServerEventResponseAudioDelta) event).getDelta();
                assertNotNull(delta);
                audioBytes += delta.length;
            } else if (event instanceof RealtimeServerEventResponseAudioTranscriptDone) {
                transcripts++;
                String transcript = ((RealtimeServerEventResponseAudioTranscriptDone) event).getTranscript();
                assertNotNull(transcript);
                assertFalse(transcript.trim().isEmpty());
            } else if (event instanceof RealtimeServerEventResponseFunctionCallArgumentsDone) {
                RealtimeServerEventResponseFunctionCallArgumentsDone call
                    = (RealtimeServerEventResponseFunctionCallArgumentsDone) event;
                assertEquals("get_weather", call.getName());
                Map<?, ?> arguments = BinaryData.fromString(call.getArguments()).toObject(Map.class);
                String city = assertInstanceOf(String.class, arguments.get("city"));
                assertNotNull(call.getCallId());
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("city", city);
                result.put("condition", "sunny");
                result.put("temperature_f", 72);
                pending
                    .add(new RealtimeClientEventConversationItemCreate(new RealtimeConversationItemFunctionCallOutput(
                        call.getCallId(), BinaryData.fromObject(result).toString())));
                toolCalls++;
            } else if (event instanceof RealtimeServerEventResponseTextDone) {
                finalText = ((RealtimeServerEventResponseTextDone) event).getText();
            } else if (event instanceof RealtimeServerEventResponseDone) {
                if (!pending.isEmpty()) {
                    List<RealtimeClientEvent> outputs = new ArrayList<>(pending);
                    pending.clear();
                    outputs.add(new RealtimeClientEventResponseCreate());
                    return outputs;
                }
                RealtimeServerEventResponseDone response = (RealtimeServerEventResponseDone) event;
                assertNotNull(response.getResponse());
                done = scenario != Scenario.FUNCTION
                    || response.getResponse().getOutput() == null
                    || response.getResponse()
                        .getOutput()
                        .stream()
                        .noneMatch(item -> "function_call".equals(item.getType().toString()));
            }
            return Collections.emptyList();
        }

        private void assertComplete() {
            assertTrue(started, "Did not receive session.created.");
            assertTrue(done, "Did not receive the final response.done within the timeout.");
            if (scenario == Scenario.AUDIO) {
                assertTrue(audioDeltas > 0, "Expected at least one audio delta.");
                assertTrue(audioBytes > 0, "Expected non-empty streamed audio.");
                assertEquals(1, transcripts, "Expected exactly one audio-transcript-done event.");
            } else if (scenario == Scenario.FUNCTION) {
                assertTrue(toolCalls > 0, "Expected at least one get_weather call.");
                assertNotNull(finalText);
                assertFalse(finalText.trim().isEmpty(), "Expected a non-empty final text reply.");
            }
        }
    }
}
