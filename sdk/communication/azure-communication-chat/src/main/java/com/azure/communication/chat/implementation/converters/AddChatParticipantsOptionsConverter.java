// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.chat.models.AddChatParticipantsOptions;

import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.communication.chat.implementation.models.AddChatParticipantsOptions} and
 * {@link AddChatParticipantsOptions}.
 */
public final class AddChatParticipantsOptionsConverter {
    /**
     * Maps from {AddChatThreadMembersOptions} to
     * {@link com.azure.communication.chat.implementation.models.AddChatParticipantsOptions}.
     */
    public static com.azure.communication.chat.implementation.models.AddChatParticipantsOptions convert(
        AddChatParticipantsOptions obj) {

        if (obj == null) {
            return null;
        }

        com.azure.communication.chat.implementation.models.AddChatParticipantsOptions addChatThreadMembersOptions
            = new com.azure.communication.chat.implementation.models.AddChatParticipantsOptions()
                .setParticipants(obj.getParticipants()
                    .stream()
                    .map(participant -> ChatParticipantConverter.convert(participant))
                    .collect(Collectors.toList()));

        return addChatThreadMembersOptions;
    }

    private AddChatParticipantsOptionsConverter() {
    }
}
