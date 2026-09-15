// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeSessionCreateRequestGAAudioOutputSerializationTests {

    @Test
    public void builtInVoiceRoundTrips() throws IOException {
        RealtimeSessionCreateRequestGAAudioOutput output
            = new RealtimeSessionCreateRequestGAAudioOutput().setVoice(VoiceIdsShared.ALLOY);

        String json = UnionTypeSerializationTestUtils.serialize(output);
        RealtimeSessionCreateRequestGAAudioOutput result
            = UnionTypeSerializationTestUtils.deserialize(json, RealtimeSessionCreateRequestGAAudioOutput::fromJson);

        assertTrue(json.contains("\"voice\":\"alloy\""));
        assertEquals(VoiceIdsShared.ALLOY, result.getVoiceAsVoiceIdsShared());
        assertNull(result.getVoiceAsRealtimeSessionCreateRequestGAAudioOutputVoice());
    }

    @Test
    public void customVoiceRoundTrips() throws IOException {
        RealtimeSessionCreateRequestGAAudioOutput output = new RealtimeSessionCreateRequestGAAudioOutput()
            .setVoice(new RealtimeSessionCreateRequestGAAudioOutputVoice("voice-id"));

        String json = UnionTypeSerializationTestUtils.serialize(output);
        RealtimeSessionCreateRequestGAAudioOutput result
            = UnionTypeSerializationTestUtils.deserialize(json, RealtimeSessionCreateRequestGAAudioOutput::fromJson);

        assertTrue(json.contains("\"voice\":{\"id\":\"voice-id\"}"));
        assertEquals("voice-id", result.getVoiceAsRealtimeSessionCreateRequestGAAudioOutputVoice().getId());
        assertNull(result.getVoiceAsVoiceIdsShared());
    }

    @Test
    public void absentAndNullVoiceReturnNull() throws IOException {
        RealtimeSessionCreateRequestGAAudioOutput output
            = UnionTypeSerializationTestUtils.deserialize("{}", RealtimeSessionCreateRequestGAAudioOutput::fromJson);
        assertNull(output.getVoiceAsVoiceIdsShared());
        assertNull(output.getVoiceAsRealtimeSessionCreateRequestGAAudioOutputVoice());

        output.setVoice(VoiceIdsShared.ALLOY).setVoice((VoiceIdsShared) null);
        assertNull(output.getVoiceAsVoiceIdsShared());
    }
}
