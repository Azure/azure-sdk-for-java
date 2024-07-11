// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Defines values for ChatAttachmentType.
 */
public final class ChatAttachmentType extends ExpandableStringEnum<ChatAttachmentType> {
    /** Static value image for ChatAttachmentType. */
    public static final ChatAttachmentType IMAGE = fromString("image");

    /** Static value file for ChatAttachmentType. */
    public static final ChatAttachmentType FILE = fromString("file");

    /**
     * Creates or finds a ChatAttachmentType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ChatAttachmentType.
     */
    public static ChatAttachmentType fromString(String name) {
        return fromString(name, ChatAttachmentType.class);
    }

    /** @return known ChatAttachmentType values. */
    public static Collection<ChatAttachmentType> values() {
        return values(ChatAttachmentType.class);
    }
}
