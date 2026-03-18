// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesAsyncClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.McpTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This sample demonstrates (using the async client) how to create an agent with a Model Context Protocol (MCP) tool
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
public class McpAsync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesAsyncClient = builder.buildResponsesAsyncClient();

        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();

        // Create an MCP tool that connects to a remote MCP server
        // Uses gitmcp.io to expose a GitHub repository as an MCP-compatible server
        McpTool tool = new McpTool("api-specs")
            .setServerUrl("https://gitmcp.io/Azure/azure-rest-api-specs")
            .setRequireApproval("always");

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful agent that can use MCP tools to assist users. "
                + "Use the available MCP tools to answer questions and perform tasks.")
            .setTools(Collections.singletonList(tool));

        agentsAsyncClient.createAgentVersion("mcp-agent", agentDefinition)
            .flatMap(agent -> {
                agentRef.set(agent);
                System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

                AgentReference agentReference = new AgentReference(agent.getName())
                    .setVersion(agent.getVersion());

                return responsesAsyncClient.createWithAgent(agentReference,
                    ResponseCreateParams.builder()
                        .input("Please summarize the Azure REST API specifications Readme"));
            })
            .flatMap(response -> {
                AgentVersionDetails agent = agentRef.get();
                AgentReference agentReference = new AgentReference(agent.getName())
                    .setVersion(agent.getVersion());

                // Process MCP approval requests
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

                if (!approvals.isEmpty()) {
                    System.out.println("Sending " + approvals.size() + " approval(s)...");
                    return responsesAsyncClient.createWithAgent(agentReference,
                        ResponseCreateParams.builder()
                            .inputOfResponse(approvals)
                            .previousResponseId(response.id()));
                }

                return Mono.just(response);
            })
            .doOnNext(response -> {
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
            })
            .then(Mono.defer(() -> {
                AgentVersionDetails agent = agentRef.get();
                if (agent != null) {
                    return agentsAsyncClient.deleteAgentVersion(agent.getName(), agent.getVersion())
                        .doOnSuccess(v -> System.out.println("Agent deleted"));
                }
                return Mono.empty();
            }))
            .doOnError(error -> System.err.println("Error: " + error.getMessage()))
            .timeout(Duration.ofSeconds(300))
            .block();
    }
}
