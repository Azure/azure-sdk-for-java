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
 * <!-- src_embed com.azure.communication.chat.chatclient.instantiation -->
 * <pre>
 *
 * &#47;&#47; Initialize the chat client builder
 * final ChatClientBuilder builder = new ChatClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;credential&#41;;
 *
 * &#47;&#47; Build the chat client
 * ChatClient chatClient = builder.buildClient&#40;&#41;;
 *
 * </pre>
 * <!-- end com.azure.communication.chat.chatclient.instantiation -->
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
     * <!-- src_embed com.azure.communication.chat.chatclient.createchatthread#createchatthreadoptions -->
     * <pre>
     *
     * &#47;&#47; Initialize the list of chat thread participants
     * List&lt;ChatParticipant&gt; participants = new ArrayList&lt;ChatParticipant&gt;&#40;&#41;;
     *
     * ChatParticipant firstParticipant = new ChatParticipant&#40;&#41;
     *     .setCommunicationIdentifier&#40;user1&#41;
     *     .setDisplayName&#40;&quot;Participant Display Name 1&quot;&#41;;
     *
     * ChatParticipant secondParticipant = new ChatParticipant&#40;&#41;
     *     .setCommunicationIdentifier&#40;user2&#41;
     *     .setDisplayName&#40;&quot;Participant Display Name 2&quot;&#41;;
     *
     * participants.add&#40;firstParticipant&#41;;
     * participants.add&#40;secondParticipant&#41;;
     *
     * &#47;&#47; Create the chat thread
     * CreateChatThreadOptions createChatThreadOptions = new CreateChatThreadOptions&#40;&quot;Topic&quot;&#41;
     *     .setParticipants&#40;participants&#41;;
     * CreateChatThreadResult result = chatClient.createChatThread&#40;createChatThreadOptions&#41;;
     *
     * &#47;&#47; Retrieve the chat thread and the id
     * ChatThreadProperties chatThread = result.getChatThread&#40;&#41;;
     * String chatThreadId = chatThread.getId&#40;&#41;;
     *
     * </pre>
     * <!-- end com.azure.communication.chat.chatclient.createchatthread#createchatthreadoptions -->
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
