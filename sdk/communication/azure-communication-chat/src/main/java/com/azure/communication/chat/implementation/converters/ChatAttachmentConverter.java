// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.chat.implementation.converters;

import com.azure.communication.chat.models.ChatAttachment;

public final class ChatAttachmentConverter {
    public static ChatAttachment convert(
            com.azure.communication.chat.implementation.models.ChatAttachment obj) {
        if (obj == null) {
            return null;
        }

        ChatAttachment chatAttachment = new ChatAttachment()
                .setId(obj.getId())
                .setAttachmentType(AttachmentTypeConverter.convert(obj.getAttachmentType()))
                .setExtension(obj.getExtension())
                .setName(obj.getName())
                .setUrl(obj.getUrl())
                .setPreviewUrl(obj.getPreviewUrl());

        return chatAttachment;
    }
}
