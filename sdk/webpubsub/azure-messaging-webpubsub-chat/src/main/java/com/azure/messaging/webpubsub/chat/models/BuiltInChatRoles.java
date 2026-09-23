// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat.models;

/** Built-in roles supported by Azure Web PubSub Chat. */
public final class BuiltInChatRoles {
    /** The normal user role. */
    public static final String USER_NORMAL = "user.normal";

    /** The room member role. */
    public static final String ROOM_MEMBER = "room.member";

    /** The room operator role. */
    public static final String ROOM_OPERATOR = "room.operator";

    private BuiltInChatRoles() {
    }
}
