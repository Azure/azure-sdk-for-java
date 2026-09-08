// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.streaming;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.ProtocolConfiguration;
import com.azure.ai.agents.models.ResponsesProtocolConfiguration;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClientAsync;
import com.openai.core.http.AsyncStreamResponse;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseStreamEvent;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This sample demonstrates how to create a streaming response using the asynchronous client.
 * Text is printed as it arrives rather than waiting for the full response.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 * </ul>
 */
public class SimpleStreamingAsync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();

        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();

        // Create an agent
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that tells short, engaging stories.");

        agentsAsyncClient.createAgentVersion("streaming-async-agent", agentDefinition)
            .flatMap(agent -> {
                agentRef.set(agent);
                System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

                AgentEndpointConfig endpointConfig = new AgentEndpointConfig()
                    .setVersionSelector(new VersionSelector().setVersionSelectionRules(Collections.singletonList(
                        new FixedRatioVersionSelectionRule(100).setAgentVersion(agent.getVersion()))))
                    .setProtocolConfiguration(new ProtocolConfiguration().setResponses(new ResponsesProtocolConfiguration()));
                OpenAIClientAsync openAIAsyncClient = builder.buildAgentScopedOpenAIAsyncClient(agent.getName());

                // BEGIN: com.azure.ai.agents.streaming.simple_async
                // OpenAI streaming events arrive through callbacks. This Mono only tracks terminal completion.
                Mono<Void> streamingCompletion = Mono.defer(() -> {
                    ResponseAccumulator responseAccumulator = ResponseAccumulator.create();
                    AsyncStreamResponse<ResponseStreamEvent> stream = openAIAsyncClient.responses().createStreaming(
                        ResponseCreateParams.builder()
                            .input("Tell me a short story about a brave explorer.")
                            .build());

                    stream.subscribe(event -> responseAccumulator.accumulate(event)
                        .outputTextDelta()
                        .ifPresent(textEvent -> System.out.print(textEvent.delta())));

                    return Mono.fromFuture(stream.onCompleteFuture())
                        .doOnSuccess(unused -> {
                            System.out.println(); // newline after streamed text

                            // Access the complete accumulated response
                            Response response = responseAccumulator.response();
                            System.out.println("\nResponse ID: " + response.id());
                        })
                        .doFinally(signal -> stream.close());
                });
                // END: com.azure.ai.agents.streaming.simple_async

                return agentsAsyncClient.updateAgentDetails(agent.getName(),
                    new UpdateAgentDetailsOptions().setAgentEndpoint(endpointConfig))
                    .then(streamingCompletion);
            })
            .then(Mono.defer(() -> {
                AgentVersionDetails agent = agentRef.get();
                if (agent != null) {
                    return agentsAsyncClient.deleteAgentVersion(agent.getName(), agent.getVersion())
                        .doOnSuccess(v -> System.out.println("Agent deleted"));
                }
                return Mono.empty();
            }))
            .block();
    }
}
