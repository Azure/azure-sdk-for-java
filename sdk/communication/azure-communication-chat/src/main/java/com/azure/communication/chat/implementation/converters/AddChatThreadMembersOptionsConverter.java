// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.implementation.converters;

import com.azure.communication.chat.models.AddChatThreadMembersOptions;

import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.communication.chat.implementation.models.AddChatThreadMembersOptions} and
 * {@link AddChatThreadMembersOptions}.
 */
public final class AddChatThreadMembersOptionsConverter {
    /**
     * Maps from {AddChatThreadMembersOptions} to
     * {@link com.azure.communication.chat.implementation.models.AddChatThreadMembersOptions}.
     */
    public static com.azure.communication.chat.implementation.models.AddChatThreadMembersOptions convert(
        AddChatThreadMembersOptions obj) {

        if (obj == null) {
            return null;
        }

        com.azure.communication.chat.implementation.models.AddChatThreadMembersOptions addChatThreadMembersOptions
            = new com.azure.communication.chat.implementation.models.AddChatThreadMembersOptions()
                .setMembers(obj.getMembers()
                    .stream()
                    .map(member -> ChatThreadMemberConverter.convert(member))
                    .collect(Collectors.toList()));

        return addChatThreadMembersOptions;
    }

    private AddChatThreadMembersOptionsConverter() {
    }
}
