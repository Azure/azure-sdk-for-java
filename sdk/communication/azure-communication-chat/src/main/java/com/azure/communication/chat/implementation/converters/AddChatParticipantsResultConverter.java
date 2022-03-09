// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.chat.models.AddChatParticipantsResult;
import com.azure.communication.chat.models.ChatError;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.communication.chat.implementation.models.AddChatParticipantsResult} and
 * {@link AddChatParticipantsResult}.
 */
public final class AddChatParticipantsResultConverter {
    private AddChatParticipantsResultConverter() {
    }

    /**
     * Maps from {@link com.azure.communication.chat.implementation.models.AddChatParticipantsResult} to
     * {@link AddChatParticipantsResult}.
     */
    public static AddChatParticipantsResult convert(
        com.azure.communication.chat.implementation.models.AddChatParticipantsResult obj) {

        if (obj == null) {
            return null;
        }

        List<ChatError> invalidParticipants = new ArrayList<>();
        if (obj.getInvalidParticipants() != null) {
            invalidParticipants = obj.getInvalidParticipants()
                .stream()
                .map((error) -> ChatErrorConverter.convert(error))
                .collect(Collectors.toList());
        }

        AddChatParticipantsResult addChatParticipantsResult = new AddChatParticipantsResult(invalidParticipants);

        return addChatParticipantsResult;
    }
}
