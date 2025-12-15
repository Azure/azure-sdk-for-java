// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.conversations.Conversation;
import com.openai.models.conversations.items.ItemCreateParams;
import com.openai.models.conversations.items.ItemListPage;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.Response;

/**
 * This sample how multiple agents can consume a centralized context source (conversation) and provide different responses
 * based on it.
 *
 */
public class MultipleAgentsConversation {
    /**
     * @param args unused
     */
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("AZURE_AGENT_MODEL");
        // Code sample for creating an agent
        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .serviceVersion(AgentsServiceVersion.V2025_05_15_PREVIEW)
            .endpoint(endpoint);
        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();
        ConversationsClient conversationsClient = builder.buildConversationsClient();

        // Setting up the conversation with initial messages
        Conversation conversation = startConversation(conversationsClient);
        addMessageToConversation(conversationsClient, conversation.id(),
            "If the user prompt is missing the location in their prompt, assume they are talking about Berlin, Germany.", EasyInputMessage.Role.SYSTEM);
        addMessageToConversation(conversationsClient, conversation.id(), "What's the weather like?", EasyInputMessage.Role.USER);

        printConversationItems(conversationsClient, conversation.id(), 2);

        // creating a new agent and their references for future responses
        AgentVersionDetails agent1 = createPromptAgent(agentsClient, model, "weather-agent-1");
        AgentVersionDetails agent2 = createPromptAgent(agentsClient, model, "weather-agent-2");

        AgentReference agent1Reference = new AgentReference(agent1.getName()).setVersion(agent1.getVersion());
        AgentReference agent2Reference = new AgentReference(agent2.getName()).setVersion(agent2.getVersion());

        // Get response from agent1
        Response response = responsesClient.createWithAgentConversation(agent1Reference, conversation.id());
        System.out.println("Agent response from: " + agent1.getName());
        System.out.println("\tResponse: " + response.output().get(0).asMessage().content().get(0).asOutputText().text());

        // Add clarification to the conversation
        addMessageToConversation(conversationsClient, conversation.id(),
                "You can make assumptions based on historical data. Today is October 7th.", EasyInputMessage.Role.USER);
        printConversationItems(conversationsClient, conversation.id(), 3);

        // Get follow-up response from agent1
        Response followUpResponse = responsesClient.createWithAgentConversation(agent1Reference, conversation.id());
        System.out.println("Agent response from: " + agent1.getName());
        System.out.println("\tResponse: " + followUpResponse.output().get(0).asMessage().content().get(0).asOutputText().text());

        // Provide all the past context and more to agent2
        addMessageToConversation(conversationsClient, conversation.id(),
                "Provide suggestions opposite of what historical data indicates.", EasyInputMessage.Role.SYSTEM);
        printConversationItems(conversationsClient, conversation.id(), 4);

        Response newMessageThread = responsesClient.createWithAgentConversation(agent2Reference, conversation.id());
        System.out.println("Agent response from: " + agent2.getName());
        System.out.println("\tResponse: " + newMessageThread.output().get(0).asMessage().content().get(0).asOutputText().text());
    }

    private static AgentVersionDetails createPromptAgent(AgentsClient agentsClient, String model, String name) {
        PromptAgentDefinition request = new PromptAgentDefinition(model);
        return agentsClient.createAgentVersion(name, request);
    }

    private static Conversation startConversation(ConversationsClient conversationsClient) {
        return conversationsClient.getConversationService().create();
    }

    private static void addMessageToConversation(ConversationsClient conversationsClient, String conversationId, String content, EasyInputMessage.Role role) {
        ItemCreateParams itemParams = ItemCreateParams.builder()
            .conversationId(conversationId)
            .addItem(
                EasyInputMessage.builder()
                    .content(content)
                    .type(EasyInputMessage.Type.MESSAGE)
                    .role(role).build()
            ).build();

        conversationsClient.getConversationService().items().create(itemParams);
    }

    private static void printConversationItems(ConversationsClient conversationsClient, String conversationId, int limit) {
        System.out.println("Printing conversation items:");
        ItemListPage page = conversationsClient.getConversationService().items().list(conversationId);
        page.autoPager().stream().limit(limit).forEach(item -> {
            System.out.println("\t" + item.asMessage().role() + ": " + item.asMessage().content().get(0).asInputText().text());
        });
        System.out.println("End of conversation items.\n");
    }
}
