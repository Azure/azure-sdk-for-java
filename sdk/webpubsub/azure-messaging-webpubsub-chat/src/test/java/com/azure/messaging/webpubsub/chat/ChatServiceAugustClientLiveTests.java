// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedResponse;
import com.azure.messaging.webpubsub.chat.models.BuiltInChatRoles;
import com.azure.messaging.webpubsub.chat.models.ChatRoom;
import com.azure.messaging.webpubsub.chat.models.ChatRoomMember;
import com.azure.messaging.webpubsub.chat.models.ChatTopic;
import com.azure.messaging.webpubsub.chat.models.ChatTopicState;
import com.azure.messaging.webpubsub.chat.models.ChatUser;
import com.azure.messaging.webpubsub.chat.models.ChatUserRoom;
import com.azure.messaging.webpubsub.chat.models.HumanChatUser;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChatServiceAugustClientLiveTests extends ChatServiceClientTestBase {
    @Test
    public void canManageTopicsWithConditionsAndPagination() {
        String roomId = testResourceNamer.randomName("java-topic-room-", 50);
        String firstTopicId = "first";
        String secondTopicId = "second";

        try {
            client.createOrReplaceRoom(roomId, new ChatRoom("Java topic test room"));
            ChatTopic created = client.createOrReplaceTopic(roomId, firstTopicId,
                new ChatTopic().setTitle("First topic"), new MatchConditions().setIfNoneMatch("*"));
            assertEquals(firstTopicId, created.getId());
            assertEquals(ChatTopicState.NORMAL, created.getState());
            assertNotNull(created.getCreatedAt());
            assertNotNull(created.getCreatedBy());
            assertNotNull(created.getConversationId());
            assertNotNull(created.getEtag());
            assertEquals("First topic", entraClient.getTopic(roomId, firstTopicId).getTitle());

            HttpResponseException duplicate
                = assertThrows(HttpResponseException.class, () -> client.createOrReplaceTopic(roomId, firstTopicId,
                    new ChatTopic().setTitle("Duplicate"), new MatchConditions().setIfNoneMatch("*")));
            assertEquals(412, duplicate.getResponse().getStatusCode());

            ChatTopic replaced = client.createOrReplaceTopic(roomId, firstTopicId,
                new ChatTopic().setTitle("Replaced topic"), new MatchConditions().setIfMatch(created.getEtag()));
            ChatTopic updated = client.updateTopic(roomId, firstTopicId,
                new ChatTopic().setTitle("Updated topic").setState(ChatTopicState.ARCHIVED),
                new MatchConditions().setIfMatch(replaced.getEtag()));
            assertEquals("Updated topic", updated.getTitle());
            assertEquals(ChatTopicState.ARCHIVED, updated.getState());
            assertEquals(created.getConversationId(), updated.getConversationId());
            HttpResponseException stale
                = assertThrows(HttpResponseException.class, () -> client.updateTopic(roomId, firstTopicId,
                    new ChatTopic().setTitle("Stale update"), new MatchConditions().setIfMatch(replaced.getEtag())));
            assertEquals(412, stale.getResponse().getStatusCode());

            client.createOrReplaceTopic(roomId, secondTopicId, new ChatTopic().setTitle("Second topic"));
            Iterator<PagedResponse<ChatTopic>> pages = client.listTopics(roomId).iterableByPage(1).iterator();
            PagedResponse<ChatTopic> firstPage = pages.next();
            assertEquals(1, firstPage.getValue().size());
            assertNotNull(firstPage.getContinuationToken());
            PagedResponse<ChatTopic> secondPage = pages.next();
            assertEquals(1, secondPage.getValue().size());
            assertNotEquals(firstPage.getValue().get(0).getId(), secondPage.getValue().get(0).getId());
            List<String> topicIds
                = client.listTopics(roomId).stream().map(ChatTopic::getId).collect(Collectors.toList());
            assertTrue(topicIds.contains(firstTopicId));
            assertTrue(topicIds.contains(secondTopicId));

            client.deleteTopic(roomId, firstTopicId, new MatchConditions().setIfMatch(updated.getEtag()));
            client.deleteTopic(roomId, secondTopicId);
        } finally {
            cleanup(() -> client.deleteRoom(roomId));
        }
    }

    @Test
    public void canListRoomsAndUsersWithPagination() {
        String suffix = testResourceNamer.randomName("java", 20);
        String firstRoomId = "java-list-room-a-" + suffix;
        String secondRoomId = "java-list-room-b-" + suffix;
        String firstUserId = "java-list-user-a-" + suffix;
        String secondUserId = "java-list-user-b-" + suffix;

        try {
            client.createOrReplaceRoom(firstRoomId, new ChatRoom("First room"));
            client.createOrReplaceRoom(secondRoomId, new ChatRoom("Second room"));
            client.createOrReplaceUser(firstUserId, new HumanChatUser("First user", BuiltInChatRoles.USER_NORMAL));
            client.createOrReplaceUser(secondUserId, new HumanChatUser("Second user", BuiltInChatRoles.USER_NORMAL));

            List<String> roomIds = StreamSupport.stream(client.listRooms().iterableByPage(1).spliterator(), false)
                .flatMap(page -> page.getValue().stream())
                .map(ChatRoom::getId)
                .collect(Collectors.toList());
            assertTrue(roomIds.contains(firstRoomId));
            assertTrue(roomIds.contains(secondRoomId));
            List<ChatUser> users = StreamSupport.stream(entraClient.listUsers().iterableByPage(1).spliterator(), false)
                .flatMap(page -> page.getValue().stream())
                .collect(Collectors.toList());
            assertTrue(
                users.stream().anyMatch(user -> firstUserId.equals(user.getId()) && user instanceof HumanChatUser));
            assertTrue(users.stream().anyMatch(user -> secondUserId.equals(user.getId())));
        } finally {
            cleanup(() -> client.deleteRoom(firstRoomId));
            cleanup(() -> client.deleteRoom(secondRoomId));
            cleanup(() -> client.deleteUser(firstUserId));
            cleanup(() -> client.deleteUser(secondUserId));
        }
    }

    @Test
    public void canListRoomsForUserWithPagination() {
        String suffix = testResourceNamer.randomName("java", 20);
        String userId = "java-user-rooms-" + suffix;
        String firstRoomId = "java-user-room-a-" + suffix;
        String secondRoomId = "java-user-room-b-" + suffix;

        try {
            client.createOrReplaceUser(userId, new HumanChatUser("Room list user", BuiltInChatRoles.USER_NORMAL));
            client.createOrReplaceRoom(firstRoomId, new ChatRoom("First user room"));
            client.createOrReplaceRoom(secondRoomId, new ChatRoom("Second user room"));
            assertEquals(0, client.listRoomsForUser(userId).stream().count());
            client.createOrReplaceRoomMember(firstRoomId, userId, new ChatRoomMember(BuiltInChatRoles.ROOM_MEMBER));
            client.createOrReplaceRoomMember(secondRoomId, userId, new ChatRoomMember(BuiltInChatRoles.ROOM_MEMBER));

            Iterator<PagedResponse<ChatUserRoom>> pages = client.listRoomsForUser(userId).iterableByPage(1).iterator();
            PagedResponse<ChatUserRoom> firstPage = pages.next();
            assertEquals(1, firstPage.getValue().size());
            assertNotNull(firstPage.getContinuationToken());
            PagedResponse<ChatUserRoom> secondPage = pages.next();
            assertEquals(1, secondPage.getValue().size());
            assertNotEquals(firstPage.getValue().get(0).getId(), secondPage.getValue().get(0).getId());
            List<ChatUserRoom> rooms = client.listRoomsForUser(userId).stream().collect(Collectors.toList());
            assertEquals(2, rooms.size());
            assertTrue(rooms.stream()
                .anyMatch(room -> firstRoomId.equals(room.getId())
                    && "First user room".equals(room.getTitle())
                    && room.getDefaultConversation() != null));
            assertTrue(rooms.stream().anyMatch(room -> secondRoomId.equals(room.getId())));
        } finally {
            cleanup(() -> client.deleteRoom(firstRoomId));
            cleanup(() -> client.deleteRoom(secondRoomId));
            cleanup(() -> client.deleteUser(userId));
        }
    }
}
