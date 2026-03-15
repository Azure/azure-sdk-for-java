// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.streaming;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesAsyncClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

/**
 * This sample demonstrates how to create a streaming response using the asynchronous client.
 * Text is printed as it arrives rather than waiting for the full response.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_DEPLOYMENT_NAME - The model deployment name.</li>
 * </ul>
 */
public class SimpleStreamingAsync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_DEPLOYMENT_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesAsyncClient = builder.buildResponsesAsyncClient();

        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();

        // Create an agent
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that tells short, engaging stories.");

        agentsAsyncClient.createAgentVersion("streaming-async-agent", agentDefinition)
            .flatMap(agent -> {
                agentRef.set(agent);
                System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

                AgentReference agentReference = new AgentReference(agent.getName())
                    .setVersion(agent.getVersion());

                // BEGIN: com.azure.ai.agents.streaming.simple_async
                // Use ResponseAccumulator to collect streamed events into a final Response
                ResponseAccumulator responseAccumulator = ResponseAccumulator.create();

                // Stream response asynchronously - text is printed as each chunk arrives
                return responsesAsyncClient.createStreamingWithAgent(agentReference,
                        ResponseCreateParams.builder()
                            .input("Tell me a short story about a brave explorer."))
                    .doOnNext(event -> {
                        responseAccumulator.accumulate(event);
                        event.outputTextDelta()
                            .ifPresent(textEvent -> System.out.print(textEvent.delta()));
                    })
                    .then(Mono.fromCallable(() -> {
                        System.out.println(); // newline after streamed text

                        // Access the complete accumulated response
                        Response response = responseAccumulator.response();
                        System.out.println("\nResponse ID: " + response.id());
                        // END: com.azure.ai.agents.streaming.simple_async
                        return response;
                    }));
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
