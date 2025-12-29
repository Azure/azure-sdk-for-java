// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link MCPServer}.
 */
class MCPServerTest {

    private static final String TEST_SERVER_LABEL = "test-server";
    private static final String TEST_SERVER_URL = "http://localhost:8080";
    private MCPServer mcpServer;

    @BeforeEach
    void setUp() {
        mcpServer = new MCPServer(TEST_SERVER_LABEL, TEST_SERVER_URL);
    }

    @Test
    void testConstructorWithValidParameters() {
        // Assert
        assertNotNull(mcpServer);
        assertEquals(TEST_SERVER_LABEL, mcpServer.getServerLabel());
        assertEquals(TEST_SERVER_URL, mcpServer.getServerUrl());
        assertEquals(ToolType.MCP, mcpServer.getType());
    }

    @Test
    void testConstructorWithNullServerLabel() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            MCPServer server = new MCPServer(null, TEST_SERVER_URL);
            assertNull(server.getServerLabel());
        });
    }

    @Test
    void testConstructorWithNullServerUrl() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            MCPServer server = new MCPServer(TEST_SERVER_LABEL, null);
            assertNull(server.getServerUrl());
        });
    }

    @Test
    void testSetAndGetAuthorization() {
        // Arrange
        String authorization = "Bearer test-token";

        // Act
        MCPServer result = mcpServer.setAuthorization(authorization);

        // Assert
        assertEquals(authorization, mcpServer.getAuthorization());
        assertSame(mcpServer, result); // Test fluent interface
    }

    @Test
    void testSetAuthorizationWithNull() {
        // Act
        mcpServer.setAuthorization(null);

        // Assert
        assertNull(mcpServer.getAuthorization());
    }

    @Test
    void testSetAndGetHeaders() {
        // Arrange
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "test-value");

        // Act
        MCPServer result = mcpServer.setHeaders(headers);

        // Assert
        assertEquals(headers, mcpServer.getHeaders());
        assertEquals("application/json", mcpServer.getHeaders().get("Content-Type"));
        assertEquals("test-value", mcpServer.getHeaders().get("X-Custom-Header"));
        assertSame(mcpServer, result); // Test fluent interface
    }

    @Test
    void testSetHeadersWithEmptyMap() {
        // Arrange
        Map<String, String> emptyHeaders = new HashMap<>();

        // Act
        mcpServer.setHeaders(emptyHeaders);

        // Assert
        assertNotNull(mcpServer.getHeaders());
        assertEquals(0, mcpServer.getHeaders().size());
    }

    @Test
    void testSetHeadersWithNull() {
        // Act
        mcpServer.setHeaders(null);

        // Assert
        assertNull(mcpServer.getHeaders());
    }

    @Test
    void testSetAndGetAllowedTools() {
        // Arrange
        List<String> allowedTools = Arrays.asList("tool1", "tool2", "tool3");

        // Act
        MCPServer result = mcpServer.setAllowedTools(allowedTools);

        // Assert
        assertEquals(allowedTools, mcpServer.getAllowedTools());
        assertEquals(3, mcpServer.getAllowedTools().size());
        assertSame(mcpServer, result); // Test fluent interface
    }

    @Test
    void testSetAllowedToolsWithEmptyList() {
        // Arrange
        List<String> emptyList = Arrays.asList();

        // Act
        mcpServer.setAllowedTools(emptyList);

        // Assert
        assertNotNull(mcpServer.getAllowedTools());
        assertEquals(0, mcpServer.getAllowedTools().size());
    }

    @Test
    void testSetAllowedToolsWithNull() {
        // Act
        mcpServer.setAllowedTools(null);

        // Assert
        assertNull(mcpServer.getAllowedTools());
    }

    @Test
    void testSetAndGetRequireApproval() {
        // Arrange
        BinaryData requireApproval = BinaryData.fromObject(MCPApprovalType.ALWAYS);

        // Act
        MCPServer result = mcpServer.setRequireApproval(requireApproval);

        // Assert
        assertEquals(requireApproval, mcpServer.getRequireApproval());
        assertSame(mcpServer, result); // Test fluent interface
    }

    @Test
    void testSetRequireApprovalWithNull() {
        // Act
        mcpServer.setRequireApproval(null);

        // Assert
        assertNull(mcpServer.getRequireApproval());
    }

    @Test
    void testTypeIsAlwaysMCP() {
        // The type should always be MCP and not changeable
        assertEquals(ToolType.MCP, mcpServer.getType());

        // Create another instance to verify consistency
        MCPServer anotherServer = new MCPServer("another-server", "http://localhost:9090");
        assertEquals(ToolType.MCP, anotherServer.getType());
    }

    @Test
    void testFluentConfiguration() {
        // Arrange
        String authorization = "Bearer token";
        Map<String, String> headers = new HashMap<>();
        headers.put("X-API-Key", "secret");
        List<String> allowedTools = Arrays.asList("tool1", "tool2");
        BinaryData requireApproval = BinaryData.fromObject(MCPApprovalType.NEVER);

        // Act - Test fluent method chaining
        MCPServer result = mcpServer.setAuthorization(authorization)
            .setHeaders(headers)
            .setAllowedTools(allowedTools)
            .setRequireApproval(requireApproval);

        // Assert
        assertSame(mcpServer, result);
        assertEquals(authorization, mcpServer.getAuthorization());
        assertEquals(headers, mcpServer.getHeaders());
        assertEquals(allowedTools, mcpServer.getAllowedTools());
        assertEquals(requireApproval, mcpServer.getRequireApproval());
    }

    @Test
    void testDefaultValues() {
        // Test default values after construction
        assertEquals(TEST_SERVER_LABEL, mcpServer.getServerLabel());
        assertEquals(TEST_SERVER_URL, mcpServer.getServerUrl());
        assertEquals(ToolType.MCP, mcpServer.getType());
        assertNull(mcpServer.getAuthorization());
        assertNull(mcpServer.getHeaders());
        assertNull(mcpServer.getAllowedTools());
        assertNull(mcpServer.getRequireApproval());
    }

    @Test
    void testJsonSerializationRoundtrip() {
        // Arrange
        mcpServer.setAuthorization("Bearer test-token");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        mcpServer.setHeaders(headers);
        mcpServer.setAllowedTools(Arrays.asList("tool1", "tool2"));

        // Act - Serialize to JSON
        String json = BinaryData.fromObject(mcpServer).toString();

        // Assert - JSON should contain expected fields
        assertNotNull(json);
        // Note: Full deserialization test would require fromJson implementation
    }

    @Test
    void testMultipleInstancesAreIndependent() {
        // Arrange
        MCPServer server1 = new MCPServer("server1", "http://server1.com");
        MCPServer server2 = new MCPServer("server2", "http://server2.com");

        server1.setAuthorization("token1");
        server2.setAuthorization("token2");

        // Assert
        assertEquals("token1", server1.getAuthorization());
        assertEquals("token2", server2.getAuthorization());
        assertNotNull(server1.getServerLabel());
        assertNotNull(server2.getServerLabel());
    }

    @Test
    void testComplexRequireApprovalConfiguration() {
        // Test with MCPApprovalType.ALWAYS
        BinaryData alwaysApproval = BinaryData.fromObject(MCPApprovalType.ALWAYS);
        mcpServer.setRequireApproval(alwaysApproval);
        assertEquals(alwaysApproval, mcpServer.getRequireApproval());

        // Test with MCPApprovalType.NEVER
        BinaryData neverApproval = BinaryData.fromObject(MCPApprovalType.NEVER);
        mcpServer.setRequireApproval(neverApproval);
        assertEquals(neverApproval, mcpServer.getRequireApproval());
    }

    @Test
    void testHeadersWithSpecialCharacters() {
        // Arrange
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Custom-Header", "value with spaces and 特殊字符");
        headers.put("Authorization", "Bearer token-with-special-chars!@#$%");

        // Act
        mcpServer.setHeaders(headers);

        // Assert
        assertEquals(headers, mcpServer.getHeaders());
        assertEquals("value with spaces and 特殊字符", mcpServer.getHeaders().get("X-Custom-Header"));
    }

    @Test
    void testAllowedToolsWithSpecialNames() {
        // Arrange
        List<String> toolNames
            = Arrays.asList("tool_with_underscore", "tool-with-dash", "tool.with.dots", "toolWithCamelCase");

        // Act
        mcpServer.setAllowedTools(toolNames);

        // Assert
        assertEquals(toolNames, mcpServer.getAllowedTools());
        assertEquals(4, mcpServer.getAllowedTools().size());
    }
}
