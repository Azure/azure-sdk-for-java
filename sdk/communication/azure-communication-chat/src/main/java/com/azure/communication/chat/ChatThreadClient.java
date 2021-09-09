// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.chat;

import com.azure.communication.chat.models.AddChatParticipantsResult;
import com.azure.communication.chat.models.ChatErrorResponseException;
import com.azure.communication.chat.models.ChatMessage;
import com.azure.communication.chat.models.ChatParticipant;
import com.azure.communication.chat.models.ChatMessageReadReceipt;
import com.azure.communication.chat.models.ChatThreadProperties;
import com.azure.communication.chat.models.InvalidParticipantException;
import com.azure.communication.chat.models.ListChatMessagesOptions;
import com.azure.communication.chat.models.ListParticipantsOptions;
import com.azure.communication.chat.models.ListReadReceiptOptions;
import com.azure.communication.chat.models.SendChatMessageOptions;
import com.azure.communication.chat.models.SendChatMessageResult;
import com.azure.communication.chat.models.TypingNotificationOptions;
import com.azure.communication.chat.models.UpdateChatMessageOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

/**
 * Sync Client that supports chat thread operations.
 *
 * <p><strong>Instantiating a synchronous Chat Thread Client</strong></p>
 *
 * {@codesnippet com.azure.communication.chat.chatthreadclient.instantiation}
 *
 * <p>View {@link ChatClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ChatClientBuilder
 */
@ServiceClient(builder = ChatThreadClientBuilder.class, isAsync = false)
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> updateTopicWithResponse(String topic, Context context) {

        return this.client.updateTopic(topic, context).block();
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param participants Collection of participants to add.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return AddChatParticipantsResult.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AddChatParticipantsResult addParticipants(Iterable<ChatParticipant> participants) {
        return this.client.addParticipants(participants).block();
    }

    /**
     * Adds participants to a thread. If participants already exist, no change occurs.
     *
     * @param participants Collection of participants to add.
     * @param context The context to associate with this operation.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AddChatParticipantsResult> addParticipantsWithResponse(
        Iterable<ChatParticipant> participants, Context context) {
        return this.client.addParticipants(participants, context).block();
    }

    /**
     * Adds a participant to a thread. If the participant already exists, no change occurs.
     *
     * @param participant The new participant.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws InvalidParticipantException thrown if the participant is rejected by the server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void addParticipant(ChatParticipant participant) {
        this.client.addParticipant(participant).block();
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> addParticipantWithResponse(ChatParticipant participant, Context context) {
        return this.client.addParticipantWithResponse(participant, context).block();
    }

    /**
     * Remove a participant from a thread.
     *
     * @param identifier Identity of the participant to remove from the thread.
     * @param context The context to associate with this operation.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeParticipantWithResponse(CommunicationIdentifier identifier, Context context) {

        return this.client.removeParticipant(identifier, context).block();
    }

    /**
     * Remove a participant from a thread.
     *
     * @param identifier Identity of the thread participant to remove from the thread.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void removeParticipant(CommunicationIdentifier identifier) {

        this.client.removeParticipant(identifier).block();
    }

    /**
     * Gets the participants of a thread.
     *
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @param context The context to associate with this operation.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the SendChatMessageResult.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SendChatMessageResult> sendMessageWithResponse(SendChatMessageOptions options, Context context) {

        return this.client.sendMessage(options, context).block();
    }

    /**
     * Sends a message to a thread.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Send a chat message based on "options".</p>
     *
     * {@codesnippet com.azure.communication.chat.chatclient.sendmessage}
     *
     * @param options Options for sending the message.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the SendChatMessageResult.
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a message by id.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ChatMessage getMessage(String chatMessageId) {

        return this.client.getMessage(chatMessageId).block();
    }

    /**
     * Gets a list of messages from a thread.
     *
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteMessage(String chatMessageId) {

        this.client.deleteMessage(chatMessageId).block();
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @param context The context to associate with this operation.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendTypingNotificationWithResponse(Context context) {
        TypingNotificationOptions options = new TypingNotificationOptions();
        return this.client.sendTypingNotification(options, context).block();
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendTypingNotification() {
        this.client.sendTypingNotification().block();
    }

    /**
     * Posts a typing event to a thread, on behalf of a user.
     *
     * @param options Options for sending the typing notification.
     * @param context The context to associate with this operation.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendTypingNotificationWithResponse(TypingNotificationOptions options, Context context) {
        return this.client.sendTypingNotification(options, context).block();
    }

    /**
     * Posts a read receipt event to a thread, on behalf of a user.
     *
     * @param chatMessageId The id of the chat message that was read.
     * @param context The context to associate with this operation.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendReadReceipt(String chatMessageId) {

        this.client.sendReadReceipt(chatMessageId).block();
    }

    /**
     * Gets read receipts for a thread.
     *
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
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
     * @param context The context to associate with this operation.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return read receipts for a thread.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ChatMessageReadReceipt> listReadReceipts(ListReadReceiptOptions listReadReceiptOptions,
                                                                  Context context) {
        return new PagedIterable<>(this.client.listReadReceipts(listReadReceiptOptions, context));
    }

    /**
     * Gets chat thread properties.
     *
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return chat thread properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ChatThreadProperties getProperties() {
        return this.client.getProperties().block();
    }

    /**
     * Gets chat thread properties.
     *
     * @param context The context to associate with this operation.
     * @throws ChatErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return chat thread properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ChatThreadProperties> getPropertiesWithResponse(Context context) {
        return this.client.getProperties(context).block();
    }

}
