// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.conversations.Conversation;

public class CreateConversation {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT");
        // Code sample for creating a conversation
        ConversationsClient conversationsClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildConversationsClient();

        Conversation conversation = conversationsClient.getOpenAIClient().create();

        System.out.println("Conversation ID: " + conversation.id());
        System.out.println("Conversation Created At: " + conversation.createdAt());
    }
}
