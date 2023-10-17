// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.chat.implementation.converters;

import com.azure.communication.chat.models.AttachmentType;

public final class AttachmentTypeConverter {
    public static AttachmentType convert(com.azure.communication.chat.implementation.models.AttachmentType obj) {
        if (obj == null) {
            return null;
        }

        new AttachmentType();
        AttachmentType attachmentType = AttachmentType.fromString(obj.toString());
        return attachmentType;
    }
}
