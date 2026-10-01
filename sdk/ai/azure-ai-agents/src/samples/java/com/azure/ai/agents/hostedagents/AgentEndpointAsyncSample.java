// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.hostedagents.utils.HostedAgentsSampleUtils;
import com.azure.ai.agents.hostedagents.utils.HostedAgentsSampleUtils.HostedAgentSessionResources;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClientAsync;
import com.openai.core.JsonValue;
import com.openai.models.responses.ResponseCreateParams;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This sample demonstrates invoking a hosted agent's default endpoint using the async OpenAI client.
 *
 * <p>Hosted-agent sessions are currently a preview feature.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_AGENT_CONTAINER_IMAGE - The hosted-agent container image.</li>
 * </ul>
 */
public class AgentEndpointAsyncSample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String image = Configuration.getGlobalConfiguration().get("FOUNDRY_AGENT_CONTAINER_IMAGE");
        String agentName = HostedAgentsSampleUtils.SAMPLE_AGENT_NAME;

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();
        AtomicReference<HostedAgentSessionResources> resourcesRef = new AtomicReference<>();

        Mono<Void> workflow = HostedAgentsSampleUtils.createAgentAndSessionAsync(agentsAsyncClient,
            agentName, image)
            .flatMap(resources -> {
                resourcesRef.set(resources);

                OpenAIClientAsync openAIAsyncClient = builder.buildAgentScopedOpenAIAsyncClient(agentName);

                return Mono.fromFuture(() -> openAIAsyncClient.responses().create(ResponseCreateParams.builder()
                        .input("What is the size of France in square miles?")
                        .putAdditionalBodyProperty("agent_session_id",
                            JsonValue.from(resources.getSession().getAgentSessionId()))
                        .build()))
                    .doOnNext(HostedAgentsSampleUtils::printResponseOutput)
                    .then();
            });

        workflow
            .onErrorResume(error -> HostedAgentsSampleUtils.cleanupAsync(agentsAsyncClient,
                agentName,
                resourcesRef.get()).then(Mono.error(error)))
            .then(Mono.defer(() -> HostedAgentsSampleUtils.cleanupAsync(agentsAsyncClient,
                agentName,
                resourcesRef.get())))
            .timeout(Duration.ofMinutes(15))
            .block();
    }
}
