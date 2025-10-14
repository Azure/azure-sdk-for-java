// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class AgentListThreadAsyncSample {

    public static void main(String[] args) {
        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());

        PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        ThreadsAsyncClient threadsAsyncClient = agentsAsyncClient.getThreadsAsyncClient();

        System.out.println("Listing threads asynchronously:");
        
        threadsAsyncClient.listThreads()
            .doOnNext(thread -> System.out.printf("Found thread ID: %s%n", thread.getId()))
            .doOnComplete(() -> System.out.println("Completed listing all threads"))
            .doOnError(error -> System.err.println("Error occurred: " + error.getMessage()))
            .blockLast(); // Block until all threads are processed
    }
}
