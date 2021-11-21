// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.chat.models.ChatMessage;

/**
 * A converter between {@link com.azure.communication.chat.implementation.models.ChatMessage} and
 * {@link ChatMessage}.
 */
public final class ChatMessageConverter {
    /**
     * Maps from {com.azure.communication.chat.implementation.models.ChatMessage} to {@link ChatMessage}.
     */
    public static ChatMessage convert(com.azure.communication.chat.implementation.models.ChatMessage obj) {
        if (obj == null) {
            return null;
        }

        ChatMessage chatMessage = new ChatMessage()
            .setId(obj.getId())
            .setType(obj.getType())
            .setVersion(obj.getVersion())
            .setContent(ChatMessageContentConverter.convert(obj.getContent()))
            .setCreatedOn(obj.getCreatedOn())
            .setDeletedOn(obj.getDeletedOn())
            .setEditedOn(obj.getEditedOn())
            .setSenderDisplayName(obj.getSenderDisplayName())
            .setMetadata(obj.getMetadata());

        if (obj.getSenderCommunicationIdentifier() != null) {
            chatMessage.setSender(
                CommunicationIdentifierConverter.convert(obj.getSenderCommunicationIdentifier()));
        }

        return chatMessage;
    }

    private ChatMessageConverter() {
    }
}
