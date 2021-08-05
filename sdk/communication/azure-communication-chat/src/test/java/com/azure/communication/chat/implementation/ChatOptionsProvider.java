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
        return sendMessageOptions(ChatMessageType.TEXT, "Content");
    }

    public static SendChatMessageOptions sendMessageOptions(ChatMessageType type, String content) {
        SendChatMessageOptions options = new SendChatMessageOptions();
        options.setContent(content);
        options.setSenderDisplayName("Tester");
        options.setType(type);
        options.setMetadata(generateMessageMetadata());

        return options;
    }

    public static UpdateChatMessageOptions updateMessageOptions() {
        UpdateChatMessageOptions options = new UpdateChatMessageOptions();
        options.setContent("Update Test");
        options.setMetadata(generateUpdatedMessageMetadata());

        return options;
    }

    private static ChatParticipant generateParticipant(String id, String displayName) {
        ChatParticipant chatParticipant = new ChatParticipant();
        chatParticipant.setCommunicationIdentifier(new CommunicationUserIdentifier(id));
        chatParticipant.setDisplayName(displayName);

        return chatParticipant;
    }

    private static Map<String, String> generateMessageMetadata() {
        return new HashMap<String, String>() {
            {
                put("tags", "tags value");
                put("deliveryMode", "deliveryMode value");
                put("onedriveReferences", "onedriveReferences");
                put("amsreferences", "[\\\"test url file 3\\\"]");
                put("key", "value key");
            }
        };
    }

    private static Map<String, String> generateUpdatedMessageMetadata() {
        return new HashMap<String, String>() {
            {
                put("tags", "");
                put("deliveryMode", "deliveryMode value - updated");
                put("onedriveReferences", "onedriveReferences - updated");
                put("amsreferences", "[\\\"test url file 3\\\"]");
            }
        };
    }
}
