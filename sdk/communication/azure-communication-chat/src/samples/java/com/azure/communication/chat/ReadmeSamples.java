// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import com.azure.communication.chat.models.AddChatParticipantsOptions;
import com.azure.communication.chat.models.ChatMessage;
import com.azure.communication.chat.models.ChatMessagePriority;
import com.azure.communication.chat.models.ChatThread;
import com.azure.communication.chat.models.ChatParticipant;
import com.azure.communication.chat.models.ChatMessageReadReceipt;
import com.azure.communication.chat.models.CreateChatThreadOptions;
import com.azure.communication.chat.models.SendChatMessageOptions;
import com.azure.communication.chat.models.UpdateChatMessageOptions;
import com.azure.communication.chat.models.UpdateChatThreadOptions;

import com.azure.communication.common.CommunicationUser;
import com.azure.communication.common.CommunicationUserCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.PagedIterable;

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

        // Create an HttpClient builder of your choice and customize it
        // Use com.azure.core.http.netty.NettyAsyncHttpClientBuilder if that suits your needs
        NettyAsyncHttpClientBuilder httpClientBuilder = new NettyAsyncHttpClientBuilder();
        HttpClient httpClient = httpClientBuilder.build();

        // Your user access token retrieved from your trusted service
        String token = "SECRET";
        CommunicationUserCredential credential = new CommunicationUserCredential(token);

        // Initialize the chat client
        final ChatClientBuilder builder = new ChatClientBuilder();
        builder.endpoint(endpoint)
            .credential(credential)
            .httpClient(httpClient);
        ChatClient chatClient = builder.buildClient();

        return chatClient;
    }

    /**
     * Sample code for creating a chat thread using the sync chat client.
     */
    public void createChatThread() {
        ChatClient chatClient = createChatClient();

        CommunicationUser user1 = new CommunicationUser("Id 1");
        CommunicationUser user2 = new CommunicationUser("Id 2");

        List<ChatParticipant> participants = new ArrayList<ChatParticipant>();

        ChatParticipant firstParticipant = new ChatParticipant()
            .setUser(user1)
            .setDisplayName("Participant Display Name 1");

        ChatParticipant secondParticipant = new ChatParticipant()
            .setUser(user2)
            .setDisplayName("Participant Display Name 2");

        participants.add(firstParticipant);
        participants.add(secondParticipant);

        CreateChatThreadOptions createChatThreadOptions = new CreateChatThreadOptions()
            .setTopic("Topic")
            .setParticipants(participants);
        ChatThreadClient chatThreadClient = chatClient.createChatThread(createChatThreadOptions);
        String chatThreadId = chatThreadClient.getChatThreadId();
    }

    /**
     * Sample code for getting a chat thread using the sync chat client.
     */
    public void getChatThread() {
        ChatClient chatClient = createChatClient();

        String chatThreadId = "Id";
        ChatThread chatThread = chatClient.getChatThread(chatThreadId);
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
     * Sample code for updating a chat thread using the sync chat thread client.
     */
    public void updateChatThread() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        UpdateChatThreadOptions updateChatThreadOptions = new UpdateChatThreadOptions()
            .setTopic("New Topic");
        chatThreadClient.updateChatThread(updateChatThreadOptions);
    }

    /**
     * Sample code for sending a chat message using the sync chat thread client.
     */
    public void sendChatMessage() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        SendChatMessageOptions sendChatMessageOptions = new SendChatMessageOptions()
            .setContent("Message content")
            .setPriority(ChatMessagePriority.NORMAL)
            .setSenderDisplayName("Sender Display Name");


        String chatMessageId = chatThreadClient.sendMessage(sendChatMessageOptions);
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
                System.out.printf("Participant id is %s.", chatParticipant.getUser().getId());
            });
        });
    }

    /**
     * Sample code adding chat participants using the sync chat thread client.
     */
    public void addChatParticipants() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        CommunicationUser user1 = new CommunicationUser("Id 1");
        CommunicationUser user2 = new CommunicationUser("Id 2");

        List<ChatParticipant> participants = new ArrayList<ChatParticipant>();

        ChatParticipant firstParticipant = new ChatParticipant()
            .setUser(user1)
            .setDisplayName("Display Name 1");

        ChatParticipant secondParticipant = new ChatParticipant()
            .setUser(user2)
            .setDisplayName("Display Name 2");

        participants.add(firstParticipant);
        participants.add(secondParticipant);

        AddChatParticipantsOptions addChatParticipantsOptions = new AddChatParticipantsOptions()
            .setParticipants(participants);
        chatThreadClient.addParticipants(addChatParticipantsOptions);
    }

    /**
     * Sample code removing a chat participant using the sync chat thread client.
     */
    public void removeChatParticipant() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        CommunicationUser user = new CommunicationUser("Id");

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

        chatThreadClient.sendTypingNotification();
    }
}
