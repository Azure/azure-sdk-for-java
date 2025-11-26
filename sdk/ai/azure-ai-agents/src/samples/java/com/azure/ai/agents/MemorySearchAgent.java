// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.MemorySearchTool;
import com.azure.ai.agents.models.MemoryStoreDefaultDefinition;
import com.azure.ai.agents.models.MemoryStoreDefaultOptions;
import com.azure.ai.agents.models.MemoryStoreDetails;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.conversations.Conversation;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Sample showing how to pair an agent with a memory search tool so it can recall earlier user preferences.
 */
public class MemorySearchAgent {
    private static final long MEMORY_WRITE_DELAY_SECONDS = 60;

    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("AZURE_AGENTS_ENDPOINT");
        String agentModel = configuration.get("AZURE_AGENT_MODEL");
        String chatModel = configuration.get("AZURE_AI_CHAT_MODEL_DEPLOYMENT_NAME");
        String embeddingModel = configuration.get("AZURE_AI_EMBEDDING_MODEL_DEPLOYMENT_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .serviceVersion(AgentsServiceVersion.V2025_05_15_PREVIEW);

        AgentsClient agentsClient = builder.buildAgentsClient();
        MemoryStoresClient memoryStoresClient = builder.buildMemoryStoresClient();
        ConversationsClient conversationsClient = builder.buildConversationsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        String memoryStoreName = "my_memory_store";
        String agentName = "MyAgent";
        String description = "Example memory store for conversations";
        String scope = "user_123";

        AgentVersionDetails agent = null;
        String firstConversationId = null;
        String followUpConversationId = null;

        try {
            cleanupMemoryStore(memoryStoresClient, memoryStoreName);

            MemoryStoreDefaultDefinition definition = new MemoryStoreDefaultDefinition(chatModel, embeddingModel)
                    .setOptions(new MemoryStoreDefaultOptions(true, true));

            MemoryStoreDetails memoryStore
                    = memoryStoresClient.createMemoryStore(memoryStoreName, definition, description, null);
            System.out.printf("Created memory store: %s (%s)\n", memoryStore.getName(), memoryStore.getId());

            MemorySearchTool tool = new MemorySearchTool(memoryStore.getName(), scope)
                    .setUpdateDelay(1); // Wait 1 second of inactivity before extracting memories

            PromptAgentDefinition agentDefinition = new PromptAgentDefinition(agentModel)
                    .setInstructions("You are a helpful assistant that answers general questions.")
                    .setTools(Collections.singletonList(tool));

            agent = agentsClient.createAgentVersion(agentName, agentDefinition);
            System.out.printf("Agent created (id: %s, version: %s)\n", agent.getId(), agent.getVersion());

            AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

            Conversation conversation = conversationsClient.getConversationService().create();
            firstConversationId = conversation.id();
            System.out.println("Created conversation (id: " + firstConversationId + ")");


            Response response = responsesClient.createWithAgentConversation(agentReference, firstConversationId,
                    ResponseCreateParams.builder().input("I prefer dark roast coffee"));
            System.out.println("Response output: " + getResponseText(response));

            System.out.println("Waiting for memories to be stored...");
            sleepSeconds(MEMORY_WRITE_DELAY_SECONDS);

            Conversation newConversation = conversationsClient.getConversationService().create();
            followUpConversationId = newConversation.id();
            System.out.println("Created new conversation (id: " + followUpConversationId + ")");

            Response followUpResponse = responsesClient.createWithAgentConversation(agentReference,
                    followUpConversationId, ResponseCreateParams.builder().input("Please order my usual coffee"));
            System.out.println("Response output: " + getResponseText(followUpResponse));

            System.out.println("Sample completed successfully.");
        } finally {
            deleteConversation(conversationsClient, firstConversationId);
            deleteConversation(conversationsClient, followUpConversationId);
            if (agent != null) {
                agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
                System.out.println("Agent deleted");
            }
            cleanupMemoryStore(memoryStoresClient, memoryStoreName);
        }
    }

    private static void cleanupMemoryStore(MemoryStoresClient memoryStoresClient, String memoryStoreName) {
        if (memoryStoreName == null) {
            return;
        }
        try {
            memoryStoresClient.deleteMemoryStore(memoryStoreName);
            System.out.println("Memory store `" + memoryStoreName + "` deleted");
        } catch (ResourceNotFoundException ignored) {
            // no-op
        }
    }

    private static void deleteConversation(ConversationsClient conversationsClient, String conversationId) {
        if (conversationId == null) {
            return;
        }
        try {
            conversationsClient.getConversationService().delete(conversationId);
            System.out.println("Conversation deleted (id: " + conversationId + ")");
        } catch (Exception ignored) {
            // best-effort cleanup
        }
    }

    private static void sleepSeconds(long seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Sleep interrupted", e);
        }
    }

    private static String getResponseText(Response response) {
        if (response == null || response.output().isEmpty()) {
            return "<no output>";
        }
        try {
            if (response.output().get(0).asMessage().content().isEmpty()) {
                return "<no output>";
            }
            return response.output().get(0).asMessage().content().get(0).asOutputText().text();
        } catch (RuntimeException ex) {
            return "<unavailable>";
        }
    }
}
