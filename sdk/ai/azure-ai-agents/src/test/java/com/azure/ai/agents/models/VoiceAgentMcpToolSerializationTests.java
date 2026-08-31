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
 * Tests for VoiceAgentMcpTool serialization, focusing on the union typed properties.
 * allowedTools is a union type: List&lt;String&gt; | McpToolFilter.
 * requireApproval is a union type: String ("always"/"never") | McpToolRequireApproval.
 */
public class VoiceAgentMcpToolSerializationTests {

    private static final String TEST_SERVER_LABEL = "test-server";

    private static VoiceAgentMcpTool newTool() {
        return new VoiceAgentMcpTool(TEST_SERVER_LABEL);
    }

    // ===== allowedTools tests =====

    @Test
    public void testSerializationWithoutAllowedTools() throws IOException {
        String json = serializeToJson(newTool());

        assertNotNull(json);
        assertTrue(json.contains("\"server_label\":\"test-server\""));
        assertFalse(json.contains("\"allowed_tools\""));
    }

    @Test
    public void testSerializationWithAllowedToolsAsStringList() throws IOException {
        String json = serializeToJson(newTool().setAllowedTools(Arrays.asList("tool_a", "tool_b")));

        assertTrue(json.contains("\"allowed_tools\":[\"tool_a\",\"tool_b\"]"), "Unexpected JSON: " + json);
    }

    @Test
    public void testSerializationWithAllowedToolsAsMcpToolFilter() throws IOException {
        McpToolFilter filter = new McpToolFilter().setToolNames(Arrays.asList("tool_a")).setReadOnly(true);

        String json = serializeToJson(newTool().setAllowedTools(filter));

        assertTrue(json.contains("\"allowed_tools\""));
        assertTrue(json.contains("\"tool_names\":[\"tool_a\"]"), "Unexpected JSON: " + json);
        assertTrue(json.contains("\"read_only\":true"), "Unexpected JSON: " + json);
    }

    @Test
    public void testDeserializationWithAllowedToolsAsStringList() throws IOException {
        String json
            = "{\"type\":\"mcp\",\"server_label\":\"test-server\"," + "\"allowed_tools\":[\"tool_a\",\"tool_b\"]}";

        VoiceAgentMcpTool tool = deserializeFromJson(json);

        List<String> allowedTools = tool.getAllowedToolsAsStringList();
        assertNotNull(allowedTools);
        assertEquals(2, allowedTools.size());
        assertEquals("tool_a", allowedTools.get(0));
    }

    @Test
    public void testDeserializationWithAllowedToolsAsMcpToolFilter() throws IOException {
        String json = "{\"type\":\"mcp\",\"server_label\":\"test-server\","
            + "\"allowed_tools\":{\"tool_names\":[\"tool_a\"],\"read_only\":true}}";

        VoiceAgentMcpTool tool = deserializeFromJson(json);

        McpToolFilter filter = tool.getAllowedToolsAsMcpToolFilter();
        assertNotNull(filter);
        assertEquals(Arrays.asList("tool_a"), filter.getToolNames());
        assertEquals(Boolean.TRUE, filter.isReadOnly());
    }

    @Test
    public void testDeserializationWithoutAllowedTools() throws IOException {
        String json = "{\"type\":\"mcp\",\"server_label\":\"test-server\"}";

        VoiceAgentMcpTool tool = deserializeFromJson(json);

        assertNull(tool.getAllowedToolsAsStringList());
        assertNull(tool.getAllowedToolsAsMcpToolFilter());
    }

    @Test
    public void testRoundTripWithAllowedToolsAsStringList() throws IOException {
        VoiceAgentMcpTool original = newTool().setAllowedTools(Arrays.asList("tool_a", "tool_b"));

        VoiceAgentMcpTool deserialized = deserializeFromJson(serializeToJson(original));

        assertEquals(Arrays.asList("tool_a", "tool_b"), deserialized.getAllowedToolsAsStringList());
    }

    @Test
    public void testRoundTripWithAllowedToolsAsMcpToolFilter() throws IOException {
        McpToolFilter filter = new McpToolFilter().setToolNames(Arrays.asList("tool_a"));
        VoiceAgentMcpTool original = newTool().setAllowedTools(filter);

        VoiceAgentMcpTool deserialized = deserializeFromJson(serializeToJson(original));

        McpToolFilter deserializedFilter = deserialized.getAllowedToolsAsMcpToolFilter();
        assertNotNull(deserializedFilter);
        assertEquals(Arrays.asList("tool_a"), deserializedFilter.getToolNames());
    }

    // ===== requireApproval tests =====

    @Test
    public void testSerializationWithoutRequireApproval() throws IOException {
        String json = serializeToJson(newTool());

        assertFalse(json.contains("\"require_approval\""));
    }

    @Test
    public void testSerializationWithRequireApprovalAsString() throws IOException {
        String json = serializeToJson(newTool().setRequireApproval("never"));

        assertTrue(json.contains("\"require_approval\":\"never\""), "Unexpected JSON: " + json);
    }

    @Test
    public void testSerializationWithRequireApprovalAsMcpToolRequireApproval() throws IOException {
        McpToolFilter alwaysFilter = new McpToolFilter().setToolNames(Arrays.asList("dangerous_tool"));
        McpToolRequireApproval approval = new McpToolRequireApproval().setAlways(alwaysFilter);

        String json = serializeToJson(newTool().setRequireApproval(approval));

        assertTrue(json.contains("\"require_approval\""));
        assertTrue(json.contains("\"always\""), "Unexpected JSON: " + json);
        assertTrue(json.contains("\"dangerous_tool\""), "Unexpected JSON: " + json);
    }

    @Test
    public void testDeserializationWithRequireApprovalAsString() throws IOException {
        String json = "{\"type\":\"mcp\",\"server_label\":\"test-server\",\"require_approval\":\"always\"}";

        VoiceAgentMcpTool tool = deserializeFromJson(json);

        assertEquals("always", tool.getRequireApprovalAsString());
    }

    @Test
    public void testDeserializationWithRequireApprovalAsMcpToolRequireApproval() throws IOException {
        String json = "{\"type\":\"mcp\",\"server_label\":\"test-server\","
            + "\"require_approval\":{\"always\":{\"tool_names\":[\"dangerous_tool\"]}}}";

        VoiceAgentMcpTool tool = deserializeFromJson(json);

        McpToolRequireApproval approval = tool.getRequireApprovalAsMcpToolRequireApproval();
        assertNotNull(approval);
        assertNotNull(approval.getAlways());
        assertEquals(Arrays.asList("dangerous_tool"), approval.getAlways().getToolNames());
    }

    @Test
    public void testDeserializationWithoutRequireApproval() throws IOException {
        String json = "{\"type\":\"mcp\",\"server_label\":\"test-server\"}";

        VoiceAgentMcpTool tool = deserializeFromJson(json);

        assertNull(tool.getRequireApprovalAsString());
        assertNull(tool.getRequireApprovalAsMcpToolRequireApproval());
    }

    @Test
    public void testRoundTripWithRequireApprovalAsString() throws IOException {
        VoiceAgentMcpTool original = newTool().setRequireApproval("always");

        VoiceAgentMcpTool deserialized = deserializeFromJson(serializeToJson(original));

        assertEquals("always", deserialized.getRequireApprovalAsString());
    }

    @Test
    public void testRoundTripWithRequireApprovalAsMcpToolRequireApproval() throws IOException {
        McpToolRequireApproval approval
            = new McpToolRequireApproval().setNever(new McpToolFilter().setToolNames(Arrays.asList("safe_tool")));
        VoiceAgentMcpTool original = newTool().setRequireApproval(approval);

        VoiceAgentMcpTool deserialized = deserializeFromJson(serializeToJson(original));

        McpToolRequireApproval deserializedApproval = deserialized.getRequireApprovalAsMcpToolRequireApproval();
        assertNotNull(deserializedApproval);
        assertNotNull(deserializedApproval.getNever());
        assertEquals(Arrays.asList("safe_tool"), deserializedApproval.getNever().getToolNames());
    }

    @Test
    public void testRoundTripWithAllowedToolsAndRequireApproval() throws IOException {
        VoiceAgentMcpTool original = newTool().setAllowedTools(Arrays.asList("tool_a")).setRequireApproval("always");

        VoiceAgentMcpTool deserialized = deserializeFromJson(serializeToJson(original));

        assertEquals(Arrays.asList("tool_a"), deserialized.getAllowedToolsAsStringList());
        assertEquals("always", deserialized.getRequireApprovalAsString());
    }

    // ===== direct set -> get (same instance, no serialization round trip) =====

    @Test
    public void testDirectSetGetAllowedToolsAsStringList() {
        VoiceAgentMcpTool tool = newTool().setAllowedTools(Arrays.asList("tool_a", "tool_b"));

        List<String> allowedTools = tool.getAllowedToolsAsStringList();
        assertEquals(Arrays.asList("tool_a", "tool_b"), allowedTools);
        for (Object element : allowedTools) {
            assertTrue(element instanceof String, "Expected String elements but got: " + element.getClass());
        }
    }

    @Test
    public void testDirectSetGetAllowedToolsAsMcpToolFilter() {
        McpToolFilter filter = new McpToolFilter().setToolNames(Arrays.asList("tool_a")).setReadOnly(true);

        McpToolFilter roundTripped = newTool().setAllowedTools(filter).getAllowedToolsAsMcpToolFilter();

        assertNotNull(roundTripped);
        assertEquals(Arrays.asList("tool_a"), roundTripped.getToolNames());
    }

    @Test
    public void testDirectSetGetRequireApprovalAsString() {
        assertEquals("never", newTool().setRequireApproval("never").getRequireApprovalAsString());
    }

    @Test
    public void testDirectSetGetRequireApprovalAsMcpToolRequireApproval() {
        McpToolRequireApproval approval
            = new McpToolRequireApproval().setNever(new McpToolFilter().setToolNames(Arrays.asList("safe_tool")));

        McpToolRequireApproval roundTripped
            = newTool().setRequireApproval(approval).getRequireApprovalAsMcpToolRequireApproval();

        assertNotNull(roundTripped);
        assertNotNull(roundTripped.getNever());
    }

    // ===== cross-variant reads return null instead of throwing =====

    @Test
    public void testAllowedToolsCrossVariantReturnsNull() {
        VoiceAgentMcpTool listTool = newTool().setAllowedTools(Arrays.asList("tool_a"));
        assertNull(listTool.getAllowedToolsAsMcpToolFilter());

        VoiceAgentMcpTool filterTool = newTool().setAllowedTools(new McpToolFilter().setReadOnly(true));
        assertNull(filterTool.getAllowedToolsAsStringList());
    }

    @Test
    public void testAllowedToolsCrossVariantReturnsNullAfterDeserialization() throws IOException {
        String json = "{\"type\":\"mcp\",\"server_label\":\"test-server\","
            + "\"allowed_tools\":{\"tool_names\":[\"tool_a\"],\"read_only\":true}}";

        VoiceAgentMcpTool tool = deserializeFromJson(json);

        assertNull(tool.getAllowedToolsAsStringList());
        assertNotNull(tool.getAllowedToolsAsMcpToolFilter());
    }

    @Test
    public void testRequireApprovalCrossVariantReturnsNull() {
        VoiceAgentMcpTool stringTool = newTool().setRequireApproval("always");
        assertNull(stringTool.getRequireApprovalAsMcpToolRequireApproval());

        VoiceAgentMcpTool modelTool = newTool().setRequireApproval(new McpToolRequireApproval());
        assertNull(modelTool.getRequireApprovalAsString());
    }

    @Test
    public void testNullArgumentClearsAllowedTools() throws IOException {
        VoiceAgentMcpTool fromList
            = newTool().setAllowedTools(Arrays.asList("tool_a")).setAllowedTools((List<String>) null);
        assertNull(fromList.getAllowedToolsAsStringList());
        assertNull(fromList.getAllowedToolsAsMcpToolFilter());
        assertFalse(serializeToJson(fromList).contains("\"allowed_tools\""));

        VoiceAgentMcpTool fromFilter
            = newTool().setAllowedTools(new McpToolFilter()).setAllowedTools((McpToolFilter) null);
        assertNull(fromFilter.getAllowedToolsAsMcpToolFilter());
        assertNull(fromFilter.getAllowedToolsAsStringList());
        assertFalse(serializeToJson(fromFilter).contains("\"allowed_tools\""));
    }

    @Test
    public void testNullArgumentClearsRequireApproval() throws IOException {
        VoiceAgentMcpTool fromString = newTool().setRequireApproval("always").setRequireApproval((String) null);
        assertNull(fromString.getRequireApprovalAsString());
        assertNull(fromString.getRequireApprovalAsMcpToolRequireApproval());
        assertFalse(serializeToJson(fromString).contains("\"require_approval\""));

        VoiceAgentMcpTool fromModel = newTool().setRequireApproval(new McpToolRequireApproval())
            .setRequireApproval((McpToolRequireApproval) null);
        assertNull(fromModel.getRequireApprovalAsMcpToolRequireApproval());
        assertNull(fromModel.getRequireApprovalAsString());
        assertFalse(serializeToJson(fromModel).contains("\"require_approval\""));
    }

    private String serializeToJson(VoiceAgentMcpTool tool) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            tool.toJson(jsonWriter);
        }
        return outputStream.toString("UTF-8");
    }

    private VoiceAgentMcpTool deserializeFromJson(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return VoiceAgentMcpTool.fromJson(jsonReader);
        }
    }
}
