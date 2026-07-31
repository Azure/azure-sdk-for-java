// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaAgentsAsyncClient;
import com.azure.ai.agents.hostedagents.utils.HostedAgentsSampleUtils;
import com.azure.ai.agents.hostedagents.utils.HostedAgentsSampleUtils.HostedAgentSessionResources;
import com.azure.ai.agents.models.AgentSessionResource;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This sample demonstrates how to create, retrieve, list, and delete hosted-agent sessions using the async client.
 *
 * <p>Sessions are currently a preview feature and only work with hosted agents.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_AGENT_CONTAINER_IMAGE - The hosted-agent container image.</li>
 * </ul>
 */
public class SessionsAsyncSample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String image = Configuration.getGlobalConfiguration().get("FOUNDRY_AGENT_CONTAINER_IMAGE");
        String agentName = HostedAgentsSampleUtils.SAMPLE_AGENT_NAME;

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);
        AgentsAsyncClient agentsAsyncClient = builder.allowPreview(true).buildAgentsAsyncClient();
        BetaAgentsAsyncClient betaAgentsAsyncClient = builder.beta().buildBetaAgentsAsyncClient();

        AtomicReference<HostedAgentSessionResources> resourcesRef = new AtomicReference<>();

        Mono<Void> workflow = HostedAgentsSampleUtils.createAgentAndSessionAsync(agentsAsyncClient, betaAgentsAsyncClient, agentName, image)
            .flatMap(resources -> {
                resourcesRef.set(resources);
                AgentSessionResource session = resources.getSession();

                return betaAgentsAsyncClient.getSession(agentName, session.getAgentSessionId(), null)
                    .doOnNext(fetched -> System.out.printf("Retrieved session (id: %s, status: %s)%n",
                        fetched.getAgentSessionId(), fetched.getStatus()))
                    .thenMany(betaAgentsAsyncClient.listSessions(agentName, null, null, null, null, null)
                        .doOnSubscribe(unused -> System.out.println("Listing sessions for the agent..."))
                        .doOnNext(item -> System.out.printf("  - %s (status: %s)%n", item.getAgentSessionId(),
                            item.getStatus())))
                    .then(Mono.defer(() -> {
                        System.out.printf("Deleting session with id: %s...%n", session.getAgentSessionId());
                        return betaAgentsAsyncClient.deleteSession(agentName, session.getAgentSessionId(), null)
                            .doOnSuccess(unused -> System.out.printf("Session with id: %s deleted.%n",
                                session.getAgentSessionId()));
                    }));
            });

        workflow
            .onErrorResume(error -> HostedAgentsSampleUtils.cleanupAsync(agentsAsyncClient, betaAgentsAsyncClient, agentName,
                resourcesRef.get()).then(Mono.error(error)))
            .then(Mono.defer(() -> HostedAgentsSampleUtils.cleanupAsync(agentsAsyncClient, betaAgentsAsyncClient, agentName,
                resourcesRef.get())))
            .timeout(Duration.ofMinutes(15))
            .block();
    }
}
