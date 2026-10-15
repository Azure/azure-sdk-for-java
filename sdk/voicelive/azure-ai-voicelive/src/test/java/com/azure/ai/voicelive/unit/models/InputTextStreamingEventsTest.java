// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.unit.models;

import com.azure.ai.voicelive.models.ClientEventInputTextDelta;
import com.azure.ai.voicelive.models.ClientEventInputTextDone;
import com.azure.ai.voicelive.models.ClientEventType;
import com.azure.ai.voicelive.models.SessionClientEvent;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the streaming input text client events ({@link ClientEventInputTextDelta}
 * and {@link ClientEventInputTextDone}).
 */
class InputTextStreamingEventsTest {

    @Test
    void testInputTextEventTypesRegistered() {
        assertEquals("input_text.delta", ClientEventType.INPUT_TEXT_DELTA.toString());
        assertEquals("input_text.done", ClientEventType.INPUT_TEXT_DONE.toString());
    }

    @Test
    void testClientEventInputTextDeltaRoundTrip() {
        ClientEventInputTextDelta event = new ClientEventInputTextDelta("item-1", "Hello, ").setContentIndex(0);
        event.setEventId("d1");

        ClientEventInputTextDelta deserialized = BinaryData.fromObject(event).toObject(ClientEventInputTextDelta.class);

        assertEquals(ClientEventType.INPUT_TEXT_DELTA, deserialized.getType());
        assertEquals("d1", deserialized.getEventId());
        assertEquals("item-1", deserialized.getId());
        assertEquals("Hello, ", deserialized.getDelta());
        assertEquals(0, deserialized.getContentIndex());
    }

    @Test
    void testClientEventInputTextDeltaPolymorphic() {
        String json = "{\"type\":\"input_text.delta\",\"event_id\":\"d2\","
            + "\"id\":\"item-2\",\"delta\":\"world\",\"content_index\":1}";

        SessionClientEvent event = BinaryData.fromString(json).toObject(SessionClientEvent.class);

        assertTrue(event instanceof ClientEventInputTextDelta);
        ClientEventInputTextDelta delta = (ClientEventInputTextDelta) event;
        assertEquals("item-2", delta.getId());
        assertEquals("world", delta.getDelta());
        assertEquals(1, delta.getContentIndex());
    }

    @Test
    void testClientEventInputTextDoneRoundTrip() {
        ClientEventInputTextDone event = new ClientEventInputTextDone("item-3").setContentIndex(2);
        event.setEventId("e1");

        SessionClientEvent deserialized = BinaryData.fromObject(event).toObject(SessionClientEvent.class);

        assertTrue(deserialized instanceof ClientEventInputTextDone);
        ClientEventInputTextDone done = (ClientEventInputTextDone) deserialized;
        assertEquals(ClientEventType.INPUT_TEXT_DONE, done.getType());
        assertEquals("e1", done.getEventId());
        assertEquals("item-3", done.getId());
        assertEquals(2, done.getContentIndex());
    }
}
