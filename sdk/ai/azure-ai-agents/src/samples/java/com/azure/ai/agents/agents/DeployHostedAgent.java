// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.agents;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.AgentProtocol;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.HostedAgentDefinition;
import com.azure.ai.agents.models.ProtocolVersionRecord;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;
import java.util.Map;

/**
 * This sample demonstrates how to create and delete a hosted agent
 * using a custom container image with the HostedAgentDefinition.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 * </ul>
 */
public class DeployHostedAgent {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");

        AgentsClient agentsClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildAgentsClient();

        // Create hosted agent with custom container image
        HostedAgentDefinition agentDefinition =
            new HostedAgentDefinition(
                Arrays.asList(
                    new ProtocolVersionRecord(
                        AgentProtocol.ACTIVITY_PROTOCOL, "v1")),
                "1",
                "2Gi")
            .setImage("your-registry.azurecr.io/your-image:tag")
            .setEnvironmentVariables(Map.of(
                "AZURE_AI_PROJECT_ENDPOINT", endpoint,
                "MODEL_NAME", "gpt-4.1"));

        AgentVersionDetails agentVersion =
            agentsClient.createAgentVersion("my-hosted-agent", agentDefinition);

        System.out.printf("Agent created: %s, version: %s%n",
            agentVersion.getName(),
            agentVersion.getVersion());

        // Clean up
        agentsClient.deleteAgentVersion(
            agentVersion.getName(),
            agentVersion.getVersion());
        System.out.println("Agent version deleted.");
    }
}
