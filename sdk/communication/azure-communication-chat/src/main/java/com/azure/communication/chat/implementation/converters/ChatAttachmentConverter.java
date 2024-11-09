// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.chat.implementation.converters;

import java.util.Objects;
import com.azure.communication.chat.models.ChatAttachment;

public final class ChatAttachmentConverter {
    public static ChatAttachment
        convert(com.azure.communication.chat.implementation.models.ChatAttachment chatAttachment) {
        Objects.requireNonNull(chatAttachment, "'chatAttachment' cannot be null.");
        ChatAttachment attachment = new ChatAttachment(chatAttachment.getId(),
            AttachmentTypeConverter.convert(chatAttachment.getAttachmentType())).setName(chatAttachment.getName())
                .setUrl(chatAttachment.getUrl())
                .setPreviewUrl(chatAttachment.getPreviewUrl());

        return attachment;
    }
}
