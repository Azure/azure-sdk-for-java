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
 * This sample shows how multiple agents can consume a centralized context source (conversation) and provide different
 * responses based on it.
 *
 * <p>Each agent's identity is baked into its agent endpoint URL. To route requests to a specific agent, build an
 * agent-scoped OpenAI client and call {@code responses().create(...)} through it.</p>
 */
public class MultipleAgentsConversation {
    /**
     * @param args unused
     */
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");

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

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model);

        // Create two agents and configure each agent's endpoint to route 100% of traffic to the newly created version.
        String agent1Name = "weather-agent-1";
        String agent2Name = "weather-agent-2";
        AgentVersionDetails agent1 = agentsClient.createAgentVersion(agent1Name, agentDefinition);
        try {
            configureAgentEndpoint(agentsClient, agent1Name, agent1.getVersion());

            AgentVersionDetails agent2 = agentsClient.createAgentVersion(agent2Name, agentDefinition);
            try {
                configureAgentEndpoint(agentsClient, agent2Name, agent2.getVersion());

                OpenAIClient agent1Client = builder.buildAgentScopedOpenAIClient(agent1Name);
                OpenAIClient agent2Client = builder.buildAgentScopedOpenAIClient(agent2Name);

                // Get response from agent1
                Response response = agent1Client.responses().create(ResponseCreateParams.builder()
                    .conversation(conversation.id())
                    .build());
                System.out.println("Agent response from: " + agent1Name);
                System.out.println("\tResponse: " + response.output().get(0).asMessage().content().get(0).asOutputText().text());

                // Add clarification to the conversation
                addMessageToConversation(conversationsClient, conversation.id(),
                    "You can make assumptions based on historical data. Today is October 7th.", EasyInputMessage.Role.USER);
                printConversationItems(conversationsClient, conversation.id(), 3);

                // Get follow-up response from agent1
                Response followUpResponse = agent1Client.responses().create(ResponseCreateParams.builder()
                    .conversation(conversation.id())
                    .build());
                System.out.println("Agent response from: " + agent1Name);
                System.out.println("\tResponse: " + followUpResponse.output().get(0).asMessage().content().get(0).asOutputText().text());

                // Provide all the past context and more to agent2
                addMessageToConversation(conversationsClient, conversation.id(),
                    "Provide suggestions opposite of what historical data indicates.", EasyInputMessage.Role.SYSTEM);
                printConversationItems(conversationsClient, conversation.id(), 4);

                Response newMessageThread = agent2Client.responses().create(ResponseCreateParams.builder()
                    .conversation(conversation.id())
                    .build());
                System.out.println("Agent response from: " + agent2Name);
                System.out.println("\tResponse: " + newMessageThread.output().get(0).asMessage().content().get(0).asOutputText().text());
            } finally {
                agentsClient.deleteAgentVersion(agent2Name, agent2.getVersion());
            }
        } finally {
            agentsClient.deleteAgentVersion(agent1Name, agent1.getVersion());
        }
    }

    // Points the agent endpoint at the specified version and enables the OpenAI Responses protocol on it.
    private static void configureAgentEndpoint(AgentsClient agentsClient, String agentName, String agentVersion) {
        agentsClient.updateAgentDetails(agentName, new UpdateAgentDetailsOptions().setAgentEndpoint(
            new AgentEndpointConfig()
                .setVersionSelector(new VersionSelector().setVersionSelectionRules(Collections.singletonList(
                    new FixedRatioVersionSelectionRule(100).setAgentVersion(agentVersion))))
                .setProtocolConfiguration(new ProtocolConfiguration().setResponses(new ResponsesProtocolConfiguration()))));
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
