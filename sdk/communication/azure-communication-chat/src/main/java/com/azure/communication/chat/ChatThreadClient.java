// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.chat;

import com.azure.communication.chat.models.AddChatParticipantsResult;
import com.azure.communication.chat.models.AddChatParticipantsOptions;
import com.azure.communication.chat.models.ChatMessage;
import com.azure.communication.chat.models.ChatParticipant;
import com.azure.communication.chat.models.ChatMessageReadReceipt;
import com.azure.communication.chat.models.ListChatMessagesOptions;
import com.azure.communication.chat.models.ListParticipantsOptions;
import com.azure.communication.chat.models.ListReadReceiptOptions;
import com.azure.communication.chat.models.SendChatMessageOptions;
import com.azure.communication.chat.models.UpdateChatMessageOptions;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.util.Collections;

/**
 * Sync Client that supports chat thread operations.
 */
@ServiceClient(builder = ChatClientBuilder.class, isAsync = false)
public final class ChatThreadClient {
    private final ClientLogger logger = new ClientLogger(ChatThreadClient.class);

    private final ChatThreadAsyncClient client;

    private final String chatThreadId;

    /**
     * Creates a ChatClient that sends requests to the chat service at {@code serviceEndpoint}. Each
     * service call goes through the {@code pipeline}.
     *
     * @param client The {@link ChatAsyncClient} that the client routes its request through.
     */
    ChatThreadClient(ChatThreadAsyncClient client) {
        this.client = client;
        this.chatThreadId = client.getChatThreadId();
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
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateTopic(String topic) {

        this.client.updateTopic(topic).block();
    }

    /**
     * Updates a thread's topic.
     *
     * @param topic The new topic.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> updateTopicWithResponse(String topic, Context context) {

        return this.client.updateTopic(topic, context).block();
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param options Options for adding participants.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void addParticipants(AddChatParticipantsOptions options) {

        this.client.addParticipants(options).block();
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param options Options for adding participants.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AddChatParticipantsResult> addParticipantsWithResponse(
        AddChatParticipantsOptions options, Context context) {
        return this.client.addParticipants(options, context).block();
    }

    /**
     * Adds a participant to a thread. If the participant already exists, no change occurs.
     *
     * @param participant The new participant.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void addParticipant(ChatParticipant participant) {

        this.client.addParticipants(new AddChatParticipantsOptions()
            .setParticipants(Collections.singletonList(participant))).block();
    }

    /**
     * Adds a participant to a thread. If the participant already exists, no change occurs.
     *
     * @param participant The new participant.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AddChatParticipantsResult> addParticipantWithResponse(ChatParticipant participant,
                                                                          Context context) {

        return this.client.addParticipants(new AddChatParticipantsOptions()
            .setParticipants(Collections.singletonList(participant)), context).block();
    }

    /**
     * Remove a participant from a thread.
     *
     * @param user User identity of the participant to remove from the thread.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeParticipantWithResponse(CommunicationUserIdentifier user, Context context) {

        return this.client.removeParticipant(user, context).block();
    }

    /**
     * Remove a participant from a thread.
     *
     * @param user User identity of the thread participant to remove from the thread.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void removeParticipant(CommunicationUserIdentifier user) {

        this.client.removeParticipant(user).block();
    }

    /**
     * Gets the participants of a thread.
     *
     * @return the participants of a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ChatParticipant> listParticipants() {

        return new PagedIterable<>(this.client.listParticipants());
    }

    /**
     * Gets the participants of a thread.
     *
     * @param listParticipantsOptions The request options.
     * @return the participants of a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ChatParticipant> listParticipants(ListParticipantsOptions listParticipantsOptions) {
        return new PagedIterable<>(this.client.listParticipants(listParticipantsOptions));
    }

    /**
     * Gets the participants of a thread.
     *
     * @param context The context to associate with this operation.
     * @return the participants of a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ChatParticipant> listParticipants(Context context) {

        return new PagedIterable<>(this.client.listParticipants(context));
    }

    /**
     * Gets the participants of a thread.
     *
     * @param listParticipantsOptions The request options.
     * @param context The context to associate with this operation.
     * @return the participants of a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ChatParticipant> listParticipants(ListParticipantsOptions listParticipantsOptions,
                                                           Context context) {
        return new PagedIterable<>(this.client.listParticipants(listParticipantsOptions, context));
    }

    /**
     * Sends a message to a thread.
     *
     * @param options Options for sending the message.
     * @param context The context to associate with this operation.
     * @return the MessageId.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> sendMessageWithResponse(SendChatMessageOptions options, Context context) {

        return this.client.sendMessage(options, context).block();
    }

    /**
     * Sends a message to a thread.
     *
     * @param options Options for sending the message.
     * @return the MessageId.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String sendMessage(SendChatMessageOptions options) {

        return this.client.sendMessage(options).block();
    }

    /**
     * Gets a message by id.
     *
     * @param chatMessageId The message id.
     * @param context The context to associate with this operation.
     * @return a message by id.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ChatMessage> getMessageWithResponse(String chatMessageId, Context context) {

        return this.client.getMessage(chatMessageId, context).block();
    }

    /**
     * Gets a message by id.
     *
     * @param chatMessageId The message id.
     * @return a message by id.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ChatMessage getMessage(String chatMessageId) {

        return this.client.getMessage(chatMessageId).block();
    }

    /**
     * Gets a list of messages from a thread.
     *
     * @return a list of messages from a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ChatMessage> listMessages() {

        return new PagedIterable<>(this.client.listMessages());
    }

    /**
     * Gets a list of messages from a thread.
     *
     * @param listMessagesOptions The request options.
     * @param context The context to associate with this operation.
     * @return a list of messages from a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ChatMessage> listMessages(ListChatMessagesOptions listMessagesOptions, Context context) {

        return new PagedIterable<>(this.client.listMessages(listMessagesOptions, context));
    }

    /**
     * Updates a message.
     *
     * @param chatMessageId The message id.
     * @param options Options for updating the message.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> updateMessageWithResponse(
        String chatMessageId, UpdateChatMessageOptions options, Context context) {

        return this.client.updateMessage(chatMessageId, options, context).block();
    }

    /**
     * Updates a message.
     *
     * @param chatMessageId The message id.
     * @param options Options for updating the message.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateMessage(String chatMessageId, UpdateChatMessageOptions options) {

        this.client.updateMessage(chatMessageId, options).block();
    }

    /**
     * Deletes a message.
     *
     * @param chatMessageId The message id.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteMessageWithResponse(String chatMessageId, Context context) {

        return this.client.deleteMessage(chatMessageId, context).block();
    }

    /**
     * Deletes a message.
     *
     * @param chatMessageId The message id.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteMessage(String chatMessageId) {

        this.client.deleteMessage(chatMessageId).block();
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendTypingNotificationWithResponse(Context context) {

        return this.client.sendTypingNotification(context).block();
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendTypingNotification() {
        this.client.sendTypingNotification().block();
    }

    /**
     * Posts a read receipt event to a thread, on behalf of a user.
     *
     * @param chatMessageId The id of the chat message that was read.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendReadReceiptWithResponse(String chatMessageId, Context context) {

        return this.client.sendReadReceipt(chatMessageId, context).block();
    }

    /**
     * Posts a read receipt event to a thread, on behalf of a user.
     *
     * @param chatMessageId The id of the chat message that was read.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendReadReceipt(String chatMessageId) {

        this.client.sendReadReceipt(chatMessageId).block();
    }

    /**
     * Gets read receipts for a thread.
     *
     * @return read receipts for a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ChatMessageReadReceipt> listReadReceipts() {

        return new PagedIterable<>(this.client.listReadReceipts());
    }

    /**
     * Gets read receipts for a thread.
     *
     * @param listReadReceiptOptions The additional options for this operation.
     * @return read receipts for a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ChatMessageReadReceipt> listReadReceipts(ListReadReceiptOptions listReadReceiptOptions) {
        return new PagedIterable<>(this.client.listReadReceipts(listReadReceiptOptions));
    }

    /**
     * Gets read receipts for a thread.
     *
     * @param context The context to associate with this operation.
     * @return read receipts for a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ChatMessageReadReceipt> listReadReceipts(Context context) {

        return new PagedIterable<>(this.client.listReadReceipts(context));
    }

    /**
     * Gets read receipts for a thread.
     *
     * @param listReadReceiptOptions The additional options for this operation.
     * @param context The context to associate with this operation.
     * @return read receipts for a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ChatMessageReadReceipt> listReadReceipts(ListReadReceiptOptions listReadReceiptOptions,
                                                                  Context context) {
        return new PagedIterable<>(this.client.listReadReceipts(listReadReceiptOptions, context));
    }
}
