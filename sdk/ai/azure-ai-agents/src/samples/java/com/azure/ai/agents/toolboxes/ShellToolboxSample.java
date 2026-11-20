// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.toolboxes;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.ToolboxesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.McpTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.ShellToolboxTool;
import com.azure.ai.agents.models.ToolboxShellContainerAutoEnvironment;
import com.azure.ai.agents.models.ToolboxTool;
import com.azure.ai.agents.models.ToolboxVersionDetails;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * This sample demonstrates how to put a shell tool in a toolbox and invoke it from a prompt agent.
 * The shell tool runs commands in an automatically provisioned container with outbound network access disabled.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 *   <li>FOUNDRY_AGENT_NAME - Optional. The agent name; defaults to {@code shell-toolbox-agent}.</li>
 * </ul>
 */
public class ShellToolboxSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_MODEL_NAME");
        String agentName = configuration.get("FOUNDRY_AGENT_NAME", "shell-toolbox-agent");
        String toolboxName = "toolbox-with-shell-tool-java";

        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(credential)
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();
        ToolboxesClient toolboxesClient = builder.buildToolboxesClient();

        deleteToolboxIfPresent(toolboxesClient, toolboxName);
        AgentVersionDetails agent = null;

        try {
            // BEGIN: com.azure.ai.agents.toolboxes.ShellToolboxSample.createShellToolbox

            ShellToolboxTool shellTool = new ShellToolboxTool(new ToolboxShellContainerAutoEnvironment())
                .setDescription("Runs shell commands in a sandboxed container.");

            ToolboxVersionDetails toolboxVersion = toolboxesClient.createToolboxVersion(
                toolboxName,
                Collections.<ToolboxTool>singletonList(shellTool),
                "Toolbox with a shell tool running in an auto-provisioned container.",
                null,
                null,
                null);

            // END: com.azure.ai.agents.toolboxes.ShellToolboxSample.createShellToolbox

            System.out.printf("Created toolbox `%s` (version %s).%n", toolboxName, toolboxVersion.getVersion());

            String toolboxMcpUrl = endpoint + (endpoint.endsWith("/") ? "" : "/")
                + "toolboxes/" + toolboxName + "/versions/" + toolboxVersion.getVersion() + "/mcp?api-version=v1";
            String token = credential.getTokenSync(
                new TokenRequestContext().addScopes("https://ai.azure.com/.default")).getToken();

            McpTool toolboxMcpTool = new McpTool("shell-toolbox")
                .setServerUrl(toolboxMcpUrl)
                .setAuthorization(token)
                .setRequireApproval("never");

            PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
                .setInstructions("You have a shell tool that runs commands in a sandboxed container with no "
                    + "network access. Use it to answer questions about that environment, and report the exact "
                    + "command output back to the user.")
                .setTools(Collections.singletonList(toolboxMcpTool));

            agent = agentsClient.createAgentVersion(agentName, agentDefinition);
            System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

            AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());
            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(agentReference),
                ResponseCreateParams.builder()
                    .input("Which Python version is installed, and what is in the working directory?"));

            printResponse(response);
        } finally {
            try {
                if (agent != null) {
                    agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
                    System.out.println("Agent deleted");
                }
            } finally {
                deleteToolboxIfPresent(toolboxesClient, toolboxName);
            }
        }
    }

    private static void printResponse(Response response) {
        StringBuilder responseText = new StringBuilder();

        for (ResponseOutputItem item : response.output()) {
            if (item.isMcpListTools()) {
                ResponseOutputItem.McpListTools listTools = item.asMcpListTools();
                String toolNames = listTools.tools().stream()
                    .map(ResponseOutputItem.McpListTools.Tool::name)
                    .collect(Collectors.joining(", "));
                System.out.printf("server_label=%s, tools=[%s]%n", listTools.serverLabel(), toolNames);
            } else if (item.isMcpCall()) {
                ResponseOutputItem.McpCall mcpCall = item.asMcpCall();
                System.out.printf("server_label=%s, name=%s, error=%s%n",
                    mcpCall.serverLabel(), mcpCall.name(), mcpCall.error().orElse(null));
                System.out.println("  arguments: " + mcpCall.arguments());
                System.out.println("  output: " + mcpCall.output().orElse(null));
            } else if (item.isMessage()) {
                for (ResponseOutputMessage.Content content : item.asMessage().content()) {
                    content.outputText().ifPresent(outputText -> responseText.append(outputText.text()));
                }
            }
        }

        System.out.printf("%nResponse: %s%n", responseText);
    }

    private static void deleteToolboxIfPresent(ToolboxesClient toolboxesClient, String toolboxName) {
        try {
            toolboxesClient.deleteToolbox(toolboxName);
            System.out.printf("Deleted toolbox `%s`%n", toolboxName);
        } catch (ResourceNotFoundException ignored) {
            // The toolbox does not exist.
        }
    }
}
