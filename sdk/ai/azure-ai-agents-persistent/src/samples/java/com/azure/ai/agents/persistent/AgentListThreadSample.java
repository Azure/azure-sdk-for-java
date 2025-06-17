// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.PersistentAgentThread;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class AgentListThreadSample {

    public static void main(String[] args) {
        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());
        PersistentAgentsClient agentsClient = clientBuilder.buildClient();
        ThreadsClient threadsClient = agentsClient.getThreadsClient();

        PagedIterable<PersistentAgentThread> threads = threadsClient.listThreads();
        for (PersistentAgentThread thread : threads) {
            System.out.printf("Found thread ID: %s%n", thread.getId());
        }
    }
}
