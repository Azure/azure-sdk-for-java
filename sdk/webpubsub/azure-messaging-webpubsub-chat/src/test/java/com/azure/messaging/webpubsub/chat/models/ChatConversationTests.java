// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChatConversationTests {
    @Test
    public void roomIdRetainsParentRoomWireName() {
        ChatConversation conversation
            = BinaryData.fromString("{\"id\":\"c.room.default\",\"parentRoom\":\"room\",\"etag\":\"etag\"}")
                .toObject(ChatConversation.class);

        assertEquals("c.room.default", conversation.getId());
        assertEquals("room", conversation.getRoomId());
        assertEquals("etag", conversation.getEtag());
        assertEquals("{\"parentRoom\":\"room\"}", BinaryData.fromObject(conversation).toString());
    }
}
