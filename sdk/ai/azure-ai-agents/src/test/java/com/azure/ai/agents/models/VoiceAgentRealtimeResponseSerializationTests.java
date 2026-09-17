// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentRealtimeResponseSerializationTests {

    @Test
    public void shadowedMaxOutputTokensVariantsRoundTrip() throws IOException {
        VoiceAgentRealtimeResponse longResult = roundTrip("{\"max_output_tokens\":4096}");
        assertEquals(4096L, longResult.getMaxOutputTokensAsLong());
        assertNull(longResult.getMaxOutputTokensAsString());

        VoiceAgentRealtimeResponse stringResult = roundTrip("{\"max_output_tokens\":\"inf\"}");
        assertEquals("inf", stringResult.getMaxOutputTokensAsString());
        assertNull(stringResult.getMaxOutputTokensAsLong());
    }

    @Test
    public void absentMaxOutputTokensReturnsNull() throws IOException {
        VoiceAgentRealtimeResponse response
            = UnionTypeSerializationTestUtils.deserialize("{}", VoiceAgentRealtimeResponse::fromJson);
        assertNull(response.getMaxOutputTokensAsLong());
        assertNull(response.getMaxOutputTokensAsString());
    }

    private VoiceAgentRealtimeResponse roundTrip(String json) throws IOException {
        VoiceAgentRealtimeResponse value
            = UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentRealtimeResponse::fromJson);
        String serialized = UnionTypeSerializationTestUtils.serialize(value);
        assertTrue(serialized.contains("\"max_output_tokens\""));
        return UnionTypeSerializationTestUtils.deserialize(serialized, VoiceAgentRealtimeResponse::fromJson);
    }
}
