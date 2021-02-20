// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.chat.models.ChatThread;

/**
 * A converter between {@link com.azure.communication.chat.implementation.models.ChatThread} and
 * {@link ChatThread}.
 */
public final class ChatThreadConverter {
    /**
     * Maps from {com.azure.communication.chat.implementation.models.ChatThread} to {@link ChatThread}.
     */
    public static ChatThread convert(com.azure.communication.chat.implementation.models.ChatThread obj) {
        if (obj == null) {
            return null;
        }

        ChatThread chatThread = new ChatThread()
            .setId(obj.getId())
            .setTopic(obj.getTopic())
            .setCreatedOn(obj.getCreatedOn());

        if (obj.getCreatedBy() != null && !obj.getCreatedBy().isEmpty()) {
            chatThread.setCreatedBy(new CommunicationUserIdentifier(obj.getCreatedBy()));
        }

        return chatThread;
    }

    private ChatThreadConverter() {
    }
}
