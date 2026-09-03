// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.AISearchIndexResource;
import com.azure.ai.agents.models.AzureAISearchQueryType;
import com.azure.ai.agents.models.AzureAISearchTool;
import com.azure.ai.agents.models.AzureAISearchToolResource;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClientAsync;
import com.openai.models.responses.ResponseCreateParams;
import reactor.core.publisher.Mono;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.ProtocolConfiguration;
import com.azure.ai.agents.models.ResponsesProtocolConfiguration;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

/**
 * This sample demonstrates (using the async client) how to create an agent with the Azure AI Search tool
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
public class AzureAISearchAsync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String connectionId = Configuration.getGlobalConfiguration().get("AZURE_AI_SEARCH_CONNECTION_ID");
        String indexName = Configuration.getGlobalConfiguration().get("AI_SEARCH_INDEX_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();

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

        // Create the agent version and pin the agent endpoint to it. The endpoint URL identifies the agent,
        // so responses.create(...) below does not need to send an agent_reference in its body.
        String agentName = "ai-search-agent";
        Mono.usingWhen(
                agentsAsyncClient.createAgentVersion(agentName, agentDefinition)
                    .flatMap(agent -> agentsAsyncClient.updateAgentDetails(agentName,
                            new UpdateAgentDetailsOptions().setAgentEndpoint(
                                new AgentEndpointConfig()
                                    .setVersionSelector(new VersionSelector().setVersionSelectionRules(Collections.singletonList(
                                        new FixedRatioVersionSelectionRule(100).setAgentVersion(agent.getVersion()))))
                                    .setProtocolConfiguration(new ProtocolConfiguration().setResponses(new ResponsesProtocolConfiguration()))))
                        .thenReturn(agent)),
                agent -> {
                    OpenAIClientAsync openAIAsyncClient
                        = builder.buildAgentScopedOpenAIAsyncClient(agentName);
                    return Mono.fromFuture(openAIAsyncClient.responses().create(ResponseCreateParams.builder()
                            .input("Search for information about Azure AI services")
                            .build()))
                        .doOnNext(response -> System.out.println("Response: " + response.output()));
                },
                agent -> agentsAsyncClient.deleteAgentVersion(agentName, agent.getVersion()))
            .doOnError(error -> System.err.println("Error: " + error.getMessage()))
            .timeout(Duration.ofSeconds(300))
            .block();
    }
}
