// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentDetails;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class GetAgent {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT");
        String agentName = "agent_created_from_java";
        // Code sample for creating an agent
        AgentsClient agentsClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildAgentsClient();

        AgentDetails agent = agentsClient.getAgent(agentName);

        System.out.println("Agent ID: " + agent.getId());
        System.out.println("Agent Name: " + agent.getName());
        System.out.println("Agent Version: " + agent.getVersions().getLatest());
    }
}
