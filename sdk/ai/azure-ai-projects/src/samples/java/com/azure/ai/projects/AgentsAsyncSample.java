// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.agents.persistent.PersistentAgentsAdministrationAsyncClient;
import com.azure.ai.agents.persistent.PersistentAgentsAsyncClient;
import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

public class AgentsAsyncSample {

    private static PersistentAgentsAsyncClient agentsAsyncClient
        = new AIProjectClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildPersistentAgentsAsyncClient();
    private static PersistentAgentsAdministrationAsyncClient administrationAsyncClient
        = agentsAsyncClient.getPersistentAgentsAdministrationAsyncClient();

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
            
        return administrationAsyncClient.createAgent(createAgentOptions)
            .doOnNext(agent -> System.out.println("Agent created: " + agent.getId()));

        // END:com.azure.ai.projects.AgentsAsyncSample.createAgent
    }

    public static Mono<Void> deleteAgent(String agentId) {
        // BEGIN:com.azure.ai.projects.AgentsAsyncSample.deleteAgent

        return administrationAsyncClient.deleteAgent(agentId)
            .doOnSuccess(aVoid -> System.out.println("Agent deleted: " + agentId));

        // END:com.azure.ai.projects.AgentsAsyncSample.deleteAgent
    }
}
