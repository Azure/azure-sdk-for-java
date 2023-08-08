// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;


import com.azure.communication.chat.models.ChatMessage;
import com.azure.communication.chat.models.SendChatMessageResult;
import com.azure.communication.chat.models.ChatThreadProperties;
import com.azure.communication.chat.models.ChatParticipant;
import com.azure.communication.chat.models.ChatMessageReadReceipt;
import com.azure.communication.chat.models.CreateChatThreadOptions;
import com.azure.communication.chat.models.CreateChatThreadResult;
import com.azure.communication.chat.models.SendChatMessageOptions;
import com.azure.communication.chat.models.TypingNotificationOptions;
import com.azure.communication.chat.models.UpdateChatMessageOptions;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.CommunicationTokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import java.util.ArrayList;
import java.util.List;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Class containing code snippets that will be injected to README.md.
 */

public class ReadmeSamples {

    /**
     * Sample code for creating a sync chat client.
     *
     * @return the chat client.
     */
    public ChatClient createChatClient() {
        // BEGIN: readme-sample-createChatClient
        String endpoint = "https://<RESOURCE_NAME>.communcationservices.azure.com";

        // Your user access token retrieved from your trusted service
        String token = "SECRET";
        CommunicationTokenCredential credential = new CommunicationTokenCredential(token);

        // Initialize the chat client
        final ChatClientBuilder builder = new ChatClientBuilder();
        builder.endpoint(endpoint)
            .credential(credential);
        ChatClient chatClient = builder.buildClient();
        // END: readme-sample-createChatClient

        return chatClient;
    }

    /**
     * Sample code for creating a chat thread using the sync chat client.
     */
    public void createChatThread() {
        ChatClient chatClient = createChatClient();

        CommunicationUserIdentifier user1 = new CommunicationUserIdentifier("Id 1");
        CommunicationUserIdentifier user2 = new CommunicationUserIdentifier("Id 2");

        // BEGIN: readme-sample-createChatThread
        List<ChatParticipant> participants = new ArrayList<ChatParticipant>();

        ChatParticipant firstParticipant = new ChatParticipant()
            .setCommunicationIdentifier(user1)
            .setDisplayName("Participant Display Name 1");

        ChatParticipant secondParticipant = new ChatParticipant()
            .setCommunicationIdentifier(user2)
            .setDisplayName("Participant Display Name 2");

        participants.add(firstParticipant);
        participants.add(secondParticipant);

        CreateChatThreadOptions createChatThreadOptions = new CreateChatThreadOptions("Topic")
            .setParticipants(participants);
        CreateChatThreadResult result = chatClient.createChatThread(createChatThreadOptions);

        String chatThreadId = result.getChatThread().getId();
        // END: readme-sample-createChatThread
    }

    /**
     * Sample code for getting a chat thread using the sync chat client.
     */
    public void getChatThread() {
        ChatClient chatClient = createChatClient();

        // BEGIN: readme-sample-getChatThread
        ChatThreadClient chatThreadClient = chatClient.getChatThreadClient("Id");
        ChatThreadProperties chatThreadProperties = chatThreadClient.getProperties();
        // END: readme-sample-getChatThread
    }

    /**
     * Sample code for deleting a chat thread using the sync chat client.
     */
    public void deleteChatThread() {
        ChatClient chatClient = createChatClient();

        // BEGIN: readme-sample-deleteChatThread
        String chatThreadId = "Id";
        chatClient.deleteChatThread(chatThreadId);
        // END: readme-sample-deleteChatThread
    }

    /**
     * Sample code for getting a sync chat thread client using the sync chat client.
     *
     * @return the chat thread client.
     */
    public ChatThreadClient getChatThreadClient() {
        ChatClient chatClient = createChatClient();

        // BEGIN: readme-sample-getChatThreadClient
        String chatThreadId = "Id";
        ChatThreadClient chatThreadClient = chatClient.getChatThreadClient(chatThreadId);
        // END: readme-sample-getChatThreadClient

        return chatThreadClient;
    }

    /**
     * Sample code for updating a chat thread topic using the sync chat thread client.
     */
    public void updateTopic() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        // BEGIN: readme-sample-updateTopic
        chatThreadClient.updateTopic("New Topic");
        // END: readme-sample-updateTopic
    }



    /**
     * Sample code for sending a chat message using the sync chat thread client.
     */
    public void sendChatMessage() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        // BEGIN: readme-sample-sendChatMessage
        SendChatMessageOptions sendChatMessageOptions = new SendChatMessageOptions()
            .setContent("Message content")
            .setSenderDisplayName("Sender Display Name");

        SendChatMessageResult sendResult = chatThreadClient.sendMessage(sendChatMessageOptions);
        // END: readme-sample-sendChatMessage
    }

    /**
     * Sample code for getting a chat message using the sync chat thread client.
     */
    public void getChatMessage() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        // BEGIN: readme-sample-getChatMessage
        String chatMessageId = "Id";
        ChatMessage chatMessage = chatThreadClient.getMessage(chatMessageId);
        // END: readme-sample-getChatMessage
    }

    /**
     * Sample code getting the thread messages using the sync chat thread client.
     */
    public void getChatMessages() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        // BEGIN: readme-sample-getChatMessages
        PagedIterable<ChatMessage> chatMessagesResponse = chatThreadClient.listMessages();
        chatMessagesResponse.iterableByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getElements().forEach(message ->
                System.out.printf("Message id is %s.", message.getId()));
        });
        // END: readme-sample-getChatMessages
    }

    /**
     * Sample code updating a thread message using the sync chat thread client.
     */
    public void updateChatMessage() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        // BEGIN: readme-sample-updateChatMessage
        String chatMessageId = "Id";
        UpdateChatMessageOptions updateChatMessageOptions = new UpdateChatMessageOptions()
            .setContent("Updated message content");

        chatThreadClient.updateMessage(chatMessageId, updateChatMessageOptions);
        // END: readme-sample-updateChatMessage
    }

    /**
     * Sample code deleting a thread message using the sync chat thread client.
     */
    public void deleteChatMessage() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        // BEGIN: readme-sample-deleteChatMessage
        String chatMessageId = "Id";
        chatThreadClient.deleteMessage(chatMessageId);
        // END: readme-sample-deleteChatMessage
    }

    /**
     * Sample code listing chat participants using the sync chat thread client.
     */
    public void listChatParticipants() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        // BEGIN: readme-sample-listChatParticipants
        PagedIterable<ChatParticipant> chatParticipantsResponse = chatThreadClient.listParticipants();
        chatParticipantsResponse.iterableByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getElements().forEach(chatParticipant ->
                System.out.printf("Participant id is %s.", ((CommunicationUserIdentifier) chatParticipant.getCommunicationIdentifier()).getId()));
        });
        // END: readme-sample-listChatParticipants
    }

    /**
     * Sample code adding chat participants using the sync chat thread client.
     */
    public void addChatParticipants() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        CommunicationUserIdentifier user1 = new CommunicationUserIdentifier("Id 1");
        CommunicationUserIdentifier user2 = new CommunicationUserIdentifier("Id 2");

        // BEGIN: readme-sample-addChatParticipants
        List<ChatParticipant> participants = new ArrayList<ChatParticipant>();

        ChatParticipant firstParticipant = new ChatParticipant()
            .setCommunicationIdentifier(user1)
            .setDisplayName("Display Name 1");

        ChatParticipant secondParticipant = new ChatParticipant()
            .setCommunicationIdentifier(user2)
            .setDisplayName("Display Name 2");

        participants.add(firstParticipant);
        participants.add(secondParticipant);

        chatThreadClient.addParticipants(participants);
        // END: readme-sample-addChatParticipants
    }

    /**
     * Sample code removing a chat participant using the sync chat thread client.
     */
    public void removeChatParticipant() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        CommunicationUserIdentifier user = new CommunicationUserIdentifier("Id");

        // BEGIN: readme-sample-removeChatParticipant
        chatThreadClient.removeParticipant(user);
        // END: readme-sample-removeChatParticipant
    }

    /**
     * Sample code sending a read receipt using the sync chat thread client.
     */
    public void sendReadReceipt() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        // BEGIN: readme-sample-sendReadReceipt
        String chatMessageId = "Id";
        chatThreadClient.sendReadReceipt(chatMessageId);
        // END: readme-sample-sendReadReceipt
    }

    /**
     * Sample code listing read receipts using the sync chat thread client.
     */
    public void listReadReceipts() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        // BEGIN: readme-sample-listReadReceipts
        PagedIterable<ChatMessageReadReceipt> readReceiptsResponse = chatThreadClient.listReadReceipts();
        readReceiptsResponse.iterableByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getElements().forEach(readReceipt ->
                System.out.printf("Read message id is %s.", readReceipt.getChatMessageId()));
        });
        // END: readme-sample-listReadReceipts
    }

    /**
     * Sample code sending a read receipt using the sync chat thread client.
     */
    public void sendTypingNotification() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        // BEGIN: readme-sample-sendTypingNotification
        TypingNotificationOptions options = new TypingNotificationOptions();
        options.setSenderDisplayName("Sender Display Name");
        chatThreadClient.sendTypingNotificationWithResponse(options, Context.NONE);
        // END: readme-sample-sendTypingNotification
    }
}
