// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.chat.implementation;

import com.azure.communication.chat.models.*;
import com.azure.communication.common.CommunicationUserIdentifier;

import java.util.*;

public class ChatOptionsProvider {

    public static CreateChatThreadOptions createThreadOptions(String userId1, String userId2) {
        CreateChatThreadOptions options = new CreateChatThreadOptions("Test");

        options.addParticipant(generateParticipant(
            userId1,
            "Tester 1"));
        options.addParticipant(generateParticipant(
            userId2,
            "Tester 2"));

        return options;
    }

    public static UpdateChatThreadOptions updateThreadOptions() {
        UpdateChatThreadOptions options = new UpdateChatThreadOptions();
        options.setTopic("Update Test");

        return options;
    }

    public static Iterable<ChatParticipant> addParticipantsOptions(String userId1, String userId2) {
        List<ChatParticipant> participants = new ArrayList<ChatParticipant>();
        participants.add(generateParticipant(
            userId1,
            "Added Tester 1"));
        participants.add(generateParticipant(
            userId2,
            "Added Tester 2"));

        return participants;
    }

    public static SendChatMessageOptions sendMessageOptions() {
        SendChatMessageOptions options = new SendChatMessageOptions();
        options.setContent("Content");
        options.setSenderDisplayName("Tester");
        options.setType(ChatMessageType.TEXT);

        return options;
    }

    public static UpdateChatMessageOptions updateMessageOptions() {
        UpdateChatMessageOptions options = new UpdateChatMessageOptions();
        options.setContent("Update Test");

        return options;
    }

    private static ChatParticipant generateParticipant(String id, String displayName) {
        ChatParticipant chatParticipant = new ChatParticipant();
        chatParticipant.setCommunicationIdentifier(new CommunicationUserIdentifier(id));
        chatParticipant.setDisplayName(displayName);

        return chatParticipant;
    }
}
