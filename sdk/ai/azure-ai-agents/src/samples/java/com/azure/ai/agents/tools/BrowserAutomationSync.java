// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.BrowserAutomationPreviewTool;
import com.azure.ai.agents.models.BrowserAutomationToolConnectionParameters;
import com.azure.ai.agents.models.BrowserAutomationToolParameters;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.Collections;

/**
 * This sample demonstrates how to create an agent with the Browser Automation tool
 * to interact with web pages.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 *   <li>BROWSER_AUTOMATION_PROJECT_CONNECTION_ID - The browser automation connection ID.</li>
 * </ul>
 */
public class BrowserAutomationSync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String connectionId = Configuration.getGlobalConfiguration().get("BROWSER_AUTOMATION_PROJECT_CONNECTION_ID");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        // BEGIN: com.azure.ai.agents.define_browser_automation
        // Create browser automation tool with connection configuration
        BrowserAutomationPreviewTool browserTool = new BrowserAutomationPreviewTool(
            new BrowserAutomationToolParameters(
                new BrowserAutomationToolConnectionParameters(connectionId)
            )
        );
        // END: com.azure.ai.agents.define_browser_automation

        // Create agent with browser automation tool
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that can interact with web pages.")
            .setTools(Collections.singletonList(browserTool));

        AgentVersionDetails agent = agentsClient.createAgentVersion("browser-agent", agentDefinition);
        System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

        try {
            // Create a response
            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            Response response = responsesClient.createWithAgent(
                agentReference,
                ResponseCreateParams.builder()
                    .input("Navigate to microsoft.com and summarize the main content"));

            System.out.println("Response: " + response.output());
        } finally {
            // Clean up
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }
}
