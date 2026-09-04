// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentDetails;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
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
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.services.blocking.ConversationService;

import java.util.Collections;

/**
 * Demonstrates synchronous prompt-agent creation, endpoint routing, and a multi-turn conversation.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class AgentBasicSample {
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

        String agentName = "basic-agent";
        AgentVersionDetails agent = null;
        AgentEndpointConfig originalEndpoint = null;
        String conversationId = null;

        try {
            agent = agentsClient.createAgentVersion(agentName,
                new CreateAgentVersionInput(new PromptAgentDefinition(model)
                    .setInstructions("You are a helpful assistant that answers general questions.")));
            System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

            AgentDetails details = agentsClient.getAgent(agentName);
            originalEndpoint = details.getAgentEndpoint();
            AgentEndpointConfig endpointConfig = new AgentEndpointConfig()
                .setVersionSelector(new VersionSelector().setVersionSelectionRules(Collections.singletonList(
                    new FixedRatioVersionSelectionRule(100).setAgentVersion(agent.getVersion()))))
                .setProtocolConfiguration(new ProtocolConfiguration()
                    .setResponses(new ResponsesProtocolConfiguration()));
            agentsClient.updateAgentDetails(agentName,
                new UpdateAgentDetailsOptions().setAgentEndpoint(endpointConfig));
            System.out.printf("Agent endpoint configured for version %s%n", agent.getVersion());

            Conversation conversation = conversations.create();
            conversationId = conversation.id();
            System.out.println("Conversation created: " + conversationId);

            AzureCreateResponseOptions options = new AzureCreateResponseOptions()
                .setAgentReference(SampleUtils.toAgentReference(agent));
            Response first = responsesClient.createAzureResponse(options,
                ResponseCreateParams.builder()
                    .conversation(conversationId)
                    .input("What is the size of France in square miles?"));
            SampleUtils.printResponseText(first);

            conversations.items().create(ItemCreateParams.builder()
                .conversationId(conversationId)
                .addItem(EasyInputMessage.builder()
                    .role(EasyInputMessage.Role.USER)
                    .content("What is its capital city?")
                    .build())
                .build());
            Response second = responsesClient.createAzureResponse(options,
                ResponseCreateParams.builder().conversation(conversationId));
            SampleUtils.printResponseText(second);
        } finally {
            try {
                if (conversationId != null) {
                    conversations.delete(conversationId);
                }
                if (agent != null) {
                    agentsClient.updateAgentDetails(agentName,
                        new UpdateAgentDetailsOptions().setAgentEndpoint(originalEndpoint));
                    agentsClient.deleteAgentVersion(agentName, agent.getVersion());
                }
            } finally {
                openAIClient.close();
            }
        }
    }
}
