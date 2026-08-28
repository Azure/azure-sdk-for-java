// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.memory;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaMemoryStoresAsyncClient;
import com.azure.ai.agents.models.ListMemoriesOptions;
import com.azure.ai.agents.models.MemoryItemKind;
import com.azure.ai.agents.models.MemoryStoreDefaultDefinition;
import com.azure.ai.agents.models.MemoryStoreDefaultOptions;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

/**
 * Sample demonstrating CRUD operations on memory store items (create, update, retrieve, list, delete) using the
 * asynchronous {@link BetaMemoryStoresAsyncClient}.
 *
 * <p>Memory stores are a preview feature. Before running, set the following environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 *   <li>{@code AZURE_AI_CHAT_MODEL_DEPLOYMENT_NAME} - a chat completion model deployment name.</li>
 *   <li>{@code AZURE_AI_EMBEDDING_MODEL_DEPLOYMENT_NAME} - an embedding model deployment name.</li>
 * </ul>
 */
public class MemoryStoreItemsAsyncSample {
    private static final String MEMORY_STORE_NAME = "memory_items_store_java";

    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String chatModel = configuration.get("AZURE_AI_CHAT_MODEL_DEPLOYMENT_NAME");
        String embeddingModel = configuration.get("AZURE_AI_EMBEDDING_MODEL_DEPLOYMENT_NAME");

        BetaMemoryStoresAsyncClient memoryStoresAsyncClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .beta()
            .buildBetaMemoryStoresAsyncClient();

        MemoryStoreDefaultDefinition definition = new MemoryStoreDefaultDefinition(chatModel, embeddingModel)
            .setOptions(new MemoryStoreDefaultOptions(true, true));

        String scope = "Store";

        // Clean up any pre-existing memory store, create it, then run the item lifecycle reactively.
        memoryStoresAsyncClient.deleteMemoryStore(MEMORY_STORE_NAME)
            .onErrorResume(ignored -> Mono.empty())
            .then(memoryStoresAsyncClient.createMemoryStore(
                MEMORY_STORE_NAME, definition, "Memory store demo.", null))
            .flatMap(memoryStore -> {
                System.out.printf("Memory store created (id: %s, name: %s)%n",
                    memoryStore.getId(), memoryStore.getName());
                String storeName = memoryStore.getName();

                return memoryStoresAsyncClient
                    .createMemory(storeName, scope, "The lover of oranges.", MemoryItemKind.USER_PROFILE)
                    .flatMap(customerData -> {
                        System.out.printf("Created memory item %s: %s%n",
                            customerData.getMemoryId(), customerData.getContent());
                        return memoryStoresAsyncClient
                            .createMemory(storeName, scope, "Orange SKU is 658954.", MemoryItemKind.CHAT_SUMMARY)
                            .flatMap(orangeSku -> {
                                System.out.printf("Created memory item %s: %s%n",
                                    orangeSku.getMemoryId(), orangeSku.getContent());
                                return memoryStoresAsyncClient
                                    .updateMemory(storeName, orangeSku.getMemoryId(), "Apple SKU is 786545.")
                                    .doOnNext(updated -> System.out.printf("Updated memory item %s, new content: %s%n",
                                        updated.getMemoryId(), updated.getContent()))
                                    .then(memoryStoresAsyncClient.getMemory(storeName, customerData.getMemoryId()))
                                    .doOnNext(fetched -> System.out.printf("Retrieved memory item %s: %s%n",
                                        fetched.getMemoryId(), fetched.getContent()))
                                    .thenMany(memoryStoresAsyncClient.listMemories(
                                        new ListMemoriesOptions(storeName, scope)))
                                    .doOnNext(item -> System.out.printf("    item %s: %s%n",
                                        item.getMemoryId(), item.getContent()))
                                    .then(memoryStoresAsyncClient.deleteMemory(storeName, customerData.getMemoryId())
                                        .doOnSuccess(unused -> System.out.printf("Deleted memory item %s%n",
                                            customerData.getMemoryId())))
                                    .then(memoryStoresAsyncClient.deleteMemory(storeName, orangeSku.getMemoryId())
                                        .doOnSuccess(unused -> System.out.printf("Deleted memory item %s%n",
                                            orangeSku.getMemoryId())))
                                    .then();
                            });
                    })
                    .then(memoryStoresAsyncClient.deleteMemoryStore(storeName))
                    .doOnSuccess(unused -> System.out.printf("Memory store deleted (name: %s)%n", storeName));
            })
            .block();
    }
}
