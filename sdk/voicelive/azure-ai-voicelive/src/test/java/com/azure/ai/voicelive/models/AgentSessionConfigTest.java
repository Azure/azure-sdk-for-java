// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AgentSessionConfig}.
 */
class AgentSessionConfigTest {

    @Test
    void testConstructorWithValidParameters() {
        // Arrange & Act
        AgentSessionConfig config = new AgentSessionConfig("test-agent", "test-project");

        // Assert
        assertEquals("test-agent", config.getAgentName());
        assertEquals("test-project", config.getProjectName());
    }

    @Test
    void testConstructorWithNullAgentName() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> new AgentSessionConfig(null, "test-project"));
    }

    @Test
    void testConstructorWithNullProjectName() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> new AgentSessionConfig("test-agent", null));
    }

    @Test
    void testConstructorWithEmptyAgentName() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new AgentSessionConfig("", "test-project"));
    }

    @Test
    void testConstructorWithEmptyProjectName() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new AgentSessionConfig("test-agent", ""));
    }

    @Test
    void testFluentSetters() {
        // Arrange
        AgentSessionConfig config = new AgentSessionConfig("test-agent", "test-project");

        // Act
        AgentSessionConfig result = config.setAgentVersion("1.0")
            .setConversationId("conv-123")
            .setAuthenticationIdentityClientId("client-id-456")
            .setFoundryResourceOverride("custom-resource");

        // Assert - verify fluent pattern returns same instance
        assertEquals(config, result);
        assertEquals("1.0", config.getAgentVersion());
        assertEquals("conv-123", config.getConversationId());
        assertEquals("client-id-456", config.getAuthenticationIdentityClientId());
        assertEquals("custom-resource", config.getFoundryResourceOverride());
    }

    @Test
    void testOptionalPropertiesDefaultToNull() {
        // Arrange & Act
        AgentSessionConfig config = new AgentSessionConfig("test-agent", "test-project");

        // Assert
        assertNull(config.getAgentVersion());
        assertNull(config.getConversationId());
        assertNull(config.getAuthenticationIdentityClientId());
        assertNull(config.getFoundryResourceOverride());
    }

    @Test
    void testToQueryParametersWithRequiredOnly() {
        // Arrange
        AgentSessionConfig config = new AgentSessionConfig("my-agent", "my-project");

        // Act
        Map<String, String> params = config.toQueryParameters();

        // Assert
        assertNotNull(params);
        assertEquals(2, params.size());
        assertEquals("my-agent", params.get("agent-name"));
        assertEquals("my-project", params.get("agent-project-name"));
    }

    @Test
    void testToQueryParametersWithAllOptions() {
        // Arrange
        AgentSessionConfig config = new AgentSessionConfig("my-agent", "my-project").setAgentVersion("2.0")
            .setConversationId("conversation-xyz")
            .setAuthenticationIdentityClientId("auth-client-id")
            .setFoundryResourceOverride("override-resource");

        // Act
        Map<String, String> params = config.toQueryParameters();

        // Assert
        assertNotNull(params);
        assertEquals(6, params.size());
        assertEquals("my-agent", params.get("agent-name"));
        assertEquals("my-project", params.get("agent-project-name"));
        assertEquals("2.0", params.get("agent-version"));
        assertEquals("conversation-xyz", params.get("conversation-id"));
        assertEquals("auth-client-id", params.get("agent-authentication-identity-client-id"));
        assertEquals("override-resource", params.get("foundry-resource-override"));
    }

    @Test
    void testToQueryParametersExcludesEmptyStrings() {
        // Arrange
        AgentSessionConfig config
            = new AgentSessionConfig("my-agent", "my-project").setAgentVersion("").setConversationId("");

        // Act
        Map<String, String> params = config.toQueryParameters();

        // Assert
        assertNotNull(params);
        assertEquals(2, params.size());
        assertTrue(params.containsKey("agent-name"));
        assertTrue(params.containsKey("agent-project-name"));
        assertFalse(params.containsKey("agent-version"));
        assertFalse(params.containsKey("conversation-id"));
    }

    @Test
    void testToQueryParametersWithSomeOptionalParams() {
        // Arrange
        AgentSessionConfig config = new AgentSessionConfig("supervisor", "ai-project").setAgentVersion("1.5");

        // Act
        Map<String, String> params = config.toQueryParameters();

        // Assert
        assertNotNull(params);
        assertEquals(3, params.size());
        assertEquals("supervisor", params.get("agent-name"));
        assertEquals("ai-project", params.get("agent-project-name"));
        assertEquals("1.5", params.get("agent-version"));
        assertFalse(params.containsKey("conversation-id"));
        assertFalse(params.containsKey("agent-authentication-identity-client-id"));
        assertFalse(params.containsKey("foundry-resource-override"));
    }
}
