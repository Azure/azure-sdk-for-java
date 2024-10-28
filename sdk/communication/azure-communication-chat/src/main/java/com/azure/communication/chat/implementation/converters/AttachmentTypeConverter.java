// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.chat.implementation.converters;

import java.util.Objects;

import com.azure.communication.chat.models.ChatAttachmentType;

public final class AttachmentTypeConverter {
    public static ChatAttachmentType
        convert(com.azure.communication.chat.implementation.models.ChatAttachmentType chatAttachmentType) {
        Objects.requireNonNull(chatAttachmentType, "'chatAttachmentType' cannot be null.");
        return ChatAttachmentType.fromString(chatAttachmentType.toString());
    }
}
