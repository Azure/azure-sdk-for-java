// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.streaming;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClientAsync;
import com.openai.core.http.AsyncStreamResponse;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseStreamEvent;
import reactor.core.publisher.Mono;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.ProtocolConfiguration;
import com.azure.ai.agents.models.ResponsesProtocolConfiguration;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;

import java.util.Collections;

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

        // Create an agent
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that tells short, engaging stories.");

        // Create the agent version and pin the agent endpoint to it. The endpoint URL identifies the agent,
        // so responses.createStreaming(...) below does not need to send an agent_reference in its body.
        String agentName = "streaming-async-agent";
        Mono.usingWhen(
                agentsAsyncClient.createAgentVersion(agentName, agentDefinition)
                    .flatMap(agent -> agentsAsyncClient.updateAgentDetails(agentName,
                            new UpdateAgentDetailsOptions().setAgentEndpoint(
                                new AgentEndpointConfig()
                                    .setVersionSelector(new VersionSelector().setVersionSelectionRules(Collections.singletonList(
                                        new FixedRatioVersionSelectionRule(100).setAgentVersion(agent.getVersion()))))
                                    .setProtocolConfiguration(new ProtocolConfiguration().setResponses(new ResponsesProtocolConfiguration()))))
                        .thenReturn(agent)),
                agent -> {
                    OpenAIClientAsync openAIAsyncClient
                        = builder.buildAgentScopedOpenAIAsyncClient(agentName);

                    // BEGIN: com.azure.ai.agents.streaming.simple_async
                    // Use ResponseAccumulator to collect streamed events into a final Response
                    ResponseAccumulator responseAccumulator = ResponseAccumulator.create();

                    // Stream response asynchronously - text is printed as each chunk arrives
                    AsyncStreamResponse<ResponseStreamEvent> stream = openAIAsyncClient.responses().createStreaming(
                        ResponseCreateParams.builder()
                            .input("Tell me a short story about a brave explorer.")
                            .build());

                    stream.subscribe(new AsyncStreamResponse.Handler<ResponseStreamEvent>() {
                        @Override
                        public void onNext(ResponseStreamEvent event) {
                            responseAccumulator.accumulate(event);
                            event.outputTextDelta()
                                .ifPresent(textEvent -> System.out.print(textEvent.delta()));
                        }

                        @Override
                        public void onComplete(java.util.Optional<Throwable> error) {
                            // No-op: onCompleteFuture below signals completion.
                        }
                    });

                    return Mono.fromFuture(stream.onCompleteFuture())
                        .doFinally(signal -> stream.close())
                        .doOnSuccess(unused -> {
                            System.out.println(); // newline after streamed text

                            // Access the complete accumulated response
                            Response response = responseAccumulator.response();
                            System.out.println("\nResponse ID: " + response.id());
                        });
                    // END: com.azure.ai.agents.streaming.simple_async
                },
                agent -> agentsAsyncClient.deleteAgentVersion(agentName, agent.getVersion()))
            .block();
    }
}
