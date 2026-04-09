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
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This sample demonstrates how to create an agent with a Model Context Protocol (MCP) tool
 * to connect to an external MCP server and handle approval requests.
 *
 * <p>Uses <a href="https://gitmcp.io">gitmcp.io</a> to expose a GitHub repository as an
 * MCP-compatible server (no authentication required).</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 * </ul>
 */
public class McpSync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        AgentVersionDetails agent = null;

        try {
            // Create an MCP tool that connects to a remote MCP server
            // BEGIN: com.azure.ai.agents.built_in_mcp
            // Uses gitmcp.io to expose a GitHub repository as an MCP-compatible server
            McpTool tool = new McpTool("api-specs")
                .setServerUrl("https://gitmcp.io/Azure/azure-rest-api-specs")
                .setRequireApproval("always");
            // END: com.azure.ai.agents.built_in_mcp

            // Create the agent definition with MCP tool enabled
            PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
                .setInstructions("You are a helpful agent that can use MCP tools to assist users. "
                    + "Use the available MCP tools to answer questions and perform tasks.")
                .setTools(Collections.singletonList(tool));

            agent = agentsClient.createAgentVersion("mcp-agent", agentDefinition);
            System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(agentReference),
                ResponseCreateParams.builder()
                    .input("Please summarize the Azure REST API specifications Readme"));

            // Process MCP approval requests — the server requires approval before executing tools
            List<ResponseInputItem> approvals = new ArrayList<ResponseInputItem>();
            for (ResponseOutputItem item : response.output()) {
                if (item.isMcpApprovalRequest()) {
                    ResponseOutputItem.McpApprovalRequest request = item.asMcpApprovalRequest();
                    System.out.printf("MCP approval requested: server=%s, id=%s%n",
                        request.serverLabel(), request.id());

                    approvals.add(ResponseInputItem.ofMcpApprovalResponse(
                        ResponseInputItem.McpApprovalResponse.builder()
                            .approvalRequestId(request.id())
                            .approve(true)
                            .build()));
                }
            }

            // If approvals were needed, send them back and get the final response
            if (!approvals.isEmpty()) {
                System.out.println("Sending " + approvals.size() + " approval(s)...");
                response = responsesClient.createAzureResponse(
                    new AzureCreateResponseOptions().setAgentReference(agentReference),
                    ResponseCreateParams.builder()
                        .inputOfResponse(approvals)
                        .previousResponseId(response.id()));
            }

            // Process and display the final response
            for (ResponseOutputItem outputItem : response.output()) {
                if (outputItem.message().isPresent()) {
                    ResponseOutputMessage message = outputItem.message().get();
                    message.content().forEach(content -> {
                        content.outputText().ifPresent(text -> {
                            System.out.println("Assistant: " + text.text());
                        });
                    });
                }

                if (outputItem.mcpCall().isPresent()) {
                    ResponseOutputItem.McpCall mcpCall = outputItem.mcpCall().get();
                    System.out.println("\n--- MCP Tool Call ---");
                    System.out.println("Call ID: " + mcpCall.id());
                    System.out.println("Server Label: " + mcpCall.serverLabel());
                    System.out.println("Tool Name: " + mcpCall.name());
                    System.out.println("Arguments: " + mcpCall.arguments());
                    mcpCall.status().ifPresent(status -> System.out.println("Status: " + status));
                    mcpCall.output().ifPresent(output -> System.out.println("Output: " + output));
                    mcpCall.error().ifPresent(error -> System.out.println("Error: " + error));
                }
            }
        } finally {
            if (agent != null) {
                agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
                System.out.println("Agent deleted");
            }
        }
    }
}
