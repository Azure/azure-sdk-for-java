// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.PromptAgentDefinitionTextOptions;
import com.azure.ai.agents.models.ResponseFormatJsonSchemaInner;
import com.azure.ai.agents.models.TextResponseFormatJsonSchema;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Demonstrates generating a response that conforms to a JSON schema.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class AgentStructuredOutputSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);
        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        AgentVersionDetails agent = agentsClient.createAgentVersion("structured-output-agent",
            new CreateAgentVersionInput(createDefinition(model)));
        try {
            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(SampleUtils.toAgentReference(agent)),
                ResponseCreateParams.builder()
                    .input("Alice and Bob are going to a science fair on 2026-11-07."));
            SampleUtils.printResponseText(response);
        } finally {
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }

    static PromptAgentDefinition createDefinition(String model) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("name", field("string", null));
        properties.put("date", field("string", "Date in YYYY-MM-DD format"));
        properties.put("participants", arrayField("string"));

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", new String[] { "name", "date", "participants" });
        schema.put("additionalProperties", false);

        ResponseFormatJsonSchemaInner schemaModel = BinaryData.fromObject(schema)
            .toObject(ResponseFormatJsonSchemaInner.class);
        TextResponseFormatJsonSchema format = new TextResponseFormatJsonSchema("CalendarEvent", schemaModel)
            .setStrict(true);
        return new PromptAgentDefinition(model)
            .setInstructions("Extract calendar event information and return only the requested structured output.")
            .setText(new PromptAgentDefinitionTextOptions().setFormat(format));
    }

    private static Map<String, Object> field(String type, String description) {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("type", type);
        if (description != null) {
            field.put("description", description);
        }
        return field;
    }

    private static Map<String, Object> arrayField(String itemType) {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("type", "array");
        field.put("items", field(itemType, null));
        return field;
    }
}
