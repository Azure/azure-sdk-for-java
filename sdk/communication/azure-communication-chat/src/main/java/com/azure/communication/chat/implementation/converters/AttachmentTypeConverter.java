// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.chat.implementation.converters;

import java.util.Objects;

import com.azure.communication.chat.models.AttachmentType;

public final class AttachmentTypeConverter {
    public static AttachmentType convert(
            com.azure.communication.chat.implementation.models.AttachmentType attachmentType) {
        Objects.requireNonNull(attachmentType, "'attachmentType' cannot be null.");
        return AttachmentType.fromString(attachmentType.toString());
    }
}
