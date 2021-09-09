// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.chat;

import com.azure.communication.chat.implementation.ChatThreadsImpl;

import com.azure.communication.chat.implementation.converters.AddChatParticipantsResultConverter;
import com.azure.communication.chat.implementation.converters.ChatErrorConverter;
import com.azure.communication.chat.implementation.converters.ChatThreadPropertiesConverter;
import com.azure.communication.chat.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.chat.models.ChatError;
import com.azure.communication.chat.models.ChatErrorResponseException;
import com.azure.communication.chat.models.ChatThreadProperties;
import com.azure.communication.chat.models.InvalidParticipantException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.azure.communication.chat.implementation.AzureCommunicationChatServiceImpl;
import com.azure.communication.chat.implementation.converters.AddChatParticipantsOptionsConverter;
import com.azure.communication.chat.implementation.converters.ChatMessageConverter;
import com.azure.communication.chat.implementation.converters.ChatParticipantConverter;
import com.azure.communication.chat.implementation.converters.ChatMessageReadReceiptConverter;
import com.azure.communication.chat.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.chat.implementation.models.SendReadReceiptRequest;
import com.azure.communication.chat.models.AddChatParticipantsResult;
import com.azure.communication.chat.models.ChatMessage;
import com.azure.communication.chat.models.ChatParticipant;
import com.azure.communication.chat.models.ChatMessageReadReceipt;
import com.azure.communication.chat.models.ListChatMessagesOptions;
import com.azure.communication.chat.models.ListParticipantsOptions;
import com.azure.communication.chat.models.ListReadReceiptOptions;
import com.azure.communication.chat.models.SendChatMessageOptions;
import com.azure.communication.chat.models.SendChatMessageResult;
import com.azure.communication.chat.models.TypingNotificationOptions;
import com.azure.communication.chat.models.UpdateChatMessageOptions;
import com.azure.communication.chat.models.UpdateChatThreadOptions;
import com.azure.communication.common.CommunicationIdentifier;
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
 *
 * <p><strong>Instantiating an asynchronous Chat Thread Client</strong></p>
 *
 * {@codesnippet com.azure.communication.chat.chatthreadasyncclient.instantiation}
 *
 * <p>View {@link ChatClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ChatClientBuilder
 */
@ServiceClient(builder = ChatThreadClientBuilder.class, isAsync = true)
public final class ChatThreadAsyncClient {
    private final ClientLogger logger = new ClientLogger(ChatThreadAsyncClient.class);

    private final AzureCommunicationChatServiceImpl chatServiceClient;
    private final ChatThreadsImpl chatThreadClient;

    private final String chatThreadId;

    ChatThreadAsyncClient(AzureCommunicationChatServiceImpl chatServiceClient, String chatThreadId) {
        this.chatServiceClient = chatServiceClient;
        this.chatThreadClient = chatServiceClient.getChatThreads();
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    Mono<Response<Void>> updateTopic(String topic, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.chatThreadClient.updateChatThreadPropertiesWithResponseAsync(
                chatThreadId,
                new UpdateChatThreadOptions()
                    .setTopic(topic),
                context
            ).onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param participants Collection of participants to add.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AddChatParticipantsResult> addParticipants(Iterable<ChatParticipant> participants) {
        try {
            Objects.requireNonNull(participants, "'participants' cannot be null.");
            return withContext(context -> addParticipants(participants, context)
                .flatMap((Response<AddChatParticipantsResult> res) -> {
                    return Mono.just(res.getValue());
                }));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param participants Collection of participants to add.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AddChatParticipantsResult>> addParticipantsWithResponse(Iterable<ChatParticipant> participants) {
        try {
            Objects.requireNonNull(participants, "'participants' cannot be null.");
            return withContext(context -> addParticipants(participants, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Adds a participant to a thread. If the participant already exists, no change occurs.
     *
     * @param participant The new participant.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws InvalidParticipantException thrown if the participant is rejected by the server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return nothing.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> addParticipant(ChatParticipant participant) {
        return withContext(context -> {
            return addParticipantWithResponse(participant, context).flatMap(resp -> {
                return Mono.empty();
            });
        });
    }

    /**
     * Adds a participant to a thread. If the participant already exists, no change occurs.
     *
     * @param participant The new participant.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws InvalidParticipantException thrown if the participant is rejected by the server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addParticipantWithResponse(ChatParticipant participant) {
        try {
            return withContext(context -> addParticipantWithResponse(participant, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Adds a participant to a thread. If the participant already exists, no change occurs.
     *
     * @param participant The new participant.
     * @param context The context to associate with this operation.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws InvalidParticipantException thrown if the participant is rejected by the server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    Mono<Response<Void>> addParticipantWithResponse(ChatParticipant participant, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return addParticipants(Collections.singletonList(participant), context)
                .flatMap((Response<AddChatParticipantsResult> res) -> {
                    if (res.getValue().getInvalidParticipants() != null) {
                        if (res.getValue().getInvalidParticipants().size() > 0) {
                            ChatError error = res.getValue().getInvalidParticipants()
                                .stream()
                                .findFirst()
                                .get();

                            return Mono.error(new InvalidParticipantException(error));
                        }
                    }

                    return Mono.empty();
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param participants Collection of participants to add.
     * @param context The context to associate with this operation.
     * @return the result.
     */
    Mono<Response<AddChatParticipantsResult>> addParticipants(Iterable<ChatParticipant> participants, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.chatThreadClient.addChatParticipantsWithResponseAsync(
                chatThreadId, AddChatParticipantsOptionsConverter.convert(participants), context)
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                .map(result -> new SimpleResponse<AddChatParticipantsResult>(
                    result, AddChatParticipantsResultConverter.convert(result.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove a participant from a thread.
     *
     * @param identifier Identity of the participant to remove from the thread.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeParticipant(CommunicationIdentifier identifier) {
        try {
            Objects.requireNonNull(identifier, "'identifier' cannot be null.");

            return withContext(context -> removeParticipant(identifier, context)
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
     * @param identifier Identity of the participant to remove from the thread.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeParticipantWithResponse(CommunicationIdentifier identifier) {
        try {
            Objects.requireNonNull(identifier, "'identifier' cannot be null.");

            return withContext(context -> removeParticipant(identifier, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove a participant from a thread.
     *
     * @param identifier Identity of the participant to remove from the thread.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    Mono<Response<Void>> removeParticipant(CommunicationIdentifier identifier, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.chatThreadClient.removeChatParticipantWithResponseAsync(
                chatThreadId, CommunicationIdentifierConverter.convert(identifier), context)
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets the participants of a thread.
     *
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the participants of a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ChatParticipant> listParticipants() {
        ListParticipantsOptions listParticipantsOptions = new ListParticipantsOptions();
        return listParticipants(listParticipantsOptions, Context.NONE);
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
                            context)
                            .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))),
                nextLink -> withContext(context ->
                    this.chatThreadClient.listChatParticipantsNextSinglePageAsync(nextLink, context)
                            .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e)))),
                f -> ChatParticipantConverter.convert(f));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
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
                            serviceContext)
                            .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e)),
                    nextLink ->
                        this.chatThreadClient.listChatParticipantsNextSinglePageAsync(nextLink, serviceContext)
                            .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))),
                f -> ChatParticipantConverter.convert(f));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Sends a message to a thread.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Send a chat message based on "options".</p>
     *
     * {@codesnippet com.azure.communication.chat.chatclient.sendmessageasync}
     *
     * @param options Options for sending the message.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the SendChatMessageResult.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SendChatMessageResult> sendMessage(SendChatMessageOptions options) {
        try {
            Objects.requireNonNull(options, "'options' cannot be null.");
            return withContext(context -> sendMessage(options, context)
                .flatMap(res -> Mono.just(res.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sends a message to a thread.
     *
     * @param options Options for sending the message.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the SendChatMessageResult.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SendChatMessageResult>> sendMessageWithResponse(SendChatMessageOptions options) {
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
     * @return the SendChatMessageResult.
     */
    Mono<Response<SendChatMessageResult>> sendMessage(SendChatMessageOptions options, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.chatThreadClient.sendChatMessageWithResponseAsync(chatThreadId, options, context)
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                .map(result -> new SimpleResponse<SendChatMessageResult>(result, (result.getValue())));
        }
        catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets a message by id.
     *
     * @param chatMessageId The message id.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
        try {
            return this.chatThreadClient.getChatMessageWithResponseAsync(chatThreadId, chatMessageId, context)
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                .map(result -> new SimpleResponse<ChatMessage>(
                    result, ChatMessageConverter.convert(result.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets a list of messages from a thread.
     *
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged list of messages from a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ChatMessage> listMessages() {
        ListChatMessagesOptions listMessagesOptions = new ListChatMessagesOptions();
        try {
            return pagedFluxConvert(new PagedFlux<>(
                    () -> withContext(context -> this.chatThreadClient.listChatMessagesSinglePageAsync(
                        chatThreadId, listMessagesOptions.getMaxPageSize(), listMessagesOptions.getStartTime(), context)
                        .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))),
                    nextLink -> withContext(context -> this.chatThreadClient.listChatMessagesNextSinglePageAsync(
                        nextLink, context)
                        .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e)))),
                f -> ChatMessageConverter.convert(f));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Gets a list of messages from a thread.
     *
     * @param listMessagesOptions The request options.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged list of messages from a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ChatMessage> listMessages(ListChatMessagesOptions listMessagesOptions) {
        final ListChatMessagesOptions serviceListMessagesOptions =
            listMessagesOptions == null ? new ListChatMessagesOptions() : listMessagesOptions;

        try {
            return pagedFluxConvert(new PagedFlux<>(
                    () -> withContext(context -> this.chatThreadClient.listChatMessagesSinglePageAsync(
                        chatThreadId,
                        serviceListMessagesOptions.getMaxPageSize(),
                        serviceListMessagesOptions.getStartTime(),
                        context)
                        .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))),
                    nextLink -> withContext(context -> this.chatThreadClient.listChatMessagesNextSinglePageAsync(
                        nextLink, context)
                        .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e)))),
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
                    () -> this.chatThreadClient.listChatMessagesSinglePageAsync(
                        chatThreadId,
                        serviceListMessagesOptions.getMaxPageSize(),
                        serviceListMessagesOptions.getStartTime(),
                        serviceContext)
                        .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e)),
                    nextLink -> this.chatThreadClient.listChatMessagesNextSinglePageAsync(
                        nextLink, serviceContext)
                        .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))),
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
        try {
            return this.chatThreadClient.updateChatMessageWithResponseAsync(chatThreadId, chatMessageId, options, context)
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a message.
     *
     * @param chatMessageId The message id.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
        try {
            return this.chatThreadClient.deleteChatMessageWithResponseAsync(chatThreadId, chatMessageId, context)
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendTypingNotification() {
        try {
            TypingNotificationOptions options = new TypingNotificationOptions();
            return withContext(context -> sendTypingNotification(options, context)
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendTypingNotificationWithResponse() {
        try {
            TypingNotificationOptions options = new TypingNotificationOptions();
            return withContext(context -> sendTypingNotification(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @param options Options for sending the typing notification.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendTypingNotificationWithResponse(TypingNotificationOptions options) {
        try {
            Objects.requireNonNull(options, "'options' cannot be null.");
            return withContext(context -> sendTypingNotification(options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @param options Options for sending the typing notification.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    Mono<Response<Void>> sendTypingNotification(TypingNotificationOptions options, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            Objects.requireNonNull(options, "'options' cannot be null.");
            return this.chatThreadClient.sendTypingNotificationWithResponseAsync(chatThreadId, options, context)
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Posts a read receipt event to a thread, on behalf of a user.
     *
     * @param chatMessageId The id of the chat message that was read.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
        try {
            SendReadReceiptRequest request = new SendReadReceiptRequest()
                .setChatMessageId(chatMessageId);
            return this.chatThreadClient.sendChatReadReceiptWithResponseAsync(chatThreadId, request, context)
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets read receipts for a thread.
     *
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return read receipts for a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ChatMessageReadReceipt> listReadReceipts() {
        try {
            ListReadReceiptOptions listReadReceiptOptions = new ListReadReceiptOptions();
            return listReadReceipts(listReadReceiptOptions, Context.NONE);
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
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
                        context)
                        .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))),
                    nextLink -> withContext(context -> this.chatThreadClient.listChatReadReceiptsNextSinglePageAsync(
                        nextLink, context)
                        .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e)))),
                f -> ChatMessageReadReceiptConverter.convert(f));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Gets read receipts for a thread.
     *
     * @param listReadReceiptOptions The additional options for this operation.
     * @param context The context to associate with this operation.
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
                        serviceContext)
                        .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e)),
                    nextLink -> this.chatThreadClient.listChatReadReceiptsNextSinglePageAsync(
                        nextLink, serviceContext)
                        .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))),
                f -> ChatMessageReadReceiptConverter.convert(f));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Gets chat thread properties.
     *
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return chat thread properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ChatThreadProperties> getProperties() {
        try {
            Objects.requireNonNull(chatThreadId, "'chatThreadId' cannot be null.");
            return withContext(context -> getProperties(context)
                .flatMap(
                    (Response<ChatThreadProperties> res) -> {
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
     * Gets chat thread properties.
     *
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return chat thread properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ChatThreadProperties>> getPropertiesWithResponse() {
        try {
            Objects.requireNonNull(chatThreadId, "'chatThreadId' cannot be null.");
            return withContext(context -> getProperties(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets chat thread properties.
     *
     * @param context The context to associate with this operation.
     * @return chat thread properties.
     */
    Mono<Response<ChatThreadProperties>> getProperties(Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.chatThreadClient.getChatThreadPropertiesWithResponseAsync(this.chatThreadId, context)
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                .flatMap(
                    (Response<com.azure.communication.chat.implementation.models.ChatThreadProperties> res) -> {
                        return Mono.just(new SimpleResponse<ChatThreadProperties>(
                            res, ChatThreadPropertiesConverter.convert(res.getValue())));
                    });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
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

    private ChatErrorResponseException translateException(CommunicationErrorResponseException exception) {
        ChatError error = null;
        if (exception.getValue() != null) {
            error = ChatErrorConverter.convert(exception.getValue().getError());
        }
        return new ChatErrorResponseException(exception.getMessage(), exception.getResponse(), error);
    }
}
