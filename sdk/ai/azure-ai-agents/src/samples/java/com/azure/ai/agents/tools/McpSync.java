// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.McpTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;

import java.util.Collections;

/**
 * This sample demonstrates how to create an agent with a Model Context Protocol (MCP) tool
 * to connect to external MCP servers.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_DEPLOYMENT_NAME - The model deployment name.</li>
 * </ul>
 */
public class McpSync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_DEPLOYMENT_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        AgentVersionDetails agent = null;

        try {
            // Create an MCP tool that connects to a remote MCP server
            McpTool tool = new McpTool("my-mcp-server")
                .setServerUrl("https://example.mcp.server/mcp")
                .setServerDescription("An MCP server that provides additional tools");

            // Create the agent definition with MCP tool enabled
            PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
                .setInstructions("You are a helpful assistant that can use MCP tools to access external services. "
                    + "When asked to perform tasks, use the available MCP tools to help the user.")
                .setTools(Collections.singletonList(tool));

            agent = agentsClient.createAgentVersion("mcp-agent", agentDefinition);
            System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            Response response = responsesClient.createWithAgent(agentReference,
                ResponseCreateParams.builder()
                    .input("What tools are available from the MCP server?"));

            // Process and display the response
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
