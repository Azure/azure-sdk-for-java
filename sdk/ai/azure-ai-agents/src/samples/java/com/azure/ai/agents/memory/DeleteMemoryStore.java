// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.memory;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaMemoryStoresClient;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class DeleteMemoryStore {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String memoryStoreName = "my_memory_store_java";

        // Code sample for deleting a memory store
        BetaMemoryStoresClient memoryStoresClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .beta().buildBetaMemoryStoresClient();

        memoryStoresClient.deleteMemoryStore(memoryStoreName);

        System.out.println("Deleted memory store: " + memoryStoreName);
    }
}
