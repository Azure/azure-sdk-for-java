// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.core.JsonValue;
import com.openai.models.conversations.Conversation;
import com.openai.models.conversations.ConversationUpdateParams;

public class UpdateConversation {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT");
        String conversationId = "your-conversation-id"; // Replace with actual conversation ID
        // Code sample for updating a conversation
        ConversationsClient conversationsClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildConversationsClient();

        // Create metadata for the update
        ConversationUpdateParams.Metadata metadata = ConversationUpdateParams.Metadata.builder()
                .putAdditionalProperty("updated_by", JsonValue.from("java_sample"))
                .putAdditionalProperty("update_timestamp", JsonValue.from(System.currentTimeMillis()))
                .build();

        ConversationUpdateParams updateParams = ConversationUpdateParams.builder()
                .metadata(metadata)
                .build();

        Conversation updatedConversation = conversationsClient.getConversationService().update(conversationId, updateParams);

        System.out.println("Updated Conversation ID: " + updatedConversation.id());
        System.out.println("Updated Conversation Metadata: " + updatedConversation._metadata());
    }
}
