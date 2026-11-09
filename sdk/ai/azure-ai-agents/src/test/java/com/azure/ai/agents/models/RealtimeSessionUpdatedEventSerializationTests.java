// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeSessionUpdatedEventSerializationTests {

    @Test
    public void sessionRoundTrips() throws IOException {
        RealtimeSessionUpdatedEvent event = UnionTypeSerializationTestUtils.deserialize(
            "{\"type\":\"session.updated\",\"event_id\":\"event\","
                + "\"session\":{\"type\":\"realtime\",\"object\":\"realtime.session\",\"id\":\"session\",\"model\":\"model\"}}",
            RealtimeSessionUpdatedEvent::fromJson);

        String json = UnionTypeSerializationTestUtils.serialize(event);
        RealtimeSessionUpdatedEvent result
            = UnionTypeSerializationTestUtils.deserialize(json, RealtimeSessionUpdatedEvent::fromJson);

        assertTrue(json.contains("\"session\":{\"type\":\"realtime\""));
        assertEquals("event", result.getEventId());
        assertEquals("session", result.getSessionResponseConfiguration().getId());
    }

    @Test
    public void absentSessionReturnsNull() throws IOException {
        RealtimeSessionUpdatedEvent event = UnionTypeSerializationTestUtils.deserialize(
            "{\"type\":\"session.updated\",\"event_id\":\"event\"}", RealtimeSessionUpdatedEvent::fromJson);

        assertNull(event.getSessionResponseConfiguration());
    }
}
