// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for McpTool serialization, focusing on the allowedTools and requireApproval union type handling.
 * allowedTools is a union type: List&lt;String&gt; | McpToolFilter.
 * requireApproval is a union type: String ("always"/"never") | McpToolRequireApproval.
 */
public class McpToolSerializationTests {

    private static final String TEST_SERVER_LABEL = "test-mcp-server";

    // ===== allowedTools tests =====

    /**
     * Tests serialization when allowedTools is not set (null).
     */
    @Test
    public void testSerializationWithoutAllowedTools() throws IOException {
        McpTool tool = new McpTool(TEST_SERVER_LABEL);

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertTrue(json.contains("\"server_label\":\"test-mcp-server\""));
        assertFalse(json.contains("\"allowed_tools\""));
    }

    /**
     * Tests serialization with allowedTools set to a list of tool names.
     */
    @Test
    public void testSerializationWithAllowedToolsAsStringList() throws IOException {
        McpTool tool = new McpTool(TEST_SERVER_LABEL).setAllowedTools(Arrays.asList("tool_a", "tool_b", "tool_c"));

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertTrue(json.contains("\"allowed_tools\""));
        assertTrue(json.contains("tool_a"));
        assertTrue(json.contains("tool_b"));
        assertTrue(json.contains("tool_c"));
    }

    /**
     * Tests serialization with allowedTools set to an McpToolFilter.
     */
    @Test
    public void testSerializationWithAllowedToolsAsMcpToolFilter() throws IOException {
        McpToolFilter filter = new McpToolFilter().setToolNames(Arrays.asList("func_1", "func_2")).setReadOnly(true);

        McpTool tool = new McpTool(TEST_SERVER_LABEL).setAllowedTools(filter);

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertTrue(json.contains("\"allowed_tools\""));
        assertTrue(json.contains("func_1"));
        assertTrue(json.contains("func_2"));
        assertTrue(json.contains("\"read_only\":true"));
    }

    /**
     * Tests deserialization with allowedTools set to a list of tool names.
     */
    @Test
    public void testDeserializationWithAllowedToolsAsStringList() throws IOException {
        String json
            = "{\"server_label\":\"test-mcp-server\",\"type\":\"mcp\",\"allowed_tools\":[\"tool_a\",\"tool_b\"]}";

        McpTool tool = deserializeFromJson(json);

        assertNotNull(tool);
        assertEquals(TEST_SERVER_LABEL, tool.getServerLabel());
        List<String> toolNames = tool.getAllowedToolsAsStringList();
        assertNotNull(toolNames);
        assertEquals(2, toolNames.size());
        assertTrue(toolNames.contains("tool_a"));
        assertTrue(toolNames.contains("tool_b"));
    }

    /**
     * Tests deserialization with allowedTools set to an McpToolFilter object.
     */
    @Test
    public void testDeserializationWithAllowedToolsAsMcpToolFilter() throws IOException {
        String json
            = "{\"server_label\":\"test-mcp-server\",\"type\":\"mcp\",\"allowed_tools\":{\"tool_names\":[\"func_1\"],\"read_only\":true}}";

        McpTool tool = deserializeFromJson(json);

        assertNotNull(tool);
        McpToolFilter filter = tool.getAllowedToolsAsMcpToolFilter();
        assertNotNull(filter);
        assertNotNull(filter.getToolNames());
        assertEquals(1, filter.getToolNames().size());
        assertEquals("func_1", filter.getToolNames().get(0));
        assertEquals(true, filter.isReadOnly());
    }

    /**
     * Tests deserialization with allowedTools absent.
     */
    @Test
    public void testDeserializationWithoutAllowedTools() throws IOException {
        String json = "{\"server_label\":\"test-mcp-server\",\"type\":\"mcp\"}";

        McpTool tool = deserializeFromJson(json);

        assertNotNull(tool);
        assertNull(tool.getAllowedToolsAsStringList());
        assertNull(tool.getAllowedToolsAsMcpToolFilter());
    }

    /**
     * Tests round-trip with allowedTools as a list of tool names.
     */
    @Test
    public void testRoundTripWithAllowedToolsAsStringList() throws IOException {
        McpTool original = new McpTool(TEST_SERVER_LABEL).setAllowedTools(Arrays.asList("my_tool_1", "my_tool_2"));

        String json = serializeToJson(original);
        McpTool deserialized = deserializeFromJson(json);

        assertNotNull(deserialized);
        List<String> tools = deserialized.getAllowedToolsAsStringList();
        assertNotNull(tools);
        assertEquals(2, tools.size());
    }

    /**
     * Tests round-trip with allowedTools as an McpToolFilter.
     */
    @Test
    public void testRoundTripWithAllowedToolsAsMcpToolFilter() throws IOException {
        McpToolFilter filter = new McpToolFilter().setToolNames(Arrays.asList("func_x")).setReadOnly(false);

        McpTool original = new McpTool(TEST_SERVER_LABEL).setAllowedTools(filter);

        String json = serializeToJson(original);
        McpTool deserialized = deserializeFromJson(json);

        assertNotNull(deserialized);
        McpToolFilter deserializedFilter = deserialized.getAllowedToolsAsMcpToolFilter();
        assertNotNull(deserializedFilter);
        assertEquals(1, deserializedFilter.getToolNames().size());
    }

    // ===== requireApproval tests =====

    /**
     * Tests serialization when requireApproval is not set (null).
     */
    @Test
    public void testSerializationWithoutRequireApproval() throws IOException {
        McpTool tool = new McpTool(TEST_SERVER_LABEL);

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertFalse(json.contains("\"require_approval\""));
    }

    /**
     * Tests serialization with requireApproval set to "always" string.
     */
    @Test
    public void testSerializationWithRequireApprovalAlwaysString() throws IOException {
        McpTool tool = new McpTool(TEST_SERVER_LABEL).setRequireApproval("always");

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertTrue(json.contains("\"require_approval\""));
        assertTrue(json.contains("always"));
    }

    /**
     * Tests serialization with requireApproval set to "never" string.
     */
    @Test
    public void testSerializationWithRequireApprovalNeverString() throws IOException {
        McpTool tool = new McpTool(TEST_SERVER_LABEL).setRequireApproval("never");

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertTrue(json.contains("\"require_approval\""));
        assertTrue(json.contains("never"));
    }

    /**
     * Tests serialization with requireApproval set to an McpToolRequireApproval filter.
     */
    @Test
    public void testSerializationWithRequireApprovalAsMcpToolRequireApproval() throws IOException {
        McpToolFilter alwaysFilter = new McpToolFilter().setToolNames(Arrays.asList("dangerous_tool"));
        McpToolFilter neverFilter = new McpToolFilter().setToolNames(Arrays.asList("safe_tool"));
        McpToolRequireApproval approval = new McpToolRequireApproval().setAlways(alwaysFilter).setNever(neverFilter);

        McpTool tool = new McpTool(TEST_SERVER_LABEL).setRequireApproval(approval);

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertTrue(json.contains("\"require_approval\""));
        assertTrue(json.contains("dangerous_tool"));
        assertTrue(json.contains("safe_tool"));
        assertTrue(json.contains("\"always\""));
        assertTrue(json.contains("\"never\""));
    }

    /**
     * Tests deserialization with requireApproval set to "always" string.
     */
    @Test
    public void testDeserializationWithRequireApprovalAlwaysString() throws IOException {
        String json = "{\"server_label\":\"test-mcp-server\",\"type\":\"mcp\",\"require_approval\":\"always\"}";

        McpTool tool = deserializeFromJson(json);

        assertNotNull(tool);
        assertNotNull(tool.getRequireApprovalAsString());
        assertTrue(tool.getRequireApprovalAsString().contains("always"));
    }

    /**
     * Tests deserialization with requireApproval set to "never" string.
     */
    @Test
    public void testDeserializationWithRequireApprovalNeverString() throws IOException {
        String json = "{\"server_label\":\"test-mcp-server\",\"type\":\"mcp\",\"require_approval\":\"never\"}";

        McpTool tool = deserializeFromJson(json);

        assertNotNull(tool);
        assertNotNull(tool.getRequireApprovalAsString());
        assertTrue(tool.getRequireApprovalAsString().contains("never"));
    }

    /**
     * Tests deserialization with requireApproval set to an McpToolRequireApproval object.
     */
    @Test
    public void testDeserializationWithRequireApprovalAsMcpToolRequireApproval() throws IOException {
        String json
            = "{\"server_label\":\"test-mcp-server\",\"type\":\"mcp\",\"require_approval\":{\"always\":{\"tool_names\":[\"dangerous_tool\"]},\"never\":{\"tool_names\":[\"safe_tool\"]}}}";

        McpTool tool = deserializeFromJson(json);

        assertNotNull(tool);
        McpToolRequireApproval approval = tool.getRequireApprovalAsMcpToolRequireApproval();
        assertNotNull(approval);
        assertNotNull(approval.getAlways());
        assertNotNull(approval.getNever());
        assertEquals(1, approval.getAlways().getToolNames().size());
        assertEquals("dangerous_tool", approval.getAlways().getToolNames().get(0));
        assertEquals("safe_tool", approval.getNever().getToolNames().get(0));
    }

    /**
     * Tests deserialization with requireApproval absent.
     */
    @Test
    public void testDeserializationWithoutRequireApproval() throws IOException {
        String json = "{\"server_label\":\"test-mcp-server\",\"type\":\"mcp\"}";

        McpTool tool = deserializeFromJson(json);

        assertNotNull(tool);
        assertNull(tool.getRequireApprovalAsString());
        assertNull(tool.getRequireApprovalAsMcpToolRequireApproval());
    }

    /**
     * Tests round-trip with requireApproval as "always" string.
     */
    @Test
    public void testRoundTripWithRequireApprovalAlwaysString() throws IOException {
        McpTool original = new McpTool(TEST_SERVER_LABEL).setRequireApproval("always");

        String json = serializeToJson(original);
        McpTool deserialized = deserializeFromJson(json);

        assertNotNull(deserialized);
        assertNotNull(deserialized.getRequireApprovalAsString());
        assertTrue(deserialized.getRequireApprovalAsString().contains("always"));
    }

    /**
     * Tests round-trip with requireApproval as McpToolRequireApproval.
     */
    @Test
    public void testRoundTripWithRequireApprovalAsMcpToolRequireApproval() throws IOException {
        McpToolFilter alwaysFilter = new McpToolFilter().setToolNames(Arrays.asList("tool_1"));
        McpToolRequireApproval approval = new McpToolRequireApproval().setAlways(alwaysFilter);

        McpTool original = new McpTool(TEST_SERVER_LABEL).setRequireApproval(approval);

        String json = serializeToJson(original);
        McpTool deserialized = deserializeFromJson(json);

        assertNotNull(deserialized);
        McpToolRequireApproval deserializedApproval = deserialized.getRequireApprovalAsMcpToolRequireApproval();
        assertNotNull(deserializedApproval);
        assertNotNull(deserializedApproval.getAlways());
        assertEquals(1, deserializedApproval.getAlways().getToolNames().size());
    }

    /**
     * Tests serialization with both allowedTools and requireApproval set.
     */
    @Test
    public void testSerializationWithBothAllowedToolsAndRequireApproval() throws IOException {
        McpTool tool = new McpTool(TEST_SERVER_LABEL).setServerUrl("https://mcp.example.com")
            .setAllowedTools(Arrays.asList("tool_1", "tool_2"))
            .setRequireApproval("always");

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertTrue(json.contains("\"server_label\":\"test-mcp-server\""));
        assertTrue(json.contains("\"server_url\":\"https://mcp.example.com\""));
        assertTrue(json.contains("\"allowed_tools\""));
        assertTrue(json.contains("\"require_approval\""));
    }

    // ===== Regression: no double-quoting =====

    /**
     * Regression: requireApproval string must not be double-quoted in serialized JSON.
     * Setting via setRequireApproval(String) must produce "require_approval":"always",
     * never "require_approval":"\"always\"".
     */
    @Test
    public void testRequireApprovalStringNoDoubleQuoting() throws IOException {
        McpTool tool = new McpTool(TEST_SERVER_LABEL).setRequireApproval("always");

        String json = serializeToJson(tool);

        assertTrue(json.contains("\"require_approval\":\"always\""),
            "requireApproval string must not be double-quoted, got: " + json);
        assertFalse(json.contains("\\\"always\\\""),
            "requireApproval string must not have escaped quotes, got: " + json);
    }

    /**
     * Regression: requireApproval "never" string must not be double-quoted in serialized JSON.
     */
    @Test
    public void testRequireApprovalNeverStringNoDoubleQuoting() throws IOException {
        McpTool tool = new McpTool(TEST_SERVER_LABEL).setRequireApproval("never");

        String json = serializeToJson(tool);

        assertTrue(json.contains("\"require_approval\":\"never\""),
            "requireApproval string must not be double-quoted, got: " + json);
    }

    /**
     * Regression: requireApproval string getter must return the plain string after round-trip,
     * not a JSON-encoded value with surrounding quotes.
     */
    @Test
    public void testRequireApprovalStringRoundTripNoExtraQuotes() throws IOException {
        McpTool original = new McpTool(TEST_SERVER_LABEL).setRequireApproval("always");

        String json = serializeToJson(original);
        McpTool deserialized = deserializeFromJson(json);

        assertEquals("always", deserialized.getRequireApprovalAsString(),
            "Round-tripped requireApproval string must not have extra quotes");
    }

    /**
     * Regression: requireApproval string deserialized from JSON must return the plain string,
     * not a JSON-encoded value with surrounding quotes.
     */
    @Test
    public void testRequireApprovalStringDeserializationNoExtraQuotes() throws IOException {
        String json = "{\"server_label\":\"test-mcp-server\",\"type\":\"mcp\",\"require_approval\":\"never\"}";

        McpTool tool = deserializeFromJson(json);

        assertEquals("never", tool.getRequireApprovalAsString(),
            "Deserialized requireApproval string must not have extra quotes");
    }

    // Helper method to serialize to JSON string
    private String serializeToJson(McpTool tool) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            tool.toJson(jsonWriter);
        }
        return outputStream.toString("UTF-8");
    }

    // Helper method to deserialize from JSON string
    private McpTool deserializeFromJson(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return McpTool.fromJson(jsonReader);
        }
    }
}
