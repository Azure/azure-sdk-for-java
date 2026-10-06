// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.core.util.BinaryData;
import com.openai.models.realtime.RealtimeFunctionTool;
import com.openai.models.realtime.RealtimeToolsConfigUnion;
import com.openai.models.realtime.RealtimeTruncationRetentionRatio;
import com.openai.models.responses.ToolChoiceFunction;
import com.openai.models.responses.ToolChoiceMcp;
import com.openai.models.responses.ToolChoiceOptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeSessionConfigurationSerializationTests {

    @Test
    public void tracingVariantsRoundTrip() throws IOException {
        RealtimeSessionConfiguration stringResult = roundTrip(new RealtimeSessionConfiguration().setTracing("auto"));
        assertEquals("auto", stringResult.getTracingAsString());
        assertNull(stringResult.getTracingAsRealtimeSessionTracingConfiguration());

        RealtimeSessionTracingConfiguration tracing
            = new RealtimeSessionTracingConfiguration().setWorkflowName("workflow")
                .setMetadata(Collections.singletonMap("key", BinaryData.fromObject("value")));
        RealtimeSessionConfiguration modelResult = roundTrip(new RealtimeSessionConfiguration().setTracing(tracing));
        assertEquals("workflow", modelResult.getTracingAsRealtimeSessionTracingConfiguration().getWorkflowName());
        assertEquals("value",
            modelResult.getTracingAsRealtimeSessionTracingConfiguration()
                .getMetadata()
                .get("key")
                .toObject(String.class));
        assertNull(modelResult.getTracingAsString());
    }

    @Test
    public void toolsVariantsRoundTrip() throws IOException {
        RealtimeFunctionTool function
            = RealtimeFunctionTool.builder().name("lookup").type(RealtimeFunctionTool.Type.FUNCTION).build();
        RealtimeToolsConfigUnion.Mcp mcp
            = RealtimeToolsConfigUnion.Mcp.builder().serverLabel("server").serverUrl("https://example.com/mcp").build();

        RealtimeSessionConfiguration result = roundTrip(new RealtimeSessionConfiguration().setToolsAsOpenAITools(
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
        RealtimeSessionConfiguration result = UnionTypeSerializationTestUtils.deserialize(
            "{\"type\":\"realtime\",\"tools\":[{\"type\":\"function\",\"name\":\"lookup\"},"
                + "{\"type\":\"mcp\",\"server_label\":\"server\",\"server_url\":\"https://example.com/mcp\"}]}",
            RealtimeSessionConfiguration::fromJson);

        List<RealtimeToolsConfigUnion> tools = result.getToolsAsOpenAITools();
        assertEquals(2, tools.size());
        assertTrue(tools.get(0).isFunction());
        assertTrue(tools.get(1).isMcp());
    }

    @Test
    public void toolChoiceVariantsRoundTrip() throws IOException {
        RealtimeSessionConfiguration request
            = new RealtimeSessionConfiguration().setToolChoice(ToolChoiceOptions.REQUIRED);
        assertEquals(ToolChoiceOptions.REQUIRED, request.getToolChoiceAsToolChoiceOptions());
        assertEquals("required", getSerializedToolChoice(request));

        RealtimeSessionConfiguration stringResult = roundTrip(request);
        assertEquals(ToolChoiceOptions.REQUIRED, stringResult.getToolChoiceAsToolChoiceOptions());
        assertNull(stringResult.getToolChoiceAsToolChoiceFunction());
        assertNull(stringResult.getToolChoiceAsToolChoiceMcp());

        ToolChoiceFunction function = ToolChoiceFunction.builder().name("lookup").build();
        RealtimeSessionConfiguration functionRequest = new RealtimeSessionConfiguration().setToolChoice(function);
        assertEquals(parseJson("{\"type\":\"function\",\"name\":\"lookup\"}"),
            getSerializedToolChoice(functionRequest));

        RealtimeSessionConfiguration functionResult = roundTrip(functionRequest);
        assertEquals("lookup", functionResult.getToolChoiceAsToolChoiceFunction().name());
        assertNull(functionResult.getToolChoiceAsToolChoiceOptions());
        assertNull(functionResult.getToolChoiceAsToolChoiceMcp());

        ToolChoiceMcp mcp = ToolChoiceMcp.builder().serverLabel("server").name("search").build();
        RealtimeSessionConfiguration mcpResult = roundTrip(new RealtimeSessionConfiguration().setToolChoice(mcp));
        assertEquals("server", mcpResult.getToolChoiceAsToolChoiceMcp().serverLabel());
        assertNull(mcpResult.getToolChoiceAsToolChoiceOptions());
        assertNull(mcpResult.getToolChoiceAsToolChoiceFunction());
    }

    @Test
    public void serverVadDurationsRoundTripAsMilliseconds() throws IOException {
        RealtimeServerVadTurnDetection original
            = new RealtimeServerVadTurnDetection().setSilenceDuration(Duration.ofMillis(500))
                .setIdleTimeout(Duration.ofMillis(1500));

        String json = UnionTypeSerializationTestUtils.serialize(original);
        assertTrue(json.contains("\"silence_duration_ms\":500"));
        assertTrue(json.contains("\"idle_timeout_ms\":1500"));

        RealtimeServerVadTurnDetection result
            = UnionTypeSerializationTestUtils.deserialize(json, RealtimeServerVadTurnDetection::fromJson);
        assertEquals(Duration.ofMillis(500), result.getSilenceDuration());
        assertEquals(Duration.ofMillis(1500), result.getIdleTimeout());
    }

    @Test
    public void maxOutputTokensVariantsRoundTrip() throws IOException {
        RealtimeSessionConfiguration numberResult
            = roundTrip(new RealtimeSessionConfiguration().setMaxOutputTokens(4096L));
        assertEquals(4096L, numberResult.getMaxOutputTokensAsLong());
        assertNull(numberResult.getMaxOutputTokensAsString());

        RealtimeSessionConfiguration stringResult
            = roundTrip(new RealtimeSessionConfiguration().setMaxOutputTokens("inf"));
        assertEquals("inf", stringResult.getMaxOutputTokensAsString());
        assertNull(stringResult.getMaxOutputTokensAsLong());
    }

    @Test
    public void truncationVariantsRoundTrip() throws IOException {
        RealtimeSessionConfiguration stringResult
            = roundTrip(new RealtimeSessionConfiguration().setTruncation("disabled"));
        assertEquals("disabled", stringResult.getTruncationAsString());
        assertNull(stringResult.getTruncationAsRealtimeTruncationRetentionRatio());

        RealtimeTruncationRetentionRatio truncation
            = RealtimeTruncationRetentionRatio.builder().retentionRatio(0.8).build();
        RealtimeSessionConfiguration modelResult
            = roundTrip(new RealtimeSessionConfiguration().setTruncation(truncation));
        assertEquals(0.8, modelResult.getTruncationAsRealtimeTruncationRetentionRatio().retentionRatio());
        assertNull(modelResult.getTruncationAsString());
    }

    @Test
    public void deserializesEveryUnionProperty() throws IOException {
        String json = "{\"type\":\"realtime\",\"tracing\":{\"workflow_name\":\"workflow\"},"
            + "\"tool_choice\":{\"type\":\"function\",\"name\":\"lookup\"}," + "\"max_output_tokens\":123,"
            + "\"truncation\":{\"type\":\"retention_ratio\",\"retention_ratio\":0.5}}";

        RealtimeSessionConfiguration result
            = UnionTypeSerializationTestUtils.deserialize(json, RealtimeSessionConfiguration::fromJson);

        assertEquals("workflow", result.getTracingAsRealtimeSessionTracingConfiguration().getWorkflowName());
        assertEquals("lookup", result.getToolChoiceAsToolChoiceFunction().name());
        assertEquals(123L, result.getMaxOutputTokensAsLong());
        assertEquals(0.5, result.getTruncationAsRealtimeTruncationRetentionRatio().retentionRatio());
    }

    @Test
    public void absentAndNullValuesReturnNull() throws IOException {
        RealtimeSessionConfiguration result = UnionTypeSerializationTestUtils.deserialize("{\"type\":\"realtime\"}",
            RealtimeSessionConfiguration::fromJson);
        assertNull(result.getTracingAsString());
        assertNull(result.getTracingAsRealtimeSessionTracingConfiguration());
        assertNull(result.getToolsAsOpenAITools());
        assertNull(result.getToolChoiceAsToolChoiceOptions());
        assertNull(result.getToolChoiceAsToolChoiceFunction());
        assertNull(result.getToolChoiceAsToolChoiceMcp());
        assertNull(result.getMaxOutputTokensAsLong());
        assertNull(result.getMaxOutputTokensAsString());
        assertNull(result.getTruncationAsString());
        assertNull(result.getTruncationAsRealtimeTruncationRetentionRatio());

        String serialized
            = UnionTypeSerializationTestUtils.serialize(new RealtimeSessionConfiguration().setTracing((String) null)
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

    private Object getSerializedToolChoice(RealtimeSessionConfiguration value) throws IOException {
        Object serialized = parseJson(UnionTypeSerializationTestUtils.serialize(value));
        return ((Map<?, ?>) serialized).get("tool_choice");
    }

    private Object parseJson(String json) throws IOException {
        return UnionTypeSerializationTestUtils.deserialize(json, reader -> reader.readUntyped());
    }

    private RealtimeSessionConfiguration roundTrip(RealtimeSessionConfiguration value) throws IOException {
        String json = UnionTypeSerializationTestUtils.serialize(value);
        return UnionTypeSerializationTestUtils.deserialize(json, RealtimeSessionConfiguration::fromJson);
    }
}
