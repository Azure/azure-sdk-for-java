// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.agents;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.AgentKind;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.ExternalAgentDefinition;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrating create, retrieve, list, and delete operations for an external agent using the
 * asynchronous {@link AgentsAsyncClient}.
 *
 * <p>External agents are third-party agents hosted outside Foundry (for example, on GCP or AWS).
 * Registration is metadata-only. External agents are a preview feature, so {@code allowPreview(true)}
 * is required to add the {@code Foundry-Features: ExternalAgents=V1Preview} header.</p>
 *
 * <p>Before running the sample, set the {@code FOUNDRY_PROJECT_ENDPOINT} environment variable.</p>
 */
public class ExternalAgentAsyncSample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");

        AgentsAsyncClient agentsAsyncClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .allowPreview(true)
            .buildAgentsAsyncClient();

        String agentName = "myExternalAgent1";

        ExternalAgentDefinition agentDefinition = new ExternalAgentDefinition()
            .setOtelAgentId("sample-external-agent");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("sample", "external_agents_crud");
        metadata.put("status", "created");

        CreateAgentVersionInput input = new CreateAgentVersionInput(agentDefinition)
            .setDescription("External agent registered by the azure-ai-agents sample.")
            .setMetadata(metadata);

        // Clean up any pre-existing agent, then run the full lifecycle reactively.
        agentsAsyncClient.deleteAgent(agentName)
            .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
            .then(agentsAsyncClient.createAgentVersion(agentName, input))
            .flatMap(agentVersion -> {
                System.out.printf("Agent created (id: %s, name: %s, version: %s)%n",
                    agentVersion.getId(), agentVersion.getName(), agentVersion.getVersion());

                Mono<Void> retrieve = agentsAsyncClient.getAgent(agentVersion.getName())
                    .doOnNext(agent -> {
                        String latestVersion = agent.getVersions() == null || agent.getVersions().getLatest() == null
                            ? null : agent.getVersions().getLatest().getVersion();
                        System.out.printf("Agent retrieved (id: %s, name: %s, latest version: %s)%n",
                            agent.getId(), agent.getName(), latestVersion);
                    })
                    .then();

                Mono<Void> listVersions = agentsAsyncClient.listAgentVersions(agentVersion.getName())
                    .doOnNext(version -> System.out.printf("    - Agent id: %s, version: %s%n",
                        version.getId(), version.getVersion()))
                    .then();

                Mono<Void> listExternal = agentsAsyncClient
                    .listAgents(AgentKind.EXTERNAL, null, null, null, null)
                    .doOnNext(externalAgent -> System.out.printf("    - Agent id: %s, name: %s%n",
                        externalAgent.getId(), externalAgent.getName()))
                    .then();

                Mono<Void> delete = agentsAsyncClient.deleteAgent(agentVersion.getName())
                    .doOnSuccess(unused -> System.out.printf("Agent deleted (name: %s)%n", agentVersion.getName()));

                return retrieve.then(listVersions).then(listExternal).then(delete);
            })
            .block();
    }
}
