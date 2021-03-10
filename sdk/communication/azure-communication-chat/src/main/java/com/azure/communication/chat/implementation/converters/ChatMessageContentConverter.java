// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.chat.models.ChatMessageContent;

import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.communication.chat.implementation.models.ChatMessageContent} and
 * {@link ChatMessageContent}.
 */
public final class ChatMessageContentConverter {
    /**
     * Maps from {com.azure.communication.chat.implementation.models.ChatMessageContent} to {@link ChatMessageContent}.
     */
    public static ChatMessageContent convert(
        com.azure.communication.chat.implementation.models.ChatMessageContent obj) {
        if (obj == null) {
            return null;
        }

        ChatMessageContent chatMessageContent = new ChatMessageContent()
            .setMessage(obj.getMessage())
            .setTopic(obj.getTopic());

        if (obj.getInitiatorCommunicationIdentifier() != null) {
            chatMessageContent.setInitiatorCommunicationIdentifier(
                CommunicationIdentifierConverter.convert(obj.getInitiatorCommunicationIdentifier()));
        }

        if (obj.getParticipants() != null) {
            chatMessageContent.setParticipants(obj.getParticipants()
                .stream()
                .map(participant -> ChatParticipantConverter.convert(participant))
                .collect(Collectors.toList()));
        }

        return chatMessageContent;
    }

    private ChatMessageContentConverter() {
    }
}
