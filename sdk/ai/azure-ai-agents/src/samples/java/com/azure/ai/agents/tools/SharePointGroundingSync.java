// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.SharepointGroundingToolParameters;
import com.azure.ai.agents.models.SharepointPreviewTool;
import com.azure.ai.agents.models.ToolProjectConnection;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.Collections;
import java.util.Arrays;

/**
 * This sample demonstrates how to create an agent with the SharePoint Grounding tool
 * to search through SharePoint documents.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_DEPLOYMENT_NAME - The model deployment name.</li>
 *   <li>SHAREPOINT_PROJECT_CONNECTION_ID - The SharePoint connection ID.</li>
 * </ul>
 */
public class SharePointGroundingSync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_DEPLOYMENT_NAME");
        String sharepointConnectionId = Configuration.getGlobalConfiguration().get("SHAREPOINT_PROJECT_CONNECTION_ID");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        // BEGIN: com.azure.ai.agents.define_sharepoint
        // Create SharePoint grounding tool with connection configuration
        SharepointPreviewTool sharepointTool = new SharepointPreviewTool(
            new SharepointGroundingToolParameters()
                .setProjectConnections(Arrays.asList(
                    new ToolProjectConnection(sharepointConnectionId)
                ))
        );
        // END: com.azure.ai.agents.define_sharepoint

        // Create agent with SharePoint tool
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that can search through SharePoint documents.")
            .setTools(Collections.singletonList(sharepointTool));

        AgentVersionDetails agent = agentsClient.createAgentVersion("sharepoint-agent", agentDefinition);
        System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

        try {
            // Create a response
            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            Response response = responsesClient.createWithAgent(
                agentReference,
                ResponseCreateParams.builder()
                    .input("Find the latest project documentation in SharePoint"));

            System.out.println("Response: " + response.output());
        } finally {
            // Clean up
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }
}
