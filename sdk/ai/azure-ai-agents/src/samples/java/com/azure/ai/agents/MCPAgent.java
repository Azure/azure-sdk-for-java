// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.MCPTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;

import java.util.Collections;

/**
 * This sample demonstrates how to create an Azure AI Agent with the MCP (Model Context Protocol) tool
 * and use it to get responses that involve MCP server calls.
 */
public class MCPAgent {
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
            // Create an MCPTool that connects to a remote MCP server
            MCPTool tool = new MCPTool("my-mcp-server")
                .setServerUrl("https://my.mcp.server/mcp")
                .setServerDescription("An MCP server that provides additional tools");

            // Create the agent definition with MCP tool enabled
            PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
                .setInstructions("You are a helpful assistant that can use MCP tools to access external services. "
                    + "When asked to perform tasks, use the available MCP tools to help the user.")
                .setTools(Collections.singletonList(tool));

            agent = agentsClient.createAgentVersion("MyAgent", agentDefinition);
            System.out.printf("Agent created (id: %s, version: %s)\n", agent.getId(), agent.getVersion());

            AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

            Response response = responsesClient.createWithAgent(agentReference,
                    ResponseCreateParams.builder().input("What tools are available from the MCP server?"));

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

                // Handle MCP tool call output
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
