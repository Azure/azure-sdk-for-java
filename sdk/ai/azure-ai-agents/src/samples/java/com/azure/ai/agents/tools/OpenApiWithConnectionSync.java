// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.SampleUtils;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.OpenApiFunctionDefinition;
import com.azure.ai.agents.models.OpenApiProjectConnectionAuthDetails;
import com.azure.ai.agents.models.OpenApiProjectConnectionSecurityScheme;
import com.azure.ai.agents.models.OpenApiTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.Collections;
import java.util.Map;

/**
 * This sample demonstrates how to create an agent with an OpenAPI tool
 * using project connection authentication. The agent can call external APIs
 * defined by OpenAPI specifications, using credentials stored in an Azure AI
 * Project connection.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 *   <li>OPENAPI_PROJECT_CONNECTION_ID - The OpenAPI project connection ID.</li>
 * </ul>
 *
 * <p>This sample uses the httpbin OpenAPI spec bundled at
 * {@code src/samples/resources/assets/httpbin_openapi.json}. Replace it with your
 * own spec to call a different API.</p>
 */
public class OpenApiWithConnectionSync {
    public static void main(String[] args) throws Exception {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String connectionId = Configuration.getGlobalConfiguration().get("OPENAPI_PROJECT_CONNECTION_ID");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        // Load the OpenAPI spec from a JSON file
        Map<String, BinaryData> spec = OpenApiFunctionDefinition.readSpecFromFile(
            SampleUtils.getResourcePath("assets/httpbin_openapi.json"));

        // BEGIN: com.azure.ai.agents.define_openapi_with_connection
        // Create OpenAPI tool with project connection authentication
        OpenApiTool openApiTool = new OpenApiTool(
            new OpenApiFunctionDefinition(
                "httpbin_get",
                spec,
                new OpenApiProjectConnectionAuthDetails(
                    new OpenApiProjectConnectionSecurityScheme(connectionId)))
                .setDescription("Get request metadata from an OpenAPI endpoint."));
        // END: com.azure.ai.agents.define_openapi_with_connection

        // Create agent with OpenAPI tool
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant.")
            .setTools(Collections.singletonList(openApiTool));

        AgentVersionDetails agent = agentsClient.createAgentVersion("openapi-connection-agent", agentDefinition);
        System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

        try {
            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(agentReference),
                ResponseCreateParams.builder()
                    .input("Call the API and summarize the returned URL and origin."));

            System.out.println("Response: " + response.output());
        } finally {
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
            System.out.println("Agent deleted");
        }
    }
}
