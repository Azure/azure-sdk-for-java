// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.chat.models.ChatParticipant;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.communication.chat.implementation.models.AddChatParticipantsOptions} and
 * a collection of {@link ChatParticipant}.
 */
public final class AddChatParticipantsOptionsConverter {
    /**
     * Maps from a collection of {@link ChatParticipant} to
     * {@link com.azure.communication.chat.implementation.models.AddChatParticipantsOptions}.
     *
     * @param participants A list of participants to add
     */
    public static com.azure.communication.chat.implementation.models.AddChatParticipantsOptions convert(
        Iterable<ChatParticipant> participants) {

        if (participants == null) {
            return null;
        }
        List<com.azure.communication.chat.implementation.models.ChatParticipant> targetParticipants = new ArrayList<>();

        participants.forEach(participant -> {
            targetParticipants.add(ChatParticipantConverter.convert(participant));
        });

        com.azure.communication.chat.implementation.models.AddChatParticipantsOptions addChatThreadMembersOptions
            = new com.azure.communication.chat.implementation.models.AddChatParticipantsOptions()
            .setParticipants(targetParticipants);

        return addChatThreadMembersOptions;
    }

    private AddChatParticipantsOptionsConverter() {
    }
}
