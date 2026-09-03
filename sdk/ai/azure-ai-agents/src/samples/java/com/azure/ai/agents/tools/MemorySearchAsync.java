// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaMemoryStoresClient;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.MemorySearchPreviewTool;
import com.azure.ai.agents.models.MemoryStoreDefaultDefinition;
import com.azure.ai.agents.models.MemoryStoreDefaultOptions;
import com.azure.ai.agents.models.MemoryStoreDetails;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.ProtocolConfiguration;
import com.azure.ai.agents.models.ResponsesProtocolConfiguration;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClientAsync;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.services.async.ConversationServiceAsync;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This sample demonstrates (using the async client) how to integrate memory into a prompt agent
 * using the Memory Search tool to retrieve relevant past user messages.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 *   <li>AZURE_AI_CHAT_MODEL_DEPLOYMENT_NAME - The chat model deployment name for memory.</li>
 *   <li>AZURE_AI_EMBEDDING_MODEL_DEPLOYMENT_NAME - The embedding model deployment name for memory.</li>
 * </ul>
 */
public class MemorySearchAsync {
    private static final long MEMORY_WRITE_DELAY_SECONDS = 60;

    public static void main(String[] args) throws Exception {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String chatModel = Configuration.getGlobalConfiguration().get("AZURE_AI_CHAT_MODEL_DEPLOYMENT_NAME");
        String embeddingModel = Configuration.getGlobalConfiguration().get("AZURE_AI_EMBEDDING_MODEL_DEPLOYMENT_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();
        ConversationServiceAsync conversationServiceAsync = builder.buildOpenAIAsyncClient().conversations();
        // Memory store operations use sync client for setup/teardown
        BetaMemoryStoresClient memoryStoresClient = builder.beta().buildBetaMemoryStoresClient();

        String memoryStoreName = "my_memory_store";
        String scope = "user_123";

        AtomicReference<String> firstConvRef = new AtomicReference<>();
        AtomicReference<String> secondConvRef = new AtomicReference<>();

        // Setup: delete memory store if it exists, then create it (sync)
        try {
            memoryStoresClient.deleteMemoryStore(memoryStoreName);
        } catch (ResourceNotFoundException ignored) {
            // no-op
        }

        MemoryStoreDefaultDefinition definition = new MemoryStoreDefaultDefinition(chatModel, embeddingModel)
            .setOptions(new MemoryStoreDefaultOptions(true, true));
        MemoryStoreDetails memoryStore = memoryStoresClient.createMemoryStore(
            memoryStoreName, definition, "Example memory store for conversations", null);
        System.out.printf("Created memory store: %s (%s)%n", memoryStore.getName(), memoryStore.getId());

        MemorySearchPreviewTool tool = new MemorySearchPreviewTool(memoryStore.getName(), scope)
            .setUpdateDelaySeconds(1);

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that answers general questions.")
            .setTools(Collections.singletonList(tool));

        String agentName = "memory-search-agent";
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

                    // First conversation: teach a preference
                    return Mono.fromFuture(conversationServiceAsync.create())
                        .<Response>flatMap(conv -> {
                            firstConvRef.set(conv.id());
                            return Mono.fromFuture(openAIAsyncClient.responses().create(
                                ResponseCreateParams.builder()
                                    .conversation(conv.id())
                                    .input("I prefer dark roast coffee")
                                    .build()));
                        })
                        .doOnNext(response -> System.out.println("First response received"))
                        .delayElement(Duration.ofSeconds(MEMORY_WRITE_DELAY_SECONDS))
                        .flatMap(ignored -> Mono.fromFuture(conversationServiceAsync.create())
                            .<Response>flatMap(conv -> {
                                secondConvRef.set(conv.id());
                                return Mono.fromFuture(openAIAsyncClient.responses().create(
                                    ResponseCreateParams.builder()
                                        .conversation(conv.id())
                                        .input("Please order my usual coffee")
                                        .build()));
                            }))
                        .doOnNext(response -> System.out.println("Response: " + response.output()));
                },
                agent -> agentsAsyncClient.deleteAgentVersion(agentName, agent.getVersion()))
            .doFinally(signal -> {
                // Cleanup — await conversation deletes before proceeding
                try {
                    String c1 = firstConvRef.get();
                    if (c1 != null) {
                        conversationServiceAsync.delete(c1).join();
                    }
                    String c2 = secondConvRef.get();
                    if (c2 != null) {
                        conversationServiceAsync.delete(c2).join();
                    }
                } catch (Exception ignored) {
                    // best-effort
                }
                try {
                    memoryStoresClient.deleteMemoryStore(memoryStoreName);
                    System.out.println("Memory store deleted");
                } catch (ResourceNotFoundException ignored) {
                    // no-op
                }
            })
            .doOnError(error -> System.err.println("Error: " + error.getMessage()))
            .timeout(Duration.ofSeconds(600))
            .block();
    }
}
