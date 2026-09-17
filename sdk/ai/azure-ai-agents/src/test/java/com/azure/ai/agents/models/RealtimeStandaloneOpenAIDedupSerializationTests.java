// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.openai.models.realtime.AudioTranscription.Delay;
import com.openai.models.realtime.RateLimitsUpdatedEvent.RateLimit;
import com.openai.models.realtime.RealtimeAudioInputTurnDetection.SemanticVad.Eagerness;
import com.openai.models.realtime.RealtimeResponse;
import com.openai.models.realtime.RealtimeResponseCreateParams.Conversation;
import com.openai.models.realtime.RealtimeSessionCreateRequest.Model;
import com.openai.models.realtime.RealtimeSessionCreateRequest.OutputModality;
import com.openai.models.realtime.RealtimeTruncationRetentionRatio;
import com.openai.models.realtime.ResponseContentPartAddedEvent.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeStandaloneOpenAIDedupSerializationTests {

    @Test
    public void testPcmRateRoundTripPolymorphically() throws IOException {
        com.openai.models.realtime.RealtimeAudioFormats.AudioPcm.Rate rate
            = com.openai.models.realtime.RealtimeAudioFormats.AudioPcm.Rate._24000;
        RealtimePcmAudioFormat original = new RealtimePcmAudioFormat().setRate(rate);
        RealtimePcmAudioFormat result = assertInstanceOf(RealtimePcmAudioFormat.class,
            deserialize(serialize(original), RealtimeAudioFormat::fromJson));

        assertEquals(rate, result.getRate());
        assertEquals(serialize(original), serialize(result));
    }

    @Test
    public void testSessionModelAndOutputModalityValuesRoundTrip() throws IOException {
        String[] modelValues = {
            "gpt-realtime",
            "gpt-realtime-1.5",
            "gpt-realtime-2",
            "gpt-realtime-2.1",
            "gpt-realtime-2.1-mini",
            "gpt-realtime-2025-08-28",
            "gpt-4o-realtime-preview",
            "gpt-4o-realtime-preview-2024-10-01",
            "gpt-4o-realtime-preview-2024-12-17",
            "gpt-4o-realtime-preview-2025-06-03",
            "gpt-4o-mini-realtime-preview",
            "gpt-4o-mini-realtime-preview-2024-12-17",
            "gpt-realtime-mini",
            "gpt-realtime-mini-2025-10-06",
            "gpt-realtime-mini-2025-12-15",
            "gpt-audio-1.5",
            "gpt-audio-mini",
            "gpt-audio-mini-2025-10-06",
            "gpt-audio-mini-2025-12-15" };

        for (String modelValue : modelValues) {
            RealtimeSessionConfiguration original = new RealtimeSessionConfiguration().setModel(Model.of(modelValue))
                .setOutputModalities(Arrays.asList(OutputModality.TEXT, OutputModality.AUDIO));
            String json = serialize(original);
            RealtimeSessionConfiguration result = deserialize(json, RealtimeSessionConfiguration::fromJson);

            assertEquals(modelValue, result.getModel().asString());
            assertEquals(OutputModality.TEXT, result.getOutputModalities().get(0));
            assertEquals(OutputModality.AUDIO, result.getOutputModalities().get(1));
            assertEquals(json, serialize(result));
        }
    }

    @Test
    public void testTruncationAndTracingUnionsRoundTrip() throws IOException {
        RealtimeTruncationRetentionRatio retentionRatio = RealtimeTruncationRetentionRatio.builder()
            .retentionRatio(0.8)
            .tokenLimits(RealtimeTruncationRetentionRatio.TokenLimits.builder().postInstructions(1024).build())
            .build();
        for (String strategy : Arrays.asList("auto", "disabled")) {
            RealtimeSessionConfiguration original = new RealtimeSessionConfiguration().setTruncation(strategy);
            RealtimeSessionConfiguration result
                = deserialize(serialize(original), RealtimeSessionConfiguration::fromJson);
            assertEquals(strategy, result.getTruncationAsString());
            assertStable(original, RealtimeSessionConfiguration::fromJson);
        }
        RealtimeSessionConfiguration retained = new RealtimeSessionConfiguration().setTruncation(retentionRatio);
        RealtimeSessionConfiguration retainedResult
            = deserialize(serialize(retained), RealtimeSessionConfiguration::fromJson);
        assertEquals(0.8, retainedResult.getTruncationAsRealtimeTruncationRetentionRatio().retentionRatio());
        assertStable(retained, RealtimeSessionConfiguration::fromJson);

        RealtimeSessionTracingConfiguration tracing
            = new RealtimeSessionTracingConfiguration().setWorkflowName("voice").setGroupId("group-1");
        RealtimeSessionConfiguration traced = new RealtimeSessionConfiguration().setTracing(tracing);
        RealtimeSessionConfiguration tracedResult
            = deserialize(serialize(traced), RealtimeSessionConfiguration::fromJson);
        assertEquals("voice", tracedResult.getTracingAsRealtimeSessionTracingConfiguration().getWorkflowName());
        assertStable(new RealtimeSessionConfiguration().setTracing("auto"), RealtimeSessionConfiguration::fromJson);
        assertFalse(serialize(new RealtimeSessionConfiguration().setTracing((String) null)).contains("tracing"));
    }

    @Test
    public void testConversationItemStatusesRoundTripPolymorphically() throws IOException {
        com.openai.models.realtime.RealtimeConversationItemFunctionCall.Status[] functionStatuses = {
            com.openai.models.realtime.RealtimeConversationItemFunctionCall.Status.COMPLETED,
            com.openai.models.realtime.RealtimeConversationItemFunctionCall.Status.INCOMPLETE,
            com.openai.models.realtime.RealtimeConversationItemFunctionCall.Status.IN_PROGRESS };
        for (com.openai.models.realtime.RealtimeConversationItemFunctionCall.Status status : functionStatuses) {
            RealtimeConversationItemFunctionCall original
                = new RealtimeConversationItemFunctionCall("lookup", "{}").setStatus(status);
            RealtimeConversationItemFunctionCall result = assertInstanceOf(RealtimeConversationItemFunctionCall.class,
                deserialize(serialize(original), RealtimeConversationItem::fromJson));
            assertEquals(status, result.getStatus());
            assertEquals(serialize(original), serialize(result));
        }

        com.openai.models.realtime.RealtimeConversationItemFunctionCallOutput.Status[] outputStatuses = {
            com.openai.models.realtime.RealtimeConversationItemFunctionCallOutput.Status.COMPLETED,
            com.openai.models.realtime.RealtimeConversationItemFunctionCallOutput.Status.INCOMPLETE,
            com.openai.models.realtime.RealtimeConversationItemFunctionCallOutput.Status.IN_PROGRESS };
        for (com.openai.models.realtime.RealtimeConversationItemFunctionCallOutput.Status status : outputStatuses) {
            RealtimeConversationItemFunctionCallOutput original
                = new RealtimeConversationItemFunctionCallOutput("call-1", "ok").setStatus(status);
            RealtimeConversationItemFunctionCallOutput result
                = assertInstanceOf(RealtimeConversationItemFunctionCallOutput.class,
                    deserialize(serialize(original), RealtimeConversationItem::fromJson));
            assertEquals(status, result.getStatus());
        }
    }

    @Test
    public void testConversationMessageContentRoundTripPolymorphically() throws IOException {
        com.openai.models.realtime.RealtimeConversationItemAssistantMessage.Content.Type[] assistantTypes = {
            com.openai.models.realtime.RealtimeConversationItemAssistantMessage.Content.Type.OUTPUT_TEXT,
            com.openai.models.realtime.RealtimeConversationItemAssistantMessage.Content.Type.OUTPUT_AUDIO };
        com.openai.models.realtime.RealtimeConversationItemAssistantMessage.Status[] assistantStatuses = {
            com.openai.models.realtime.RealtimeConversationItemAssistantMessage.Status.COMPLETED,
            com.openai.models.realtime.RealtimeConversationItemAssistantMessage.Status.INCOMPLETE,
            com.openai.models.realtime.RealtimeConversationItemAssistantMessage.Status.IN_PROGRESS };
        for (com.openai.models.realtime.RealtimeConversationItemAssistantMessage.Content.Type contentType : assistantTypes) {
            for (com.openai.models.realtime.RealtimeConversationItemAssistantMessage.Status status : assistantStatuses) {
                com.openai.models.realtime.RealtimeConversationItemAssistantMessage.Content content
                    = com.openai.models.realtime.RealtimeConversationItemAssistantMessage.Content.builder()
                        .type(contentType)
                        .text("hello")
                        .build();
                RealtimeConversationItemAssistantMessage assistant
                    = new RealtimeConversationItemAssistantMessage(Arrays.asList(content)).setStatus(status);
                RealtimeConversationItemAssistantMessage result
                    = assertInstanceOf(RealtimeConversationItemAssistantMessage.class,
                        deserialize(serialize(assistant), RealtimeConversationItem::fromJson));
                assertEquals(contentType, result.getContent().get(0).type().get());
                assertEquals(status, result.getStatus());
                assertEquals(serialize(assistant), serialize(result));
            }
        }

        com.openai.models.realtime.RealtimeConversationItemSystemMessage.Content systemContent
            = com.openai.models.realtime.RealtimeConversationItemSystemMessage.Content.builder()
                .type(com.openai.models.realtime.RealtimeConversationItemSystemMessage.Content.Type.INPUT_TEXT)
                .text("policy")
                .build();
        com.openai.models.realtime.RealtimeConversationItemSystemMessage.Status[] systemStatuses = {
            com.openai.models.realtime.RealtimeConversationItemSystemMessage.Status.COMPLETED,
            com.openai.models.realtime.RealtimeConversationItemSystemMessage.Status.INCOMPLETE,
            com.openai.models.realtime.RealtimeConversationItemSystemMessage.Status.IN_PROGRESS };
        for (com.openai.models.realtime.RealtimeConversationItemSystemMessage.Status status : systemStatuses) {
            RealtimeConversationItemSystemMessage system
                = new RealtimeConversationItemSystemMessage(Arrays.asList(systemContent)).setStatus(status);
            RealtimeConversationItemSystemMessage result = assertInstanceOf(RealtimeConversationItemSystemMessage.class,
                deserialize(serialize(system), RealtimeConversationItem::fromJson));
            assertEquals("policy", result.getContent().get(0).text().get());
            assertEquals(status, result.getStatus());
            assertEquals(serialize(system), serialize(result));
        }

        com.openai.models.realtime.RealtimeConversationItemUserMessage.Content.Type[] userTypes = {
            com.openai.models.realtime.RealtimeConversationItemUserMessage.Content.Type.INPUT_TEXT,
            com.openai.models.realtime.RealtimeConversationItemUserMessage.Content.Type.INPUT_AUDIO,
            com.openai.models.realtime.RealtimeConversationItemUserMessage.Content.Type.INPUT_IMAGE };
        com.openai.models.realtime.RealtimeConversationItemUserMessage.Content.Detail[] details = {
            com.openai.models.realtime.RealtimeConversationItemUserMessage.Content.Detail.AUTO,
            com.openai.models.realtime.RealtimeConversationItemUserMessage.Content.Detail.LOW,
            com.openai.models.realtime.RealtimeConversationItemUserMessage.Content.Detail.HIGH };
        com.openai.models.realtime.RealtimeConversationItemUserMessage.Status[] userStatuses = {
            com.openai.models.realtime.RealtimeConversationItemUserMessage.Status.COMPLETED,
            com.openai.models.realtime.RealtimeConversationItemUserMessage.Status.INCOMPLETE,
            com.openai.models.realtime.RealtimeConversationItemUserMessage.Status.IN_PROGRESS };
        for (int i = 0; i < userTypes.length; i++) {
            com.openai.models.realtime.RealtimeConversationItemUserMessage.Content content
                = com.openai.models.realtime.RealtimeConversationItemUserMessage.Content.builder()
                    .type(userTypes[i])
                    .detail(details[i])
                    .text("user content")
                    .build();
            RealtimeConversationItemUserMessage user
                = new RealtimeConversationItemUserMessage(Arrays.asList(content)).setStatus(userStatuses[i]);
            RealtimeConversationItemUserMessage result = assertInstanceOf(RealtimeConversationItemUserMessage.class,
                deserialize(serialize(user), RealtimeConversationItem::fromJson));
            assertEquals(userTypes[i], result.getContent().get(0).type().get());
            assertEquals(details[i], result.getContent().get(0).detail().get());
            assertEquals(userStatuses[i], result.getStatus());
            assertEquals(serialize(user), serialize(result));
        }
    }

    @Test
    public void testServerEventStandaloneModelsRoundTripPolymorphically() throws IOException {
        RealtimeConversationCreatedEvent conversationCreated = assertInstanceOf(RealtimeConversationCreatedEvent.class,
            deserialize("{\"type\":\"conversation.created\",\"event_id\":\"evt-1\",\"conversation\":"
                + "{\"id\":\"conv-1\",\"object\":\"realtime.conversation\"}}", RealtimeServerEvent::fromJson));
        assertEquals("conv-1", conversationCreated.getConversation().id().get());
        assertStable(conversationCreated, RealtimeServerEvent::fromJson);

        RealtimeConversationItemInputAudioTranscriptionFailedEvent failed
            = assertInstanceOf(RealtimeConversationItemInputAudioTranscriptionFailedEvent.class,
                deserialize(
                    "{\"type\":\"conversation.item.input_audio_transcription.failed\",\"event_id\":\"evt-2\","
                        + "\"item_id\":\"item-1\",\"content_index\":0,\"error\":{\"type\":\"server_error\","
                        + "\"code\":\"bad_audio\",\"message\":\"bad audio\",\"param\":\"audio\"}}",
                    RealtimeServerEvent::fromJson));
        assertEquals("bad_audio", failed.getError().code().get());
        assertStable(failed, RealtimeServerEvent::fromJson);

        RealtimeErrorEvent errorEvent = assertInstanceOf(RealtimeErrorEvent.class,
            deserialize("{\"type\":\"error\",\"event_id\":\"evt-3\",\"error\":{\"type\":\"server_error\","
                + "\"code\":\"bad_request\",\"message\":\"failed\",\"param\":\"request\","
                + "\"event_id\":\"source-event\"}}", RealtimeServerEvent::fromJson));
        assertEquals("source-event", errorEvent.getError().eventId().get());
        assertStable(errorEvent, RealtimeServerEvent::fromJson);

        for (RateLimit.Name name : new RateLimit.Name[] { RateLimit.Name.REQUESTS, RateLimit.Name.TOKENS }) {
            String json = "{\"type\":\"rate_limits.updated\",\"event_id\":\"evt-4\",\"rate_limits\":[{" + "\"name\":\""
                + name.asString() + "\",\"limit\":100,\"remaining\":75,\"reset_seconds\":1.5}]}";
            RealtimeRateLimitsUpdatedEvent result = assertInstanceOf(RealtimeRateLimitsUpdatedEvent.class,
                deserialize(json, RealtimeServerEvent::fromJson));
            assertEquals(name, result.getRateLimits().get(0).name().get());
            assertStable(result, RealtimeServerEvent::fromJson);
        }
    }

    @Test
    public void testContentPartTypesRoundTripPolymorphically() throws IOException {
        for (Part.Type type : new Part.Type[] { Part.Type.TEXT, Part.Type.AUDIO }) {
            String json = "{\"type\":\"response.content_part.added\",\"event_id\":\"evt-5\","
                + "\"response_id\":\"resp-1\",\"item_id\":\"item-1\",\"output_index\":0,"
                + "\"content_index\":0,\"part\":{\"type\":\"" + type.asString() + "\",\"text\":\"hi\"}}";
            RealtimeResponseContentPartAddedEvent added = assertInstanceOf(RealtimeResponseContentPartAddedEvent.class,
                deserialize(json, RealtimeServerEvent::fromJson));
            assertEquals(type, added.getPart().type().get());
            assertStable(added, RealtimeServerEvent::fromJson);
        }

        for (com.openai.models.realtime.ResponseContentPartDoneEvent.Part.Type type : new com.openai.models.realtime.ResponseContentPartDoneEvent.Part.Type[] {
            com.openai.models.realtime.ResponseContentPartDoneEvent.Part.Type.TEXT,
            com.openai.models.realtime.ResponseContentPartDoneEvent.Part.Type.AUDIO }) {
            String json = "{\"type\":\"" + type.asString() + "\",\"text\":\"done\"}";
            RealtimeResponseContentPart result = deserialize(json, RealtimeResponseContentPart::fromJson);
            assertEquals(type, result.getType());
            assertEquals(json, serialize(result));
        }
    }

    @Test
    public void testDelayConversationAndEagernessValuesRoundTrip() throws IOException {
        Delay[] delays = { Delay.MINIMAL, Delay.LOW, Delay.MEDIUM, Delay.HIGH, Delay.XHIGH };
        for (Delay delay : delays) {
            AudioTranscription transcription = new AudioTranscription().setDelay(delay);
            assertEquals(delay, deserialize(serialize(transcription), AudioTranscription::fromJson).getDelay());
            VoiceAgentInputTranscription voiceTranscription
                = new VoiceAgentInputTranscription(VoiceAgentInputTranscriptionModel.GPT_REALTIME_WHISPER)
                    .setDelay(delay);
            assertEquals(delay,
                deserialize(serialize(voiceTranscription), VoiceAgentInputTranscription::fromJson).getDelay());
        }

        for (Conversation conversation : new Conversation[] { Conversation.AUTO, Conversation.NONE }) {
            VoiceAgentResponseCreateOptions original
                = new VoiceAgentResponseCreateOptions().setConversation(conversation);
            assertEquals(conversation,
                deserialize(serialize(original), VoiceAgentResponseCreateOptions::fromJson).getConversation());
        }

        for (Eagerness eagerness : new Eagerness[] {
            Eagerness.LOW,
            Eagerness.MEDIUM,
            Eagerness.HIGH,
            Eagerness.AUTO }) {
            RealtimeSemanticVadTurnDetection realtime = new RealtimeSemanticVadTurnDetection().setEagerness(eagerness);
            assertEquals(eagerness,
                deserialize(serialize(realtime), RealtimeSemanticVadTurnDetection::fromJson).getEagerness());
            VoiceAgentSemanticVadTurnDetection voice = new VoiceAgentSemanticVadTurnDetection().setEagerness(eagerness);
            assertEquals(eagerness,
                deserialize(serialize(voice), VoiceAgentSemanticVadTurnDetection::fromJson).getEagerness());
        }
    }

    @Test
    public void testTranscriptionTokenDetailsRoundTripPolymorphically() throws IOException {
        String json = "{\"type\":\"tokens\",\"input_tokens\":10,\"output_tokens\":5,\"total_tokens\":15,"
            + "\"input_token_details\":{\"audio_tokens\":7,\"text_tokens\":3}}";
        TranscriptTextUsageTokens usage
            = assertInstanceOf(TranscriptTextUsageTokens.class, deserialize(json, TranscriptTextUsage::fromJson));
        TranscriptTextInputTokenDetails details = usage.getInputTokenDetails();
        assertEquals(7L, details.getAudioTokens());
        assertEquals(3L, details.getTextTokens());
        assertStable(usage, TranscriptTextUsage::fromJson);
    }

    @Test
    public void testRealtimeResponseEnumsAndAbsentValues() throws IOException {
        RealtimeResponse.Status[] statuses = {
            RealtimeResponse.Status.COMPLETED,
            RealtimeResponse.Status.CANCELLED,
            RealtimeResponse.Status.FAILED,
            RealtimeResponse.Status.INCOMPLETE,
            RealtimeResponse.Status.IN_PROGRESS };
        for (RealtimeResponse.Status status : statuses) {
            for (RealtimeResponse.OutputModality modality : new RealtimeResponse.OutputModality[] {
                RealtimeResponse.OutputModality.TEXT,
                RealtimeResponse.OutputModality.AUDIO }) {
                String json = "{\"object\":\"realtime.response\",\"status\":\"" + status.asString()
                    + "\",\"output_modalities\":[\"" + modality.asString() + "\"]}";
                VoiceResponseBase result = deserialize(json, VoiceResponseBase::fromJson);
                assertEquals(VoiceResponseBaseObject.REALTIME_RESPONSE, result.getObject());
                assertEquals(status, result.getStatus());
                assertEquals(modality, result.getOutputModalities().get(0));
                assertEquals(json, serialize(result));
            }
        }

        String emptyAudio = serialize(new AudioTranscription());
        assertFalse(emptyAudio.contains("delay"));
        assertNull(deserialize(emptyAudio, AudioTranscription::fromJson).getDelay());
        String emptySession = serialize(new RealtimeSessionConfiguration());
        assertFalse(emptySession.contains("tracing"));
        assertFalse(emptySession.contains("truncation"));
        assertNull(deserialize(emptySession, RealtimeSessionConfiguration::fromJson)
            .getTracingAsRealtimeSessionTracingConfiguration());
        assertTrue(emptySession.contains("\"type\":\"realtime\""));
    }

    private static <T extends JsonSerializable<?>> void assertStable(T value, Deserializer<? extends T> deserializer)
        throws IOException {
        String json = serialize(value);
        T result = deserialize(json, deserializer);
        assertEquals(json, serialize(result));
    }

    private static <T> T deserialize(String json, Deserializer<T> deserializer) throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return deserializer.deserialize(reader);
        }
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
