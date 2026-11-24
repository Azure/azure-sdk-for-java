// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.hostedagents.utils.HostedAgentsSampleUtils;
import com.azure.ai.agents.models.AgentSessionResource;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;
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
    private static final String AGENT_NAME = "java-disable-async-" + UUID.randomUUID().toString().substring(0, 8);
    private static final Duration WORKFLOW_TIMEOUT = Duration.ofMinutes(5);
    private static final Duration CLEANUP_TIMEOUT = Duration.ofMinutes(1);

    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String image = Configuration.getGlobalConfiguration().get("FOUNDRY_AGENT_CONTAINER_IMAGE");
        String agentName = AGENT_NAME;

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .allowPreview(true);
        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();

        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();
        AtomicReference<AgentSessionResource> unexpectedSessionRef = new AtomicReference<>();
        AtomicBoolean agentDisabled = new AtomicBoolean();

        Mono<Void> workflow = agentsAsyncClient.enableAgent(agentName)
            .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
            .then(HostedAgentsSampleUtils.createActiveHostedAgentVersionAsync(agentsAsyncClient, agentName, image))
            .flatMap(agent -> {
                agentRef.set(agent);
                String agentVersion = agent.getVersion();

                // BEGIN: com.azure.ai.agents.hostedagents.HostedAgentDisableAsyncSample.disableAgent

                return agentsAsyncClient.disableAgent(agentName)
                    .doOnSuccess(unused -> agentDisabled.set(true))
                    .then(HostedAgentsSampleUtils.createSessionAsync(agentsAsyncClient, agentName, agentVersion)
                        .doOnNext(unexpectedSessionRef::set)
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
                    .then();

                    // END: com.azure.ai.agents.hostedagents.HostedAgentDisableAsyncSample.enableAgent
            });

        workflow.timeout(WORKFLOW_TIMEOUT)
            .onErrorResume(error -> Mono.defer(() -> cleanupAsync(agentsAsyncClient, agentName, agentDisabled,
                unexpectedSessionRef, agentRef)).then(Mono.error(error)))
            .then(Mono.defer(() -> cleanupAsync(agentsAsyncClient, agentName, agentDisabled, unexpectedSessionRef,
                agentRef)))
            .block();
    }

    private static Mono<Void> cleanupAsync(AgentsAsyncClient agentsAsyncClient, String agentName,
        AtomicBoolean agentDisabled, AtomicReference<AgentSessionResource> unexpectedSessionRef,
        AtomicReference<AgentVersionDetails> agentRef) {
        Mono<Void> restoreAgent = Mono.defer(() -> {
            if (!agentDisabled.get()) {
                return Mono.empty();
            }
            return agentsAsyncClient.enableAgent(agentName)
                .doOnSuccess(unused -> agentDisabled.set(false))
                .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
                .timeout(CLEANUP_TIMEOUT)
                .onErrorResume(error -> {
                    System.err.println("Unable to restore the agent to the enabled state: " + error.getMessage());
                    return Mono.empty();
                });
        });

        Mono<Void> deleteSession = Mono.defer(() -> {
            AgentSessionResource session = unexpectedSessionRef.get();
            if (session == null) {
                return Mono.empty();
            }
            return agentsAsyncClient.deleteSession(agentName, session.getAgentSessionId())
                .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
                .timeout(CLEANUP_TIMEOUT)
                .onErrorResume(error -> {
                    System.err.println("Unable to delete the unexpected session: " + error.getMessage());
                    return Mono.empty();
                });
        });

        return restoreAgent
            .then(deleteSession)
            .then(Mono.defer(() -> {
                AgentVersionDetails agent = agentRef.get();
                if (agent == null) {
                    return Mono.empty();
                }
                return agentsAsyncClient.deleteAgentVersion(agentName, agent.getVersion())
                    .doOnSuccess(unused -> System.out.printf("Agent version %s deleted.%n", agent.getVersion()));
            })
                .timeout(CLEANUP_TIMEOUT)
                .onErrorResume(error -> {
                    System.err.println("Unable to finish hosted-agent cleanup: " + error.getMessage());
                    return Mono.empty();
                }));
    }
}
