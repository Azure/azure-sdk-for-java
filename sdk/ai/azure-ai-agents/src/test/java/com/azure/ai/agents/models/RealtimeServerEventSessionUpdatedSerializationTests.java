// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeServerEventSessionUpdatedSerializationTests {

    @Test
    public void sessionRoundTrips() throws IOException {
        VoiceAgentSessionResponseConfig session = UnionTypeSerializationTestUtils.deserialize(
            "{\"type\":\"realtime\",\"object\":\"realtime.session\",\"id\":\"session\",\"model\":\"model\"}",
            VoiceAgentSessionResponseConfig::fromJson);
        RealtimeServerEventSessionUpdated event = new RealtimeServerEventSessionUpdated("event", session);

        String json = UnionTypeSerializationTestUtils.serialize(event);
        RealtimeServerEventSessionUpdated result
            = UnionTypeSerializationTestUtils.deserialize(json, RealtimeServerEventSessionUpdated::fromJson);

        assertTrue(json.contains("\"session\":{\"type\":\"realtime\""));
        assertEquals("session", result.getSessionAsVoiceAgentSessionResponseConfig().getId());
    }

    @Test
    public void absentSessionReturnsNull() throws IOException {
        RealtimeServerEventSessionUpdated event = UnionTypeSerializationTestUtils.deserialize(
            "{\"type\":\"session.updated\",\"event_id\":\"event\"}", RealtimeServerEventSessionUpdated::fromJson);

        assertNull(event.getSessionAsVoiceAgentSessionResponseConfig());
    }
}
