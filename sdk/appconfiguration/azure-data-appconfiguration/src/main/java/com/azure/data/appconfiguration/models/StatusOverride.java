// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Overrides the feature flag status when a variant is selected.
 */
public final class StatusOverride extends ExpandableStringEnum<StatusOverride> {
    /** Does not override the feature flag status. */
    public static final StatusOverride NONE = fromString("None");

    /** Overrides the feature flag status to enabled. */
    public static final StatusOverride ENABLED = fromString("Enabled");

    /** Overrides the feature flag status to disabled. */
    public static final StatusOverride DISABLED = fromString("Disabled");

    /**
     * Creates a status override.
     * @deprecated Use {@link #fromString(String)} instead.
     */
    @Deprecated
    public StatusOverride() {
    }

    /**
     * Creates or finds a status override from its wire value.
     * @param name The wire value.
     * @return The status override.
     */
    public static StatusOverride fromString(String name) {
        return fromString(name, StatusOverride.class);
    }

    /**
     * Gets the known status overrides.
     * @return The known status overrides.
     */
    public static Collection<StatusOverride> values() {
        return values(StatusOverride.class);
    }
}
