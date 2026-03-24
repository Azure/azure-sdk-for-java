// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AISearchIndexResource;
import com.azure.ai.agents.models.AzureAISearchQueryType;
import com.azure.ai.agents.models.AzureAISearchTool;
import com.azure.ai.agents.models.AzureAISearchToolResource;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.Arrays;
import java.util.Collections;

/**
 * This sample demonstrates how to create an agent with the Azure AI Search tool
 * to search through indexed documents.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 *   <li>AZURE_AI_SEARCH_CONNECTION_ID - The Azure AI Search connection ID.</li>
 *   <li>AI_SEARCH_INDEX_NAME - The name of the search index.</li>
 * </ul>
 */
public class AzureAISearchSync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String connectionId = Configuration.getGlobalConfiguration().get("AZURE_AI_SEARCH_CONNECTION_ID");
        String indexName = Configuration.getGlobalConfiguration().get("AI_SEARCH_INDEX_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        // BEGIN: com.azure.ai.agents.define_azure_ai_search
        // Create Azure AI Search tool with index configuration
        AzureAISearchTool aiSearchTool = new AzureAISearchTool(
            new AzureAISearchToolResource(Arrays.asList(
                new AISearchIndexResource()
                    .setProjectConnectionId(connectionId)
                    .setIndexName(indexName)
                    .setQueryType(AzureAISearchQueryType.SIMPLE)
            ))
        );
        // END: com.azure.ai.agents.define_azure_ai_search

        // Create agent with AI Search tool
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that can search through indexed documents. "
                + "Always provide citations for answers using the tool.")
            .setTools(Collections.singletonList(aiSearchTool));

        AgentVersionDetails agent = agentsClient.createAgentVersion("ai-search-agent", agentDefinition);
        System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

        try {
            // Create a response
            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(agentReference),
                ResponseCreateParams.builder()
                    .input("Search for information about Azure AI services"));

            System.out.println("Response: " + response.output());
        } finally {
            // Clean up
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }
}
