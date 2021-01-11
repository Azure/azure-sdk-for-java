// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.chat.models.ChatThreadMember;

/**
 * A converter between {@link com.azure.communication.chat.implementation.models.ChatThreadMember} and
 * {@link ChatThreadMember}.
 */
public final class ChatThreadMemberConverter {
    /**
     * Maps from {com.azure.communication.chat.implementation.models.ChatThreadMember} to {@link ChatThreadMember}.
     */
    public static ChatThreadMember convert(com.azure.communication.chat.implementation.models.ChatThreadMember obj) {
        if (obj == null) {
            return null;
        }

        ChatThreadMember chatThreadMember = new ChatThreadMember()
            .setUser(new CommunicationUserIdentifier(obj.getId()))
            .setDisplayName(obj.getDisplayName())
            .setShareHistoryTime(obj.getShareHistoryTime());

        return chatThreadMember;
    }

    /**
     * Maps from {ChatThreadMember} to {@link com.azure.communication.chat.implementation.models.ChatThreadMember}.
     */
    public static com.azure.communication.chat.implementation.models.ChatThreadMember convert(ChatThreadMember obj) {
        if (obj == null) {
            return null;
        }

        com.azure.communication.chat.implementation.models.ChatThreadMember chatThreadMember
            = new com.azure.communication.chat.implementation.models.ChatThreadMember()
                .setId(obj.getUser().getId())
                .setDisplayName(obj.getDisplayName())
                .setShareHistoryTime(obj.getShareHistoryTime());

        return chatThreadMember;
    }

    private ChatThreadMemberConverter() {
    }
}
