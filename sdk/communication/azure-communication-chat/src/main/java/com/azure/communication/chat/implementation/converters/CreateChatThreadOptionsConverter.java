// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.chat.models.CreateChatThreadOptions;

import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.communication.chat.implementation.models.CreateChatThreadOptions} and
 * {@link CreateChatThreadOptions}.
 */
public final class CreateChatThreadOptionsConverter {
    /**
     * Maps from {CreateChatThreadOptions} to
     * {@link com.azure.communication.chat.implementation.models.CreateChatThreadOptions}.
     */
    public static com.azure.communication.chat.implementation.models.CreateChatThreadOptions convert(
        CreateChatThreadOptions obj) {

        if (obj == null) {
            return null;
        }

        com.azure.communication.chat.implementation.models.CreateChatThreadOptions createChatThreadOptions
            = new com.azure.communication.chat.implementation.models.CreateChatThreadOptions()
                .setTopic(obj.getTopic())
                .setParticipants(obj.getParticipants()
                    .stream()
                    .map(member -> ChatParticipantConverter.convert(member))
                    .collect(Collectors.toList()));

        return createChatThreadOptions;
    }

    private CreateChatThreadOptionsConverter() {
    }
}
