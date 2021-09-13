// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.chat;

import com.azure.communication.chat.models.ChatErrorResponseException;
import com.azure.communication.chat.models.ChatThreadItem;
import com.azure.communication.chat.models.CreateChatThreadOptions;
import com.azure.communication.chat.models.CreateChatThreadResult;
import com.azure.communication.chat.models.ListChatThreadsOptions;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

/**
 * Sync Client that supports chat operations.
 *
 * <p><strong>Instantiating a synchronous Chat Client</strong></p>
 *
 * {@codesnippet com.azure.communication.chat.chatclient.instantiation}
 *
 * <p>View {@link ChatClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ChatClientBuilder
 */
@ServiceClient(builder = ChatClientBuilder.class, isAsync = false)
public final class ChatClient {
    private final ClientLogger logger = new ClientLogger(ChatClient.class);

    private final ChatAsyncClient client;

    /**
     * Creates a ChatClient that sends requests to the chat service at {@code serviceEndpoint}. Each
     * service call goes through the {@code pipeline}.
     *
     * @param client The {@link ChatAsyncClient} that the client routes its request through.
     */
    ChatClient(ChatAsyncClient client) {

        this.client = client;
    }

    /**
     * Creates a chat thread client.
     *
     * @param chatThreadId The id of the chat thread.
     * @return the client.
     */
    public ChatThreadClient getChatThreadClient(String chatThreadId) {

        ChatThreadAsyncClient chatThreadAsyncClient = this.client.getChatThreadClient(chatThreadId);
        return new ChatThreadClient(chatThreadAsyncClient);
    }

    /**
     * Creates a chat thread.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a chat thread based on "options".</p>
     *
     * {@codesnippet com.azure.communication.chat.chatclient.createchatthread#createchatthreadoptions}
     *
     * @param options Options for creating a chat thread.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CreateChatThreadResult createChatThread(CreateChatThreadOptions options) {
        return this.client.createChatThread(options).block();
    }

    /**
     * Creates a chat thread.
     *
     * @param options Options for creating a chat thread.
     * @param context The context to associate with this operation.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CreateChatThreadResult> createChatThreadWithResponse(CreateChatThreadOptions options,
                                                                         Context context) {

        return this.client.createChatThread(options, context).map(
            result -> new SimpleResponse<CreateChatThreadResult>(result, result.getValue())).block();
    }

    /**
     * Deletes a chat thread.
     *
     * @param chatThreadId Chat thread id to delete.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteChatThread(String chatThreadId) {
        this.client.deleteChatThread(chatThreadId).block();
    }

    /**
     * Deletes a chat thread.
     *
     * @param chatThreadId Chat thread id to delete.
     * @param context The context to associate with this operation.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteChatThreadWithResponse(String chatThreadId, Context context) {

        return this.client.deleteChatThread(chatThreadId, context).block();
    }

    /**
     * Gets the list of chat threads of a user.
     *
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the paged list of chat threads of a user.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ChatThreadItem> listChatThreads() {

        return new PagedIterable<>(this.client.listChatThreads());
    }

    /**
     * Gets the list of chat threads of a user.
     *
     * @param listThreadsOptions The request options.
     * @param context The context to associate with this operation.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the paged list of chat threads of a user.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ChatThreadItem> listChatThreads(ListChatThreadsOptions listThreadsOptions, Context context) {

        return new PagedIterable<>(this.client.listChatThreads(listThreadsOptions, context));
    }
}
