// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.memory;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaMemoryStoresClient;
import com.azure.ai.agents.models.ListMemoriesOptions;
import com.azure.ai.agents.models.MemoryItem;
import com.azure.ai.agents.models.MemoryItemKind;
import com.azure.ai.agents.models.MemoryStoreDefaultDefinition;
import com.azure.ai.agents.models.MemoryStoreDefaultOptions;
import com.azure.ai.agents.models.MemoryStoreDetails;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Sample demonstrating CRUD operations on memory store items (create, update, retrieve, list, delete) using the
 * synchronous {@link BetaMemoryStoresClient}.
 *
 * <p>Memory stores are a preview feature. Before running, set the following environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 *   <li>{@code AZURE_AI_CHAT_MODEL_DEPLOYMENT_NAME} - a chat completion model deployment name.</li>
 *   <li>{@code AZURE_AI_EMBEDDING_MODEL_DEPLOYMENT_NAME} - an embedding model deployment name.</li>
 * </ul>
 */
public class MemoryStoreItemsSample {
    private static final String MEMORY_STORE_NAME = "memory_items_store_java";

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

        // Clean up any pre-existing memory store with the same name.
        try {
            memoryStoresClient.deleteMemoryStore(MEMORY_STORE_NAME);
        } catch (RuntimeException ignored) {
            // The sample memory store does not already exist.
        }

        // Create the memory store. A memory store requires a chat model and an embedding model.
        MemoryStoreDefaultDefinition definition = new MemoryStoreDefaultDefinition(chatModel, embeddingModel)
            .setOptions(new MemoryStoreDefaultOptions(true, true));
        MemoryStoreDetails memoryStore
            = memoryStoresClient.createMemoryStore(MEMORY_STORE_NAME, definition, "Memory store demo.", null);
        System.out.printf("Memory store created (id: %s, name: %s)%n", memoryStore.getId(), memoryStore.getName());

        String scope = "Store";
        try {
            // BEGIN:com.azure.ai.agents.memory.MemoryStoreItemsSample.createItems
            MemoryItem customerData = memoryStoresClient.createMemory(
                memoryStore.getName(), scope, "The lover of oranges.", MemoryItemKind.USER_PROFILE);
            MemoryItem orangeSku = memoryStoresClient.createMemory(
                memoryStore.getName(), scope, "Orange SKU is 658954.", MemoryItemKind.CHAT_SUMMARY);
            System.out.printf("Created memory item %s: %s%n", customerData.getMemoryId(), customerData.getContent());
            System.out.printf("Created memory item %s: %s%n", orangeSku.getMemoryId(), orangeSku.getContent());
            // END:com.azure.ai.agents.memory.MemoryStoreItemsSample.createItems

            // Update a memory item.
            MemoryItem updated = memoryStoresClient.updateMemory(
                memoryStore.getName(), orangeSku.getMemoryId(), "Apple SKU is 786545.");
            System.out.printf("Updated memory item %s, new content: %s%n", updated.getMemoryId(), updated.getContent());

            // Retrieve a memory item.
            MemoryItem fetched = memoryStoresClient.getMemory(memoryStore.getName(), customerData.getMemoryId());
            System.out.printf("Retrieved memory item %s: %s%n", fetched.getMemoryId(), fetched.getContent());

            // List memory items in the scope.
            System.out.printf("Listing memory items from %s%n", memoryStore.getName());
            for (MemoryItem item : memoryStoresClient.listMemories(
                    new ListMemoriesOptions(memoryStore.getName(), scope))) {
                System.out.printf("    item %s: %s%n", item.getMemoryId(), item.getContent());
            }

            // BEGIN:com.azure.ai.agents.memory.MemoryStoreItemsSample.deleteItems
            memoryStoresClient.deleteMemory(memoryStore.getName(), customerData.getMemoryId());
            System.out.printf("Deleted memory item %s%n", customerData.getMemoryId());
            memoryStoresClient.deleteMemory(memoryStore.getName(), orangeSku.getMemoryId());
            System.out.printf("Deleted memory item %s%n", orangeSku.getMemoryId());
            // END:com.azure.ai.agents.memory.MemoryStoreItemsSample.deleteItems
        } finally {
            // Delete the memory store to clean up.
            memoryStoresClient.deleteMemoryStore(memoryStore.getName());
            System.out.printf("Memory store deleted (name: %s)%n", memoryStore.getName());
        }
    }
}
