// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
