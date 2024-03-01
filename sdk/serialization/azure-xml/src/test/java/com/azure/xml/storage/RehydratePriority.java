// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.storage;

import java.util.Collection;

/** Defines values for RehydratePriority. */
public final class RehydratePriority extends ExpandableStringEnum<RehydratePriority> {
    /** Static value High for RehydratePriority. */
    public static final RehydratePriority HIGH = fromString("High");

    /** Static value Standard for RehydratePriority. */
    public static final RehydratePriority STANDARD = fromString("Standard");

    /**
     * Creates or finds a RehydratePriority from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding RehydratePriority.
     */
    public static RehydratePriority fromString(String name) {
        return fromString(name, RehydratePriority.class);
    }

    /**
     * Gets known RehydratePriority values.
     *
     * @return known RehydratePriority values.
     */
    public static Collection<RehydratePriority> values() {
        return values(RehydratePriority.class);
    }
}
