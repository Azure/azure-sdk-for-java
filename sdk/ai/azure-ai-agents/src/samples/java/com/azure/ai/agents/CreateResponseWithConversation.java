// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.conversations.Conversation;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.services.blocking.ConversationService;

/**
 * This sample demonstrates how to use the createWithAgentConversation helper method
 * to create a response with a conversation.
 */
public class CreateResponseWithConversation {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .serviceVersion(AgentsServiceVersion.getLatest())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ConversationService conversationService = builder.buildOpenAIClient().conversations();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        AgentVersionDetails agent = null;
        String conversationId = null;

        try {
            // Create a prompt agent
            PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
                .setInstructions("You are a helpful assistant.");

            agent = agentsClient.createAgentVersion("my-agent", agentDefinition);
            System.out.printf("Agent created (id: %s, version: %s)\n", agent.getId(), agent.getVersion());

            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            // Create a conversation
            Conversation conversation = conversationService.create();
            conversationId = conversation.id();
            System.out.println("Created conversation: " + conversationId);

            // Create a response using the conversation
            Response response = responsesClient.createWithAgentConversation(
                agentReference,
                conversationId,
                ResponseCreateParams.builder().input("Hi, how can you help me?"));

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
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cleanup conversation
            if (conversationId != null) {
                conversationService.delete(conversationId);
                System.out.println("Conversation deleted.");
            }
            // Cleanup agent
            if (agent != null) {
                agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
                System.out.println("Agent deleted.");
            }
        }
    }
}
