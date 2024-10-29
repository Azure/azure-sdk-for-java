// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.chat.models.ChatAttachment;
import com.azure.communication.chat.models.ChatMessageContent;
import com.azure.communication.chat.models.ChatParticipant;
import com.azure.communication.common.CommunicationIdentifier;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A converter between
 * {@link com.azure.communication.chat.implementation.models.ChatMessageContent}
 * and
 * {@link ChatMessageContent}.
 */
public final class ChatMessageContentConverter {
    /**
     * Maps from
     * {com.azure.communication.chat.implementation.models.ChatMessageContent} to
     * {@link ChatMessageContent}.
     */
    public static ChatMessageContent
        convert(com.azure.communication.chat.implementation.models.ChatMessageContent chatMessageContent) {
        Objects.requireNonNull(chatMessageContent, "'chatMessageContent' cannot be null.");

        Iterable<ChatParticipant> participants = new ArrayList<ChatParticipant>();
        Iterable<ChatAttachment> attachments = new ArrayList<ChatAttachment>();
        CommunicationIdentifier initiator = null;

        if (chatMessageContent.getInitiatorCommunicationIdentifier() != null) {
            initiator
                = CommunicationIdentifierConverter.convert(chatMessageContent.getInitiatorCommunicationIdentifier());
        }

        if (chatMessageContent.getParticipants() != null) {
            participants = chatMessageContent.getParticipants()
                .stream()
                .map(participant -> ChatParticipantConverter.convert(participant))
                .collect(Collectors.toList());
        }

        if (chatMessageContent.getAttachments() != null) {
            attachments = chatMessageContent.getAttachments()
                .stream()
                .map(attachment -> ChatAttachmentConverter.convert(attachment))
                .collect(Collectors.toList());
        }

        ChatMessageContent content = new ChatMessageContent(chatMessageContent.getMessage(),
            chatMessageContent.getTopic(), participants, initiator).setAttachments(attachments);

        return content;
    }

    private ChatMessageContentConverter() {
    }
}
