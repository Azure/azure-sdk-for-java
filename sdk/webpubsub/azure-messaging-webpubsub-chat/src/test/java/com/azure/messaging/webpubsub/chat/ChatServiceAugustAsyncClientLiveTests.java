// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.chat.models.BuiltInChatRoles;
import com.azure.messaging.webpubsub.chat.models.ChatRoom;
import com.azure.messaging.webpubsub.chat.models.ChatRoomMember;
import com.azure.messaging.webpubsub.chat.models.ChatTopic;
import com.azure.messaging.webpubsub.chat.models.ChatTopicState;
import com.azure.messaging.webpubsub.chat.models.ChatUser;
import com.azure.messaging.webpubsub.chat.models.ChatUserRoom;
import com.azure.messaging.webpubsub.chat.models.HumanChatUser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChatServiceAugustAsyncClientLiveTests extends ChatServiceClientTestBase {
    @Test
    public void canManageTopicsWithConditionsAndPagination() {
        String roomId = testResourceNamer.randomName("java-async-topic-room-", 55);
        String firstTopicId = "first";
        String secondTopicId = "second";

        try {
            asyncClient.createOrReplaceRoom(roomId, new ChatRoom("Java async topic test room")).block();
            ChatTopic created
                = asyncClient
                    .createOrReplaceTopic(roomId, firstTopicId, new ChatTopic().setTitle("First topic"),
                        new MatchConditions().setIfNoneMatch("*"))
                    .block();
            assertEquals(firstTopicId, created.getId());
            assertEquals(ChatTopicState.NORMAL, created.getState());
            assertNotNull(created.getCreatedAt());
            assertNotNull(created.getCreatedBy());
            assertNotNull(created.getConversationId());
            assertNotNull(created.getEtag());
            assertEquals("First topic", entraAsyncClient.getTopic(roomId, firstTopicId).block().getTitle());

            HttpResponseException duplicate = assertThrows(HttpResponseException.class,
                () -> asyncClient
                    .createOrReplaceTopic(roomId, firstTopicId, new ChatTopic().setTitle("Duplicate"),
                        new MatchConditions().setIfNoneMatch("*"))
                    .block());
            assertEquals(412, duplicate.getResponse().getStatusCode());
            ChatTopic replaced
                = asyncClient
                    .createOrReplaceTopic(roomId, firstTopicId, new ChatTopic().setTitle("Replaced topic"),
                        new MatchConditions().setIfMatch(created.getEtag()))
                    .block();
            ChatTopic updated
                = asyncClient
                    .updateTopic(roomId, firstTopicId,
                        new ChatTopic().setTitle("Updated topic").setState(ChatTopicState.ARCHIVED),
                        new MatchConditions().setIfMatch(replaced.getEtag()))
                    .block();
            assertEquals("Updated topic", updated.getTitle());
            assertEquals(ChatTopicState.ARCHIVED, updated.getState());
            assertEquals(created.getConversationId(), updated.getConversationId());
            HttpResponseException stale = assertThrows(HttpResponseException.class,
                () -> asyncClient
                    .updateTopic(roomId, firstTopicId, new ChatTopic().setTitle("Stale update"),
                        new MatchConditions().setIfMatch(replaced.getEtag()))
                    .block());
            assertEquals(412, stale.getResponse().getStatusCode());

            asyncClient.createOrReplaceTopic(roomId, secondTopicId, new ChatTopic().setTitle("Second topic")).block();
            // Protocol paging preserves page size until microsoft/typespec#12149 is fixed.
            List<PagedResponse<BinaryData>> pages
                = asyncClient.listTopics(roomId, new RequestOptions()).byPage(1).take(2).collectList().block();
            assertEquals(2, pages.size());
            assertEquals(1, pages.get(0).getValue().size());
            assertNotNull(pages.get(0).getContinuationToken());
            assertEquals(1, pages.get(1).getValue().size());
            assertNotEquals(pages.get(0).getValue().get(0).toObject(ChatTopic.class).getId(),
                pages.get(1).getValue().get(0).toObject(ChatTopic.class).getId());
            List<String> topicIds = asyncClient.listTopics(roomId).map(ChatTopic::getId).collectList().block();
            assertTrue(topicIds.contains(firstTopicId));
            assertTrue(topicIds.contains(secondTopicId));

            asyncClient.deleteTopic(roomId, firstTopicId, new MatchConditions().setIfMatch(updated.getEtag())).block();
            asyncClient.deleteTopic(roomId, secondTopicId).block();
        } finally {
            cleanup(() -> asyncClient.deleteRoom(roomId).block());
        }
    }

    @Test
    public void canListRoomsAndUsersWithPagination() {
        String suffix = testResourceNamer.randomName("java", 20);
        String firstRoomId = "java-async-list-room-a-" + suffix;
        String secondRoomId = "java-async-list-room-b-" + suffix;
        String firstUserId = "java-async-list-user-a-" + suffix;
        String secondUserId = "java-async-list-user-b-" + suffix;

        try {
            asyncClient.createOrReplaceRoom(firstRoomId, new ChatRoom("First room")).block();
            asyncClient.createOrReplaceRoom(secondRoomId, new ChatRoom("Second room")).block();
            asyncClient.createOrReplaceUser(firstUserId, new HumanChatUser("First user", BuiltInChatRoles.USER_NORMAL))
                .block();
            asyncClient
                .createOrReplaceUser(secondUserId, new HumanChatUser("Second user", BuiltInChatRoles.USER_NORMAL))
                .block();

            List<String> roomIds = asyncClient.listRooms(new RequestOptions())
                .byPage(1)
                .flatMapIterable(PagedResponse::getValue)
                .map(data -> data.toObject(ChatRoom.class).getId())
                .collectList()
                .block();
            assertTrue(roomIds.contains(firstRoomId));
            assertTrue(roomIds.contains(secondRoomId));
            List<ChatUser> users = entraAsyncClient.listUsers(new RequestOptions())
                .byPage(1)
                .flatMapIterable(PagedResponse::getValue)
                .map(data -> data.toObject(ChatUser.class))
                .collectList()
                .block();
            assertTrue(
                users.stream().anyMatch(user -> firstUserId.equals(user.getId()) && user instanceof HumanChatUser));
            assertTrue(users.stream().anyMatch(user -> secondUserId.equals(user.getId())));
        } finally {
            cleanup(() -> asyncClient.deleteRoom(firstRoomId).block());
            cleanup(() -> asyncClient.deleteRoom(secondRoomId).block());
            cleanup(() -> asyncClient.deleteUser(firstUserId).block());
            cleanup(() -> asyncClient.deleteUser(secondUserId).block());
        }
    }

    @Test
    public void canListRoomsForUserWithPagination() {
        String suffix = testResourceNamer.randomName("java", 20);
        String userId = "java-async-user-rooms-" + suffix;
        String firstRoomId = "java-async-user-room-a-" + suffix;
        String secondRoomId = "java-async-user-room-b-" + suffix;

        try {
            asyncClient.createOrReplaceUser(userId, new HumanChatUser("Room list user", BuiltInChatRoles.USER_NORMAL))
                .block();
            asyncClient.createOrReplaceRoom(firstRoomId, new ChatRoom("First user room")).block();
            asyncClient.createOrReplaceRoom(secondRoomId, new ChatRoom("Second user room")).block();
            assertEquals(0, asyncClient.listRoomsForUser(userId).count().block());
            asyncClient.createOrReplaceRoomMember(firstRoomId, userId, new ChatRoomMember(BuiltInChatRoles.ROOM_MEMBER))
                .block();
            asyncClient
                .createOrReplaceRoomMember(secondRoomId, userId, new ChatRoomMember(BuiltInChatRoles.ROOM_MEMBER))
                .block();

            List<PagedResponse<BinaryData>> pages
                = asyncClient.listRoomsForUser(userId, new RequestOptions()).byPage(1).collectList().block();
            assertEquals(2, pages.size());
            assertEquals(1, pages.get(0).getValue().size());
            assertNotNull(pages.get(0).getContinuationToken());
            assertEquals(1, pages.get(1).getValue().size());
            assertNotEquals(pages.get(0).getValue().get(0).toObject(ChatUserRoom.class).getId(),
                pages.get(1).getValue().get(0).toObject(ChatUserRoom.class).getId());
            List<ChatUserRoom> rooms = asyncClient.listRoomsForUser(userId).collectList().block();
            assertEquals(2, rooms.size());
            assertTrue(rooms.stream()
                .anyMatch(room -> firstRoomId.equals(room.getId())
                    && "First user room".equals(room.getTitle())
                    && room.getDefaultConversation() != null));
            assertTrue(rooms.stream().anyMatch(room -> secondRoomId.equals(room.getId())));
        } finally {
            cleanup(() -> asyncClient.deleteRoom(firstRoomId).block());
            cleanup(() -> asyncClient.deleteRoom(secondRoomId).block());
            cleanup(() -> asyncClient.deleteUser(userId).block());
        }
    }
}
