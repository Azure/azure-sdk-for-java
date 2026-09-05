// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.conversations;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.conversations.Conversation;
import com.openai.models.conversations.ConversationUpdateParams;
import com.openai.services.blocking.ConversationService;

/**
 * Demonstrates updating a conversation.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_CONVERSATION_ID} - The ID of the conversation to update.</li>
 * </ul>
 */
public class UpdateConversation {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String conversationId = configuration.get("FOUNDRY_CONVERSATION_ID");

        OpenAIClient openAIClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildOpenAIClient();
        try {
            ConversationService conversationService = openAIClient.conversations();
            ConversationUpdateParams.Metadata metadata = ConversationUpdateParams.Metadata.builder()
                .putAdditionalProperty("updated_by", JsonValue.from("java_sample"))
                .putAdditionalProperty("update_timestamp", JsonValue.from(String.valueOf(System.currentTimeMillis())))
                .build();
            ConversationUpdateParams updateParams = ConversationUpdateParams.builder()
                .metadata(metadata)
                .build();

            Conversation updatedConversation = conversationService.update(conversationId, updateParams);
            System.out.println("Updated Conversation ID: " + updatedConversation.id());
            System.out.println("Updated Conversation Metadata: " + updatedConversation._metadata());
        } finally {
            openAIClient.close();
        }
    }
}
