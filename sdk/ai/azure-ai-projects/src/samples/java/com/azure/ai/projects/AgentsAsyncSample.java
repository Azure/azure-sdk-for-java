// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.agents.persistent.AgentsServiceVersion;
import com.azure.ai.agents.persistent.PersistentAgentsAdministrationAsyncClient;
import com.azure.ai.agents.persistent.PersistentAgentsClientBuilder;
import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

public class AgentsAsyncSample {

    private static PersistentAgentsAdministrationAsyncClient agentsAsyncClient
        = new PersistentAgentsClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build())
        .serviceVersion(AgentsServiceVersion.V2025_05_15_PREVIEW)
        .buildPersistentAgentsAdministrationAsyncClient();

    public static void main(String[] args) {
        // Using block() to wait for the async operations to complete in the sample
        PersistentAgent createdAgent = createAgent().block();
        deleteAgent(createdAgent.getId()).block();
    }

    public static Mono<PersistentAgent> createAgent() {
        // BEGIN:com.azure.ai.projects.AgentsAsyncSample.createAgent

        String agentName = "basic_example";
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a helpful agent");
            
        return agentsAsyncClient.createAgent(createAgentOptions)
            .doOnNext(agent -> System.out.println("Agent created: " + agent.getId()));

        // END:com.azure.ai.projects.AgentsAsyncSample.createAgent
    }

    public static Mono<Boolean> deleteAgent(String agentId) {
        // BEGIN:com.azure.ai.projects.AgentsAsyncSample.deleteAgent

        return agentsAsyncClient.deleteAgent(agentId)
            .doOnNext(deletionStatus -> {
                System.out.println("Agent: " + agentId);
                System.out.println("Delete confirmation: " + deletionStatus);
            });

        // END:com.azure.ai.projects.AgentsAsyncSample.deleteAgent
    }
}
