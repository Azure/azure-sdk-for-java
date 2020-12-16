// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.common.CommunicationUserIdentifier;
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
            .setPriority(obj.getPriority())
            .setVersion(obj.getVersion())
            .setContent(obj.getContent())
            .setCreatedOn(obj.getCreatedOn())
            .setSender(new CommunicationUserIdentifier(obj.getSenderId()))
            .setDeletedOn(obj.getDeletedOn())
            .setEditedOn(obj.getEditedOn())
            .setSenderDisplayName(obj.getSenderDisplayName());

        return chatMessage;
    }

    private ChatMessageConverter() {
    }
}
