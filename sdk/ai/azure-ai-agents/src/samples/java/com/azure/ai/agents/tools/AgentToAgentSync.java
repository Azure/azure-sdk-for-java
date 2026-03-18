// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.A2APreviewTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.Collections;

/**
 * This sample demonstrates how to create an agent with an Agent-to-Agent (A2A) tool
 * that can communicate with remote A2A endpoints.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 *   <li>A2A_PROJECT_CONNECTION_ID - The A2A project connection ID.</li>
 * </ul>
 */
public class AgentToAgentSync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String a2aConnectionId = Configuration.getGlobalConfiguration().get("A2A_PROJECT_CONNECTION_ID");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        // BEGIN: com.azure.ai.agents.define_agent_to_agent
        // Create agent-to-agent tool with connection ID
        A2APreviewTool a2aTool = new A2APreviewTool()
            .setProjectConnectionId(a2aConnectionId);
        // END: com.azure.ai.agents.define_agent_to_agent

        // Create agent with agent-to-agent tool
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a coordinator agent that can communicate with other agents.")
            .setTools(Collections.singletonList(a2aTool));

        AgentVersionDetails agent = agentsClient.createAgentVersion("a2a-agent", agentDefinition);
        System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

        try {
            // Create a response
            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            Response response = responsesClient.createWithAgent(
                agentReference,
                ResponseCreateParams.builder()
                    .input("What can the secondary agent do?"));

            System.out.println("Response: " + response.output());
        } finally {
            // Clean up
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }
}
