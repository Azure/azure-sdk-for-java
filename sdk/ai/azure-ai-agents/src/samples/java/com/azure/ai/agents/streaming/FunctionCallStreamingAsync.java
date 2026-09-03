// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.streaming;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.FunctionTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClientAsync;
import com.openai.core.http.AsyncStreamResponse;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseStreamEvent;
import reactor.core.publisher.Mono;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.ProtocolConfiguration;
import com.azure.ai.agents.models.ResponsesProtocolConfiguration;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This sample demonstrates how to stream a response from an agent configured with a
 * Function Calling tool using the asynchronous client. Function call arguments and text
 * output are printed as they arrive.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 * </ul>
 */
public class FunctionCallStreamingAsync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();

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

        // Create the agent version and pin the agent endpoint to it. The endpoint URL identifies the agent,
        // so responses.createStreaming(...) below does not need to send an agent_reference in its body.
        String agentName = "function-streaming-async-agent";
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

                    // BEGIN: com.azure.ai.agents.streaming.function_call_async
                    // Stream response asynchronously with function tool
                    ResponseAccumulator responseAccumulator = ResponseAccumulator.create();

                    AsyncStreamResponse<ResponseStreamEvent> stream = openAIAsyncClient.responses().createStreaming(
                        ResponseCreateParams.builder()
                            .input("What's the weather like in Seattle?")
                            .build());

                    stream.subscribe(new AsyncStreamResponse.Handler<ResponseStreamEvent>() {
                        @Override
                        public void onNext(ResponseStreamEvent event) {
                            responseAccumulator.accumulate(event);
                            // Print text deltas as they arrive
                            event.outputTextDelta()
                                .ifPresent(textEvent -> System.out.print(textEvent.delta()));
                            // Print function call argument deltas as they arrive
                            event.functionCallArgumentsDelta()
                                .ifPresent(argEvent -> System.out.print(argEvent.delta()));
                        }

                        @Override
                        public void onComplete(java.util.Optional<Throwable> error) {
                            // No-op: onCompleteFuture below signals completion.
                        }
                    });

                    return Mono.fromFuture(stream.onCompleteFuture())
                        .doFinally(signal -> stream.close())
                        .doOnSuccess(unused -> {
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
                        });
                    // END: com.azure.ai.agents.streaming.function_call_async
                },
                agent -> agentsAsyncClient.deleteAgentVersion(agentName, agent.getVersion()))
            .block();
    }
}
