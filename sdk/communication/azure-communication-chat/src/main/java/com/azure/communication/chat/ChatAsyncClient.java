// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.chat;

import com.azure.communication.chat.implementation.converters.ChatErrorConverter;
import com.azure.communication.chat.implementation.converters.CreateChatThreadResultConverter;
import com.azure.communication.chat.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.chat.models.ChatError;
import com.azure.communication.chat.models.ChatErrorResponseException;
import reactor.core.publisher.Mono;

import com.azure.communication.chat.models.ChatThreadItem;
import com.azure.communication.chat.models.CreateChatThreadOptions;
import com.azure.communication.chat.models.CreateChatThreadResult;
import com.azure.communication.chat.models.ListChatThreadsOptions;
import com.azure.communication.chat.implementation.converters.CreateChatThreadOptionsConverter;
import com.azure.communication.chat.implementation.AzureCommunicationChatServiceImpl;
import com.azure.communication.chat.implementation.ChatsImpl;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Async Client that supports chat operations.
 *
 * <p><strong>Instantiating an asynchronous Chat Client</strong></p>
 *
 * <!-- src_embed com.azure.communication.chat.chatasyncclient.instantiation -->
 * <pre>
 *
 * &#47;&#47; Initialize the chat client builder
 * final ChatClientBuilder builder = new ChatClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;credential&#41;;
 *
 * &#47;&#47; Build the chat client
 * ChatAsyncClient chatClient = builder.buildAsyncClient&#40;&#41;;
 *
 * </pre>
 * <!-- end com.azure.communication.chat.chatasyncclient.instantiation -->
 *
 * <p>View {@link ChatClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ChatClientBuilder
 */
@ServiceClient(builder = ChatClientBuilder.class, isAsync = true)
public final class ChatAsyncClient {
    private final ClientLogger logger = new ClientLogger(ChatAsyncClient.class);

    private final AzureCommunicationChatServiceImpl chatServiceClient;
    private final ChatsImpl chatClient;

    ChatAsyncClient(AzureCommunicationChatServiceImpl chatServiceClient) {
        this.chatServiceClient = chatServiceClient;
        this.chatClient = chatServiceClient.getChats();
    }

    /**
     * Creates a chat thread client.
     *
     * @param chatThreadId The id of the thread.
     * @return the client.
     */
    public ChatThreadAsyncClient getChatThreadClient(String chatThreadId) {
        Objects.requireNonNull(chatThreadId, "'chatThreadId' cannot be null.");

        return new ChatThreadAsyncClient(chatServiceClient, chatThreadId);
    }

    /**
     * Creates a chat thread.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a chat thread based on "options".</p>
     *
     * <!-- src_embed com.azure.communication.chat.chatasyncclient.createchatthread#createchatthreadoptions -->
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
     * CreateChatThreadResult result = chatClient.createChatThread&#40;createChatThreadOptions&#41;.block&#40;&#41;;
     *
     * &#47;&#47; Retrieve the chat thread and the id
     * ChatThreadProperties chatThread = result.getChatThread&#40;&#41;;
     * String chatThreadId = chatThread.getId&#40;&#41;;
     *
     * </pre>
     * <!-- end com.azure.communication.chat.chatasyncclient.createchatthread#createchatthreadoptions -->
     *
     * @param options Options for creating a chat thread.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CreateChatThreadResult> createChatThread(CreateChatThreadOptions options) {
        try {
            Objects.requireNonNull(options, "'options' cannot be null.");
            return withContext(context -> createChatThread(options, context)
                .flatMap(
                    (Response<CreateChatThreadResult> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a chat thread.
     *
     * @param options Options for creating a chat thread.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CreateChatThreadResult>> createChatThreadWithResponse(CreateChatThreadOptions options) {
        try {
            Objects.requireNonNull(options, "'options' cannot be null.");
            return withContext(context -> createChatThread(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
    Mono<Response<CreateChatThreadResult>> createChatThread(CreateChatThreadOptions options, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.chatClient.createChatThreadWithResponseAsync(
                CreateChatThreadOptionsConverter.convert(options), options.getIdempotencyToken(), context)
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                .map(result -> new SimpleResponse<CreateChatThreadResult>(
                        result, CreateChatThreadResultConverter.convert(result.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets the list of chat threads of a user.
     *
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the paged list of chat threads of a user.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ChatThreadItem> listChatThreads() {
        ListChatThreadsOptions listThreadsOptions = new ListChatThreadsOptions();
        try {
            return new PagedFlux<>(
                () -> withContext(context ->  this.chatClient.listChatThreadsSinglePageAsync(
                    listThreadsOptions.getMaxPageSize(), listThreadsOptions.getStartTime(), context)
                    .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))),
                nextLink -> withContext(context -> this.chatClient.listChatThreadsNextSinglePageAsync(
                    nextLink, context)
                    .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Gets the list of chat threads of a user.
     *
     * @param listThreadsOptions The request options.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the paged list of chat threads of a user.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ChatThreadItem> listChatThreads(ListChatThreadsOptions listThreadsOptions) {
        final ListChatThreadsOptions serviceListThreadsOptions
            = listThreadsOptions == null ? new ListChatThreadsOptions() : listThreadsOptions;
        try {
            return new PagedFlux<>(
                () -> withContext(context ->  this.chatClient.listChatThreadsSinglePageAsync(
                    serviceListThreadsOptions.getMaxPageSize(), serviceListThreadsOptions.getStartTime(), context)
                    .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))),
                nextLink -> withContext(context -> this.chatClient.listChatThreadsNextSinglePageAsync(
                    nextLink, context)
                    .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Gets the list of chat threads of a user.
     *
     * @param listThreadsOptions The request options.
     * @return the paged list of chat threads of a user.
     */
    PagedFlux<ChatThreadItem> listChatThreads(ListChatThreadsOptions listThreadsOptions, Context context) {
        final Context serviceContext = context == null ? Context.NONE : context;
        final ListChatThreadsOptions serviceListThreadsOptions
            = listThreadsOptions == null ? new ListChatThreadsOptions() : listThreadsOptions;
        try {
            return this.chatClient.listChatThreadsAsync(
                serviceListThreadsOptions.getMaxPageSize(), serviceListThreadsOptions.getStartTime(), serviceContext);
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Deletes a chat thread.
     *
     * @param chatThreadId Chat thread id to delete.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteChatThread(String chatThreadId) {
        try {
            Objects.requireNonNull(chatThreadId, "'chatThreadId' cannot be null.");
            return withContext(context -> deleteChatThread(chatThreadId, context))
                .flatMap((Response<Void> res) -> {
                    return Mono.empty();
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a chat thread.
     *
     * @param chatThreadId Chat thread id to delete.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteChatThreadWithResponse(String chatThreadId) {
        try {
            Objects.requireNonNull(chatThreadId, "'chatThreadId' cannot be null.");
            return withContext(context -> deleteChatThread(chatThreadId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
    Mono<Response<Void>> deleteChatThread(String chatThreadId, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.chatClient.deleteChatThreadWithResponseAsync(chatThreadId, context)
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private ChatErrorResponseException translateException(CommunicationErrorResponseException exception) {
        ChatError error = null;
        if (exception.getValue() != null) {
            error = ChatErrorConverter.convert(exception.getValue().getError());
        }
        return new ChatErrorResponseException(exception.getMessage(), exception.getResponse(), error);
    }

}
