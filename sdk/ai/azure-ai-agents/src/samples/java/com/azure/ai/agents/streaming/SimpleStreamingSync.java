// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.streaming;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.core.util.IterableStream;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseStreamEvent;

/**
 * This sample demonstrates how to create a streaming response using the synchronous client.
 * Text is printed as it arrives rather than waiting for the full response.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 * </ul>
 */
public class SimpleStreamingSync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        AgentVersionDetails agent = null;

        try {
            // Create an agent
            PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
                .setInstructions("You are a helpful assistant that tells short, engaging stories.");

            agent = agentsClient.createAgentVersion("streaming-agent", agentDefinition);
            System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            // BEGIN: com.azure.ai.agents.streaming.simple_sync
            // Use ResponseAccumulator to collect streamed events into a final Response
            ResponseAccumulator responseAccumulator = ResponseAccumulator.create();

            // Stream response - text is printed as it arrives
            IterableStream<ResponseStreamEvent> events =
                responsesClient.createStreamingWithAgent(agentReference,
                    ResponseCreateParams.builder()
                        .input("Tell me a short story about a brave explorer."));

            for (ResponseStreamEvent event : events) {
                responseAccumulator.accumulate(event);
                event.outputTextDelta()
                    .ifPresent(textEvent -> System.out.print(textEvent.delta()));
            }
            System.out.println(); // newline after streamed text

            // Access the complete accumulated response
            Response response = responseAccumulator.response();
            System.out.println("\nResponse ID: " + response.id());
            // END: com.azure.ai.agents.streaming.simple_sync
        } finally {
            if (agent != null) {
                agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
                System.out.println("Agent deleted");
            }
        }
    }
}
