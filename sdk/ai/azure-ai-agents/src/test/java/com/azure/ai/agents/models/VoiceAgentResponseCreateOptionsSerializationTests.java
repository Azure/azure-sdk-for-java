// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentResponseCreateOptionsSerializationTests {

    @Test
    public void inputJsonRoundTripsWithoutStringEncoding() throws IOException {
        String input = "{\"type\":\"item_reference\",\"id\":\"item-test\"}";
        VoiceAgentResponseCreateOptions original
            = new VoiceAgentResponseCreateOptions().setInput(Arrays.asList(BinaryData.fromString(input), null));
        String json = UnionTypeSerializationTestUtils.serialize(original);
        try (JsonReader reader = JsonProviders.createReader(json)) {
            List<?> serializedInput = (List<?>) reader.readMap(JsonReader::readUntyped).get("input");
            assertEquals(2, serializedInput.size());
            assertEquals("item-test", ((Map<?, ?>) serializedInput.get(0)).get("id"));
            assertNull(serializedInput.get(1));
        }
        VoiceAgentResponseCreateOptions result = roundTrip(original);
        assertEquals("item-test", result.getInput().get(0).toObject(Map.class).get("id"));
        assertNull(result.getInput().get(1));
    }

    @Test
    public void handoffErrorRoundTripsWithoutStringEncoding() throws IOException {
        String json = "{\"event_id\":\"event-test\",\"handoff_id\":\"handoff-test\",\"edge_id\":\"edge-test\","
            + "\"from_node_id\":\"from\",\"to_node_id\":\"to\",\"from_model\":\"model-from\","
            + "\"to_model\":\"model-to\",\"tool_call_id\":\"call-test\",\"node_generation\":1,"
            + "\"reason\":\"cancelled\",\"error\":{\"type\":\"server_error\",\"message\":\"handoff failed\"}}";
        VoiceAgentServerEventSessionHandoffAborted event
            = UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentServerEventSessionHandoffAborted::fromJson);
        String serialized = UnionTypeSerializationTestUtils.serialize(event);
        assertTrue(serialized.contains("\"error\":{"));
        VoiceAgentServerEventSessionHandoffAborted result = UnionTypeSerializationTestUtils.deserialize(serialized,
            VoiceAgentServerEventSessionHandoffAborted::fromJson);
        assertEquals("handoff failed", result.getError().message());

        VoiceAgentServerEventSessionHandoffAborted withoutError = UnionTypeSerializationTestUtils.deserialize(
            json.replace(",\"error\":{\"type\":\"server_error\",\"message\":\"handoff failed\"}", ""),
            VoiceAgentServerEventSessionHandoffAborted::fromJson);
        assertNull(
            UnionTypeSerializationTestUtils
                .deserialize(UnionTypeSerializationTestUtils.serialize(withoutError),
                    VoiceAgentServerEventSessionHandoffAborted::fromJson)
                .getError());
    }

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
