// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeSessionAudioOutputSerializationTests {

    @Test
    public void builtInVoiceRoundTrips() throws IOException {
        RealtimeSessionAudioOutput output = new RealtimeSessionAudioOutput().setVoice(VoiceIds.ALLOY);
        assertEquals(VoiceIds.ALLOY, output.getVoiceAsVoiceIds());

        String json = UnionTypeSerializationTestUtils.serialize(output);
        RealtimeSessionAudioOutput result
            = UnionTypeSerializationTestUtils.deserialize(json, RealtimeSessionAudioOutput::fromJson);

        assertTrue(json.contains("\"voice\":\"alloy\""));
        assertEquals(VoiceIds.ALLOY, result.getVoiceAsVoiceIds());
        assertNull(result.getVoiceAsRealtimeSessionCreateRequestGAAudioOutputVoice());
    }

    @Test
    public void customVoiceRoundTrips() throws IOException {
        RealtimeSessionAudioOutput output
            = new RealtimeSessionAudioOutput().setVoice(new RealtimeSessionCreateRequestGAAudioOutputVoice("voice-id"));

        String json = UnionTypeSerializationTestUtils.serialize(output);
        RealtimeSessionAudioOutput result
            = UnionTypeSerializationTestUtils.deserialize(json, RealtimeSessionAudioOutput::fromJson);

        assertTrue(json.contains("\"voice\":{\"id\":\"voice-id\"}"));
        assertEquals("voice-id", result.getVoiceAsRealtimeSessionCreateRequestGAAudioOutputVoice().getId());
        assertNull(result.getVoiceAsVoiceIds());
    }

    @Test
    public void absentAndNullVoiceReturnNull() throws IOException {
        RealtimeSessionAudioOutput output
            = UnionTypeSerializationTestUtils.deserialize("{}", RealtimeSessionAudioOutput::fromJson);
        assertNull(output.getVoiceAsVoiceIds());
        assertNull(output.getVoiceAsRealtimeSessionCreateRequestGAAudioOutputVoice());

        output.setVoice(VoiceIds.ALLOY).setVoice((VoiceIds) null);
        assertNull(output.getVoiceAsVoiceIds());
    }
}
