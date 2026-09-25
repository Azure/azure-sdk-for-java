// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeTimestampSerializationTests {
    private static final long EPOCH_SECONDS = 1763605109L;

    private static final OffsetDateTime TIMESTAMP
        = OffsetDateTime.ofInstant(Instant.ofEpochSecond(EPOCH_SECONDS), ZoneOffset.UTC);

    @Test
    public void assistantMessageCreatedAtUsesOffsetDateTimeAndUnixSecondsWireFormat() throws IOException {
        String input = "{\"type\":\"message\",\"role\":\"assistant\",\"content\":[]," + "\"created_at\":"
            + EPOCH_SECONDS + ",\"response_id\":\"resp-1\"}";

        RealtimeConversationItemAssistantMessage message
            = deserialize(input, RealtimeConversationItemAssistantMessage::fromJson);
        assertEquals(TIMESTAMP, message.getCreatedAt());

        String serialized = serialize(message);
        assertUnixSeconds(serialized, "created_at");
        RealtimeConversationItemAssistantMessage roundTripped
            = deserialize(serialized, RealtimeConversationItemAssistantMessage::fromJson);
        assertEquals(TIMESTAMP, roundTripped.getCreatedAt());
    }

    @Test
    public void functionCallCreatedAtUsesOffsetDateTimeAndUnixSecondsWireFormat() throws IOException {
        String input = "{\"type\":\"function_call\",\"name\":\"lookup\",\"arguments\":\"{}\"," + "\"created_at\":"
            + EPOCH_SECONDS + ",\"response_id\":\"resp-1\"}";

        RealtimeConversationItemFunctionCall functionCall
            = deserialize(input, RealtimeConversationItemFunctionCall::fromJson);
        assertEquals(TIMESTAMP, functionCall.getCreatedAt());

        String serialized = serialize(functionCall);
        assertUnixSeconds(serialized, "created_at");
        RealtimeConversationItemFunctionCall roundTripped
            = deserialize(serialized, RealtimeConversationItemFunctionCall::fromJson);
        assertEquals(TIMESTAMP, roundTripped.getCreatedAt());
    }

    @Test
    public void dtmfReceivedAtUsesOffsetDateTimeAndUnixSecondsWireFormat() throws IOException {
        String input = "{\"type\":\"input_audio_buffer.dtmf_event_received\",\"event\":\"9\"," + "\"received_at\":"
            + EPOCH_SECONDS + "}";

        RealtimeInputAudioBufferDtmfReceivedEvent event
            = deserialize(input, RealtimeInputAudioBufferDtmfReceivedEvent::fromJson);
        assertEquals(TIMESTAMP, event.getReceivedAt());

        String serialized = serialize(event);
        assertUnixSeconds(serialized, "received_at");
        RealtimeInputAudioBufferDtmfReceivedEvent roundTripped
            = deserialize(serialized, RealtimeInputAudioBufferDtmfReceivedEvent::fromJson);
        assertEquals(TIMESTAMP, roundTripped.getReceivedAt());
    }

    private static void assertUnixSeconds(String json, String fieldName) {
        assertTrue(json.contains("\"" + fieldName + "\":" + EPOCH_SECONDS));
        assertFalse(json.contains("\"" + fieldName + "\":\""));
    }

    private static <T> T deserialize(String json, Deserializer<T> deserializer) throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return deserializer.deserialize(reader);
        }
    }

    private static String serialize(JsonSerializable<?> value) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(output)) {
            value.toJson(writer);
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

    @FunctionalInterface
    private interface Deserializer<T> {
        T deserialize(JsonReader reader) throws IOException;
    }
}
