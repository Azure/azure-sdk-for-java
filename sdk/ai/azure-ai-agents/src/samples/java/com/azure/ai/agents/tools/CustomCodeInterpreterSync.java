// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.McpTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.Collections;

/**
 * This sample demonstrates how to create an agent with a Custom Code Interpreter tool
 * backed by an MCP server running in a sandboxed Container Apps session.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 *   <li>MCP_SERVER_URL - The MCP server URL for the custom code interpreter.</li>
 *   <li>MCP_PROJECT_CONNECTION_ID - The MCP project connection ID.</li>
 * </ul>
 */
public class CustomCodeInterpreterSync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String mcpServerUrl = Configuration.getGlobalConfiguration().get("MCP_SERVER_URL");
        String connectionId = Configuration.getGlobalConfiguration().get("MCP_PROJECT_CONNECTION_ID");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        // Create custom code interpreter MCP tool
        // Uses require_approval: "never" because code runs in a sandboxed Container Apps session
        McpTool customCodeInterpreter = new McpTool("custom-code-interpreter")
            .setServerUrl(mcpServerUrl)
            .setProjectConnectionId(connectionId)
            .setRequireApproval("never");

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that can run Python code to analyze data and solve problems.")
            .setTools(Collections.singletonList(customCodeInterpreter));

        AgentVersionDetails agent = agentsClient.createAgentVersion(
            "CustomCodeInterpreterAgent", agentDefinition);
        System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

        try {
            // Create a response
            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(agentReference),
                ResponseCreateParams.builder()
                    .input("Calculate the factorial of 10 using Python."));

            System.out.println("Response: " + response.output());
        } finally {
            // Clean up
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
            System.out.println("Agent deleted");
        }
    }
}
