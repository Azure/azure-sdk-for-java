// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** The status of a rendering session. */
public final class RenderingSessionStatus extends ExpandableStringEnum<RenderingSessionStatus> {
    /** Static value Error for SessionStatus. */
    public static final RenderingSessionStatus ERROR = fromString("Error");

    /** Static value Expired for SessionStatus. */
    public static final RenderingSessionStatus EXPIRED = fromString("Expired");

    /** Static value Starting for SessionStatus. */
    public static final RenderingSessionStatus STARTING = fromString("Starting");

    /** Static value Ready for SessionStatus. */
    public static final RenderingSessionStatus READY = fromString("Ready");

    /** Static value Stopped for SessionStatus. */
    public static final RenderingSessionStatus STOPPED = fromString("Stopped");

    /**
     * Creates a new instance of {@link RenderingSessionStatus} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link RenderingSessionStatus} which doesn't
     * have a String enum value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public RenderingSessionStatus() {

    }

    /**
     * Creates or finds a SessionStatus from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding SessionStatus.
     */
    public static RenderingSessionStatus fromString(String name) {
        return fromString(name, RenderingSessionStatus.class);
    }

    /**
     * Gets known SessionStatus values.
     *
     * @return known SessionStatus values.
     */
    public static Collection<RenderingSessionStatus> values() {
        return values(RenderingSessionStatus.class);
    }
}

