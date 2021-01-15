// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.chat;

import reactor.core.publisher.Mono;

import com.azure.communication.chat.implementation.converters.ChatThreadConverter;
import com.azure.communication.chat.implementation.converters.CreateChatThreadOptionsConverter;
import com.azure.communication.chat.implementation.models.IndividualStatusResponse;
import com.azure.communication.chat.implementation.models.MultiStatusResponse;
import com.azure.communication.chat.implementation.AzureCommunicationChatServiceImpl;
import com.azure.communication.chat.models.CreateChatThreadOptions;
import com.azure.communication.chat.models.ListChatThreadsOptions;
import com.azure.communication.chat.models.ChatThread;
import com.azure.communication.chat.models.ChatThreadInfo;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;
import java.util.List;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Async Client that supports chat operations.
 */
@ServiceClient(builder = ChatClientBuilder.class, isAsync = true)
public final class ChatAsyncClient {
    private final ClientLogger logger = new ClientLogger(ChatAsyncClient.class);

    private final AzureCommunicationChatServiceImpl chatServiceClient;

    private static final String THREAD_RESOURCE_STATUS_TYPE = "thread";

    ChatAsyncClient(AzureCommunicationChatServiceImpl chatServiceClient) {
        this.chatServiceClient = chatServiceClient;
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
     * @param options Options for creating a chat thread.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ChatThreadAsyncClient> createChatThread(CreateChatThreadOptions options) {
        try {
            Objects.requireNonNull(options, "'options' cannot be null.");
            return withContext(context -> createChatThread(options, context)
                .flatMap(
                    (Response<ChatThreadAsyncClient> res) -> {
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
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ChatThreadAsyncClient>> createChatThreadWithResponse(CreateChatThreadOptions options) {
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
     * @return the response.
     */
    Mono<Response<ChatThreadAsyncClient>> createChatThread(CreateChatThreadOptions options, Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatServiceClient.createChatThreadWithResponseAsync(
            CreateChatThreadOptionsConverter.convert(options), context).map(
                result -> new SimpleResponse<ChatThreadAsyncClient>(
                    result, getChatThreadClient(getThreadIdFromMultiStatusResponse(result.getValue()))));
    }

    /**
     * Gets a chat thread.
     *
     * @param chatThreadId Chat thread id to get.
     * @return a chat thread.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ChatThread> getChatThread(String chatThreadId) {
        try {
            Objects.requireNonNull(chatThreadId, "'chatThreadId' cannot be null.");
            return withContext(context -> getChatThread(chatThreadId, context)
                .flatMap(
                    (Response<ChatThread> res) -> {
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
     * Gets a chat thread.
     *
     * @param chatThreadId Chat thread id to get.
     * @return a chat thread.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ChatThread>> getChatThreadWithResponse(String chatThreadId) {
        try {
            Objects.requireNonNull(chatThreadId, "'chatThreadId' cannot be null.");
            return withContext(context -> getChatThread(chatThreadId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets a chat thread.
     *
     * @param chatThreadId Chat thread id to get.
     * @param context The context to associate with this operation.
     * @return a chat thread.
     */
    Mono<Response<ChatThread>> getChatThread(String chatThreadId, Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatServiceClient.getChatThreadWithResponseAsync(chatThreadId, context)
            .flatMap(
                (Response<com.azure.communication.chat.implementation.models.ChatThread> res) -> {
                    return Mono.just(new SimpleResponse<ChatThread>(
                        res, ChatThreadConverter.convert(res.getValue())));
                });
    }

    /**
     * Gets the list of chat threads of a user.
     *
     * @return the paged list of chat threads of a user.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ChatThreadInfo> listChatThreads() {
        ListChatThreadsOptions listThreadsOptions = new ListChatThreadsOptions();
        try {
            return new PagedFlux<>(
                () -> withContext(context ->  this.chatServiceClient.listChatThreadsSinglePageAsync(
                    listThreadsOptions.getMaxPageSize(), listThreadsOptions.getStartTime(), context)),
                nextLink -> withContext(context -> this.chatServiceClient.listChatThreadsNextSinglePageAsync(
                    nextLink, context)));
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
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ChatThreadInfo> listChatThreads(ListChatThreadsOptions listThreadsOptions) {
        final ListChatThreadsOptions serviceListThreadsOptions
            = listThreadsOptions == null ? new ListChatThreadsOptions() : listThreadsOptions;
        try {
            return new PagedFlux<>(
                () -> withContext(context ->  this.chatServiceClient.listChatThreadsSinglePageAsync(
                    serviceListThreadsOptions.getMaxPageSize(), serviceListThreadsOptions.getStartTime(), context)),
                nextLink -> withContext(context -> this.chatServiceClient.listChatThreadsNextSinglePageAsync(
                    nextLink, context)));
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
    PagedFlux<ChatThreadInfo> listChatThreads(ListChatThreadsOptions listThreadsOptions, Context context) {
        final Context serviceContext = context == null ? Context.NONE : context;
        final ListChatThreadsOptions serviceListThreadsOptions
            = listThreadsOptions == null ? new ListChatThreadsOptions() : listThreadsOptions;

        return this.chatServiceClient.listChatThreadsAsync(
            serviceListThreadsOptions.getMaxPageSize(), serviceListThreadsOptions.getStartTime(), serviceContext);
    }

    /**
     * Deletes a chat thread.
     *
     * @param chatThreadId Chat thread id to delete.
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
     * @return the completion.
     */
    Mono<Response<Void>> deleteChatThread(String chatThreadId, Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatServiceClient.deleteChatThreadWithResponseAsync(chatThreadId, context);
    }

    private String getThreadIdFromMultiStatusResponse(MultiStatusResponse multiStatusResponse) {

        List<IndividualStatusResponse> individualStatusResponses = multiStatusResponse.getMultipleStatus();
        for (IndividualStatusResponse individualStatusResponse : individualStatusResponses) {
            if (individualStatusResponse.getType().equalsIgnoreCase(THREAD_RESOURCE_STATUS_TYPE)) {
                if (individualStatusResponse.getStatusCode() == 201) {
                    return individualStatusResponse.getId();
                }

                throw logger.logExceptionAsError(new RuntimeException(
                    String.format(
                        "%s. Status code: %s.",
                        individualStatusResponse.getMessage(),
                        individualStatusResponse.getStatusCode())));
            }
        }

        throw logger.logExceptionAsError(new RuntimeException("Failed to create thread."));
    }
}
