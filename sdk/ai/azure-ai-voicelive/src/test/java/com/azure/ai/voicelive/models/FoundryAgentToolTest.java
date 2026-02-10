// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link FoundryAgentTool} and related classes.
 */
class FoundryAgentToolTest {

    @Test
    void testFoundryAgentToolCreation() {
        // Arrange & Act
        FoundryAgentTool tool = new FoundryAgentTool("test-agent", "test-project");

        // Assert
        assertNotNull(tool);
        assertEquals("test-agent", tool.getAgentName());
        assertEquals("test-project", tool.getProjectName());
        assertEquals(ToolType.FOUNDRY_AGENT, tool.getType());
    }

    @Test
    void testFoundryAgentToolWithAllProperties() {
        // Arrange & Act
        FoundryAgentTool tool = new FoundryAgentTool("my-agent", "my-project").setAgentVersion("1.0.0")
            .setClientId("client-123")
            .setDescription("A test agent tool")
            .setFoundryResourceOverride("custom-resource")
            .setAgentContextType(FoundryAgentContextType.AGENT_CONTEXT)
            .setReturnAgentResponseDirectly(true);

        // Assert
        assertEquals("my-agent", tool.getAgentName());
        assertEquals("my-project", tool.getProjectName());
        assertEquals("1.0.0", tool.getAgentVersion());
        assertEquals("client-123", tool.getClientId());
        assertEquals("A test agent tool", tool.getDescription());
        assertEquals("custom-resource", tool.getFoundryResourceOverride());
        assertEquals(FoundryAgentContextType.AGENT_CONTEXT, tool.getAgentContextType());
        assertTrue(tool.isReturnAgentResponseDirectly());
    }

    @Test
    void testFoundryAgentContextTypeValues() {
        // Assert all known values exist
        assertNotNull(FoundryAgentContextType.NO_CONTEXT);
        assertNotNull(FoundryAgentContextType.AGENT_CONTEXT);

        assertEquals("no_context", FoundryAgentContextType.NO_CONTEXT.toString());
        assertEquals("agent_context", FoundryAgentContextType.AGENT_CONTEXT.toString());
    }

    @Test
    void testFoundryAgentContextTypeFromString() {
        // Act & Assert
        assertEquals(FoundryAgentContextType.NO_CONTEXT, FoundryAgentContextType.fromString("no_context"));
        assertEquals(FoundryAgentContextType.AGENT_CONTEXT, FoundryAgentContextType.fromString("agent_context"));
    }

    @Test
    void testFoundryAgentToolJsonSerialization() {
        // Arrange
        FoundryAgentTool tool = new FoundryAgentTool("agent-1", "project-1").setDescription("Test description")
            .setAgentContextType(FoundryAgentContextType.NO_CONTEXT);

        // Act
        BinaryData serialized = BinaryData.fromObject(tool);
        FoundryAgentTool deserialized = serialized.toObject(FoundryAgentTool.class);

        // Assert
        assertEquals(tool.getAgentName(), deserialized.getAgentName());
        assertEquals(tool.getProjectName(), deserialized.getProjectName());
        assertEquals(tool.getDescription(), deserialized.getDescription());
        assertEquals(tool.getAgentContextType(), deserialized.getAgentContextType());
        assertEquals(ToolType.FOUNDRY_AGENT, deserialized.getType());
    }

    @Test
    void testFoundryAgentToolJsonDeserialization() {
        // Arrange
        String json = "{\"type\":\"foundry_agent\",\"agent_name\":\"my-agent\",\"project_name\":\"my-project\","
            + "\"agent_version\":\"2.0\",\"client_id\":\"cid\",\"description\":\"desc\","
            + "\"agent_context_type\":\"agent_context\",\"return_agent_response_directly\":false}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        FoundryAgentTool tool = data.toObject(FoundryAgentTool.class);

        // Assert
        assertNotNull(tool);
        assertEquals("my-agent", tool.getAgentName());
        assertEquals("my-project", tool.getProjectName());
        assertEquals("2.0", tool.getAgentVersion());
        assertEquals("cid", tool.getClientId());
        assertEquals("desc", tool.getDescription());
        assertEquals(FoundryAgentContextType.AGENT_CONTEXT, tool.getAgentContextType());
        assertEquals(false, tool.isReturnAgentResponseDirectly());
    }

    @Test
    void testToolTypeFoundryAgent() {
        // Assert
        assertEquals("foundry_agent", ToolType.FOUNDRY_AGENT.toString());
        assertEquals(ToolType.FOUNDRY_AGENT, ToolType.fromString("foundry_agent"));
    }
}
