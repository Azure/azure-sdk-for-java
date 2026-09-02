// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.agents;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.models.AgentDetails;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Demonstrates creating and retrieving an agent.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class GetAgent {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_MODEL_NAME");

        AgentsClient client = new com.azure.ai.agents.AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildAgentsClient();
        AgentVersionDetails created = client.createAgentVersion("retrieve-agent-java",
            new CreateAgentVersionInput(new PromptAgentDefinition(model)
                .setInstructions("You are a helpful assistant.")));
        try {
            AgentDetails agent = client.getAgent(created.getName());
            System.out.println("Agent ID: " + agent.getId());
            System.out.println("Agent name: " + agent.getName());
            System.out.println("Latest version: " + agent.getVersions().getLatest().getVersion());
        } finally {
            client.deleteAgentVersion(created.getName(), created.getVersion());
        }
    }
}
