// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.models.conversations.Conversation;
import com.openai.models.conversations.items.ItemCreateParams;
import com.openai.models.conversations.items.ItemListPage;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.services.blocking.ConversationService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This sample demonstrates passing conversation history between agents, each using its own agent-scoped conversation.
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
        AgentVersionDetails agent1 = createPromptAgent(agentsClient, model, "weather-agent-1");
        AgentVersionDetails agent2 = createPromptAgent(agentsClient, model, "weather-agent-2");
        OpenAIClient agent1Client = builder.buildAgentScopedOpenAIClient(agent1.getName());
        OpenAIClient agent2Client = builder.buildAgentScopedOpenAIClient(agent2.getName());
        ConversationService conversationsClient = agent1Client.conversations();
        List<EasyInputMessage> history = new ArrayList<>();

        // Setting up the conversation with initial messages
        Conversation conversation = startConversation(conversationsClient);
        addMessageToConversation(conversationsClient, conversation.id(), history,
            "If the user prompt is missing the location in their prompt, assume they are talking about Berlin, Germany.",
            EasyInputMessage.Role.SYSTEM);
        addMessageToConversation(conversationsClient, conversation.id(), history, "What's the weather like?",
            EasyInputMessage.Role.USER);

        printConversationItems(conversationsClient, conversation.id(), 2);

        // Get response from agent1
        Response response = agent1Client.responses().create(ResponseCreateParams.builder()
            .conversation(conversation.id())
            .build());
        System.out.println("Agent response from: " + agent1.getName());
        String responseText = getResponseText(response);
        System.out.println("\tResponse: " + responseText);
        history.add(EasyInputMessage.builder().role(EasyInputMessage.Role.ASSISTANT).content(responseText).build());

        // Add clarification to the conversation
        addMessageToConversation(conversationsClient, conversation.id(), history,
                "You can make assumptions based on historical data. Today is October 7th.", EasyInputMessage.Role.USER);
        printConversationItems(conversationsClient, conversation.id(), 3);

        // Get follow-up response from agent1
        Response followUpResponse = agent1Client.responses().create(ResponseCreateParams.builder()
            .conversation(conversation.id())
            .build());
        System.out.println("Agent response from: " + agent1.getName());
        String followUpText = getResponseText(followUpResponse);
        System.out.println("\tResponse: " + followUpText);
        history.add(EasyInputMessage.builder().role(EasyInputMessage.Role.ASSISTANT).content(followUpText).build());

        // Conversation IDs are agent-scoped, so copy the text history into the second agent's conversation.
        ConversationService secondConversationsClient = agent2Client.conversations();
        Conversation secondConversation = startConversation(secondConversationsClient);
        ItemCreateParams.Builder copiedItems = ItemCreateParams.builder().conversationId(secondConversation.id());
        history.forEach(copiedItems::addItem);
        secondConversationsClient.items().create(copiedItems.build());
        addMessageToConversation(secondConversationsClient, secondConversation.id(), history,
                "Provide suggestions opposite of what historical data indicates.", EasyInputMessage.Role.SYSTEM);
        printConversationItems(secondConversationsClient, secondConversation.id(), 6);

        Response newMessageThread = agent2Client.responses().create(ResponseCreateParams.builder()
            .conversation(secondConversation.id())
            .build());
        System.out.println("Agent response from: " + agent2.getName());
        System.out.println("\tResponse: " + getResponseText(newMessageThread));
    }

    private static AgentVersionDetails createPromptAgent(AgentsClient agentsClient, String model, String name) {
        PromptAgentDefinition request = new PromptAgentDefinition(model);
        return agentsClient.createAgentVersion(name, request);
    }

    private static Conversation startConversation(ConversationService conversationsClient) {
        return conversationsClient.create();
    }

    private static void addMessageToConversation(ConversationService conversationService, String conversationId,
        List<EasyInputMessage> history, String content, EasyInputMessage.Role role) {
        EasyInputMessage message = EasyInputMessage.builder()
            .content(content)
            .type(EasyInputMessage.Type.MESSAGE)
            .role(role)
            .build();
        ItemCreateParams itemParams = ItemCreateParams.builder()
            .conversationId(conversationId)
            .addItem(message)
            .build();

        conversationService.items().create(itemParams);
        history.add(message);
    }

    private static void printConversationItems(ConversationService conversationsClient, String conversationId, int limit) {
        System.out.println("Printing conversation items:");
        ItemListPage page = conversationsClient.items().list(conversationId);
        page.autoPager().stream().limit(limit).forEach(item -> item.message().ifPresent(message ->
            message.content().forEach(content -> {
                content.inputText().ifPresent(text -> System.out.println("\t" + message.role() + ": " + text.text()));
                content.outputText().ifPresent(text -> System.out.println("\t" + message.role() + ": " + text.text()));
            })));
        System.out.println("End of conversation items.\n");
    }

    private static String getResponseText(Response response) {
        return response.output().stream()
            .filter(item -> item.message().isPresent())
            .flatMap(item -> item.message().get().content().stream())
            .filter(content -> content.outputText().isPresent())
            .map(content -> content.outputText().get().text())
            .collect(Collectors.joining("\n"));
    }
}
