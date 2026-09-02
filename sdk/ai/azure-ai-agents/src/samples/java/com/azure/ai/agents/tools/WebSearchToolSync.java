// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.SampleUtils;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.WebSearchApproximateLocation;
import com.azure.ai.agents.models.WebSearchTool;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ToolChoiceOptions;

import java.util.Collections;

/**
 * Demonstrates using the Web Search tool with an approximate user location.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class WebSearchToolSync {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);
        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();
        AgentVersionDetails agent = null;

        try {
            WebSearchTool tool = new WebSearchTool()
                .setUserLocation(new WebSearchApproximateLocation()
                    .setCountry("GB")
                    .setRegion("London")
                    .setCity("London"));
            agent = agentsClient.createAgentVersion("web-search-agent",
                new CreateAgentVersionInput(new PromptAgentDefinition(model)
                    .setInstructions("Search the web for current information and cite sources.")
                    .setTools(Collections.singletonList(tool))));
            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(SampleUtils.toAgentReference(agent)),
                ResponseCreateParams.builder()
                    .input("Show the latest London Underground service updates.")
                    .toolChoice(ToolChoiceOptions.REQUIRED));
            SampleUtils.printResponseText(response);
            ToolSampleUtils.printUrlCitations(response);
        } finally {
            if (agent != null) {
                agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
            }
        }
    }
}
