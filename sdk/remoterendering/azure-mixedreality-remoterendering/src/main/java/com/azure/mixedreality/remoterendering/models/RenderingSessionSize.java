// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** The size of a rendering session. */
public final class RenderingSessionSize extends ExpandableStringEnum<RenderingSessionSize> {
    /** Static value Standard for SessionSize. */
    public static final RenderingSessionSize STANDARD = fromString("Standard");

    /** Static value Premium for SessionSize. */
    public static final RenderingSessionSize PREMIUM = fromString("Premium");

    /**
     * Creates or finds a SessionSize from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding SessionSize.
     */
    public static RenderingSessionSize fromString(String name) {
        return fromString(name, RenderingSessionSize.class);
    }

    /** @return known SessionSize values. */
    public static Collection<RenderingSessionSize> values() {
        return values(RenderingSessionSize.class);
    }
}
