// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.memory;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.MemoryStoresClient;
import com.azure.ai.agents.models.MemoryStoreDetails;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class UpdateMemoryStore {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT");
        String memoryStoreName = "my_memory_store_java";

        // Code sample for updating a memory store
        MemoryStoresClient memoryStoresClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildMemoryStoresClient();

        String updatedDescription = "Updated description";
        MemoryStoreDetails updatedStore
                = memoryStoresClient.updateMemoryStore(memoryStoreName, updatedDescription, null);

        System.out.println("Memory Store ID: " + updatedStore.getId());
        System.out.println("Memory Store Name: " + updatedStore.getName());
        System.out.println("Memory Store Description: " + updatedStore.getDescription());
    }
}
