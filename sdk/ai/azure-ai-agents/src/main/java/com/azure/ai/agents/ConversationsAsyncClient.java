// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents;

import com.azure.core.annotation.ServiceClient;

import com.openai.client.OpenAIClientAsync;
import com.openai.services.async.ConversationServiceAsync;

/**
 * Initializes a new instance of the asynchronous ConversationsClient type.
 */
@ServiceClient(builder = AgentsClientBuilder.class, isAsync = true)
public final class ConversationsAsyncClient {

    private final ConversationServiceAsync openAIConversationsClientAsync;

    /**
     * Initializes an instance of ConversationsAsyncClient class.
     *
     * @param openAIClientAsync the service client implementation.
     */
    ConversationsAsyncClient(OpenAIClientAsync openAIClientAsync) {
        this.openAIConversationsClientAsync = openAIClientAsync.conversations();
    }

    /**
     * Get the OpenAI client for conversations.
     *
     * @return the OpenAI conversation service client.
     */
    public ConversationServiceAsync getConversationServiceAsync() {
        return this.openAIConversationsClientAsync;
    }
}
