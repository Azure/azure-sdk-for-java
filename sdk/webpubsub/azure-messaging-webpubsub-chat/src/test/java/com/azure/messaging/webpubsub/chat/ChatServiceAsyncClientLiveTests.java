// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat;

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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChatServiceAsyncClientLiveTests extends ChatServiceClientTestBase {
    @Test
    public void canManageRolesAndPagination() {
        String firstRoleName = testResourceNamer.randomName("user.java-async-test-", 55);
        String secondRoleName = testResourceNamer.randomName("room.java-async-test-", 55);

        try {
            ChatRole createdRole
                = asyncClient
                    .createOrReplaceRole(firstRoleName,
                        new ChatRole(Collections.singletonList(ChatPermission.USER_CREATE_ROOM)))
                    .block();
            assertEquals(firstRoleName, createdRole.getName());
            assertEquals(firstRoleName, asyncClient.getRole(firstRoleName).block().getName());

            asyncClient
                .createOrReplaceRole(secondRoleName,
                    new ChatRole(Collections.singletonList(ChatPermission.ROOM_PUBLISH_MESSAGE)))
                .block();
            List<ChatRole> roles = asyncClient.listRoles().collectList().block();
            assertTrue(roles.stream().anyMatch(role -> firstRoleName.equals(role.getName())));

            List<PagedResponse<ChatRole>> pages = asyncClient.listRoles().byPage(1).take(2).collectList().block();
            assertFalse(pages.isEmpty());
            PagedResponse<ChatRole> firstPage = pages.get(0);
            assertFalse(firstPage.getValue().isEmpty());
            if (firstPage.getContinuationToken() != null) {
                assertEquals(2, pages.size());
                PagedResponse<ChatRole> secondPage = pages.get(1);
                assertFalse(secondPage.getValue().isEmpty());
                assertFalse(firstPage.getValue().get(0).getName().equals(secondPage.getValue().get(0).getName()));
            }
        } finally {
            cleanup(() -> asyncClient.deleteRole(firstRoleName).block());
            cleanup(() -> asyncClient.deleteRole(secondRoleName).block());
        }
    }

    @Test
    public void canManageRoomsAndReadEmptyConversation() {
        String roomId = testResourceNamer.randomName("java-async-test-room-", 55);

        try {
            ChatRoom createdRoom
                = asyncClient.createOrReplaceRoom(roomId, new ChatRoom("Java async test room")).block();
            assertEquals(roomId, createdRoom.getId());
            assertEquals("Java async test room", asyncClient.getRoom(roomId).block().getTitle());

            ChatConversation conversation = asyncClient.getConversation(createdRoom.getDefaultConversation()).block();
            assertEquals(createdRoom.getDefaultConversation(), conversation.getId());
            assertEquals(roomId, conversation.getParentRoom());
            assertTrue(asyncClient.listMessages(conversation.getId()).collectList().block().isEmpty());
            assertTrue(asyncClient.listRoomMembers(roomId).collectList().block().isEmpty());
        } finally {
            cleanup(() -> asyncClient.deleteRoom(roomId).block());
        }
    }

    @Test
    public void canManageUserRoomAndMember() {
        String suffix = testResourceNamer.randomName("java", 20);
        String userId = "java-async-test-user-" + suffix;
        String roomId = "java-async-test-room-" + suffix;

        try {
            ChatUser user = asyncClient
                .createOrReplaceUser(userId, new HumanChatUser("Java test user", BuiltInChatRoles.USER_NORMAL))
                .block();
            assertEquals(userId, user.getId());
            assertEquals(userId, asyncClient.getUser(userId).block().getId());

            ChatRoom room = asyncClient.createOrReplaceRoom(roomId, new ChatRoom("Java test room")).block();
            assertEquals(roomId, room.getId());

            ChatRoomMember member = asyncClient
                .createOrReplaceRoomMember(roomId, userId, new ChatRoomMember(BuiltInChatRoles.ROOM_MEMBER))
                .block();
            assertEquals(userId, member.getUserId());
            assertEquals(BuiltInChatRoles.ROOM_MEMBER, member.getRoleName());
            assertTrue(asyncClient.listRoomMembers(roomId)
                .collectList()
                .block()
                .stream()
                .anyMatch(item -> userId.equals(item.getUserId())));
            asyncClient.deleteRoomMember(roomId, userId).block();
        } finally {
            cleanup(() -> asyncClient.deleteRoomMember(roomId, userId).block());
            cleanup(() -> asyncClient.deleteRoom(roomId).block());
            cleanup(() -> asyncClient.deleteUser(userId).block());
        }
    }

    @Test
    public void canListUpdateAndDeleteMessages() {
        String suffix = testResourceNamer.randomName("java", 20);
        String userId = "java-async-message-user-" + suffix;
        String roomId = "java-async-message-room-" + suffix;
        String messageText = "Java async live test message " + suffix;
        String conversationId = null;
        String messageId = null;

        try {
            asyncClient
                .createOrReplaceUser(userId,
                    new HumanChatUser("Java async message test user", BuiltInChatRoles.USER_NORMAL))
                .block();
            ChatRoom room
                = asyncClient.createOrReplaceRoom(roomId, new ChatRoom("Java async message test room")).block();
            conversationId = room.getDefaultConversation();
            asyncClient.createOrReplaceRoomMember(roomId, userId, new ChatRoomMember(BuiltInChatRoles.ROOM_MEMBER))
                .block();

            WebPubSubClientAccessToken accessToken
                = entraAsyncClient.getClientAccessToken(new GetClientAccessTokenOptions().setUserId(userId)).block();
            assertNotNull(accessToken.getToken());
            assertNotNull(accessToken.getUrl());
            if (getTestMode() != TestMode.PLAYBACK) {
                ChatMessageSeeder.sendTextMessage(accessToken.getUrl(), conversationId, messageText);
            }

            ChatMessage createdMessage = asyncClient.listMessages(conversationId)
                .filter(message -> userId.equals(message.getCreatedBy())
                    && messageText.equals(message.getContent().getText()))
                .blockFirst();
            assertNotNull(createdMessage);
            messageId = createdMessage.getId();

            ChatMessage updatedText
                = asyncClient
                    .updateMessage(conversationId, messageId,
                        new ChatMessage().setCreatedBy(userId)
                            .setContent(new MessageContent().setText(messageText + " updated")))
                    .block();
            assertEquals(messageText + " updated", updatedText.getContent().getText());

            byte[] binary = new byte[] { 0, 1, 2, (byte) 254, (byte) 255 };
            ChatMessage updatedBinary
                = asyncClient
                    .updateMessage(conversationId, messageId,
                        new ChatMessage().setCreatedBy(userId).setContent(new MessageContent().setBinary(binary)))
                    .block();
            assertArrayEquals(binary, updatedBinary.getContent().getBinary());
            asyncClient.deleteMessage(conversationId, messageId).block();
        } finally {
            String finalConversationId = conversationId;
            String finalMessageId = messageId;
            if (finalConversationId != null && finalMessageId != null) {
                cleanup(() -> asyncClient.deleteMessage(finalConversationId, finalMessageId).block());
            }
            cleanup(() -> asyncClient.deleteRoomMember(roomId, userId).block());
            cleanup(() -> asyncClient.deleteRoom(roomId).block());
            cleanup(() -> asyncClient.deleteUser(userId).block());
        }
    }
}
