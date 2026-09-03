// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
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
import com.openai.client.OpenAIClientAsync;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.ProtocolConfiguration;
import com.azure.ai.agents.models.ResponsesProtocolConfiguration;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This sample demonstrates how to create an agent with a Function Calling tool
 * that defines custom functions the agent can invoke, using the async client.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 * </ul>
 */
public class FunctionCallAsync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();

        // Create a FunctionTool with parameters schema
        // Use BinaryData.fromObject() to produce correct JSON types (not double-encoded strings)
        // BEGIN: com.azure.ai.agents.define_function_call
        Map<String, Object> locationProp = new LinkedHashMap<String, Object>();
        locationProp.put("type", "string");
        locationProp.put("description", "The city and state, e.g. Seattle, WA");

        Map<String, Object> unitProp = new LinkedHashMap<String, Object>();
        unitProp.put("type", "string");
        unitProp.put("enum", Arrays.asList("celsius", "fahrenheit"));

        Map<String, Object> properties = new LinkedHashMap<String, Object>();
        properties.put("location", locationProp);
        properties.put("unit", unitProp);

        Map<String, BinaryData> parameters = new HashMap<String, BinaryData>();
        parameters.put("type", BinaryData.fromObject("object"));
        parameters.put("properties", BinaryData.fromObject(properties));
        parameters.put("required", BinaryData.fromObject(Arrays.asList("location", "unit")));
        parameters.put("additionalProperties", BinaryData.fromObject(false));

        FunctionTool tool = new FunctionTool("get_weather", parameters, true)
            .setDescription("Get the current weather in a given location");
        // END: com.azure.ai.agents.define_function_call
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that can get weather information. "
                + "When asked about the weather, use the get_weather function to retrieve weather data.")
            .setTools(Collections.singletonList(tool));

        String agentName = "function-call-agent";
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
                    return Mono.fromFuture(openAIAsyncClient.responses().create(ResponseCreateParams.builder()
                            .input("What's the weather like in Seattle?")
                            .build()))
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
                        });
                },
                agent -> agentsAsyncClient.deleteAgentVersion(agentName, agent.getVersion()))
            .doOnError(error -> System.err.println("Error: " + error.getMessage()))
            .timeout(Duration.ofSeconds(30))
            .block();
    }
}
