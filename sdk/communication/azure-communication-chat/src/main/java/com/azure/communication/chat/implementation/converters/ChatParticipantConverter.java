// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.chat.models.ChatParticipant;

/**
 * A converter between {@link com.azure.communication.chat.implementation.models.ChatParticipant} and
 * {@link ChatParticipant}.
 */
public final class ChatParticipantConverter {
    /**
     * Maps from {com.azure.communication.chat.implementation.models.ChatThreadMember} to {@link ChatParticipant}.
     */
    public static ChatParticipant convert(com.azure.communication.chat.implementation.models.ChatParticipant obj) {
        if (obj == null) {
            return null;
        }

        ChatParticipant chatParticipant = new ChatParticipant()
            .setCommunicationIdentifier(CommunicationIdentifierConverter.convert(obj.getCommunicationIdentifier()))
            .setDisplayName(obj.getDisplayName())
            .setShareHistoryTime(obj.getShareHistoryTime());

        return chatParticipant;
    }

    /**
     * Maps from {ChatParticipant} to {@link com.azure.communication.chat.implementation.models.ChatParticipant}.
     */
    public static com.azure.communication.chat.implementation.models.ChatParticipant convert(ChatParticipant obj) {
        if (obj == null) {
            return null;
        }

        com.azure.communication.chat.implementation.models.ChatParticipant chatParticipant
            = new com.azure.communication.chat.implementation.models.ChatParticipant()
                .setCommunicationIdentifier(CommunicationIdentifierConverter.convert(obj.getCommunicationIdentifier()))
                .setDisplayName(obj.getDisplayName())
                .setShareHistoryTime(obj.getShareHistoryTime());

        return chatParticipant;
    }

    private ChatParticipantConverter() {
    }
}
