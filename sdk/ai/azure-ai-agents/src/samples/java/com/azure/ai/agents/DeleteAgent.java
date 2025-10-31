// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.DeleteAgentResponse;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class DeleteAgent {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT");
        String agentName = "agent_created_from_java";
        // Code sample for creating an agent
        AgentsClient agentsClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildClient();

        DeleteAgentResponse agent = agentsClient.deleteAgent(agentName);

        System.out.println("Deleted agent with the following details:");
        System.out.println("\tAgent Name: " + agent.getName());
        System.out.println("\tAgent was deleted: " + agent.isDeleted());
    }
}
