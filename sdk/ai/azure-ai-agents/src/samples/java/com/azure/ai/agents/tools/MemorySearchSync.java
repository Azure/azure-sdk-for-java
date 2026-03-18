// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.MemorySearchPreviewTool;
import com.azure.ai.agents.models.MemoryStoreDefaultDefinition;
import com.azure.ai.agents.models.MemoryStoreDefaultOptions;
import com.azure.ai.agents.models.MemoryStoreDetails;
import com.azure.ai.agents.MemoryStoresClient;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.conversations.Conversation;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.services.blocking.ConversationService;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * This sample demonstrates how to integrate memory into a prompt agent
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
public class MemorySearchSync {
    private static final long MEMORY_WRITE_DELAY_SECONDS = 60;

    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String chatModel = Configuration.getGlobalConfiguration().get("AZURE_AI_CHAT_MODEL_DEPLOYMENT_NAME");
        String embeddingModel = Configuration.getGlobalConfiguration().get("AZURE_AI_EMBEDDING_MODEL_DEPLOYMENT_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        MemoryStoresClient memoryStoresClient = builder.buildMemoryStoresClient();
        ConversationService conversationService = builder.buildOpenAIClient().conversations();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        String memoryStoreName = "my_memory_store";
        String scope = "user_123";
        AgentVersionDetails agent = null;
        String firstConversationId = null;
        String followUpConversationId = null;

        try {
            // Delete memory store if it already exists
            deleteMemoryStoreQuietly(memoryStoresClient, memoryStoreName);

            // Create a memory store
            MemoryStoreDefaultDefinition definition = new MemoryStoreDefaultDefinition(chatModel, embeddingModel)
                .setOptions(new MemoryStoreDefaultOptions(true, true));

            MemoryStoreDetails memoryStore = memoryStoresClient.createMemoryStore(
                memoryStoreName, definition, "Example memory store for conversations", null);
            System.out.printf("Created memory store: %s (%s)%n", memoryStore.getName(), memoryStore.getId());

            // BEGIN: com.azure.ai.agents.define_memory_search
            // Create memory search tool
            MemorySearchPreviewTool tool = new MemorySearchPreviewTool(memoryStore.getName(), scope)
                .setUpdateDelaySeconds(1);
            // END: com.azure.ai.agents.define_memory_search

            // Create agent with memory search tool
            PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
                .setInstructions("You are a helpful assistant that answers general questions.")
                .setTools(Collections.singletonList(tool));

            agent = agentsClient.createAgentVersion("memory-search-agent", agentDefinition);
            System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            // First conversation: teach the agent a preference
            Conversation conversation = conversationService.create();
            firstConversationId = conversation.id();
            System.out.println("Created conversation (id: " + firstConversationId + ")");

            Response response = responsesClient.createWithAgentConversation(
                agentReference, firstConversationId,
                ResponseCreateParams.builder().input("I prefer dark roast coffee"));
            System.out.println("Response: " + getResponseText(response));

            // Wait for memories to be extracted and stored
            System.out.println("Waiting for memories to be stored...");
            TimeUnit.SECONDS.sleep(MEMORY_WRITE_DELAY_SECONDS);

            // Second conversation: test memory recall
            Conversation newConversation = conversationService.create();
            followUpConversationId = newConversation.id();
            System.out.println("Created new conversation (id: " + followUpConversationId + ")");

            Response followUpResponse = responsesClient.createWithAgentConversation(
                agentReference, followUpConversationId,
                ResponseCreateParams.builder().input("Please order my usual coffee"));
            System.out.println("Response: " + getResponseText(followUpResponse));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Sleep interrupted", e);
        } finally {
            deleteConversationQuietly(conversationService, firstConversationId);
            deleteConversationQuietly(conversationService, followUpConversationId);
            if (agent != null) {
                agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
                System.out.println("Agent deleted");
            }
            deleteMemoryStoreQuietly(memoryStoresClient, memoryStoreName);
        }
    }

    private static void deleteMemoryStoreQuietly(MemoryStoresClient client, String name) {
        try {
            client.deleteMemoryStore(name);
            System.out.println("Memory store deleted: " + name);
        } catch (ResourceNotFoundException ignored) {
            // no-op
        }
    }

    private static void deleteConversationQuietly(ConversationService client, String id) {
        if (id == null) {
            return;
        }
        try {
            client.delete(id);
        } catch (Exception ignored) {
            // best-effort cleanup
        }
    }

    private static String getResponseText(Response response) {
        if (response == null || response.output().isEmpty()) {
            return "<no output>";
        }
        try {
            return response.output().get(0).asMessage().content().get(0).asOutputText().text();
        } catch (RuntimeException ex) {
            return "<unavailable>";
        }
    }
}
