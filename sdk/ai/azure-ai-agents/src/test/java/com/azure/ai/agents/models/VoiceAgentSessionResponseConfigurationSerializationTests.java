// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentSessionResponseConfigurationSerializationTests {

    @Test
    public void longAndStringTokenLimitsRoundTrip() throws IOException {
        VoiceAgentSessionResponseConfiguration longResult
            = roundTrip("{\"type\":\"realtime\",\"object\":\"realtime.session\",\"id\":\"s\","
                + "\"model\":\"m\",\"max_output_tokens\":2048}");
        assertEquals(2048L, longResult.getMaxOutputTokensAsLong());
        assertNull(longResult.getMaxOutputTokensAsString());

        VoiceAgentSessionResponseConfiguration stringResult
            = roundTrip("{\"type\":\"realtime\",\"object\":\"realtime.session\",\"id\":\"s\","
                + "\"model\":\"m\",\"max_output_tokens\":\"inf\"}");
        assertEquals("inf", stringResult.getMaxOutputTokensAsString());
        assertNull(stringResult.getMaxOutputTokensAsLong());
    }

    @Test
    public void absentValuesReturnNull() throws IOException {
        VoiceAgentSessionResponseConfiguration result = UnionTypeSerializationTestUtils.deserialize(
            "{\"type\":\"realtime\",\"object\":\"realtime.session\",\"id\":\"s\",\"model\":\"m\"}",
            VoiceAgentSessionResponseConfiguration::fromJson);
        assertNull(result.getMaxOutputTokensAsLong());
        assertNull(result.getMaxOutputTokensAsString());
    }

    private VoiceAgentSessionResponseConfiguration roundTrip(String json) throws IOException {
        VoiceAgentSessionResponseConfiguration value
            = UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentSessionResponseConfiguration::fromJson);
        String serialized = UnionTypeSerializationTestUtils.serialize(value);
        assertTrue(serialized.contains("\"id\":\"s\""));
        return UnionTypeSerializationTestUtils.deserialize(serialized,
            VoiceAgentSessionResponseConfiguration::fromJson);
    }
}
