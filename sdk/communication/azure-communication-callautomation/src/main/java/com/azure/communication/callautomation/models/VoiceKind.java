// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for VoiceKind. */
public final class VoiceKind extends ExpandableStringEnum<VoiceKind> {
    /** Static value male for VoiceKind. */
    public static final VoiceKind MALE = fromString("male");

    /** Static value female for VoiceKind. */
    public static final VoiceKind FEMALE = fromString("female");

    /**
     * Creates or finds a VoiceKind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding VoiceKind.
     */
    public static VoiceKind fromString(String name) {
        return fromString(name, VoiceKind.class);
    }

    /**
     * Gets known VoiceKind values.
     *
     * @return known VoiceKind values.
     */
    public static Collection<VoiceKind> values() {
        return values(VoiceKind.class);
    }
}
