// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.BingCustomSearchConfiguration;
import com.azure.ai.agents.models.BingCustomSearchPreviewTool;
import com.azure.ai.agents.models.BingCustomSearchToolParameters;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.Arrays;
import java.util.Collections;

/**
 * This sample demonstrates how to create an agent with the Bing Custom Search tool
 * to search custom search instances and provide responses with relevant results.
 *
 * <p><b>Warning:</b> Grounding with Bing Custom Search tool uses Grounding with Bing,
 * which has additional costs and terms.
 * See <a href="https://www.microsoft.com/bing/apis/grounding-legal-enterprise">Terms of use</a> and
 * <a href="https://go.microsoft.com/fwlink/?LinkId=521839&amp;clcid=0x409">Privacy statement</a>.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 *   <li>BING_CUSTOM_SEARCH_PROJECT_CONNECTION_ID - The Bing Custom Search project connection ID.</li>
 *   <li>BING_CUSTOM_SEARCH_INSTANCE_NAME - The Bing Custom Search instance name.</li>
 * </ul>
 */
public class BingCustomSearchSync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String connectionId = Configuration.getGlobalConfiguration().get("BING_CUSTOM_SEARCH_PROJECT_CONNECTION_ID");
        String instanceName = Configuration.getGlobalConfiguration().get("BING_CUSTOM_SEARCH_INSTANCE_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        // BEGIN: com.azure.ai.agents.define_bing_custom_search
        // Create Bing Custom Search tool with connection and instance configuration
        BingCustomSearchPreviewTool bingCustomSearchTool = new BingCustomSearchPreviewTool(
            new BingCustomSearchToolParameters(Arrays.asList(
                new BingCustomSearchConfiguration(connectionId, instanceName)
            ))
        );
        // END: com.azure.ai.agents.define_bing_custom_search

        // Create agent with Bing Custom Search tool
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful agent that can use Bing Custom Search tools to assist users. "
                + "Use the available Bing Custom Search tools to answer questions and perform tasks.")
            .setTools(Collections.singletonList(bingCustomSearchTool));

        AgentVersionDetails agent = agentsClient.createAgentVersion("bing-custom-search-agent", agentDefinition);
        System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

        try {
            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(agentReference),
                ResponseCreateParams.builder()
                    .input("Search for the latest Azure AI documentation"));

            System.out.println("Response: " + response.output());
        } finally {
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
            System.out.println("Agent deleted");
        }
    }
}
