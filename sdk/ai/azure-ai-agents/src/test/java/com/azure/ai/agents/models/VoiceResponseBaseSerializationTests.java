// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceResponseBaseSerializationTests {

    @Test
    public void maxOutputTokensVariantsRoundTrip() throws IOException {
        VoiceResponseBase longResult = roundTrip("{\"max_output_tokens\":4096}");
        assertEquals(4096L, longResult.getMaxOutputTokensAsLong());
        assertNull(longResult.getMaxOutputTokensAsString());

        VoiceResponseBase stringResult = roundTrip("{\"max_output_tokens\":\"inf\"}");
        assertEquals("inf", stringResult.getMaxOutputTokensAsString());
        assertNull(stringResult.getMaxOutputTokensAsLong());
    }

    @Test
    public void absentMaxOutputTokensReturnsNull() throws IOException {
        VoiceResponseBase response = UnionTypeSerializationTestUtils.deserialize("{}", VoiceResponseBase::fromJson);
        assertNull(response.getMaxOutputTokensAsLong());
        assertNull(response.getMaxOutputTokensAsString());
    }

    private VoiceResponseBase roundTrip(String json) throws IOException {
        VoiceResponseBase value = UnionTypeSerializationTestUtils.deserialize(json, VoiceResponseBase::fromJson);
        String serialized = UnionTypeSerializationTestUtils.serialize(value);
        assertTrue(serialized.contains("\"max_output_tokens\""));
        return UnionTypeSerializationTestUtils.deserialize(serialized, VoiceResponseBase::fromJson);
    }
}
