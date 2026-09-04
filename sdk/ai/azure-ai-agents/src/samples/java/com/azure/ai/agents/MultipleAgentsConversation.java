// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.models.conversations.Conversation;
import com.openai.models.conversations.Message;
import com.openai.models.conversations.items.ItemCreateParams;
import com.openai.models.conversations.items.ItemListPage;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.services.blocking.ConversationService;

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
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        // Code sample for creating an agent
        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .serviceVersion(AgentsServiceVersion.getLatest())
            .endpoint(endpoint);
        AgentsClient agentsClient = builder.buildAgentsClient();
        OpenAIClient openAIClient = builder.buildOpenAIClient();
        ResponsesClient responsesClient = new ResponsesClient(openAIClient);
        ConversationService conversationsClient = openAIClient.conversations();

        Conversation conversation = null;
        AgentVersionDetails agent1 = null;
        AgentVersionDetails agent2 = null;
        try {
            // Setting up the conversation with initial messages
            conversation = startConversation(conversationsClient);
            addMessageToConversation(conversationsClient, conversation.id(),
                "If the user prompt is missing the location in their prompt, assume they are talking about Berlin, Germany.",
                EasyInputMessage.Role.SYSTEM);
            addMessageToConversation(conversationsClient, conversation.id(), "What's the weather like?",
                EasyInputMessage.Role.USER);

            printConversationItems(conversationsClient, conversation.id(), 2);

            // creating a new agent and their references for future responses
            agent1 = createPromptAgent(agentsClient, model, "weather-agent-1");
            agent2 = createPromptAgent(agentsClient, model, "weather-agent-2");

            AgentReference agent1Reference = new AgentReference(agent1.getName()).setVersion(agent1.getVersion());
            AgentReference agent2Reference = new AgentReference(agent2.getName()).setVersion(agent2.getVersion());

            // Get response from agent1
            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(agent1Reference),
                ResponseCreateParams.builder().conversation(conversation.id()));
            System.out.println("Agent response from: " + agent1.getName());
            SampleUtils.printResponseText(response);

            // Add clarification to the conversation
            addMessageToConversation(conversationsClient, conversation.id(),
                "You can make assumptions based on historical data. Today is October 7th.", EasyInputMessage.Role.USER);
            printConversationItems(conversationsClient, conversation.id(), 3);

            // Get follow-up response from agent1
            Response followUpResponse = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(agent1Reference),
                ResponseCreateParams.builder().conversation(conversation.id()));
            System.out.println("Agent response from: " + agent1.getName());
            SampleUtils.printResponseText(followUpResponse);

            // Provide all the past context and more to agent2
            addMessageToConversation(conversationsClient, conversation.id(),
                "Provide suggestions opposite of what historical data indicates.", EasyInputMessage.Role.SYSTEM);
            printConversationItems(conversationsClient, conversation.id(), 4);

            Response newMessageThread = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(agent2Reference),
                ResponseCreateParams.builder().conversation(conversation.id()));
            System.out.println("Agent response from: " + agent2.getName());
            SampleUtils.printResponseText(newMessageThread);
        } finally {
            try {
                if (conversation != null) {
                    conversationsClient.delete(conversation.id());
                }
                if (agent2 != null) {
                    agentsClient.deleteAgentVersion(agent2.getName(), agent2.getVersion());
                }
                if (agent1 != null) {
                    agentsClient.deleteAgentVersion(agent1.getName(), agent1.getVersion());
                }
            } finally {
                openAIClient.close();
            }
        }
    }

    private static AgentVersionDetails createPromptAgent(AgentsClient agentsClient, String model, String name) {
        PromptAgentDefinition request = new PromptAgentDefinition(model);
        return agentsClient.createAgentVersion(name, new CreateAgentVersionInput(request));
    }

    private static Conversation startConversation(ConversationService conversationsClient) {
        return conversationsClient.create();
    }

    private static void addMessageToConversation(ConversationService conversationService, String conversationId, String content, EasyInputMessage.Role role) {
        ItemCreateParams itemParams = ItemCreateParams.builder()
            .conversationId(conversationId)
            .addItem(
                EasyInputMessage.builder()
                    .content(content)
                    .type(EasyInputMessage.Type.MESSAGE)
                    .role(role).build()
            ).build();

        conversationService.items().create(itemParams);
    }

    private static void printConversationItems(ConversationService conversationsClient, String conversationId,
        int limit) {
        System.out.println("Printing conversation items:");
        ItemListPage page = conversationsClient.items().list(conversationId);
        page.autoPager().stream().filter(item -> item.isMessage()).limit(limit).forEach(item -> {
            Message message = item.asMessage();
            message.content().stream().map(MultipleAgentsConversation::getText).filter(text -> text != null)
                .forEach(text -> System.out.println("\t" + message.role() + ": " + text));
        });
        System.out.println("End of conversation items.\n");
    }

    private static String getText(Message.Content content) {
        if (content.isInputText()) {
            return content.asInputText().text();
        } else if (content.isOutputText()) {
            return content.asOutputText().text();
        } else if (content.isText()) {
            return content.asText().text();
        }
        return null;
    }
}
