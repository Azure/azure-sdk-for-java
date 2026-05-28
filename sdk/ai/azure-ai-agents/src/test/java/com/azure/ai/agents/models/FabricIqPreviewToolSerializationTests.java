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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for FabricIqPreviewTool serialization, focusing on the requireApproval union type handling.
 * requireApproval is a union type: String ("always"/"never") | McpToolRequireApproval.
 */
public class FabricIqPreviewToolSerializationTests {

    private static final String TEST_CONNECTION_ID = "test-connection-id";

    // ===== requireApproval tests =====

    /**
     * Tests serialization when requireApproval is not set (null).
     */
    @Test
    public void testSerializationWithoutRequireApproval() throws IOException {
        FabricIqPreviewTool tool = new FabricIqPreviewTool(TEST_CONNECTION_ID);

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertTrue(json.contains("\"project_connection_id\":\"test-connection-id\""));
        assertFalse(json.contains("\"require_approval\""));
    }

    /**
     * Tests serialization with requireApproval set to "always" string.
     */
    @Test
    public void testSerializationWithRequireApprovalAlwaysString() throws IOException {
        FabricIqPreviewTool tool = new FabricIqPreviewTool(TEST_CONNECTION_ID).setRequireApproval("always");

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertTrue(json.contains("\"require_approval\":\"always\""));
    }

    /**
     * Tests serialization with requireApproval set to "never" string.
     */
    @Test
    public void testSerializationWithRequireApprovalNeverString() throws IOException {
        FabricIqPreviewTool tool = new FabricIqPreviewTool(TEST_CONNECTION_ID).setRequireApproval("never");

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertTrue(json.contains("\"require_approval\":\"never\""));
    }

    /**
     * Tests serialization with requireApproval set to an McpToolRequireApproval filter.
     */
    @Test
    public void testSerializationWithRequireApprovalAsMcpToolRequireApproval() throws IOException {
        McpToolFilter alwaysFilter = new McpToolFilter().setToolNames(Arrays.asList("dangerous_tool"));
        McpToolFilter neverFilter = new McpToolFilter().setToolNames(Arrays.asList("safe_tool"));
        McpToolRequireApproval approval = new McpToolRequireApproval().setAlways(alwaysFilter).setNever(neverFilter);

        FabricIqPreviewTool tool = new FabricIqPreviewTool(TEST_CONNECTION_ID).setRequireApproval(approval);

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
        String json
            = "{\"project_connection_id\":\"test-connection-id\",\"type\":\"fabric_iq_preview\",\"require_approval\":\"always\"}";

        FabricIqPreviewTool tool = deserializeFromJson(json);

        assertNotNull(tool);
        assertEquals("always", tool.getRequireApprovalAsString());
    }

    /**
     * Tests deserialization with requireApproval set to "never" string.
     */
    @Test
    public void testDeserializationWithRequireApprovalNeverString() throws IOException {
        String json
            = "{\"project_connection_id\":\"test-connection-id\",\"type\":\"fabric_iq_preview\",\"require_approval\":\"never\"}";

        FabricIqPreviewTool tool = deserializeFromJson(json);

        assertNotNull(tool);
        assertEquals("never", tool.getRequireApprovalAsString());
    }

    /**
     * Tests deserialization with requireApproval set to an McpToolRequireApproval object.
     */
    @Test
    public void testDeserializationWithRequireApprovalAsMcpToolRequireApproval() throws IOException {
        String json
            = "{\"project_connection_id\":\"test-connection-id\",\"type\":\"fabric_iq_preview\",\"require_approval\":{\"always\":{\"tool_names\":[\"dangerous_tool\"]},\"never\":{\"tool_names\":[\"safe_tool\"]}}}";

        FabricIqPreviewTool tool = deserializeFromJson(json);

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
        String json = "{\"project_connection_id\":\"test-connection-id\",\"type\":\"fabric_iq_preview\"}";

        FabricIqPreviewTool tool = deserializeFromJson(json);

        assertNotNull(tool);
        assertNull(tool.getRequireApprovalAsString());
        assertNull(tool.getRequireApprovalAsMcpToolRequireApproval());
    }

    /**
     * Tests round-trip with requireApproval as "always" string.
     */
    @Test
    public void testRoundTripWithRequireApprovalAlwaysString() throws IOException {
        FabricIqPreviewTool original = new FabricIqPreviewTool(TEST_CONNECTION_ID).setRequireApproval("always");

        String json = serializeToJson(original);
        FabricIqPreviewTool deserialized = deserializeFromJson(json);

        assertNotNull(deserialized);
        assertEquals("always", deserialized.getRequireApprovalAsString());
    }

    /**
     * Tests round-trip with requireApproval as "never" string.
     */
    @Test
    public void testRoundTripWithRequireApprovalNeverString() throws IOException {
        FabricIqPreviewTool original = new FabricIqPreviewTool(TEST_CONNECTION_ID).setRequireApproval("never");

        String json = serializeToJson(original);
        FabricIqPreviewTool deserialized = deserializeFromJson(json);

        assertNotNull(deserialized);
        assertEquals("never", deserialized.getRequireApprovalAsString());
    }

    /**
     * Tests round-trip with requireApproval as McpToolRequireApproval.
     */
    @Test
    public void testRoundTripWithRequireApprovalAsMcpToolRequireApproval() throws IOException {
        McpToolFilter alwaysFilter = new McpToolFilter().setToolNames(Arrays.asList("tool_1"));
        McpToolRequireApproval approval = new McpToolRequireApproval().setAlways(alwaysFilter);

        FabricIqPreviewTool original = new FabricIqPreviewTool(TEST_CONNECTION_ID).setRequireApproval(approval);

        String json = serializeToJson(original);
        FabricIqPreviewTool deserialized = deserializeFromJson(json);

        assertNotNull(deserialized);
        McpToolRequireApproval deserializedApproval = deserialized.getRequireApprovalAsMcpToolRequireApproval();
        assertNotNull(deserializedApproval);
        assertNotNull(deserializedApproval.getAlways());
        assertEquals(1, deserializedApproval.getAlways().getToolNames().size());
        assertEquals("tool_1", deserializedApproval.getAlways().getToolNames().get(0));
    }

    // ===== Regression: no double-quoting =====

    /**
     * Regression: requireApproval string must not be double-quoted in serialized JSON.
     */
    @Test
    public void testRequireApprovalStringNoDoubleQuoting() throws IOException {
        FabricIqPreviewTool tool = new FabricIqPreviewTool(TEST_CONNECTION_ID).setRequireApproval("always");

        String json = serializeToJson(tool);

        assertTrue(json.contains("\"require_approval\":\"always\""),
            "requireApproval string must not be double-quoted, got: " + json);
        assertFalse(json.contains("\\\"always\\\""),
            "requireApproval string must not have escaped quotes, got: " + json);
    }

    /**
     * Regression: requireApproval string getter must return the plain string after round-trip.
     */
    @Test
    public void testRequireApprovalStringRoundTripNoExtraQuotes() throws IOException {
        FabricIqPreviewTool original = new FabricIqPreviewTool(TEST_CONNECTION_ID).setRequireApproval("always");

        String json = serializeToJson(original);
        FabricIqPreviewTool deserialized = deserializeFromJson(json);

        assertEquals("always", deserialized.getRequireApprovalAsString(),
            "Round-tripped requireApproval string must not have extra quotes");
    }

    /**
     * Tests that other properties serialize correctly alongside requireApproval.
     */
    @Test
    public void testSerializationWithAllProperties() throws IOException {
        FabricIqPreviewTool tool = new FabricIqPreviewTool(TEST_CONNECTION_ID).setServerLabel("my-server")
            .setServerUrl("https://fabriciq.example.com")
            .setRequireApproval("always")
            .setName("my-fabric-tool")
            .setDescription("A test FabricIQ tool");

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertTrue(json.contains("\"project_connection_id\":\"test-connection-id\""));
        assertTrue(json.contains("\"server_label\":\"my-server\""));
        assertTrue(json.contains("\"server_url\":\"https://fabriciq.example.com\""));
        assertTrue(json.contains("\"require_approval\":\"always\""));
        assertTrue(json.contains("\"name\":\"my-fabric-tool\""));
        assertTrue(json.contains("\"description\":\"A test FabricIQ tool\""));
        assertTrue(json.contains("\"type\":\"fabric_iq_preview\""));
    }

    // Helper method to serialize to JSON string
    private String serializeToJson(FabricIqPreviewTool tool) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            tool.toJson(jsonWriter);
        }
        return outputStream.toString("UTF-8");
    }

    // Helper method to deserialize from JSON string
    private FabricIqPreviewTool deserializeFromJson(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return FabricIqPreviewTool.fromJson(jsonReader);
        }
    }
}
