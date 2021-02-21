// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.chat.models.ChatMessageReadReceipt;

/**
 * A converter between {@link com.azure.communication.chat.implementation.models.ChatMessageReadReceipt} and
 * {@link ChatMessageReadReceipt}.
 */
public final class ChatMessageReadReceiptConverter {
    /**
     * Maps from {com.azure.communication.chat.implementation.models.ReadReceipt} to {@link ChatMessageReadReceipt}.
     */
    public static ChatMessageReadReceipt convert(
        com.azure.communication.chat.implementation.models.ChatMessageReadReceipt obj) {
        if (obj == null) {
            return null;
        }

        ChatMessageReadReceipt readReceipt = new ChatMessageReadReceipt()
            .setSender(new CommunicationUserIdentifier(obj.getSenderId()))
            .setChatMessageId(obj.getChatMessageId())
            .setReadOn(obj.getReadOn());

        return readReceipt;
    }

    private ChatMessageReadReceiptConverter() {
    }
}
