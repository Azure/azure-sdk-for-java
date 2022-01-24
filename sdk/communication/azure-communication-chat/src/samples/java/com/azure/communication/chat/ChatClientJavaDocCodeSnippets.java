// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import com.azure.communication.chat.models.SendChatMessageResult;
import com.azure.communication.chat.models.ChatParticipant;
import com.azure.communication.chat.models.ChatThreadProperties;
import com.azure.communication.chat.models.CreateChatThreadOptions;
import com.azure.communication.chat.models.CreateChatThreadResult;
import com.azure.communication.chat.models.SendChatMessageOptions;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.CommunicationTokenCredential;
import java.util.ArrayList;
import java.util.List;

public final class ChatClientJavaDocCodeSnippets {

    public ChatClient createChatClient() {

        String endpoint = "https://<RESOURCE_NAME>.communcationservices.azure.com";

        // Your user access token retrieved from your trusted service
        String token = "SECRET";
        CommunicationTokenCredential credential = new CommunicationTokenCredential(token);

        // BEGIN: com.azure.communication.chat.chatclient.instantiation

        // Initialize the chat client builder
        final ChatClientBuilder builder = new ChatClientBuilder()
            .endpoint(endpoint)
            .credential(credential);

        // Build the chat client
        ChatClient chatClient = builder.buildClient();

        // END: com.azure.communication.chat.chatclient.instantiation
        return chatClient;
    }

    public ChatAsyncClient createAsyncChatClient() {

        String endpoint = "https://<RESOURCE_NAME>.communcationservices.azure.com";

        // Your user access token retrieved from your trusted service
        String token = "SECRET";
        CommunicationTokenCredential credential = new CommunicationTokenCredential(token);

        // BEGIN: com.azure.communication.chat.chatasyncclient.instantiation

        // Initialize the chat client builder
        final ChatClientBuilder builder = new ChatClientBuilder()
            .endpoint(endpoint)
            .credential(credential);

        // Build the chat client
        ChatAsyncClient chatClient = builder.buildAsyncClient();

        // END: com.azure.communication.chat.chatasyncclient.instantiation
        return chatClient;
    }

    public ChatThreadClient createChatThreadClient() {

        String endpoint = "https://<RESOURCE_NAME>.communcationservices.azure.com";

        // Your user access token retrieved from your trusted service
        String threadId = "ID";
        String token = "SECRET";
        CommunicationTokenCredential credential = new CommunicationTokenCredential(token);

        // BEGIN: com.azure.communication.chat.chatthreadclient.instantiation

        // Initialize the chat client builder
        final ChatClientBuilder builder = new ChatClientBuilder()
            .endpoint(endpoint)
            .credential(credential);

        // Build the chat client
        ChatClient chatClient = builder.buildClient();

        // Get the chat thread client for your thread's id
        ChatThreadClient chatThreadClient = chatClient.getChatThreadClient(threadId);

        // END: com.azure.communication.chat.chatthreadclient.instantiation
        return chatThreadClient;
    }

    public ChatThreadAsyncClient createChatThreadAsyncClient() {

        String endpoint = "https://<RESOURCE_NAME>.communcationservices.azure.com";

        // Your user access token retrieved from your trusted service
        String threadId = "ID";
        String token = "SECRET";
        CommunicationTokenCredential credential = new CommunicationTokenCredential(token);

        // BEGIN: com.azure.communication.chat.chatthreadasyncclient.instantiation

        // Initialize the chat client builder
        final ChatClientBuilder builder = new ChatClientBuilder()
            .endpoint(endpoint)
            .credential(credential);

        // Build the chat client
        ChatAsyncClient chatClient = builder.buildAsyncClient();

        // Get the chat thread client for your thread's id
        ChatThreadAsyncClient chatThreadClient = chatClient.getChatThreadClient(threadId);

        // END: com.azure.communication.chat.chatthreadasyncclient.instantiation
        return chatThreadClient;
    }

    public void createChatThread() {

        ChatClient chatClient = createChatClient();

        CommunicationUserIdentifier user1 = new CommunicationUserIdentifier("Id 1");
        CommunicationUserIdentifier user2 = new CommunicationUserIdentifier("Id 2");

        // BEGIN: com.azure.communication.chat.chatclient.createchatthread#createchatthreadoptions

        // Initialize the list of chat thread participants
        List<ChatParticipant> participants = new ArrayList<ChatParticipant>();

        ChatParticipant firstParticipant = new ChatParticipant()
            .setCommunicationIdentifier(user1)
            .setDisplayName("Participant Display Name 1");

        ChatParticipant secondParticipant = new ChatParticipant()
            .setCommunicationIdentifier(user2)
            .setDisplayName("Participant Display Name 2");

        participants.add(firstParticipant);
        participants.add(secondParticipant);

        // Create the chat thread
        CreateChatThreadOptions createChatThreadOptions = new CreateChatThreadOptions("Topic")
            .setParticipants(participants);
        CreateChatThreadResult result = chatClient.createChatThread(createChatThreadOptions);

        // Retrieve the chat thread and the id
        ChatThreadProperties chatThread = result.getChatThread();
        String chatThreadId = chatThread.getId();

        // END: com.azure.communication.chat.chatclient.createchatthread#createchatthreadoptions
    }

    public void createChatThreadAsync() {

        ChatAsyncClient chatClient = createAsyncChatClient();

        CommunicationUserIdentifier user1 = new CommunicationUserIdentifier("Id 1");
        CommunicationUserIdentifier user2 = new CommunicationUserIdentifier("Id 2");

        // BEGIN: com.azure.communication.chat.chatasyncclient.createchatthread#createchatthreadoptions

        // Initialize the list of chat thread participants
        List<ChatParticipant> participants = new ArrayList<ChatParticipant>();

        ChatParticipant firstParticipant = new ChatParticipant()
            .setCommunicationIdentifier(user1)
            .setDisplayName("Participant Display Name 1");

        ChatParticipant secondParticipant = new ChatParticipant()
            .setCommunicationIdentifier(user2)
            .setDisplayName("Participant Display Name 2");

        participants.add(firstParticipant);
        participants.add(secondParticipant);

        // Create the chat thread
        CreateChatThreadOptions createChatThreadOptions = new CreateChatThreadOptions("Topic")
            .setParticipants(participants);
        CreateChatThreadResult result = chatClient.createChatThread(createChatThreadOptions).block();

        // Retrieve the chat thread and the id
        ChatThreadProperties chatThread = result.getChatThread();
        String chatThreadId = chatThread.getId();

        // END: com.azure.communication.chat.chatasyncclient.createchatthread#createchatthreadoptions
    }

    public void sendChatMessage() {

        ChatThreadClient chatThreadClient = createChatThreadClient();

        // BEGIN: com.azure.communication.chat.chatthreadclient.sendmessage#sendchatmessageoptions

        // Set the chat message options
        SendChatMessageOptions sendChatMessageOptions = new SendChatMessageOptions()
            .setContent("Message content")
            .setSenderDisplayName("Sender Display Name");

        // Get the request result and the chat message id
        SendChatMessageResult sendResult = chatThreadClient.sendMessage(sendChatMessageOptions);
        String messageId = sendResult.getId();

        // END: com.azure.communication.chat.chatthreadclient.sendmessage#sendchatmessageoptions
    }

    public void sendChatMessageAsync() {

        ChatThreadAsyncClient chatThreadClient = createChatThreadAsyncClient();

        // BEGIN: com.azure.communication.chat.chatthreadasyncclient.sendmessage#sendchatmessageoptions

        // Set the chat message options
        SendChatMessageOptions sendChatMessageOptions = new SendChatMessageOptions()
            .setContent("Message content")
            .setSenderDisplayName("Sender Display Name");

        // Get the request result and the chat message id
        SendChatMessageResult sendResult = chatThreadClient.sendMessage(sendChatMessageOptions).block();
        String messageId = sendResult.getId();

        // END: com.azure.communication.chat.chatthreadasyncclient.sendmessage#sendchatmessageoptions
    }
}
