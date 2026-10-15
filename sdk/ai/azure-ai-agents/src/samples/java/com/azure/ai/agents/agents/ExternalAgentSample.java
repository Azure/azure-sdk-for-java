// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.agents;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.AgentDetails;
import com.azure.ai.agents.models.AgentKind;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.ExternalAgentDefinition;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrating create, retrieve, list, and delete operations for an external agent using the
 * synchronous {@link AgentsClient}.
 *
 * <p>External agents are third-party agents hosted outside Foundry (for example, on GCP or AWS).
 * Registration is metadata-only: Foundry records the agent definition to light up observability
 * experiences over customer-emitted OpenTelemetry data. External agents are a preview feature, so
 * {@code allowPreview(true)} is required to add the {@code Foundry-Features: ExternalAgents=V1Preview}
 * header.</p>
 *
 * <p>Before running the sample, set the {@code FOUNDRY_PROJECT_ENDPOINT} environment variable.</p>
 */
public class ExternalAgentSample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");

        AgentsClient agentsClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .allowPreview(true)
            .buildAgentsClient();

        String agentName = "myExternalAgent1";

        // Clean up any pre-existing agent with the same name.
        try {
            agentsClient.deleteAgent(agentName);
        } catch (ResourceNotFoundException ignored) {
            // The sample agent does not already exist.
        }

        // BEGIN:com.azure.ai.agents.agents.ExternalAgentSample.createAgentVersion
        ExternalAgentDefinition agentDefinition = new ExternalAgentDefinition()
            .setOtelAgentId("sample-external-agent");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("sample", "external_agents_crud");
        metadata.put("status", "created");

        CreateAgentVersionInput input = new CreateAgentVersionInput(agentDefinition)
            .setDescription("External agent registered by the azure-ai-agents sample.")
            .setMetadata(metadata);

        AgentVersionDetails agentVersion = agentsClient.createAgentVersion(agentName, input);
        System.out.printf("Agent created (id: %s, name: %s, version: %s)%n",
            agentVersion.getId(), agentVersion.getName(), agentVersion.getVersion());
        // END:com.azure.ai.agents.agents.ExternalAgentSample.createAgentVersion

        try {
            // Retrieve the agent.
            AgentDetails agent = agentsClient.getAgent(agentVersion.getName());
            String latestVersion = agent.getVersions() == null || agent.getVersions().getLatest() == null
                ? null : agent.getVersions().getLatest().getVersion();
            System.out.printf("Agent retrieved (id: %s, name: %s, latest version: %s)%n",
                agent.getId(), agent.getName(), latestVersion);

            // List versions of the agent.
            System.out.printf("Versions for agent %s%n", agentVersion.getName());
            for (AgentVersionDetails version : agentsClient.listAgentVersions(agentVersion.getName())) {
                System.out.printf("    - Agent id: %s, version: %s%n", version.getId(), version.getVersion());
            }

            // List all external agents.
            System.out.println("Found the following external agents:");
            for (AgentDetails externalAgent : agentsClient.listAgents(AgentKind.EXTERNAL, null, null, null, null)) {
                System.out.printf("    - Agent id: %s, name: %s%n", externalAgent.getId(), externalAgent.getName());
            }
        } finally {
            // Delete the agent.
            agentsClient.deleteAgent(agentVersion.getName());
            System.out.printf("Agent deleted (name: %s)%n", agentVersion.getName());
        }
    }
}
