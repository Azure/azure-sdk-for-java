// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.BingGroundingSearchConfiguration;
import com.azure.ai.agents.models.BingGroundingSearchToolParameters;
import com.azure.ai.agents.models.BingGroundingTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.Collections;
import java.util.Arrays;

/**
 * This sample demonstrates how to create an agent with the Bing Grounding tool
 * to find up-to-date information from the web.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 *   <li>BING_PROJECT_CONNECTION_ID - The Bing project connection ID.</li>
 * </ul>
 */
public class BingGroundingSync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String bingConnectionId = Configuration.getGlobalConfiguration().get("BING_PROJECT_CONNECTION_ID");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        // BEGIN: com.azure.ai.agents.define_bing_grounding
        // Create Bing grounding tool with connection configuration
        BingGroundingTool bingTool = new BingGroundingTool(
            new BingGroundingSearchToolParameters(Arrays.asList(
                new BingGroundingSearchConfiguration(bingConnectionId)
            ))
        );
        // END: com.azure.ai.agents.define_bing_grounding

        // Create agent with Bing grounding tool
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant. Use Bing to find up-to-date information.")
            .setTools(Collections.singletonList(bingTool));

        AgentVersionDetails agent = agentsClient.createAgentVersion("bing-grounding-agent", agentDefinition);
        System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

        try {
            // Create a response
            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(agentReference),
                ResponseCreateParams.builder()
                    .input("What are the latest developments in AI?"));

            System.out.println("Response: " + response.output());
        } finally {
            // Clean up
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }
}
