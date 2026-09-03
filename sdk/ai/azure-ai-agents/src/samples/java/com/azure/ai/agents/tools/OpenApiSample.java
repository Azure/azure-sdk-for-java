// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.SampleUtils;
import com.azure.ai.agents.models.OpenApiAnonymousAuthDetails;
import com.azure.ai.agents.models.OpenApiFunctionDefinition;
import com.azure.ai.agents.models.OpenApiTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.models.conversations.Conversation;
import com.openai.models.conversations.items.ItemCreateParams;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.services.blocking.ConversationService;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.ProtocolConfiguration;
import com.azure.ai.agents.models.ResponsesProtocolConfiguration;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * This sample demonstrates how to create an agent with an OpenAPI tool that calls
 * an external API defined by an OpenAPI specification file.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 * </ul>
 *
 * <p>Also place an OpenAPI spec JSON file at {@code src/samples/resources/assets/httpbin_openapi.json}.</p>
 */
public class OpenApiSample {
    public static void main(String[] args) throws Exception {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ConversationService conversationService = builder.buildOpenAIClient().conversations();


        // Load the OpenAPI spec from a JSON file
        Map<String, BinaryData> spec = OpenApiFunctionDefinition.readSpecFromFile(
            SampleUtils.getResourcePath("assets/httpbin_openapi.json"));

        OpenApiFunctionDefinition toolDefinition = new OpenApiFunctionDefinition(
            "httpbin_get",
            spec,
            new OpenApiAnonymousAuthDetails())
            .setDescription("Get request metadata from an OpenAPI endpoint.");

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("Use the OpenAPI tool for HTTP request metadata.")
            .setTools(Arrays.asList(new OpenApiTool(toolDefinition)));

        String agentName = "openapi-agent";
        AgentVersionDetails agent = agentsClient.createAgentVersion(agentName, agentDefinition);
        try {
            agentsClient.updateAgentDetails(agentName, new UpdateAgentDetailsOptions().setAgentEndpoint(
                new AgentEndpointConfig()
                    .setVersionSelector(new VersionSelector().setVersionSelectionRules(Collections.singletonList(
                        new FixedRatioVersionSelectionRule(100).setAgentVersion(agent.getVersion()))))
                    .setProtocolConfiguration(new ProtocolConfiguration().setResponses(new ResponsesProtocolConfiguration()))));


            OpenAIClient openAIClient = builder.buildAgentScopedOpenAIClient(agentName);

            // Create a conversation and add a user message
            Conversation conversation = conversationService.create();
            conversationService.items().create(
                ItemCreateParams.builder()
                    .conversationId(conversation.id())
                    .addItem(EasyInputMessage.builder()
                        .role(EasyInputMessage.Role.USER)
                        .content("Use the OpenAPI tool and summarize the returned URL and origin in one sentence.")
                        .build())
                    .build());

            Response response = openAIClient.responses().create(ResponseCreateParams.builder()
                .maxOutputTokens(300L)
                .conversation(conversation.id())
                .build());

            String text = response.output().stream()
                .filter(item -> item.isMessage())
                .map(item -> item.asMessage().content()
                    .get(item.asMessage().content().size() - 1)
                    .asOutputText()
                    .text())
                .reduce((first, second) -> second)
                .orElse("<no message output>");

            System.out.println("Status: " + response.status().map(Object::toString).orElse("unknown"));
            System.out.println("Response: " + text);
        } finally {
            agentsClient.deleteAgentVersion(agentName, agent.getVersion());
        }
    }
}
