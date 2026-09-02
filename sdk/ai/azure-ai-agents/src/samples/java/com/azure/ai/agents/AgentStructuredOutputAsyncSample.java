// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.ResponseCreateParams;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Demonstrates asynchronously generating a response that conforms to a JSON schema.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class AgentStructuredOutputAsyncSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);
        AgentsAsyncClient agentsClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesClient = builder.buildResponsesAsyncClient();
        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();

        agentsClient.createAgentVersion("structured-output-async-agent",
                new CreateAgentVersionInput(AgentStructuredOutputSample.createDefinition(model)))
            .doOnNext(agentRef::set)
            .flatMap(agent -> responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(SampleUtils.toAgentReference(agent)),
                ResponseCreateParams.builder()
                    .input("Alice and Bob are going to a science fair on 2026-11-07.")))
            .doOnNext(SampleUtils::printResponseText)
            .then(Mono.defer(() -> {
                AgentVersionDetails agent = agentRef.get();
                return agent == null ? Mono.empty()
                    : agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
            }))
            .block();
    }
}
