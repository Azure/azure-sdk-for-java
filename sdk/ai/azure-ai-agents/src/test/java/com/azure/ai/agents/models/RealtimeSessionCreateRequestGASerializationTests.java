// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.openai.models.realtime.RealtimeFunctionTool;
import com.openai.models.realtime.RealtimeToolsConfigUnion;
import com.openai.models.responses.ToolChoiceFunction;
import com.openai.models.responses.ToolChoiceMcp;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeSessionCreateRequestGASerializationTests {

    @Test
    public void tracingVariantsRoundTrip() throws IOException {
        RealtimeSessionCreateRequestGA stringResult
            = roundTrip(new RealtimeSessionCreateRequestGA().setTracing("auto"));
        assertEquals("auto", stringResult.getTracingAsString());
        assertNull(stringResult.getTracingAsRealtimeSessionCreateRequestGATracing());

        RealtimeSessionCreateRequestGATracing tracing
            = new RealtimeSessionCreateRequestGATracing().setWorkflowName("workflow");
        RealtimeSessionCreateRequestGA modelResult
            = roundTrip(new RealtimeSessionCreateRequestGA().setTracing(tracing));
        assertEquals("workflow", modelResult.getTracingAsRealtimeSessionCreateRequestGATracing().getWorkflowName());
        assertNull(modelResult.getTracingAsString());
    }

    @Test
    public void toolsVariantsRoundTrip() throws IOException {
        RealtimeFunctionTool function
            = RealtimeFunctionTool.builder().name("lookup").type(RealtimeFunctionTool.Type.FUNCTION).build();
        RealtimeToolsConfigUnion.Mcp mcp
            = RealtimeToolsConfigUnion.Mcp.builder().serverLabel("server").serverUrl("https://example.com/mcp").build();

        RealtimeSessionCreateRequestGA result = roundTrip(new RealtimeSessionCreateRequestGA().setToolsAsOpenAITools(
            Arrays.asList(RealtimeToolsConfigUnion.ofFunction(function), RealtimeToolsConfigUnion.ofMcp(mcp))));

        List<RealtimeToolsConfigUnion> tools = result.getToolsAsOpenAITools();
        assertEquals(2, tools.size());
        assertTrue(tools.get(0).isFunction());
        assertEquals("lookup", tools.get(0).asFunction().name().orElse(null));
        assertTrue(tools.get(1).isMcp());
        assertEquals("server", tools.get(1).asMcp().serverLabel());
    }

    @Test
    public void deserializesToolsVariants() throws IOException {
        RealtimeSessionCreateRequestGA result = UnionTypeSerializationTestUtils.deserialize(
            "{\"type\":\"realtime\",\"tools\":[{\"type\":\"function\",\"name\":\"lookup\"},"
                + "{\"type\":\"mcp\",\"server_label\":\"server\",\"server_url\":\"https://example.com/mcp\"}]}",
            RealtimeSessionCreateRequestGA::fromJson);

        List<RealtimeToolsConfigUnion> tools = result.getToolsAsOpenAITools();
        assertEquals(2, tools.size());
        assertTrue(tools.get(0).isFunction());
        assertTrue(tools.get(1).isMcp());
    }

    @Test
    public void toolChoiceVariantsRoundTrip() throws IOException {
        RealtimeSessionCreateRequestGA stringResult
            = roundTrip(new RealtimeSessionCreateRequestGA().setToolChoice(ToolChoiceOptions.REQUIRED));
        assertEquals(ToolChoiceOptions.REQUIRED, stringResult.getToolChoiceAsToolChoiceOptions());
        assertNull(stringResult.getToolChoiceAsToolChoiceFunction());
        assertNull(stringResult.getToolChoiceAsToolChoiceMcp());

        ToolChoiceFunction function = ToolChoiceFunction.builder().name("lookup").build();
        RealtimeSessionCreateRequestGA functionResult
            = roundTrip(new RealtimeSessionCreateRequestGA().setToolChoice(function));
        assertEquals("lookup", functionResult.getToolChoiceAsToolChoiceFunction().name());
        assertNull(functionResult.getToolChoiceAsToolChoiceOptions());
        assertNull(functionResult.getToolChoiceAsToolChoiceMcp());

        ToolChoiceMcp mcp = ToolChoiceMcp.builder().serverLabel("server").name("search").build();
        RealtimeSessionCreateRequestGA mcpResult = roundTrip(new RealtimeSessionCreateRequestGA().setToolChoice(mcp));
        assertEquals("server", mcpResult.getToolChoiceAsToolChoiceMcp().serverLabel());
        assertNull(mcpResult.getToolChoiceAsToolChoiceOptions());
        assertNull(mcpResult.getToolChoiceAsToolChoiceFunction());
    }

    @Test
    public void maxOutputTokensVariantsRoundTrip() throws IOException {
        RealtimeSessionCreateRequestGA numberResult
            = roundTrip(new RealtimeSessionCreateRequestGA().setMaxOutputTokens(4096L));
        assertEquals(4096L, numberResult.getMaxOutputTokensAsLong());
        assertNull(numberResult.getMaxOutputTokensAsString());

        RealtimeSessionCreateRequestGA stringResult
            = roundTrip(new RealtimeSessionCreateRequestGA().setMaxOutputTokens("inf"));
        assertEquals("inf", stringResult.getMaxOutputTokensAsString());
        assertNull(stringResult.getMaxOutputTokensAsLong());
    }

    @Test
    public void truncationVariantsRoundTrip() throws IOException {
        RealtimeSessionCreateRequestGA stringResult
            = roundTrip(new RealtimeSessionCreateRequestGA().setTruncation("disabled"));
        assertEquals("disabled", stringResult.getTruncationAsString());
        assertNull(stringResult.getTruncationAsRealtimeClientEventSessionUpdateSessionTruncationRetentionRatio());

        RealtimeClientEventSessionUpdateSessionTruncationRetentionRatio truncation
            = new RealtimeClientEventSessionUpdateSessionTruncationRetentionRatio(0.8);
        RealtimeSessionCreateRequestGA modelResult
            = roundTrip(new RealtimeSessionCreateRequestGA().setTruncation(truncation));
        assertEquals(0.8, modelResult.getTruncationAsRealtimeClientEventSessionUpdateSessionTruncationRetentionRatio()
            .getRetentionRatio());
        assertNull(modelResult.getTruncationAsString());
    }

    @Test
    public void deserializesEveryUnionProperty() throws IOException {
        String json = "{\"type\":\"realtime\",\"tracing\":{\"workflow_name\":\"workflow\"},"
            + "\"tool_choice\":{\"type\":\"function\",\"name\":\"lookup\"}," + "\"max_output_tokens\":123,"
            + "\"truncation\":{\"type\":\"retention_ratio\",\"retention_ratio\":0.5}}";

        RealtimeSessionCreateRequestGA result
            = UnionTypeSerializationTestUtils.deserialize(json, RealtimeSessionCreateRequestGA::fromJson);

        assertEquals("workflow", result.getTracingAsRealtimeSessionCreateRequestGATracing().getWorkflowName());
        assertEquals("lookup", result.getToolChoiceAsToolChoiceFunction().name());
        assertEquals(123L, result.getMaxOutputTokensAsLong());
        assertEquals(0.5, result.getTruncationAsRealtimeClientEventSessionUpdateSessionTruncationRetentionRatio()
            .getRetentionRatio());
    }

    @Test
    public void absentAndNullValuesReturnNull() throws IOException {
        RealtimeSessionCreateRequestGA result = UnionTypeSerializationTestUtils.deserialize("{\"type\":\"realtime\"}",
            RealtimeSessionCreateRequestGA::fromJson);
        assertNull(result.getTracingAsString());
        assertNull(result.getTracingAsRealtimeSessionCreateRequestGATracing());
        assertNull(result.getToolsAsOpenAITools());
        assertNull(result.getToolChoiceAsToolChoiceOptions());
        assertNull(result.getToolChoiceAsToolChoiceFunction());
        assertNull(result.getToolChoiceAsToolChoiceMcp());
        assertNull(result.getMaxOutputTokensAsLong());
        assertNull(result.getMaxOutputTokensAsString());
        assertNull(result.getTruncationAsString());
        assertNull(result.getTruncationAsRealtimeClientEventSessionUpdateSessionTruncationRetentionRatio());

        String serialized
            = UnionTypeSerializationTestUtils.serialize(new RealtimeSessionCreateRequestGA().setTracing((String) null)
                .setToolsAsOpenAITools(null)
                .setToolChoice((ToolChoiceOptions) null)
                .setMaxOutputTokens((String) null)
                .setTruncation((String) null));
        assertNotNull(serialized);
        assertTrue(!serialized.contains("\"tracing\"")
            && !serialized.contains("\"tools\"")
            && !serialized.contains("\"tool_choice\"")
            && !serialized.contains("\"max_output_tokens\"")
            && !serialized.contains("\"truncation\""));
    }

    private RealtimeSessionCreateRequestGA roundTrip(RealtimeSessionCreateRequestGA value) throws IOException {
        String json = UnionTypeSerializationTestUtils.serialize(value);
        return UnionTypeSerializationTestUtils.deserialize(json, RealtimeSessionCreateRequestGA::fromJson);
    }
}
