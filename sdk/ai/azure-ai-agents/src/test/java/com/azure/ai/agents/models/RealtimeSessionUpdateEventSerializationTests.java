// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeSessionUpdateEventSerializationTests {

    @Test
    public void realtimeSessionRoundTrips() throws IOException {
        RealtimeSessionUpdateEvent event = new RealtimeSessionUpdateEvent(new RealtimeSessionConfiguration());

        String json = UnionTypeSerializationTestUtils.serialize(event);
        RealtimeSessionUpdateEvent result
            = UnionTypeSerializationTestUtils.deserialize(json, RealtimeSessionUpdateEvent::fromJson);

        assertTrue(json.contains("\"session\":{\"type\":\"realtime\""));
        assertInstanceOf(RealtimeSessionConfiguration.class, result.getSession());
    }

    @Test
    public void transcriptionSessionRoundTrips() throws IOException {
        RealtimeSessionUpdateEvent event
            = new RealtimeSessionUpdateEvent(new RealtimeTranscriptionSessionConfiguration());

        String json = UnionTypeSerializationTestUtils.serialize(event);
        RealtimeSessionUpdateEvent result
            = UnionTypeSerializationTestUtils.deserialize(json, RealtimeSessionUpdateEvent::fromJson);

        assertTrue(json.contains("\"session\":{\"type\":\"transcription\""));
        assertInstanceOf(RealtimeTranscriptionSessionConfiguration.class, result.getSession());
    }

    @Test
    public void absentSessionReturnsNull() throws IOException {
        RealtimeSessionUpdateEvent event = UnionTypeSerializationTestUtils.deserialize("{\"type\":\"session.update\"}",
            RealtimeSessionUpdateEvent::fromJson);

        assertNull(event.getSession());
    }
}
