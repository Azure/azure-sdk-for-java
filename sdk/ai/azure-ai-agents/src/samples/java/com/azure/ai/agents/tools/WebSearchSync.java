// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.SampleUtils;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.ApproximateLocation;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.WebSearchPreviewTool;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.util.IterableStream;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseStreamEvent;
import com.openai.models.responses.ToolChoiceOptions;

import java.util.Collections;

/**
 * Demonstrates streaming a response from the preview Web Search tool with approximate location and citations.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class WebSearchSync {
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
            // BEGIN: com.azure.ai.agents.define_web_search
            WebSearchPreviewTool tool = new WebSearchPreviewTool()
                .setUserLocation(new ApproximateLocation()
                    .setCountry("GB")
                    .setRegion("London")
                    .setCity("London"));
            // END: com.azure.ai.agents.define_web_search
            agent = agentsClient.createAgentVersion("web-search-preview-agent",
                new CreateAgentVersionInput(new PromptAgentDefinition(model)
                    .setInstructions("Search the web for current information and cite sources.")
                    .setTools(Collections.singletonList(tool))));

            ResponseAccumulator accumulator = ResponseAccumulator.create();
            IterableStream<ResponseStreamEvent> events = responsesClient.createStreamingAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(SampleUtils.toAgentReference(agent)),
                ResponseCreateParams.builder()
                    .input("Show the latest London Underground service updates.")
                    .toolChoice(ToolChoiceOptions.REQUIRED));
            for (ResponseStreamEvent event : events) {
                accumulator.accumulate(event);
                event.outputTextDelta().ifPresent(delta -> System.out.print(delta.delta()));
            }
            System.out.println();
            Response response = accumulator.response();
            ToolSampleUtils.printUrlCitations(response);
        } finally {
            if (agent != null) {
                agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
            }
        }
    }
}
