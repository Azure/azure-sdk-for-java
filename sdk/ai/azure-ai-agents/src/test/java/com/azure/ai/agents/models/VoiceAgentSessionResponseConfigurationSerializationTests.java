// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.openai.models.responses.ToolChoiceOptions;
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
    public void toolChoiceVariantsRoundTrip() throws IOException {
        VoiceAgentSessionResponseConfiguration stringResult
            = roundTrip("{\"type\":\"realtime\",\"object\":\"realtime.session\",\"id\":\"s\","
                + "\"model\":\"m\",\"tool_choice\":\"auto\"}");
        assertEquals(ToolChoiceOptions.AUTO, stringResult.getToolChoiceAsToolChoiceOptions());
        assertNull(stringResult.getToolChoiceAsToolChoiceFunction());
        assertNull(stringResult.getToolChoiceAsToolChoiceMcp());

        VoiceAgentSessionResponseConfiguration functionResult
            = roundTrip("{\"type\":\"realtime\",\"object\":\"realtime.session\",\"id\":\"s\","
                + "\"model\":\"m\",\"tool_choice\":{\"type\":\"function\",\"name\":\"lookup\"}}");
        assertEquals("lookup", functionResult.getToolChoiceAsToolChoiceFunction().name());
        assertNull(functionResult.getToolChoiceAsToolChoiceOptions());

        VoiceAgentSessionResponseConfiguration mcpResult
            = roundTrip("{\"type\":\"realtime\",\"object\":\"realtime.session\",\"id\":\"s\","
                + "\"model\":\"m\",\"tool_choice\":{\"type\":\"mcp\",\"server_label\":\"server\"}}");
        assertEquals("server", mcpResult.getToolChoiceAsToolChoiceMcp().serverLabel());
        assertNull(mcpResult.getToolChoiceAsToolChoiceOptions());
    }

    @Test
    public void absentValuesReturnNull() throws IOException {
        VoiceAgentSessionResponseConfiguration result = UnionTypeSerializationTestUtils.deserialize(
            "{\"type\":\"realtime\",\"object\":\"realtime.session\",\"id\":\"s\",\"model\":\"m\"}",
            VoiceAgentSessionResponseConfiguration::fromJson);
        assertNull(result.getMaxOutputTokensAsLong());
        assertNull(result.getMaxOutputTokensAsString());
        assertNull(result.getToolChoiceAsToolChoiceOptions());
        assertNull(result.getToolChoiceAsToolChoiceFunction());
        assertNull(result.getToolChoiceAsToolChoiceMcp());
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
