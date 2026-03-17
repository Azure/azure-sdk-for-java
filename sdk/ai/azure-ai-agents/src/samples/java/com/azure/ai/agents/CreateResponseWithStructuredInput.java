// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.StructuredInputDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This sample demonstrates how to create a response with structured inputs.
 * Structured inputs are key-value pairs defined on an agent that get substituted
 * into the agent's prompt template at runtime.
 */
public class CreateResponseWithStructuredInput {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .serviceVersion(AgentsServiceVersion.getLatest());

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        // Create an agent with structured input definitions
        Map<String, StructuredInputDefinition> structuredInputDefinitions = new LinkedHashMap<>();
        structuredInputDefinitions.put("userName", new StructuredInputDefinition().setDescription("User's name").setRequired(true));
        structuredInputDefinitions.put("userRole", new StructuredInputDefinition().setDescription("User's role").setRequired(true));

        AgentVersionDetails agent = agentsClient.createAgentVersion("structured-input-agent",
            new PromptAgentDefinition(model)
                .setInstructions("You are a helpful assistant. "
                    + "The user's name is {{userName}} and their role is {{userRole}}. "
                    + "Greet them and confirm their details.")
                .setStructuredInputs(structuredInputDefinitions));

        // Create a response, passing structured input values that match the agent's definitions
        Map<String, Object> structuredInputValues = new LinkedHashMap<>();
        structuredInputValues.put("userName", "Alice Smith");
        structuredInputValues.put("userRole", "Senior Developer");

        Response response = responsesClient.createWithAgentStructuredInput(
            new AgentReference(agent.getName()).setVersion(agent.getVersion()),
            structuredInputValues,
            ResponseCreateParams.builder().input("Hello! Can you confirm my details?")
        );

        System.out.println("Response: " + response.output());

        // Cleanup
        agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
    }
}
