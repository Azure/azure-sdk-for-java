// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.openai.models.responses.ToolChoiceFunction;
import com.openai.models.responses.ToolChoiceMcp;
import com.openai.models.responses.ToolChoiceOptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InlineVoiceSubagentSerializationTests {

    @Test
    public void toolChoiceModesRoundTrip() throws IOException {
        for (ToolChoiceOptions mode : Arrays.asList(ToolChoiceOptions.NONE, ToolChoiceOptions.AUTO,
            ToolChoiceOptions.REQUIRED)) {
            InlineVoiceSubagent original = new InlineVoiceSubagent("specialist", "Specialist").setToolChoice(mode);
            assertEquals(mode, original.getToolChoiceAsToolChoiceOptions());
            String json = UnionTypeSerializationTestUtils.serialize(original);
            assertTrue(json.contains("\"tool_choice\":\"" + mode + "\""));
            InlineVoiceSubagent result = deserialize(json);
            assertEquals(mode, result.getToolChoiceAsToolChoiceOptions());
            assertNull(result.getToolChoiceAsToolChoiceFunction());
            assertNull(result.getToolChoiceAsToolChoiceMcp());
        }
    }

    @Test
    public void toolChoiceObjectsRoundTrip() throws IOException {
        InlineVoiceSubagent function = new InlineVoiceSubagent("specialist", "Specialist")
            .setToolChoice(ToolChoiceFunction.builder().name("lookup").build());
        String functionJson = UnionTypeSerializationTestUtils.serialize(function);
        assertTrue(functionJson.contains("\"type\":\"function\""));
        assertTrue(functionJson.contains("\"name\":\"lookup\""));
        InlineVoiceSubagent functionResult = deserialize(functionJson);
        assertEquals("lookup", functionResult.getToolChoiceAsToolChoiceFunction().name());
        assertNull(functionResult.getToolChoiceAsToolChoiceOptions());
        assertNull(functionResult.getToolChoiceAsToolChoiceMcp());

        InlineVoiceSubagent mcp = new InlineVoiceSubagent("specialist", "Specialist")
            .setToolChoice(ToolChoiceMcp.builder().serverLabel("server").name("search").build());
        String mcpJson = UnionTypeSerializationTestUtils.serialize(mcp);
        assertTrue(mcpJson.contains("\"type\":\"mcp\""));
        assertTrue(mcpJson.contains("\"server_label\":\"server\""));
        InlineVoiceSubagent mcpResult = deserialize(mcpJson);
        assertEquals("server", mcpResult.getToolChoiceAsToolChoiceMcp().serverLabel());
        assertEquals("search", mcpResult.getToolChoiceAsToolChoiceMcp().name().get());
        assertNull(mcpResult.getToolChoiceAsToolChoiceOptions());
        assertNull(mcpResult.getToolChoiceAsToolChoiceFunction());
    }

    @Test
    public void absentExplicitNullAndClearedValuesReturnNull() throws IOException {
        for (String json : Arrays.asList("{\"id\":\"specialist\",\"description\":\"Specialist\"}",
            "{\"id\":\"specialist\",\"description\":\"Specialist\",\"tool_choice\":null}")) {
            assertUnset(deserialize(json));
        }
        InlineVoiceSubagent value
            = new InlineVoiceSubagent("specialist", "Specialist").setToolChoice(ToolChoiceOptions.AUTO)
                .setToolChoice((ToolChoiceOptions) null);
        assertUnset(value);
        assertUnset(deserialize(UnionTypeSerializationTestUtils.serialize(value)));
        value.setToolChoice(ToolChoiceFunction.builder().name("lookup").build())
            .setToolChoice((ToolChoiceFunction) null);
        assertUnset(value);
        value.setToolChoice(ToolChoiceMcp.builder().serverLabel("server").build()).setToolChoice((ToolChoiceMcp) null);
        assertUnset(value);
    }

    private static void assertUnset(InlineVoiceSubagent value) {
        assertNull(value.getToolChoiceAsToolChoiceOptions());
        assertNull(value.getToolChoiceAsToolChoiceFunction());
        assertNull(value.getToolChoiceAsToolChoiceMcp());
    }

    private static InlineVoiceSubagent deserialize(String json) throws IOException {
        return UnionTypeSerializationTestUtils.deserialize(json, InlineVoiceSubagent::fromJson);
    }
}
