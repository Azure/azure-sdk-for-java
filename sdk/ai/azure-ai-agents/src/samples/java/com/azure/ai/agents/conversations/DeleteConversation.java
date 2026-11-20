// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.conversations;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.conversations.ConversationDeletedResource;
import com.openai.services.blocking.ConversationService;

public class DeleteConversation {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String conversationId = "your-conversation-id"; // Replace with actual conversation ID
        // Code sample for deleting a conversation
        ConversationService conversationService = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildOpenAIClient()
                .conversations();

        ConversationDeletedResource deletedConversation = conversationService.delete(conversationId);

        System.out.println("Deleted conversation with the following details:");
        System.out.println("\tConversation ID: " + deletedConversation.id());
        System.out.println("\tConversation was deleted: " + deletedConversation.deleted());
    }
}
