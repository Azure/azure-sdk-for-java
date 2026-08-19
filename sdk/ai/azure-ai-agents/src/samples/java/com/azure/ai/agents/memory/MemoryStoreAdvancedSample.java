// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.memory;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaMemoryStoresClient;
import com.azure.ai.agents.models.MemoryOperation;
import com.azure.ai.agents.models.MemorySearchItem;
import com.azure.ai.agents.models.MemorySearchOptions;
import com.azure.ai.agents.models.MemoryStoreDefaultDefinition;
import com.azure.ai.agents.models.MemoryStoreDefaultOptions;
import com.azure.ai.agents.models.MemoryStoreDetails;
import com.azure.ai.agents.models.MemoryStoreSearchResponse;
import com.azure.ai.agents.models.MemoryStoreUpdateCompletedResult;
import com.azure.ai.agents.models.MemoryStoreUpdateResponse;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.ResponseInputItem;

import java.time.Duration;
import java.util.Arrays;

/**
 * Sample demonstrating conversational memory store operations using the synchronous
 * {@link BetaMemoryStoresClient}.
 *
 * <p>Memory stores are a preview feature. Before running, set the following environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 *   <li>{@code AZURE_AI_CHAT_MODEL_DEPLOYMENT_NAME} - a chat completion model deployment name.</li>
 *   <li>{@code AZURE_AI_EMBEDDING_MODEL_DEPLOYMENT_NAME} - an embedding model deployment name.</li>
 * </ul>
 */
public class MemoryStoreAdvancedSample {
    private static final String MEMORY_STORE_NAME = "memory_advanced_store_java_sync";
    private static final Duration POLL_TIMEOUT = Duration.ofMinutes(3);

    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String chatModel = configuration.get("AZURE_AI_CHAT_MODEL_DEPLOYMENT_NAME");
        String embeddingModel = configuration.get("AZURE_AI_EMBEDDING_MODEL_DEPLOYMENT_NAME");

        BetaMemoryStoresClient memoryStoresClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .beta()
            .buildBetaMemoryStoresClient();

        try {
            memoryStoresClient.deleteMemoryStore(MEMORY_STORE_NAME);
        } catch (RuntimeException ignored) {
            // The sample memory store does not already exist.
        }

        MemoryStoreDefaultOptions options = new MemoryStoreDefaultOptions(true, true)
            .setUserProfileDetails("Preferences and interests relevant to a coffee expert agent");
        MemoryStoreDefaultDefinition definition = new MemoryStoreDefaultDefinition(chatModel, embeddingModel)
            .setOptions(options);
        MemoryStoreDetails memoryStore
            = memoryStoresClient.createMemoryStore(MEMORY_STORE_NAME, definition,
                "Example memory store for conversations", null);

        String scope = "user_123";
        try {
            ResponseInputItem initialMessage = ResponseInputItem.ofEasyInputMessage(
                EasyInputMessage.builder()
                    .role(EasyInputMessage.Role.USER)
                    .content("I prefer dark roast coffee and usually drink it in the morning")
                    .type(EasyInputMessage.Type.MESSAGE)
                    .build());

            SyncPoller<MemoryStoreUpdateResponse, MemoryStoreUpdateCompletedResult> initialPoller
                = memoryStoresClient.beginUpdateMemories(
                    memoryStore.getName(), scope, Arrays.asList(initialMessage), null, 300);
            MemoryStoreUpdateResponse initialResponse = initialPoller.poll().getValue();
            System.out.printf("Scheduled memory update (id: %s, status: %s)%n",
                initialResponse.getUpdateId(), initialResponse.getStatus());

            ResponseInputItem chainedMessage = ResponseInputItem.ofEasyInputMessage(
                EasyInputMessage.builder()
                    .role(EasyInputMessage.Role.USER)
                    .content("I also like cappuccinos in the afternoon")
                    .type(EasyInputMessage.Type.MESSAGE)
                    .build());
            SyncPoller<MemoryStoreUpdateResponse, MemoryStoreUpdateCompletedResult> chainedPoller
                = memoryStoresClient.beginUpdateMemories(
                    memoryStore.getName(), scope, Arrays.asList(chainedMessage),
                    initialResponse.getUpdateId(), 0);
            MemoryStoreUpdateResponse chainedResponse = chainedPoller.poll().getValue();
            System.out.printf("Scheduled chained memory update (id: %s, status: %s)%n",
                chainedResponse.getUpdateId(), chainedResponse.getStatus());

            chainedPoller.waitForCompletion(POLL_TIMEOUT);
            MemoryStoreUpdateCompletedResult updateResult = chainedPoller.getFinalResult();
            System.out.printf("Memory update completed with %d operations%n",
                updateResult.getMemoryOperations().size());
            for (MemoryOperation operation : updateResult.getMemoryOperations()) {
                System.out.printf("  - Operation: %s, memory ID: %s, content: %s%n",
                    operation.getKind(), operation.getMemoryItem().getMemoryId(),
                    operation.getMemoryItem().getContent());
            }

            MemorySearchOptions searchOptions = new MemorySearchOptions().setMaxMemories(5);
            ResponseInputItem searchQuery = ResponseInputItem.ofEasyInputMessage(
                EasyInputMessage.builder()
                    .role(EasyInputMessage.Role.USER)
                    .content("What are my morning coffee preferences?")
                    .type(EasyInputMessage.Type.MESSAGE)
                    .build());
            MemoryStoreSearchResponse searchResponse = memoryStoresClient.searchMemories(
                memoryStore.getName(), scope, Arrays.asList(searchQuery), null, searchOptions);
            printSearchResults(searchResponse);

            ResponseInputItem agentMessage = ResponseInputItem.ofEasyInputMessage(
                EasyInputMessage.builder()
                    .role(EasyInputMessage.Role.ASSISTANT)
                    .content("You previously indicated a preference for dark roast coffee in the morning.")
                    .type(EasyInputMessage.Type.MESSAGE)
                    .build());
            ResponseInputItem followupQuery = ResponseInputItem.ofEasyInputMessage(
                EasyInputMessage.builder()
                    .role(EasyInputMessage.Role.USER)
                    .content("What about afternoon?")
                    .type(EasyInputMessage.Type.MESSAGE)
                    .build());
            MemoryStoreSearchResponse followupSearchResponse = memoryStoresClient.searchMemories(
                memoryStore.getName(), scope, Arrays.asList(agentMessage, followupQuery),
                searchResponse.getSearchId(), searchOptions);
            printSearchResults(followupSearchResponse);

            memoryStoresClient.deleteScope(memoryStore.getName(), scope);
            System.out.printf("Deleted memories for scope '%s'%n", scope);
        } finally {
            memoryStoresClient.deleteMemoryStore(memoryStore.getName());
            System.out.printf("Memory store deleted (name: %s)%n", memoryStore.getName());
        }
    }

    private static void printSearchResults(MemoryStoreSearchResponse searchResponse) {
        System.out.printf("Found %d memories (search ID: %s)%n",
            searchResponse.getMemories().size(), searchResponse.getSearchId());
        for (MemorySearchItem memory : searchResponse.getMemories()) {
            System.out.printf("  - Memory ID: %s, content: %s%n",
                memory.getMemoryItem().getMemoryId(), memory.getMemoryItem().getContent());
        }
    }
}
