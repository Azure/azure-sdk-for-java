// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.SampleUtils;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.OpenApiAnonymousAuthDetails;
import com.azure.ai.agents.models.OpenApiFunctionDefinition;
import com.azure.ai.agents.models.OpenApiTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.conversations.Conversation;
import com.openai.models.conversations.items.ItemCreateParams;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.services.blocking.ConversationService;

import java.util.Arrays;
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
public class OpenApiSync {
    public static void main(String[] args) throws Exception {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();
        ConversationService conversationService = builder.buildOpenAIClient().conversations();


        // BEGIN: com.azure.ai.agents.define_openapi
        // Load the OpenAPI spec from a JSON file
        Map<String, BinaryData> spec = OpenApiFunctionDefinition.readSpecFromFile(
            SampleUtils.getResourcePath("assets/httpbin_openapi.json"));

        OpenApiTool tool = new OpenApiTool(
            new OpenApiFunctionDefinition(
                "httpbin_get",
                spec,
                new OpenApiAnonymousAuthDetails())
                .setDescription("Get request metadata from an OpenAPI endpoint."));
        // END: com.azure.ai.agents.define_openapi

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("Use the OpenAPI tool for HTTP request metadata.")
            .setTools(Arrays.asList(tool));

        AgentVersionDetails agentVersion = agentsClient.createAgentVersion("openapi-agent", agentDefinition);
        System.out.println("Agent: " + agentVersion.getName() + ", version: " + agentVersion.getVersion());

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

        try {
            AgentReference agentReference = new AgentReference(agentVersion.getName())
                .setVersion(agentVersion.getVersion());

            ResponseCreateParams.Builder options = ResponseCreateParams.builder()
                .maxOutputTokens(300L);

            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(agentReference),
                options.conversation(conversation.id()));

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
            agentsClient.deleteAgentVersion(agentVersion.getName(), agentVersion.getVersion());
            System.out.println("Agent deleted");
        }
    }
}
