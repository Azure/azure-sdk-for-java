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
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
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

    @SyncAsyncTest
    public void canManageRolesWithConditionsAndPagination() {
        String firstRoleName = testResourceNamer.randomName("user.java-test-", 50);
        String secondRoleName = testResourceNamer.randomName("room.java-test-", 50);

        try {
            ChatRole createdRole = execute(
                () -> client.createOrReplaceRole(firstRoleName,
                    new ChatRole(Collections.singletonList(ChatPermission.USER_CREATE_ROOM))),
                () -> asyncClient.createOrReplaceRole(firstRoleName,
                    new ChatRole(Collections.singletonList(ChatPermission.USER_CREATE_ROOM))));
            assertEquals(firstRoleName, createdRole.getName());
            ChatRole fetchedRole
                = execute(() -> client.getRole(firstRoleName), () -> asyncClient.getRole(firstRoleName));
            assertEquals(firstRoleName, fetchedRole.getName());
            assertEquals(ChatPermission.USER_CREATE_ROOM, fetchedRole.getPermissions().get(0));

            ChatRole replacedRole
                = execute(
                    () -> client.createOrReplaceRole(firstRoleName,
                        new ChatRole(
                            Arrays.asList(ChatPermission.USER_CREATE_ROOM, ChatPermission.USER_FETCH_ALL_ROOMS)),
                        new MatchConditions().setIfMatch(createdRole.getEtag())),
                    () -> asyncClient.createOrReplaceRole(firstRoleName,
                        new ChatRole(
                            Arrays.asList(ChatPermission.USER_CREATE_ROOM, ChatPermission.USER_FETCH_ALL_ROOMS)),
                        new MatchConditions().setIfMatch(createdRole.getEtag())));
            assertFalse(createdRole.getEtag().equals(replacedRole.getEtag()));
            HttpResponseException staleEtag = assertThrows(HttpResponseException.class,
                () -> execute(
                    () -> client.createOrReplaceRole(firstRoleName,
                        new ChatRole(Collections.singletonList(ChatPermission.USER_CREATE_ROOM)),
                        new MatchConditions().setIfMatch(createdRole.getEtag())),
                    () -> asyncClient.createOrReplaceRole(firstRoleName,
                        new ChatRole(Collections.singletonList(ChatPermission.USER_CREATE_ROOM)),
                        new MatchConditions().setIfMatch(createdRole.getEtag()))));
            assertEquals(412, staleEtag.getResponse().getStatusCode());

            execute(
                () -> client.createOrReplaceRole(secondRoleName,
                    new ChatRole(Collections.singletonList(ChatPermission.ROOM_PUBLISH_MESSAGE)),
                    new MatchConditions().setIfNoneMatch("*")),
                () -> asyncClient.createOrReplaceRole(secondRoleName,
                    new ChatRole(Collections.singletonList(ChatPermission.ROOM_PUBLISH_MESSAGE)),
                    new MatchConditions().setIfNoneMatch("*")));
            HttpResponseException alreadyExists = assertThrows(HttpResponseException.class,
                () -> execute(
                    () -> client.createOrReplaceRole(secondRoleName,
                        new ChatRole(Collections.singletonList(ChatPermission.ROOM_HISTORY)),
                        new MatchConditions().setIfNoneMatch("*")),
                    () -> asyncClient.createOrReplaceRole(secondRoleName,
                        new ChatRole(Collections.singletonList(ChatPermission.ROOM_HISTORY)),
                        new MatchConditions().setIfNoneMatch("*"))));
            assertEquals(412, alreadyExists.getResponse().getStatusCode());

            assertTrue(execute(() -> client.listRoles().stream().anyMatch(role -> firstRoleName.equals(role.getName())),
                () -> asyncClient.listRoles().any(role -> firstRoleName.equals(role.getName()))));
            List<PagedResponse<ChatRole>> pages
                = execute(() -> StreamSupport.stream(client.listRoles().iterableByPage(1).spliterator(), false)
                    .limit(2)
                    .collect(Collectors.toList()), () -> asyncClient.listRoles().byPage(1).take(2).collectList());
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
            cleanup(() -> execute(() -> client.deleteRole(firstRoleName), () -> asyncClient.deleteRole(firstRoleName)));
            cleanup(
                () -> execute(() -> client.deleteRole(secondRoleName), () -> asyncClient.deleteRole(secondRoleName)));
        }
    }

    @SyncAsyncTest
    public void canManageRoomsAndReadEmptyConversation() {
        String roomId = testResourceNamer.randomName("java-test-room-", 50);

        try {
            ChatRoom createdRoom = execute(() -> client.createOrReplaceRoom(roomId, new ChatRoom("Java test room")),
                () -> asyncClient.createOrReplaceRoom(roomId, new ChatRoom("Java test room")));
            assertEquals(roomId, createdRoom.getId());
            assertEquals("Java test room",
                execute(() -> client.getRoom(roomId), () -> asyncClient.getRoom(roomId)).getTitle());

            ChatConversation conversation = execute(() -> client.getConversation(createdRoom.getDefaultConversation()),
                () -> asyncClient.getConversation(createdRoom.getDefaultConversation()));
            assertEquals(createdRoom.getDefaultConversation(), conversation.getId());
            assertEquals(roomId, conversation.getParentRoom());
            assertFalse(execute(() -> client.listMessages(conversation.getId()).stream().findAny().isPresent(),
                () -> asyncClient.listMessages(conversation.getId()).hasElements()));
            assertFalse(execute(() -> client.listRoomMembers(roomId).stream().findAny().isPresent(),
                () -> asyncClient.listRoomMembers(roomId).hasElements()));
        } finally {
            cleanup(() -> execute(() -> client.deleteRoom(roomId), () -> asyncClient.deleteRoom(roomId)));
        }
    }

    @SyncAsyncTest
    public void canManageUsersAndRoomMembers() {
        String suffix = testResourceNamer.randomName("java", 20);
        String userId = "java-test-user-" + suffix;
        String roomId = "java-test-room-" + suffix;

        try {
            ChatUser createdUser = execute(
                () -> client.createOrReplaceUser(userId,
                    new HumanChatUser("Java test user", BuiltInChatRoles.USER_NORMAL)),
                () -> asyncClient.createOrReplaceUser(userId,
                    new HumanChatUser("Java test user", BuiltInChatRoles.USER_NORMAL)));
            assertEquals(userId, createdUser.getId());
            assertEquals(userId, execute(() -> client.getUser(userId), () -> asyncClient.getUser(userId)).getId());

            ChatRoom room = execute(() -> client.createOrReplaceRoom(roomId, new ChatRoom("Java member test room")),
                () -> asyncClient.createOrReplaceRoom(roomId, new ChatRoom("Java member test room")));
            assertEquals(roomId, room.getId());
            ChatRoomMember member = execute(
                () -> client.createOrReplaceRoomMember(roomId, userId,
                    new ChatRoomMember(BuiltInChatRoles.ROOM_MEMBER)),
                () -> asyncClient.createOrReplaceRoomMember(roomId, userId,
                    new ChatRoomMember(BuiltInChatRoles.ROOM_MEMBER)));
            assertEquals(userId, member.getUserId());
            assertEquals(BuiltInChatRoles.ROOM_MEMBER, member.getRoleName());
            assertTrue(
                execute(() -> client.listRoomMembers(roomId).stream().anyMatch(item -> userId.equals(item.getUserId())),
                    () -> asyncClient.listRoomMembers(roomId).any(item -> userId.equals(item.getUserId()))));
            execute(() -> client.deleteRoomMember(roomId, userId), () -> asyncClient.deleteRoomMember(roomId, userId));
        } finally {
            cleanup(() -> execute(() -> client.deleteRoomMember(roomId, userId),
                () -> asyncClient.deleteRoomMember(roomId, userId)));
            cleanup(() -> execute(() -> client.deleteRoom(roomId), () -> asyncClient.deleteRoom(roomId)));
            cleanup(() -> execute(() -> client.deleteUser(userId), () -> asyncClient.deleteUser(userId)));
        }
    }

    @SyncAsyncTest
    public void canListUpdateAndDeleteMessages() {
        String suffix = testResourceNamer.randomName("java", 20);
        String userId = "java-test-message-user-" + suffix;
        String roomId = "java-test-message-room-" + suffix;
        String messageText = "Java live test message " + suffix;
        String conversationId = null;
        String messageId = null;

        try {
            execute(
                () -> client.createOrReplaceUser(userId,
                    new HumanChatUser("Java message test user", BuiltInChatRoles.USER_NORMAL)),
                () -> asyncClient.createOrReplaceUser(userId,
                    new HumanChatUser("Java message test user", BuiltInChatRoles.USER_NORMAL)));
            ChatRoom room = execute(() -> client.createOrReplaceRoom(roomId, new ChatRoom("Java message test room")),
                () -> asyncClient.createOrReplaceRoom(roomId, new ChatRoom("Java message test room")));
            conversationId = room.getDefaultConversation();
            execute(
                () -> client.createOrReplaceRoomMember(roomId, userId,
                    new ChatRoomMember(BuiltInChatRoles.ROOM_MEMBER)),
                () -> asyncClient.createOrReplaceRoomMember(roomId, userId,
                    new ChatRoomMember(BuiltInChatRoles.ROOM_MEMBER)));

            WebPubSubClientAccessToken accessToken
                = execute(() -> entraClient.getClientAccessToken(new GetClientAccessTokenOptions().setUserId(userId)),
                    () -> entraAsyncClient.getClientAccessToken(new GetClientAccessTokenOptions().setUserId(userId)));
            assertNotNull(accessToken.getToken());
            assertNotNull(accessToken.getUrl());

            if (getTestMode() != TestMode.PLAYBACK) {
                ChatMessageSeeder.sendTextMessage(accessToken.getUrl(), conversationId, messageText);
            }
            String currentConversationId = conversationId;
            ChatMessage createdMessage = execute(
                () -> client.listMessages(currentConversationId)
                    .stream()
                    .filter(message -> userId.equals(message.getCreatedBy())
                        && messageText.equals(message.getContent().getText()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("The seeded Chat message was not found.")),
                () -> asyncClient.listMessages(currentConversationId)
                    .filter(message -> userId.equals(message.getCreatedBy())
                        && messageText.equals(message.getContent().getText()))
                    .next());
            assertNotNull(createdMessage);
            messageId = createdMessage.getId();
            String currentMessageId = messageId;

            ChatMessage updatedText = execute(
                () -> client.updateMessage(currentConversationId, currentMessageId,
                    new ChatMessage().setCreatedBy(userId)
                        .setContent(MessageContent.fromText(messageText + " updated"))),
                () -> asyncClient.updateMessage(currentConversationId, currentMessageId,
                    new ChatMessage().setCreatedBy(userId)
                        .setContent(MessageContent.fromText(messageText + " updated"))));
            assertEquals(messageText + " updated", updatedText.getContent().getText());

            byte[] binary = new byte[] { 0, 1, 2, (byte) 254, (byte) 255 };
            ChatMessage updatedBinary = execute(
                () -> client.updateMessage(currentConversationId, currentMessageId,
                    new ChatMessage().setCreatedBy(userId).setContent(MessageContent.fromBinary(binary))),
                () -> asyncClient.updateMessage(currentConversationId, currentMessageId,
                    new ChatMessage().setCreatedBy(userId).setContent(MessageContent.fromBinary(binary))));
            assertArrayEquals(binary, updatedBinary.getContent().getBinary());
            execute(() -> client.deleteMessage(currentConversationId, currentMessageId),
                () -> asyncClient.deleteMessage(currentConversationId, currentMessageId));
        } finally {
            String finalConversationId = conversationId;
            String finalMessageId = messageId;
            if (finalConversationId != null && finalMessageId != null) {
                cleanup(() -> execute(() -> client.deleteMessage(finalConversationId, finalMessageId),
                    () -> asyncClient.deleteMessage(finalConversationId, finalMessageId)));
            }
            cleanup(() -> execute(() -> client.deleteRoomMember(roomId, userId),
                () -> asyncClient.deleteRoomMember(roomId, userId)));
            cleanup(() -> execute(() -> client.deleteRoom(roomId), () -> asyncClient.deleteRoom(roomId)));
            cleanup(() -> execute(() -> client.deleteUser(userId), () -> asyncClient.deleteUser(userId)));
        }
    }
}
