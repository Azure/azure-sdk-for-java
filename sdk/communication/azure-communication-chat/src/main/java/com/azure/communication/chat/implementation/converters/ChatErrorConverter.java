// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.chat.models.ChatError;

import java.util.ArrayList;
import java.util.List;
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

        List<ChatError> details = new ArrayList<ChatError>();

        if (obj.getDetails() != null) {
            details = obj.getDetails()
                .stream()
                .map(detail -> convert(detail))
                .collect(Collectors.toList());
        }

        ChatError chatError = new ChatError(
            obj.getMessage(),
            obj.getCode(),
            obj.getTarget(),
            details,
            convert(obj.getInnerError())
        );

        return chatError;
    }

    private ChatErrorConverter() {
    }
}
