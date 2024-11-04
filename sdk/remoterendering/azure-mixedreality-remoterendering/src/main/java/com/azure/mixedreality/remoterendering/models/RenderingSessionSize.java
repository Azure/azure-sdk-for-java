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
     * Creates a new instance of {@link RenderingSessionSize} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link RenderingSessionSize} which doesn't
     * have a String enum value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public RenderingSessionSize() {

    }

    /**
     * Creates or finds a SessionSize from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding SessionSize.
     */
    public static RenderingSessionSize fromString(String name) {
        return fromString(name, RenderingSessionSize.class);
    }

    /**
     * Gets known SessionSize values.
     *
     * @return known SessionSize values.
     */
    public static Collection<RenderingSessionSize> values() {
        return values(RenderingSessionSize.class);
    }
}
