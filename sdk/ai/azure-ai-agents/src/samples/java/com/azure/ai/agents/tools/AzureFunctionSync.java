// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureFunctionBinding;
import com.azure.ai.agents.models.AzureFunctionDefinition;
import com.azure.ai.agents.models.AzureFunctionDefinitionDetails;
import com.azure.ai.agents.models.AzureFunctionStorageQueue;
import com.azure.ai.agents.models.AzureFunctionTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ToolChoiceOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This sample demonstrates how to create an agent with an Azure Function tool
 * that calls an Azure Function via Storage Queue input/output bindings.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 *   <li>STORAGE_INPUT_QUEUE_NAME - The Azure Storage Queue name for input.</li>
 *   <li>STORAGE_OUTPUT_QUEUE_NAME - The Azure Storage Queue name for output.</li>
 *   <li>STORAGE_QUEUE_SERVICE_ENDPOINT - The Azure Storage Queue service endpoint.</li>
 * </ul>
 */
public class AzureFunctionSync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String inputQueueName = Configuration.getGlobalConfiguration().get("STORAGE_INPUT_QUEUE_NAME");
        String outputQueueName = Configuration.getGlobalConfiguration().get("STORAGE_OUTPUT_QUEUE_NAME");
        String queueServiceEndpoint = Configuration.getGlobalConfiguration().get("STORAGE_QUEUE_SERVICE_ENDPOINT");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        // Define function parameters
        // Use BinaryData.fromObject() to produce correct JSON types
        Map<String, Object> locationProp = new LinkedHashMap<String, Object>();
        locationProp.put("type", "string");
        locationProp.put("description", "location to determine weather for");

        Map<String, Object> props = new LinkedHashMap<String, Object>();
        props.put("location", locationProp);

        Map<String, BinaryData> parameters = new HashMap<String, BinaryData>();
        parameters.put("type", BinaryData.fromObject("object"));
        parameters.put("properties", BinaryData.fromObject(props));

        // BEGIN: com.azure.ai.agents.define_azure_function
        // Create Azure Function tool with Storage Queue bindings
        AzureFunctionTool azureFunctionTool = new AzureFunctionTool(
            new AzureFunctionDefinition(
                new AzureFunctionDefinitionDetails("queue_trigger", parameters)
                    .setDescription("Get weather for a given location"),
                new AzureFunctionBinding(
                    new AzureFunctionStorageQueue(queueServiceEndpoint, inputQueueName)),
                new AzureFunctionBinding(
                    new AzureFunctionStorageQueue(queueServiceEndpoint, outputQueueName))
            )
        );
        // END: com.azure.ai.agents.define_azure_function

        // Create agent with Azure Function tool
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant.")
            .setTools(Collections.singletonList(azureFunctionTool));

        AgentVersionDetails agent = agentsClient.createAgentVersion("azure-function-agent", agentDefinition);
        System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

        try {
            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(agentReference),
                ResponseCreateParams.builder()
                    .toolChoice(ToolChoiceOptions.REQUIRED)
                    .input("What is the weather in Seattle?"));

            System.out.println("Response: " + response.output());
        } finally {
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
            System.out.println("Agent deleted");
        }
    }
}
