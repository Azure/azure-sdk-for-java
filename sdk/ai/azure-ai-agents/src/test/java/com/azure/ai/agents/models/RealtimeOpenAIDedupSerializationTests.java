// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.openai.models.realtime.LogProbProperties;
import com.openai.models.realtime.NoiseReductionType;
import com.openai.models.realtime.RealtimeReasoning;
import com.openai.models.realtime.RealtimeReasoningEffort;
import com.openai.models.realtime.RealtimeResponseStatus;
import com.openai.models.realtime.RealtimeResponseUsage;
import com.openai.models.responses.ResponsePrompt;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeOpenAIDedupSerializationTests {

    private static final String USAGE_JSON = "{\"total_tokens\":15,\"input_tokens\":10,\"output_tokens\":5,"
        + "\"input_token_details\":{\"text_tokens\":7,\"audio_tokens\":3},"
        + "\"output_token_details\":{\"text_tokens\":4,\"audio_tokens\":1}}";

    private static final String RESPONSE_FIELDS_JSON
        = "{\"object\":\"realtime.response\"," + "\"status\":\"failed\",\"status_details\":{\"type\":\"failed\","
            + "\"reason\":\"content_filter\",\"error\":{\"type\":\"server_error\",\"code\":\"bad_request\"}},"
            + "\"usage\":" + USAGE_JSON + "}";

    @Test
    public void testRealtimeReasoningAndPromptRoundTrip() throws IOException {
        RealtimeReasoningEffort[] efforts = {
            RealtimeReasoningEffort.MINIMAL,
            RealtimeReasoningEffort.LOW,
            RealtimeReasoningEffort.MEDIUM,
            RealtimeReasoningEffort.HIGH,
            RealtimeReasoningEffort.XHIGH };

        for (RealtimeReasoningEffort effort : efforts) {
            RealtimeReasoning reasoning = RealtimeReasoning.builder().effort(effort).build();
            ResponsePrompt prompt = ResponsePrompt.builder().id("pmpt_123").version("2").build();
            RealtimeSessionCreateRequestGA original = new RealtimeSessionCreateRequestGA().setInstructions("Be concise")
                .setReasoning(reasoning)
                .setPrompt(prompt);

            String json = serialize(original);
            assertTrue(json.contains("\"effort\":\"" + effort.asString() + "\""));
            assertTrue(json.contains("\"prompt\":{\"id\":\"pmpt_123\""));

            RealtimeSessionCreateRequestGA result;
            try (JsonReader reader = JsonProviders.createReader(json)) {
                result = RealtimeSessionCreateRequestGA.fromJson(reader);
            }

            assertEquals(effort, result.getReasoning().effort().get());
            assertEquals("pmpt_123", result.getPrompt().id());
            assertEquals("2", result.getPrompt().version().get());
            assertEquals(json, serialize(result));
        }
    }

    @Test
    public void testAbsentRealtimeReasoningAndPromptRemainAbsent() throws IOException {
        String json = serialize(new RealtimeSessionCreateRequestGA().setInstructions("No reasoning"));

        assertFalse(json.contains("\"reasoning\""));
        assertFalse(json.contains("\"prompt\""));
    }

    @Test
    public void testRealtimeReasoningAcrossVoiceSessionModels() throws IOException {
        String input = "{\"reasoning\":{\"effort\":\"high\"}}";

        VoiceAgentResponseCreateParams createParams;
        VoiceAgentSessionUpdateConfig updateConfig;
        VoiceAgentSessionResponseConfig responseConfig;
        try (JsonReader reader = JsonProviders.createReader(input)) {
            createParams = VoiceAgentResponseCreateParams.fromJson(reader);
        }
        try (JsonReader reader = JsonProviders.createReader(input)) {
            updateConfig = VoiceAgentSessionUpdateConfig.fromJson(reader);
        }
        try (JsonReader reader = JsonProviders.createReader(input)) {
            responseConfig = VoiceAgentSessionResponseConfig.fromJson(reader);
        }

        assertReasoningRoundTrip(createParams.getReasoning(), serialize(createParams));
        assertReasoningRoundTrip(updateConfig.getReasoning(), serialize(updateConfig));
        assertReasoningRoundTrip(responseConfig.getReasoning(), serialize(responseConfig));
    }

    @Test
    public void testNoiseReductionEnumRoundTrip() throws IOException {
        NoiseReductionType[] types = { NoiseReductionType.NEAR_FIELD, NoiseReductionType.FAR_FIELD };

        for (NoiseReductionType type : types) {
            RealtimeSessionCreateRequestGAAudioInputNoiseReduction realtime
                = new RealtimeSessionCreateRequestGAAudioInputNoiseReduction().setType(type);
            RealtimeTranscriptionSessionCreateRequestGAAudioInputNoiseReduction transcription
                = new RealtimeTranscriptionSessionCreateRequestGAAudioInputNoiseReduction().setType(type);

            String realtimeJson = serialize(realtime);
            String transcriptionJson = serialize(transcription);
            assertEquals("{\"type\":\"" + type.asString() + "\"}", realtimeJson);
            assertEquals(realtimeJson, transcriptionJson);

            try (JsonReader reader = JsonProviders.createReader(realtimeJson)) {
                assertEquals(type, RealtimeSessionCreateRequestGAAudioInputNoiseReduction.fromJson(reader).getType());
            }
            try (JsonReader reader = JsonProviders.createReader(transcriptionJson)) {
                assertEquals(type,
                    RealtimeTranscriptionSessionCreateRequestGAAudioInputNoiseReduction.fromJson(reader).getType());
            }
        }
    }

    @Test
    public void testLogProbsRoundTripThroughPolymorphicServerEvent() throws IOException {
        String input = "{\"type\":\"conversation.item.input_audio_transcription.delta\","
            + "\"event_id\":\"evt_1\",\"item_id\":\"item_1\",\"content_index\":0,\"delta\":\"hello\","
            + "\"logprobs\":[{\"token\":\"hello\",\"logprob\":-0.25,\"bytes\":[104,101]}]}";

        RealtimeServerEvent event;
        try (JsonReader reader = JsonProviders.createReader(input)) {
            event = RealtimeServerEvent.fromJson(reader);
        }

        RealtimeServerEventConversationItemInputAudioTranscriptionDelta delta
            = assertInstanceOf(RealtimeServerEventConversationItemInputAudioTranscriptionDelta.class, event);
        LogProbProperties logProb = delta.getLogprobs().get(0);
        assertEquals("hello", logProb.token());
        assertEquals(-0.25, logProb.logprob());
        assertEquals(2, logProb.bytes().size());

        String json = serialize(delta);
        RealtimeServerEvent reparsed;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            reparsed = RealtimeServerEvent.fromJson(reader);
        }
        assertEquals(json, serialize(
            assertInstanceOf(RealtimeServerEventConversationItemInputAudioTranscriptionDelta.class, reparsed)));
    }

    @Test
    public void testStatusAndUsageRoundTripThroughPolymorphicServerEvent() throws IOException {
        String input = "{\"type\":\"response.done\",\"event_id\":\"evt_2\",\"response\":{\"id\":\"resp_1\","
            + RESPONSE_FIELDS_JSON.substring(1) + "}";

        RealtimeServerEvent event;
        try (JsonReader reader = JsonProviders.createReader(input)) {
            event = RealtimeServerEvent.fromJson(reader);
        }

        RealtimeServerEventResponseDone done = assertInstanceOf(RealtimeServerEventResponseDone.class, event);
        assertResponseFields(done.getResponse().getStatusDetails(), done.getResponse().getUsage());

        String json = serialize(done);
        RealtimeServerEvent reparsed;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            reparsed = RealtimeServerEvent.fromJson(reader);
        }
        RealtimeServerEventResponseDone reparsedDone
            = assertInstanceOf(RealtimeServerEventResponseDone.class, reparsed);
        assertResponseFields(reparsedDone.getResponse().getStatusDetails(), reparsedDone.getResponse().getUsage());
        assertEquals(json, serialize(reparsedDone));
    }

    @Test
    public void testStatusAndUsageAcrossVoiceResponseModels() throws IOException {
        VoiceResponseBase responseBase;
        VoiceAgentRealtimeResponseBase realtimeResponseBase;
        VoiceAgentRealtimeResponse realtimeResponse;
        VoiceResponse response;
        VoiceConversation conversation;

        try (JsonReader reader = JsonProviders.createReader(RESPONSE_FIELDS_JSON)) {
            responseBase = VoiceResponseBase.fromJson(reader);
        }
        try (
            JsonReader reader = JsonProviders.createReader("{\"id\":\"resp_1\"," + RESPONSE_FIELDS_JSON.substring(1))) {
            realtimeResponseBase = VoiceAgentRealtimeResponseBase.fromJson(reader);
        }
        try (
            JsonReader reader = JsonProviders.createReader("{\"id\":\"resp_1\"," + RESPONSE_FIELDS_JSON.substring(1))) {
            realtimeResponse = VoiceAgentRealtimeResponse.fromJson(reader);
        }
        try (
            JsonReader reader = JsonProviders.createReader("{\"id\":\"resp_1\"," + RESPONSE_FIELDS_JSON.substring(1))) {
            response = VoiceResponse.fromJson(reader);
        }
        try (JsonReader reader = JsonProviders.createReader(
            "{\"id\":\"conv_1\",\"status\":\"completed\"," + "\"created_at\":1,\"usage\":" + USAGE_JSON + "}")) {
            conversation = VoiceConversation.fromJson(reader);
        }

        assertResponseFields(responseBase.getStatusDetails(), responseBase.getUsage());
        assertResponseFields(realtimeResponseBase.getStatusDetails(), realtimeResponseBase.getUsage());
        assertResponseFields(realtimeResponse.getStatusDetails(), realtimeResponse.getUsage());
        assertResponseFields(response.getStatusDetails(), response.getUsage());
        assertUsage(conversation.getUsage());

        assertStable(responseBase, VoiceResponseBase::fromJson);
        assertStable(realtimeResponseBase, VoiceAgentRealtimeResponseBase::fromJson);
        assertStable(realtimeResponse, VoiceAgentRealtimeResponse::fromJson);
        assertStable(response, VoiceResponse::fromJson);
        assertStable(conversation, VoiceConversation::fromJson);
    }

    @Test
    public void testAbsentStatusAndUsageRemainAbsent() throws IOException {
        VoiceAgentRealtimeResponseBase response;
        try (JsonReader reader = JsonProviders.createReader("{\"id\":\"resp_1\"}")) {
            response = VoiceAgentRealtimeResponseBase.fromJson(reader);
        }

        assertNull(response.getStatusDetails());
        assertNull(response.getUsage());
        String json = serialize(response);
        assertFalse(json.contains("\"status_details\""));
        assertFalse(json.contains("\"usage\""));
    }

    private static void assertReasoningRoundTrip(RealtimeReasoning reasoning, String serialized) {
        assertNotNull(reasoning);
        assertEquals(RealtimeReasoningEffort.HIGH, reasoning.effort().get());
        assertTrue(serialized.contains("\"reasoning\":{\"effort\":\"high\"}"), serialized);
    }

    private static void assertResponseFields(RealtimeResponseStatus status, RealtimeResponseUsage usage) {
        assertNotNull(status);
        assertEquals(RealtimeResponseStatus.Type.FAILED, status.type().get());
        assertEquals(RealtimeResponseStatus.Reason.CONTENT_FILTER, status.reason().get());
        assertEquals("server_error", status.error().get().type().get());
        assertEquals("bad_request", status.error().get().code().get());
        assertUsage(usage);
    }

    private static void assertUsage(RealtimeResponseUsage usage) {
        assertNotNull(usage);
        assertEquals(15L, usage.totalTokens().get());
        assertEquals(10L, usage.inputTokens().get());
        assertEquals(5L, usage.outputTokens().get());
        assertEquals(7L, usage.inputTokenDetails().get().textTokens().get());
        assertEquals(3L, usage.inputTokenDetails().get().audioTokens().get());
        assertEquals(4L, usage.outputTokenDetails().get().textTokens().get());
        assertEquals(1L, usage.outputTokenDetails().get().audioTokens().get());
    }

    private static <T extends JsonSerializable<?>> void assertStable(T value, Deserializer<T> deserializer)
        throws IOException {
        String json = serialize(value);
        T result;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            result = deserializer.deserialize(reader);
        }
        assertEquals(json, serialize(result));
    }

    private static String serialize(JsonSerializable<?> value) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(output)) {
            value.toJson(writer);
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

    @FunctionalInterface
    private interface Deserializer<T> {
        T deserialize(JsonReader reader) throws IOException;
    }
}
