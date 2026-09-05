// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentDetails;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.models.conversations.Conversation;
import com.openai.models.conversations.items.ItemCreateParams;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.services.blocking.ConversationService;

/**
 * Demonstrates synchronously retrieving an agent and conversation before creating a response.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class AgentRetrieveBasicSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);
        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();
        OpenAIClient openAIClient = builder.buildOpenAIClient();
        ConversationService conversations = openAIClient.conversations();

        String agentName = "retrieve-agent";
        AgentVersionDetails agent = null;
        String conversationId = null;

        try {
            agent = agentsClient.createAgentVersion(agentName,
                new CreateAgentVersionInput(new PromptAgentDefinition(model)
                    .setInstructions("You are a helpful assistant.")));

            AgentDetails retrievedAgent = agentsClient.getAgent(agentName);
            System.out.printf("Retrieved agent: %s (%s), latest version: %s%n", retrievedAgent.getName(),
                retrievedAgent.getId(), retrievedAgent.getVersions().getLatest().getVersion());

            Conversation conversation = conversations.create();
            conversationId = conversation.id();
            System.out.println("Conversation created: " + conversationId);

            Conversation retrievedConversation = conversations.retrieve(conversationId);
            System.out.println("Retrieved conversation: " + retrievedConversation.id());

            conversations.items().create(ItemCreateParams.builder()
                .conversationId(conversationId)
                .addItem(EasyInputMessage.builder()
                    .role(EasyInputMessage.Role.USER)
                    .content("How many feet are in a mile?")
                    .build())
                .build());
            System.out.println("Added a user message to the conversation");

            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(SampleUtils.toAgentReference(agent)),
                ResponseCreateParams.builder().conversation(conversationId));
            SampleUtils.printResponseText(response);
        } finally {
            try {
                if (conversationId != null) {
                    conversations.delete(conversationId);
                }
                if (agent != null) {
                    agentsClient.deleteAgentVersion(agentName, agent.getVersion());
                }
            } finally {
                openAIClient.close();
            }
        }
    }
}
