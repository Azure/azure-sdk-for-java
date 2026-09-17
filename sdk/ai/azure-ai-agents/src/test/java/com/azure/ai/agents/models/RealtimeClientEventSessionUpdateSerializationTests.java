// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeClientEventSessionUpdateSerializationTests {

    @Test
    public void realtimeSessionRoundTrips() throws IOException {
        RealtimeClientEventSessionUpdate event
            = new RealtimeClientEventSessionUpdate(new RealtimeSessionCreateRequestGA());

        String json = UnionTypeSerializationTestUtils.serialize(event);
        RealtimeClientEventSessionUpdate result
            = UnionTypeSerializationTestUtils.deserialize(json, RealtimeClientEventSessionUpdate::fromJson);

        assertTrue(json.contains("\"session\":{\"type\":\"realtime\""));
        assertInstanceOf(RealtimeSessionCreateRequestGA.class, result.getSessionAsRealtimeSessionCreateRequestGA());
        assertNull(result.getSessionAsRealtimeTranscriptionSessionCreateRequestGA());
    }

    @Test
    public void transcriptionSessionRoundTrips() throws IOException {
        RealtimeClientEventSessionUpdate event
            = new RealtimeClientEventSessionUpdate(new RealtimeTranscriptionSessionCreateRequestGA());

        String json = UnionTypeSerializationTestUtils.serialize(event);
        RealtimeClientEventSessionUpdate result
            = UnionTypeSerializationTestUtils.deserialize(json, RealtimeClientEventSessionUpdate::fromJson);

        assertTrue(json.contains("\"session\":{\"type\":\"transcription\""));
        assertInstanceOf(RealtimeTranscriptionSessionCreateRequestGA.class,
            result.getSessionAsRealtimeTranscriptionSessionCreateRequestGA());
        assertNull(result.getSessionAsRealtimeSessionCreateRequestGA());
    }

    @Test
    public void absentSessionReturnsNull() throws IOException {
        RealtimeClientEventSessionUpdate event = UnionTypeSerializationTestUtils
            .deserialize("{\"type\":\"session.update\"}", RealtimeClientEventSessionUpdate::fromJson);

        assertNull(event.getSessionAsRealtimeSessionCreateRequestGA());
        assertNull(event.getSessionAsRealtimeTranscriptionSessionCreateRequestGA());
    }
}
