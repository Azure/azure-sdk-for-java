// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.DeleteAgentResponse;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class AgentsSample {

    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("AZURE_AGENT_MODEL");

        AgentsClient agentsClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildAgentsClient();

        PromptAgentDefinition request = new PromptAgentDefinition(model);
        AgentVersionDetails agent = agentsClient.createAgentVersion("agent_created_from_java", request);

        System.out.println("Agent ID: " + agent.getId());
        System.out.println("Agent Name: " + agent.getName());
        System.out.println("Agent Version: " + agent.getVersion());

        DeleteAgentResponse agentDeletion = agentsClient.deleteAgent(agent.getName());

        System.out.println("Deleted agent with the following details:");
        System.out.println("\tAgent Name: " + agentDeletion.getName());
        System.out.println("\tAgent was deleted: " + agentDeletion.isDeleted());
    }
}
