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
 * Tests for WebIqPreviewTool serialization, focusing on the requireApproval union type handling.
 * requireApproval is a union type: String ("always"/"never") | McpToolRequireApproval.
 */
public class WebIqPreviewToolSerializationTests {

    private static final String TEST_CONNECTION_ID = "test-connection-id";

    private static WebIqPreviewTool newTool() {
        return new WebIqPreviewTool(TEST_CONNECTION_ID);
    }

    @Test
    public void testSerializationWithoutRequireApproval() throws IOException {
        String json = serializeToJson(newTool());

        assertNotNull(json);
        assertTrue(json.contains("\"project_connection_id\":\"test-connection-id\""));
        assertFalse(json.contains("\"require_approval\""));
    }

    @Test
    public void testSerializationWithRequireApprovalAlwaysString() throws IOException {
        String json = serializeToJson(newTool().setRequireApproval("always"));

        assertTrue(json.contains("\"require_approval\":\"always\""), "Unexpected JSON: " + json);
    }

    @Test
    public void testSerializationWithRequireApprovalNeverString() throws IOException {
        String json = serializeToJson(newTool().setRequireApproval("never"));

        assertTrue(json.contains("\"require_approval\":\"never\""), "Unexpected JSON: " + json);
    }

    @Test
    public void testSerializationWithRequireApprovalAsMcpToolRequireApproval() throws IOException {
        McpToolRequireApproval approval
            = new McpToolRequireApproval().setAlways(new McpToolFilter().setToolNames(Arrays.asList("browse")));

        String json = serializeToJson(newTool().setRequireApproval(approval));

        assertTrue(json.contains("\"require_approval\""));
        assertTrue(json.contains("\"always\""), "Unexpected JSON: " + json);
        assertTrue(json.contains("\"browse\""), "Unexpected JSON: " + json);
    }

    @Test
    public void testDeserializationWithRequireApprovalAsString() throws IOException {
        String json = "{\"type\":\"web_iq_preview\",\"project_connection_id\":\"test-connection-id\","
            + "\"require_approval\":\"never\"}";

        WebIqPreviewTool tool = deserializeFromJson(json);

        assertEquals("never", tool.getRequireApprovalAsString());
    }

    @Test
    public void testDeserializationWithRequireApprovalAsMcpToolRequireApproval() throws IOException {
        String json = "{\"type\":\"web_iq_preview\",\"project_connection_id\":\"test-connection-id\","
            + "\"require_approval\":{\"always\":{\"tool_names\":[\"browse\"]}}}";

        WebIqPreviewTool tool = deserializeFromJson(json);

        McpToolRequireApproval approval = tool.getRequireApprovalAsMcpToolRequireApproval();
        assertNotNull(approval);
        assertNotNull(approval.getAlways());
        assertEquals(Arrays.asList("browse"), approval.getAlways().getToolNames());
    }

    @Test
    public void testDeserializationWithoutRequireApproval() throws IOException {
        String json = "{\"type\":\"web_iq_preview\",\"project_connection_id\":\"test-connection-id\"}";

        WebIqPreviewTool tool = deserializeFromJson(json);

        assertNull(tool.getRequireApprovalAsString());
        assertNull(tool.getRequireApprovalAsMcpToolRequireApproval());
    }

    @Test
    public void testRoundTripWithRequireApprovalAsString() throws IOException {
        WebIqPreviewTool original = newTool().setServerLabel("web-iq").setRequireApproval("always");

        WebIqPreviewTool deserialized = deserializeFromJson(serializeToJson(original));

        assertEquals("web-iq", deserialized.getServerLabel());
        assertEquals("always", deserialized.getRequireApprovalAsString());
    }

    @Test
    public void testRoundTripWithRequireApprovalAsMcpToolRequireApproval() throws IOException {
        McpToolRequireApproval approval
            = new McpToolRequireApproval().setNever(new McpToolFilter().setToolNames(Arrays.asList("search")));
        WebIqPreviewTool original = newTool().setRequireApproval(approval);

        WebIqPreviewTool deserialized = deserializeFromJson(serializeToJson(original));

        McpToolRequireApproval deserializedApproval = deserialized.getRequireApprovalAsMcpToolRequireApproval();
        assertNotNull(deserializedApproval);
        assertNotNull(deserializedApproval.getNever());
        assertEquals(Arrays.asList("search"), deserializedApproval.getNever().getToolNames());
    }

    @Test
    public void testDirectSetGetRequireApprovalAsString() {
        assertEquals("always", newTool().setRequireApproval("always").getRequireApprovalAsString());
    }

    @Test
    public void testDirectSetGetRequireApprovalAsMcpToolRequireApproval() {
        McpToolRequireApproval approval
            = new McpToolRequireApproval().setAlways(new McpToolFilter().setToolNames(Arrays.asList("browse")));

        McpToolRequireApproval roundTripped
            = newTool().setRequireApproval(approval).getRequireApprovalAsMcpToolRequireApproval();

        assertNotNull(roundTripped);
        assertNotNull(roundTripped.getAlways());
    }

    @Test
    public void testRequireApprovalCrossVariantReturnsNull() {
        assertNull(newTool().setRequireApproval("always").getRequireApprovalAsMcpToolRequireApproval());
        assertNull(newTool().setRequireApproval(new McpToolRequireApproval()).getRequireApprovalAsString());
    }

    @Test
    public void testRequireApprovalCrossVariantReturnsNullAfterDeserialization() throws IOException {
        String json = "{\"type\":\"web_iq_preview\",\"project_connection_id\":\"test-connection-id\","
            + "\"require_approval\":{\"always\":{\"tool_names\":[\"browse\"]}}}";

        assertNull(deserializeFromJson(json).getRequireApprovalAsString());
    }

    @Test
    public void testNullArgumentClearsRequireApproval() throws IOException {
        WebIqPreviewTool fromString = newTool().setRequireApproval("always").setRequireApproval((String) null);
        assertNull(fromString.getRequireApprovalAsString());
        assertNull(fromString.getRequireApprovalAsMcpToolRequireApproval());
        assertFalse(serializeToJson(fromString).contains("\"require_approval\""));

        WebIqPreviewTool fromModel = newTool().setRequireApproval(new McpToolRequireApproval())
            .setRequireApproval((McpToolRequireApproval) null);
        assertNull(fromModel.getRequireApprovalAsMcpToolRequireApproval());
        assertNull(fromModel.getRequireApprovalAsString());
        assertFalse(serializeToJson(fromModel).contains("\"require_approval\""));
    }

    private String serializeToJson(WebIqPreviewTool tool) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            tool.toJson(jsonWriter);
        }
        return outputStream.toString("UTF-8");
    }

    private WebIqPreviewTool deserializeFromJson(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return WebIqPreviewTool.fromJson(jsonReader);
        }
    }
}
