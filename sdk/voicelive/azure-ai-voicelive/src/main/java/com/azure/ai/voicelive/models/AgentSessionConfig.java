// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.annotation.Fluent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration for connecting to an Azure AI Foundry agent session.
 *
 * <p>This class provides the necessary parameters to establish a connection with an
 * Azure AI Foundry agent, including the agent name, project name, and optional
 * parameters like agent version, conversation ID, and authentication settings.</p>
 *
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * AgentSessionConfig config = new AgentSessionConfig("my-agent", "my-project")
 *     .setAgentVersion("1.0")
 *     .setConversationId("conv-123");
 *
 * client.startSession(config).subscribe(session -> {
 *     // Use the session
 * });
 * }</pre>
 */
@Fluent
public final class AgentSessionConfig {

    private final String agentName;
    private final String projectName;
    private String agentVersion;
    private String authenticationIdentityClientId;
    private String conversationId;
    private String foundryResourceOverride;

    /**
     * Creates a new AgentSessionConfig with the required parameters.
     *
     * @param agentName The name of the agent. This is required.
     * @param projectName The name of the project containing the agent. This is required.
     * @throws NullPointerException if agentName or projectName is null.
     * @throws IllegalArgumentException if agentName or projectName is empty.
     */
    public AgentSessionConfig(String agentName, String projectName) {
        Objects.requireNonNull(agentName, "'agentName' cannot be null");
        Objects.requireNonNull(projectName, "'projectName' cannot be null");

        if (agentName.isEmpty()) {
            throw new IllegalArgumentException("'agentName' cannot be empty");
        }
        if (projectName.isEmpty()) {
            throw new IllegalArgumentException("'projectName' cannot be empty");
        }

        this.agentName = agentName;
        this.projectName = projectName;
    }

    /**
     * Gets the agent name.
     *
     * @return The agent name.
     */
    public String getAgentName() {
        return agentName;
    }

    /**
     * Gets the project name.
     *
     * @return The project name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Gets the agent version.
     *
     * @return The agent version, or null if not set.
     */
    public String getAgentVersion() {
        return agentVersion;
    }

    /**
     * Sets the agent version.
     *
     * @param agentVersion The agent version.
     * @return This AgentSessionConfig for chaining.
     */
    public AgentSessionConfig setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
        return this;
    }

    /**
     * Gets the authentication identity client ID.
     *
     * <p>This is used when the agent requires a specific managed identity for authentication.</p>
     *
     * @return The authentication identity client ID, or null if not set.
     */
    public String getAuthenticationIdentityClientId() {
        return authenticationIdentityClientId;
    }

    /**
     * Sets the authentication identity client ID.
     *
     * <p>This is used when the agent requires a specific managed identity for authentication.</p>
     *
     * @param authenticationIdentityClientId The client ID of the managed identity to use.
     * @return This AgentSessionConfig for chaining.
     */
    public AgentSessionConfig setAuthenticationIdentityClientId(String authenticationIdentityClientId) {
        this.authenticationIdentityClientId = authenticationIdentityClientId;
        return this;
    }

    /**
     * Gets the conversation ID.
     *
     * <p>This can be used to resume a previous conversation with the agent.</p>
     *
     * @return The conversation ID, or null if not set.
     */
    public String getConversationId() {
        return conversationId;
    }

    /**
     * Sets the conversation ID.
     *
     * <p>This can be used to resume a previous conversation with the agent.</p>
     *
     * @param conversationId The conversation ID.
     * @return This AgentSessionConfig for chaining.
     */
    public AgentSessionConfig setConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    /**
     * Gets the Foundry resource override.
     *
     * <p>This can be used to specify a different Azure AI Foundry resource than the default.</p>
     *
     * @return The Foundry resource override, or null if not set.
     */
    public String getFoundryResourceOverride() {
        return foundryResourceOverride;
    }

    /**
     * Sets the Foundry resource override.
     *
     * <p>This can be used to specify a different Azure AI Foundry resource than the default.</p>
     *
     * @param foundryResourceOverride The Foundry resource override.
     * @return This AgentSessionConfig for chaining.
     */
    public AgentSessionConfig setFoundryResourceOverride(String foundryResourceOverride) {
        this.foundryResourceOverride = foundryResourceOverride;
        return this;
    }

    /**
     * Converts this configuration to query parameters for the WebSocket connection.
     *
     * @return A map of query parameter names to values.
     */
    public Map<String, String> toQueryParameters() {
        Map<String, String> params = new LinkedHashMap<>();

        // Required parameters
        params.put("agent-name", agentName);
        params.put("agent-project-name", projectName);

        // Optional parameters
        if (agentVersion != null && !agentVersion.isEmpty()) {
            params.put("agent-version", agentVersion);
        }
        if (conversationId != null && !conversationId.isEmpty()) {
            params.put("conversation-id", conversationId);
        }
        if (authenticationIdentityClientId != null && !authenticationIdentityClientId.isEmpty()) {
            params.put("agent-authentication-identity-client-id", authenticationIdentityClientId);
        }
        if (foundryResourceOverride != null && !foundryResourceOverride.isEmpty()) {
            params.put("foundry-resource-override", foundryResourceOverride);
        }

        return params;
    }
}
