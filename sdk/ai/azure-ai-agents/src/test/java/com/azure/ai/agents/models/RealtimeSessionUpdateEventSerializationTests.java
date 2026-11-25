// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.openai.models.realtime.RealtimeSessionCreateRequest;
import com.openai.models.realtime.RealtimeTranscriptionSessionCreateRequest;
import com.openai.models.realtime.SessionUpdateEvent.Session;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeSessionUpdateEventSerializationTests {

    @Test
    public void realtimeSessionRoundTrips() throws IOException {
        RealtimeSessionUpdateEvent event = new RealtimeSessionUpdateEvent(Session
            .ofRealtimeSessionCreateRequest(RealtimeSessionCreateRequest.builder().instructions("Be concise").build()));

        String json = UnionTypeSerializationTestUtils.serialize(event);
        RealtimeSessionUpdateEvent result = assertInstanceOf(RealtimeSessionUpdateEvent.class,
            UnionTypeSerializationTestUtils.deserialize(json, RealtimeClientEvent::fromJson));

        assertTrue(result.getSession().isRealtimeSessionCreateRequest());
        assertEquals("Be concise", result.getSession().asRealtimeSessionCreateRequest().instructions().get());
        assertEquals(json, UnionTypeSerializationTestUtils.serialize(result));
    }

    @Test
    public void transcriptionSessionRoundTrips() throws IOException {
        RealtimeSessionUpdateEvent event = new RealtimeSessionUpdateEvent(Session
            .ofRealtimeTranscriptionSessionCreateRequest(RealtimeTranscriptionSessionCreateRequest.builder().build()));

        String json = UnionTypeSerializationTestUtils.serialize(event);
        RealtimeSessionUpdateEvent result = assertInstanceOf(RealtimeSessionUpdateEvent.class,
            UnionTypeSerializationTestUtils.deserialize(json, RealtimeClientEvent::fromJson));

        assertTrue(result.getSession().isRealtimeTranscriptionSessionCreateRequest());
        assertTrue(json.contains("\"type\":\"transcription\""));
        assertEquals(json, UnionTypeSerializationTestUtils.serialize(result));
    }

    @Test
    public void absentSessionReturnsNull() throws IOException {
        RealtimeSessionUpdateEvent event = UnionTypeSerializationTestUtils.deserialize("{\"type\":\"session.update\"}",
            RealtimeSessionUpdateEvent::fromJson);

        assertNull(event.getSession());
        assertFalse(UnionTypeSerializationTestUtils.serialize(event).contains("\"session\""));
        event = UnionTypeSerializationTestUtils.deserialize("{\"type\":\"session.update\",\"session\":null}",
            RealtimeSessionUpdateEvent::fromJson);
        assertNull(event.getSession());
        assertFalse(UnionTypeSerializationTestUtils.serialize(event).contains("\"session\""));
    }

    @Test
    public void nestedConfigurationRetainsExistingNullOmission() throws IOException {
        String session = "{\"type\":\"realtime\",\"model\":\"future-model\",\"audio\":{\"input\":"
            + "{\"format\":{\"type\":\"audio/pcm\",\"rate\":24000},\"noise_reduction\":null,"
            + "\"turn_detection\":{\"type\":\"server_vad\",\"silence_duration_ms\":500}}},"
            + "\"tracing\":{\"workflow_name\":\"workflow\"},\"max_output_tokens\":\"inf\","
            + "\"truncation\":{\"type\":\"retention_ratio\",\"retention_ratio\":0.8},"
            + "\"tools\":[{\"type\":\"function\",\"name\":\"lookup\"}]}";
        RealtimeSessionUpdateEvent event = UnionTypeSerializationTestUtils.deserialize(
            "{\"type\":\"session.update\",\"session\":" + session + "}", RealtimeSessionUpdateEvent::fromJson);
        String json = UnionTypeSerializationTestUtils.serialize(event);
        assertFalse(json.contains("\"noise_reduction\""));
        assertEquals(
            UnionTypeSerializationTestUtils.deserialize(session.replace(",\"noise_reduction\":null", ""),
                reader -> reader.readUntyped()),
            UnionTypeSerializationTestUtils.deserialize(json, reader -> reader.readMap(r -> r.readUntyped()))
                .get("session"));
    }

    @Test
    public void unknownSessionVariantIsPreserved() throws IOException {
        String session = "{\"type\":\"future_session\",\"setting\":true}";
        RealtimeSessionUpdateEvent event = UnionTypeSerializationTestUtils.deserialize(
            "{\"type\":\"session.update\",\"session\":" + session + "}", RealtimeSessionUpdateEvent::fromJson);
        assertTrue(UnionTypeSerializationTestUtils.serialize(event).contains("\"session\":" + session));
    }
}
