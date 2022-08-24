// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for PlaySourceType. */
public final class PlaySourceType extends ExpandableStringEnum<PlaySourceType> {
    /** Static value file for PlaySourceType. */
    public static final PlaySourceType FILE = fromString("file");

    /**
     * Creates or finds a PlaySourceType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding PlaySourceType.
     */
    public static PlaySourceType fromString(String name) {
        return fromString(name, PlaySourceType.class);
    }

    /** @return known PlaySourceType values. */
    public static Collection<PlaySourceType> values() {
        return values(PlaySourceType.class);
    }
}
