// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.conversations;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.conversations.Conversation;
import com.openai.services.blocking.ConversationService;

public class GetConversation {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String conversationId = "your-conversation-id"; // Replace with actual conversation ID
        // Code sample for retrieving a conversation
        ConversationService conversationService = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildOpenAIClient()
                .conversations();

        Conversation conversation = conversationService.retrieve(conversationId);

        System.out.println("Conversation ID: " + conversation.id());
        System.out.println("Conversation Created At: " + conversation.createdAt());
        System.out.println("Conversation Metadata: " + conversation._metadata());
    }
}
