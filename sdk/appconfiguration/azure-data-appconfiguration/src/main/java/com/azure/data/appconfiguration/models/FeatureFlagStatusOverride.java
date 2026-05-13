// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Determines if a variant should override the enabled status of the feature flag.
 */
public final class FeatureFlagStatusOverride extends ExpandableStringEnum<FeatureFlagStatusOverride> {
    /**
     * No override.
     */
    public static final FeatureFlagStatusOverride NONE = fromString("None");

    /**
     * Override to enabled.
     */
    public static final FeatureFlagStatusOverride ENABLED = fromString("Enabled");

    /**
     * Override to disabled.
     */
    public static final FeatureFlagStatusOverride DISABLED = fromString("Disabled");

    /**
     * Creates a new instance of FeatureFlagStatusOverride value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public FeatureFlagStatusOverride() {
    }

    /**
     * Creates or finds a FeatureFlagStatusOverride from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding FeatureFlagStatusOverride.
     */
    public static FeatureFlagStatusOverride fromString(String name) {
        return fromString(name, FeatureFlagStatusOverride.class);
    }

    /**
     * Gets known FeatureFlagStatusOverride values.
     *
     * @return known FeatureFlagStatusOverride values.
     */
    public static Collection<FeatureFlagStatusOverride> values() {
        return values(FeatureFlagStatusOverride.class);
    }
}
