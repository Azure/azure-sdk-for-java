// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.openai.models.responses.ToolChoiceFunction;
import com.openai.models.responses.ToolChoiceMcp;
import com.openai.models.responses.ToolChoiceOptions;
import com.openai.models.realtime.RealtimeAudioFormats.AudioPcm.Rate;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentDefinitionSerializationTests {

    @Test
    public void maxOutputTokensVariantsRoundTrip() throws IOException {
        VoiceAgentDefinition longResult = roundTrip(new VoiceAgentDefinition().setMaxOutputTokens(512L));
        assertEquals(512L, longResult.getMaxOutputTokensAsLong());
        assertNull(longResult.getMaxOutputTokensAsString());

        VoiceAgentDefinition stringResult = roundTrip(new VoiceAgentDefinition().setMaxOutputTokens("inf"));
        assertEquals("inf", stringResult.getMaxOutputTokensAsString());
        assertNull(stringResult.getMaxOutputTokensAsLong());
    }

    @Test
    public void toolChoiceVariantsRoundTrip() throws IOException {
        VoiceAgentDefinition definition = new VoiceAgentDefinition().setToolChoice(ToolChoiceOptions.AUTO);
        assertEquals(ToolChoiceOptions.AUTO, definition.getToolChoiceAsToolChoiceOptions());

        VoiceAgentDefinition stringResult = roundTrip(definition);
        assertEquals(ToolChoiceOptions.AUTO, stringResult.getToolChoiceAsToolChoiceOptions());
        assertNull(stringResult.getToolChoiceAsToolChoiceFunction());
        assertNull(stringResult.getToolChoiceAsToolChoiceMcp());

        VoiceAgentDefinition functionResult
            = roundTrip(new VoiceAgentDefinition().setToolChoice(ToolChoiceFunction.builder().name("lookup").build()));
        assertEquals("lookup", functionResult.getToolChoiceAsToolChoiceFunction().name());
        assertNull(functionResult.getToolChoiceAsToolChoiceOptions());

        VoiceAgentDefinition mcpResult = roundTrip(new VoiceAgentDefinition()
            .setToolChoice(ToolChoiceMcp.builder().serverLabel("server").name("search").build()));
        assertEquals("server", mcpResult.getToolChoiceAsToolChoiceMcp().serverLabel());
        assertNull(mcpResult.getToolChoiceAsToolChoiceOptions());
    }

    @Test
    public void absentAndNullValuesReturnNull() throws IOException {
        VoiceAgentDefinition result
            = UnionTypeSerializationTestUtils.deserialize("{\"kind\":\"voice\"}", VoiceAgentDefinition::fromJson);
        assertNull(result.getMaxOutputTokensAsLong());
        assertNull(result.getMaxOutputTokensAsString());
        assertNull(result.getToolChoiceAsToolChoiceOptions());
        assertNull(result.getToolChoiceAsToolChoiceFunction());
        assertNull(result.getToolChoiceAsToolChoiceMcp());

        VoiceAgentDefinition cleared = new VoiceAgentDefinition().setMaxOutputTokens("inf")
            .setMaxOutputTokens((String) null)
            .setToolChoice(ToolChoiceOptions.AUTO)
            .setToolChoice((ToolChoiceOptions) null);
        assertNull(cleared.getMaxOutputTokensAsString());
        assertNull(cleared.getToolChoiceAsToolChoiceOptions());
    }

    private VoiceAgentDefinition roundTrip(VoiceAgentDefinition value) throws IOException {
        String json = UnionTypeSerializationTestUtils.serialize(value);
        return UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentDefinition::fromJson);
    }

    @Test
    public void fullVoiceDefinitionRoundTrips() throws IOException {
        RealtimePcmAudioFormat pcm = new RealtimePcmAudioFormat().setRate(Rate._24000);
        VoiceAgentAudioInputConfiguration input = new VoiceAgentAudioInputConfiguration().setFormat(pcm)
            .setTurnDetection(new VoiceAgentServerVadTurnDetection().setThreshold(0.5)
                .setPrefixPaddingMs(300L)
                .setSilenceDurationMs(500L))
            .setTranscription(new VoiceAgentInputTranscription(VoiceAgentInputTranscriptionModel.WHISPER_1));
        VoiceAgentAudioOutputConfiguration output = new VoiceAgentAudioOutputConfiguration().setFormat(pcm)
            .setVoice("en-US-AvaNeural")
            .setVoiceType(VoiceType.AZURE_STANDARD);
        VoiceAgentFunctionTool functionTool
            = new VoiceAgentFunctionTool("get_weather").setDescription("Get weather for a city.")
                .setParameters(BinaryData.fromString("{}"));
        VoiceAgentSystemTool systemTool = new VoiceAgentEndConversationSystemTool();

        VoiceAgentDefinition original = new VoiceAgentDefinition().setModelType(VoiceModelType.MANAGED)
            .setModel("gpt-realtime")
            .setInstructions("Keep replies short and natural.")
            .setAudio(new VoiceAgentAudioConfiguration().setInput(input).setOutput(output))
            .setOutputModalities(Collections.singletonList(VoiceOutputModality.AUDIO))
            .setTools(Arrays.<VoiceAgentTool>asList(functionTool, systemTool))
            .setStore(true);

        String json = serialize(original);
        assertTrue(json.contains("\"kind\":\"voice\""));
        assertTrue(json.contains("\"model_type\":\"managed\""));
        assertTrue(json.contains("\"model\":\"gpt-realtime\""));
        assertTrue(json.contains("\"voice\":\"en-US-AvaNeural\""));
        assertTrue(json.contains("\"voice_type\":\"azure-standard\""));
        assertTrue(json.contains("\"rate\":24000"));
        assertTrue(json.contains("\"type\":\"server_vad\""));
        assertTrue(json.contains("\"model\":\"whisper-1\""));
        assertTrue(json.contains("\"output_modalities\":[\"audio\"]"));
        assertTrue(json.contains("\"store\":true"));
        assertTrue(json.contains("\"name\":\"get_weather\""));
        assertTrue(json.contains("\"name\":\"end_conversation\""));

        AgentDefinition deserialized;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            deserialized = AgentDefinition.fromJson(reader);
        }
        assertInstanceOf(VoiceAgentDefinition.class, deserialized);
        VoiceAgentDefinition voice = (VoiceAgentDefinition) deserialized;
        assertEquals(AgentKind.VOICE, voice.getKind());
        assertEquals(VoiceModelType.MANAGED, voice.getModelType());
        assertEquals("gpt-realtime", voice.getModel());
        assertEquals("Keep replies short and natural.", voice.getInstructions());
        assertEquals(Boolean.TRUE, voice.isStore());
        assertEquals(VoiceOutputModality.AUDIO, voice.getOutputModalities().get(0));

        VoiceAgentAudioInputConfiguration deserializedInput = voice.getAudio().getInput();
        RealtimePcmAudioFormat deserializedInputFormat
            = assertInstanceOf(RealtimePcmAudioFormat.class, deserializedInput.getFormat());
        assertEquals(pcm.getRate(), deserializedInputFormat.getRate());
        VoiceAgentServerVadTurnDetection deserializedVad
            = assertInstanceOf(VoiceAgentServerVadTurnDetection.class, deserializedInput.getTurnDetection());
        VoiceAgentServerVadTurnDetection originalVad = (VoiceAgentServerVadTurnDetection) input.getTurnDetection();
        assertEquals(originalVad.getThreshold(), deserializedVad.getThreshold());
        assertEquals(originalVad.getPrefixPaddingMs(), deserializedVad.getPrefixPaddingMs());
        assertEquals(originalVad.getSilenceDurationMs(), deserializedVad.getSilenceDurationMs());
        assertEquals(input.getTranscription().getModel(), deserializedInput.getTranscription().getModel());

        VoiceAgentAudioOutputConfiguration deserializedOutput = voice.getAudio().getOutput();
        RealtimePcmAudioFormat deserializedOutputFormat
            = assertInstanceOf(RealtimePcmAudioFormat.class, deserializedOutput.getFormat());
        assertEquals(pcm.getRate(), deserializedOutputFormat.getRate());
        assertEquals(output.getVoice(), deserializedOutput.getVoice());
        assertEquals(output.getVoiceType(), deserializedOutput.getVoiceType());

        assertEquals(2, voice.getTools().size());
        VoiceAgentFunctionTool deserializedFunction
            = assertInstanceOf(VoiceAgentFunctionTool.class, voice.getTools().get(0));
        assertEquals(functionTool.getName(), deserializedFunction.getName());
        assertEquals(functionTool.getDescription(), deserializedFunction.getDescription());
        VoiceAgentEndConversationSystemTool deserializedSystem
            = assertInstanceOf(VoiceAgentEndConversationSystemTool.class, voice.getTools().get(1));
        assertEquals(systemTool.getName(), deserializedSystem.getName());
    }

    @Test
    public void selfDeployedVoiceDefinitionRoundTrips() throws IOException {
        VoiceAgentDefinition original = new VoiceAgentDefinition().setModelType(VoiceModelType.SELF_DEPLOYED)
            .setModel("customer-realtime-deployment")
            .setInstructions("Use the customer deployment.");

        String json = serialize(original);
        VoiceAgentDefinition deserialized;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            deserialized = VoiceAgentDefinition.fromJson(reader);
        }

        assertEquals(VoiceModelType.SELF_DEPLOYED, deserialized.getModelType());
        assertEquals("customer-realtime-deployment", deserialized.getModel());
        assertEquals("Use the customer deployment.", deserialized.getInstructions());
    }

    private static String serialize(VoiceAgentDefinition definition) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(output)) {
            definition.toJson(writer);
        }
        return output.toString("UTF-8");
    }
}
