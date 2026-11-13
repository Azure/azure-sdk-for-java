// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.openai.models.realtime.RealtimeAudioFormats;
import com.openai.models.realtime.RealtimeAudioFormats.AudioPcm.Rate;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentDefinitionSerializationTests {

    @Test
    public void fullVoiceDefinitionRoundTrips() throws IOException {
        RealtimeAudioFormats pcm = RealtimeAudioFormats.ofAudioPcm(RealtimeAudioFormats.AudioPcm.builder()
            .type(RealtimeAudioFormats.AudioPcm.Type.AUDIO_PCM)
            .rate(Rate._24000)
            .build());
        VoiceAgentAudioInputConfiguration input = new VoiceAgentAudioInputConfiguration().setFormat(pcm)
            .setTurnDetection(new VoiceAgentServerVadTurnDetection().setThreshold(0.5)
                .setPrefixPadding(Duration.ofMillis(300))
                .setSilenceDuration(Duration.ofMillis(500))
                .setIdleTimeout(Duration.ofMillis(1500)))
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
        assertTrue(json.contains("\"prefix_padding_ms\":300"));
        assertTrue(json.contains("\"silence_duration_ms\":500"));
        assertTrue(json.contains("\"idle_timeout_ms\":1500"));
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
        assertEquals(Rate._24000, deserializedInput.getFormat().asAudioPcm().rate().get());
        VoiceAgentServerVadTurnDetection deserializedVad
            = assertInstanceOf(VoiceAgentServerVadTurnDetection.class, deserializedInput.getTurnDetection());
        VoiceAgentServerVadTurnDetection originalVad = (VoiceAgentServerVadTurnDetection) input.getTurnDetection();
        assertEquals(originalVad.getThreshold(), deserializedVad.getThreshold());
        assertEquals(originalVad.getPrefixPadding(), deserializedVad.getPrefixPadding());
        assertEquals(originalVad.getSilenceDuration(), deserializedVad.getSilenceDuration());
        assertEquals(originalVad.getIdleTimeout(), deserializedVad.getIdleTimeout());
        assertEquals(input.getTranscription().getModel(), deserializedInput.getTranscription().getModel());

        VoiceAgentAudioOutputConfiguration deserializedOutput = voice.getAudio().getOutput();
        assertEquals(Rate._24000, deserializedOutput.getFormat().asAudioPcm().rate().get());
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
