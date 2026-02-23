// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.memory;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.MemoryStoresClient;
import com.azure.ai.agents.models.MemoryStoreDefaultDefinition;
import com.azure.ai.agents.models.MemoryStoreDetails;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class CreateMemoryStore {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("AZURE_AGENTS_ENDPOINT");
        String chatModel = configuration.get("AZURE_AI_CHAT_MODEL_DEPLOYMENT_NAME");
        String embeddingModel = configuration.get("AZURE_AI_EMBEDDING_MODEL_DEPLOYMENT_NAME");

        // Code sample for creating a memory store
        MemoryStoresClient memoryStoresClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildMemoryStoresClient();

        String memoryStoreName = "my_memory_store_java";
        String description = "Example memory store for conversations";

        MemoryStoreDefaultDefinition definition = new MemoryStoreDefaultDefinition(chatModel, embeddingModel);
        MemoryStoreDetails memoryStore
                = memoryStoresClient.createMemoryStore(memoryStoreName, definition, description, null);

        System.out.println("Memory Store ID: " + memoryStore.getId());
        System.out.println("Memory Store Name: " + memoryStore.getName());
        System.out.println("Memory Store Description: " + memoryStore.getDescription());
    }
}
