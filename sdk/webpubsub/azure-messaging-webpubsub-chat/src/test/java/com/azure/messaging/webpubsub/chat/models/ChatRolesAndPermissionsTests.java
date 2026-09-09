// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChatRolesAndPermissionsTests {
    @Test
    public void chatRolesHaveExpectedValues() {
        assertEquals("user.normal", BuiltInChatRoles.USER_NORMAL);
        assertEquals("room.member", BuiltInChatRoles.ROOM_MEMBER);
        assertEquals("room.operator", BuiltInChatRoles.ROOM_OPERATOR);
    }

    @Test
    public void userPermissionsHaveExpectedValues() {
        assertEquals("user.create_room", ChatPermission.USER_CREATE_ROOM.toString());
        assertEquals("user.fetch_all_rooms", ChatPermission.USER_FETCH_ALL_ROOMS.toString());
    }

    @Test
    public void roomPermissionsHaveExpectedValues() {
        assertEquals("room.invite", ChatPermission.ROOM_INVITE.toString());
        assertEquals("room.remove_user", ChatPermission.ROOM_REMOVE_USER.toString());
        assertEquals("room.history", ChatPermission.ROOM_HISTORY.toString());
        assertEquals("room.publish_message", ChatPermission.ROOM_PUBLISH_MESSAGE.toString());
    }
}
