// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.McpTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.models.conversations.Conversation;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.services.blocking.ConversationService;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.ProtocolConfiguration;
import com.azure.ai.agents.models.ResponsesProtocolConfiguration;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;

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
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 *   <li>MCP_PROJECT_CONNECTION_ID - The MCP project connection ID (Custom Keys connection
 *       with key "Authorization" and value "Bearer &lt;your GitHub PAT token&gt;").</li>
 * </ul>
 */
public class McpWithConnectionSync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String mcpConnectionId = Configuration.getGlobalConfiguration().get("MCP_PROJECT_CONNECTION_ID");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
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

        String agentName = "mcp-connection-agent";
        AgentVersionDetails agent = agentsClient.createAgentVersion(agentName, agentDefinition);
        try {
            agentsClient.updateAgentDetails(agentName, new UpdateAgentDetailsOptions().setAgentEndpoint(
                new AgentEndpointConfig()
                    .setVersionSelector(new VersionSelector().setVersionSelectionRules(Collections.singletonList(
                        new FixedRatioVersionSelectionRule(100).setAgentVersion(agent.getVersion()))))
                    .setProtocolConfiguration(new ProtocolConfiguration().setResponses(new ResponsesProtocolConfiguration()))));


            OpenAIClient openAIClient = builder.buildAgentScopedOpenAIClient(agentName);

            // Create a conversation for context
            Conversation conversation = conversationService.create();

            // Send initial request that triggers the MCP tool
            Response response = openAIClient.responses().create(ResponseCreateParams.builder()
                .conversation(conversation.id())
                .input("What is my username in GitHub profile?")
                .build());

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
                Response followUp = openAIClient.responses().create(ResponseCreateParams.builder()
                    .conversation(conversation.id())
                    .inputOfResponse(approvals)
                    .previousResponseId(response.id())
                    .build());

                System.out.println("Response: " + followUp.output());
            } else {
                System.out.println("Response: " + response.output());
            }
        } finally {
            agentsClient.deleteAgentVersion(agentName, agent.getVersion());
        }
    }
}
