// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.memory;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.MemoryStoresClient;
import com.azure.ai.agents.models.MemoryStoreDetails;
import com.azure.ai.agents.models.PageOrder;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class ListMemoryStores {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT");

        // Code sample for listing all memory stores
        MemoryStoresClient memoryStoresClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildMemoryStoresClient();

        System.out.println("Listing all memory stores:");
        for (MemoryStoreDetails store : memoryStoresClient.listMemoryStores(10, PageOrder.DESC, null, null)) {
            System.out.println("Memory Store ID: " + store.getId());
            System.out.println("Memory Store Name: " + store.getName());
            System.out.println("Memory Store Description: " + store.getDescription());
            System.out.println("---");
        }
    }
}
