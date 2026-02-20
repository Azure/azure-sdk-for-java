// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.FunctionTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This sample demonstrates how to create an Azure AI Agent with the Function tool
 * and use it to get responses that involve function calls.
 */
public class FunctionCallAgent {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_MODEL");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .serviceVersion(AgentsServiceVersion.getLatest())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        AgentVersionDetails agent = null;

        try {
            // Create a FunctionTool with parameters schema
            Map<String, BinaryData> parameters = new HashMap<String, BinaryData>();
            parameters.put("type", BinaryData.fromString("\"object\""));
            parameters.put("properties", BinaryData.fromString("{\"location\": {\"type\": \"string\", \"description\": \"The city and state, e.g. San Francisco, CA\"}, \"unit\": {\"type\": \"string\", \"enum\": [\"celsius\", \"fahrenheit\"]}}"));
            parameters.put("required", BinaryData.fromString("[\"location\", \"unit\"]"));
            parameters.put("additionalProperties", BinaryData.fromString("false"));

            FunctionTool tool = new FunctionTool("get_weather", parameters, true)
                .setDescription("Get the current weather in a given location");

            // Create the agent definition with Function tool enabled
            PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
                .setInstructions("You are a helpful assistant that can get weather information. "
                    + "When asked about the weather, use the get_weather function to retrieve weather data.")
                .setTools(Collections.singletonList(tool));

            agent = agentsClient.createAgentVersion("MyAgent", agentDefinition);
            System.out.printf("Agent created (id: %s, version: %s)\n", agent.getId(), agent.getVersion());

            AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

            Response response = responsesClient.createWithAgent(agentReference, 
                    ResponseCreateParams.builder().input("What's the weather like in Seattle?"));

            // Process and display the response
            System.out.println("\n=== Agent Response ===");
            for (ResponseOutputItem outputItem : response.output()) {
                // Handle message output
                if (outputItem.message().isPresent()) {
                    ResponseOutputMessage message = outputItem.message().get();
                    message.content().forEach(content -> {
                        content.outputText().ifPresent(text -> {
                            System.out.println("Assistant: " + text.text());
                        });
                    });
                }

                // Handle function tool call output
                if (outputItem.functionCall().isPresent()) {
                    ResponseFunctionToolCall functionCall = outputItem.functionCall().get();
                    System.out.println("\n--- Function Tool Call ---");
                    System.out.println("Call ID: " + functionCall.callId());
                    System.out.println("Function Name: " + functionCall.name());
                    System.out.println("Arguments: " + functionCall.arguments());
                    System.out.println("Status: " + functionCall.status());
                }
            }

            System.out.println("\nResponse ID: " + response.id());
            System.out.println("Model Used: " + response.model());
        } finally {
            // Cleanup agent
            if (agent != null) {
                agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
                System.out.println("Agent deleted successfully.");
            }
        }
    }
}
