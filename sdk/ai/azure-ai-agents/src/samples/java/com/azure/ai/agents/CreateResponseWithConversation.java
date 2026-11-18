// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.models.conversations.Conversation;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.services.blocking.ConversationService;

/**
 * This sample demonstrates how to invoke the OpenAI Responses API against a Prompt Agent,
 * using the same agent-scoped client for conversation and response operations without configuring the endpoint.
 *
 * <p>Set {@code FOUNDRY_PROJECT_ENDPOINT} and {@code FOUNDRY_MODEL_NAME} before running.
 * Optionally set {@code FOUNDRY_AGENT_NAME} to a new agent name; the default is {@code my-agent}.</p>
 */
public class CreateResponseWithConversation {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String agentName = Configuration.getGlobalConfiguration().get("FOUNDRY_AGENT_NAME", "my-agent");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .serviceVersion(AgentsServiceVersion.getLatest())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        OpenAIClient openAIClient = builder.buildAgentScopedOpenAIClient(agentName);
        ConversationService conversationService = openAIClient.conversations();

        AgentVersionDetails agent = null;
        String conversationId = null;

        try {
            // Create a prompt agent
            PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
                .setInstructions("You are a helpful assistant.");

            agent = agentsClient.createAgentVersion(agentName, agentDefinition);
            System.out.printf("Agent created (id: %s, version: %s)\n", agent.getId(), agent.getVersion());

            // Create a conversation
            Conversation conversation = conversationService.create();
            conversationId = conversation.id();
            System.out.println("Created conversation: " + conversationId);

            // Create a response using the conversation
            Response response = openAIClient.responses().create(
                ResponseCreateParams.builder()
                    .conversation(conversationId)
                    .input("Hi, how can you help me?")
                    .build());

            // Process and display the response
            System.out.println("\n=== Agent Response ===");
            for (ResponseOutputItem outputItem : response.output()) {
                if (outputItem.message().isPresent()) {
                    ResponseOutputMessage message = outputItem.message().get();
                    message.content().forEach(content -> {
                        content.outputText().ifPresent(text -> {
                            System.out.println("Assistant: " + text.text());
                        });
                    });
                }
            }
            System.out.println("Response ID: " + response.id());
        } finally {
            try {
                if (conversationId != null) {
                    conversationService.delete(conversationId);
                    System.out.println("Conversation deleted.");
                }
            } finally {
                if (agent != null) {
                    agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
                    System.out.println("Agent version deleted.");
                }
            }
        }
    }
}
