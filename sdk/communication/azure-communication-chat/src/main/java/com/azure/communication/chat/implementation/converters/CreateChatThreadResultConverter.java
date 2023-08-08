// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.chat.models.ChatError;
import com.azure.communication.chat.models.CreateChatThreadResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.communication.chat.implementation.models.CreateChatThreadResult} and
 * {@link CreateChatThreadResult}.
 */
public final class CreateChatThreadResultConverter {
    private CreateChatThreadResultConverter() {
    }

    /**
     * Maps from {@link com.azure.communication.chat.implementation.models.CreateChatThreadResult} to
     * {@link CreateChatThreadResult}.
     */
    public static CreateChatThreadResult convert(
        com.azure.communication.chat.implementation.models.CreateChatThreadResult obj) {

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

        CreateChatThreadResult createChatThreadResult = new CreateChatThreadResult(
            ChatThreadPropertiesConverter.convert(obj.getChatThread()), invalidParticipants);

        return createChatThreadResult;
    }
}
