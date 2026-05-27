// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the new avatar lifecycle events and the output-audio-buffer clear/cleared events
 * introduced for video avatar support.
 */
class AvatarAndAudioBufferEventsTest {

    @Test
    void testAvatarSwitchEventTypesRegistered() {
        assertEquals("session.avatar.switch_to_speaking", ServerEventType.SESSION_AVATAR_SWITCH_TO_SPEAKING.toString());
        assertEquals("session.avatar.switch_to_idle", ServerEventType.SESSION_AVATAR_SWITCH_TO_IDLE.toString());
        assertEquals("response.video.delta", ServerEventType.RESPONSE_VIDEO_DELTA.toString());
        assertEquals("output_audio_buffer.cleared", ServerEventType.OUTPUT_AUDIO_BUFFER_CLEARED.toString());
    }

    @Test
    void testClientEventOutputAudioBufferClearTypeRegistered() {
        assertEquals("output_audio_buffer.clear", ClientEventType.OUTPUT_AUDIO_BUFFER_CLEAR.toString());
    }

    @Test
    void testAvatarSwitchToSpeakingDeserialization() {
        String json = "{\"type\":\"session.avatar.switch_to_speaking\",\"event_id\":\"e1\",\"turn_id\":\"t1\"}";

        ServerEventSessionAvatarSwitchToSpeaking event
            = BinaryData.fromString(json).toObject(ServerEventSessionAvatarSwitchToSpeaking.class);

        assertNotNull(event);
        assertEquals(ServerEventType.SESSION_AVATAR_SWITCH_TO_SPEAKING, event.getType());
        assertEquals("e1", event.getEventId());
        assertEquals("t1", event.getTurnId());
    }

    @Test
    void testAvatarSwitchToIdleDeserialization() {
        String json = "{\"type\":\"session.avatar.switch_to_idle\",\"event_id\":\"e2\",\"turn_id\":\"t2\"}";

        ServerEventSessionAvatarSwitchToIdle event
            = BinaryData.fromString(json).toObject(ServerEventSessionAvatarSwitchToIdle.class);

        assertNotNull(event);
        assertEquals(ServerEventType.SESSION_AVATAR_SWITCH_TO_IDLE, event.getType());
        assertEquals("e2", event.getEventId());
        assertEquals("t2", event.getTurnId());
    }

    @Test
    void testAvatarSwitchToSpeakingPolymorphicViaSessionUpdate() {
        String json = "{\"type\":\"session.avatar.switch_to_speaking\",\"event_id\":\"e3\",\"turn_id\":\"t3\"}";

        SessionUpdate update = BinaryData.fromString(json).toObject(SessionUpdate.class);

        assertTrue(update instanceof ServerEventSessionAvatarSwitchToSpeaking,
            "Expected ServerEventSessionAvatarSwitchToSpeaking, got " + update.getClass());
        assertEquals("t3", ((ServerEventSessionAvatarSwitchToSpeaking) update).getTurnId());
    }

    @Test
    void testResponseVideoDeltaDeserialization() {
        String json = "{\"type\":\"response.video.delta\",\"event_id\":\"e4\","
            + "\"output_index\":3,\"codec\":\"h264\",\"delta\":\"AAAAAQ==\"}";

        ServerEventResponseVideoDelta event = BinaryData.fromString(json).toObject(ServerEventResponseVideoDelta.class);

        assertEquals(ServerEventType.RESPONSE_VIDEO_DELTA, event.getType());
        assertEquals("e4", event.getEventId());
        assertEquals(3, event.getOutputIndex());
        assertEquals("h264", event.getCodec());
        assertEquals("AAAAAQ==", event.getDelta());
    }

    @Test
    void testResponseVideoDeltaRoundTrip() {
        String json = "{\"type\":\"response.video.delta\",\"event_id\":\"e5\","
            + "\"output_index\":0,\"codec\":\"vp8\",\"delta\":\"ZGVsdGE=\"}";
        ServerEventResponseVideoDelta event = BinaryData.fromString(json).toObject(ServerEventResponseVideoDelta.class);

        ServerEventResponseVideoDelta deserialized
            = BinaryData.fromObject(event).toObject(ServerEventResponseVideoDelta.class);

        assertEquals(event.getType(), deserialized.getType());
        assertEquals(event.getEventId(), deserialized.getEventId());
        assertEquals(event.getOutputIndex(), deserialized.getOutputIndex());
        assertEquals(event.getCodec(), deserialized.getCodec());
        assertEquals(event.getDelta(), deserialized.getDelta());
    }

    @Test
    void testServerEventOutputAudioBufferClearedDeserialization() {
        String json = "{\"type\":\"output_audio_buffer.cleared\",\"event_id\":\"e6\"}";

        ServerEventOutputAudioBufferCleared event
            = BinaryData.fromString(json).toObject(ServerEventOutputAudioBufferCleared.class);

        assertEquals(ServerEventType.OUTPUT_AUDIO_BUFFER_CLEARED, event.getType());
        assertEquals("e6", event.getEventId());
    }

    @Test
    void testServerEventOutputAudioBufferClearedPolymorphicViaSessionUpdate() {
        String json = "{\"type\":\"output_audio_buffer.cleared\",\"event_id\":\"e7\"}";

        SessionUpdate update = BinaryData.fromString(json).toObject(SessionUpdate.class);

        assertTrue(update instanceof ServerEventOutputAudioBufferCleared,
            "Expected ServerEventOutputAudioBufferCleared, got " + update.getClass());
        assertEquals("e7", update.getEventId());
    }

    @Test
    void testClientEventOutputAudioBufferClearConstruction() {
        ClientEventOutputAudioBufferClear event = new ClientEventOutputAudioBufferClear();

        assertEquals(ClientEventType.OUTPUT_AUDIO_BUFFER_CLEAR, event.getType());

        ClientEventOutputAudioBufferClear chained = event.setEventId("evt-1");
        assertSame(event, chained, "setEventId should return this for fluent chaining");
        assertEquals("evt-1", event.getEventId());
    }

    @Test
    void testClientEventOutputAudioBufferClearSerialization() {
        ClientEventOutputAudioBufferClear event = new ClientEventOutputAudioBufferClear().setEventId("clear-1");

        String json = BinaryData.fromObject(event).toString();

        assertTrue(json.contains("\"type\":\"output_audio_buffer.clear\""), json);
        assertTrue(json.contains("\"event_id\":\"clear-1\""), json);
    }

    @Test
    void testClientEventOutputAudioBufferClearRoundTrip() {
        ClientEventOutputAudioBufferClear original = new ClientEventOutputAudioBufferClear().setEventId("clear-2");

        ClientEventOutputAudioBufferClear deserialized
            = BinaryData.fromObject(original).toObject(ClientEventOutputAudioBufferClear.class);

        assertEquals(original.getType(), deserialized.getType());
        assertEquals(original.getEventId(), deserialized.getEventId());
    }

    @Test
    void testClientEventOutputAudioBufferClearPolymorphicViaClientEvent() {
        ClientEventOutputAudioBufferClear original = new ClientEventOutputAudioBufferClear().setEventId("clear-3");

        ClientEvent deserialized = BinaryData.fromObject(original).toObject(ClientEvent.class);

        assertTrue(deserialized instanceof ClientEventOutputAudioBufferClear,
            "Expected ClientEventOutputAudioBufferClear, got " + deserialized.getClass());
        assertEquals(ClientEventType.OUTPUT_AUDIO_BUFFER_CLEAR, deserialized.getType());
        assertEquals("clear-3", deserialized.getEventId());
    }
}
