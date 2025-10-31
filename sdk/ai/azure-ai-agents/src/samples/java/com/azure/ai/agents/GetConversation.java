// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.conversations.Conversation;

public class GetConversation {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT");
        String conversationId = "your-conversation-id"; // Replace with actual conversation ID
        // Code sample for retrieving a conversation
        ConversationsClient conversationsClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildConversationsClient();

        Conversation conversation = conversationsClient.getOpenAIClient().retrieve(conversationId);

        System.out.println("Conversation ID: " + conversation.id());
        System.out.println("Conversation Created At: " + conversation.createdAt());
        System.out.println("Conversation Metadata: " + conversation._metadata());
    }
}
