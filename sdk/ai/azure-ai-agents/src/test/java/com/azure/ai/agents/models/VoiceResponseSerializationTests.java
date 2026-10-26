// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceResponseSerializationTests {

    @Test
    public void maxOutputTokensVariantsRoundTrip() throws IOException {
        VoiceResponse longResult = roundTrip("{\"max_output_tokens\":4096}");
        assertEquals(4096L, longResult.getMaxOutputTokensAsLong());
        assertNull(longResult.getMaxOutputTokensAsString());

        VoiceResponse stringResult = roundTrip("{\"max_output_tokens\":\"inf\"}");
        assertEquals("inf", stringResult.getMaxOutputTokensAsString());
        assertNull(stringResult.getMaxOutputTokensAsLong());
    }

    @Test
    public void absentMaxOutputTokensReturnsNull() throws IOException {
        VoiceResponse response = UnionTypeSerializationTestUtils
            .deserialize("{\"id\":\"response\",\"conversation_id\":\"conversation\"}", VoiceResponse::fromJson);
        assertNull(response.getMaxOutputTokensAsLong());
        assertNull(response.getMaxOutputTokensAsString());
    }

    @Test
    public void objectTypeRoundTripsUnderObjectWireName() throws IOException {
        VoiceResponse response = UnionTypeSerializationTestUtils.deserialize(
            "{\"id\":\"response\",\"conversation_id\":\"conversation\",\"object\":\"realtime.response\"}",
            VoiceResponse::fromJson);
        assertEquals(VoiceResponseObjectType.REALTIME_RESPONSE, response.getObjectType());
        assertEquals("response", response.getId());
        assertEquals("conversation", response.getConversationId());

        String serialized = UnionTypeSerializationTestUtils.serialize(response);
        assertTrue(serialized.contains("\"object\":\"realtime.response\""));
        assertFalse(serialized.contains("\"objectType\""));
        assertEquals(response.getObjectType(),
            UnionTypeSerializationTestUtils.deserialize(serialized, VoiceResponse::fromJson).getObjectType());
        assertNull(UnionTypeSerializationTestUtils.deserialize("{}", VoiceResponse::fromJson).getObjectType());
        assertNull(
            UnionTypeSerializationTestUtils.deserialize("{\"object\":null}", VoiceResponse::fromJson).getObjectType());
    }

    private VoiceResponse roundTrip(String json) throws IOException {
        VoiceResponse value = UnionTypeSerializationTestUtils.deserialize(json, VoiceResponse::fromJson);
        String serialized = UnionTypeSerializationTestUtils.serialize(value);
        assertTrue(serialized.contains("\"max_output_tokens\""));
        return UnionTypeSerializationTestUtils.deserialize(serialized, VoiceResponse::fromJson);
    }
}
