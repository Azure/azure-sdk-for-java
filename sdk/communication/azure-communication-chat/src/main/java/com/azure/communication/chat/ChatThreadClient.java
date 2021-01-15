// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.chat;

import com.azure.communication.chat.models.AddChatThreadMembersOptions;
import com.azure.communication.chat.models.ChatMessage;
import com.azure.communication.chat.models.ListChatMessagesOptions;
import com.azure.communication.chat.models.ReadReceipt;
import com.azure.communication.chat.models.SendChatMessageOptions;
import com.azure.communication.chat.models.SendChatMessageResult;
import com.azure.communication.chat.models.ChatThreadMember;
import com.azure.communication.chat.models.UpdateChatMessageOptions;
import com.azure.communication.chat.models.UpdateChatThreadOptions;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

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
     * Updates a thread's properties.
     *
     * @param options Options for updating a chat thread.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateChatThread(UpdateChatThreadOptions options) {

        this.client.updateChatThread(options).block();
    }

    /**
     * Updates a thread's properties.
     *
     * @param options Options for updating a chat thread.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> updateChatThreadWithResponse(UpdateChatThreadOptions options, Context context) {

        return this.client.updateChatThread(options, context).block();
    }

    /**
     * Adds thread members to a thread. If members already exist, no change occurs.
     *
     * @param options Options for adding thread members.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void addMembers(AddChatThreadMembersOptions options) {

        this.client.addMembers(options).block();
    }

    /**
     * Adds thread members to a thread. If members already exist, no change occurs.
     *
     * @param options Options for adding thread members.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> addMembersWithResponse(AddChatThreadMembersOptions options, Context context) {

        return this.client.addMembers(options, context).block();
    }

    /**
     * Remove a member from a thread.
     *
     * @param user User identity of the thread member to remove from the thread.
     * @param context The context to associate with this operation.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeMemberWithResponse(CommunicationUserIdentifier user, Context context) {

        return this.client.removeMember(user, context).block();
    }

    /**
     * Remove a member from a thread.
     *
     * @param user User identity of the thread member to remove from the thread.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void removeMember(CommunicationUserIdentifier user) {

        this.client.removeMember(user).block();
    }

    /**
     * Gets the members of a thread.
     *
     * @return the members of a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ChatThreadMember> listMembers() {

        return new PagedIterable<>(this.client.listMembers());
    }

    /**
     * Gets the members of a thread.
     *
     * @param context The context to associate with this operation.
     * @return the members of a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ChatThreadMember> listMembers(Context context) {

        return new PagedIterable<>(this.client.listMembers(context));
    }

    /**
     * Sends a message to a thread.
     *
     * @param options Options for sending the message.
     * @param context The context to associate with this operation.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SendChatMessageResult> sendMessageWithResponse(SendChatMessageOptions options, Context context) {

        return this.client.sendMessage(options, context).block();
    }

    /**
     * Sends a message to a thread.
     *
     * @param options Options for sending the message.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SendChatMessageResult sendMessage(SendChatMessageOptions options) {

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
    public PagedIterable<ReadReceipt> listReadReceipts() {

        return new PagedIterable<>(this.client.listReadReceipts());
    }

    /**
     * Gets read receipts for a thread.
     *
     * @param context The context to associate with this operation.
     * @return read receipts for a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ReadReceipt> listReadReceipts(Context context) {

        return new PagedIterable<>(this.client.listReadReceipts(context));
    }
}
