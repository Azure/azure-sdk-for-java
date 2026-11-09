// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.openai.models.realtime.RealtimeFunctionTool;
import com.openai.models.realtime.RealtimeResponseCreateMcpTool;
import com.openai.models.realtime.RealtimeResponseCreateParams;
import com.openai.models.responses.ToolChoiceFunction;
import com.openai.models.responses.ToolChoiceMcp;
import com.openai.models.responses.ToolChoiceOptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentResponseCreateOptionsSerializationTests {

    @Test
    public void maxOutputTokensVariantsRoundTrip() throws IOException {
        VoiceAgentResponseCreateOptions longResult
            = roundTrip(new VoiceAgentResponseCreateOptions().setMaxOutputTokens(4096L));
        assertEquals(4096L, longResult.getMaxOutputTokensAsLong());
        assertNull(longResult.getMaxOutputTokensAsString());

        VoiceAgentResponseCreateOptions stringResult
            = roundTrip(new VoiceAgentResponseCreateOptions().setMaxOutputTokens("inf"));
        assertEquals("inf", stringResult.getMaxOutputTokensAsString());
        assertNull(stringResult.getMaxOutputTokensAsLong());
    }

    @Test
    public void toolsVariantsRoundTrip() throws IOException {
        RealtimeFunctionTool function
            = RealtimeFunctionTool.builder().name("lookup").type(RealtimeFunctionTool.Type.FUNCTION).build();
        RealtimeResponseCreateMcpTool mcp = RealtimeResponseCreateMcpTool.builder()
            .serverLabel("server")
            .serverUrl("https://example.com/mcp")
            .build();

        VoiceAgentResponseCreateOptions result = roundTrip(new VoiceAgentResponseCreateOptions()
            .setToolsAsOpenAITools(Arrays.asList(RealtimeResponseCreateParams.Tool.ofRealtimeFunction(function),
                RealtimeResponseCreateParams.Tool.ofRealtimeResponseCreateMcp(mcp))));

        List<RealtimeResponseCreateParams.Tool> tools = result.getToolsAsOpenAITools();
        assertEquals(2, tools.size());
        assertTrue(tools.get(0).isRealtimeFunction());
        assertEquals("lookup", tools.get(0).asRealtimeFunction().name().orElse(null));
        assertTrue(tools.get(1).isRealtimeResponseCreateMcp());
        assertEquals("server", tools.get(1).asRealtimeResponseCreateMcp().serverLabel());
    }

    @Test
    public void deserializesToolsVariants() throws IOException {
        VoiceAgentResponseCreateOptions result = UnionTypeSerializationTestUtils.deserialize(
            "{\"tools\":[{\"type\":\"function\",\"name\":\"lookup\"},"
                + "{\"type\":\"mcp\",\"server_label\":\"server\",\"server_url\":\"https://example.com/mcp\"}]}",
            VoiceAgentResponseCreateOptions::fromJson);

        List<RealtimeResponseCreateParams.Tool> tools = result.getToolsAsOpenAITools();
        assertEquals(2, tools.size());
        assertTrue(tools.get(0).isRealtimeFunction());
        assertTrue(tools.get(1).isRealtimeResponseCreateMcp());
    }

    @Test
    public void toolChoiceVariantsRoundTrip() throws IOException {
        VoiceAgentResponseCreateOptions params
            = new VoiceAgentResponseCreateOptions().setToolChoice(ToolChoiceOptions.REQUIRED);
        assertEquals(ToolChoiceOptions.REQUIRED, params.getToolChoiceAsToolChoiceOptions());

        VoiceAgentResponseCreateOptions optionsResult = roundTrip(params);
        assertEquals(ToolChoiceOptions.REQUIRED, optionsResult.getToolChoiceAsToolChoiceOptions());
        assertNull(optionsResult.getToolChoiceAsToolChoiceFunction());
        assertNull(optionsResult.getToolChoiceAsToolChoiceMcp());

        VoiceAgentResponseCreateOptions functionResult = roundTrip(
            new VoiceAgentResponseCreateOptions().setToolChoice(ToolChoiceFunction.builder().name("lookup").build()));
        assertEquals("lookup", functionResult.getToolChoiceAsToolChoiceFunction().name());
        assertNull(functionResult.getToolChoiceAsToolChoiceOptions());

        VoiceAgentResponseCreateOptions mcpResult = roundTrip(
            new VoiceAgentResponseCreateOptions().setToolChoice(ToolChoiceMcp.builder().serverLabel("server").build()));
        assertEquals("server", mcpResult.getToolChoiceAsToolChoiceMcp().serverLabel());
        assertNull(mcpResult.getToolChoiceAsToolChoiceOptions());
    }

    @Test
    public void absentAndNullValuesReturnNull() throws IOException {
        VoiceAgentResponseCreateOptions result
            = UnionTypeSerializationTestUtils.deserialize("{}", VoiceAgentResponseCreateOptions::fromJson);
        assertNull(result.getMaxOutputTokensAsLong());
        assertNull(result.getMaxOutputTokensAsString());
        assertNull(result.getToolsAsOpenAITools());
        assertNull(result.getToolChoiceAsToolChoiceOptions());
        assertNull(result.getToolChoiceAsToolChoiceFunction());
        assertNull(result.getToolChoiceAsToolChoiceMcp());

        result.setMaxOutputTokens("inf").setMaxOutputTokens((String) null);
        result.setToolsAsOpenAITools(null);
        result.setToolChoice(ToolChoiceOptions.AUTO).setToolChoice((ToolChoiceOptions) null);
        assertNull(result.getMaxOutputTokensAsString());
        assertNull(result.getToolsAsOpenAITools());
        assertNull(result.getToolChoiceAsToolChoiceOptions());
    }

    private VoiceAgentResponseCreateOptions roundTrip(VoiceAgentResponseCreateOptions value) throws IOException {
        String json = UnionTypeSerializationTestUtils.serialize(value);
        return UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentResponseCreateOptions::fromJson);
    }
}
