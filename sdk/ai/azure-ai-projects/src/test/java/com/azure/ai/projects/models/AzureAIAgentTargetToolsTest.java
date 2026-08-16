// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.openai.models.responses.Tool;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests for AzureAIAgentTarget tools typed getters/setters using
 * {@link com.openai.models.responses.Tool} (openai-java Stainless SDK).
 */
public class AzureAIAgentTargetToolsTest {

    private static final String AGENT_NAME = "test-agent";

    // ===== Deserialization tests =====

    /**
     * Tests deserialization with a function tool in the tools array.
     */
    @Test
    public void testDeserializationWithFunctionTool() throws IOException {
        String json = "{\"name\":\"test-agent\",\"type\":\"azure_ai_agent\","
            + "\"tools\":[{\"type\":\"function\",\"name\":\"get_weather\","
            + "\"parameters\":{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"}}}}]}";

        AzureAIAgentTarget target = deserializeFromJson(json);

        assertNotNull(target);
        assertEquals(AGENT_NAME, target.getName());

        List<Tool> tools = target.getToolsAsOpenAITools();
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertTrue(tools.get(0).isFunction());
        assertEquals("get_weather", tools.get(0).asFunction().name());
    }

    /**
     * Tests deserialization with a file_search tool in the tools array.
     */
    @Test
    public void testDeserializationWithFileSearchTool() throws IOException {
        String json = "{\"name\":\"test-agent\",\"type\":\"azure_ai_agent\","
            + "\"tools\":[{\"type\":\"file_search\",\"vector_store_ids\":[\"vs_abc123\"]}]}";

        AzureAIAgentTarget target = deserializeFromJson(json);

        assertNotNull(target);
        List<Tool> tools = target.getToolsAsOpenAITools();
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertTrue(tools.get(0).isFileSearch());
    }

    /**
     * Tests deserialization with a web_search tool in the tools array.
     */
    @Test
    public void testDeserializationWithWebSearchTool() throws IOException {
        String json = "{\"name\":\"test-agent\",\"type\":\"azure_ai_agent\","
            + "\"tools\":[{\"type\":\"web_search\",\"search_context_size\":\"medium\"}]}";

        AzureAIAgentTarget target = deserializeFromJson(json);

        assertNotNull(target);
        List<Tool> tools = target.getToolsAsOpenAITools();
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertTrue(tools.get(0).isWebSearch());
    }

    /**
     * Tests deserialization with multiple tools of different types.
     */
    @Test
    public void testDeserializationWithMultipleTools() throws IOException {
        String json = "{\"name\":\"test-agent\",\"type\":\"azure_ai_agent\"," + "\"tools\":["
            + "{\"type\":\"function\",\"name\":\"calculate\",\"parameters\":{\"type\":\"object\"}},"
            + "{\"type\":\"file_search\",\"vector_store_ids\":[\"vs_1\",\"vs_2\"]},"
            + "{\"type\":\"web_search\",\"search_context_size\":\"low\"}" + "]}";

        AzureAIAgentTarget target = deserializeFromJson(json);

        assertNotNull(target);
        List<Tool> tools = target.getToolsAsOpenAITools();
        assertNotNull(tools);
        assertEquals(3, tools.size());
        assertTrue(tools.get(0).isFunction());
        assertTrue(tools.get(1).isFileSearch());
        assertTrue(tools.get(2).isWebSearch());
        assertEquals("calculate", tools.get(0).asFunction().name());
    }

    /**
     * Tests deserialization with an empty tools array.
     */
    @Test
    public void testDeserializationWithEmptyTools() throws IOException {
        String json = "{\"name\":\"test-agent\",\"type\":\"azure_ai_agent\",\"tools\":[]}";

        AzureAIAgentTarget target = deserializeFromJson(json);

        assertNotNull(target);
        List<Tool> tools = target.getToolsAsOpenAITools();
        assertNotNull(tools);
        assertTrue(tools.isEmpty());
    }

    /**
     * Tests deserialization without tools field (null).
     */
    @Test
    public void testDeserializationWithoutTools() throws IOException {
        String json = "{\"name\":\"test-agent\",\"type\":\"azure_ai_agent\"}";

        AzureAIAgentTarget target = deserializeFromJson(json);

        assertNotNull(target);
        assertNull(target.getToolsAsOpenAITools());
    }

    // ===== Serialization tests =====

    /**
     * Tests serialization after setting tools via setToolsAsOpenAITools with a function tool.
     */
    @Test
    public void testSerializationWithFunctionTool() throws IOException {
        // Build via deserialization first (avoids builder complexity)
        String inputJson = "{\"name\":\"test-agent\",\"type\":\"azure_ai_agent\","
            + "\"tools\":[{\"type\":\"function\",\"name\":\"lookup\","
            + "\"parameters\":{\"type\":\"object\",\"properties\":{}}}]}";

        AzureAIAgentTarget target = deserializeFromJson(inputJson);
        List<Tool> tools = target.getToolsAsOpenAITools();

        // Create a new target and set the tools
        AzureAIAgentTarget newTarget = new AzureAIAgentTarget("new-agent").setToolsAsOpenAITools(tools);

        String json = serializeToJson(newTarget);

        assertNotNull(json);
        assertTrue(json.contains("\"tools\""), "JSON should contain tools field");
        assertTrue(json.contains("\"name\":\"new-agent\""));
        assertTrue(json.contains("\"lookup\""), "JSON should contain function tool name");
    }

    /**
     * Tests serialization without tools produces no tools field.
     */
    @Test
    public void testSerializationWithoutTools() throws IOException {
        AzureAIAgentTarget target = new AzureAIAgentTarget(AGENT_NAME);

        String json = serializeToJson(target);

        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"test-agent\""));
        // tools should not appear when null
        assertFalse(json.contains("\"tools\""));
    }

    // ===== Round-trip tests =====

    /**
     * Tests that a function tool survives a full serialize-deserialize round-trip.
     */
    @Test
    public void testRoundTripWithFunctionTool() throws IOException {
        String originalJson = "{\"name\":\"test-agent\",\"type\":\"azure_ai_agent\","
            + "\"tools\":[{\"type\":\"function\",\"name\":\"get_weather\","
            + "\"parameters\":{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"}}}}]}";

        AzureAIAgentTarget original = deserializeFromJson(originalJson);
        String serialized = serializeToJson(original);
        AzureAIAgentTarget deserialized = deserializeFromJson(serialized);

        assertNotNull(deserialized);
        assertEquals("test-agent", deserialized.getName());
        List<Tool> tools = deserialized.getToolsAsOpenAITools();
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertTrue(tools.get(0).isFunction());
        assertEquals("get_weather", tools.get(0).asFunction().name());
    }

    /**
     * Tests that multiple tools survive a full round-trip through setToolsAsOpenAITools.
     */
    @Test
    public void testRoundTripViaTypedSetter() throws IOException {
        String inputJson = "{\"name\":\"agent\",\"type\":\"azure_ai_agent\"," + "\"tools\":["
            + "{\"type\":\"function\",\"name\":\"func1\",\"parameters\":{\"type\":\"object\"}},"
            + "{\"type\":\"file_search\",\"vector_store_ids\":[\"vs_x\"]}" + "]}";

        // Deserialize, get typed tools, set on new target, serialize, deserialize again
        AzureAIAgentTarget source = deserializeFromJson(inputJson);
        List<Tool> tools = source.getToolsAsOpenAITools();

        AzureAIAgentTarget newTarget = new AzureAIAgentTarget("new-agent").setToolsAsOpenAITools(tools);

        String serialized = serializeToJson(newTarget);
        AzureAIAgentTarget result = deserializeFromJson(serialized);

        assertNotNull(result);
        assertEquals("new-agent", result.getName());
        List<Tool> resultTools = result.getToolsAsOpenAITools();
        assertNotNull(resultTools);
        assertEquals(2, resultTools.size());
        assertTrue(resultTools.get(0).isFunction());
        assertTrue(resultTools.get(1).isFileSearch());
    }

    /**
     * Tests that other AzureAIAgentTarget properties (version, toolDescriptions) are
     * preserved alongside typed tools.
     */
    @Test
    public void testOtherPropertiesPreserved() throws IOException {
        String json = "{\"name\":\"agent-v2\",\"type\":\"azure_ai_agent\",\"version\":\"2.0\","
            + "\"tool_descriptions\":[{\"name\":\"my_tool\",\"description\":\"Does stuff\"}],"
            + "\"tools\":[{\"type\":\"function\",\"name\":\"calc\",\"parameters\":{\"type\":\"object\"}}]}";

        AzureAIAgentTarget target = deserializeFromJson(json);

        assertNotNull(target);
        assertEquals("agent-v2", target.getName());
        assertEquals("2.0", target.getVersion());
        assertNotNull(target.getToolDescriptions());
        assertEquals(1, target.getToolDescriptions().size());
        assertEquals("my_tool", target.getToolDescriptions().get(0).getName());

        List<Tool> tools = target.getToolsAsOpenAITools();
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertTrue(tools.get(0).isFunction());
    }

    /**
     * Tests deserialization with an MCP tool.
     */
    @Test
    public void testDeserializationWithMcpTool() throws IOException {
        String json = "{\"name\":\"test-agent\",\"type\":\"azure_ai_agent\","
            + "\"tools\":[{\"type\":\"mcp\",\"server_label\":\"my-mcp\","
            + "\"server_url\":\"https://mcp.example.com\"}]}";

        AzureAIAgentTarget target = deserializeFromJson(json);

        assertNotNull(target);
        List<Tool> tools = target.getToolsAsOpenAITools();
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertTrue(tools.get(0).isMcp());
    }

    /**
     * Tests deserialization with a code_interpreter tool.
     */
    @Test
    public void testDeserializationWithCodeInterpreterTool() throws IOException {
        String json = "{\"name\":\"test-agent\",\"type\":\"azure_ai_agent\","
            + "\"tools\":[{\"type\":\"code_interpreter\",\"container\":\"ctr_abc123\"}]}";

        AzureAIAgentTarget target = deserializeFromJson(json);

        assertNotNull(target);
        List<Tool> tools = target.getToolsAsOpenAITools();
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertTrue(tools.get(0).isCodeInterpreter());
    }

    // ===== Helper methods =====

    private String serializeToJson(AzureAIAgentTarget target) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            target.toJson(writer);
        }
        return outputStream.toString("UTF-8");
    }

    private AzureAIAgentTarget deserializeFromJson(String json) throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return AzureAIAgentTarget.fromJson(reader);
        }
    }
}
