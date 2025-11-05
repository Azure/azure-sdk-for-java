// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentObject;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class ListAgents {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT");
        // Code sample for listing all agents
        AgentsClient agentsClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildAgentsClient();

        System.out.println("Listing all agents:");
        for (AgentObject agent : agentsClient.listAgents()) {
            System.out.println("Agent ID: " + agent.getId());
            System.out.println("Agent Name: " + agent.getName());
            if (agent.getVersions() != null && agent.getVersions().getLatest() != null) {
                System.out.println("Latest Version ID: " + agent.getVersions().getLatest().getId());
            }
            System.out.println("---");
        }
    }
}
