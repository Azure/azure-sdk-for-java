// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.chat.models.ChatError;

import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.communication.chat.implementation.models.CommunicationError} and
 * {@link ChatError}.
 */
public final class ChatErrorConverter {
    /**
     * Maps from {com.azure.communication.chat.implementation.models.CommunicationError} to {@link ChatError}.
     */
    public static ChatError convert(com.azure.communication.chat.implementation.models.CommunicationError obj) {
        if (obj == null) {
            return null;
        }

        ChatError chatError = new ChatError()
            .setInnerError(convert(obj.getInnerError()))
            .setCode(obj.getCode())
            .setMessage(obj.getMessage())
            .setTarget(obj.getTarget());

        if (obj.getDetails() != null) {
            chatError.setDetails(obj.getDetails()
                .stream()
                .map(detail -> convert(detail))
                .collect(Collectors.toList()));
        }

        return chatError;
    }

    private ChatErrorConverter() {
    }
}
