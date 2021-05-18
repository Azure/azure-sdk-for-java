// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.chat;

import com.azure.communication.chat.models.ChatParticipant;
import com.azure.communication.chat.models.CreateChatThreadOptions;
import com.azure.communication.chat.models.CreateChatThreadResult;
import com.azure.communication.chat.models.SendChatMessageOptions;
import com.azure.communication.chat.models.SendChatMessageResult;
import com.azure.communication.common.CommunicationTokenCredential;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.PagedIterable;
import java.util.ArrayList;
import java.util.List;

public class CreateChatThreadExample {
    public static void main(String[] args) {
        String endpoint = System.getenv("COMMUNICATION_ENDPOINT");
        String token = System.getenv("COMMUNICATION_TOKEN");
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(token);

        // Initialize the chat client
        final ChatClientBuilder builder = new ChatClientBuilder();
        builder.endpoint(endpoint)
            .credential(tokenCredential);
        ChatClient chatClient = builder.buildClient();

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
        System.out.println("Chat thread id " + chatThreadId);

        ChatThreadClient chatThreadClient = chatClient.getChatThreadClient(chatThreadId);

        SendChatMessageOptions sendChatMessageOptions = new SendChatMessageOptions()
            .setContent("Message content")
            .setSenderDisplayName("Sender Display Name");

        SendChatMessageResult sendResult = chatThreadClient.sendMessage(sendChatMessageOptions);

        System.out.println("Send message result ID: " +  sendResult.getId());

        PagedIterable<ChatParticipant> chatParticipantsResponse = chatThreadClient.listParticipants();
        chatParticipantsResponse.iterableByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(chatParticipant -> {
                System.out.printf("Participant id is %s.", ((CommunicationUserIdentifier) chatParticipant.getCommunicationIdentifier()).getId());
            });
        });
    }
}
