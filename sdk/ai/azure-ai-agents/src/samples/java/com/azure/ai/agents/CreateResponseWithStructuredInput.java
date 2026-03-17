// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponse;
import com.azure.ai.agents.models.AzureCreateResponseResult;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.StructuredInputDefinition;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This sample demonstrates how to create a response with structured inputs using
 * {@link AzureCreateResponse}. Structured inputs are key-value pairs defined on an
 * agent that get substituted into the agent's prompt template at runtime.
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

        // BEGIN: com.azure.ai.agents.define_structured_inputs
        // Create an agent with structured input definitions
        Map<String, StructuredInputDefinition> structuredInputDefinitions = new LinkedHashMap<>();
        structuredInputDefinitions.put("userName",
            new StructuredInputDefinition().setDescription("User's name").setRequired(true));
        structuredInputDefinitions.put("userRole",
            new StructuredInputDefinition().setDescription("User's role").setRequired(true));

        AgentVersionDetails agent = agentsClient.createAgentVersion("structured-input-agent",
            new PromptAgentDefinition(model)
                .setInstructions("You are a helpful assistant. "
                    + "The user's name is {{userName}} and their role is {{userRole}}. "
                    + "Greet them and confirm their details.")
                .setStructuredInputs(structuredInputDefinitions));
        // END: com.azure.ai.agents.define_structured_inputs

        // BEGIN: com.azure.ai.agents.create_response_with_structured_input
        // Build the structured input values that match the agent's definitions
        Map<String, BinaryData> structuredInputValues = new LinkedHashMap<>();
        structuredInputValues.put("userName", BinaryData.fromObject("Alice Smith"));
        structuredInputValues.put("userRole", BinaryData.fromObject("Senior Developer"));

        // Create a response using AzureCreateResponse, which flattens agent_reference
        // and structured_inputs as top-level properties in the request body
        Response response = responsesClient.createAzureResponse(
            new AzureCreateResponse()
                .setAgentReference(new AgentReference(agent.getName()).setVersion(agent.getVersion()))
                .setStructuredInputs(structuredInputValues),
            ResponseCreateParams.builder().input("Hello! Can you confirm my details?")
        );
        // END: com.azure.ai.agents.create_response_with_structured_input

        System.out.println("Response output: " + response.output());
        System.out.println("Response ID: " + response.id());
        System.out.println("Response model: " + response.model());

        // Extract Azure-specific fields from the response
        AzureCreateResponseResult azureResult = ResponsesUtils.getAzureFields(response);
        if (azureResult != null && azureResult.getAgentReference() != null) {
            AgentReference ref = azureResult.getAgentReference();
            System.out.println("Azure agent_reference.type: " + ref.getType());
            System.out.println("Azure agent_reference.name: " + ref.getName());
            System.out.println("Azure agent_reference.version: " + ref.getVersion());
        } else {
            System.out.println("No Azure-specific fields found in the response.");
        }

        // Cleanup
        agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
    }
}
