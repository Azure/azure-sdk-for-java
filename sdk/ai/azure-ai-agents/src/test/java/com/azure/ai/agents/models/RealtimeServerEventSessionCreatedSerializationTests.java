// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeServerEventSessionCreatedSerializationTests {

    @Test
    public void sessionRoundTrips() throws IOException {
        VoiceAgentSessionResponseConfig session = UnionTypeSerializationTestUtils.deserialize(
            "{\"type\":\"realtime\",\"object\":\"realtime.session\",\"id\":\"session\",\"model\":\"model\"}",
            VoiceAgentSessionResponseConfig::fromJson);
        RealtimeServerEventSessionCreated event = new RealtimeServerEventSessionCreated("event", session);

        String json = UnionTypeSerializationTestUtils.serialize(event);
        RealtimeServerEventSessionCreated result
            = UnionTypeSerializationTestUtils.deserialize(json, RealtimeServerEventSessionCreated::fromJson);

        assertTrue(json.contains("\"session\":{\"type\":\"realtime\""));
        assertEquals("session", result.getSessionAsVoiceAgentSessionResponseConfig().getId());
    }

    @Test
    public void absentSessionReturnsNull() throws IOException {
        RealtimeServerEventSessionCreated event = UnionTypeSerializationTestUtils.deserialize(
            "{\"type\":\"session.created\",\"event_id\":\"event\"}", RealtimeServerEventSessionCreated::fromJson);

        assertNull(event.getSessionAsVoiceAgentSessionResponseConfig());
    }
}
