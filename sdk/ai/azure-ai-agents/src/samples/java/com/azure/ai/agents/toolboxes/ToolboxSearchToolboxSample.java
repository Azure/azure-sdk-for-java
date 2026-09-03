// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.toolboxes;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.ToolboxesClient;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.McpTool;
import com.azure.ai.agents.models.McpToolboxTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.ToolSearchToolboxTool;
import com.azure.ai.agents.models.ToolboxTool;
import com.azure.ai.agents.models.ToolboxVersionDetails;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.Arrays;

/**
 * Demonstrates creating a tool-search toolbox and invoking its MCP endpoint from a prompt agent.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code MCP_PROJECT_CONNECTION_ID} - The project connection resource ID used by the inner MCP server.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class ToolboxSearchToolboxSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String toolboxName = "toolbox-search-java";
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        AgentsClientBuilder builder = new AgentsClientBuilder().credential(credential).endpoint(endpoint);
        ToolboxesClient toolboxesClient = builder.buildToolboxesClient();
        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();
        AgentVersionDetails agent = null;

        deleteToolboxIfPresent(toolboxesClient, toolboxName);
        try {
            // BEGIN: com.azure.ai.agents.toolboxes.ToolboxSearchToolboxSample.createToolboxSearchToolbox
            McpToolboxTool innerMcp = new McpToolboxTool("github")
                .setServerUrl("https://api.githubcopilot.com/mcp")
                .setProjectConnectionId(configuration.get("MCP_PROJECT_CONNECTION_ID"))
                .setRequireApproval(BinaryData.fromString("\"never\""))
                .setDeferLoading(true);
            ToolSearchToolboxTool search = new ToolSearchToolboxTool();
            ToolboxVersionDetails version = toolboxesClient.createToolboxVersion(toolboxName,
                Arrays.<ToolboxTool>asList(innerMcp, search), "Tool-search toolbox", null, null, null);
            // END: com.azure.ai.agents.toolboxes.ToolboxSearchToolboxSample.createToolboxSearchToolbox

            String toolboxUrl = endpoint + "/toolboxes/" + toolboxName + "/versions/"
                + version.getVersion() + "/mcp?api-version=v1";
            String token = credential.getToken(new TokenRequestContext()
                .addScopes("https://ai.azure.com/.default")).block().getToken();
            McpTool toolboxMcp = new McpTool("search-tool")
                .setServerUrl(toolboxUrl)
                .setAuthorization(token)
                .setRequireApproval("never");
            agent = agentsClient.createAgentVersion("toolbox-search-agent",
                new CreateAgentVersionInput(new PromptAgentDefinition(configuration.get("FOUNDRY_MODEL_NAME"))
                    .setInstructions("Use tool_search to discover a tool, then call_tool to invoke it.")
                    .setTools(java.util.Collections.singletonList(toolboxMcp))));

            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(
                    new com.azure.ai.agents.models.AgentReference(agent.getName()).setVersion(agent.getVersion())),
                ResponseCreateParams.builder().input("What is my GitHub profile username?"));
            System.out.println("Response: " + response.output());
        } finally {
            if (agent != null) {
                agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
            }
            deleteToolboxIfPresent(toolboxesClient, toolboxName);
        }
    }

    private static void deleteToolboxIfPresent(ToolboxesClient client, String toolboxName) {
        try {
            client.deleteToolbox(toolboxName);
        } catch (ResourceNotFoundException ignored) {
            // The toolbox does not exist.
        }
    }
}
