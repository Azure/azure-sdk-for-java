// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.agents;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentDetails;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

/**
 * This sample demonstrates the full lifecycle of managing a hosted agent:
 * creating, listing, retrieving, invoking, and deleting agents and their versions.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_DEPLOYMENT_NAME - The model deployment name.</li>
 * </ul>
 */
public class HostedAgent {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_DEPLOYMENT_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build());

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        // --- 1. Create an agent version ---
        PromptAgentDefinition definition = new PromptAgentDefinition(model);

        AgentVersionDetails agentVersion =
            agentsClient.createAgentVersion("my-agent", definition);

        System.out.println("Created version: " + agentVersion.getVersion());

        // --- 2. List and retrieve agents ---

        // List all agents
        for (AgentDetails agent : agentsClient.listAgents()) {
            System.out.println("Agent: " + agent.getName());
        }

        // Get a specific agent
        AgentDetails myAgent = agentsClient.getAgent("my-agent");
        System.out.println("Agent: " + myAgent.getName());

        // Get a specific version
        AgentVersionDetails version =
            agentsClient.getAgentVersionDetails("my-agent", agentVersion.getVersion());
        System.out.println("Version: " + version.getVersion());

        // List versions of an agent
        for (AgentVersionDetails v : agentsClient.listAgentVersions("my-agent")) {
            System.out.println("Version: " + v.getVersion());
        }

        // --- 3. Invoke the agent via the responses client ---
        AgentReference agentRef = new AgentReference(myAgent.getName())
            .setVersion(agentVersion.getVersion());

        Response response = responsesClient.createWithAgent(
            agentRef,
            ResponseCreateParams.builder()
                .input("Hello! What can you help me with?"));

        System.out.println("Response: " + response.output());

        // --- 4. Clean up ---

        // Delete a specific version
        agentsClient.deleteAgentVersion("my-agent", agentVersion.getVersion());
        System.out.println("Agent version deleted.");

        // Delete the agent and all its versions
        agentsClient.deleteAgent("my-agent");
        System.out.println("Agent deleted.");
    }
}
