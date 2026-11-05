// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.core.util.BinaryData;
import com.openai.models.live.AudioFormat;
import com.openai.models.live.CustomVoice;
import com.openai.models.live.SessionConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentLiveOpenAITypesSerializationTests {
    @Test
    public void audioRoundTripsThroughAgentDefinition() throws IOException {
        CustomVoice voice = CustomVoice.builder().id("voice_123").build();
        SessionConfig.Audio audio = SessionConfig.Audio.builder()
            .format(AudioFormat.AudioPcm.builder().rate(AudioFormat.AudioPcm.Rate._24000).build())
            .output(SessionConfig.Audio.Output.builder().voice(voice).build())
            .build();
        VoiceAgentLiveDefinition original = new VoiceAgentLiveDefinition("live-deployment").setAudio(audio);

        String json = UnionTypeSerializationTestUtils.serialize(original);
        assertTrue(json.contains("\"audio\":{"));
        assertTrue(json.contains("\"type\":\"audio/pcm\""));
        assertTrue(json.contains("\"rate\":24000"));
        assertTrue(json.contains("\"voice\":{\"id\":\"voice_123\"}"));
        VoiceAgentLiveDefinition result = assertInstanceOf(VoiceAgentLiveDefinition.class,
            UnionTypeSerializationTestUtils.deserialize(json, AgentDefinition::fromJson));
        assertEquals(audio, result.getAudio());
        assertEquals(json, UnionTypeSerializationTestUtils.serialize(result));
    }

    @Test
    public void usageRoundTripsAsAnOpenAIModel() throws IOException {
        String json = "{\"id\":\"conv_live\",\"object\":\"voice.conversation\",\"status\":\"completed\","
            + "\"created_at\":1700000000,\"completed_at\":1700000013,"
            + "\"metadata\":{\"source\":\"test\"},\"usage\":{\"seconds\":12.5}}";
        VoiceAgentLiveConversation value
            = UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentLiveConversation::fromJson);
        assertEquals(12.5, value.getUsage().seconds());

        String serialized = UnionTypeSerializationTestUtils.serialize(value);
        assertTrue(serialized.contains("\"usage\":{"));
        assertEquals(BinaryData.fromString(json).toObject(Object.class),
            BinaryData.fromString(serialized).toObject(Object.class));
        VoiceAgentLiveConversation result
            = UnionTypeSerializationTestUtils.deserialize(serialized, VoiceAgentLiveConversation::fromJson);
        assertEquals(value.getUsage(), result.getUsage());
    }

    @Test
    public void absentAndNullOpenAIPropertiesRemainNull() throws IOException {
        for (String audio : new String[] { "", ",\"audio\":null" }) {
            VoiceAgentLiveDefinition value = UnionTypeSerializationTestUtils.deserialize(
                "{\"kind\":\"voice\",\"sub_protocol\":\"live\",\"model\":\"live-deployment\"" + audio + "}",
                VoiceAgentLiveDefinition::fromJson);
            assertNull(value.getAudio());
            assertFalse(UnionTypeSerializationTestUtils.serialize(value).contains("\"audio\""));
        }
        for (String usage : new String[] { "", ",\"usage\":null" }) {
            VoiceAgentLiveConversation value = UnionTypeSerializationTestUtils.deserialize(
                "{\"id\":\"conv_live\",\"status\":\"in_progress\",\"created_at\":1700000000" + usage + "}",
                VoiceAgentLiveConversation::fromJson);
            assertNull(value.getUsage());
            assertFalse(UnionTypeSerializationTestUtils.serialize(value).contains("\"usage\""));
        }
    }
}
