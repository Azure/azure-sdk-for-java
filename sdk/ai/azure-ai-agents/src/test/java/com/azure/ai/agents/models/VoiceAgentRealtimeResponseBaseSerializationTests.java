// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentRealtimeResponseBaseSerializationTests {

    @Test
    public void maxOutputTokensVariantsRoundTrip() throws IOException {
        VoiceAgentRealtimeResponseBase longResult = roundTrip("{\"max_output_tokens\":4096}");
        assertEquals(4096L, longResult.getMaxOutputTokensAsLong());
        assertNull(longResult.getMaxOutputTokensAsString());

        VoiceAgentRealtimeResponseBase stringResult = roundTrip("{\"max_output_tokens\":\"inf\"}");
        assertEquals("inf", stringResult.getMaxOutputTokensAsString());
        assertNull(stringResult.getMaxOutputTokensAsLong());
    }

    @Test
    public void absentMaxOutputTokensReturnsNull() throws IOException {
        VoiceAgentRealtimeResponseBase response
            = UnionTypeSerializationTestUtils.deserialize("{}", VoiceAgentRealtimeResponseBase::fromJson);
        assertNull(response.getMaxOutputTokensAsLong());
        assertNull(response.getMaxOutputTokensAsString());
    }

    private VoiceAgentRealtimeResponseBase roundTrip(String json) throws IOException {
        VoiceAgentRealtimeResponseBase value
            = UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentRealtimeResponseBase::fromJson);
        String serialized = UnionTypeSerializationTestUtils.serialize(value);
        assertTrue(serialized.contains("\"max_output_tokens\""));
        return UnionTypeSerializationTestUtils.deserialize(serialized, VoiceAgentRealtimeResponseBase::fromJson);
    }
}
