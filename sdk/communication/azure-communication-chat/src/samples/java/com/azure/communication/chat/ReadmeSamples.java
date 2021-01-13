// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import com.azure.communication.chat.models.AddChatThreadMembersOptions;
import com.azure.communication.chat.models.ChatMessagePriority;
import com.azure.communication.chat.models.ChatMessage;
import com.azure.communication.chat.models.ChatThread;
import com.azure.communication.chat.models.ChatThreadMember;
import com.azure.communication.chat.models.CreateChatThreadOptions;
import com.azure.communication.chat.models.ReadReceipt;
import com.azure.communication.chat.models.SendChatMessageOptions;
import com.azure.communication.chat.models.SendChatMessageResult;
import com.azure.communication.chat.models.UpdateChatMessageOptions;
import com.azure.communication.chat.models.UpdateChatThreadOptions;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.CommunicationTokenCredential;
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
        CommunicationTokenCredential credential = new CommunicationTokenCredential(token);

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

        CommunicationUserIdentifier user1 = new CommunicationUserIdentifier("Id 1");
        CommunicationUserIdentifier user2 = new CommunicationUserIdentifier("Id 2");

        List<ChatThreadMember> members = new ArrayList<ChatThreadMember>();

        ChatThreadMember firstThreadMember = new ChatThreadMember()
            .setUser(user1)
            .setDisplayName("Member Display Name 1");

        ChatThreadMember secondThreadMember = new ChatThreadMember()
            .setUser(user2)
            .setDisplayName("Member Display Name 2");

        members.add(firstThreadMember);
        members.add(secondThreadMember);

        CreateChatThreadOptions createChatThreadOptions = new CreateChatThreadOptions()
            .setTopic("Topic")
            .setMembers(members);
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

        SendChatMessageResult sendChatMessageResult = chatThreadClient.sendMessage(sendChatMessageOptions);
        String chatMessageId = sendChatMessageResult.getId();
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
     * Sample code listing chat thread members using the sync chat thread client.
     */
    public void listChatThreadMember() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        PagedIterable<ChatThreadMember> chatThreadMembersResponse = chatThreadClient.listMembers();
        chatThreadMembersResponse.iterableByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(chatMember -> {
                System.out.printf("Member id is %s.", chatMember.getUser().getId());
            });
        });
    }

    /**
     * Sample code adding chat thread members using the sync chat thread client.
     */
    public void addChatThreadMembers() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        CommunicationUserIdentifier user1 = new CommunicationUserIdentifier("Id 1");
        CommunicationUserIdentifier user2 = new CommunicationUserIdentifier("Id 2");

        List<ChatThreadMember> members = new ArrayList<ChatThreadMember>();

        ChatThreadMember firstThreadMember = new ChatThreadMember()
            .setUser(user1)
            .setDisplayName("Display Name 1");

        ChatThreadMember secondThreadMember = new ChatThreadMember()
            .setUser(user2)
            .setDisplayName("Display Name 2");

        members.add(firstThreadMember);
        members.add(secondThreadMember);

        AddChatThreadMembersOptions addChatThreadMembersOptions = new AddChatThreadMembersOptions()
            .setMembers(members);
        chatThreadClient.addMembers(addChatThreadMembersOptions);
    }

    /**
     * Sample code removing a chat thread member using the sync chat thread client.
     */
    public void removeChatThreadMember() {
        ChatThreadClient chatThreadClient = getChatThreadClient();

        CommunicationUserIdentifier user = new CommunicationUserIdentifier("Id");

        chatThreadClient.removeMember(user);
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

        PagedIterable<ReadReceipt> readReceiptsResponse = chatThreadClient.listReadReceipts();
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
