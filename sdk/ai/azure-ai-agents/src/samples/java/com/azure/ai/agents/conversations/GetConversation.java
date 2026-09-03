// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.conversations;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.conversations.Conversation;
import com.openai.services.blocking.ConversationService;

/**
 * Demonstrates creating and retrieving a conversation.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 * </ul>
 */
public class GetConversation {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);
        ConversationService conversations = builder.buildOpenAIClient().conversations();
        Conversation created = conversations.create();
        try {
            Conversation retrieved = conversations.retrieve(created.id());
            System.out.println("Conversation ID: " + retrieved.id());
            System.out.println("Created at: " + retrieved.createdAt());
            System.out.println("Metadata: " + retrieved._metadata());
        } finally {
            conversations.delete(created.id());
        }
    }
}
