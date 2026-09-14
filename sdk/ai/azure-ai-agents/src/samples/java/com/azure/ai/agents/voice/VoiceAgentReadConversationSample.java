// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.BetaAgentEndpointConversationsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.RealtimeConversationItem;
import com.azure.ai.agents.models.VoiceConversation;
import com.azure.ai.agents.models.VoiceResponse;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Map;

/**
 * Demonstrates reading a persisted voice conversation, its responses, and transcript items.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_VOICE_AGENT_NAME} - The voice agent name.</li>
 *   <li>{@code FOUNDRY_VOICE_CONVERSATION_ID} - The persisted voice conversation ID.</li>
 * </ul>
 */
public class VoiceAgentReadConversationSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String agentName = configuration.get("FOUNDRY_VOICE_AGENT_NAME");
        String conversationId = configuration.get("FOUNDRY_VOICE_CONVERSATION_ID");
        BetaAgentEndpointConversationsClient conversations = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildBetaAgentEndpointConversationsClient();

        VoiceConversation conversation = conversations.getAgentConversation(agentName, conversationId);
        System.out.printf("Conversation %s: status=%s, created=%s, usage=%s%n",
            conversation.getId(), conversation.getStatus(), conversation.getCreatedAt(), conversation.getUsage());

        for (VoiceResponse response : conversations.listAgentConversationResponses(agentName, conversationId)) {
            VoiceResponse detail = conversations.getAgentConversationResponse(agentName, conversationId,
                response.getId());
            System.out.printf("Response %s: status=%s, usage=%s%n",
                detail.getId(), detail.getStatus(), detail.getUsage());
            for (RealtimeConversationItem item : conversations.listAgentConversationResponseItems(
                agentName, conversationId, response.getId())) {
                System.out.println("  Response item type: " + item.getType());
            }
        }

        for (BinaryData itemData : conversations.listAgentConversationItems(agentName, conversationId,
            new RequestOptions())) {
            @SuppressWarnings("unchecked")
            Map<String, Object> item = itemData.toObject(Map.class);
            String itemId = (String) item.get("id");
            System.out.printf("Transcript item: type=%s, id=%s%n", item.get("type"), itemId);
            if (itemId != null) {
                RealtimeConversationItem fetched = conversations.getAgentConversationItem(agentName,
                    conversationId, itemId);
                System.out.println("  Fetched item type: " + fetched.getType());
            }
        }
    }
}
