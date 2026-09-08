// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.ProtocolConfiguration;
import com.azure.ai.agents.models.ResponsesProtocolConfiguration;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;
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

import java.util.Collections;

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
        ConversationService conversationsClient = builder.buildOpenAIClient().conversations();

        // Setting up the conversation with initial messages
        Conversation conversation = startConversation(conversationsClient);
        addMessageToConversation(conversationsClient, conversation.id(),
            "If the user prompt is missing the location in their prompt, assume they are talking about Berlin, Germany.", EasyInputMessage.Role.SYSTEM);
        addMessageToConversation(conversationsClient, conversation.id(), "What's the weather like?", EasyInputMessage.Role.USER);

        printConversationItems(conversationsClient, conversation.id(), 2);

        // creating a new agent and their references for future responses
        AgentVersionDetails agent1 = createPromptAgent(agentsClient, model, "weather-agent-1");
        AgentVersionDetails agent2 = createPromptAgent(agentsClient, model, "weather-agent-2");

        AgentEndpointConfig agent1EndpointConfig = new AgentEndpointConfig()
            .setVersionSelector(new VersionSelector().setVersionSelectionRules(Collections.singletonList(
                new FixedRatioVersionSelectionRule(100).setAgentVersion(agent1.getVersion()))))
            .setProtocolConfiguration(new ProtocolConfiguration().setResponses(new ResponsesProtocolConfiguration()));
        agentsClient.updateAgentDetails(agent1.getName(),
            new UpdateAgentDetailsOptions().setAgentEndpoint(agent1EndpointConfig));

        AgentEndpointConfig agent2EndpointConfig = new AgentEndpointConfig()
            .setVersionSelector(new VersionSelector().setVersionSelectionRules(Collections.singletonList(
                new FixedRatioVersionSelectionRule(100).setAgentVersion(agent2.getVersion()))))
            .setProtocolConfiguration(new ProtocolConfiguration().setResponses(new ResponsesProtocolConfiguration()));
        agentsClient.updateAgentDetails(agent2.getName(),
            new UpdateAgentDetailsOptions().setAgentEndpoint(agent2EndpointConfig));
        OpenAIClient agent1Client = builder.buildAgentScopedOpenAIClient(agent1.getName());
        OpenAIClient agent2Client = builder.buildAgentScopedOpenAIClient(agent2.getName());

        // Get response from agent1
        Response response = agent1Client.responses().create(ResponseCreateParams.builder()
            .conversation(conversation.id())
            .build());
        System.out.println("Agent response from: " + agent1.getName());
        System.out.println("\tResponse: " + response.output().get(0).asMessage().content().get(0).asOutputText().text());

        // Add clarification to the conversation
        addMessageToConversation(conversationsClient, conversation.id(),
                "You can make assumptions based on historical data. Today is October 7th.", EasyInputMessage.Role.USER);
        printConversationItems(conversationsClient, conversation.id(), 3);

        // Get follow-up response from agent1
        Response followUpResponse = agent1Client.responses().create(ResponseCreateParams.builder()
            .conversation(conversation.id())
            .build());
        System.out.println("Agent response from: " + agent1.getName());
        System.out.println("\tResponse: " + followUpResponse.output().get(0).asMessage().content().get(0).asOutputText().text());

        // Provide all the past context and more to agent2
        addMessageToConversation(conversationsClient, conversation.id(),
                "Provide suggestions opposite of what historical data indicates.", EasyInputMessage.Role.SYSTEM);
        printConversationItems(conversationsClient, conversation.id(), 4);

        Response newMessageThread = agent2Client.responses().create(ResponseCreateParams.builder()
            .conversation(conversation.id())
            .build());
        System.out.println("Agent response from: " + agent2.getName());
        System.out.println("\tResponse: " + newMessageThread.output().get(0).asMessage().content().get(0).asOutputText().text());
    }

    private static AgentVersionDetails createPromptAgent(AgentsClient agentsClient, String model, String name) {
        PromptAgentDefinition request = new PromptAgentDefinition(model);
        return agentsClient.createAgentVersion(name, request);
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

    private static void printConversationItems(ConversationService conversationsClient, String conversationId, int limit) {
        System.out.println("Printing conversation items:");
        ItemListPage page = conversationsClient.items().list(conversationId);
        page.autoPager().stream().limit(limit).forEach(item -> {
            System.out.println("\t" + item.asMessage().role() + ": " + item.asMessage().content().get(0).asInputText().text());
        });
        System.out.println("End of conversation items.\n");
    }
}
