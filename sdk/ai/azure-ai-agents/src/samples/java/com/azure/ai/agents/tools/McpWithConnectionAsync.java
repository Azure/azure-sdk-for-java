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
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This sample demonstrates (using the async client) how to create an agent with an MCP tool
 * using a project connection for authentication, and how to handle MCP approval requests.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 *   <li>MCP_PROJECT_CONNECTION_ID - The MCP project connection ID.</li>
 * </ul>
 */
public class McpWithConnectionAsync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String mcpConnectionId = Configuration.getGlobalConfiguration().get("MCP_PROJECT_CONNECTION_ID");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesAsyncClient = builder.buildResponsesAsyncClient();

        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();

        McpTool mcpTool = new McpTool("api-specs")
            .setServerUrl("https://api.githubcopilot.com/mcp")
            .setProjectConnectionId(mcpConnectionId)
            .setRequireApproval("always");

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("Use MCP tools as needed")
            .setTools(Collections.singletonList(mcpTool));

        agentsAsyncClient.createAgentVersion("mcp-connection-agent", agentDefinition)
            .flatMap(agent -> {
                agentRef.set(agent);
                System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

                AgentReference agentReference = new AgentReference(agent.getName())
                    .setVersion(agent.getVersion());

                return responsesAsyncClient.createWithAgent(agentReference,
                    ResponseCreateParams.builder()
                        .input("What is my username in GitHub profile?"));
            })
            .flatMap(response -> {
                AgentVersionDetails agent = agentRef.get();
                AgentReference agentReference = new AgentReference(agent.getName())
                    .setVersion(agent.getVersion());

                // Process MCP approval requests
                List<ResponseInputItem> approvals = new ArrayList<>();
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
                System.out.println("Response: " + response.output());
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
