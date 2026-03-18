// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesAsyncClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.BingCustomSearchConfiguration;
import com.azure.ai.agents.models.BingCustomSearchPreviewTool;
import com.azure.ai.agents.models.BingCustomSearchToolParameters;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.ResponseCreateParams;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This sample demonstrates (using the async client) how to create an agent with the Bing Custom Search tool
 * to search custom search instances and provide responses with relevant results.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 *   <li>BING_CUSTOM_SEARCH_PROJECT_CONNECTION_ID - The Bing Custom Search project connection ID.</li>
 *   <li>BING_CUSTOM_SEARCH_INSTANCE_NAME - The Bing Custom Search instance name.</li>
 * </ul>
 */
public class BingCustomSearchAsync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String connectionId = Configuration.getGlobalConfiguration().get("BING_CUSTOM_SEARCH_PROJECT_CONNECTION_ID");
        String instanceName = Configuration.getGlobalConfiguration().get("BING_CUSTOM_SEARCH_INSTANCE_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesAsyncClient = builder.buildResponsesAsyncClient();

        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();

        BingCustomSearchPreviewTool bingCustomSearchTool = new BingCustomSearchPreviewTool(
            new BingCustomSearchToolParameters(Arrays.asList(
                new BingCustomSearchConfiguration(connectionId, instanceName)
            ))
        );

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful agent that can use Bing Custom Search tools to assist users. "
                + "Use the available Bing Custom Search tools to answer questions and perform tasks.")
            .setTools(Collections.singletonList(bingCustomSearchTool));

        agentsAsyncClient.createAgentVersion("bing-custom-search-agent", agentDefinition)
            .flatMap(agent -> {
                agentRef.set(agent);
                System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

                AgentReference agentReference = new AgentReference(agent.getName())
                    .setVersion(agent.getVersion());

                return responsesAsyncClient.createWithAgent(agentReference,
                    ResponseCreateParams.builder()
                        .input("Search for the latest Azure AI documentation"));
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
