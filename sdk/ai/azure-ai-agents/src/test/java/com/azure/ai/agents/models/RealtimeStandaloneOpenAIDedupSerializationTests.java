// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.openai.models.realtime.RealtimeAudioFormats;
import com.openai.models.realtime.AudioTranscription.Delay;
import com.openai.models.realtime.ConversationItemInputAudioTranscriptionCompletedEvent.Usage;
import com.openai.models.realtime.RateLimitsUpdatedEvent.RateLimit;
import com.openai.models.realtime.RealtimeAudioInputTurnDetection.SemanticVad.Eagerness;
import com.openai.models.realtime.RealtimeResponse;
import com.openai.models.realtime.RealtimeResponseCreateParams.Conversation;
import com.openai.models.realtime.ResponseContentPartAddedEvent.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeStandaloneOpenAIDedupSerializationTests {

    @Test
    public void testAudioFormatsAcrossFoundryContainers() throws IOException {
        for (String formatJson : Arrays.asList("{\"type\":\"audio/pcm\",\"rate\":24000}", "{\"type\":\"audio/pcma\"}",
            "{\"type\":\"audio/pcmu\"}", "{\"type\":\"future_audio\",\"setting\":true}")) {
            String json = "{\"format\":" + formatJson + "}";
            VoiceAgentAudioInputConfiguration input = deserialize(json, VoiceAgentAudioInputConfiguration::fromJson);
            VoiceAgentAudioOutputConfiguration output
                = deserialize("{\"voice\":\"en-US-AvaNeural\",\"format\":" + formatJson + "}",
                    VoiceAgentAudioOutputConfiguration::fromJson);
            VoiceResponseAudioOutput response = deserialize(json, VoiceResponseAudioOutput::fromJson);
            RealtimeResponseContentPart part = deserialize(json, RealtimeResponseContentPart::fromJson);

            assertJsonFieldEquals(formatJson, input, "format");
            assertJsonFieldEquals(formatJson, output, "format");
            assertJsonFieldEquals(formatJson, response, "format");
            assertJsonFieldEquals(formatJson, part, "format");
            assertEquals("en-US-AvaNeural", output.getVoice());
            assertStable(input, VoiceAgentAudioInputConfiguration::fromJson);
            assertStable(output, VoiceAgentAudioOutputConfiguration::fromJson);
            assertStable(response, VoiceResponseAudioOutput::fromJson);
            assertStable(part, RealtimeResponseContentPart::fromJson);
            assertEquals(input.getFormat(), response.getFormat());
            assertEquals(input.getFormat(), part.getFormat());
            assertEquals(input.getFormat(), output.getFormat());
        }

        RealtimeAudioFormats pcm = RealtimeAudioFormats.ofAudioPcm(RealtimeAudioFormats.AudioPcm.builder()
            .type(RealtimeAudioFormats.AudioPcm.Type.AUDIO_PCM)
            .rate(RealtimeAudioFormats.AudioPcm.Rate._24000)
            .build());
        assertTrue(serialize(new VoiceAgentAudioInputConfiguration().setFormat(pcm)).contains("\"rate\":24000"));
        assertTrue(serialize(new VoiceAgentAudioOutputConfiguration().setFormat(pcm)).contains("\"rate\":24000"));
        VoiceAgentAudioInputConfiguration roundTripped
            = deserialize(serialize(new VoiceAgentAudioInputConfiguration().setFormat(pcm)),
                VoiceAgentAudioInputConfiguration::fromJson);
        assertTrue(roundTripped.getFormat().isAudioPcm());
    }

    @Test
    public void testNullAndAbsentAudioFormatsAreOmitted() throws IOException {
        for (String json : Arrays.asList("{}", "{\"format\":null}")) {
            VoiceAgentAudioInputConfiguration input = deserialize(json, VoiceAgentAudioInputConfiguration::fromJson);
            VoiceAgentAudioOutputConfiguration output = deserialize(json, VoiceAgentAudioOutputConfiguration::fromJson);
            VoiceResponseAudioOutput response = deserialize(json, VoiceResponseAudioOutput::fromJson);
            RealtimeResponseContentPart part = deserialize(json, RealtimeResponseContentPart::fromJson);
            assertNull(input.getFormat());
            assertNull(output.getFormat());
            assertNull(response.getFormat());
            assertNull(part.getFormat());
            assertFalse(serialize(input).contains("\"format\""));
            assertFalse(serialize(output).contains("\"format\""));
            assertFalse(serialize(response).contains("\"format\""));
            assertFalse(serialize(part).contains("\"format\""));
        }
    }

    @Test
    public void testMcpErrorVariantsRoundTripThroughConversationHierarchy() throws IOException {
        for (String errorJson : Arrays.asList(
            "{\"type\":\"protocol_error\",\"code\":-32600,\"message\":\"bad request\"}",
            "{\"type\":\"http_error\",\"code\":503,\"message\":\"unavailable\"}",
            "{\"type\":\"tool_execution_error\",\"message\":\"failed\"}",
            "{\"type\":\"future_error\",\"detail\":\"preserved\"}")) {
            String json = "{\"type\":\"mcp_call\",\"id\":\"item-1\",\"server_label\":\"server\",\"name\":\"lookup\","
                + "\"arguments\":\"{}\",\"created_at\":123,\"response_id\":\"resp-1\",\"error\":" + errorJson + "}";
            RealtimeMcpToolCall call
                = assertInstanceOf(RealtimeMcpToolCall.class, deserialize(json, RealtimeConversationItem::fromJson));
            if (errorJson.contains("\"protocol_error\"")) {
                assertTrue(call.getError().isProtocol());
            } else if (errorJson.contains("\"http_error\"")) {
                assertTrue(call.getError().isHttp());
            } else if (errorJson.contains("\"tool_execution_error\"")) {
                assertTrue(call.getError().isToolExecution());
            }
            assertEquals(OffsetDateTime.parse("1970-01-01T00:02:03Z"), call.getCreatedAt());
            assertEquals("resp-1", call.getResponseId());
            String serialized = serialize(call);
            assertJsonFieldEquals(errorJson, call, "error");
            assertFalse(serialized.contains("\"created_at\""));
            assertFalse(serialized.contains("\"response_id\""));
            RealtimeMcpToolCall result = assertInstanceOf(RealtimeMcpToolCall.class,
                deserialize(serialized, RealtimeConversationItem::fromJson));
            assertEquals(call.getError(), result.getError());
            assertEquals(serialized,
                serialize(new RealtimeMcpToolCall("item-1", "server", "lookup", "{}").setError(call.getError())));
        }
    }

    @Test
    public void testNullAndAbsentMcpErrorsAreOmitted() throws IOException {
        for (String json : Arrays.asList("{}", "{\"error\":null}")) {
            RealtimeMcpToolCall call = deserialize(json, RealtimeMcpToolCall::fromJson);
            assertNull(call.getError());
            assertFalse(serialize(call).contains("\"error\""));
        }
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
    public void testBlendshapeDoneOutputIndexUsesLong() throws IOException {
        long outputIndex = 3_000_000_000L;
        String json = "{\"type\":\"response.animation_blendshapes.done\",\"event_id\":\"evt-5\","
            + "\"response_id\":\"resp-1\",\"item_id\":\"item-1\",\"output_index\":" + outputIndex + "}";

        VoiceAgentResponseAnimationBlendshapeDoneEvent event
            = deserialize(json, VoiceAgentResponseAnimationBlendshapeDoneEvent::fromJson);
        assertEquals(outputIndex, event.getOutputIndex());
        assertTrue(serialize(event).contains("\"output_index\":" + outputIndex));
        assertEquals(outputIndex,
            deserialize(serialize(event), VoiceAgentResponseAnimationBlendshapeDoneEvent::fromJson).getOutputIndex());
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
            VoiceAgentSemanticVadTurnDetection voice = new VoiceAgentSemanticVadTurnDetection().setEagerness(eagerness);
            assertEquals(eagerness,
                deserialize(serialize(voice), VoiceAgentSemanticVadTurnDetection::fromJson).getEagerness());
        }
    }

    @Test
    public void testTranscriptionTokenDetailsRoundTripPolymorphically() throws IOException {
        String json = "{\"type\":\"tokens\",\"input_tokens\":10,\"output_tokens\":5,\"total_tokens\":15,"
            + "\"input_token_details\":{\"audio_tokens\":7,\"text_tokens\":3}}";
        RealtimeConversationItemInputAudioTranscriptionCompletedEvent event = deserialize("{\"usage\":" + json + "}",
            RealtimeConversationItemInputAudioTranscriptionCompletedEvent::fromJson);
        Usage.TranscriptTextUsageTokens usage = event.getUsage().asTranscriptTextUsageTokens();
        Usage.TranscriptTextUsageTokens.InputTokenDetails details = usage.inputTokenDetails().get();
        assertEquals(7L, details.audioTokens().get().longValue());
        assertEquals(3L, details.textTokens().get().longValue());
        assertStable(event, RealtimeConversationItemInputAudioTranscriptionCompletedEvent::fromJson);
    }

    @Test
    public void testTranscriptionLanguagesRoundTripWithExistingPayloads() throws IOException {
        String languages = "[{\"code\":\"en\",\"confidence\":0.9},null,{\"code\":\"fr\"}]";
        String usage = "{\"type\":\"duration\",\"seconds\":1.5}";
        String logprobs = "[{\"token\":\"hello\",\"logprob\":-0.25,\"bytes\":[104]}]";
        RealtimeConversationItemInputAudioTranscriptionCompletedEvent event
            = assertInstanceOf(RealtimeConversationItemInputAudioTranscriptionCompletedEvent.class,
                deserialize("{\"type\":\"conversation.item.input_audio_transcription.completed\","
                    + "\"event_id\":\"event-1\",\"item_id\":\"item-1\",\"content_index\":0,\"transcript\":\"hello\","
                    + "\"languages\":" + languages + ",\"usage\":" + usage + ",\"logprobs\":" + logprobs
                    + ",\"phrases\":[]}", RealtimeServerEvent::fromJson));

        assertEquals("en", event.getLanguages().get(0).code());
        assertNull(event.getLanguages().get(1));
        assertEquals("fr", event.getLanguages().get(2).code());
        assertJsonFieldEquals(languages, event, "languages");
        assertJsonFieldEquals(usage, event, "usage");
        assertJsonFieldEquals(logprobs, event, "logprobs");
        assertJsonFieldEquals("[]", event, "phrases");
        assertStable(event, RealtimeServerEvent::fromJson);
    }

    @Test
    public void testMcpListedToolsPreserveSchemaAndAnnotations() throws IOException {
        String toolJson = "{\"name\":\"search\",\"description\":\"Search records\","
            + "\"input_schema\":{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"}},"
            + "\"required\":[\"query\"],\"additionalProperties\":false},"
            + "\"annotations\":{\"readOnlyHint\":true,\"provider\":{\"limits\":[1,2]}},\"future_setting\":true}";
        RealtimeMcpListTools item = assertInstanceOf(RealtimeMcpListTools.class,
            deserialize("{\"type\":\"mcp_list_tools\",\"server_label\":\"server\",\"id\":\"item-1\"," + "\"tools\":["
                + toolJson + ",null],\"created_at\":123,\"response_id\":\"response-1\"}",
                RealtimeConversationItem::fromJson));

        com.openai.models.realtime.RealtimeMcpListTools.Tool tool = item.getTools().get(0);
        assertEquals("search", tool.name());
        assertEquals("Search records", tool.description().get());
        assertNull(item.getTools().get(1));
        assertJsonFieldEquals("[" + toolJson + ",null]", item, "tools");
        assertEquals(OffsetDateTime.parse("1970-01-01T00:02:03Z"), item.getCreatedAt());
        assertEquals("response-1", item.getResponseId());
        assertFalse(serialize(item).contains("\"created_at\""));
        assertFalse(serialize(item).contains("\"response_id\""));
        assertStable(item, RealtimeConversationItem::fromJson);

        RealtimeMcpListTools constructed
            = new RealtimeMcpListTools("server", Arrays.asList(tool.toBuilder().build(), null));
        assertJsonFieldEquals("[" + toolJson + ",null]", constructed, "tools");
        assertStable(constructed, RealtimeConversationItem::fromJson);
    }

    @Test
    public void testNullAbsentAndEmptyLanguageAndToolLists() throws IOException {
        for (String json : Arrays.asList("{}", "{\"languages\":null,\"tools\":null}",
            "{\"languages\":[],\"tools\":[]}")) {
            RealtimeConversationItemInputAudioTranscriptionCompletedEvent event
                = deserialize(json, RealtimeConversationItemInputAudioTranscriptionCompletedEvent::fromJson);
            RealtimeMcpListTools item = deserialize(json, RealtimeMcpListTools::fromJson);
            if (json.contains("[]")) {
                assertTrue(event.getLanguages().isEmpty());
                assertTrue(item.getTools().isEmpty());
                assertJsonFieldEquals("[]", event, "languages");
                assertJsonFieldEquals("[]", item, "tools");
            } else {
                assertNull(event.getLanguages());
                assertNull(item.getTools());
                assertFalse(serialize(event).contains("\"languages\""));
                assertFalse(serialize(item).contains("\"tools\""));
            }
        }
    }

    @Test
    public void testMcpListedToolReplacementPreservesAgentToolHierarchy() throws IOException {
        FunctionTool original = new FunctionTool("lookup", Collections.emptyMap(), false);
        String json = serialize(original);
        FunctionTool result = assertInstanceOf(FunctionTool.class, deserialize(json, Tool::fromJson));
        assertEquals("lookup", result.getName());
        assertEquals(ToolType.FUNCTION, result.getType());
        assertFalse(json.contains("\"input_schema\""));
        assertFalse(json.contains("\"annotations\""));
        assertEquals(json, serialize(result));
    }

    @Test
    public void testRealtimeResponseEnums() throws IOException {
        RealtimeResponse.Status[] statuses = {
            RealtimeResponse.Status.COMPLETED,
            RealtimeResponse.Status.CANCELLED,
            RealtimeResponse.Status.FAILED,
            RealtimeResponse.Status.INCOMPLETE,
            RealtimeResponse.Status.IN_PROGRESS,
            RealtimeResponse.Status.of("future-response-status") };
        for (RealtimeResponse.Status status : statuses) {
            for (RealtimeResponse.OutputModality modality : new RealtimeResponse.OutputModality[] {
                RealtimeResponse.OutputModality.TEXT,
                RealtimeResponse.OutputModality.AUDIO }) {
                String json = "{\"object\":\"realtime.response\",\"status\":\"" + status.asString()
                    + "\",\"output_modalities\":[\"" + modality.asString() + "\"]}";
                VoiceResponse result = deserialize(json, VoiceResponse::fromJson);
                assertEquals(VoiceResponseObjectType.REALTIME_RESPONSE, result.getObjectType());
                assertEquals(status, result.getStatus());
                assertEquals(modality, result.getOutputModalities().get(0));
                assertEquals(json, serialize(result));
            }
        }

    }

    private static void assertJsonFieldEquals(String expected, JsonSerializable<?> value, String field)
        throws IOException {
        assertEquals(deserialize(expected, JsonReader::readUntyped),
            deserialize(serialize(value), reader -> reader.readMap(JsonReader::readUntyped)).get(field));
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
