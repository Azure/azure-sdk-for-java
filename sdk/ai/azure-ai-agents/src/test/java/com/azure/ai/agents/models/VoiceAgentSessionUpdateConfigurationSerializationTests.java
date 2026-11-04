// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class VoiceAgentSessionUpdateConfigurationSerializationTests {

    @Test
    public void maxOutputTokensVariantsRoundTrip() throws IOException {
        VoiceAgentSessionUpdateConfiguration longResult
            = roundTrip(new VoiceAgentSessionUpdateConfiguration().setMaxOutputTokens(1024L));
        assertEquals(1024L, longResult.getMaxOutputTokensAsLong());
        assertNull(longResult.getMaxOutputTokensAsString());

        VoiceAgentSessionUpdateConfiguration stringResult
            = roundTrip(new VoiceAgentSessionUpdateConfiguration().setMaxOutputTokens("inf"));
        assertEquals("inf", stringResult.getMaxOutputTokensAsString());
        assertNull(stringResult.getMaxOutputTokensAsLong());
    }

    @Test
    public void absentAndNullValuesReturnNull() throws IOException {
        VoiceAgentSessionUpdateConfiguration result = UnionTypeSerializationTestUtils
            .deserialize("{\"type\":\"realtime\"}", VoiceAgentSessionUpdateConfiguration::fromJson);
        assertNull(result.getMaxOutputTokensAsLong());
        assertNull(result.getMaxOutputTokensAsString());

        result.setMaxOutputTokens("inf").setMaxOutputTokens((String) null);
        assertNull(result.getMaxOutputTokensAsString());
    }

    private VoiceAgentSessionUpdateConfiguration roundTrip(VoiceAgentSessionUpdateConfiguration value)
        throws IOException {
        String json = UnionTypeSerializationTestUtils.serialize(value);
        return UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentSessionUpdateConfiguration::fromJson);
    }
}
