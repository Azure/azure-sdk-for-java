// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.test.TestMode;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.messaging.webpubsub.chat.models.BuiltInChatRoles;
import com.azure.messaging.webpubsub.chat.models.ChatConversation;
import com.azure.messaging.webpubsub.chat.models.ChatMessage;
import com.azure.messaging.webpubsub.chat.models.ChatPermission;
import com.azure.messaging.webpubsub.chat.models.ChatRole;
import com.azure.messaging.webpubsub.chat.models.ChatRoom;
import com.azure.messaging.webpubsub.chat.models.ChatRoomMember;
import com.azure.messaging.webpubsub.chat.models.ChatTopic;
import com.azure.messaging.webpubsub.chat.models.ChatTopicState;
import com.azure.messaging.webpubsub.chat.models.ChatUser;
import com.azure.messaging.webpubsub.chat.models.ChatUserRoom;
import com.azure.messaging.webpubsub.chat.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.chat.models.HumanChatUser;
import com.azure.messaging.webpubsub.chat.models.MessageContent;
import com.azure.messaging.webpubsub.chat.models.WebPubSubClientAccessToken;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.azure.core.test.SyncAsyncExtension.execute;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChatServiceClientLiveTests extends ChatServiceClientTestBase {
    @SyncAsyncTest
    public void canManageTopicsWithConditionsAndPagination() {
        String roomId = execute(() -> testResourceNamer.randomName("java-topic-room-", 50),
            () -> Mono.just(testResourceNamer.randomName("java-async-topic-room-", 55)));
        String title = execute(() -> "Java topic test room", () -> Mono.just("Java async topic test room"));
        String firstTopicId = "first";
        String secondTopicId = "second";

        try {
            execute(() -> client.createOrReplaceRoom(roomId, new ChatRoom(title)),
                () -> asyncClient.createOrReplaceRoom(roomId, new ChatRoom(title)));
            ChatTopic created = execute(
                () -> client.createOrReplaceTopic(roomId, firstTopicId, new ChatTopic().setTitle("First topic"),
                    new MatchConditions().setIfNoneMatch("*")),
                () -> asyncClient.createOrReplaceTopic(roomId, firstTopicId, new ChatTopic().setTitle("First topic"),
                    new MatchConditions().setIfNoneMatch("*")));
            assertEquals(firstTopicId, created.getId());
            assertEquals(ChatTopicState.NORMAL, created.getState());
            assertNotNull(created.getCreatedAt());
            assertNotNull(created.getCreatedBy());
            assertNotNull(created.getConversationId());
            assertNotNull(created.getEtag());
            assertEquals("First topic", execute(() -> entraClient.getTopic(roomId, firstTopicId),
                () -> entraAsyncClient.getTopic(roomId, firstTopicId)).getTitle());

            HttpResponseException duplicate = assertThrows(HttpResponseException.class,
                () -> execute(
                    () -> client.createOrReplaceTopic(roomId, firstTopicId, new ChatTopic().setTitle("Duplicate"),
                        new MatchConditions().setIfNoneMatch("*")),
                    () -> asyncClient.createOrReplaceTopic(roomId, firstTopicId, new ChatTopic().setTitle("Duplicate"),
                        new MatchConditions().setIfNoneMatch("*"))));
            assertEquals(412, duplicate.getResponse().getStatusCode());

            ChatTopic replaced = execute(
                () -> client.createOrReplaceTopic(roomId, firstTopicId, new ChatTopic().setTitle("Replaced topic"),
                    new MatchConditions().setIfMatch(created.getEtag())),
                () -> asyncClient.createOrReplaceTopic(roomId, firstTopicId, new ChatTopic().setTitle("Replaced topic"),
                    new MatchConditions().setIfMatch(created.getEtag())));
            ChatTopic updated = execute(
                () -> client.updateTopic(roomId, firstTopicId,
                    new ChatTopic().setTitle("Updated topic").setState(ChatTopicState.ARCHIVED),
                    new MatchConditions().setIfMatch(replaced.getEtag())),
                () -> asyncClient.updateTopic(roomId, firstTopicId,
                    new ChatTopic().setTitle("Updated topic").setState(ChatTopicState.ARCHIVED),
                    new MatchConditions().setIfMatch(replaced.getEtag())));
            assertEquals("Updated topic", updated.getTitle());
            assertEquals(ChatTopicState.ARCHIVED, updated.getState());
            assertEquals(created.getConversationId(), updated.getConversationId());
            HttpResponseException stale = assertThrows(HttpResponseException.class,
                () -> execute(
                    () -> client.updateTopic(roomId, firstTopicId, new ChatTopic().setTitle("Stale update"),
                        new MatchConditions().setIfMatch(replaced.getEtag())),
                    () -> asyncClient.updateTopic(roomId, firstTopicId, new ChatTopic().setTitle("Stale update"),
                        new MatchConditions().setIfMatch(replaced.getEtag()))));
            assertEquals(412, stale.getResponse().getStatusCode());

            execute(() -> client.createOrReplaceTopic(roomId, secondTopicId, new ChatTopic().setTitle("Second topic")),
                () -> asyncClient.createOrReplaceTopic(roomId, secondTopicId,
                    new ChatTopic().setTitle("Second topic")));
            List<PagedResponse<ChatTopic>> pages = execute(
                () -> StreamSupport.stream(client.listTopics(roomId).iterableByPage(1).spliterator(), false)
                    .limit(2)
                    .collect(Collectors.toList()),
                () -> asyncClient.listTopics(roomId).byPage(1).take(2).collectList());
            assertEquals(2, pages.size());
            assertEquals(1, pages.get(0).getValue().size());
            assertNotNull(pages.get(0).getContinuationToken());
            assertEquals(1, pages.get(1).getValue().size());
            assertNotEquals(pages.get(0).getValue().get(0).getId(), pages.get(1).getValue().get(0).getId());
            List<String> topicIds
                = execute(() -> client.listTopics(roomId).stream().map(ChatTopic::getId).collect(Collectors.toList()),
                    () -> asyncClient.listTopics(roomId).map(ChatTopic::getId).collectList());
            assertTrue(topicIds.contains(firstTopicId));
            assertTrue(topicIds.contains(secondTopicId));

            execute(() -> client.deleteTopic(roomId, firstTopicId, new MatchConditions().setIfMatch(updated.getEtag())),
                () -> asyncClient.deleteTopic(roomId, firstTopicId,
                    new MatchConditions().setIfMatch(updated.getEtag())));
            execute(() -> client.deleteTopic(roomId, secondTopicId),
                () -> asyncClient.deleteTopic(roomId, secondTopicId));
        } finally {
            cleanup(() -> execute(() -> client.deleteRoom(roomId), () -> asyncClient.deleteRoom(roomId)));
        }
    }

    @SyncAsyncTest
    public void canListRoomsAndUsersWithPagination() {
        String suffix = testResourceNamer.randomName("java", 20);
        String prefix = execute(() -> "java", () -> Mono.just("java-async"));
        String firstRoomId = prefix + "-list-room-a-" + suffix;
        String secondRoomId = prefix + "-list-room-b-" + suffix;
        String firstUserId = prefix + "-list-user-a-" + suffix;
        String secondUserId = prefix + "-list-user-b-" + suffix;

        try {
            execute(() -> client.createOrReplaceRoom(firstRoomId, new ChatRoom("First room")),
                () -> asyncClient.createOrReplaceRoom(firstRoomId, new ChatRoom("First room")));
            execute(() -> client.createOrReplaceRoom(secondRoomId, new ChatRoom("Second room")),
                () -> asyncClient.createOrReplaceRoom(secondRoomId, new ChatRoom("Second room")));
            execute(
                () -> client.createOrReplaceUser(firstUserId,
                    new HumanChatUser("First user", BuiltInChatRoles.USER_NORMAL)),
                () -> asyncClient.createOrReplaceUser(firstUserId,
                    new HumanChatUser("First user", BuiltInChatRoles.USER_NORMAL)));
            execute(
                () -> client.createOrReplaceUser(secondUserId,
                    new HumanChatUser("Second user", BuiltInChatRoles.USER_NORMAL)),
                () -> asyncClient.createOrReplaceUser(secondUserId,
                    new HumanChatUser("Second user", BuiltInChatRoles.USER_NORMAL)));

            List<String> roomIds = execute(
                () -> StreamSupport.stream(client.listRooms().iterableByPage(1).spliterator(), false)
                    .flatMap(page -> page.getValue().stream())
                    .map(ChatRoom::getId)
                    .collect(Collectors.toList()),
                () -> asyncClient.listRooms()
                    .byPage(1)
                    .flatMapIterable(PagedResponse::getValue)
                    .map(ChatRoom::getId)
                    .collectList());
            assertTrue(roomIds.contains(firstRoomId));
            assertTrue(roomIds.contains(secondRoomId));
            List<ChatUser> users = execute(
                () -> StreamSupport.stream(entraClient.listUsers().iterableByPage(1).spliterator(), false)
                    .flatMap(page -> page.getValue().stream())
                    .collect(Collectors.toList()),
                () -> entraAsyncClient.listUsers().byPage(1).flatMapIterable(PagedResponse::getValue).collectList());
            assertTrue(
                users.stream().anyMatch(user -> firstUserId.equals(user.getId()) && user instanceof HumanChatUser));
            assertTrue(users.stream().anyMatch(user -> secondUserId.equals(user.getId())));
        } finally {
            cleanup(() -> execute(() -> client.deleteRoom(firstRoomId), () -> asyncClient.deleteRoom(firstRoomId)));
            cleanup(() -> execute(() -> client.deleteRoom(secondRoomId), () -> asyncClient.deleteRoom(secondRoomId)));
            cleanup(() -> execute(() -> client.deleteUser(firstUserId), () -> asyncClient.deleteUser(firstUserId)));
            cleanup(() -> execute(() -> client.deleteUser(secondUserId), () -> asyncClient.deleteUser(secondUserId)));
        }
    }

    @SyncAsyncTest
    public void canListRoomsForUserWithPagination() {
        String suffix = testResourceNamer.randomName("java", 20);
        String prefix = execute(() -> "java", () -> Mono.just("java-async"));
        String userId = prefix + "-user-rooms-" + suffix;
        String firstRoomId = prefix + "-user-room-a-" + suffix;
        String secondRoomId = prefix + "-user-room-b-" + suffix;

        try {
            execute(
                () -> client.createOrReplaceUser(userId,
                    new HumanChatUser("Room list user", BuiltInChatRoles.USER_NORMAL)),
                () -> asyncClient.createOrReplaceUser(userId,
                    new HumanChatUser("Room list user", BuiltInChatRoles.USER_NORMAL)));
            execute(() -> client.createOrReplaceRoom(firstRoomId, new ChatRoom("First user room")),
                () -> asyncClient.createOrReplaceRoom(firstRoomId, new ChatRoom("First user room")));
            execute(() -> client.createOrReplaceRoom(secondRoomId, new ChatRoom("Second user room")),
                () -> asyncClient.createOrReplaceRoom(secondRoomId, new ChatRoom("Second user room")));
            assertEquals(0L, execute(() -> client.listRoomsForUser(userId).stream().count(),
                () -> asyncClient.listRoomsForUser(userId).count()));
            execute(
                () -> client.createOrReplaceRoomMember(firstRoomId, userId,
                    new ChatRoomMember(BuiltInChatRoles.ROOM_MEMBER)),
                () -> asyncClient.createOrReplaceRoomMember(firstRoomId, userId,
                    new ChatRoomMember(BuiltInChatRoles.ROOM_MEMBER)));
            execute(
                () -> client.createOrReplaceRoomMember(secondRoomId, userId,
                    new ChatRoomMember(BuiltInChatRoles.ROOM_MEMBER)),
                () -> asyncClient.createOrReplaceRoomMember(secondRoomId, userId,
                    new ChatRoomMember(BuiltInChatRoles.ROOM_MEMBER)));

            List<PagedResponse<ChatUserRoom>> pages
                = execute(
                    () -> StreamSupport.stream(client.listRoomsForUser(userId).iterableByPage(1).spliterator(), false)
                        .collect(Collectors.toList()),
                    () -> asyncClient.listRoomsForUser(userId).byPage(1).collectList());
            assertEquals(2, pages.size());
            assertEquals(1, pages.get(0).getValue().size());
            assertNotNull(pages.get(0).getContinuationToken());
            assertEquals(1, pages.get(1).getValue().size());
            assertNotEquals(pages.get(0).getValue().get(0).getId(), pages.get(1).getValue().get(0).getId());
            List<ChatUserRoom> rooms
                = execute(() -> client.listRoomsForUser(userId).stream().collect(Collectors.toList()),
                    () -> asyncClient.listRoomsForUser(userId).collectList());
            assertEquals(2, rooms.size());
            assertTrue(rooms.stream()
                .anyMatch(room -> firstRoomId.equals(room.getId())
                    && "First user room".equals(room.getTitle())
                    && room.getDefaultConversation() != null));
            assertTrue(rooms.stream().anyMatch(room -> secondRoomId.equals(room.getId())));
        } finally {
            cleanup(() -> execute(() -> client.deleteRoom(firstRoomId), () -> asyncClient.deleteRoom(firstRoomId)));
            cleanup(() -> execute(() -> client.deleteRoom(secondRoomId), () -> asyncClient.deleteRoom(secondRoomId)));
            cleanup(() -> execute(() -> client.deleteUser(userId), () -> asyncClient.deleteUser(userId)));
        }
    }

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

            ChatMessage updatedText = client.updateMessage(conversationId, messageId,
                new ChatMessage().setCreatedBy(userId).setContent(MessageContent.fromText(messageText + " updated")));
            assertEquals(messageText + " updated", updatedText.getContent().getText());

            byte[] binary = new byte[] { 0, 1, 2, (byte) 254, (byte) 255 };
            ChatMessage updatedBinary = client.updateMessage(conversationId, messageId,
                new ChatMessage().setCreatedBy(userId).setContent(MessageContent.fromBinary(binary)));
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
