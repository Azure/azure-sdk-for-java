// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeSessionCreatedEventSerializationTests {

    @Test
    public void sessionRoundTrips() throws IOException {
        VoiceAgentSessionResponseConfiguration session = UnionTypeSerializationTestUtils.deserialize(
            "{\"type\":\"realtime\",\"object\":\"realtime.session\",\"id\":\"session\",\"model\":\"model\"}",
            VoiceAgentSessionResponseConfiguration::fromJson);
        RealtimeSessionCreatedEvent event = new RealtimeSessionCreatedEvent("event", session);

        String json = UnionTypeSerializationTestUtils.serialize(event);
        RealtimeSessionCreatedEvent result
            = UnionTypeSerializationTestUtils.deserialize(json, RealtimeSessionCreatedEvent::fromJson);

        assertTrue(json.contains("\"session\":{\"type\":\"realtime\""));
        assertEquals("session", result.getSessionAsVoiceAgentSessionResponseConfiguration().getId());
    }

    @Test
    public void absentSessionReturnsNull() throws IOException {
        RealtimeSessionCreatedEvent event = UnionTypeSerializationTestUtils.deserialize(
            "{\"type\":\"session.created\",\"event_id\":\"event\"}", RealtimeSessionCreatedEvent::fromJson);

        assertNull(event.getSessionAsVoiceAgentSessionResponseConfiguration());
    }
}
