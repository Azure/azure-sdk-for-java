// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.streaming;

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
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This sample demonstrates how to stream a response from an agent configured with a
 * Function Calling tool using the asynchronous client. Function call arguments and text
 * output are printed as they arrive.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_DEPLOYMENT_NAME - The model deployment name.</li>
 * </ul>
 */
public class FunctionCallStreamingAsync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_DEPLOYMENT_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesAsyncClient = builder.buildResponsesAsyncClient();

        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();

        // Define a function tool with parameter schema
        Map<String, Object> locationProp = new LinkedHashMap<>();
        locationProp.put("type", "string");
        locationProp.put("description", "The city and state, e.g. Seattle, WA");

        Map<String, Object> unitProp = new LinkedHashMap<>();
        unitProp.put("type", "string");
        unitProp.put("enum", Arrays.asList("celsius", "fahrenheit"));

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("location", locationProp);
        properties.put("unit", unitProp);

        Map<String, BinaryData> parameters = new HashMap<>();
        parameters.put("type", BinaryData.fromObject("object"));
        parameters.put("properties", BinaryData.fromObject(properties));
        parameters.put("required", BinaryData.fromObject(Arrays.asList("location", "unit")));
        parameters.put("additionalProperties", BinaryData.fromObject(false));

        FunctionTool tool = new FunctionTool("get_weather", parameters, true)
            .setDescription("Get the current weather in a given location");

        // Create agent with function tool
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that can get weather information. "
                + "When asked about the weather, use the get_weather function.")
            .setTools(Collections.singletonList(tool));

        agentsAsyncClient.createAgentVersion("function-streaming-async-agent", agentDefinition)
            .flatMap(agent -> {
                agentRef.set(agent);
                System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

                AgentReference agentReference = new AgentReference(agent.getName())
                    .setVersion(agent.getVersion());

                // BEGIN: com.azure.ai.agents.streaming.function_call_async
                // Stream response asynchronously with function tool
                ResponseAccumulator responseAccumulator = ResponseAccumulator.create();

                return responsesAsyncClient.createStreamingWithAgent(agentReference,
                        ResponseCreateParams.builder()
                            .input("What's the weather like in Seattle?"))
                    .doOnNext(event -> {
                        responseAccumulator.accumulate(event);
                        // Print text deltas as they arrive
                        event.outputTextDelta()
                            .ifPresent(textEvent -> System.out.print(textEvent.delta()));
                        // Print function call argument deltas as they arrive
                        event.functionCallArgumentsDelta()
                            .ifPresent(argEvent -> System.out.print(argEvent.delta()));
                    })
                    .then(Mono.fromCallable(() -> {
                        System.out.println();

                        // Access the final response and inspect function calls
                        Response response = responseAccumulator.response();
                        for (ResponseOutputItem outputItem : response.output()) {
                            outputItem.functionCall().ifPresent(functionCall -> {
                                System.out.println("\n--- Function Tool Call ---");
                                System.out.println("Call ID: " + functionCall.callId());
                                System.out.println("Function Name: " + functionCall.name());
                                System.out.println("Arguments: " + functionCall.arguments());
                                System.out.println("Status: " + functionCall.status());
                            });
                        }
                        // END: com.azure.ai.agents.streaming.function_call_async
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
