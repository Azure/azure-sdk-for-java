// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentVersionObject;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class CreateAgent {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("AZURE_AGENT_MODEL");
        // Code sample for creating an agent
        AgentsClient agentsClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildClient();

        PromptAgentDefinition request = new PromptAgentDefinition(model);
        AgentVersionObject agent = agentsClient.createAgentVersion("agent_created_from_java", request);

        System.out.println("Agent ID: " + agent.getId());
        System.out.println("Agent Name: " + agent.getName());
        System.out.println("Agent Version: " + agent.getVersion());
    }
}
