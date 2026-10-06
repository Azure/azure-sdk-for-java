// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.conversations;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.core.JsonValue;
import com.openai.models.conversations.Conversation;
import com.openai.models.conversations.ConversationUpdateParams;
import com.openai.models.conversations.ConversationDeletedResource;
import com.openai.services.blocking.ConversationService;

/**
 * Demonstrates applying the {@code x-ms-user-identity} header to OpenAI conversation calls.
 *
 * <p>Set {@code FOUNDRY_PROJECT_ENDPOINT} and {@code FOUNDRY_USER_IDENTITY} before running this sample.
 * The user identity should be an opaque, application-generated value and must not contain secrets.
 */
public class UserIdentityConversation {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String userIdentity = Configuration.getGlobalConfiguration().get("FOUNDRY_USER_IDENTITY");
        if (userIdentity == null || userIdentity.trim().isEmpty()) {
            throw new IllegalStateException("Set FOUNDRY_USER_IDENTITY to an opaque end-user identity value.");
        }

        ConversationService conversationService = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildOpenAIClient()
            .conversations()
            .withOptions(options -> options.putHeader("x-ms-user-identity", userIdentity));

        String conversationId = null;
        try {
            Conversation conversation = conversationService.create();
            conversationId = conversation.id();
            System.out.println("Created conversation: " + conversationId);

            ConversationUpdateParams.Metadata metadata = ConversationUpdateParams.Metadata.builder()
                .putAdditionalProperty("sample", JsonValue.from("java-user-identity"))
                .build();
            conversation = conversationService.update(conversationId,
                ConversationUpdateParams.builder().metadata(metadata).build());
            System.out.println("Updated conversation: " + conversation.id());

            conversation = conversationService.retrieve(conversationId);
            System.out.println("Retrieved conversation: " + conversation.id());
        } finally {
            if (conversationId != null) {
                ConversationDeletedResource deletedConversation = conversationService.delete(conversationId);
                System.out.println("Deleted conversation: " + deletedConversation.id());
            }
        }
    }
}
