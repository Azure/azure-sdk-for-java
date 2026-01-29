// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.conversations.ConversationDeletedResource;

public class DeleteConversation {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT");
        String conversationId = "your-conversation-id"; // Replace with actual conversation ID
        // Code sample for deleting a conversation
        ConversationsClient conversationsClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildConversationsClient();

        ConversationDeletedResource deletedConversation = conversationsClient.getConversationService().delete(conversationId);

        System.out.println("Deleted conversation with the following details:");
        System.out.println("\tConversation ID: " + deletedConversation.id());
        System.out.println("\tConversation was deleted: " + deletedConversation.deleted());
    }
}
