// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.memory;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.MemoryStoresClient;
import com.azure.ai.agents.models.DeleteMemoryStoreResult;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class DeleteMemoryStore {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT");
        String memoryStoreName = "my_memory_store_java";

        // Code sample for deleting a memory store
        MemoryStoresClient memoryStoresClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildMemoryStoresClient();

        DeleteMemoryStoreResult deleteResult = memoryStoresClient.deleteMemoryStore(memoryStoreName);

        System.out.println("Deleted memory store with the following details:");
        System.out.println("\tMemory Store Name: " + memoryStoreName);
        System.out.println("\tMemory Store was deleted: " + deleteResult.isDeleted());
    }
}
