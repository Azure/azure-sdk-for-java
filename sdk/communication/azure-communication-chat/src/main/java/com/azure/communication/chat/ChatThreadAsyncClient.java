// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.chat;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.azure.communication.chat.implementation.AzureCommunicationChatServiceImpl;
import com.azure.communication.chat.implementation.converters.AddChatParticipantsOptionsConverter;
import com.azure.communication.chat.implementation.converters.ChatMessageConverter;
import com.azure.communication.chat.implementation.converters.ChatParticipantConverter;
import com.azure.communication.chat.implementation.converters.ChatMessageReadReceiptConverter;
import com.azure.communication.chat.implementation.models.SendReadReceiptRequest;
import com.azure.communication.chat.models.AddChatParticipantsOptions;
import com.azure.communication.chat.models.ChatMessage;
import com.azure.communication.chat.models.ChatParticipant;
import com.azure.communication.chat.models.ChatMessageReadReceipt;
import com.azure.communication.chat.models.ListChatMessagesOptions;
import com.azure.communication.chat.models.SendChatMessageResult;
import com.azure.communication.chat.models.SendChatMessageOptions;
import com.azure.communication.chat.models.UpdateChatMessageOptions;
import com.azure.communication.chat.models.UpdateChatThreadOptions;
import com.azure.communication.common.CommunicationUser;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.paging.PageRetriever;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Async Client that supports chat thread operations.
 */
@ServiceClient(builder = ChatClientBuilder.class, isAsync = true)
public final class ChatThreadAsyncClient {
    private final ClientLogger logger = new ClientLogger(ChatThreadAsyncClient.class);

    private final AzureCommunicationChatServiceImpl chatServiceClient;

    private final String chatThreadId;

    ChatThreadAsyncClient(AzureCommunicationChatServiceImpl chatServiceClient, String chatThreadId) {
        this.chatServiceClient = chatServiceClient;
        this.chatThreadId = chatThreadId;
    }

    /**
     * Get the thread id property.
     *
     * @return the thread id value.
     */
    public String getChatThreadId() {
        return chatThreadId;
    }

    /**
     * Updates a thread's properties.
     *
     * @param options Options for updating a chat thread.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateChatThread(UpdateChatThreadOptions options) {
        try {
            return withContext(context -> updateChatThread(options, context)
                .flatMap((Response<Void> res) -> {
                    return Mono.empty();
                }));
        } catch (RuntimeException ex) {

            return monoError(logger, ex);
        }
    }

    /**
     * Updates a thread's properties.
     *
     * @param options Options for updating a chat thread.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> updateChatThreadWithResponse(UpdateChatThreadOptions options) {
        try {
            return withContext(context -> updateChatThread(options, context));
        } catch (RuntimeException ex) {

            return monoError(logger, ex);
        }
    }

    /**
     * Updates a thread's properties.
     *
     * @param options Options for updating a chat thread.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    Mono<Response<Void>> updateChatThread(UpdateChatThreadOptions options, Context context) {
        context = context == null ? Context.NONE : context;

        Objects.requireNonNull(options, "'options' cannot be null.");

        return this.chatServiceClient.updateChatThreadWithResponseAsync(chatThreadId, options, context);
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param options Options for adding participants.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> addParticipants(AddChatParticipantsOptions options) {
        try {
            return withContext(context -> addParticipants(options, context)
                .flatMap((Response<Void> res) -> {
                    return Mono.empty();
                }));
        } catch (RuntimeException ex) {

            return monoError(logger, ex);
        }
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param options Options for adding participants.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addParticipantsWithResponse(AddChatParticipantsOptions options) {
        try {
            return withContext(context -> addParticipants(options, context));
        } catch (RuntimeException ex) {

            return monoError(logger, ex);
        }
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param options Options for adding participants.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    Mono<Response<Void>> addParticipants(AddChatParticipantsOptions options, Context context) {
        context = context == null ? Context.NONE : context;

        Objects.requireNonNull(options, "'options' cannot be null.");

        return this.chatServiceClient.addChatParticipantsWithResponseAsync(
            chatThreadId, AddChatParticipantsOptionsConverter.convert(options), context);
    }

    /**
     * Remove a participant from a thread.
     *
     * @param user User identity of the participant to remove from the thread.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeParticipant(CommunicationUser user) {
        try {
            return withContext(context -> removeParticipant(user, context)
                .flatMap((Response<Void> res) -> {
                    return Mono.empty();
                }));
        } catch (RuntimeException ex) {

            return monoError(logger, ex);
        }
    }

    /**
     * Remove a participant from a thread.
     *
     * @param user User identity of the participant to remove from the thread.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeParticipantWithResponse(CommunicationUser user) {
        try {
            return withContext(context -> removeParticipant(user, context));
        } catch (RuntimeException ex) {

            return monoError(logger, ex);
        }
    }

    /**
     * Remove a participant from a thread.
     *
     * @param user User identity of the participant to remove from the thread.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    Mono<Response<Void>> removeParticipant(CommunicationUser user, Context context) {
        context = context == null ? Context.NONE : context;

        Objects.requireNonNull(user, "'user' cannot be null.");
        Objects.requireNonNull(user.getId(), "'user.getId()' cannot be null.");

        return this.chatServiceClient.removeChatParticipantWithResponseAsync(chatThreadId, user.getId(), context);
    }

    /**
     * Gets the participants of a thread.
     *
     * @return the participants of a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ChatParticipant> listParticipants() {
        try {
            return pagedFluxConvert(new PagedFlux<>(
                () -> withContext(context ->
                    this.chatServiceClient.listChatParticipantsSinglePageAsync(chatThreadId, context)),
                nextLink -> withContext(context ->
                    this.chatServiceClient.listChatParticipantsNextSinglePageAsync(nextLink, context))),
                f -> ChatParticipantConverter.convert(f));
        } catch (RuntimeException ex) {

            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Gets the participants of a thread.
     *
     * @param context The context to associate with this operation.
     * @return the participants of a thread.
     */
    PagedFlux<ChatParticipant> listParticipants(Context context) {
        final Context serviceContext = context == null ? Context.NONE : context;

        try {
            return pagedFluxConvert(new PagedFlux<>(
                () ->
                    this.chatServiceClient.listChatParticipantsSinglePageAsync(chatThreadId, serviceContext),
                nextLink ->
                    this.chatServiceClient.listChatParticipantsNextSinglePageAsync(nextLink, serviceContext)),
                f -> ChatParticipantConverter.convert(f));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Sends a message to a thread.
     *
     * @param options Options for sending the message.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SendChatMessageResult> sendMessage(SendChatMessageOptions options) {
        try {
            return withContext(context -> sendMessage(options, context)
                .flatMap(
                    (Response<SendChatMessageResult> res) -> {
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
     * Sends a message to a thread.
     *
     * @param options Options for sending the message.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SendChatMessageResult>> sendMessageWithResponse(SendChatMessageOptions options) {
        try {
            return withContext(context -> sendMessage(options, context));
        } catch (RuntimeException ex) {

            return monoError(logger, ex);
        }
    }

    /**
     * Sends a message to a thread.
     *
     * @param options Options for sending the message.
     * @param context The context to associate with this operation.
     * @return the response.
     */
    Mono<Response<SendChatMessageResult>> sendMessage(SendChatMessageOptions options, Context context) {
        context = context == null ? Context.NONE : context;

        Objects.requireNonNull(options, "'options' cannot be null.");

        return this.chatServiceClient.sendChatMessageWithResponseAsync(
            chatThreadId, options, context);
    }

    /**
     * Gets a message by id.
     *
     * @param chatMessageId The message id.
     * @return a message by id.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ChatMessage> getMessage(String chatMessageId) {
        try {
            return withContext(context -> getMessage(chatMessageId, context)
                .flatMap(
                    (Response<ChatMessage> res) -> {
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
     * Gets a message by id.
     *
     * @param chatMessageId The message id.
     * @return a message by id.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ChatMessage>> getMessageWithResponse(String chatMessageId) {
        try {
            return withContext(context -> getMessage(chatMessageId, context));
        } catch (RuntimeException ex) {

            return monoError(logger, ex);
        }
    }

    /**
     * Gets a message by id.
     *
     * @param chatMessageId The message id.
     * @param context The context to associate with this operation.
     * @return a message by id.
     */
    Mono<Response<ChatMessage>> getMessage(String chatMessageId, Context context) {
        context = context == null ? Context.NONE : context;

        Objects.requireNonNull(chatMessageId, "'chatMessageId' cannot be null.");

        return this.chatServiceClient.getChatMessageWithResponseAsync(chatThreadId, chatMessageId, context).map(
            result -> new SimpleResponse<ChatMessage>(
                result, ChatMessageConverter.convert(result.getValue())));
    }

    /**
     * Gets a list of messages from a thread.
     *
     * @return a paged list of messages from a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ChatMessage> listMessages() {
        ListChatMessagesOptions listMessagesOptions = new ListChatMessagesOptions();
        try {
            return pagedFluxConvert(new PagedFlux<>(
                () -> withContext(context ->  this.chatServiceClient.listChatMessagesSinglePageAsync(
                    chatThreadId, listMessagesOptions.getMaxPageSize(), listMessagesOptions.getStartTime(), context)),
                nextLink -> withContext(context -> this.chatServiceClient.listChatMessagesNextSinglePageAsync(
                    nextLink, context))),
                f -> ChatMessageConverter.convert(f));
        } catch (RuntimeException ex) {

            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Gets a list of messages from a thread.
     *
     * @param listMessagesOptions The request options.
     * @return a paged list of messages from a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ChatMessage> listMessages(ListChatMessagesOptions listMessagesOptions) {
        final ListChatMessagesOptions serviceListMessagesOptions =
            listMessagesOptions == null ? new ListChatMessagesOptions() : listMessagesOptions;

        try {
            return pagedFluxConvert(new PagedFlux<>(
                () -> withContext(context ->  this.chatServiceClient.listChatMessagesSinglePageAsync(
                    chatThreadId,
                    serviceListMessagesOptions.getMaxPageSize(),
                    serviceListMessagesOptions.getStartTime(),
                    context)),
                nextLink -> withContext(context -> this.chatServiceClient.listChatMessagesNextSinglePageAsync(
                    nextLink, context))),
                f -> ChatMessageConverter.convert(f));
        } catch (RuntimeException ex) {

            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Gets a list of messages from a thread.
     *
     * @param listMessagesOptions The request options.
     * @param context The context to associate with this operation.
     * @return a paged list of messages from a thread.
     */
    PagedFlux<ChatMessage> listMessages(ListChatMessagesOptions listMessagesOptions, Context context) {
        final ListChatMessagesOptions serviceListMessagesOptions
            = listMessagesOptions == null ? new ListChatMessagesOptions() : listMessagesOptions;
        final Context serviceContext = context == null ? Context.NONE : context;

        try {
            return pagedFluxConvert(new PagedFlux<>(
                () ->  this.chatServiceClient.listChatMessagesSinglePageAsync(
                    chatThreadId,
                    serviceListMessagesOptions.getMaxPageSize(),
                    serviceListMessagesOptions.getStartTime(),
                    serviceContext),
                nextLink -> this.chatServiceClient.listChatMessagesNextSinglePageAsync(
                    nextLink, serviceContext)),
                f -> ChatMessageConverter.convert(f));
        } catch (RuntimeException ex) {

            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Updates a message.
     *
     * @param chatMessageId The message id.
     * @param options Options for updating the message.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateMessage(String chatMessageId, UpdateChatMessageOptions options) {
        try {
            return withContext(context -> updateMessage(chatMessageId, options, context)
                .flatMap((Response<Void> res) -> {
                    return Mono.empty();
                }));
        } catch (RuntimeException ex) {

            return monoError(logger, ex);
        }
    }

    /**
     * Updates a message.
     *
     * @param chatMessageId The message id.
     * @param options Options for updating the message.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> updateMessageWithResponse(String chatMessageId, UpdateChatMessageOptions options) {
        try {
            return withContext(context -> updateMessage(chatMessageId, options, context));
        } catch (RuntimeException ex) {

            return monoError(logger, ex);
        }
    }

    /**
     * Updates a message.
     *
     * @param chatMessageId The message id.
     * @param options Options for updating the message.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    Mono<Response<Void>> updateMessage(String chatMessageId, UpdateChatMessageOptions options, Context context) {
        context = context == null ? Context.NONE : context;

        Objects.requireNonNull(chatMessageId, "'chatMessageId' cannot be null.");
        Objects.requireNonNull(options, "'options' cannot be null.");

        return this.chatServiceClient.updateChatMessageWithResponseAsync(chatThreadId, chatMessageId, options, context);
    }

    /**
     * Deletes a message.
     *
     * @param chatMessageId The message id.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteMessage(String chatMessageId) {
        try {
            return withContext(context -> deleteMessage(chatMessageId, context)
                .flatMap((Response<Void> res) -> {
                    return Mono.empty();
                }));
        } catch (RuntimeException ex) {

            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a message.
     *
     * @param chatMessageId The message id.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteMessageWithResponse(String chatMessageId) {
        try {
            return withContext(context -> deleteMessage(chatMessageId, context));
        } catch (RuntimeException ex) {

            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a message.
     *
     * @param chatMessageId The message id.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    Mono<Response<Void>> deleteMessage(String chatMessageId, Context context) {
        context = context == null ? Context.NONE : context;

        Objects.requireNonNull(chatMessageId, "'chatMessageId' cannot be null.");

        return this.chatServiceClient.deleteChatMessageWithResponseAsync(chatThreadId, chatMessageId, context);
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendTypingNotification() {
        try {
            return withContext(context -> sendTypingNotification(context)
                .flatMap((Response<Void> res) -> {
                    return Mono.empty();
                }));
        } catch (RuntimeException ex) {

            return monoError(logger, ex);
        }
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendTypingNotificationWithResponse() {
        try {
            return withContext(context -> sendTypingNotification(context));
        } catch (RuntimeException ex) {

            return monoError(logger, ex);
        }
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    Mono<Response<Void>> sendTypingNotification(Context context) {
        context = context == null ? Context.NONE : context;

        return this.chatServiceClient.sendTypingNotificationWithResponseAsync(chatThreadId, context);
    }

    /**
     * Posts a read receipt event to a thread, on behalf of a user.
     *
     * @param chatMessageId The id of the chat message that was read.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendReadReceipt(String chatMessageId) {
        try {
            return withContext(context -> sendReadReceipt(chatMessageId, context)
                .flatMap((Response<Void> res) -> {
                    return Mono.empty();
                }));
        } catch (RuntimeException ex) {

            return monoError(logger, ex);
        }
    }

    /**
     * Posts a read receipt event to a thread, on behalf of a user.
     *
     * @param chatMessageId The id of the chat message that was read.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendReadReceiptWithResponse(String chatMessageId) {
        try {
            return withContext(context -> sendReadReceipt(chatMessageId, context));
        } catch (RuntimeException ex) {

            return monoError(logger, ex);
        }
    }

    /**
     * Posts a read receipt event to a thread, on behalf of a user.
     *
     * @param chatMessageId The id of the chat message that was read.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    Mono<Response<Void>> sendReadReceipt(String chatMessageId, Context context) {
        context = context == null ? Context.NONE : context;

        Objects.requireNonNull(chatMessageId, "'chatMessageId' cannot be null.");

        SendReadReceiptRequest request = new SendReadReceiptRequest()
            .setChatMessageId(chatMessageId);
        return this.chatServiceClient.sendChatReadReceiptWithResponseAsync(chatThreadId, request, context);
    }

    /**
     * Gets read receipts for a thread.
     *
     * @return read receipts for a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ChatMessageReadReceipt> listReadReceipts() {
        try {
            return pagedFluxConvert(new PagedFlux<>(
                () -> withContext(context ->  this.chatServiceClient.listChatReadReceiptsSinglePageAsync(
                    chatThreadId, context)),
                nextLink -> withContext(context -> this.chatServiceClient.listChatReadReceiptsNextSinglePageAsync(
                    nextLink, context))),
                f -> ChatMessageReadReceiptConverter.convert(f));
        } catch (RuntimeException ex) {

            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Gets read receipts for a thread.
     *
     * @param context The context to associate with this operation.
     * @return read receipts for a thread.
     */
    PagedFlux<ChatMessageReadReceipt> listReadReceipts(Context context) {
        final Context serviceContext = context == null ? Context.NONE : context;

        try {
            return pagedFluxConvert(new PagedFlux<>(
                () -> this.chatServiceClient.listChatReadReceiptsSinglePageAsync(
                    chatThreadId, serviceContext),
                nextLink -> this.chatServiceClient.listChatReadReceiptsNextSinglePageAsync(
                    nextLink, serviceContext)),
                f -> ChatMessageReadReceiptConverter.convert(f));
        } catch (RuntimeException ex) {

            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    private <T1, T2> PagedFlux<T1> pagedFluxConvert(PagedFlux<T2> originalPagedFlux, Function<T2, T1> func) {

        final Function<PagedResponse<T2>,
                PagedResponse<T1>> responseMapper
            = response -> new PagedResponseBase<Void, T1>(response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                response.getValue()
                    .stream()
                    .map(value -> func.apply(value)).collect(Collectors.toList()),
                response.getContinuationToken(),
                null);

        final Supplier<PageRetriever<String, PagedResponse<T1>>> provider = () ->
            (continuationToken, pageSize) -> {
                Flux<PagedResponse<T2>> flux
                    = (continuationToken == null)
                        ? originalPagedFlux.byPage()
                        : originalPagedFlux.byPage(continuationToken);
                return flux.map(responseMapper);
            };

        return PagedFlux.create(provider);
    }
}
