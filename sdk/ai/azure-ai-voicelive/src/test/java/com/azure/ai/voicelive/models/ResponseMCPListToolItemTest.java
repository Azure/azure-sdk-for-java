// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ResponseMCPListToolItem}.
 */
class ResponseMCPListToolItemTest {

    @Test
    void testFromJsonWithEmptyTools() throws IOException {
        // Arrange
        String json = "{" + "\"id\":\"list-1\"," + "\"object\":\"session.item\"," + "\"type\":\"mcp_list_tools\","
            + "\"tools\":[]," + "\"server_label\":\"test-server\"" + "}";

        // Act
        ResponseMCPListToolItem item = BinaryData.fromString(json).toObject(ResponseMCPListToolItem.class);

        // Assert
        assertNotNull(item);
        assertEquals("list-1", item.getId());
        assertEquals(ItemType.MCP_LIST_TOOLS, item.getType());
        assertEquals("test-server", item.getServerLabel());
        assertNotNull(item.getTools());
        assertEquals(0, item.getTools().size());
    }

    @Test
    void testFromJsonWithMultipleTools() throws IOException {
        // Arrange
        String json = "{" + "\"id\":\"list-2\"," + "\"object\":\"session.item\"," + "\"type\":\"mcp_list_tools\","
            + "\"server_label\":\"production-server\"," + "\"tools\":[" + "  {" + "    \"name\":\"get_weather\","
            + "    \"description\":\"Get weather information\"," + "    \"input_schema\":{\"type\":\"object\"}" + "  },"
            + "  {" + "    \"name\":\"send_email\"," + "    \"description\":\"Send an email\","
            + "    \"input_schema\":{\"type\":\"object\"}" + "  }" + "]" + "}";

        // Act
        ResponseMCPListToolItem item = BinaryData.fromString(json).toObject(ResponseMCPListToolItem.class);

        // Assert
        assertNotNull(item);
        assertEquals("list-2", item.getId());
        assertEquals("production-server", item.getServerLabel());
        assertNotNull(item.getTools());
        assertEquals(2, item.getTools().size());

        List<MCPTool> tools = item.getTools();
        assertEquals("get_weather", tools.get(0).getName());
        assertEquals("Get weather information", tools.get(0).getDescription());
        assertEquals("send_email", tools.get(1).getName());
        assertEquals("Send an email", tools.get(1).getDescription());
    }

    @Test
    void testTypeIsAlwaysMcpListTools() throws IOException {
        // Arrange
        String json = "{" + "\"id\":\"list-3\"," + "\"object\":\"session.item\"," + "\"type\":\"mcp_list_tools\","
            + "\"tools\":[]," + "\"server_label\":\"test\"" + "}";

        // Act
        ResponseMCPListToolItem item = BinaryData.fromString(json).toObject(ResponseMCPListToolItem.class);

        // Assert
        assertEquals(ItemType.MCP_LIST_TOOLS, item.getType());
    }

    @Test
    void testJsonRoundTrip() throws IOException {
        // Arrange
        String originalJson = "{" + "\"id\":\"round-trip-list\"," + "\"object\":\"session.item\","
            + "\"type\":\"mcp_list_tools\"," + "\"server_label\":\"my-server\"," + "\"tools\":[" + "  {"
            + "    \"name\":\"calculator\"," + "    \"description\":\"Perform calculations\","
            + "    \"input_schema\":{\"type\":\"object\",\"properties\":{\"expression\":{\"type\":\"string\"}}}" + "  }"
            + "]" + "}";

        // Act
        ResponseMCPListToolItem item = BinaryData.fromString(originalJson).toObject(ResponseMCPListToolItem.class);
        String serializedJson = BinaryData.fromObject(item).toString();

        // Assert
        assertNotNull(serializedJson);
        ResponseMCPListToolItem deserializedItem
            = BinaryData.fromString(serializedJson).toObject(ResponseMCPListToolItem.class);
        assertEquals(item.getId(), deserializedItem.getId());
        assertEquals(item.getServerLabel(), deserializedItem.getServerLabel());
        assertEquals(item.getTools().size(), deserializedItem.getTools().size());
    }

    @Test
    void testToolsWithComplexSchema() throws IOException {
        // Arrange
        String json = "{" + "\"id\":\"complex-tools\"," + "\"object\":\"session.item\","
            + "\"type\":\"mcp_list_tools\"," + "\"server_label\":\"api-server\"," + "\"tools\":[" + "  {"
            + "    \"name\":\"search\"," + "    \"description\":\"Search for items\"," + "    \"input_schema\":{"
            + "      \"type\":\"object\"," + "      \"properties\":{" + "        \"query\":{\"type\":\"string\"},"
            + "        \"limit\":{\"type\":\"integer\"},"
            + "        \"filters\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}}" + "      },"
            + "      \"required\":[\"query\"]" + "    }" + "  }" + "]" + "}";

        // Act
        ResponseMCPListToolItem item = BinaryData.fromString(json).toObject(ResponseMCPListToolItem.class);

        // Assert
        assertNotNull(item);
        assertEquals(1, item.getTools().size());
        MCPTool tool = item.getTools().get(0);
        assertEquals("search", tool.getName());
        assertEquals("Search for items", tool.getDescription());
        assertNotNull(tool.getInputSchema());
    }

    @Test
    void testToolsWithAnnotations() throws IOException {
        // Arrange
        String json
            = "{" + "\"id\":\"annotated-tools\"," + "\"object\":\"session.item\"," + "\"type\":\"mcp_list_tools\","
                + "\"server_label\":\"annotated-server\"," + "\"tools\":[" + "  {" + "    \"name\":\"annotated_tool\","
                + "    \"description\":\"Tool with annotations\"," + "    \"input_schema\":{\"type\":\"object\"},"
                + "    \"annotations\":{\"version\":\"1.0\",\"author\":\"test\"}" + "  }" + "]" + "}";

        // Act
        ResponseMCPListToolItem item = BinaryData.fromString(json).toObject(ResponseMCPListToolItem.class);

        // Assert
        assertNotNull(item);
        assertEquals(1, item.getTools().size());
        MCPTool tool = item.getTools().get(0);
        assertEquals("annotated_tool", tool.getName());
        assertNotNull(tool.getAnnotations());
    }

    @Test
    void testMultipleToolsFromSameServer() throws IOException {
        // Arrange
        String json = "{" + "\"id\":\"many-tools\"," + "\"object\":\"session.item\"," + "\"type\":\"mcp_list_tools\","
            + "\"server_label\":\"utility-server\"," + "\"tools\":["
            + "  {\"name\":\"tool1\",\"input_schema\":{\"type\":\"object\"}},"
            + "  {\"name\":\"tool2\",\"input_schema\":{\"type\":\"object\"}},"
            + "  {\"name\":\"tool3\",\"input_schema\":{\"type\":\"object\"}},"
            + "  {\"name\":\"tool4\",\"input_schema\":{\"type\":\"object\"}},"
            + "  {\"name\":\"tool5\",\"input_schema\":{\"type\":\"object\"}}" + "]" + "}";

        // Act
        ResponseMCPListToolItem item = BinaryData.fromString(json).toObject(ResponseMCPListToolItem.class);

        // Assert
        assertEquals(5, item.getTools().size());
        assertEquals("utility-server", item.getServerLabel());
        assertTrue(item.getTools().stream().anyMatch(t -> t.getName().equals("tool1")));
        assertTrue(item.getTools().stream().anyMatch(t -> t.getName().equals("tool5")));
    }
}
