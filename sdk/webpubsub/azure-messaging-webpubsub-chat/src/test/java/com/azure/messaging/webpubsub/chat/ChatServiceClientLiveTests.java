// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.test.TestMode;
import com.azure.messaging.webpubsub.chat.models.BuiltInChatRoles;
import com.azure.messaging.webpubsub.chat.models.ChatConversation;
import com.azure.messaging.webpubsub.chat.models.ChatMessage;
import com.azure.messaging.webpubsub.chat.models.ChatPermission;
import com.azure.messaging.webpubsub.chat.models.ChatRole;
import com.azure.messaging.webpubsub.chat.models.ChatRoom;
import com.azure.messaging.webpubsub.chat.models.ChatRoomMember;
import com.azure.messaging.webpubsub.chat.models.ChatUser;
import com.azure.messaging.webpubsub.chat.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.chat.models.HumanChatUser;
import com.azure.messaging.webpubsub.chat.models.MessageContent;
import com.azure.messaging.webpubsub.chat.models.WebPubSubClientAccessToken;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChatServiceClientLiveTests extends ChatServiceClientTestBase {
    @Test
    public void canManageRolesWithConditionsAndPagination() {
        String firstRoleName = testResourceNamer.randomName("user.java-test-", 50);
        String secondRoleName = testResourceNamer.randomName("room.java-test-", 50);

        try {
            ChatRole createdRole = client.createOrReplaceRole(firstRoleName,
                new ChatRole(Collections.singletonList(ChatPermission.USER_CREATE_ROOM)));
            assertEquals(firstRoleName, createdRole.getName());
            assertEquals(ChatPermission.USER_CREATE_ROOM, client.getRole(firstRoleName).getPermissions().get(0));

            ChatRole replacedRole = client.createOrReplaceRole(firstRoleName,
                new ChatRole(Arrays.asList(ChatPermission.USER_CREATE_ROOM, ChatPermission.USER_FETCH_ALL_ROOMS)),
                new MatchConditions().setIfMatch(createdRole.getEtag()));
            assertFalse(createdRole.getEtag().equals(replacedRole.getEtag()));
            HttpResponseException staleEtag = assertThrows(HttpResponseException.class,
                () -> client.createOrReplaceRole(firstRoleName,
                    new ChatRole(Collections.singletonList(ChatPermission.USER_CREATE_ROOM)),
                    new MatchConditions().setIfMatch(createdRole.getEtag())));
            assertEquals(412, staleEtag.getResponse().getStatusCode());

            client.createOrReplaceRole(secondRoleName,
                new ChatRole(Collections.singletonList(ChatPermission.ROOM_PUBLISH_MESSAGE)),
                new MatchConditions().setIfNoneMatch("*"));
            HttpResponseException alreadyExists = assertThrows(HttpResponseException.class,
                () -> client.createOrReplaceRole(secondRoleName,
                    new ChatRole(Collections.singletonList(ChatPermission.ROOM_HISTORY)),
                    new MatchConditions().setIfNoneMatch("*")));
            assertEquals(412, alreadyExists.getResponse().getStatusCode());

            assertTrue(client.listRoles().stream().anyMatch(role -> firstRoleName.equals(role.getName())));
            Iterator<PagedResponse<ChatRole>> pages = client.listRoles().iterableByPage(1).iterator();
            PagedResponse<ChatRole> firstPage = pages.next();
            assertFalse(firstPage.getValue().isEmpty());
            if (firstPage.getContinuationToken() != null) {
                assertTrue(pages.hasNext());
                PagedResponse<ChatRole> secondPage = pages.next();
                assertFalse(secondPage.getValue().isEmpty());
                assertFalse(firstPage.getValue().get(0).getName().equals(secondPage.getValue().get(0).getName()));
            }
        } finally {
            cleanup(() -> client.deleteRole(firstRoleName));
            cleanup(() -> client.deleteRole(secondRoleName));
        }
    }

    @Test
    public void canManageRoomsAndReadEmptyConversation() {
        String roomId = testResourceNamer.randomName("java-test-room-", 50);

        try {
            ChatRoom createdRoom = client.createOrReplaceRoom(roomId, new ChatRoom("Java test room"));
            assertEquals(roomId, createdRoom.getId());
            assertEquals("Java test room", client.getRoom(roomId).getTitle());

            ChatConversation conversation = client.getConversation(createdRoom.getDefaultConversation());
            assertEquals(createdRoom.getDefaultConversation(), conversation.getId());
            assertEquals(roomId, conversation.getParentRoom());
            assertFalse(client.listMessages(conversation.getId()).stream().findAny().isPresent());
            assertFalse(client.listRoomMembers(roomId).stream().findAny().isPresent());
        } finally {
            cleanup(() -> client.deleteRoom(roomId));
        }
    }

    @Test
    public void canManageUsersAndRoomMembers() {
        String suffix = testResourceNamer.randomName("java", 20);
        String userId = "java-test-user-" + suffix;
        String roomId = "java-test-room-" + suffix;

        try {
            ChatUser createdUser
                = client.createOrReplaceUser(userId, new HumanChatUser("Java test user", BuiltInChatRoles.USER_NORMAL));
            assertEquals(userId, createdUser.getId());
            assertEquals(userId, client.getUser(userId).getId());

            client.createOrReplaceRoom(roomId, new ChatRoom("Java member test room"));
            ChatRoomMember member
                = client.createOrReplaceRoomMember(roomId, userId, new ChatRoomMember(BuiltInChatRoles.ROOM_MEMBER));
            assertEquals(userId, member.getUserId());
            assertTrue(client.listRoomMembers(roomId).stream().anyMatch(item -> userId.equals(item.getUserId())));
            client.deleteRoomMember(roomId, userId);
        } finally {
            cleanup(() -> client.deleteRoomMember(roomId, userId));
            cleanup(() -> client.deleteRoom(roomId));
            cleanup(() -> client.deleteUser(userId));
        }
    }

    @Test
    public void canListUpdateAndDeleteMessages() {
        String suffix = testResourceNamer.randomName("java", 20);
        String userId = "java-test-message-user-" + suffix;
        String roomId = "java-test-message-room-" + suffix;
        String messageText = "Java live test message " + suffix;
        String conversationId = null;
        String messageId = null;

        try {
            client.createOrReplaceUser(userId,
                new HumanChatUser("Java message test user", BuiltInChatRoles.USER_NORMAL));
            ChatRoom room = client.createOrReplaceRoom(roomId, new ChatRoom("Java message test room"));
            conversationId = room.getDefaultConversation();
            client.createOrReplaceRoomMember(roomId, userId, new ChatRoomMember(BuiltInChatRoles.ROOM_MEMBER));

            WebPubSubClientAccessToken accessToken
                = entraClient.getClientAccessToken(new GetClientAccessTokenOptions().setUserId(userId));
            assertNotNull(accessToken.getToken());
            assertNotNull(accessToken.getUrl());

            if (getTestMode() != TestMode.PLAYBACK) {
                ChatMessageSeeder.sendTextMessage(accessToken.getUrl(), conversationId, messageText);
            }
            ChatMessage createdMessage = client.listMessages(conversationId)
                .stream()
                .filter(message -> userId.equals(message.getCreatedBy())
                    && messageText.equals(message.getContent().getText()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("The seeded Chat message was not found."));
            messageId = createdMessage.getId();

            ChatMessage updatedText
                = client.updateMessage(conversationId, messageId, new ChatMessage().setCreatedBy(userId)
                    .setContent(new MessageContent().setText(messageText + " updated")));
            assertEquals(messageText + " updated", updatedText.getContent().getText());

            byte[] binary = new byte[] { 0, 1, 2, (byte) 254, (byte) 255 };
            ChatMessage updatedBinary = client.updateMessage(conversationId, messageId,
                new ChatMessage().setCreatedBy(userId).setContent(new MessageContent().setBinary(binary)));
            assertArrayEquals(binary, updatedBinary.getContent().getBinary());
            client.deleteMessage(conversationId, messageId);
        } finally {
            String finalConversationId = conversationId;
            String finalMessageId = messageId;
            if (finalConversationId != null && finalMessageId != null) {
                cleanup(() -> client.deleteMessage(finalConversationId, finalMessageId));
            }
            cleanup(() -> client.deleteRoomMember(roomId, userId));
            cleanup(() -> client.deleteRoom(roomId));
            cleanup(() -> client.deleteUser(userId));
        }
    }
}
