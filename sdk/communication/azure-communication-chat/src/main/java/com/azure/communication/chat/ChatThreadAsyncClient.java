// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.chat;

import com.azure.communication.chat.implementation.ChatThreadImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.azure.communication.chat.implementation.AzureCommunicationChatServiceImpl;
import com.azure.communication.chat.implementation.converters.AddChatParticipantsOptionsConverter;
import com.azure.communication.chat.implementation.converters.ChatMessageConverter;
import com.azure.communication.chat.implementation.converters.ChatParticipantConverter;
import com.azure.communication.chat.implementation.converters.ChatMessageReadReceiptConverter;
import com.azure.communication.chat.implementation.converters.SendChatMessageResultConverter;
import com.azure.communication.chat.implementation.models.SendReadReceiptRequest;
import com.azure.communication.chat.models.AddChatParticipantsOptions;
import com.azure.communication.chat.models.AddChatParticipantsResult;
import com.azure.communication.chat.models.ChatMessage;
import com.azure.communication.chat.models.ChatParticipant;
import com.azure.communication.chat.models.ChatMessageReadReceipt;
import com.azure.communication.chat.models.ListChatMessagesOptions;
import com.azure.communication.chat.models.ListParticipantsOptions;
import com.azure.communication.chat.models.ListReadReceiptOptions;
import com.azure.communication.chat.models.SendChatMessageOptions;
import com.azure.communication.chat.models.UpdateChatMessageOptions;
import com.azure.communication.chat.models.UpdateChatThreadOptions;
import com.azure.communication.common.CommunicationUserIdentifier;
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

import java.util.Collections;
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
    private final ChatThreadImpl chatThreadClient;

    private final String chatThreadId;

    ChatThreadAsyncClient(AzureCommunicationChatServiceImpl chatServiceClient, String chatThreadId) {
        this.chatServiceClient = chatServiceClient;
        this.chatThreadClient = chatServiceClient.getChatThreadClient();
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
     * Updates a thread's topic.
     *
     * @param topic The new topic.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateTopic(String topic) {
        try {
            Objects.requireNonNull(topic, "'topic' cannot be null.");
            return withContext(context -> updateTopic(topic, context)
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
     * @param topic The new topic.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> updateTopicWithResponse(String topic) {
        try {
            Objects.requireNonNull(topic, "'topic' cannot be null.");
            return withContext(context -> updateTopic(topic, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Updates a thread's topic.
     *
     * @param topic The new topic.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    Mono<Response<Void>> updateTopic(String topic, Context context) {
        context = context == null ? Context.NONE : context;

        return this.chatThreadClient.updateChatThreadWithResponseAsync(
            chatThreadId,
            new UpdateChatThreadOptions()
                .setTopic(topic),
            context
        );
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param options Options for adding participants.
     * @return the result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> addParticipants(AddChatParticipantsOptions options) {
        try {
            Objects.requireNonNull(options, "'options' cannot be null.");
            return withContext(context -> addParticipants(options, context)
                .flatMap((Response<AddChatParticipantsResult> res) -> {
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
     * @return the result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AddChatParticipantsResult>> addParticipantsWithResponse(AddChatParticipantsOptions options) {
        try {
            Objects.requireNonNull(options, "'options' cannot be null.");
            return withContext(context -> addParticipants(options, context));
        } catch (RuntimeException ex) {

            return monoError(logger, ex);
        }
    }

    /**
     * Adds a participant to a thread. If the participant already exists, no change occurs.
     *
     * @param participant The new participant.
     * @return the result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> addParticipant(ChatParticipant participant) {
        try {
            return withContext(context -> addParticipants(
                new AddChatParticipantsOptions()
                    .setParticipants(Collections.singletonList(participant)),
                context)
                .flatMap((Response<AddChatParticipantsResult> res) -> {
                    return Mono.empty();
                }));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Adds a participant to a thread. If the participant already exists, no change occurs.
     *
     * @param participant The new participant.
     * @return the result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AddChatParticipantsResult>> addParticipantWithResponse(ChatParticipant participant) {
        try {
            return withContext(context -> addParticipants(
                new AddChatParticipantsOptions()
                    .setParticipants(Collections.singletonList(participant)),
                context));
        } catch (RuntimeException ex) {

            return monoError(logger, ex);
        }
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param options Options for adding participants.
     * @param context The context to associate with this operation.
     * @return the result.
     */
    Mono<Response<AddChatParticipantsResult>> addParticipants(AddChatParticipantsOptions options, Context context) {
        context = context == null ? Context.NONE : context;
        return this.chatThreadClient.addChatParticipantsWithResponseAsync(
            chatThreadId, AddChatParticipantsOptionsConverter.convert(options), context);
    }

    /**
     * Remove a participant from a thread.
     *
     * @param user User identity of the participant to remove from the thread.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeParticipant(CommunicationUserIdentifier user) {
        try {
            Objects.requireNonNull(user, "'user' cannot be null.");
            Objects.requireNonNull(user.getId(), "'user.getId()' cannot be null.");
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
    public Mono<Response<Void>> removeParticipantWithResponse(CommunicationUserIdentifier user) {
        try {
            Objects.requireNonNull(user, "'user' cannot be null.");
            Objects.requireNonNull(user.getId(), "'user.getId()' cannot be null.");
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
    Mono<Response<Void>> removeParticipant(CommunicationUserIdentifier user, Context context) {
        context = context == null ? Context.NONE : context;

        return this.chatThreadClient.removeChatParticipantWithResponseAsync(chatThreadId, user.getId(), context);
    }

    /**
     * Gets the participants of a thread.
     *
     * @return the participants of a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ChatParticipant> listParticipants() {
        ListParticipantsOptions listParticipantsOptions = new ListParticipantsOptions();
        return listParticipants(listParticipantsOptions);
    }

    /**
     * Gets the participants of a thread.
     *
     * @param listParticipantsOptions The request options.
     * @return the participants of a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ChatParticipant> listParticipants(ListParticipantsOptions listParticipantsOptions) {
        final ListParticipantsOptions serviceListParticipantsOptions =
            listParticipantsOptions == null ? new ListParticipantsOptions() : listParticipantsOptions;
        try {
            return pagedFluxConvert(new PagedFlux<>(
                () -> withContext(context ->
                    this.chatThreadClient.listChatParticipantsSinglePageAsync(
                        chatThreadId,
                        serviceListParticipantsOptions.getMaxPageSize(),
                        serviceListParticipantsOptions.getSkip(),
                        context)),
                nextLink -> withContext(context ->
                    this.chatThreadClient.listChatParticipantsNextSinglePageAsync(nextLink, context))),
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
        ListParticipantsOptions listParticipantsOptions = new ListParticipantsOptions();
        return listParticipants(listParticipantsOptions, context);
    }

    /**
     * Gets the participants of a thread.
     *
     * @param context The context to associate with this operation.
     * @param listParticipantsOptions The request options.
     * @return the participants of a thread.
     */
    PagedFlux<ChatParticipant> listParticipants(ListParticipantsOptions listParticipantsOptions, Context context) {
        final Context serviceContext = context == null ? Context.NONE : context;
        final ListParticipantsOptions serviceListParticipantsOptions =
            listParticipantsOptions == null ? new ListParticipantsOptions() : listParticipantsOptions;

        try {
            return pagedFluxConvert(new PagedFlux<>(
                () ->
                    this.chatThreadClient.listChatParticipantsSinglePageAsync(
                        chatThreadId,
                        serviceListParticipantsOptions.getMaxPageSize(),
                        serviceListParticipantsOptions.getSkip(),
                        serviceContext),
                nextLink ->
                    this.chatThreadClient.listChatParticipantsNextSinglePageAsync(nextLink, serviceContext)),
                f -> ChatParticipantConverter.convert(f));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Sends a message to a thread.
     *
     * @param options Options for sending the message.
     * @return the MessageId.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> sendMessage(SendChatMessageOptions options) {
        try {
            Objects.requireNonNull(options, "'options' cannot be null.");
            return withContext(context -> sendMessage(options, context)
                .flatMap(
                    res -> Mono.just(res.getValue())));
        } catch (RuntimeException ex) {

            return monoError(logger, ex);
        }
    }

    /**
     * Sends a message to a thread.
     *
     * @param options Options for sending the message.
     * @return the MessageId.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> sendMessageWithResponse(SendChatMessageOptions options) {
        try {
            Objects.requireNonNull(options, "'options' cannot be null.");
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
     * @return the MessageId.
     */
    Mono<Response<String>> sendMessage(SendChatMessageOptions options, Context context) {
        context = context == null ? Context.NONE : context;

        return this.chatThreadClient.sendChatMessageWithResponseAsync(
            chatThreadId, options, context).map(
                result -> new SimpleResponse<String>(
                    result, SendChatMessageResultConverter.convert(result.getValue())));
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
            Objects.requireNonNull(chatMessageId, "'chatMessageId' cannot be null.");
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
            Objects.requireNonNull(chatMessageId, "'chatMessageId' cannot be null.");
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

        return this.chatThreadClient.getChatMessageWithResponseAsync(chatThreadId, chatMessageId, context).map(
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
                () -> withContext(context ->  this.chatThreadClient.listChatMessagesSinglePageAsync(
                    chatThreadId, listMessagesOptions.getMaxPageSize(), listMessagesOptions.getStartTime(), context)),
                nextLink -> withContext(context -> this.chatThreadClient.listChatMessagesNextSinglePageAsync(
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
                () -> withContext(context ->  this.chatThreadClient.listChatMessagesSinglePageAsync(
                    chatThreadId,
                    serviceListMessagesOptions.getMaxPageSize(),
                    serviceListMessagesOptions.getStartTime(),
                    context)),
                nextLink -> withContext(context -> this.chatThreadClient.listChatMessagesNextSinglePageAsync(
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
                () ->  this.chatThreadClient.listChatMessagesSinglePageAsync(
                    chatThreadId,
                    serviceListMessagesOptions.getMaxPageSize(),
                    serviceListMessagesOptions.getStartTime(),
                    serviceContext),
                nextLink -> this.chatThreadClient.listChatMessagesNextSinglePageAsync(
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
            Objects.requireNonNull(chatMessageId, "'chatMessageId' cannot be null.");
            Objects.requireNonNull(options, "'options' cannot be null.");
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
            Objects.requireNonNull(chatMessageId, "'chatMessageId' cannot be null.");
            Objects.requireNonNull(options, "'options' cannot be null.");
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

        return this.chatThreadClient.updateChatMessageWithResponseAsync(chatThreadId, chatMessageId, options, context);
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
            Objects.requireNonNull(chatMessageId, "'chatMessageId' cannot be null.");
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
            Objects.requireNonNull(chatMessageId, "'chatMessageId' cannot be null.");
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

        return this.chatThreadClient.deleteChatMessageWithResponseAsync(chatThreadId, chatMessageId, context);
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

        return this.chatThreadClient.sendTypingNotificationWithResponseAsync(chatThreadId, context);
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
            Objects.requireNonNull(chatMessageId, "'chatMessageId' cannot be null.");
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
            Objects.requireNonNull(chatMessageId, "'chatMessageId' cannot be null.");
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

        SendReadReceiptRequest request = new SendReadReceiptRequest()
            .setChatMessageId(chatMessageId);
        return this.chatThreadClient.sendChatReadReceiptWithResponseAsync(chatThreadId, request, context);
    }

    /**
     * Gets read receipts for a thread.
     *
     * @return read receipts for a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ChatMessageReadReceipt> listReadReceipts() {
        ListReadReceiptOptions listReadReceiptOptions = new ListReadReceiptOptions();
        return listReadReceipts(listReadReceiptOptions);
    }

    /**
     * Gets read receipts for a thread.
     *
     * @param listReadReceiptOptions The additional options for this operation.
     * @return read receipts for a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ChatMessageReadReceipt> listReadReceipts(ListReadReceiptOptions listReadReceiptOptions) {
        final ListReadReceiptOptions serviceListReadReceiptOptions =
            listReadReceiptOptions == null ? new ListReadReceiptOptions() : listReadReceiptOptions;
        try {
            return pagedFluxConvert(new PagedFlux<>(
                () -> withContext(context ->  this.chatThreadClient.listChatReadReceiptsSinglePageAsync(
                    chatThreadId,
                    serviceListReadReceiptOptions.getMaxPageSize(),
                    serviceListReadReceiptOptions.getSkip(),
                    context)),
                nextLink -> withContext(context -> this.chatThreadClient.listChatReadReceiptsNextSinglePageAsync(
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
     *
     * @return read receipts for a thread.
     */
    PagedFlux<ChatMessageReadReceipt> listReadReceipts(Context context) {
        ListReadReceiptOptions listReadReceiptOptions = new ListReadReceiptOptions();
        return listReadReceipts(listReadReceiptOptions, context);
    }

    /**
     * Gets read receipts for a thread.
     *
     * @param listReadReceiptOptions The additional options for this operation.
     * @param context The context to associate with this operation.
     *
     * @return read receipts for a thread.
     */
    PagedFlux<ChatMessageReadReceipt> listReadReceipts(ListReadReceiptOptions listReadReceiptOptions, Context context) {
        final Context serviceContext = context == null ? Context.NONE : context;
        final ListReadReceiptOptions serviceListReadReceiptOptions =
            listReadReceiptOptions == null ? new ListReadReceiptOptions() : listReadReceiptOptions;

        try {
            return pagedFluxConvert(new PagedFlux<>(
                () -> this.chatThreadClient.listChatReadReceiptsSinglePageAsync(
                    chatThreadId,
                    serviceListReadReceiptOptions.getMaxPageSize(),
                    serviceListReadReceiptOptions.getSkip(),
                    serviceContext),
                nextLink -> this.chatThreadClient.listChatReadReceiptsNextSinglePageAsync(
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
