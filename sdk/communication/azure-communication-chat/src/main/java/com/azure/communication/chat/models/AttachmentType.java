// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for AttachmentType. */
public final class AttachmentType extends ExpandableStringEnum<AttachmentType> {
    /** Static value image for AttachmentType. */
    public static final AttachmentType IMAGE = fromString("image");

    /**
     * Creates or finds a AttachmentType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding AttachmentType.
     */
    @JsonCreator
    public static AttachmentType fromString(String name) {
        return fromString(name, AttachmentType.class);
    }

    /** @return known AttachmentType values. */
    public static Collection<AttachmentType> values() {
        return values(AttachmentType.class);
    }
}
