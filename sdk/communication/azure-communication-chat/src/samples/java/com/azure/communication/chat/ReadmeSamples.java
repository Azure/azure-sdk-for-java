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
        String endpoint = "https://<RESOURCE_NAME>.communcationservices.azure.com";

        // Your user access token retrieved from your trusted service
        String token = "SECRET";
        CommunicationTokenCredential credential = new CommunicationTokenCredential(token);

        // Initialize the chat client
        final ChatClientBuilder builder = new ChatClientBuilder();
        builder.endpoint(endpoint)
            .credential(credential);
        ChatClient chatClient = builder.buildClient();

        return chatClient;
    }

    /**
     * Sample code for creating a chat thread using the sync chat client.
     */
    public void createChatThread() {
        ChatClient chatClient = createChatClient();

        CommunicationUserIdentifier user1 = new CommunicationUserIdentifier("Id 1");
        CommunicationUserIdentifier user2 = new CommunicationUserIdentifier("Id 2");

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
    }

    /**
     * Sample code for getting a chat thread using the sync chat client.
     */
    public void getChatThread() {
        ChatClient chatClient = createChatClient();

        ChatThreadClient chatThreadClient = chatClient.getChatThreadClient("Id");
        ChatThreadProperties chatThreadProperties = chatThreadClient.getProperties();
    }

    /**
     * Sample code for deleting a chat thread using the sync chat client.
     */
    public void deleteChatThread() {
        ChatClient chatClient = createChatClient();

        String chatThreadId = "Id";
        chatClient.deleteChatThread(chatThreadId);
    }

    /**
     * Sample code for getting a sync chat thread client using the sync chat client.
     *
     * @return the chat thread client.
     */
    public ChatThreadClient getChatThreadClient() {
        ChatClient chatClient = createChatClient();

        String chatThreadId = "Id";
        ChatThreadClient chatThreadClient = chatClient.getChatThreadClient(chatThreadId);

        return chatThreadClient;
    }

    /**
     * Sample code for updating a chat thread topic using the sync chat thread client.
     */
    public void updateTopic() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        chatThreadClient.updateTopic("New Topic");
    }



    /**
     * Sample code for sending a chat message using the sync chat thread client.
     */
    public void sendChatMessage() {

        ChatThreadClient chatThreadClient = getChatThreadClient();

        SendChatMessageOptions sendChatMessageOptions = new SendChatMessageOptions()
            .setContent("Message content")
            .setSenderDisplayName("Sender Display Name");

        SendChatMessageResult sendResult = chatThreadClient.sendMessage(sendChatMessageOptions);
    }

    /**
     * Sample code for getting a chat message using the sync chat thread client.
     */
    public void getChatMessage() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        String chatMessageId = "Id";
        ChatMessage chatMessage = chatThreadClient.getMessage(chatMessageId);
    }

    /**
     * Sample code getting the thread messages using the sync chat thread client.
     */
    public void getChatMessages() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        PagedIterable<ChatMessage> chatMessagesResponse = chatThreadClient.listMessages();
        chatMessagesResponse.iterableByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(message -> {
                System.out.printf("Message id is %s.", message.getId());
            });
        });
    }

    /**
     * Sample code updating a thread message using the sync chat thread client.
     */
    public void updateChatMessage() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        String chatMessageId = "Id";
        UpdateChatMessageOptions updateChatMessageOptions = new UpdateChatMessageOptions()
            .setContent("Updated message content");

        chatThreadClient.updateMessage(chatMessageId, updateChatMessageOptions);
    }

    /**
     * Sample code deleting a thread message using the sync chat thread client.
     */
    public void deleteChatMessage() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        String chatMessageId = "Id";
        chatThreadClient.deleteMessage(chatMessageId);
    }

    /**
     * Sample code listing chat participants using the sync chat thread client.
     */
    public void listChatParticipants() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        PagedIterable<ChatParticipant> chatParticipantsResponse = chatThreadClient.listParticipants();
        chatParticipantsResponse.iterableByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(chatParticipant -> {
                System.out.printf("Participant id is %s.", ((CommunicationUserIdentifier) chatParticipant.getCommunicationIdentifier()).getId());
            });
        });
    }

    /**
     * Sample code adding chat participants using the sync chat thread client.
     */
    public void addChatParticipants() {

        ChatThreadClient chatThreadClient = getChatThreadClient();

        CommunicationUserIdentifier user1 = new CommunicationUserIdentifier("Id 1");
        CommunicationUserIdentifier user2 = new CommunicationUserIdentifier("Id 2");

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
    }

    /**
     * Sample code removing a chat participant using the sync chat thread client.
     */
    public void removeChatParticipant() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        CommunicationUserIdentifier user = new CommunicationUserIdentifier("Id");

        chatThreadClient.removeParticipant(user);
    }

    /**
     * Sample code sending a read receipt using the sync chat thread client.
     */
    public void sendReadReceipt() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        String chatMessageId = "Id";
        chatThreadClient.sendReadReceipt(chatMessageId);
    }

    /**
     * Sample code listing read receipts using the sync chat thread client.
     */
    public void listReadReceipts() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        PagedIterable<ChatMessageReadReceipt> readReceiptsResponse = chatThreadClient.listReadReceipts();
        readReceiptsResponse.iterableByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(readReceipt -> {
                System.out.printf("Read message id is %s.", readReceipt.getChatMessageId());
            });
        });
    }

    /**
     * Sample code sending a read receipt using the sync chat thread client.
     */
    public void sendTypingNotification() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        TypingNotificationOptions options = new TypingNotificationOptions();
        options.setSenderDisplayName("Sender Display Name");
        chatThreadClient.sendTypingNotificationWithResponse(options, Context.NONE);
    }
}
