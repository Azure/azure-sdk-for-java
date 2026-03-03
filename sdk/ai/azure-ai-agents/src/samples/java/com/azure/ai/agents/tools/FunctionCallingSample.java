// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.FunctionTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This sample demonstrates how to create an agent with a Function Calling tool
 * that defines custom functions the agent can invoke.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_DEPLOYMENT_NAME - The model deployment name.</li>
 * </ul>
 */
public class FunctionCallingSample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_DEPLOYMENT_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        // Define function parameters
        Map<String, BinaryData> parameters = new HashMap<>();
        parameters.put("type", BinaryData.fromString("\"object\""));
        parameters.put("properties", BinaryData.fromString(
            "{\"location\":{\"type\":\"string\",\"description\":\"The city and state, e.g. Seattle, WA\"},"
            + "\"unit\":{\"type\":\"string\",\"enum\":[\"celsius\",\"fahrenheit\"]}}"));
        parameters.put("required", BinaryData.fromString("[\"location\"]"));

        FunctionTool weatherFunction = new FunctionTool("get_weather", parameters, true);

        // Create agent with function tool
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a weather assistant. Use the get_weather function to retrieve weather information.")
            .setTools(Arrays.asList(weatherFunction));

        AgentVersionDetails agent = agentsClient.createAgentVersion("function-calling-agent", agentDefinition);
        System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

        // Create a response - the agent will call the function
        AgentReference agentReference = new AgentReference(agent.getName())
            .setVersion(agent.getVersion());

        Response response = responsesClient.createWithAgent(
            agentReference,
            ResponseCreateParams.builder()
                .input("What is the weather in Seattle?"));

        System.out.println("Response: " + response.output());

        // Clean up
        agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
    }
}
