// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.hostedagents.utils.HostedAgentsSampleUtils;
import com.azure.ai.agents.hostedagents.utils.HostedAgentsSampleUtils.HostedAgentSessionResources;
import com.azure.ai.agents.models.AgentSessionResource;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This sample demonstrates disabling and enabling a hosted agent using the async client.
 *
 * <p>When disabled, a hosted agent cannot accept new sessions. Before running, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Foundry project endpoint.</li>
 *   <li>FOUNDRY_AGENT_CONTAINER_IMAGE - The hosted-agent container image.</li>
 * </ul>
 */
public class HostedAgentDisableAsyncSample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String image = Configuration.getGlobalConfiguration().get("FOUNDRY_AGENT_CONTAINER_IMAGE");
        String agentName = HostedAgentsSampleUtils.SAMPLE_AGENT_NAME;

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .allowPreview(true);
        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();

        AtomicReference<HostedAgentSessionResources> resourcesRef = new AtomicReference<>();
        AtomicReference<AgentSessionResource> additionalSessionRef = new AtomicReference<>();
        AtomicBoolean agentDisabled = new AtomicBoolean();

        Mono<Void> workflow = agentsAsyncClient.enableAgent(agentName)
            .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
            .then(HostedAgentsSampleUtils.createAgentAndSessionAsync(agentsAsyncClient, agentName, image))
            .flatMap(resources -> {
                resourcesRef.set(resources);
                String agentVersion = resources.getAgent().getVersion();

                // BEGIN: com.azure.ai.agents.hostedagents.HostedAgentDisableAsyncSample.disableAgent

                return agentsAsyncClient.disableAgent(agentName)
                    .doOnSuccess(unused -> agentDisabled.set(true))
                    .then(HostedAgentsSampleUtils.createSessionAsync(agentsAsyncClient, agentName, agentVersion)
                        .doOnNext(additionalSessionRef::set)
                        .flatMap(unused -> Mono.error(new IllegalStateException(
                            "A disabled agent unexpectedly created a session.")))
                        .onErrorResume(HttpResponseException.class, ex -> {
                            if (ex.getResponse().getStatusCode() != 403) {
                                return Mono.error(ex);
                            }
                            System.out.println(
                                "Creating a session for the disabled agent failed with HTTP 403 as expected.");
                            return Mono.empty();
                        }))

                    // END: com.azure.ai.agents.hostedagents.HostedAgentDisableAsyncSample.disableAgent
                    // BEGIN: com.azure.ai.agents.hostedagents.HostedAgentDisableAsyncSample.enableAgent

                    .then(agentsAsyncClient.enableAgent(agentName))
                    .doOnSuccess(unused -> agentDisabled.set(false))
                    .then(HostedAgentsSampleUtils.createSessionAsync(agentsAsyncClient, agentName, agentVersion)
                        .doOnNext(additionalSessionRef::set))
                    .then();

                    // END: com.azure.ai.agents.hostedagents.HostedAgentDisableAsyncSample.enableAgent
            });

        workflow
            .onErrorResume(error -> cleanupAsync(agentsAsyncClient, agentName, agentDisabled, additionalSessionRef,
                resourcesRef).then(Mono.error(error)))
            .then(cleanupAsync(agentsAsyncClient, agentName, agentDisabled, additionalSessionRef, resourcesRef))
            .timeout(Duration.ofMinutes(15))
            .block();
    }

    private static Mono<Void> cleanupAsync(AgentsAsyncClient agentsAsyncClient, String agentName,
        AtomicBoolean agentDisabled, AtomicReference<AgentSessionResource> additionalSessionRef,
        AtomicReference<HostedAgentSessionResources> resourcesRef) {
        Mono<Void> restoreAgent = Mono.defer(() -> {
            if (!agentDisabled.get()) {
                return Mono.empty();
            }
            return agentsAsyncClient.enableAgent(agentName)
                .doOnSuccess(unused -> agentDisabled.set(false))
                .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
                .onErrorResume(error -> {
                    System.err.println("Unable to restore the agent to the enabled state: " + error.getMessage());
                    return Mono.empty();
                });
        });

        Mono<Void> deleteSession = Mono.defer(() -> {
            AgentSessionResource session = additionalSessionRef.get();
            if (session == null) {
                return Mono.empty();
            }
            return agentsAsyncClient.deleteSession(agentName, session.getAgentSessionId())
                .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty());
        });

        return restoreAgent
            .then(deleteSession)
            .then(HostedAgentsSampleUtils.cleanupAsync(agentsAsyncClient, agentName, resourcesRef.get()));
    }
}
