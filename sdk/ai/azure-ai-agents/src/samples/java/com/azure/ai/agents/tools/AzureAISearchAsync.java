// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesAsyncClient;
import com.azure.ai.agents.models.AISearchIndexResource;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureAISearchQueryType;
import com.azure.ai.agents.models.AzureAISearchTool;
import com.azure.ai.agents.models.AzureAISearchToolResource;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.ResponseCreateParams;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import reactor.core.publisher.Mono;

/**
 * This sample demonstrates (using the async client) how to create an agent with the Azure AI Search tool
 * to search through indexed documents.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_DEPLOYMENT_NAME - The model deployment name.</li>
 *   <li>AZURE_AI_SEARCH_CONNECTION_ID - The Azure AI Search connection ID.</li>
 *   <li>AI_SEARCH_INDEX_NAME - The name of the search index.</li>
 * </ul>
 */
public class AzureAISearchAsync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_DEPLOYMENT_NAME");
        String connectionId = Configuration.getGlobalConfiguration().get("AZURE_AI_SEARCH_CONNECTION_ID");
        String indexName = Configuration.getGlobalConfiguration().get("AI_SEARCH_INDEX_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesAsyncClient = builder.buildResponsesAsyncClient();

        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();

        // Create Azure AI Search tool with index configuration
        AzureAISearchTool aiSearchTool = new AzureAISearchTool(
            new AzureAISearchToolResource(Arrays.asList(
                new AISearchIndexResource()
                    .setProjectConnectionId(connectionId)
                    .setIndexName(indexName)
                    .setQueryType(AzureAISearchQueryType.SIMPLE)
            ))
        );
        // Create agent with AI Search tool
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that can search through indexed documents. "
                + "Always provide citations for answers using the tool.")
            .setTools(Collections.singletonList(aiSearchTool));

        agentsAsyncClient.createAgentVersion("ai-search-agent", agentDefinition)
            .flatMap(agent -> {
                agentRef.set(agent);
                System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

                AgentReference agentReference = new AgentReference(agent.getName())
                    .setVersion(agent.getVersion());

                return responsesAsyncClient.createWithAgent(agentReference,
                    ResponseCreateParams.builder()
                        .input("Search for information about Azure AI services"));
            })
            .doOnNext(response -> {
                System.out.println("Response: " + response.output());
            })
            .then(Mono.defer(() -> {
                AgentVersionDetails agent = agentRef.get();
                if (agent != null) {
                    return agentsAsyncClient.deleteAgentVersion(agent.getName(), agent.getVersion())
                        .doOnSuccess(v -> System.out.println("Agent deleted"));
                }
                return Mono.empty();
            }))
            .doOnError(error -> System.err.println("Error: " + error.getMessage()))
            .timeout(Duration.ofSeconds(300))
            .block();
    }
}
