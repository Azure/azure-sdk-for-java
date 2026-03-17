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
import com.openai.models.conversations.Conversation;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.services.blocking.ConversationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This sample demonstrates how to create an agent with a Model Context Protocol (MCP) tool
 * using a project connection for authentication, and how to handle MCP approval requests.
 *
 * <p>The sample shows the full approval loop: the agent sends an MCP approval request,
 * the client approves it, and the agent continues its work.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_DEPLOYMENT_NAME - The model deployment name.</li>
 *   <li>MCP_PROJECT_CONNECTION_ID - The MCP project connection ID (Custom Keys connection
 *       with key "Authorization" and value "Bearer &lt;your GitHub PAT token&gt;").</li>
 * </ul>
 */
public class McpWithConnectionSync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_DEPLOYMENT_NAME");
        String mcpConnectionId = Configuration.getGlobalConfiguration().get("MCP_PROJECT_CONNECTION_ID");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();
        ConversationService conversationService = builder.buildOpenAIClient().conversations();

        // BEGIN: com.azure.ai.agents.define_mcp_with_connection
        // Create MCP tool with project connection authentication
        McpTool mcpTool = new McpTool("api-specs")
            .setServerUrl("https://api.githubcopilot.com/mcp")
            .setProjectConnectionId(mcpConnectionId)
            .setRequireApproval("always");
        // END: com.azure.ai.agents.define_mcp_with_connection

        // Create agent with MCP tool
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("Use MCP tools as needed")
            .setTools(Collections.singletonList(mcpTool));

        AgentVersionDetails agent = agentsClient.createAgentVersion("mcp-connection-agent", agentDefinition);
        System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

        try {
            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            // Create a conversation for context
            Conversation conversation = conversationService.create();

            // Send initial request that triggers the MCP tool
            Response response = responsesClient.createWithAgentConversation(
                agentReference, conversation.id(),
                ResponseCreateParams.builder()
                    .input("What is my username in GitHub profile?"));

            // Process MCP approval requests: approve each one so the agent can proceed
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

                // Send approvals back to continue the agent's work
                Response followUp = responsesClient.createWithAgentConversation(
                    agentReference, conversation.id(),
                    ResponseCreateParams.builder()
                        .inputOfResponse(approvals)
                        .previousResponseId(response.id()));

                System.out.println("Response: " + followUp.output());
            } else {
                System.out.println("Response: " + response.output());
            }
        } finally {
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
            System.out.println("Agent deleted");
        }
    }
}
