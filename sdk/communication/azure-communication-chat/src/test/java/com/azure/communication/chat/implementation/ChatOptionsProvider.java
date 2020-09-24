// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.chat.implementation;

import com.azure.communication.chat.models.*;
import com.azure.communication.common.CommunicationUser;

import java.util.*;

public class ChatOptionsProvider {

    public static CreateChatThreadOptions createThreadOptions(String userId1, String userId2) {
        List<ChatThreadMember> members = new ArrayList<ChatThreadMember>();
        members.add(generateThreadMember(
            userId1,
            "Tester 1"));
        members.add(generateThreadMember(
            userId2,
            "Tester 2"));

        CreateChatThreadOptions options = new CreateChatThreadOptions()
            .setTopic("Test")
            .setMembers(members);

        return options;
    }

    public static UpdateChatThreadOptions updateThreadOptions() {
        UpdateChatThreadOptions options = new UpdateChatThreadOptions();
        options.setTopic("Update Test");

        return options;
    }

    public static AddChatThreadMembersOptions addThreadMembersOptions(String userId1, String userId2) {
        List<ChatThreadMember> members = new ArrayList<ChatThreadMember>();
        members.add(generateThreadMember(
            userId1,
            "Added Tester 1"));
        members.add(generateThreadMember(
            userId2,
            "Added Tester 2"));

        AddChatThreadMembersOptions options = new AddChatThreadMembersOptions();
        options.setMembers(members);
        return options;
    }

    public static SendChatMessageOptions sendMessageOptions() {
        SendChatMessageOptions options = new SendChatMessageOptions();
        options.setPriority(ChatMessagePriority.NORMAL);
        options.setContent("Content");
        options.setSenderDisplayName("Tester");

        return options;
    }

    public static UpdateChatMessageOptions updateMessageOptions() {
        UpdateChatMessageOptions options = new UpdateChatMessageOptions();
        options.setContent("Update Test");

        return options;
    }

    private static ChatThreadMember generateThreadMember(String id, String displayName) {
        ChatThreadMember threadMember = new ChatThreadMember();
        threadMember.setUser(new CommunicationUser(id));
        threadMember.setDisplayName(displayName);

        return threadMember;
    }
}
