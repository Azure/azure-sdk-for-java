// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesAsyncClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.FunctionTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This sample demonstrates how to create an agent with a Function Calling tool
 * that defines custom functions the agent can invoke, using the async client.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_DEPLOYMENT_NAME - The model deployment name.</li>
 * </ul>
 */
public class FunctionCallAsync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_DEPLOYMENT_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesAsyncClient = builder.buildResponsesAsyncClient();

        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();

        // Create a FunctionTool with parameters schema
        Map<String, BinaryData> parameters = new HashMap<String, BinaryData>();
        parameters.put("type", BinaryData.fromString("\"object\""));
        parameters.put("properties", BinaryData.fromString(
            "{\"location\":{\"type\":\"string\",\"description\":\"The city and state, e.g. Seattle, WA\"},"
            + "\"unit\":{\"type\":\"string\",\"enum\":[\"celsius\",\"fahrenheit\"]}}"));
        parameters.put("required", BinaryData.fromString("[\"location\", \"unit\"]"));
        parameters.put("additionalProperties", BinaryData.fromString("false"));

        FunctionTool tool = new FunctionTool("get_weather", parameters, true)
            .setDescription("Get the current weather in a given location");

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that can get weather information. "
                + "When asked about the weather, use the get_weather function to retrieve weather data.")
            .setTools(Collections.singletonList(tool));

        agentsAsyncClient.createAgentVersion("function-call-agent", agentDefinition)
            .flatMap(agent -> {
                agentRef.set(agent);
                System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

                AgentReference agentReference = new AgentReference(agent.getName())
                    .setVersion(agent.getVersion());

                return responsesAsyncClient.createWithAgent(agentReference,
                    ResponseCreateParams.builder()
                        .input("What's the weather like in Seattle?"));
            })
            .doOnNext(response -> {
                for (ResponseOutputItem outputItem : response.output()) {
                    if (outputItem.message().isPresent()) {
                        ResponseOutputMessage message = outputItem.message().get();
                        message.content().forEach(content -> {
                            content.outputText().ifPresent(text -> {
                                System.out.println("Assistant: " + text.text());
                            });
                        });
                    }

                    if (outputItem.functionCall().isPresent()) {
                        ResponseFunctionToolCall functionCall = outputItem.functionCall().get();
                        System.out.println("\n--- Function Tool Call ---");
                        System.out.println("Call ID: " + functionCall.callId());
                        System.out.println("Function Name: " + functionCall.name());
                        System.out.println("Arguments: " + functionCall.arguments());
                        System.out.println("Status: " + functionCall.status());
                    }
                }
            })
            .then(Mono.defer(() -> {
                AgentVersionDetails agent = agentRef.get();
                if (agent != null) {
                    return agentsAsyncClient.deleteAgentVersion(agent.getName(), agent.getVersion())
                        .doOnSuccess(v -> System.out.println("Agent deleted"));
                }
                return Mono.empty();
            }))
            .doOnError(error -> System.err.println("Error: " + error.getMessage()))
            .timeout(Duration.ofSeconds(30))
            .block();
    }
}
