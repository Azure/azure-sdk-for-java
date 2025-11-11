// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents;

import com.azure.core.annotation.ServiceClient;
import com.openai.client.OpenAIClient;
import com.openai.services.blocking.ConversationService;

/**
 * Initializes a new instance of the synchronous ConversationsClient type.
 */
@ServiceClient(builder = AgentsClientBuilder.class)
public final class ConversationsClient {

    private final ConversationService openAIConversationClient;

    /**
     * Initializes an instance of ConversationsClient class.
     *
     * @param openAIClient the service client implementation.
     */
    ConversationsClient(OpenAIClient openAIClient) {
        this.openAIConversationClient = openAIClient.conversations();
    }

    /**
     * Get the OpenAI client for conversations.
     *
     * @return the OpenAI conversation service client.
     */
    public ConversationService getConversationService() {
        return this.openAIConversationClient;
    }
}
